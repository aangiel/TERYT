package io.github.aangiel.teryt.merger;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedList;

class TerytPostalMergerTest {

    @Test
    void merge() throws IOException {

        var downloadedTeryt = Path.of("target", "tmp", "mapResultUTF.json").toFile();
        var downloadedPostalCodes = Path.of("src", "test","resources", "expectedPostalCodes.json").toFile();

        var teryt = new ObjectMapper().readValue(downloadedTeryt, LinkedHashMap.class);
        var postal = new ObjectMapper().readValue(downloadedPostalCodes, LinkedList.class);
//        teryt = (Map<String, Map<String, String>>) teryt;

        var merger = TerytPostalMerger.create(teryt, postal);

        var merged = merger.merge();

        System.out.println("****");
    }
}