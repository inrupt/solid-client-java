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
package com.inrupt.client.vocabulary;

import java.net.URI;

/**
 * URI constants from the LDP vocabulary.
 *
 * @see <a href="https://www.w3.org/ns/ldp">LDP Vocabulary</a>
 */
public final class LDP {

    private static String namespace = "http://www.w3.org/ns/ldp#";

    // Properties
    /**
     * The ldp:contains URI.
     */
    public static final URI contains = URI.create(namespace + "contains");
    /**
     * The ldp:inbox URI.
     */
    public static final URI inbox = URI.create(namespace + "inbox");

    // Classes
    /**
     * The ldp:RDFSource URI.
     */
    public static final URI RDFSource = URI.create(namespace + "RDFSource");
    /**
     * The ldp:BasicContainer URI.
     */
    public static final URI BasicContainer = URI.create(namespace + "BasicContainer");

    /**
     * Get the LDP namespace URI.
     *
     * @return the LDP namespace
     */
    public static URI getNamespace() {
        return URI.create(namespace);
    }

    private LDP() {
        // Prevent instantiation
    }
}
