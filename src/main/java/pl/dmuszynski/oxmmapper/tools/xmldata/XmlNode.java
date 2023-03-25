package pl.dmuszynski.oxmmapper.tools.xmldata;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

public class XmlNode {

    private final String name;
    private final Object value;
    private final XmlParentNode parent;
    private final Set<XmlAttribute> attributes;

    public XmlNode(String name, Object value, XmlParentNode parent) {
        this.name = name;
        this.value = value;
        this.parent = parent;
        this.attributes = new LinkedHashSet<>();
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    public XmlParentNode getParent() {
        return parent;
    }

    public Set<XmlAttribute> getAttributes() {
        return attributes;
    }

    @Override
    public String toString() {
        return "XmlNode{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", parent=" + getParentName() +
                ", attributes=" + attributes +
                '}';
    }

    private String getParentName() {
        return Optional.ofNullable(parent)
                .map(XmlNode::getName)
                .orElse("null");
    }
}
