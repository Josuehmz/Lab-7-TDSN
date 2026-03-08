package com.lab7;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.lab7.http.Request;
import com.lab7.http.Response;
import com.lab7.http.RouteHandler;

public class WebFramework {

    private static final Map<String, RouteHandler> getRoutes = new HashMap<>();
    private static String staticFilesPath = null;
    private static final int DEFAULT_PORT = 8080;

    public static void staticfiles(String path) {
        staticFilesPath = path == null ? null : path.replace('\\', '/').replaceAll("/+$", "");
    }

    public static void get(String path, RouteHandler handler) {
        String normalized = path.replace('\\', '/');
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        getRoutes.put(normalized, handler);
    }

    public static void start() {
        start(DEFAULT_PORT);
    }

    public static void start(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server listening on port " + port + " ...");
            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    handleClient(clientSocket);
                } catch (IOException e) {
                    System.err.println("Accept or handle failed: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + port + ".");
            System.exit(1);
        }
    }

    private static void handleClient(Socket clientSocket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        OutputStream out = clientSocket.getOutputStream();

        String inputLine;
        String reqPath = "";
        String queryString = null;

        while ((inputLine = in.readLine()) != null) {
            if (inputLine.startsWith("GET ") || inputLine.startsWith("POST ")) {
                String[] tokens = inputLine.split(" ", 3);
                if (tokens.length >= 2) {
                    try {
                        URI uri = new URI(tokens[1]);
                        reqPath = uri.getPath();
                        queryString = uri.getQuery();
                    } catch (URISyntaxException e) {
                        reqPath = tokens[1].split("\\?")[0];
                    }
                }
                break;
            }
            if (!in.ready()) break;
        }

        while (in.ready()) in.read();

        buildResponse(reqPath, queryString, out);
        out.flush();
    }

    private static void buildResponse(String reqPath, String queryString, OutputStream out) throws IOException {
        if (reqPath == null) reqPath = "/";

        RouteHandler handler = getRoutes.get(reqPath);
        if (handler != null) {
            Request req = new Request(reqPath, queryString);
            Response resp = new Response();
            String body = handler.handle(req, resp);
            byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
            String headers = "HTTP/1.1 200 OK\r\nContent-Type: text/plain; charset=UTF-8\r\nContent-Length: " + bodyBytes.length + "\r\n\r\n";
            out.write(headers.getBytes(StandardCharsets.UTF_8));
            out.write(bodyBytes);
            return;
        }

        if (staticFilesPath != null) {
            String filePath = staticFilesPath + (reqPath.startsWith("/") ? reqPath : "/" + reqPath);
            if (!filePath.contains("..")) {
                InputStream is = WebFramework.class.getClassLoader().getResourceAsStream(filePath);
                if (is != null) {
                    byte[] data = readAllBytes(is);
                    String contentType = getContentType(filePath);
                    String headers = "HTTP/1.1 200 OK\r\nContent-Type: " + contentType + "\r\nContent-Length: " + data.length + "\r\n\r\n";
                    out.write(headers.getBytes(StandardCharsets.UTF_8));
                    out.write(data);
                    return;
                }
            }
        }

        byte[] notFoundBody = "404 Not Found".getBytes(StandardCharsets.UTF_8);
        String headers = "HTTP/1.1 404 Not Found\r\nContent-Type: text/plain; charset=UTF-8\r\nContent-Length: " + notFoundBody.length + "\r\n\r\n";
        out.write(headers.getBytes(StandardCharsets.UTF_8));
        out.write(notFoundBody);
    }

    private static byte[] readAllBytes(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = is.read(buf)) > 0) {
            baos.write(buf, 0, n);
        }
        return baos.toByteArray();
    }

    private static String getContentType(String path) {
        if (path.endsWith(".html")) return "text/html; charset=UTF-8";
        if (path.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (path.endsWith(".css")) return "text/css; charset=UTF-8";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        if (path.endsWith(".gif")) return "image/gif";
        if (path.endsWith(".ico")) return "image/x-icon";
        return "application/octet-stream";
    }
}
