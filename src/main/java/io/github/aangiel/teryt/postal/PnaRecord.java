package io.github.aangiel.teryt.postal;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class PnaRecord {

    private final String voivodeship;
    private final String county;
    private final String community;
    private final String town;
    private final String townPart;
    private final String street;
    private final String numbers;
    private final String postalCode;

//    private static final Pattern townPartPattern = Pattern.compile("^(.+)(?!\\()");

    private PnaRecord(Map<String, String> properties, String town, String townPart) {
        this(
                properties.get("Województwo"),
                properties.get("Powiat"),
                properties.get("Gmina"),
                town,
                townPart,
                properties.getOrDefault("Ulica", ""),
                properties.getOrDefault("Numery", ""),
                properties.get("PNA")
        );
    }

    public static PnaRecord create(Map<String, String> properties) {
        var town = properties.getOrDefault("Miejscowość", "");
        var townPart = "";

        var openingParenthesis = town.indexOf('(');

        if (openingParenthesis >= 0) {
            townPart = town.substring(openingParenthesis + 1, town.indexOf(')'));
            town = town.substring(0, openingParenthesis).trim();
        }


        return new PnaRecord(properties, town, townPart);
    }
}
