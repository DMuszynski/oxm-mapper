package pl.dmuszynski.oxmmapper.context.model;

import pl.dmuszynski.oxmmapper.tools.xmldata.XmlAdapter;

import lombok.experimental.SuperBuilder;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.AccessLevel;
import lombok.Getter;

import java.lang.reflect.Field;

@Getter
@SuperBuilder
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class ElementData {
    protected final Field field;
    protected final String name;
    protected final Class<? extends XmlAdapter<?, ?>> adapter;
}
