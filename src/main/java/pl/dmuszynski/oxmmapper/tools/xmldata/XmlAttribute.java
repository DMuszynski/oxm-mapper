package pl.dmuszynski.oxmmapper.tools.xmldata;

import java.util.Objects;

public class XmlAttribute {

    private final String name;
    private final Object value;

    public XmlAttribute(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof XmlAttribute)) return false;
        XmlAttribute that = (XmlAttribute) o;
        return Objects.equals(name, that.name) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }

    @Override
    public String toString() {
        return "XmlAttribute{" +
                "name='" + name + '\'' +
                ", value=" + value +
                '}';
    }
}
