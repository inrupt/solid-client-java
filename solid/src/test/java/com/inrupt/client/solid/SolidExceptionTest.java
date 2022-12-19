/*
 * Copyright 2022 Inrupt Inc.
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
package com.inrupt.client.solid;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SolidExceptionTest {

    @Test
    void checkSolidException() {
        final String msg = "Error";
        final SolidResourceException err = new SolidResourceException(msg);
        assertEquals(msg, err.getMessage());
    }

    @Test
    void checkSolidWrappedException() {
        final Throwable upstream = new IllegalArgumentException("Bad input");
        final String msg = "Invalid input";
        final SolidResourceException err = new SolidResourceException(msg, upstream);
        assertEquals(upstream, err.getCause());
        assertEquals(msg, err.getMessage());
    }
}
