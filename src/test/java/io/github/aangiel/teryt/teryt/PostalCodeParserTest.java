package io.github.aangiel.teryt.teryt;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.aangiel.teryt.postal.PostalCodeParser;
import lombok.extern.log4j.Log4j;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j
class PostalCodeParserTest {

  @Test
  void parse() throws IOException {

    File file = Path.of("src", "test", "resources", "spispna.pdf").toFile();

    var properties = PostalCodeParser.create(file).parse();

    var postalCodesFile = Path.of("src", "test", "resources", "expectedPostalCodes.json").toFile();
    var postalCodes =
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(properties);

    FileUtils.writeStringToFile(
        Path.of("target", "tmp", "expectedPostalCodes.json").toFile(), postalCodes);

    var expected = FileUtils.readFileToString(postalCodesFile);

    assertEquals(expected, postalCodes);
  }
}
