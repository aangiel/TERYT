package io.github.aangiel.teryt.merger;

import com.google.common.collect.ImmutableMap;
import io.github.aangiel.teryt.teryt.*;
import io.github.aangiel.teryt.ws.ITerytWs1;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor(staticName = "create")
public class TerytPostalMerger {

    private final ITerytWs1 terytClient;

    private final File postalCodesFile;

    public Map<String, Map<String, String>> merge() throws IOException {

        var tercDate = terytClient.pobierzDateAktualnegoKatTerc();
        var simcDate = terytClient.pobierzDateAktualnegoKatSimc();
        var ulicDate = terytClient.pobierzDateAktualnegoKatUlic();

        var tercAddress = TerytDownloader
                .getCsvLines(TercCsvRecord.class, terytClient.pobierzKatalogTERCAdr(tercDate));

        var simcAddress = TerytDownloader
                .getCsvLines(SimcCsvRecord.class, terytClient.pobierzKatalogSIMCAdr(simcDate));

        var ulicAddress = TerytDownloader
                .getCsvLines(UlicCsvRecord.class, terytClient.pobierzKatalogULICAdr(ulicDate));

        var wmRodz = TerytDownloader
                .getCsvLines(WmRodzCsvRecord.class, terytClient.pobierzKatalogWMRODZ(ulicDate));

        var tercAddressMap = tercAddress.stream()
                .collect(Collectors.toMap(
                        k ->  k.createKey("terc-address"),
                        Function.identity()));

        var result = ImmutableMap.<String, Map<String, String>>builder();

        for (var entry : tercAddressMap.entrySet()) {
            var v = entry.getValue();
            var parent = tercAddressMap.get(v.retrieveParentKey("terc-address"));
            var properties = ImmutableMap.<String, String>builder();
            properties.put("name", v.getName());
            properties.put("type", v.getExtraName());
            while (parent != null) {
                properties.put(parent.getExtraName(), parent.getName());
                parent = tercAddressMap.get(parent.retrieveParentKey("terc-address"));
            }
            result.put(entry.getKey(), properties.build());
        }

//        var voivodeships = tercAddress.stream()
//                .filter(e -> "województwo".equals(e.getExtraName()))
//                .collect(Collectors.toMap(
//                        k -> new StringJoiner(":")
//                                .add("terc-address")
//                                .add(tercDate.toString())
//                                .add(k.getVoivodeshipCode())
//                                .toString(),
//                        v -> ImmutableMap.of(v.getExtraName(), v.getName())
//                ));
//
//        var counties = tercAddress.stream()
//                .filter(e -> "powiat".equals(e.getExtraName()))
//                .collect(Collectors.toMap(
//                        k -> new StringJoiner(":")
//                                .add("terc-address")
//                                .add(tercDate.toString())
//                                .add(k.getVoivodeshipCode())
//                                .add(k.getCountyCode())
//                                .toString(),
//                        v -> ImmutableMap.of(v.getExtraName(), v.getName())
//                ));

        return null;
    }
}

