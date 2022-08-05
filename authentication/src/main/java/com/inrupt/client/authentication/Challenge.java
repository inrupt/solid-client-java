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
package com.inrupt.client.authentication;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Part of the HTTP Challenge and Response authentication framework, this class represents a
 * challenge object as represented in a WWW-Authenticate Response Header.
 *
 * @see <a href="https://httpwg.org/specs/rfc7235.html#challenge.and.response">RFC 7235 2.1</a>
 */
public class Challenge {

    private final String scheme;
    private final Map<String, String> parameters;

    public Challenge(final String scheme) {
        this(scheme, Collections.emptyMap());
    }

    public Challenge(final String scheme, final Map<String, String> parameters) {
        this.scheme = Objects.requireNonNull(scheme);
        this.parameters = Objects.requireNonNull(parameters);
    }

    public String getScheme() {
        return scheme;
    }

    public String getParameter(final String parameter) {
        return parameters.get(parameter);
    }

    public Map<String, String> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }

    @Override
    public String toString() {
        return getScheme() + " " + parameters.entrySet().stream()
            .map(e -> e.getKey() + "=\"" + e.getValue() + "\"")
            .collect(Collectors.joining(", "));
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof Challenge)) {
            return false;
        }

        final Challenge c = (Challenge) obj;

        if (!scheme.equalsIgnoreCase(c.scheme)) {
            return false;
        }

        return Objects.equals(parameters, c.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scheme, parameters);
    }
}
