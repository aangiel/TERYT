package io.github.aangiel.teryt.postal;

import com.google.common.collect.ImmutableMap;
import lombok.extern.log4j.Log4j;
import org.apache.commons.collections4.map.LinkedMap;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.pdfbox.text.TextPosition;

import java.io.File;
import java.io.IOException;
import java.util.List;

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

    var result = MultiKeyMap.<String, List<TextPosition>>multiKeyMap(new LinkedMap<>());

    //        pagesWithLetters.entrySet().stream()
    //                .map()

//    var headers =
//        pagesWithLetters.entrySet().stream()
//            .collect(
//                Collectors.groupingBy(
//                    e -> new MultiKey<>((int) e.getKey().getKey(0), (int) e.getKey().getKey(1)),
//                    LinkedMap::new,
//                    Collectors.toList()))
//            .entrySet()
//            .stream()
//            .filter(
//                e ->
//                    e.getValue().stream()
//                            .filter(t -> t.getKey().getKey(3).equals("PNA"))
//                            .toList()
//                            .size()
//                        > 0)
//                .flatMap(e -> Map.Entry)
//            .flatMap(e -> e.getValue().stream())
//            .map(e -> new MultiKey(e.getKey().getKey(0), e.getKey().getKey(3), e.getKey().getKey(2)))
//                .map(e -> )
//            .toList();

    return null;
  }
}
