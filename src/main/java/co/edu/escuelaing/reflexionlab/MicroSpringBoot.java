package co.edu.escuelaing.reflexionlab;

import java.lang.reflect.Method;
import com.lab6.WebFramework;
import com.lab6.http.Request;
import com.lab6.http.Response;

/**
 * Punto de entrada tipo Spring Boot que carga un POJO (clase) desde la línea de comandos,
 * usa reflexión para descubrir métodos anotados con @GetMapping y publica esos servicios
 * en el servidor web. También sirve archivos estáticos (HTML, PNG, etc.).
 * <p>
 * Uso: java -cp target/classes co.edu.escuelaing.reflexionlab.MicroSpringBoot &lt;clasePOJO&gt;
 * Ejemplo: java -cp target/classes co.edu.escuelaing.reflexionlab.MicroSpringBoot co.edu.escuelaing.reflexionlab.FirstWebService
 */
public class MicroSpringBoot {

    private static final int PORT = 8080;

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            System.err.println("Uso: java -cp target/classes co.edu.escuelaing.reflexionlab.MicroSpringBoot <clasePOJO>");
            System.err.println("Ejemplo: java -cp target/classes co.edu.escuelaing.reflexionlab.MicroSpringBoot co.edu.escuelaing.reflexionlab.FirstWebService");
            System.exit(1);
        }

        String controllerClassName = args[0].trim();
        try {
            Class<?> controllerClass = Class.forName(controllerClassName);

            if (!controllerClass.isAnnotationPresent(RestController.class)) {
                System.err.println("La clase " + controllerClassName + " debe estar anotada con @RestController");
                System.exit(1);
            }

            Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance();

            for (Method method : controllerClass.getDeclaredMethods()) {
                GetMapping getMapping = method.getAnnotation(GetMapping.class);
                if (getMapping == null) continue;

                if (method.getParameterCount() != 0) {
                    System.err.println("Se omite " + method.getName() + ": @GetMapping solo soporta métodos sin parámetros.");
                    continue;
                }
                if (method.getReturnType() != String.class) {
                    System.err.println("Se omite " + method.getName() + ": @GetMapping solo soporta retorno String.");
                    continue;
                }

                String path = normalizePath(getMapping.value());
                method.setAccessible(true);
                final Method m = method;
                final Object instance = controllerInstance;
                WebFramework.get(path, (Request req, Response resp) -> {
                    try {
                        return (String) m.invoke(instance);
                    } catch (Exception e) {
                        Throwable cause = e.getCause() != null ? e.getCause() : e;
                        throw new RuntimeException("Error invocando " + m.getName(), cause);
                    }
                });
                System.out.println("GET " + path + " -> " + controllerClass.getSimpleName() + "." + method.getName() + "()");
            }

            WebFramework.staticfiles("webroot");
            System.out.println("Servidor escuchando en http://localhost:" + PORT + " (archivos estáticos: webroot)");
            WebFramework.start(PORT);

        } catch (ClassNotFoundException e) {
            System.err.println("Clase no encontrada: " + controllerClassName);
            System.exit(1);
        } catch (NoSuchMethodException e) {
            System.err.println("La clase " + controllerClassName + " debe tener un constructor público sin argumentos.");
            System.exit(1);
        } catch (ReflectiveOperationException e) {
            System.err.println("Error al cargar el controlador: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static String normalizePath(String path) {
        if (path == null || path.isEmpty()) return "/";
        path = path.replace('\\', '/');
        if (!path.startsWith("/")) path = "/" + path;
        return path;
    }
}
