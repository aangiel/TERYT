package io.github.aangiel.teryt.teryt;

import com.google.common.collect.ImmutableMap;
import io.github.aangiel.teryt.Constants;
import io.github.aangiel.teryt.ws.ITerytWs1;
import io.github.aangiel.teryt.ws.PlikKatalog;
import lombok.Getter;
import lombok.extern.log4j.Log4j;
import net.lingala.zip4j.ZipFile;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Log4j
@Getter
public class TerytDownloader {

    private static final ITerytWs1 terytClient = TerytClient.create(Constants.TERYT_USER, Constants.TERYT_PASS);

    private Map<String, XMLGregorianCalendar> dates;
    private Map<String, String> unitTypeDictionary;
    private List<SIMCTypeDictionary> downloadSimcTypeDictionary;
    private Map<String, String> streetPropertiesDictionary;

    private final ImmutableMap.Builder<String, Map<String, String>> downloadedCatalogs = ImmutableMap.builder();

    public Map<String, Map<String, String>> getDownloadedCatalogs() {
        return downloadedCatalogs.build();
    }

    public void downloadData() throws IOException {

        downloadDates();

        downloadDictionaries();

        downloadCatalogs();
    }


    private void downloadDictionaries() {
        this.unitTypeDictionary = downloadUnitTypeDictionary();
        log.trace(this.unitTypeDictionary);

        this.downloadSimcTypeDictionary = downloadSimcTypeDictionary();
        log.trace(this.downloadSimcTypeDictionary);

        this.streetPropertiesDictionary = downloadStreetPropertiesDictionary();
        log.trace(this.streetPropertiesDictionary);
    }

    private Map<String, String> downloadStreetPropertiesDictionary() {
        var dictionary = terytClient.pobierzSlownikCechULIC().getString();

        var resultBuilder = ImmutableMap.<String, String>builder();

        for (var entry : dictionary) {
            var split = entry.split("\s*,\s*");

            resultBuilder.put(split[1], split[0]);
        }

        return resultBuilder.build();
    }

    private List<SIMCTypeDictionary> downloadSimcTypeDictionary() {
        return terytClient
                .pobierzSlownikRodzajowSIMC(this.dates.get(Constants.TERYT_CATALOG_SIMC))
                .getRodzajMiejscowosci()
                .stream()
                .map(SIMCTypeDictionary::create)
                .toList();
    }

    private Map<String, String> downloadUnitTypeDictionary() {
        return terytClient.pobierzSlownikRodzajowJednostek()
                .getString()
                .stream()
                .collect(Collectors.toMap(k -> k.split("\s*,\s*")[0], v -> v.split("\s*,\s*")[1]));
    }

    private void downloadDates() {
        var resultBuilder = ImmutableMap.<String, XMLGregorianCalendar>builder();

        resultBuilder.put(Constants.TERYT_CATALOG_TERC, terytClient.pobierzDateAktualnegoKatTerc());
        resultBuilder.put(Constants.TERYT_CATALOG_NTS, terytClient.pobierzDateAktualnegoKatNTS());
        resultBuilder.put(Constants.TERYT_CATALOG_SIMC, terytClient.pobierzDateAktualnegoKatSimc());
        resultBuilder.put(Constants.TERYT_CATALOG_ULIC, terytClient.pobierzDateAktualnegoKatUlic());

        var result = resultBuilder.build();
        log.trace(result);
        this.dates = resultBuilder.build();
    }

