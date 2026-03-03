package com.lab6;

import com.lab6.http.Request;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RequestTest {

    @Test
    void getValues_returnsParameterWhenPresent() {
        Request req = new Request("/App/hello", "name=Pedro");
        assertEquals("Pedro", req.getValues("name"));
    }

    @Test
    void getValues_returnsNullWhenMissing() {
        Request req = new Request("/App/hello", "other=value");
        assertNull(req.getValues("name"));
    }

    @Test
    void getValues_worksWithEmptyQuery() {
        Request req = new Request("/App/pi", null);
        assertNull(req.getValues("name"));
    }

    @Test
    void getPath_returnsPath() {
        Request req = new Request("/App/hello", "name=Pedro");
        assertEquals("/App/hello", req.getPath());
    }
}
