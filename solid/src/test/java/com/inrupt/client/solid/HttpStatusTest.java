package com.inrupt.client.solid;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class HttpStatusTest {
    @Test
    void checkHttpStatusSearchKnownStatus() {
        assertEquals(
            HttpStatus.getStatusMessage(HttpStatus.NOT_FOUND.code), HttpStatus.NOT_FOUND.message
        );
    }

    @Test
    void checkHttpStatusSearchUnknownClientError () {
        assertEquals(
                HttpStatus.getStatusMessage(418), HttpStatus.BAD_REQUEST.message
        );
    }

    @Test
    void checkHttpStatusSearchUnknownServerError () {
        assertEquals(
                HttpStatus.getStatusMessage(555), HttpStatus.INTERNAL_SERVER_ERROR.message
        );
        assertEquals(
                HttpStatus.getStatusMessage(999), HttpStatus.INTERNAL_SERVER_ERROR.message
        );
    }
}
