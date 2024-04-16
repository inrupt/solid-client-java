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
 * A runtime exception representing an HTTP error response carrying a structured representation of the problem. The
 * problem description is embedded in a {@link ProblemDetails} instance.
 */
public class ClientHttpException extends InruptClientException {
    private final ProblemDetails problemDetails;
    private final URI uri;
    private final int statusCode;
    private final String body;
    private final transient Headers headers;

    /**
     * Create a ClientHttpException.
     * @param message the exception message
     * @param uri the error response URI
     * @param statusCode the error response status code
     * @param headers the error response headers
     * @param body the error response body
     */
    public ClientHttpException(final String message, final URI uri, final int statusCode,
                                final Headers headers, final String body) {
        super(message);
        this.uri = uri;
        this.statusCode = statusCode;
        this.headers = headers;
        this.body = body;
        this.problemDetails = ProblemDetails.fromErrorResponse(statusCode, headers, body.getBytes());
    }

    /**
     * Retrieve the URI associated with this exception.
     *
     * @return the uri
     */
    public URI getUri() {
        return uri;
    }

    /**
     * Retrieve the status code associated with this exception.
     *
     * @return the status code
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Retrieve the headers associated with this exception.
     *
     * @return the headers
     */
    public Headers getHeaders() {
        return headers;
    }

    /**
     * Retrieve the body associated with this exception.
     *
     * @return the body
     */
    public String getBody() {
        return body;
    }

    /**
     * Retrieve the {@link ProblemDetails} instance describing the HTTP error response.
     * @return the problem details object
     */
    public ProblemDetails getProblemDetails() {
        return this.problemDetails;
    }

}
