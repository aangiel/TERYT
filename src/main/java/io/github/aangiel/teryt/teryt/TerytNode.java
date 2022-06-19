package io.github.aangiel.teryt.teryt;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.Serializable;
import java.util.Comparator;
import java.util.TreeMap;

@RequiredArgsConstructor(staticName = "create", access = AccessLevel.PRIVATE)
@ToString(exclude = "parent")
@EqualsAndHashCode(exclude = {"parent", "children"})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Getter
public class TerytNode implements Serializable {

    @JsonIgnore
    private final TerytNode parent;
    private final String name;
    private final String code;
    private final String description;
    private final String extraName;
    private final String type;
    private final XMLGregorianCalendar date;

    private final TreeMap<String, TerytNode> children = new TreeMap<>(Comparator.naturalOrder());

    static TerytNode createRoot() {
        return TerytNode.builder()
                .code("00")
                .name("root").build();
    }

    public static TerytNodeBuilder builder() {
        return new TerytNodeBuilder();
    }

    TerytNode addChild(TerytNodeBuilder childBuilder) {
        var child = childBuilder.parent(this).build();
        children.put(child.code, child);
        return child;
    }

    TerytNode addChildIfNotExists(TerytNodeBuilder childBuilder) {
        var child = getChildByCode(childBuilder.code);
        if (child == null)
            return addChild(childBuilder);
        return child;
    }

    TerytNode getChildByCode(String code) {
        return children.get(code);
    }


    public static class TerytNodeBuilder {
        private TerytNode parent;
        private String name;
        private String code;
        private String description;
        private String extraName;
        private String type;
        private XMLGregorianCalendar date;

        TerytNodeBuilder() {
        }

        private TerytNodeBuilder parent(TerytNode parent) {
            this.parent = parent;
            return this;
        }

        public TerytNodeBuilder name(String name) {
            this.name = name;
            return this;
        }

        public TerytNodeBuilder code(String code) {
            this.code = code;
            return this;
        }

        public TerytNodeBuilder description(String description) {
            this.description = description;
            return this;
        }

        public TerytNodeBuilder extraName(String extraName) {
            this.extraName = extraName;
            return this;
        }

        public TerytNodeBuilder type(String type) {
            this.type = type;
            return this;
        }

        public TerytNodeBuilder date(XMLGregorianCalendar date) {
            this.date = date;
            return this;
        }

        public TerytNode build() {
            return new TerytNode(parent, name, code, description, extraName, type, date);
        }

        public String toString() {
            return "TerytNode.TerytNodeBuilder(parent=" + this.parent + ", name=" + this.name + ", code=" + this.code + ", description=" + this.description + ", date=" + this.date + ")";
        }
    }
}
