package io.github.aangiel.teryt.postal;

import io.github.aangiel.teryt.Constants;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.pdfbox.text.TextPosition;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE, staticName = "create")
@ToString
final class PnaPage {

    private final PnaLines lines = new PnaLines();

    private final AtomicInteger pageCounter = new AtomicInteger();
    private final int pageNumber = pageCounter.getAndIncrement();

    private static final Pattern bierunPattern = Pattern.compile("^((?:Bieruń|Dębogórze|Jelenia Góra|Jelenia Góra \\(Cieplice)|Józefów)\s+([A-Za-zżźćńółęąśŻŹĆĄŚĘŁÓŃ]+.*)$");

    Stream<Map<String, String>> stream() {
        return lines.stream();
    }

    void addWord(String text, List<TextPosition> textPositions) {
        if (text.isEmpty()) return;

        var matcher = bierunPattern.matcher(text);

        if (text.contains(",")) {
            var split = Constants.COLON_PATTERN.split(text);
            addSplitWords(text, textPositions, split);
        } else if (text.startsWith("Prymasa Tysiąclecia")) {
            var split = new String[]{
                    "Prymasa Tysiąclecia Kardynała Stefana",
                    "Bogatynia"
            };
            addSplitWords(text, textPositions, split);
        } else if (matcher.matches()) {
            if ("Józefów nad Wisłą".equals(text)) {
                lines.addWord(new PnaWord(text, textPositions));
                return;
            }
            var split = new String[] {
                    matcher.group(1), matcher.group(2)
            };
            addSplitWords(text, textPositions, split);
        } else {
            lines.addWord(new PnaWord(text, textPositions));
        }
    }

    private void addSplitWords(String text, List<TextPosition> textPositions, String[] split) {
        for (var item : split) {
            var index = text.indexOf(item);
            var textPositions1 = textPositions.subList(index, index + item.length());
            lines.addWord(new PnaWord(item, textPositions1));
        }
    }


}
