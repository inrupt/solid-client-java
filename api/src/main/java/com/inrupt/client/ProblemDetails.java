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

import com.inrupt.client.spi.JsonService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * A data class representing a structured problem description sent by the server on error response.
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc9457">RFC 9457 Problem Details for HTTP APIs</a>
 */
public class ProblemDetails {
    public static final String MIME_TYPE = "application/problem+json";
    public static final String DEFAULT_TYPE = "about:blank";
    private final URI type;
    private final String title;
    private final String details;
    private final int status;
    private final URI instance;

    public ProblemDetails(
        final URI type,
        final String title,
        final String details,
        final int status,
        final URI instance
    ) {
        // The `type` is not mandatory in RFC9457, so we want to set
        // a default value here even when deserializing from JSON.
        if (type != null) {
            this.type = type;
        } else {
            this.type = URI.create(DEFAULT_TYPE);
        }
        this.title = title;
        this.details = details;
        this.status = status;
        this.instance = instance;
    }

    public URI getType() {
        return this.type;
    };

    public String getTitle() {
        return this.title;
    };

    public String getDetails() {
        return this.details;
    };

    public int getStatus() {
        return this.status;
    };

    public URI getInstance() {
        return this.instance;
    };

    public static ProblemDetails fromErrorResponse(
            final int statusCode,
            final Headers headers,
            final byte[] body,
            final JsonService jsonService
    ) {
        if (jsonService == null
                || (headers != null && !headers.allValues("Content-Type").contains(ProblemDetails.MIME_TYPE))) {
            return new ProblemDetails(
                null,
                HttpStatus.StatusMessages.getStatusMessage(statusCode),
                null,
                statusCode,
                null
            );
        }
        try {
            // ProblemDetails doesn't have a default constructor, and we can't use JSON mapping annotations because
            // the JSON service is an abstraction over JSON-B and Jackson, so we deserialize the JSON object in a Map
            // and build the ProblemDetails from the Map values.
            final Map<String, Object> pdData = jsonService.fromJson(
                    new ByteArrayInputStream(body),
                    new HashMap<String, Object>(){}.getClass().getGenericSuperclass()
            );
            final String title = (String) pdData.get("title");
            final String details = (String) pdData.get("details");
            final URI type = URI.create((String) pdData.get("type"));
            final URI instance = URI.create((String) pdData.get("instance"));
            final int status = (int) pdData.get("status");
            return new ProblemDetails(type, title, details, status, instance);
        } catch (IOException e) {
            return new ProblemDetails(
                null,
                HttpStatus.StatusMessages.getStatusMessage(statusCode),
                null,
                statusCode,
                null
            );
        }
    }
}
