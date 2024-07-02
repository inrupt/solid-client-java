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
import com.inrupt.client.ProblemDetails;
import com.inrupt.client.spi.JsonService;
import com.inrupt.client.spi.ServiceProvider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolidProblemDetails implements ProblemDetails {

    private static final Logger LOGGER = LoggerFactory.getLogger(SolidProblemDetails.class);
    private static final long serialVersionUID = -4597170432270957765L;

    private final URI type;
    private final String title;
    private final String detail;
    private final int status;
    private final URI instance;
    private static JsonService jsonService;
    private static boolean isJsonServiceInitialized;

    /**
     * Build a ProblemDetails instance providing the expected fields as described in
     * <a href="https://www.rfc-editor.org/rfc/rfc9457">RFC9457</a>.
     * @param type the problem type
     * @param title the problem title
     * @param detail the problem details
     * @param status the error response status code
     * @param instance the problem instance
     */
    public SolidProblemDetails(
        final URI type,
        final String title,
        final String detail,
        final int status,
        final URI instance
    ) {
        this.type = type;
        this.title = title;
        this.detail = detail;
        this.status = status;
        this.instance = instance;
    }

    @Override
    public URI getType() {
        return this.type;
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    public String getDetail() {
        return this.detail;
    }

    @Override
    public int getStatus() {
        return this.status;
    }

    @Override
    public URI getInstance() {
        return this.instance;
    }

    /**
     * This inner class is only ever used for JSON deserialization. Please do not use in any other context.
     */
    static class Data {
        /**
         * The problem type.
         */
        public URI type;
        /**
         * The problem title.
         */
        public String title;
        /**
         * The problem detail.
         */
        public String detail;
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
        if (SolidProblemDetails.isJsonServiceInitialized) {
            return SolidProblemDetails.jsonService;
        }
        // It is a legitimate use case that this is loaded in a context where no implementation of the JSON service is
        // available. On SPI lookup failure, the ProblemDetails exceptions will fall back to default and not be parsed.
        try {
            SolidProblemDetails.jsonService = ServiceProvider.getJsonService();
        } catch (IllegalStateException e) {
            SolidProblemDetails.jsonService = null;
        }
        SolidProblemDetails.isJsonServiceInitialized = true;
        return SolidProblemDetails.jsonService;
    }

    /**
     * Builds a {@link ProblemDetails} instance from an HTTP error response.
     * @param uri the original URI
     * @param statusCode the HTTP error response status code
     * @param headers the HTTP error response headers
     * @param body the HTTP error response body
     * @return a {@link ProblemDetails} instance
     */
    static SolidProblemDetails fromErrorResponse(final URI uri, final int statusCode, final Headers headers,
            final byte[] body) {
        final JsonService jsonService = getJsonService();
        if (jsonService != null
                && (headers != null && headers.allValues("Content-Type").contains(ProblemDetails.MIME_TYPE))) {
            try (final InputStream input = new ByteArrayInputStream(body)) {
                final Data pdData = jsonService.fromJson(input, Data.class);
                final URI type = Optional.ofNullable(pdData.type)
                    .map(uri::resolve)
                    .orElse(ProblemDetails.DEFAULT_TYPE);
                // JSON mappers map invalid integers to 0, which is an invalid value in our case anyway.
                final int status = Optional.of(pdData.status).filter(s -> s != 0).orElse(statusCode);
                return new SolidProblemDetails(type, pdData.title, pdData.detail, status, pdData.instance);
            } catch (IOException e) {
                LOGGER.debug("Unable to parse ProblemDetails response from server", e);
            }
        }
        return new SolidProblemDetails(ProblemDetails.DEFAULT_TYPE, null, null, statusCode, null);
    }
}
