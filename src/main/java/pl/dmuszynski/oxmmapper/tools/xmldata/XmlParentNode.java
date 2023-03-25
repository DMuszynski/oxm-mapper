package pl.dmuszynski.oxmmapper.tools.xmldata;

import java.util.LinkedHashSet;
import java.util.Set;

public class XmlParentNode extends XmlNode {

    private final int depth;
    private final Set<XmlNode> children;

    public XmlParentNode(String name, String value, XmlParentNode parent, int depth) {
        super(name, value, parent);
        this.depth = depth;
        this.children = new LinkedHashSet<>();
    }

    public int getDepth() {
        return depth;
    }

    public Set<XmlNode> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        return "XmlParentNode{" +
                "depth=" + depth +
                ", children=" + children +
                '}';
    }
}
