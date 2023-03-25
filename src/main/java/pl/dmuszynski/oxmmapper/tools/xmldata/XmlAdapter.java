package pl.dmuszynski.oxmmapper.tools.xmldata;

/**
 * Interfejs reprezentujący adapter kowertujący dwa obiekty
 * @param <E> Obiekt wejściowy
 * @param <R> Obiekt wyjściowy
 */
public interface XmlAdapter<E, R> {
    R convert(E object);
}
