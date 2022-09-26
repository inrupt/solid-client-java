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
package com.inrupt.client.http;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Part of the HTTP Challenge and Response authentication framework, this class represents a
 * challenge object as represented in a WWW-Authenticate Response Header.
 *
 * @see <a href="https://httpwg.org/specs/rfc8288#header">RFC 7235 2.1</a>
 */
public class LinkValue{

    private final URI uriReference;
    private final Map<String, String> parameters;

    /**
     * Create a new LinkValue object with a specific URI-Reference and no parameters.
     *
     * @param scheme the authentication scheme
     * @throws URISyntaxException
     */
    public LinkValue(final String uriReference) {
        this(uriReference, Collections.emptyMap());
    }

    /**
     * Create a new LinkValue object with a specific URI-Reference and parameters.
     *
     * @param scheme the authentication scheme
     * @param parameters the authentication parameters
     * @throws URISyntaxException
     */
    public LinkValue(final String uriReference, final Map<String, String> parameters) {
        this.uriReference = URI.create(Objects.requireNonNull(uriReference));
        this.parameters = Objects.requireNonNull(parameters);
    }

    /**
     * Get the URI-Reference for this LinkValue.
     *
     * @return the scheme name
     */
    public URI getUri() {
        return uriReference;
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
     * Get all the parameters for this LinkHeader.
     *
     * @return the complete collection of parameters
     */
    public Map<String, String> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }

    @Override
    public String toString() {
        return getUri() + " " + parameters.entrySet().stream()
            .map(e -> e.getKey() + "=\"" + e.getValue() + "\"")
            .collect(Collectors.joining(", "));
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof LinkValue)) {
            return false;
        }

        final LinkValue c = (LinkValue) obj;

        if (!this.getUri().equals(c.getUri())) {
            return false;
        }

        return Objects.equals(parameters, c.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getUri(), parameters);
    }
    
}
