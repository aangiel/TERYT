package io.github.aangiel.teryt.merger;

import io.github.aangiel.teryt.Constants;
import io.github.aangiel.teryt.teryt.TerytClient;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

class TerytPostalMergerTest {

    @Test
    void merge() throws IOException {

//        var downloadedTeryt = Path.of("target", "tmp", "mapResultUTF.json").toFile();
//        var downloadedPostalCodes = Path.of("src", "test","resources", "expectedPostalCodes.json").toFile();

//        var teryt = new ObjectMapper().readValue(downloadedTeryt, LinkedHashMap.class);
//        var postal = new ObjectMapper().readValue(downloadedPostalCodes, LinkedList.class);
//        teryt = (Map<String, Map<String, String>>) teryt;

        var terytClient = TerytClient.create(Constants.TERYT_USER, Constants.TERYT_PASS);
        File file = Path.of("src", "test", "resources", "spispna.pdf").toFile();


        var merger = TerytPostalMerger.create(terytClient, file);

        var merged = merger.merge();

        System.out.println("****");
    }
}