package pl.dmuszynski.oxmmapper.tools.xmldata;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Klasa definiująca podstawowe właściwości pól w klasie
 */
@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class XmlProperty {
    private final String name;
    private final Object value;
}
