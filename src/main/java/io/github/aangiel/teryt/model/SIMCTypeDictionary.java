package io.github.aangiel.teryt.model;

import io.github.aangiel.teryt.ws.RodzajMiejscowosci;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@ToString
public class SIMCTypeDictionary {

    private final String name;
    private final String description;
    private final String code;

    public static SIMCTypeDictionary create(RodzajMiejscowosci from) {
        return new SIMCTypeDictionary(
                from.getNazwa().getValue(),
                from.getOpis().getValue(),
                from.getSymbol().getValue()
        );
    }
}
