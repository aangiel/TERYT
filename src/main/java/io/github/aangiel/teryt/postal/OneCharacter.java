package io.github.aangiel.teryt.postal;

import org.apache.pdfbox.text.TextPosition;

record OneCharacter(TextPosition character, TextPosition firstCharacter) {
    public static OneCharacter create(TextPosition character, TextPosition firstCharacter) {
        return new OneCharacter(character, firstCharacter);
    }


}