    private void downloadCatalogs() throws IOException {
        var tercAddressesCatalog = download(terytClient.pobierzKatalogTERCAdr(dates.get(Constants.TERYT_CATALOG_TERC)), "terc-address");
        this.downloadedCatalogs.putAll(tercAddressesCatalog);
        log.trace(tercAddressesCatalog);

        var tercCatalog = download(terytClient.pobierzKatalogTERC(dates.get(Constants.TERYT_CATALOG_TERC)), "terc");
        this.downloadedCatalogs.putAll(tercCatalog);
        log.trace(tercCatalog);

        var ntsCatalog = download(terytClient.pobierzKatalogNTS(dates.get(Constants.TERYT_CATALOG_NTS)), "nts");
        this.downloadedCatalogs.putAll(ntsCatalog);
        log.trace(ntsCatalog);

        var simcAddressesCatalog = download(terytClient.pobierzKatalogSIMCAdr(dates.get(Constants.TERYT_CATALOG_SIMC)), "simc-address");
        this.downloadedCatalogs.putAll(simcAddressesCatalog);
        log.trace(simcAddressesCatalog);

        var simcCatalog = download(terytClient.pobierzKatalogSIMC(dates.get(Constants.TERYT_CATALOG_SIMC)), "simc");
        this.downloadedCatalogs.putAll(simcCatalog);
        log.trace(simcCatalog);

        var simcStatCatalog = download(terytClient.pobierzKatalogSIMCStat(dates.get(Constants.TERYT_CATALOG_SIMC)), "simc-stat");
        this.downloadedCatalogs.putAll(simcStatCatalog);
        log.trace(simcStatCatalog);

        var ulicCatalog = download(terytClient.pobierzKatalogULIC(dates.get(Constants.TERYT_CATALOG_ULIC)), "ulic");
        this.downloadedCatalogs.putAll(ulicCatalog);
        log.trace(ulicCatalog);

        var ulicAddressesCatalog = download(terytClient.pobierzKatalogULICAdr(dates.get(Constants.TERYT_CATALOG_ULIC)), "ulic-address");
        this.downloadedCatalogs.putAll(ulicAddressesCatalog);
        log.trace(ulicAddressesCatalog);

        var ulicWithoutDistrictsCatalog = download(terytClient.pobierzKatalogULICBezDzielnic(dates.get(Constants.TERYT_CATALOG_ULIC)), "ulic-without-district");
        this.downloadedCatalogs.putAll(ulicWithoutDistrictsCatalog);
        log.trace(ulicWithoutDistrictsCatalog);

        var townTypeCatalog = download(terytClient.pobierzKatalogWMRODZ(dates.get(Constants.TERYT_CATALOG_SIMC)), "type");
        this.downloadedCatalogs.putAll(townTypeCatalog);
        log.trace(townTypeCatalog);
    }


    private Map<String, Map<String, String>> download(PlikKatalog catalogFile, String catalogName) throws IOException {


//        PlikKatalog tercAdrFile = terytClient.pobierzKatalogTERCAdr(dates.get(Constants.TERYT_CATALOG_TERC));

        var base64Value = catalogFile.getPlikZawartosc().getValue();
        var data = Base64.getDecoder().decode(base64Value);
        var zipTempFile = File.createTempFile("temp-", ".zip");
        writeZipFile(data, zipTempFile);

        var fileName = String.format("%s.csv", catalogFile.getNazwaPliku().getValue());
        var csvTempDirectory = Files.createTempDirectory("files");
        extractZipFile(zipTempFile, fileName, csvTempDirectory);

        var csvPath = Path.of(csvTempDirectory.toString(), fileName);
        log.debug(csvPath);
        var convertedLines = convertLines(csvPath, catalogName);

        return convertedLines.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        v -> v.getValue().getProperties()
                ));
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

    private Map<String, CsvLine> convertLines(Path csvPath, String catalogName) throws IOException {

        try (var lines = Files.lines(csvPath)) {

            var list = lines.distinct().toList();

            var header = list.stream().limit(1).toList().get(0);
            var headers = new String(header.getBytes(StandardCharsets.UTF_8)).split("\s*;\s*");
            log.debug(header);

            var list2 = list.stream().skip(1)
                    .filter(Predicate.not(String::isEmpty))
                    .map(e -> CsvLine.create(catalogName, e, headers))
                    .toList();

            var resultBuilder = ImmutableMap.<String, CsvLine>builder();

            for (var element : list2) {
                resultBuilder.put(element.getKey(), element);
            }

            var result = resultBuilder.buildKeepingLast();
            log.debug(result.entrySet().stream().limit(5).toList());

            return result;

        }
    }


}
