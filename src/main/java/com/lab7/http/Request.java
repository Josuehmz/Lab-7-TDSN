package com.lab7.http;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class Request {

    private final String path;
    private final Map<String, String> queryParams;

    public Request(String path, String queryString) {
        this.path = path;
        this.queryParams = new HashMap<>();
        if (queryString != null && !queryString.isEmpty()) {
            parseQueryString(queryString);
        }
    }

    public Request(URI uri) {
        this(uri.getPath(), uri.getQuery());
    }

    private void parseQueryString(String queryString) {
        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            int eq = pair.indexOf('=');
            if (eq >= 0) {
                String key = decode(pair.substring(0, eq).trim());
                String value = decode(pair.substring(eq + 1).trim());
                queryParams.put(key, value);
            } else if (!pair.trim().isEmpty()) {
                queryParams.put(decode(pair.trim()), "");
            }
        }
    }

    private static String decode(String s) {
        try {
            return java.net.URLDecoder.decode(s, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            return s;
        }
    }

    public String getValues(String name) {
        return queryParams.get(name);
    }

    public String getPath() {
        return path;
    }
}
