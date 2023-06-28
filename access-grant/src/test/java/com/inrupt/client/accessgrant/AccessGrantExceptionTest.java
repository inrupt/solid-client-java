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
package com.inrupt.client.accessgrant;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AccessGrantExceptionTest {

    @Test
    void testWithMessage() {
        final String msg = "Expected error";
        final AccessGrantException err = new AccessGrantException(msg);
        assertEquals(msg, err.getMessage());
        assertFalse(err.getStatusCode().isPresent());
    }

    @Test
    void testWithMessageException() {
        final String msg = "Expected error";
        final RuntimeException cause = new RuntimeException("Underlying cause");
        final AccessGrantException err = new AccessGrantException(msg, cause);
        assertEquals(msg, err.getMessage());
        assertEquals(cause, err.getCause());
        assertFalse(err.getStatusCode().isPresent());
    }

    @Test
    void testWithMessageStatus() {
        final String msg = "Expected error";
        final AccessGrantException err = new AccessGrantException(msg, 401);
        assertEquals(msg, err.getMessage());
        assertTrue(err.getStatusCode().isPresent());
        assertEquals(401, err.getStatusCode().getAsInt());
    }

    @Test
    void testWithMessageStatusNotHttp() {
        final String msg = "Expected error";
        final AccessGrantException err = new AccessGrantException(msg, 10);
        assertEquals(msg, err.getMessage());
        assertFalse(err.getStatusCode().isPresent());
    }

    @Test
    void testWithMessageStatusException() {
        final String msg = "Expected error";
        final RuntimeException cause = new RuntimeException("Underlying cause");
        final AccessGrantException err = new AccessGrantException(msg, 404, cause);
        assertEquals(msg, err.getMessage());
        assertEquals(cause, err.getCause());
        assertTrue(err.getStatusCode().isPresent());
        assertEquals(404, err.getStatusCode().getAsInt());
    }

}
