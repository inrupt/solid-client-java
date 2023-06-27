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
package com.inrupt.client.vocabulary;

import java.net.URI;

/**
 * URI constants from the PIM vocabulary.
 *
 * @see <a href="http://www.w3.org/ns/pim/space#">PIM vocabulary</a>
 */
public final class PIM {

    private static String namespace = "http://www.w3.org/ns/pim/space#";

    // Properties
    /**
     * The pim:storage URI.
     */
    public static final URI storage = URI.create(namespace + "storage");

    /**
     * Get the PIM namespace URI.
     *
     * @return the PIM namespace
     */
    public static URI getNamespace() {
        return URI.create(namespace);
    }

    private PIM() {
        // Prevent instantiation
    }
}

