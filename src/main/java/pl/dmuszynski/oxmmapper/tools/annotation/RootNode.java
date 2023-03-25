package pl.dmuszynski.oxmmapper.tools.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Adnotacja przechwytująca nazwę klasy
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RootNode {
    String name() default "defaultName";
}
