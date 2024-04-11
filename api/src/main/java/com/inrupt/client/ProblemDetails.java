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
}
