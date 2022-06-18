package io.github.aangiel.teryt.teryt;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import io.github.aangiel.teryt.Constants;
import io.github.aangiel.teryt.ws.ITerytWs1;
import io.github.aangiel.teryt.ws.PlikKatalog;
import lombok.Getter;
import lombok.extern.log4j.Log4j;
import net.lingala.zip4j.ZipFile;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Log4j
@Getter
public class TerytDownloader {

    private static final ITerytWs1 terytClient = TerytClient.create(Constants.TERYT_USER, Constants.TERYT_PASS);

    private Map<String, XMLGregorianCalendar> dates;
    private Map<String, String> unitTypeDictionary;
    private List<SIMCTypeDictionary> downloadSimcTypeDictionary;
    private Map<String, String> streetPropertiesDictionary;

    private final TerytNode downloadedRoot = TerytNode.createRoot();

    public TerytNode getDownloadedRoot() {
        return downloadedRoot;
    }

    public void downloadData() throws IOException, CsvException {

        downloadDates();

        downloadDictionaries();

//        downloadCatalogs();
    }


    private void downloadDictionaries() {

        var dictionaries = getDownloadedRoot().addChild("dictionaries");

        downloadUnitTypeDictionary(dictionaries);
        downloadSimcTypeDictionary(dictionaries);
        downloadStreetPropertiesDictionary(dictionaries);
    }

    private void downloadStreetPropertiesDictionary(TerytNode dictionaries) {
        var dictionary = dictionaries.addChild("streetProperties");

        var entries = terytClient.pobierzSlownikCechULIC()
                .getString()
                .stream()
                .map(e -> e.split("\s*,\s*"))
                .toList();

        for (var entry : entries) {
            dictionary.addChild(entry[0], entry[1]);
        }

    }

    private void downloadSimcTypeDictionary(TerytNode dictionaries) {
        var dictionary = dictionaries.addChild("simcType");



        var dates = getDownloadedRoot().getChild("dates").orElseThrow();
        var date = dates.getChild("simc").orElseThrow();


        var entries = terytClient.pobierzSlownikRodzajowSIMC(date.getDate())
                .getRodzajMiejscowosci()
                .stream()
                .toList();

        for (var entry : entries) {
            dictionary.addChild(entry.getNazwa().getValue(),
                    entry.getSymbol().getValue(),
                    entry.getOpis().getValue());
        }
    }

    private void downloadUnitTypeDictionary(TerytNode dictionaries) {

        var dictionary = dictionaries.addChild("unitType");

        List<String[]> entries = terytClient.pobierzSlownikRodzajowJednostek()
                .getString()
                .stream()
                .map(e -> e.split("\s*,\s*"))
                .toList();

        for (var entry : entries) {
            dictionary.addChild(entry[1], entry[0]);
        }
    }

    private void downloadDates() {

        var dates = this.downloadedRoot.addChild("dates");

//        var terc = dates.addChild(Constants.TERYT_CATALOG_TERC);
//        var nts = dates.addChild(Constants.TERYT_CATALOG_NTS);
//        var simc = dates.addChild(Constants.TERYT_CATALOG_SIMC);
//        var ulic = dates.addChild(Constants.TERYT_CATALOG_ULIC);

        dates.addChild("terc", terytClient.pobierzDateAktualnegoKatTerc());
        dates.addChild("nts", terytClient.pobierzDateAktualnegoKatNTS());
        dates.addChild("simc", terytClient.pobierzDateAktualnegoKatSimc());
        dates.addChild("ulic", terytClient.pobierzDateAktualnegoKatUlic());
    }

    private void downloadCatalogs() throws IOException, CsvException {
        var tercAddressesCatalog = download(terytClient.pobierzKatalogTERCAdr(dates.get(Constants.TERYT_CATALOG_TERC)), "terc-address");
//        this.downloadedCatalogs.putAll(tercAddressesCatalog);
        log.trace(tercAddressesCatalog);

        var tercCatalog = download(terytClient.pobierzKatalogTERC(dates.get(Constants.TERYT_CATALOG_TERC)), "terc");
//        this.downloadedCatalogs.putAll(tercCatalog);
        log.trace(tercCatalog);

        var ntsCatalog = download(terytClient.pobierzKatalogNTS(dates.get(Constants.TERYT_CATALOG_NTS)), "nts");
//        this.downloadedCatalogs.putAll(ntsCatalog);
        log.trace(ntsCatalog);

        var simcAddressesCatalog = download(terytClient.pobierzKatalogSIMCAdr(dates.get(Constants.TERYT_CATALOG_SIMC)), "simc-address");
//        this.downloadedCatalogs.putAll(simcAddressesCatalog);
        log.trace(simcAddressesCatalog);

        var simcCatalog = download(terytClient.pobierzKatalogSIMC(dates.get(Constants.TERYT_CATALOG_SIMC)), "simc");
//        this.downloadedCatalogs.putAll(simcCatalog);
        log.trace(simcCatalog);

        var simcStatCatalog = download(terytClient.pobierzKatalogSIMCStat(dates.get(Constants.TERYT_CATALOG_SIMC)), "simc-stat");
//        this.downloadedCatalogs.putAll(simcStatCatalog);
        log.trace(simcStatCatalog);

        var ulicCatalog = download(terytClient.pobierzKatalogULIC(dates.get(Constants.TERYT_CATALOG_ULIC)), "ulic");
//        this.downloadedCatalogs.putAll(ulicCatalog);
        log.trace(ulicCatalog);

        var ulicAddressesCatalog = download(terytClient.pobierzKatalogULICAdr(dates.get(Constants.TERYT_CATALOG_ULIC)), "ulic-address");
//        this.downloadedCatalogs.putAll(ulicAddressesCatalog);
        log.trace(ulicAddressesCatalog);

        var ulicWithoutDistrictsCatalog = download(terytClient.pobierzKatalogULICBezDzielnic(dates.get(Constants.TERYT_CATALOG_ULIC)), "ulic-without-district");
//        this.downloadedCatalogs.putAll(ulicWithoutDistrictsCatalog);
        log.trace(ulicWithoutDistrictsCatalog);

        var townTypeCatalog = download(terytClient.pobierzKatalogWMRODZ(dates.get(Constants.TERYT_CATALOG_SIMC)), "type");
//        this.downloadedCatalogs.putAll(townTypeCatalog);
        log.trace(townTypeCatalog);
    }


    private Map<String, String[]> download(PlikKatalog catalogFile, String catalogName) throws IOException, CsvException {


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

        var reader = new InputStreamReader(new FileInputStream(csvPath.toFile()));
        var csvReader = new CSVReader(reader);
        var readFile = csvReader.readAll();

        return null;
//        return ImmutableMap.of(catalogName, readFile);
//        return
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
