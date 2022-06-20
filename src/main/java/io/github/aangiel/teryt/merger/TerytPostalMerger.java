package io.github.aangiel.teryt.merger;

import com.google.common.collect.ImmutableMap;
import io.github.aangiel.teryt.teryt.*;
import io.github.aangiel.teryt.ws.ITerytWs1;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor(staticName = "create")
@Log4j
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
                .getCsvLines(WmRodzCsvRecord.class, terytClient.pobierzKatalogWMRODZ(simcDate));

        var tercAddressMap = tercMap(tercAddress);

        var wmRodzMap = wmRodzMap(wmRodz);

        var simcMap = simcMap(simcAddress, tercAddressMap, tercDate, wmRodzMap);

        return null;
    }

    private Map<String, Map<String, String>> simcMap(List<SimcCsvRecord> simc,
                                                     Map<String, Map<String, String>> tercAddressMap,
                                                     XMLGregorianCalendar tercDate,
                                                     Map<String, String> wmRodz) {
        var simcMap = simc.stream()
                .collect(Collectors.toMap(
                        k -> k.createKey("simc-address"),
                        Function.identity()
                ));

        var baseTownMap = simc.stream()
                .filter(e -> e.getBaseSymbol().equals(e.getSymbol()))
                .collect(Collectors.toMap(
                   SimcCsvRecord::createBaseSymbolKey,
                   v -> v.createKey("simc-address")
                ));

        var resultBuilder = ImmutableMap.<String, Map<String, String>>builder();

        var counter = -1;
        for (var entry : simcMap.entrySet()) {
            var k = entry.getKey();
            var v = entry.getValue();

            var tercProperties = tercAddressMap.get(v.createTercKey("terc-address", tercDate.toGregorianCalendar().toZonedDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE)));
            var baseTown = baseTownMap.get(v.createBaseSymbolKey());


            var properties = ImmutableMap.of(
                    "województwo", tercProperties.get("województwo"),
                    "powiat", tercProperties.getOrDefault("powiat", ""),
                    "gmina", tercProperties.get("nazwa"),
                    "rodzaj-gminy", tercProperties.get("rodzaj"),
                    "miejscowość-bazowa", simcMap.get(baseTown).getName(),
                    "nazwa", v.getName(),
                    "rodzaj-miejscowości", wmRodz.get(String.join(":", "wmRodz", "2013-02-28", v.getTownType()))
            );

            if (counter++ % 500 == 0)
                log.info(counter);

            resultBuilder.put(k, properties);


        }

        return resultBuilder.build();
    }

    private Map<String, String> wmRodzMap(List<WmRodzCsvRecord> wmRodz) {

        return wmRodz.stream()
                .collect(Collectors.toMap(
                        k -> String.join(":", "wmRodz", k.getStateDate(), k.getTownType()),
                        WmRodzCsvRecord::getTypeName
                ));
    }


    private Map<String, Map<String, String>> tercMap(List<TercCsvRecord> tercAddress) {
        var tercAddressMap = tercAddress.stream()
                .collect(Collectors.toMap(
                        k -> k.createKey("terc-address"),
                        Function.identity()));

        var result = ImmutableMap.<String, Map<String, String>>builder();

        for (var entry : tercAddressMap.entrySet()) {
            var v = entry.getValue();
            var parent = tercAddressMap.get(v.retrieveParentKey("terc-address"));
            var properties = ImmutableMap.<String, String>builder();
            properties.put("nazwa", v.getName());
            properties.put("rodzaj", v.getExtraName());
            while (parent != null) {
                properties.put(parent.getExtraName(), parent.getName());
                parent = tercAddressMap.get(parent.retrieveParentKey("terc-address"));
            }
            result.put(entry.getKey(), properties.build());
        }
        return result.build();
    }
}

