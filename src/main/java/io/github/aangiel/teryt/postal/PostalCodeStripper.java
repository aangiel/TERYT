package io.github.aangiel.teryt.postal;

import lombok.extern.log4j.Log4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Log4j
final class PostalCodeStripper extends PDFTextStripper {

    private final File pdfToStrip;

    private final Map<Integer, List<OneCharacter>> pages = new LinkedHashMap<>();

    PostalCodeStripper(File pdfToStrip) throws IOException {
        super();
        this.pdfToStrip = pdfToStrip;
    }

    Map<Integer, List<OneCharacter>> strip() {
        try (PDDocument document = PDDocument.load(pdfToStrip)) {
            super.setSortByPosition(true);
            super.writeText(document, Writer.nullWriter());
        } catch (IOException e) {
            e.printStackTrace();
            log.info(e.getMessage());
        }
        return pages;
    }

    @Override
    protected void writeWordSeparator() {
        // do nothing as we don't need spaces
    }

    @Override
    protected void writeLineSeparator() {
        // do nothing as writeString(String, List<TextPosition>) below handles the new lines
    }

    @Override
    protected void writeString(String text, List<TextPosition> textPositions) {

        for (var textPosition : textPositions) {
            var charactersList = pages.getOrDefault(getCurrentPageNo(), new LinkedList<>());
            charactersList.add(OneCharacter.create(textPosition, textPositions.get(0)));
            pages.put(getCurrentPageNo(), charactersList);
        }
    }
}
