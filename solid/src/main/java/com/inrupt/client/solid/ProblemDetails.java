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
import com.inrupt.client.spi.JsonService;
import com.inrupt.client.spi.ServiceProvider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;

/**
 * A data class representing a structured problem description sent by the server on error response.
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc9457">RFC 9457 Problem Details for HTTP APIs</a>
 */
public class ProblemDetails {

    private static final long serialVersionUID = -4597170432270957765L;

    /**
     * The <a href="https://www.rfc-editor.org/rfc/rfc9457">RFC9457</a> default MIME type.
     */
    public static final String MIME_TYPE = "application/problem+json";
    /**
     * The <a href="https://www.rfc-editor.org/rfc/rfc9457">RFC9457</a> default problem type.
     */
    public static final String DEFAULT_TYPE = "about:blank";
    private final URI type;
    private final String title;
    private final String details;
    private final int status;
    private final URI instance;
    private static JsonService jsonService;
    private static boolean isJsonServiceInitialized;

    /**
     * Build a ProblemDetails instance providing the expected fields as described in
     * <a href="https://www.rfc-editor.org/rfc/rfc9457">RFC9457</a>.
     * @param type the problem type
     * @param title the problem title
     * @param details the problem details
     * @param status the error response status code
     * @param instance the problem instance
     */
    public ProblemDetails(
        final URI type,
        final String title,
        final String details,
        final int status,
        final URI instance
    ) {
        this.type = type;
        this.title = title;
        this.details = details;
        this.status = status;
        this.instance = instance;
    }

    /**
     * The problem type.
     * @return the type
     */
    public URI getType() {
        return this.type;
    }

    /**
     * The problem title.
     * @return the title
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * The problem details.
     * @return the details
     */
    public String getDetails() {
        return this.details;
    }

    /**
     * The problem status code.
     * @return the status code
     */
    public int getStatus() {
        return this.status;
    }

    /**
     * The problem instance.
     * @return the instance
     */
    public URI getInstance() {
        return this.instance;
    }

    /**
     * This inner class is only ever used for JSON deserialization. Please do not use in any other context.
     */
    public static class Data {
        /**
         * The problem type.
         */
        public URI type;
        /**
         * The problem title.
         */
        public String title;
        /**
         * The problem details.
         */
        public String details;
        /**
         * The problem status code.
         */
        public int status;
        /**
         * The problem instance.
         */
        public URI instance;
    }

    private static JsonService getJsonService() {
        if (ProblemDetails.isJsonServiceInitialized) {
            return ProblemDetails.jsonService;
        }
        // It is a legitimate use case that this is loaded in a context where no implementation of the JSON service is
        // available. On SPI lookup failure, the ProblemDetails exceptions will fall back to default and not be parsed.
        try {
            ProblemDetails.jsonService = ServiceProvider.getJsonService();
        } catch (IllegalStateException e) {
            ProblemDetails.jsonService = null;
        }
        ProblemDetails.isJsonServiceInitialized = true;
        return ProblemDetails.jsonService;
    }

    /**
     * Builds a {@link ProblemDetails} instance from an HTTP error response.
     * @param statusCode the HTTP error response status code
     * @param headers the HTTP error response headers
     * @param body the HTTP error response body
     * @return a {@link ProblemDetails} instance
     */
    public static ProblemDetails fromErrorResponse(
            final int statusCode,
            final Headers headers,
            final byte[] body
    ) {
        final JsonService jsonService = getJsonService();
        if (jsonService == null
                || (headers != null && !headers.allValues("Content-Type").contains(ProblemDetails.MIME_TYPE))) {
            return new ProblemDetails(
                URI.create(ProblemDetails.DEFAULT_TYPE),
                null,
                null,
                statusCode,
                null
            );
        }
        try {
            final Data pdData = jsonService.fromJson(
                    new ByteArrayInputStream(body),
                    Data.class
            );
            final URI type = Optional.ofNullable(pdData.type)
                .orElse(URI.create(ProblemDetails.DEFAULT_TYPE));
            // JSON mappers map invalid integers to 0, which is an invalid value in our case anyway.
            final int status = Optional.of(pdData.status).filter(s -> s != 0).orElse(statusCode);
            return new ProblemDetails(
                    type,
                    pdData.title,
                    pdData.details,
                    status,
                    pdData.instance
            );
        } catch (IOException e) {
            return new ProblemDetails(
                URI.create(ProblemDetails.DEFAULT_TYPE),
                null,
                null,
                statusCode,
                null
            );
        }
    }
}
