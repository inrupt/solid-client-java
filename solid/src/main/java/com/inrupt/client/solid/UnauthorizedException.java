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
package com.inrupt.client.solid;

import com.inrupt.client.Headers;
import com.inrupt.client.HttpStatus;
import com.inrupt.client.ProblemDetails;

import java.net.URI;

/**
 * A runtime exception that represents an HTTP unauthorized (401) response.
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#status.401">RFC 9110 (15.5.2.) 401 Unauthorized</a>
 */
public class UnauthorizedException extends SolidClientException {
    private static final long serialVersionUID = -3219668517323678497L;

    public static final int STATUS_CODE = HttpStatus.UNAUTHORIZED;

    /**
     * Create an UnauthorizedException exception.
     *
     * @param message the message
     * @param uri the uri
     * @param headers the response headers
     * @param body the body
     */
    public UnauthorizedException(
            final String message,
            final URI uri,
            final Headers headers,
            final String body) {
        super(message, uri, STATUS_CODE, headers, body);
    }
}
