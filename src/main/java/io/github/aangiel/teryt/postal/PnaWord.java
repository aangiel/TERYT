package io.github.aangiel.teryt.postal;

import org.apache.pdfbox.text.TextPosition;

import java.util.List;

record PnaWord(String text, List<TextPosition> positions) {
    float getX() {
        return positions.get(0).getXDirAdj();
    }

    float getY() {
        return positions.get(0).getYDirAdj();
    }
}
