package io.github.aangiel.teryt.postal;

import com.google.common.collect.ImmutableMap;
import lombok.extern.log4j.Log4j;
import org.apache.pdfbox.text.TextPosition;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Log4j
public final class PostalCodeParser {

    private final PostalCodeStripper postalCodeStripper;

    private PostalCodeParser(File pdfToStrip) throws IOException {
        this.postalCodeStripper = new PostalCodeStripper(pdfToStrip);
    }

    public static PostalCodeParser create(File pdfToStrip) throws IOException {
        return new PostalCodeParser(pdfToStrip);
    }

    public List<PnaRecord> parse() {

        var pagesWithLetters = this.postalCodeStripper.strip();

        var pageWithLinesBuilder = ImmutableMap.<Integer, List<TextPosition>>builder();

        for (var pageWithLetter : pagesWithLetters.entrySet()) {

            var firstCharactersInLines = pageWithLetter.getValue().stream()
                    .map(OneCharacter::firstCharacter)
                    .distinct()
                    .collect(Collectors.groupingBy(e -> Math.round(e.getXDirAdj() / 10.0) * 10))
                    .entrySet().stream()
                    .min(Comparator.comparingDouble(Map.Entry::getKey))
                    .orElseThrow(() -> new IllegalStateException("No letters found"))
                    .getValue().stream()
                    .sorted(Comparator.comparingDouble(TextPosition::getYDirAdj))
                    .toList();

            pageWithLinesBuilder.put(pageWithLetter.getKey(), firstCharactersInLines);
        }

        var pagesWithLinesY = pageWithLinesBuilder.build();

        var result = ImmutableMap.<Integer, Map<Long, List<TextPosition>>>builder();

        for (var i = 1; i < pagesWithLinesY.entrySet().size(); i++) {
            var y1 = Objects.requireNonNull(pagesWithLinesY.get(i)).get(0).getYDirAdj();
            var y2 = Objects.requireNonNull(pagesWithLinesY.get(i + 1)).get(0).getYDirAdj();

            List<TextPosition> textPositions = pagesWithLetters.values().stream()
                    .flatMap(List::stream)
                    .map(OneCharacter::character)
                    .filter(c -> c.getYDirAdj() >= y1 && c.getYDirAdj() < y2)
                    .toList();

        }

        return null;
    }
}