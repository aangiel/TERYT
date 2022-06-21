package io.github.aangiel.teryt.teryt;

import com.opencsv.bean.CsvToBeanBuilder;
import io.github.aangiel.teryt.ws.PlikKatalog;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import net.lingala.zip4j.ZipFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;

@Log4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, staticName = "create")
public class TerytDownloader {


//    private void downloadDictionaries(TerytNode root, TerytNode dates) {
//
//        var dictionaries = root.addChild(TerytNode.builder().code("dictionaries"));
//
//        downloadUnitTypeDictionary(dictionaries);
//        downloadSimcTypeDictionary(dictionaries, dates);
//        downloadStreetPropertiesDictionary(dictionaries);
//    }

//    private void downloadStreetPropertiesDictionary(TerytNode dictionaries) {
//
//        var dictionary = dictionaries.addChild(TerytNode.builder().code("streetProperties"));
//
//        var entries = terytClient.pobierzSlownikCechULIC()
//                .getString()
//                .stream()
//                .map(e -> e.split("\s*,\s*"))
//                .toList();
//
//        for (var entry : entries) {
//            dictionary.addChild(TerytNode.builder().name(entry[0]).code(entry[1]));
//        }
//
//    }

//    private void downloadSimcTypeDictionary(TerytNode dictionaries, TerytNode dates) {
//        var dictionaryBuilder = TerytNode.builder().code("simcType");
//        var dictionary = dictionaries.addChild(dictionaryBuilder);
//
//        var date = dates.getChildByCode("simc");
//
//        var entries = terytClient.pobierzSlownikRodzajowSIMC(date.getDate())
//                .getRodzajMiejscowosci()
//                .stream()
//                .toList();
//
//        for (var entry : entries) {
//            var child = TerytNode.builder()
//                    .name(entry.getNazwa().getValue())
//                    .code(entry.getSymbol().getValue())
//                    .description(entry.getOpis().getValue());
//
//            dictionary.addChild(child);
//        }
//    }

//    private void downloadUnitTypeDictionary(TerytNode dictionaries) {
//
//        var dictionary = dictionaries.addChild(TerytNode.builder().code("unitType"));
//
//        List<String[]> entries = terytClient.pobierzSlownikRodzajowJednostek()
//                .getString()
//                .stream()
//                .map(e -> e.split("\s*,\s*"))
//                .toList();
//
//        for (var entry : entries) {
//            dictionary.addChild(TerytNode.builder().name(entry[1]).code(entry[0]));
//        }
//    }

//    private TerytNode downloadDates(TerytNode root) {
//
//        var dates = root.addChild(TerytNode.builder().code("dates"));
//
//        dates.addChild(TerytNode.builder().code("terc").date(terytClient.pobierzDateAktualnegoKatTerc()));
//        dates.addChild(TerytNode.builder().code("nts").date(terytClient.pobierzDateAktualnegoKatNTS()));
//        dates.addChild(TerytNode.builder().code("simc").date(terytClient.pobierzDateAktualnegoKatSimc()));
//        dates.addChild(TerytNode.builder().code("ulic").date(terytClient.pobierzDateAktualnegoKatUlic()));
//
//        return dates;
//    }

//    private void downloadCatalogs(TerytNode root, TerytNode dates) throws IOException {
//
//        var catalogs = root.addChild(TerytNode.builder().code("catalogs"));
//
//
//        var tercDate = dates.getChildByCode("terc").getDate();
//        var ntsDate = dates.getChildByCode("nts").getDate();
//        var simcDate = dates.getChildByCode("simc").getDate();
//        var ulicDate = dates.getChildByCode("ulic").getDate();
//
//
//        var lines = getCsvLines(terytClient.pobierzKatalogTERCAdr(tercDate));
////        downloadCatalog(catalogs, terytClient.pobierzKatalogTERC(tercDate), "terc");
//
////        downloadCatalog(catalogs, terytClient.pobierzKatalogSIMCAdr(simcDate), "simc-address");
////        downloadCatalog(catalogs, terytClient.pobierzKatalogSIMC(simcDate), "simc");
////        downloadCatalog(catalogs, terytClient.pobierzKatalogSIMCStat(simcDate), "simc-stat");
////
////        downloadCatalog(catalogs, terytClient.pobierzKatalogULIC(ulicDate), "ulic");
////        downloadCatalog(catalogs, terytClient.pobierzKatalogULICAdr(ulicDate), "ulic-address");
////        downloadCatalog(catalogs, terytClient.pobierzKatalogULICBezDzielnic(ulicDate), "ulic-without-districts");
////
////        downloadCatalog(catalogs, terytClient.pobierzKatalogNTS(ulicDate), "nts");
////
////        downloadCatalog(catalogs, terytClient.pobierzKatalogWMRODZ(simcDate), "wmrodz");
//
//
//
//    }



    public static <T extends CsvRecord> List<T> getCsvLines(Class<T> cls, PlikKatalog catalogFile) throws IOException {
        var base64Value = catalogFile.getPlikZawartosc().getValue();
        var data = Base64.getDecoder().decode(base64Value);
        var zipTempFile = File.createTempFile("temp-", ".zip");
        writeZipFile(data, zipTempFile);

        var fileName = String.format("%s.csv", catalogFile.getNazwaPliku().getValue());
        var csvTempDirectory = Files.createTempDirectory("files");
        extractZipFile(zipTempFile, fileName, csvTempDirectory);

        var csvPath = Path.of(csvTempDirectory.toString(), fileName);


        var result =  new CsvToBeanBuilder<T>(new FileReader(csvPath.toString()))
                .withType(cls)
                .withSeparator(';')
                .withIgnoreEmptyLine(true)
                .withIgnoreLeadingWhiteSpace(true)
                .build()
                .parse();

        return result.stream().distinct().toList();
    }


    private static void extractZipFile(File zipTempFile, String fileName, Path csvTempDirectory) throws IOException {
        try (var zipFile = new ZipFile(zipTempFile)) {
            zipFile.extractFile(fileName, csvTempDirectory.toString());
        }
    }

    private static void writeZipFile(byte[] data, File zipTempFile) {
        try (OutputStream stream = new FileOutputStream(zipTempFile)) {
            stream.write(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
