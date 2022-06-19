package io.github.aangiel.teryt.teryt;

import com.opencsv.exceptions.CsvException;
import io.github.aangiel.teryt.Constants;
import io.github.aangiel.teryt.ws.ITerytWs1;
import io.github.aangiel.teryt.ws.PlikKatalog;
import lombok.extern.log4j.Log4j;
import net.lingala.zip4j.ZipFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Log4j
public class TerytDownloader {

    private static final ITerytWs1 terytClient = TerytClient.create(Constants.TERYT_USER, Constants.TERYT_PASS);


    public TerytNode downloadData() throws IOException, CsvException {

        var root = TerytNode.createRoot();
        var dates = downloadDates(root);
        downloadDictionaries(root, dates);
        downloadCatalogs(root, dates);

        return root;
    }


    private void downloadDictionaries(TerytNode root, TerytNode dates) {

        var dictionaries = root.addChild(TerytNode.builder().code("dictionaries"));

        downloadUnitTypeDictionary(dictionaries);
        downloadSimcTypeDictionary(dictionaries, dates);
        downloadStreetPropertiesDictionary(dictionaries);
    }

    private void downloadStreetPropertiesDictionary(TerytNode dictionaries) {

        var dictionary = dictionaries.addChild(TerytNode.builder().code("streetProperties"));

        var entries = terytClient.pobierzSlownikCechULIC()
                .getString()
                .stream()
                .map(e -> e.split("\s*,\s*"))
                .toList();

        for (var entry : entries) {
            dictionary.addChild(TerytNode.builder().name(entry[0]).code(entry[1]));
        }

    }

    private void downloadSimcTypeDictionary(TerytNode dictionaries, TerytNode dates) {
        var dictionaryBuilder = TerytNode.builder().code("simcType");
        var dictionary = dictionaries.addChild(dictionaryBuilder);

        var date = dates.getChildByCode("simc");

        var entries = terytClient.pobierzSlownikRodzajowSIMC(date.getDate())
                .getRodzajMiejscowosci()
                .stream()
                .toList();

        for (var entry : entries) {
            var child = TerytNode.builder()
                    .name(entry.getNazwa().getValue())
                    .code(entry.getSymbol().getValue())
                    .description(entry.getOpis().getValue());

            dictionary.addChild(child);
        }
    }

    private void downloadUnitTypeDictionary(TerytNode dictionaries) {

        var dictionary = dictionaries.addChild(TerytNode.builder().code("unitType"));

        List<String[]> entries = terytClient.pobierzSlownikRodzajowJednostek()
                .getString()
                .stream()
                .map(e -> e.split("\s*,\s*"))
                .toList();

        for (var entry : entries) {
            dictionary.addChild(TerytNode.builder().name(entry[1]).code(entry[0]));
        }
    }

    private TerytNode downloadDates(TerytNode root) {

        var dates = root.addChild(TerytNode.builder().code("dates"));

        dates.addChild(TerytNode.builder().code("terc").date(terytClient.pobierzDateAktualnegoKatTerc()));
        dates.addChild(TerytNode.builder().code("nts").date(terytClient.pobierzDateAktualnegoKatNTS()));
        dates.addChild(TerytNode.builder().code("simc").date(terytClient.pobierzDateAktualnegoKatSimc()));
        dates.addChild(TerytNode.builder().code("ulic").date(terytClient.pobierzDateAktualnegoKatUlic()));

        return dates;
    }

