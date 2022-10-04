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
package com.inrupt.client.http;

import java.util.List;

public final class Headers {

    /** 
     * Create a list of links based on a given header.
     * 
     * @param headers the values of the headers to be parsed
     * @return arrayList of links from header
     */
    public static List<Link> link(final String... headers) {
        return link(new HeaderParser(), headers);
    }

    /** 
     * Create a list of links based on a given header.
     * 
     * @param parser
     * @param headers the values of the headers to be parsed
     * @return arrayList of links from header
     */
    public static List<Link> link(final HeaderParser parser, final String... headers) {
        return parser.parseLinkHeaders(headers);
    }

    private Headers() {
        // Prevent instantiation
    }
}

