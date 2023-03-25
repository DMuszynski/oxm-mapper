package pl.dmuszynski.oxmmapper.tools.annotation;

import pl.dmuszynski.oxmmapper.tools.xmldata.XmlAdapter;

import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NodeAdapter {
    Class<? extends XmlAdapter<?, ?>> classType();
}