    private void downloadCatalogs(TerytNode root, TerytNode dates) throws IOException {

        var catalogs = root.addChild(TerytNode.builder().code("catalogs"));


        var tercDate = dates.getChildByCode("terc").getDate();
        var ntsDate = dates.getChildByCode("nts").getDate();
        var simcDate = dates.getChildByCode("simc").getDate();
        var ulicDate = dates.getChildByCode("ulic").getDate();


        downloadCatalog(catalogs, terytClient.pobierzKatalogTERCAdr(tercDate), "terc-address");
        downloadCatalog(catalogs, terytClient.pobierzKatalogTERC(tercDate), "terc");

        downloadCatalog(catalogs, terytClient.pobierzKatalogSIMCAdr(simcDate), "simc-address");
        downloadCatalog(catalogs, terytClient.pobierzKatalogSIMC(simcDate), "simc");
        downloadCatalog(catalogs, terytClient.pobierzKatalogSIMCStat(simcDate), "simc-stat");



//        var tercCatalog = download(terytClient.pobierzKatalogTERC(dates.get(Constants.TERYT_CATALOG_TERC)), "terc");
//        this.downloadedCatalogs.putAll(tercCatalog);
//        log.trace(tercCatalog);
//
//        var ntsCatalog = download(terytClient.pobierzKatalogNTS(dates.get(Constants.TERYT_CATALOG_NTS)), "nts");
//        this.downloadedCatalogs.putAll(ntsCatalog);
//        log.trace(ntsCatalog);
//
//        var simcAddressesCatalog = download(terytClient.pobierzKatalogSIMCAdr(dates.get(Constants.TERYT_CATALOG_SIMC)), "simc-address");
//        this.downloadedCatalogs.putAll(simcAddressesCatalog);
//        log.trace(simcAddressesCatalog);
//
//        var simcCatalog = download(terytClient.pobierzKatalogSIMC(dates.get(Constants.TERYT_CATALOG_SIMC)), "simc");
//        this.downloadedCatalogs.putAll(simcCatalog);
//        log.trace(simcCatalog);
//
//        var simcStatCatalog = download(terytClient.pobierzKatalogSIMCStat(dates.get(Constants.TERYT_CATALOG_SIMC)), "simc-stat");
//        this.downloadedCatalogs.putAll(simcStatCatalog);
//        log.trace(simcStatCatalog);
//
//        var ulicCatalog = download(terytClient.pobierzKatalogULIC(dates.get(Constants.TERYT_CATALOG_ULIC)), "ulic");
//        this.downloadedCatalogs.putAll(ulicCatalog);
//        log.trace(ulicCatalog);
//
//        var ulicAddressesCatalog = download(terytClient.pobierzKatalogULICAdr(dates.get(Constants.TERYT_CATALOG_ULIC)), "ulic-address");
//        this.downloadedCatalogs.putAll(ulicAddressesCatalog);
//        log.trace(ulicAddressesCatalog);
//
//        var ulicWithoutDistrictsCatalog = download(terytClient.pobierzKatalogULICBezDzielnic(dates.get(Constants.TERYT_CATALOG_ULIC)), "ulic-without-district");
//        this.downloadedCatalogs.putAll(ulicWithoutDistrictsCatalog);
//        log.trace(ulicWithoutDistrictsCatalog);
//
//        var townTypeCatalog = download(terytClient.pobierzKatalogWMRODZ(dates.get(Constants.TERYT_CATALOG_SIMC)), "type");
//        this.downloadedCatalogs.putAll(townTypeCatalog);
//        log.trace(townTypeCatalog);
    }


    private void downloadCatalog(TerytNode catalogs, PlikKatalog catalogFile, String catalogName) throws IOException {


        var catalog = catalogs.addChild(TerytNode.builder().code(catalogName));
        var lines = getCsvLines(catalogFile);

        var dates = lines.stream()
                .map(l -> l[l.length - 1])
                .distinct()
                .toList();

        for (var date : dates) {
            catalog.addChild(TerytNode.builder().code(date));
        }

        if (catalogName.startsWith("terc")) downloadTercCatalog(catalog, lines);
        if ("simc-stat".equals(catalogName)) downloadSimcStatCatalog(catalog, lines);
        if ("simc".equals(catalogName) || "simc-address".equals(catalogName)) downloadSimcCatalog(catalog, lines);
    }

