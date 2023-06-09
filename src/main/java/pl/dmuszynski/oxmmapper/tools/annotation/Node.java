package pl.dmuszynski.oxmmapper.tools.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Adnotacja przechwytująca nazwe pola w klasie
 * Węzły reprezentują pola opakowane
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Node {
    String name() default "defaultName";
}
