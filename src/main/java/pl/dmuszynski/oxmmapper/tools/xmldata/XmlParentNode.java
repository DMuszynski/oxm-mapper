package pl.dmuszynski.oxmmapper.tools.xmldata;

import lombok.Getter;
import lombok.ToString;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Klasa definiująca opcjonalnego rodzica dla zagnieżdzonych węzłów
 * Zawiera informacje o ilości zagnieżdzonych węzłów oraz zbiór potomków
 */
@Getter
@ToString(callSuper = true)
public class XmlParentNode extends XmlNode {
    private final int depth;
    private final Set<XmlNode> children;

    public XmlParentNode(String name, String value, XmlParentNode parent, int depth) {
        super(name, value, parent);
        this.depth = depth;
        this.children = new LinkedHashSet<>();
    }
}
