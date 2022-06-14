package io.github.aangiel.teryt.postal;

import lombok.extern.log4j.Log4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

@Log4j
final class PostalCodeStripper extends PDFTextStripper {

    private final PnaDocument pnaDocument = PnaDocument.create();
    private final File pdfToStrip;

    PostalCodeStripper(File pdfToStrip) throws IOException {
        super();
        this.pdfToStrip = pdfToStrip;
    }

    PnaDocument strip() {
        try (PDDocument document = PDDocument.load(pdfToStrip)) {
            super.setSortByPosition(true);
            super.writeText(document, Writer.nullWriter());
        } catch (IOException e) {
            e.printStackTrace();
            log.info(e.getMessage());
        }
        return pnaDocument;
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
        pnaDocument.getPage(super.getCurrentPageNo()).addWord(text, textPositions);
    }
}
