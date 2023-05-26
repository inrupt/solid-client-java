/*
 * Copyright 2023 Inrupt Inc.
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

import org.junit.jupiter.api.Test;

class AccessGrantConfigurationTest {

    @Test
    void testDefaultConfig() {
        final URI issuer = URI.create("https://access.test");
        final AccessGrantConfiguration config = new AccessGrantConfiguration(issuer);
        assertEquals(AccessGrantConfiguration.GCONSENT, config.getSchema());
        config.setSchema(AccessGrantConfiguration.GCONSENT);
        assertEquals(AccessGrantConfiguration.GCONSENT, config.getSchema());
        assertEquals(issuer, config.getIssuer());
    }

    @Test
    void testInvalid() {
        final URI issuer = URI.create("https://access.test");
        final AccessGrantConfiguration config = new AccessGrantConfiguration(issuer);
        final URI uri = URI.create("https://example.com/");
        assertThrows(IllegalArgumentException.class, () -> config.setSchema(uri));
        assertThrows(IllegalArgumentException.class, () -> config.setSchema(null));
    }

}
