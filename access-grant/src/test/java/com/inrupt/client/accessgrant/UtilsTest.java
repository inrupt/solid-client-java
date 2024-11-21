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

import java.net.URI;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class UtilsTest {

    @Test
    void testAsList() {
        assertEquals(Optional.empty(), Utils.asList("not a list"));
        final Object list = Arrays.asList("one", 2, 12.5f);
        assertEquals(Optional.of(list), Utils.asList(list));
    }

    @Test
    void testQueryParam() {
        final URI uri = URI.create("https://example.com/?query&page=foo&item=bar");
        assertEquals("foo", Utils.getQueryParam(uri, "page"));
        assertEquals("bar", Utils.getQueryParam(uri, "item"));
        assertNull(Utils.getQueryParam(uri, "query"));
        assertNull(Utils.getQueryParam(uri, "other"));
        assertNull(Utils.getQueryParam(URI.create("https://example.com/path"), "param"));
    }
}
