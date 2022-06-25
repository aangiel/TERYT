package io.github.aangiel.teryt.postal;

import com.google.common.collect.ImmutableList;
import lombok.extern.log4j.Log4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Log4j
class PostalCodeStripperTest {

  @Test
  void streetMatch() {
    var shouldMatch =
        ImmutableList.of(
            "rynek Rynek im. Jana Pawła",
            "rynek Rynek im. Jana Pawła II",
            "700-lecia",
            "rotmistrza Witolda Pileckiego",
            "pl. Konstytucji",
            "Bohaterów z Kopalni „Wujek”");

    var shouldNotMatch = ImmutableList.of("ska", "olonia)", "a-Kolonia");

    shouldMatch.forEach(
        s -> {
          log.info("Matching: " + s);
          assertTrue(PostalCodeStripper.streetMatch(s), () -> "Should match: " + s);
        });

    shouldNotMatch.forEach(
        s -> {
          log.info("Matching: " + s);
          assertFalse(PostalCodeStripper.streetMatch(s), () -> "Should not match: " + s);
        });
  }
}
