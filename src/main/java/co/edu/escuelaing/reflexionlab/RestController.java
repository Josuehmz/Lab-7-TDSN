package co.edu.escuelaing.reflexionlab;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca una clase como controlador REST; sus métodos anotados con @GetMapping
 * se publican como servicios GET en el servidor.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RestController {
}
