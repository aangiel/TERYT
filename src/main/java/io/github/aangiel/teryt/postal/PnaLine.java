package io.github.aangiel.teryt.postal;

import lombok.Getter;
import lombok.ToString;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@ToString
@Getter
final class PnaLine {

    private final List<PnaWord> words = new LinkedList<>();

    private static PnaWord currentGroup = null;

    void addWord(PnaWord word) {
        words.add(word);
    }

    Stream<Map<String, String>> createMap(PnaLine header) {

        var properties = new LinkedHashMap<String, String>();

        for (var h : header.words) {
            for (var w : words) {
                var wordList = words.stream()
                        .filter(e -> isInLineWithHeadersYWithTolerance(h, e))
//                        .map(PnaWord::text)
                        .toList();

                if(wordList.isEmpty()) continue;

                if (PnaLines.matchesPostalCodeFormat(wordList.get(0)) && wordList.size() > 1) {
                    currentGroup = wordList.get(1);
                    wordList = List.of(wordList.get(0));
                }

                if (currentGroup != null) {
                    properties.put("Grupa", currentGroup.text());
                }

                properties.put(h.text(), getJoinedValue(h, wordList.stream().map(PnaWord::text).toList()));
                break;
            }
        }

        return Stream.of(properties);
    }

    private String getJoinedValue(PnaWord h, List<String> wordList) {
        String propertyValue;
        if (h.text().equals("Numery")) {
            propertyValue = String.join(", ", wordList);
        } else if (wordList.get(0).startsWith("Prymasa Tysiąclecia")) {
            propertyValue = String.join(" ", wordList);
        } else {
            propertyValue = String.join("", wordList);
        }
        return propertyValue;
    }

    private static boolean isInLineWithHeadersYWithTolerance(PnaWord header, PnaWord field) {
        return header.getX() + 2.0f > field.getX() &&
                header.getX() - 2.0f < field.getX();
    }


}
