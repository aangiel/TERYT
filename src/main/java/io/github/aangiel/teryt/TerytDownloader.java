package io.github.aangiel.teryt;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.github.aangiel.teryt.ws.ITerytWs1;
import io.github.aangiel.teryt.ws.PlikKatalog;
import jodd.util.collection.MapEntry;
import lombok.Builder;
import lombok.Getter;
import net.lingala.zip4j.ZipFile;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.search.IndexDefinition;
import redis.clients.jedis.search.IndexOptions;
import redis.clients.jedis.search.Schema;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TerytDownloader {

    private static final ITerytWs1 terytClient = TerytClient.create("TestPubliczny", "1234abcd");
    private static final XMLGregorianCalendar dateTerc = terytClient.pobierzDateAktualnegoKatTerc();

    public Map<String, List<CsvLine>> download() throws IOException {


        PlikKatalog tercAdrFile = terytClient.pobierzKatalogTERCAdr(dateTerc);

        String base64Value = tercAdrFile.getPlikZawartosc().getValue();
        byte[] data = Base64.getDecoder().decode(base64Value);

        File zipTempFile = File.createTempFile("tercAdrFile-", ".zip");

        writeZipFile(data, zipTempFile);

        String fileName = String.format("%s.csv", tercAdrFile.getNazwaPliku().getValue());

        Path csvTempDirectory = Files.createTempDirectory("files");

        extractZipFile(zipTempFile, fileName, csvTempDirectory);

        var csvPath = Path.of(csvTempDirectory.toString(), fileName);

        return convertLines(csvPath);
    }

    void writeToRedis() throws IOException, InterruptedException {
        var client = new JedisPooled();


        var downloaded = download();


        Pipeline pipelined = new Jedis(client.getPool().getResource()).pipelined();

        Map<String, String> voivodeships = downloaded.get("V").stream()
                .filter(e -> "województwo".equals(e.extraName))
                .collect(Collectors.toMap(CsvLine::getVoivodeship, CsvLine::getName));

        Map<String, String> counties = downloaded.get("C").stream()
                .filter(e -> e.extraName.contains("powiat"))
                .collect(
                        Collectors.toMap(
                                e -> String.format("%s:%s", e.voivodeship, e.county),
                                TerytDownloader.CsvLine::getName)
                );

        Map<String, String> communities = downloaded.get("R").stream()
                .filter(Predicate.not(e -> "województwo".equals(e.extraName)))
                .filter(Predicate.not(e -> e.extraName.contains("powiat")))
                .map(e -> MapEntry.create(
                        String.format("%s:%s:%s", e.voivodeship, e.county, e.community),
                        e.getName()
                        ))
                .distinct()
                .collect(
                        Collectors.toMap(MapEntry::getKey, MapEntry::getValue)
                );

        var all = ImmutableList.<CsvLine>builder()
                .addAll(downloaded.get("V"))
                .addAll(downloaded.get("C"))
                .addAll(downloaded.get("R"));

        for (var v : all.build()) {
            pipelined.hset(v.getKey(), v.getValue(voivodeships, counties, communities));
        }
        pipelined.sync();


        Schema schema = new Schema()
                .addTextField("name", 10.0)
//                .addTextField("community", 7.5)
                .addTextField("county", 5.0)
                .addTextField("voivodeship", 1.0);

        IndexDefinition rule = new IndexDefinition(IndexDefinition.Type.HASH)
                .setPrefixes("terc:2022-01-01:");


        try {
            client.ftCreate("idx:terc:2022-01-01:name", IndexOptions.defaultOptions().setDefinition(rule), schema);
        } catch (JedisDataException e) {
            e.printStackTrace();
        }
    }

    private void extractZipFile(File zipTempFile, String fileName, Path csvTempDirectory) throws IOException {
        try (var zipFile = new ZipFile(zipTempFile)) {
            zipFile.extractFile(fileName, csvTempDirectory.toString());
        }
    }

    private void writeZipFile(byte[] data, File zipTempFile) {
        try (OutputStream stream = new FileOutputStream(zipTempFile)) {
            stream.write(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, List<TerytDownloader.CsvLine>> convertLines(Path csvPath) {
        try (var lines = Files.lines(csvPath)) {
            return lines.skip(1)
                    .filter(Predicate.not(String::isEmpty))
                    .map(TerytDownloader.CsvLine::create)
                    .collect(Collectors.groupingBy(
                            e -> {
                                if ("województwo".equals(e.extraName))
                                    return "V";
                                if (e.extraName.contains("powiat"))
                                    return "C";
                                return "R";
                            }
                    ));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }




    @Builder
    @Getter
    static class CsvLine implements Serializable {

        private final String voivodeship;
        private final String county;
        private final String community;
        private final String type;
        private final String name;
        private final String extraName;
        private final String stateDate;

        String getKey() {
            var result = new StringJoiner(":")
                    .add("terc")
                    .add(this.stateDate)
                    .add(this.voivodeship);

            if (!this.county.isEmpty()) result.add(this.county);
            if (!this.community.isEmpty()) result.add(this.community);
            if (!this.type.isEmpty()) result.add(this.type);

            return result.toString();
        }

        Map<String, String> getValue(Map<String, String> voivodeships, Map<String, String> counties, Map<String, String> communities) {
            var voivodeship = voivodeships.get(this.voivodeship);
            var county = counties.get(String.format("%s:%s", this.voivodeship, this.county));
            var community = communities.get(String.format("%s:%s:%s", this.voivodeship, this.county, this.community));

            county = county == null ? "" : county;
            community = community == null ? "" : community;

            return ImmutableMap.of(
                    "voivodeship", voivodeship.toLowerCase(),
                    "county", county,
                    "name", this.name,
                    "extraName", this.extraName,
                    "community", community
            );
        }


        static CsvLine create(String line) {
            var fields = line.split(";");

            return CsvLine.builder()
                    .voivodeship(fields[0])
                    .county(fields[1])
                    .community(fields[2])
                    .type(fields[3])
                    .name(fields[4])
                    .extraName(fields[5])
                    .stateDate(fields[6])
                    .build();
        }
    }
}
