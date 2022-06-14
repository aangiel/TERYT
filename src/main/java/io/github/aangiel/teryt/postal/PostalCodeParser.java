package io.github.aangiel.teryt.postal;

import lombok.extern.log4j.Log4j;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Log4j
public final class PostalCodeParser {

    private final PostalCodeStripper postalCodeStripper;

    private PostalCodeParser(File pdfToStrip) throws IOException {
        this.postalCodeStripper = new PostalCodeStripper(pdfToStrip);
    }

    public static PostalCodeParser create(File pdfToStrip) throws IOException {
        return new PostalCodeParser(pdfToStrip);
    }

    public List<Map<String, String>> parse() {

        var pnaDocument = this.postalCodeStripper.strip();
        return pnaDocument.stream()
                .flatMap(PnaPage::stream)
                .toList();
    }

}