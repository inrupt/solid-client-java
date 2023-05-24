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
package com.inrupt.client.solid;

import com.inrupt.client.Headers;

import java.net.URI;

/**
 * A runtime exception that represents an HTTP forbidden (403) response.
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#status.403">RFC 9110 (15.5.4.) 403 Forbidden</a>
 */
public class ForbiddenException extends SolidClientException {
    private static final long serialVersionUID = 3299286274724874244L;

    public static final int STATUS_CODE = 403;

    /**
     * Create a ForbiddenException exception.
     *
     * @param message the message
     * @param uri the uri
     * @param headers the response headers
     * @param body the body
     */
    public ForbiddenException(
            final String message,
            final URI uri,
            final Headers headers,
            final String body) {
        super(message, uri, STATUS_CODE, headers, body);
    }
}
