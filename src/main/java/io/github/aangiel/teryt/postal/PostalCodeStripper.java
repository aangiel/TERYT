package io.github.aangiel.teryt.postal;

import lombok.Getter;
import lombok.extern.log4j.Log4j;
import org.apache.commons.collections4.map.LinkedMap;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.lang3.Range;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Log4j
final class PostalCodeStripper extends PDFTextStripper {

  public static final Pattern STREET_PATTERN = Pattern.compile(
          "^(?![a-ząćęłńóśźż]+-)(?:[A-ZĄĆĘŁŃÓŚŹŻ]|[a-ząćęłńóśźż]+\\.|\\d|^.*[A-ZĄĆĘŁŃÓŚŹŻ]+.*(?:[a-ząćęłńóśźż]+|\\s+|\"|[A-Z])$)+(?:\\s|-|[a-ząćęłńóśźż]*|[A-ZĄĆĘŁŃÓŚŹŻ]+|\\d*|\\.*|\"*|'*)+$");
  private static final String POSTAL_CODE_PATTERN = "^\\d{2}-\\d{3}$";
  private static float headerY = 0.0f;
  private static float footerY = 0.0f;
  private static int lineCounter = 0;
  private static int currentPage = 0;
  private static List<Range<Float>> headerRanges;

  private static int cellCounter = 1;
  private final File pdfToStrip;

  @Getter
  private final MultiKeyMap<Object, List<TextPosition>> pages =
      MultiKeyMap.multiKeyMap(new LinkedMap<>());

  private final List<PnaRecord> result = new LinkedList<>();

  PostalCodeStripper(File pdfToStrip) throws IOException {
    super();
    this.pdfToStrip = pdfToStrip;
  }

  List<PnaRecord> strip() {
    try (PDDocument document = PDDocument.load(pdfToStrip)) {
      super.setSortByPosition(true);
      super.writeText(document, Writer.nullWriter());
    } catch (IOException e) {
      e.printStackTrace();
      log.info(e.getMessage());
    }
    return result;
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

    if (currentPage > 1648) {
      return;
    }

    if (getCurrentPageNo() > currentPage) {
      reorganizeCells();
      currentPage++;
      lineCounter = 0;
      headerRanges = null;
      cellCounter = 1;
    }

    if (text.equals("PNA")) {
      headerY = textPositions.get(0).getYDirAdj();
    }

    if (Math.abs(textPositions.get(0).getYDirAdj() - headerY) < 2.0f) {
      pages.put(currentPage, 0, cellCounter++, textPositions);
    }

    if (text.startsWith("© Copyright")) {
      footerY = textPositions.get(0).getYDirAdj();
    }

    if (Math.abs(textPositions.get(0).getYDirAdj() - footerY) < 2.0f) {
      return;
    }

    if (text.matches(POSTAL_CODE_PATTERN) && getCellNumber(textPositions.get(0)) == 1) {
      if (lineCounter > 0) {
        reorganizeCells();
      }
      lineCounter++;
      for (var i = 1; i <= 7; i++) {
        pages.put(currentPage, lineCounter, i, new LinkedList<>());
      }
    }

    if (lineCounter == 0) {
      return;
    }

    for (var textPosition : textPositions) {
      pages.get(currentPage, lineCounter, getCellNumber(textPosition)).add(textPosition);
    }
  }

  private void reorganizeCells() {
    for (var i = 3; i < cellCounter; i++) {
      var cell = pages.get(currentPage, lineCounter, i);
      var filtered = Optional.<String>empty();
      if (i == 3) {

        var previousCellEndsWithDash =
                pages
                        .get(currentPage, lineCounter, i - 1)
                        .get(pages.get(currentPage, lineCounter, i - 1).size() - 1)
                        .getUnicode()
                        .equals("-");

        var cellString = cell.stream().map(TextPosition::getUnicode).collect(Collectors.joining());

        var matchesStreetPattern = streetMatch(cellString);
        if (matchesStreetPattern && !previousCellEndsWithDash) {
          filtered = Optional.empty();
        } else {
          filtered = Optional.of(cellString);
        }
      }
      if (i == 4) {
        filtered =
            cell.stream().map(TextPosition::getUnicode).filter(e -> e.matches("\\d")).findFirst();

        if (filtered.isPresent()) {
          filtered = Optional.empty();
        } else {
          filtered = Optional.of("x");
        }
      }
      if (filtered.isPresent()) {
        var previousCell = pages.get(currentPage, lineCounter, i - 1);
        previousCell.addAll(cell);
        previousCell.sort(
            Comparator.comparingDouble(TextPosition::getYDirAdj)
                .thenComparing(TextPosition::getXDirAdj));

        pages.put(currentPage, lineCounter, i, new LinkedList<>());
      }
    }

    if (lineCounter == 0) {
      return;
    }

    var properties = new LinkedHashMap<String, String>();

    properties.put(
        "PNA",
        pages.get(currentPage, lineCounter, 1).stream()
            .map(TextPosition::getUnicode)
            .collect(Collectors.joining()));
    properties.put(
        "Miejscowość",
        pages.get(currentPage, lineCounter, 2).stream()
            .map(TextPosition::getUnicode)
            .collect(Collectors.joining()));
    properties.put(
        "Ulica",
        pages.get(currentPage, lineCounter, 3).stream()
            .map(TextPosition::getUnicode)
            .collect(Collectors.joining()));
    properties.put(
        "Numery",
        pages.get(currentPage, lineCounter, 4).stream()
            .map(TextPosition::getUnicode)
            .collect(Collectors.joining()));
    properties.put(
        "Gmina",
        pages.get(currentPage, lineCounter, 5).stream()
            .map(TextPosition::getUnicode)
            .collect(Collectors.joining()));
    properties.put(
        "Powiat",
        pages.get(currentPage, lineCounter, 6).stream()
            .map(TextPosition::getUnicode)
            .collect(Collectors.joining()));
    properties.put(
        "Województwo",
        pages.get(currentPage, lineCounter, 7).stream()
            .map(TextPosition::getUnicode)
            .collect(Collectors.joining()));

    var pnaRecord = PnaRecord.create(properties);
    result.add(pnaRecord);
  }

  static boolean streetMatch(String toMatch) {
    return STREET_PATTERN.matcher(toMatch).matches();
  }

  private int getCellNumber(TextPosition textPosition) {
    if (headerRanges == null) {
      headerRanges = new LinkedList<>();
      for (var i = 1; i < cellCounter - 1; i++) {
        var left = pages.get(currentPage, 0, i).get(0).getXDirAdj();
        var right = pages.get(currentPage, 0, i + 1).get(0).getXDirAdj();
        headerRanges.add(Range.between(left, right));
      }
      headerRanges.add(
          Range.between(
              pages.get(currentPage, 0, cellCounter - 1).get(0).getXDirAdj(),
              this.getCurrentPage().getMediaBox().getWidth()));
    }
    for (var i = 0; i < headerRanges.size(); i++) {
      if (i < cellCounter - 2
          && Math.abs(headerRanges.get(i + 1).getMinimum() - textPosition.getXDirAdj()) < 2.0f) {
        return i + 2;
      } else if (headerRanges.get(i).getMinimum() <= textPosition.getXDirAdj()
          && headerRanges.get(i).getMaximum() > textPosition.getXDirAdj()) {
        return i + 1;
      }
    }
    throw new NoSuchElementException();
  }
}
