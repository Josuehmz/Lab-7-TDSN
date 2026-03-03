package co.edu.escuelaing.reflexionlab;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Publica el método en la URI indicada para peticiones GET.
 * Soporta únicamente métodos que retornan String.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface GetMapping {
    /** URI del servicio, por ejemplo "/" o "/hello". */
    String value() default "/";
}
