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
package com.inrupt.client;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

/**
 * A read-only view of a collection of HTTP headers.
 */
public final class Headers {

    private final NavigableMap<String, List<String>> data = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    /**
     * Get the first value of a header, if it exists.
     *
     * @param name the header name
     * @return the first value, if present
     */
    public Optional<String> firstValue(final String name) {
        final List<String> values = data.get(Objects.requireNonNull(name));
        if (values != null && !values.isEmpty()) {
            return Optional.of(values.get(0));
        }
        return Optional.empty();
    }

    /**
     * Get all values for a header.
     *
     * @param name the header name
     * @return the values for the header. If no values are present, an empty list will be returned
     */
    public List<String> allValues(final String name) {
        final List<String> values = data.get(Objects.requireNonNull(name));
        if (values != null) {
            return Collections.unmodifiableList(values);
        }
        return Collections.emptyList();
    }

    public Map<String, List<String>> asMap() {
        return Collections.unmodifiableNavigableMap(data);
    }

    public static Headers of(final Map<String, List<String>> headers) {
        return new Headers(Objects.requireNonNull(headers));
    }

    private Headers(final Map<String, List<String>> headers) {
        this.data.putAll(headers);
    }
}
