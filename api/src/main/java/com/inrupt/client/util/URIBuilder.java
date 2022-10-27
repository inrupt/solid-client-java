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
package com.inrupt.client.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility for building {@link URI} objects.
 */
public final class URIBuilder {

    private static int PAIR = 2;

    private String scheme;
    private StringBuilder schemeSpecificPart;
    private List<String> queryParams = new ArrayList<>();
    private String uriFragment;

    /**
     * Create a new URI builder from an existing URI.
     *
     * @param uri the URI
     * @return the builder
     */
    public static URIBuilder newBuilder(final URI uri) {
        final URIBuilder builder = new URIBuilder();
        builder.scheme = uri.getScheme();
        builder.uriFragment = uri.getFragment();

        final String[] parts = uri.getSchemeSpecificPart().split("\\?", PAIR);

        builder.schemeSpecificPart = new StringBuilder(parts[0]);
        if (parts.length == PAIR) {
            for (final String param : parts[1].split("&")) {
                builder.queryParams.add(param);
            }
        }

        return builder;
    }

    /**
     * Append a path segment to a URI.
     *
     * @param path the path segment
     * @return this builder
     */
    public URIBuilder path(final String path) {
        if (path != null && !path.isEmpty() && !path.trim().isEmpty()) {
            if (schemeSpecificPart.charAt(schemeSpecificPart.length() - 1) == '/') {
                schemeSpecificPart.append(path.startsWith("/") ? path.substring(1) : path);
            } else {
                schemeSpecificPart.append(path.startsWith("/") ? path : "/" + path);
            }
        }
        return this;
    }

    /**
     * Set a query parameter for a URI.
     *
     * @param param the parameter name
     * @param value the parameter value
     * @return this builder
     */
    public URIBuilder queryParam(final String param, final String value) {
        queryParams.add(formatQueryParam(param, value));
        return this;
    }

    /**
     * Set a fragment value for a URI.
     *
     * @param fragment the fragment value
     * @return this builder
     */
    public URIBuilder fragment(final String fragment) {
        uriFragment = fragment;
        return this;
    }

    /**
     * Build a URI.
     *
     * @return the newly constructed URI
     */
    public URI build() {
        if (!queryParams.isEmpty()) {
            schemeSpecificPart.append("?");
            schemeSpecificPart.append(String.join("&", queryParams));
        }
        try {
            return new URI(scheme, schemeSpecificPart.toString(), uriFragment);
        } catch (final URISyntaxException ex) {
            throw new IllegalArgumentException("Invalid URI value", ex);
        }
    }

    static String formatQueryParam(final String key, final String value) {
        return formatParam(key) + "=" + formatParam(value);
    }

    static String formatParam(final String param) {
        if (param != null) {
            return param;
        }
        return "";
    }

    private URIBuilder() {
        // Prevent direct instantiation
    }
}
