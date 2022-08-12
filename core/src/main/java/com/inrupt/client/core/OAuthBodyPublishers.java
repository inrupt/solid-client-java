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
package com.inrupt.client.core;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.util.Map;
import java.util.stream.Collectors;

public final class OAuthBodyPublishers {

    private static final String EQUALS = "=";
    private static final String ETC = "&";

    /**
     * Convert a {@link Map} into a body publisher, serialized as a {@code application/x-www-form-urlencoded} string.
     *
     * @param data the input data
     * @return the body publisher
     */
    public static HttpRequest.BodyPublisher ofFormData(final Map<String, String> data) {
        final var form = data.entrySet().stream().map(entry -> {
            final var name = URLEncoder.encode(entry.getKey(), UTF_8);
            final var value = URLEncoder.encode(entry.getValue(), UTF_8);
            return String.join(EQUALS, name, value);
        }).collect(Collectors.joining(ETC));

        return HttpRequest.BodyPublishers.ofString(form);
    }

    private OAuthBodyPublishers() {
        // Prevent instantiation
    }
}
