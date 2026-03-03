package co.edu.escuelaing.reflexionlab;

/**
 * Servicio web de ejemplo: POJO descubierto por reflexión y publicado por MicroSpringBoot.
 */
@RestController
public class FirstWebService {

    @GetMapping("/")
    public String index() {
        return "Greetings from Spring Boot!";
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello from FirstWebService (reflexión).";
    }
}
