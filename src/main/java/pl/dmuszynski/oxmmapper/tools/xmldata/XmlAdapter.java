package pl.dmuszynski.oxmmapper.tools.xmldata;

public interface XmlAdapter<E, R> {
    R convert(E object);
}
