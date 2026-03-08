package com.lab7;

public class Application {

    public static void main(String[] args) {
        WebFramework.staticfiles("webroot");
        WebFramework.get("/App/hello", (req, resp) -> "Hello " + (req.getValues("name") != null ? req.getValues("name") : ""));
        WebFramework.get("/App/pi", (req, resp) -> String.valueOf(Math.PI));
        WebFramework.start(8080);
    }
}
