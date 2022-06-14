package io.github.aangiel.teryt.model;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.math.NumberUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.StringJoiner;

@Getter
@ToString
public final class CsvLine {

    private final Map<String, String> properties;

    private CsvLine(String catalogName, String line, String[] headers) {


        line = new String(line.getBytes(StandardCharsets.UTF_8));
        var fieldValues = line.split("\s*;\s*");

        var resultBuilder = ImmutableMap.<String, String>builder()
                .put("catalogName", catalogName);

        for (int i = 0; i < headers.length; i++) {

            var bytes = headers[i].getBytes(StandardCharsets.UTF_8);
            var resultBytes = new byte[0];

            for (var oneByte : bytes) {
                if (oneByte > 0)
                    resultBytes = push(resultBytes, oneByte);
            }

            resultBuilder.put(new String(resultBytes).trim(), fieldValues[i]);
        }

        this.properties = resultBuilder.build();
    }

    private static byte[] push(byte[] array, byte push) {
        byte[] longer = new byte[array.length + 1];
        for (int i = 0; i < array.length; i++)
            longer[i] = array[i];
        longer[array.length] = push;
        return longer;
    }

    public static CsvLine create(String catalogName, String line, String[] headers) {
        return new CsvLine(catalogName, line, headers);
    }

    public String getKey() {

        var resultJoiner = new StringJoiner(":")
                .add(properties.get("catalogName"))
                .add(properties.get("STAN_NA"));

        for (var entryKey : properties.keySet()) {
            var value = properties.get(entryKey);

            if (NumberUtils.isDigits(value)) {
                resultJoiner.add(value);
            }
        }

        return resultJoiner.toString();
    }


}
