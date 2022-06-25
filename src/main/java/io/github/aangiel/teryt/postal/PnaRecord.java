package io.github.aangiel.teryt.postal;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

  private String simcPnaKey = null;

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
        properties.get("PNA"));
  }

  public static PnaRecord create(Map<String, String> properties) {
    var town = properties.getOrDefault("Miejscowość", "");
    var townPart = "";

    var openingParenthesis = town.indexOf('(');

    if (openingParenthesis >= 0) {
      townPart = town.substring(openingParenthesis + 1, town.indexOf(')'));
      town = town.substring(0, openingParenthesis).trim();
    }

    if (townPart.matches("[a-ząćęłńóśźż\s]*")) {
      townPart = "";
    }

    return new PnaRecord(properties, town, townPart);
  }

  public String getSimcPnaKey() {
    if ("Warszawa".equals(town) && "mazowieckie".equals(voivodeship)) {
      return "Ł";
    }

    if (simcPnaKey == null) {
      var newCommunity =
          "Kraków".equals(town)
                  || ("Łódź".equals(town) && "łódzkie".equals(voivodeship))
                  || ("Poznań".equals(town) && "wielkopolskie".equals(voivodeship))
//                  || ("Warszawa".equals(town) && "mazowieckie".equals(voivodeship))
                  || ("Wrocław".equals(town) && "dolnośląskie".equals(voivodeship))
              ? townPart
              : community;

      var newTown = town.endsWith("\"") ? town.substring(0, town.length() - 1) : town;
      simcPnaKey = Stream.of(voivodeship, county, newCommunity, newTown, townPart)
          .filter(Predicate.not(String::isEmpty))
          .map(String::toLowerCase)
          .collect(Collectors.joining(":"));
//          .toString();
//          String.join(":", voivodeship, county, newCommunity, newTown).toLowerCase(Locale.ROOT);
    }
    return simcPnaKey;
  }
}
