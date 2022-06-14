package io.github.aangiel.teryt.postal;

import io.github.aangiel.teryt.Constants;
import lombok.ToString;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@ToString
class PnaLines {

    private final PnaLine header = new PnaLine();
    private final List<PnaLine> lines = new LinkedList<>();
    private final PnaLine footer = new PnaLine();

    void addWord(PnaWord word) {

        if (word.positions().isEmpty()) return;

        if (matchesTableHeader(word)) {
            header.addWord(word);
            return;
        } else if (matchesPageFooter(word)) {
            footer.addWord(word);
        }


        if (matchesPostalCodeFormat(word)) {
            var line = new PnaLine();
            line.addWord(word);
            lines.add(line);
        } else if (fitsTheLine(word)) {
            getLastLine().addWord(word);
        }
    }

    private boolean fitsTheLine(PnaWord word) {
        if (lines.isEmpty()) return false;
        return word.getY() >= getLastLineY();
    }

    public Stream<Map<String, String>> stream() {
        return lines.stream()
                .flatMap(line -> line.createMap(header));
    }

    private PnaLine getLastLine() {
        return lines.get(lines.size() - 1);
    }

    private float getLastLineY() {
        return getLastLine().getWords().get(0)
                .positions().get(0)
                .getYDirAdj();
    }


    private boolean matchesTableHeader(PnaWord word) {
        return "PNA".equals(word.text()) || (
                !header.getWords().isEmpty() &&
                        header.getWords().get(0).getY() == word.getY());
    }

    static boolean matchesPostalCodeFormat(PnaWord word) {
        return Constants.POSTAL_CODE_PATTERN.matcher(word.text()).matches() && word.getX() < 100.0f;
    }

    private boolean matchesPageFooter(PnaWord word) {
        return word.text().startsWith("© Copyright by Poczta Polska") || (
                !footer.getWords().isEmpty() &&
                        footer.getWords().get(0).getY() == word.getY()
        );
    }


}
