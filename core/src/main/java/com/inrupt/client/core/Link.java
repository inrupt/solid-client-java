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

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A class for representing an HTTP Link header.
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc8288">RFC 8288</a>
 */
public final class Link {

    private final URI uri;
    private final Map<String, String> parameters;

    private Link(final URI uri, final Map<String, String> parameters) {
        this.uri = Objects.requireNonNull(uri);
        this.parameters = Objects.requireNonNull(parameters);
    }

    public URI getUri() {
        return uri;
    }

    /**
     * Get the value of the given parameter.
     *
     * @param parameter the parameter name
     * @return the parameter value, may be {@code null}
     */
    public String getParameter(final String parameter) {
        return parameters.get(parameter);
    }

    /**
     * Get all the parameters for this Link.
     *
     * @return the complete collection of parameters
     */
    public Map<String, String> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }

    @Override
    public String toString() {
        if (parameters.isEmpty()) {
            return "<" + getUri() + ">";
        }
        return "<" + getUri() + ">; " + parameters.entrySet().stream()
            .map(e -> e.getKey() + "=\"" + e.getValue() + "\"")
            .collect(Collectors.joining("; "));
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof Link)) {
            return false;
        }

        final Link c = (Link) obj;

        if (!this.getUri().equals(c.getUri())) {
            return false;
        }

        return Objects.equals(new HashMap<>(parameters), new HashMap<>(c.parameters));
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getUri(), new HashMap<>(parameters));
    }

    /**
     * Create a new Link object with a specific URI-Reference and parameters.
     *
     * @param uri the link URI
     * @param parameters the link parameters
     * @return the new {@link Link} object
     */
    public static Link of(final URI uri, final Map<String, String> parameters) {
        return new Link(uri, parameters);
    }
}
