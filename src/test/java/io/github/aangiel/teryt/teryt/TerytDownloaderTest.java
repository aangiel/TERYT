package io.github.aangiel.teryt.teryt;

import com.opencsv.exceptions.CsvException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class TerytDownloaderTest {

//    @Test
//    void download() throws IOException {
//        TerytDownloader terytZipDownloader = new TerytDownloader();
//
//        var expected = this.getClass().getResourceAsStream("/result.json");
//
//        var result = terytZipDownloader.download();
//        assert expected != null;
//        assertEquals(result, result);
//
//
//    }

//    @Test
//    void writeToRedis() throws IOException, InterruptedException {
//        TerytDownloader downloader = new TerytDownloader();
//
//        downloader.writeToRedis();
//    }

    @Test
    void downloadData() throws IOException, CsvException {
        var downloader = new TerytDownloader();
        var downloadedRoot = downloader.downloadData();

//        var result = new ObjectMapper().writerWithDefaultPrettyPrinter()
//                .writeValueAsString(downloadedRoot);

//        Files.writeString(Path.of("target", "tmp", "expectedTerytTreeResult.json"), result);

//        var expected = FileUtils.readFileToString(Path.of("src", "test", "resources", "expectedTerytTreeResult.json").toFile());

//        assertEquals(expected, result);
    }

//    @Test
//    void getDates() throws DatatypeConfigurationException {
//
//        var downloader = new TerytDownloader();
//
//        var expected = ImmutableMap.of(
//                "TERC", DatatypeFactory.newInstance().newXMLGregorianCalendar("2022-01-01T00:00:00"),
//                "NTS", DatatypeFactory.newInstance().newXMLGregorianCalendar("2017-01-01T00:00:00"),
//                "SIMC", DatatypeFactory.newInstance().newXMLGregorianCalendar("2022-01-01T00:00:00"),
//                "ULIC", DatatypeFactory.newInstance().newXMLGregorianCalendar("2022-06-13T00:00:00")
//        );
//
//        var result = downloader.getDates();
//
//        assertTrue(Maps.difference(expected, result).areEqual());
//    }
}