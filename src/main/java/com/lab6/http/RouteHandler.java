package com.lab6.http;

@FunctionalInterface
public interface RouteHandler {
    String handle(Request req, Response resp);
}
