package io.github.aangiel.teryt.postal;

import com.google.common.collect.ImmutableMap;
import lombok.extern.log4j.Log4j;
import org.apache.commons.collections4.map.LinkedMap;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.text.TextPosition;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
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

  public List<PnaRecord> parse() throws IOException {

    var pagesWithLetters = this.postalCodeStripper.strip();

    var pageWithLinesBuilder = ImmutableMap.<Integer, List<TextPosition>>builder();

    var result = MultiKeyMap.<String, List<TextPosition>>multiKeyMap(new LinkedMap<>());

    var printed = print(pagesWithLetters);

    FileUtils.writeStringToFile(
        Path.of("target", "tmp", "postalCodesFormatted.txt").toFile(), printed);
    return null;
  }

  private String print(MultiKeyMap<Object, List<TextPosition>> pagesWithLetters) {
    var builder = new StringBuilder();
    var currentPage = 2;
    for (var entry : pagesWithLetters.entrySet()) {

      if (((int) entry.getKey().getKey(0) > currentPage) ){
        builder.append(String.format("%n%n%nStrona: %d%n%s", ++currentPage, "-".repeat(200)));
      }

      if ((int) entry.getKey().getKey(2) == 1) {
//        System.out.printf("%nPage %d%n", (int) entry.getKey().getKey(1));
        builder.append(String.format("%n%1$4d:  ", (int) entry.getKey().getKey(1)));
      }
      builder.append(String.format(
          getFormat((int) entry.getKey().getKey(2)),
          entry.getValue().stream().map(TextPosition::getUnicode).collect(Collectors.joining())));
    }

    return builder.toString();
  }

  private String getFormat(int cellNumber) {
    return switch (cellNumber) {
      case 1 -> "%1$-10s";
      case 2, 3 -> "%1$-50s";
      default -> "%1$-35s";
    };
  }
}
