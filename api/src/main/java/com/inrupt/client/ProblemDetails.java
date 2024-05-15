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

import java.net.URI;

/**
 * A data class representing a structured problem description sent by the server on error response.
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc9457">RFC 9457 Problem Details for HTTP APIs</a>
 */
public interface ProblemDetails {

    /**
     * The <a href="https://www.rfc-editor.org/rfc/rfc9457">RFC9457</a> default MIME type.
     */
    String MIME_TYPE = "application/problem+json";

    /**
     * The <a href="https://www.rfc-editor.org/rfc/rfc9457">RFC9457</a> default problem type.
     */
    String DEFAULT_TYPE = "about:blank";

    /**
     * The problem type.
     * @return the type
     */
    URI getType();

    /**
     * The problem title.
     * @return the title
     */
    String getTitle();

    /**
     * The problem details.
     * @return the details
     */
    String getDetails();

    /**
     * The problem status code.
     * @return the status code
     */
    int getStatus();

    /**
     * The problem instance.
     * @return the instance
     */
    URI getInstance();
}
