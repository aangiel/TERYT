package io.github.aangiel.teryt.merger;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor(staticName = "create")
public class TerytPostalMerger {

    private final Map<String, Map<String, String>> downloadedTerytData;

    private final List<Map<String, String>> downloadedAndParsedPostalCodes;


    public Map<String, Map<String, String>> merge() {
        return null;
    }

    class PostalCodeKey {


    }
}
