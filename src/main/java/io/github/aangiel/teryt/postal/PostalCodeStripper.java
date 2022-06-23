package io.github.aangiel.teryt.postal;

import lombok.extern.log4j.Log4j;
import org.apache.commons.collections4.map.LinkedMap;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

@Log4j
final class PostalCodeStripper extends PDFTextStripper {

  private static final String POSTAL_CODE_PATTERN = "^\\d{2}-\\d{3}$";
  private static float lastLineY = 0.0f;
  private static float lastLineX = 0.0f;
  private static float headerY = 0.0f;
  private static float footerY = 0.0f;
  private static int lineCounter = 0;
  private static int currentPage = 0;
  private final File pdfToStrip;
  private final MultiKeyMap<Object, List<TextPosition>> pages =
      MultiKeyMap.multiKeyMap(new LinkedMap<>());

  PostalCodeStripper(File pdfToStrip) throws IOException {
    super();
    this.pdfToStrip = pdfToStrip;
  }

  MultiKeyMap<Object, List<TextPosition>> strip() {
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

    if (getCurrentPageNo() >= 6 || getCurrentPageNo() < 3) {
      return;
    }

    if (getCurrentPageNo() > currentPage) {
      currentPage++;
      lineCounter = 0;
    }

    if (text.equals("PNA")) {
      headerY = textPositions.get(0).getYDirAdj();
    }

    if (Math.abs(textPositions.get(0).getYDirAdj() - headerY) < 2.0f) {
      pages.put(currentPage, "header", text, textPositions);
      return;
    }

    if (text.startsWith("© Copyright")) {
      footerY = textPositions.get(0).getYDirAdj();
    }

    if (Math.abs(textPositions.get(0).getYDirAdj() - footerY) < 2.0f) {
      return;
    }

    if (text.matches(POSTAL_CODE_PATTERN)) {
      pages.put(currentPage, ++lineCounter, new LinkedList<>());
    }

    if (lineCounter == 0) {
      return;
    }
    pages.get(currentPage, lineCounter).addAll(textPositions);
  }
}
