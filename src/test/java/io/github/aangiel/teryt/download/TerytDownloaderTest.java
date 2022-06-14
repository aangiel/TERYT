package io.github.aangiel.teryt.download;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.fail;

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
    void downloadData() throws IOException {
        var downloader = new TerytDownloader();
        downloader.downloadData();
        var downloaded = downloader.getDownloadedCatalogs();

        var pretty = new ObjectMapper()//.writerWithDefaultPrettyPrinter()
                .writeValueAsString(downloaded);

        Files.writeString(Path.of("src", "test", "resources", "mapResultUTF.json"), pretty);

        fail();
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