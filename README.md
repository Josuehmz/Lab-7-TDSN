# Lab 7 – Web Server and IoC Framework

A minimal Java web server (Apache-style) that serves HTML and PNG files and provides a reflection-based IoC framework to build web applications from POJOs. The server handles multiple requests sequentially (non-concurrent).

## Features

- **Static files**: Serves HTML, PNG, CSS, JS from `src/main/resources/webroot/`.
- **REST from POJOs**: Controllers are plain Java classes annotated with `@RestController`. Methods annotated with `@GetMapping` are exposed as GET endpoints (return type `String`).
- **Query parameters**: `@RequestParam(value = "name", defaultValue = "World")` binds query parameters to method arguments.
- **Two ways to load controllers**:
  1. **Command line**: Pass controller class name(s) as arguments.
  2. **Classpath scanning**: With no arguments, the framework scans the package `co.edu.escuelaing.reflexionlab` for all `@RestController` classes and registers them.

## Requirements

- Java 11+
- Maven 3.x

## Build

```bash
mvn clean compile
```

## Run

**Option 1 – Classpath scanning (all `@RestController` in the package):**

```bash
mvn exec:java
```

Or:

```bash
java -cp target/classes co.edu.escuelaing.reflexionlab.MicroSpringBoot
```

**Option 2 – Explicit controller class(es):**

```bash
java -cp target/classes co.edu.escuelaing.reflexionlab.MicroSpringBoot co.edu.escuelaing.reflexionlab.FirstWebService
```

With multiple controllers:

```bash
java -cp target/classes co.edu.escuelaing.reflexionlab.MicroSpringBoot co.edu.escuelaing.reflexionlab.FirstWebService co.edu.escuelaing.reflexionlab.GreetingController
```

**Maven run with one controller:**

```bash
mvn exec:java -Dexec.executionId=with-controller
```

The server listens on **http://localhost:8080**.

## Example endpoints (with default scan)

| URL | Description |
|-----|-------------|
| `/` | Greetings from Spring Boot! |
| `/hello` | Hello from FirstWebService |
| `/greeting` | Hola World (default) |
| `/greeting?name=YourName` | Hola YourName |
| `/index.html` | Static HTML from webroot |
| `/image.png` | Static PNG from webroot |

## Example controller (no parameters)

```java
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
```

## Example controller with @RequestParam

```java
@RestController
public class GreetingController {

    @GetMapping("/greeting")
    public String greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        return "Hola " + name;
    }
}
```

## Project structure (Maven)

- **Source**: `src/main/java/`
  - `com.lab7`: Web server and routing (`WebFramework`, `Request`, `Response`, `RouteHandler`).
  - `co.edu.escuelaing.reflexionlab`: IoC entry point and annotations (`MicroSpringBoot`, `@RestController`, `@GetMapping`, `@RequestParam`, example controllers).
- **Static resources**: `src/main/resources/webroot/` (HTML, CSS, PNG, etc.).
- **Tests**: `src/test/java/`.

Lifecycle and dependencies are managed with Maven (`pom.xml`).

## GitHub

The project is stored in the student’s GitHub account. Clone or push using the repository URL.

## AWS deployment (evidence)

To run the application on AWS:

1. **Build a runnable JAR** (with dependencies, if you add a shade plugin), or copy `target/classes` and run with `java -cp ... MicroSpringBoot`.
2. **Deploy** to an EC2 instance (or similar): install Java 11, copy the built artifact, and run the same `java -cp ...` or `java -jar ...` command.
3. **Open port 8080** in the security group for the instance.
4. **Evidence**: A screenshot or short description showing the server running on the AWS instance (e.g. browser accessing `http://<public-ip>:8080/` or `http://<public-ip>:8080/greeting?name=AWS`).

Example after deployment:

- `http://<your-ec2-public-ip>:8080/` → Greetings from Spring Boot!
- `http://<your-ec2-public-ip>:8080/greeting?name=AWS` → Hola AWS

## License

Educational use (Lab 7 – TDSN).
