package io.github.aangiel.teryt;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TerytDownloaderTest {

    @Test
    void download() throws IOException {
        TerytDownloader terytZipDownloader = new TerytDownloader();

        var expected = this.getClass().getResourceAsStream("/result.json");

        var result = terytZipDownloader.download();
        assert expected != null;
        assertEquals(result, result);


    }

    @Test
    void writeToRedis() throws IOException, InterruptedException {
        TerytDownloader downloader = new TerytDownloader();

        downloader.writeToRedis();
    }
}