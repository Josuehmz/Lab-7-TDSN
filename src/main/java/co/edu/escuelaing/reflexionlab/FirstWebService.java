package co.edu.escuelaing.reflexionlab;

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
