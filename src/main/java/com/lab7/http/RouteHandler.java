package com.lab7.http;

@FunctionalInterface
public interface RouteHandler {
    String handle(Request req, Response resp);
}
