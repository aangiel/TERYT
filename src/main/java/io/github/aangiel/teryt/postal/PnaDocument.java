package io.github.aangiel.teryt.postal;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

@NoArgsConstructor(access = AccessLevel.PACKAGE, staticName = "create")
@ToString
final class PnaDocument {

    private final List<PnaPage> pages = new LinkedList<>();

    PnaPage getPage(int pageNumber) {
        if (pageNumber < pages.size() - 1) {
            return pages.get(pageNumber);
        } else {
            var page = PnaPage.create();
            pages.add(page);
            return page;
        }
    }

    Stream<PnaPage> stream() {
        return pages.stream();
    }
}