    private void downloadSimcStatCatalog(TerytNode catalog, List<String[]> lines) {
//        WOJ;POW;GMI;RODZ_GMI;RM;    MZ;NMST;NMSK;SYMBM;SYMSTAT;      NAZWA;SYM;SYMPOD;STAN_NA

        for (var line : lines) {
            catalog.getChildByCode(line[line.length - 1])
                    .addChildIfNotExists(TerytNode.builder().code(line[0]).extraName("województwo"))
                    .addChildIfNotExists(TerytNode.builder().code(line[1]).extraName("powiat"))
                    .addChildIfNotExists(TerytNode.builder().code(line[2]).extraName("gmina"))
                    .addChildIfNotExists(TerytNode.builder().code(line[3]).extraName("rodzaj gminy"))
                    .addChildIfNotExists(TerytNode.builder().code(line[4]).extraName("rodzaj miejscowości"))
                    .addChildIfNotExists(TerytNode.builder().code(line[5]).extraName("występowanie nazwy zwyczajowej"))
                    .addChildIfNotExists(TerytNode.builder().code(line[12]).extraName("symbol podstawowy"))
                    .addChildIfNotExists(TerytNode.builder().code(line[11]).extraName("symbol"))
                    .addChildIfNotExists(TerytNode.builder().code(line[9]).extraName("identyfikator miejscowości statystycznej"))
                    .addChildIfNotExists(TerytNode.builder().code(line[6]).extraName("numer miejscowości statystycznej"))
                    .addChildIfNotExists(TerytNode.builder().code(line[7]).extraName("numer miejscowości składowej"))
                    .addChildIfNotExists(TerytNode.builder().code(line[8]).name(line[10]).extraName("określenie miejscowości"))
                    ;
        }
    }

    private void downloadSimcCatalog(TerytNode catalog, List<String[]> lines) {
        //WOJ;POW;GMI;RODZ_GMI;RM;MZ;NAZWA;SYM;SYMPOD;STAN_NA
        for (var line : lines) {
            catalog.getChildByCode(line[line.length - 1])
                    .addChildIfNotExists(TerytNode.builder().code(line[0]).extraName("województwo"))
                    .addChildIfNotExists(TerytNode.builder().code(line[1]).extraName("powiat"))
                    .addChildIfNotExists(TerytNode.builder().code(line[2]).extraName("gmina"))
                    .addChildIfNotExists(TerytNode.builder().code(line[3]).extraName("rodzaj gminy"))
                    .addChildIfNotExists(TerytNode.builder().code(line[4]).extraName("rodzaj miejscowości"))
                    .addChildIfNotExists(TerytNode.builder().code(line[5]).extraName("występowanie nazwy zwyczajowej"))
                    .addChildIfNotExists(TerytNode.builder().code(line[8]).extraName("symbol podstawowy"))
                    .addChildIfNotExists(TerytNode.builder().code(line[7]).name(line[6]).extraName("symbol"));
        }
    }

    private void downloadTercCatalog(TerytNode catalog, List<String[]> lines) {
        var grouped = lines.stream()
                .collect(Collectors.groupingBy(l -> {
                    if (l[5].equals("województwo")) return "V";
                    if (l[5].contains("powiat")) return "C";
                    return "R";
                }));

        for (var voivodeship : grouped.get("V")) {
            catalog.getChildByCode(voivodeship[6])
                    .addChild(TerytNode.builder().code(voivodeship[0]).name(voivodeship[4]).extraName(voivodeship[5]));
        }

        for (var county : grouped.get("C")) {
            catalog.getChildByCode(county[6])
                    .getChildByCode(county[0])
                    .addChild(TerytNode.builder().code(county[1]).name(county[4]).extraName(county[5]));
        }

        for (var community : grouped.get("R")) {
            catalog.getChildByCode(community[6])
                    .getChildByCode(community[0])
                    .getChildByCode(community[1])
                    .addChild(TerytNode.builder().code(community[2]).name(community[4]));
        }

        for (var communityType : grouped.get("R")) {
            catalog.getChildByCode(communityType[6])
                    .getChildByCode(communityType[0])
                    .getChildByCode(communityType[1])
                    .getChildByCode(communityType[2])
                    .addChild(TerytNode.builder().code(communityType[3]).name(communityType[5]));
        }
    }

    private List<String[]> getCsvLines(PlikKatalog catalogFile) throws IOException {
        var base64Value = catalogFile.getPlikZawartosc().getValue();
        var data = Base64.getDecoder().decode(base64Value);
        var zipTempFile = File.createTempFile("temp-", ".zip");
        writeZipFile(data, zipTempFile);

        var fileName = String.format("%s.csv", catalogFile.getNazwaPliku().getValue());
        var csvTempDirectory = Files.createTempDirectory("files");
        extractZipFile(zipTempFile, fileName, csvTempDirectory);

        var csvPath = Path.of(csvTempDirectory.toString(), fileName);

        try (var lines = Files.lines(csvPath, StandardCharsets.UTF_8)) {
            return lines.skip(1)
                    .filter(Predicate.not(String::isEmpty))
                    .map(l -> l.split("\s*;\s*"))
                    .toList();
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

}
