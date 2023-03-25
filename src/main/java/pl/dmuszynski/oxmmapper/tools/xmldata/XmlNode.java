package pl.dmuszynski.oxmmapper.tools.xmldata;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Klasa definiująca węzeł pola przypisany do zagnieżdzonych obiektów
 * Różnica pomiędzy XmlProperty jest taka, że zagnieżdzone obiekty mogą mieć swoich rodziców i atrybuty
 */
@Getter
@ToString
@EqualsAndHashCode
public class XmlNode {
    private final String name;
    private final Object value;
    private final XmlParentNode parent;
    private final Set<XmlProperty> attributes;

    public XmlNode(String name, Object value, XmlParentNode parent) {
        this.name = name;
        this.value = value;
        this.parent = parent;
        this.attributes = new LinkedHashSet<>();
    }
}
