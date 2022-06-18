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

@Log4j
public class TerytDownloader {

    private static final ITerytWs1 terytClient = TerytClient.create(Constants.TERYT_USER, Constants.TERYT_PASS);


//    public TerytNode getDownloadedRoot() {
//        return downloadedRoot;
//    }

    public TerytNode downloadData() throws IOException, CsvException {

        var root = TerytNode.createRoot();
        var dates = downloadDates(root);
        downloadDictionaries(root, dates);
        downloadCatalogs(root, dates);

        return root;
    }


    private void downloadDictionaries(TerytNode root, TerytNode dates) {

        var dictionaries = root.addOrGetChild("dictionaries");

        downloadUnitTypeDictionary(dictionaries);
        downloadSimcTypeDictionary(dictionaries, dates);
        downloadStreetPropertiesDictionary(dictionaries);
    }

    private void downloadStreetPropertiesDictionary(TerytNode dictionaries) {
        var dictionary = dictionaries.addOrGetChild("streetProperties");

        var entries = terytClient.pobierzSlownikCechULIC()
                .getString()
                .stream()
                .map(e -> e.split("\s*,\s*"))
                .toList();

        for (var entry : entries) {
            dictionary.addOrGetChild(entry[0], entry[1]);
        }

    }

    private void downloadSimcTypeDictionary(TerytNode dictionaries, TerytNode dates) {
        var dictionary = dictionaries.addOrGetChild("simcType");

        var date = dates.addOrGetChild("simc");

        var entries = terytClient.pobierzSlownikRodzajowSIMC(date.getDate())
                .getRodzajMiejscowosci()
                .stream()
                .toList();

        for (var entry : entries) {
            dictionary.addOrGetChild(entry.getNazwa().getValue(),
                    entry.getSymbol().getValue(),
                    entry.getOpis().getValue());
        }
    }

    private void downloadUnitTypeDictionary(TerytNode dictionaries) {

        var dictionary = dictionaries.addOrGetChild("unitType");

        List<String[]> entries = terytClient.pobierzSlownikRodzajowJednostek()
                .getString()
                .stream()
                .map(e -> e.split("\s*,\s*"))
                .toList();

        for (var entry : entries) {
            dictionary.addOrGetChild(entry[1], entry[0]);
        }
    }

    private TerytNode downloadDates(TerytNode root) {

        var dates = root.addOrGetChild("dates");

        dates.addOrGetChild("terc", terytClient.pobierzDateAktualnegoKatTerc());
        dates.addOrGetChild("nts", terytClient.pobierzDateAktualnegoKatNTS());
        dates.addOrGetChild("simc", terytClient.pobierzDateAktualnegoKatSimc());
        dates.addOrGetChild("ulic", terytClient.pobierzDateAktualnegoKatUlic());

        return dates;
    }

    private void downloadCatalogs(TerytNode root, TerytNode dates) throws IOException, CsvException {

        var catalogs = root.addOrGetChild("catalogs");


        var tercDate = dates.addOrGetChild("terc").getDate();
        var ntsDate = dates.addOrGetChild("nts").getDate();
        var simcDate = dates.addOrGetChild("simc").getDate();
        var ulicDate = dates.addOrGetChild("ulic").getDate();


        downloadTercCatalog(catalogs, terytClient.pobierzKatalogTERCAdr(tercDate), "terc-address");

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


    private void downloadTercCatalog(TerytNode catalogs, PlikKatalog catalogFile, String catalogName) throws IOException, CsvException {


        var catalog = catalogs.addOrGetChild(catalogName);
        var lines = getCsvLines(catalogFile);

        TerytNode currentVoivodeship = null;
        TerytNode currentCounty = null;
        TerytNode currentTown = null;

        for (var line : lines) {

            var voivodeship = line[0];
            var county = line[1];
            var community = line[2];
            var type = line[3];
            var name = line[4];
            var extraName = line[5];
            var stateDate = line[6];

            var date = catalog.addOrGetChild(stateDate);


            if ("województwo".equals(extraName)) {
                currentVoivodeship = date.addOrGetChild(name, voivodeship);
            } else if ("powiat".equals(extraName)) {
                assert currentVoivodeship != null;
                currentCounty = currentVoivodeship.addOrGetChild(name, county);
            } else {
                assert currentVoivodeship != null;
                assert currentCounty != null;
                currentTown = currentCounty.addOrGetChild(name, community);
                currentTown.addOrGetChild(extraName, type);
            }

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
