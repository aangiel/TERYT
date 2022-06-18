package io.github.aangiel.teryt.teryt;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    TerytNode addChild(String name) {
        return addChild(name, "");
    }

    TerytNode addChild(XMLGregorianCalendar date) {
        return addChild("", "", "", date);
    }

    TerytNode addChild(String name, XMLGregorianCalendar date) {
        return addChild(name, "", "", date);
    }

    TerytNode addChild(String name, String code) {
        return addChild(name, code, "");
    }

    TerytNode addChild(String name, String code, String description) {
        return addChild(name, code, description, null);
    }

    TerytNode addChild(String name, String code, String description, XMLGregorianCalendar date) {
        var newNode = create(this, name, code, description, date);
        children.add(newNode);
        return newNode;
    }

    Optional<TerytNode> getChild(String name) {
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
