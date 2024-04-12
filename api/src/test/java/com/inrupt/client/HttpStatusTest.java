/*
 * Copyright Inrupt Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.inrupt.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class HttpStatusTest {
    @Test
    void checkHttpStatusSearchKnownStatus() {
        assertEquals(
            HttpStatus.StatusMessages.getStatusMessage(HttpStatus.NOT_FOUND), HttpStatus.StatusMessages.NOT_FOUND.message
        );
    }

    @Test
    void checkHttpStatusSearchUnknownClientError () {
        assertEquals(
                HttpStatus.StatusMessages.getStatusMessage(418), HttpStatus.StatusMessages.BAD_REQUEST.message
        );
    }

    @Test
    void checkHttpStatusSearchUnknownServerError () {
        assertEquals(
                HttpStatus.StatusMessages.getStatusMessage(555), HttpStatus.StatusMessages.INTERNAL_SERVER_ERROR.message
        );
        assertEquals(
                HttpStatus.StatusMessages.getStatusMessage(999), HttpStatus.StatusMessages.INTERNAL_SERVER_ERROR.message
        );
        assertEquals(
                HttpStatus.StatusMessages.getStatusMessage(-1), HttpStatus.StatusMessages.INTERNAL_SERVER_ERROR.message
        );
        assertEquals(
                HttpStatus.StatusMessages.getStatusMessage(15), HttpStatus.StatusMessages.INTERNAL_SERVER_ERROR.message
        );
    }
}
