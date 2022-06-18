package io.github.aangiel.teryt.teryt;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

@RequiredArgsConstructor(staticName = "create", access = AccessLevel.PRIVATE)
@ToString(exclude = "parent")
@EqualsAndHashCode(of = {"code", "name", "description"})
@Getter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TerytNode implements Serializable {

    @JsonIgnore
    private final TerytNode parent;
    private final String name;
    private final String code;
    private final String description;
    private final XMLGregorianCalendar date;

//    @JsonIgnore
//    private final ImmutableSet.Builder<TerytNode> children = ImmutableSet.builder();

    private final Set<TerytNode> children = new TreeSet<>(Comparator.comparing(child -> child.name));

    static TerytNode createRoot() {
        return create(null, "", "root", "", null);
    }

    TerytNode addOrGetChild(String name) {
        return addOrGetChild(name, "");
    }

    TerytNode addOrGetChild(XMLGregorianCalendar date) {
        return addOrGetChild("", "", "", date);
    }

    TerytNode addOrGetChild(String name, XMLGregorianCalendar date) {
        return addOrGetChild(name, "", "", date);
    }

    TerytNode addOrGetChild(String name, String code) {
        return addOrGetChild(name, code, "");
    }

    TerytNode addOrGetChild(String name, String code, String description) {
        return addOrGetChild(name, code, description, null);
    }

    TerytNode addOrGetChild(String name, String code, String description, XMLGregorianCalendar date) {
        var child = getChild(name);
        if (child.isPresent()) {
            return child.get();
        }
        var newNode = create(this, name, code, description, date);
        children.add(newNode);
        return newNode;
    }

    private Optional<TerytNode> getChild(String name) {
        for (var child : children) {
            if (name.equals(child.name))
                return Optional.of(child);
        }
        return Optional.empty();
    }

//    @JsonProperty
//    Set<TerytNode> getChildren() {
//        return children.build();
//    }
}
