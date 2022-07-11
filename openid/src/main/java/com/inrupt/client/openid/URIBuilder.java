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
package com.inrupt.client.openid;

import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

final class URIBuilder {

    private static int PAIR = 2;

    private String builderScheme;
    private String builderUserInfo;
    private String builderHost;
    private int builderPort;
    private String builderPath;
    private List<String> builderQueryParams = new ArrayList<>();
    private String builderFragment;

    public static URIBuilder newBuilder(final URI uri) {
        final var builder = new URIBuilder();
        builder.builderScheme = uri.getScheme();
        builder.builderUserInfo = uri.getUserInfo();
        builder.builderHost = uri.getHost();
        builder.builderPort = uri.getPort();
        builder.builderPath = uri.getPath();
        builder.builderFragment = uri.getFragment();
        final var params = uri.getQuery();
        if (params != null) {
            for (final var param : params.split("&")) {
                final var parts = param.split("=", 2);
                if (parts.length == PAIR) {
                    builder.builderQueryParams.add(encodeQueryParam(parts[0], parts[1]));
                }
            }
        }
        return builder;
    }

    public URIBuilder path(final String path) {
        if (builderPath.endsWith("/")) {
            builderPath += path;
        } else {
            builderPath += "/" + path;
        }
        return this;
    }

    public URIBuilder queryParam(final String param, final String value) {
        builderQueryParams.add(encodeQueryParam(param, value));
        return this;
    }

    public URIBuilder fragment(final String fragment) {
        builderFragment = fragment;
        return this;
    }

    public URI build() {
        try {
            return new URI(builderScheme, builderUserInfo, builderHost, builderPort, builderPath,
                    String.join("&", builderQueryParams), builderFragment);
        } catch (final URISyntaxException ex) {
            throw new IllegalArgumentException("Invalid URI value", ex);
        }
    }

    static String encodeQueryParam(final String key, final String value) {
        return encode(key, UTF_8) + "=" + encode(value, UTF_8);
    }

    private URIBuilder() {
        // Prevent direct instantiation
    }

}
