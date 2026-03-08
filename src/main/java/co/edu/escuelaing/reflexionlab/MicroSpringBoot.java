package co.edu.escuelaing.reflexionlab;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import com.lab7.WebFramework;
import com.lab7.http.Request;
import com.lab7.http.Response;

public class MicroSpringBoot {

    private static final int PORT = 8080;
    private static final String SCAN_PACKAGE = "co.edu.escuelaing.reflexionlab";

    public static void main(String[] args) {
        List<String> controllerClassNames = new ArrayList<>();
        if (args != null && args.length > 0) {
            for (String a : args) {
                if (a != null && !a.trim().isEmpty()) controllerClassNames.add(a.trim());
            }
        } else {
            controllerClassNames = findRestControllerClasses(SCAN_PACKAGE);
            if (controllerClassNames.isEmpty()) {
                System.err.println("No @RestController classes found in package " + SCAN_PACKAGE + ". Pass controller class name(s) as argument(s).");
                System.err.println("Example: java -cp target/classes co.edu.escuelaing.reflexionlab.MicroSpringBoot co.edu.escuelaing.reflexionlab.FirstWebService");
                System.exit(1);
            }
            System.out.println("Discovered controllers: " + controllerClassNames);
        }

        try {
            for (String controllerClassName : controllerClassNames) {
                Class<?> controllerClass = Class.forName(controllerClassName);

                if (!controllerClass.isAnnotationPresent(RestController.class)) {
                    System.err.println("Class " + controllerClassName + " must be annotated with @RestController; skipping.");
                    continue;
                }

                Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance();
                registerController(controllerClass, controllerInstance);
            }

            WebFramework.staticfiles("webroot");
            System.out.println("Server listening on http://localhost:" + PORT + " (static files: webroot)");
            WebFramework.start(PORT);

        } catch (ClassNotFoundException e) {
            System.err.println("Class not found: " + e.getMessage());
            System.exit(1);
        } catch (NoSuchMethodException e) {
            System.err.println("Controller must have a public no-args constructor: " + e.getMessage());
            System.exit(1);
        } catch (ReflectiveOperationException e) {
            System.err.println("Error loading controller: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void registerController(Class<?> controllerClass, Object controllerInstance) throws ReflectiveOperationException {
        for (Method method : controllerClass.getDeclaredMethods()) {
            GetMapping getMapping = method.getAnnotation(GetMapping.class);
            if (getMapping == null) continue;

            if (method.getReturnType() != String.class) {
                System.err.println("Skipping " + method.getName() + ": @GetMapping only supports String return type.");
                continue;
            }

            Parameter[] parameters = method.getParameters();
            RequestParam[] paramAnnotations = new RequestParam[parameters.length];
            boolean validParams = true;
            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i].getType() != String.class) {
                    System.err.println("Skipping " + method.getName() + ": parameters must be String.");
                    validParams = false;
                    break;
                }
                RequestParam rp = parameters[i].getAnnotation(RequestParam.class);
                if (rp == null) {
                    System.err.println("Skipping " + method.getName() + ": all parameters must have @RequestParam.");
                    validParams = false;
                    break;
                }
                paramAnnotations[i] = rp;
            }
            if (!validParams) continue;

            String path = normalizePath(getMapping.value());
            method.setAccessible(true);
            final Method m = method;
            final Object instance = controllerInstance;
            final RequestParam[] annos = paramAnnotations;
            WebFramework.get(path, (Request req, Response resp) -> {
                try {
                    Object[] args = new Object[parameters.length];
                    for (int i = 0; i < parameters.length; i++) {
                        String paramName = annos[i].value().isEmpty() ? parameters[i].getName() : annos[i].value();
                        String val = req.getValues(paramName);
                        if (val == null && !annos[i].defaultValue().isEmpty()) {
                            val = annos[i].defaultValue();
                        }
                        args[i] = val != null ? val : "";
                    }
                    return (String) m.invoke(instance, args);
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    throw new RuntimeException("Error invoking " + m.getName(), cause);
                }
            });
            System.out.println("GET " + path + " -> " + controllerClass.getSimpleName() + "." + method.getName() + "()");
        }
    }

    private static List<String> findRestControllerClasses(String basePackage) {
        List<String> result = new ArrayList<>();
        String path = basePackage.replace('.', '/');
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) cl = MicroSpringBoot.class.getClassLoader();
        try {
            Enumeration<URL> resources = cl.getResources(path);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                if ("file".equals(url.getProtocol())) {
                    File dir = new File(url.toURI());
                    if (dir.isDirectory()) {
                        findClassesInDir(dir, basePackage, result);
                    }
                } else if ("jar".equals(url.getProtocol())) {
                    String jarPath = url.getPath();
                    int sep = jarPath.indexOf("!");
                    if (sep >= 0) {
                        try (JarFile jar = new JarFile(jarPath.substring(0, sep).replaceFirst("^file:", ""))) {
                            Enumeration<JarEntry> entries = jar.entries();
                            while (entries.hasMoreElements()) {
                                JarEntry entry = entries.nextElement();
                                String name = entry.getName();
                                if (name.startsWith(path) && name.endsWith(".class") && !name.contains("$")) {
                                    String className = name.substring(0, name.length() - 6).replace('/', '.');
                                    if (isRestController(className)) result.add(className);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Scan error: " + e.getMessage());
        }
        return result;
    }

    private static void findClassesInDir(File dir, String packageName, List<String> result) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) {
                findClassesInDir(f, packageName + "." + f.getName(), result);
            } else if (f.getName().endsWith(".class") && !f.getName().contains("$")) {
                String className = packageName + "." + f.getName().substring(0, f.getName().length() - 6);
                if (isRestController(className)) result.add(className);
            }
        }
    }

    private static boolean isRestController(String className) {
        try {
            Class<?> c = Class.forName(className);
            return c.isAnnotationPresent(RestController.class);
        } catch (Throwable e) {
            return false;
        }
    }

    private static String normalizePath(String path) {
        if (path == null || path.isEmpty()) return "/";
        path = path.replace('\\', '/');
        if (!path.startsWith("/")) path = "/" + path;
        return path;
    }
}
