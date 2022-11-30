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

import com.inrupt.client.Authenticator.Challenge;
import com.inrupt.client.spi.ServiceProvider;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

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

    /**
     * A class for representing an HTTP Link header.
     *
     * @see <a href="https://www.rfc-editor.org/rfc/rfc8288">RFC 8288</a>
     */
    public static final class Link {

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

        /**
         * A parser to convert link header string representations into a collection of links.
         *
         * @param headers the link headers
         * @return a list of links
         */
        public static List<Link> parse(final String... headers) {
            return ServiceProvider.getHeaderParser().parseLink(Arrays.asList(headers));
        }
    }

    /**
     * Part of the HTTP Challenge and Response authentication framework, this class represents a
     * challenge object as represented in a WWW-Authenticate Response Header.
     *
     * @see <a href="https://httpwg.org/specs/rfc7235.html#challenge.and.response">RFC 7235 2.1</a>
     */
    public static final class WwwAuthenticate {

        private final List<Challenge> challenges;

        private WwwAuthenticate(final List<Challenge> challenges) {
            this.challenges = Objects.requireNonNull(challenges);
        }

        /**
         * Get the challenges associated with this HTTP authentication interaction.
         *
         * @return the challenges
         */
        public List<Challenge> getChallenges() {
            return Collections.unmodifiableList(challenges);
        }

        /**
         * Create a new WWW-Authenticate object with a collection of challenges.
         *
         * @param challenges the challenges
         * @return the www-authenticate object
         */
        public static WwwAuthenticate of(final Challenge... challenges) {
            return of(Arrays.asList(challenges));
        }

        /**
         * Create a new WWW-Authenticate object with a collection of challenges.
         *
         * @param challenges the challenges
         * @return the www-authenticate object
         */
        public static WwwAuthenticate of(final List<Challenge> challenges) {
            return new WwwAuthenticate(challenges);
        }

        /**
         * Parse header strings into a list of Challenge objects.
         *
         * @param headers the header strings
         * @return the challenge objects
         */
        public static WwwAuthenticate parse(final String... headers) {
            return ServiceProvider.getHeaderParser().parseWwwAuthenticate(Arrays.asList(headers));
        }
    }

    /**
     * a class for parsing WAC-allow headers.
     *
     * @see <a href="https://solidproject.org/TR/wac#wac-allow">Solid TR: Web Access Control</a>
     */
    public static final class WacAllow {

        private final Map<String, Set<String>> accessParams;

        private WacAllow(final Map<String, Set<String>> accessParams) {
            this.accessParams = Objects.requireNonNull(accessParams);
        }

        /**
         * Create a new WAC-Allow object with a collection of Access Parameters.
         *
         * @param accessParams the Access Parameters
         * @return the WAC-Allow object
         */
        public static WacAllow of(final Map<String, Set<String>> accessParams) {
            return new WacAllow(accessParams);
        }

        /**
         * Get the Access Parameters associated with this HTTP WAC-Allow interaction.
         *
         * @return the  Access Parameters
         */
        public Map<String, Set<String>> getAccessParams() {
            return Collections.unmodifiableMap(accessParams);
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == this) {
                return true;
            }

            if (!(obj instanceof WacAllow)) {
                return false;
            }

            final WacAllow c = (WacAllow) obj;

            return Objects.equals(new HashMap<>(accessParams), new HashMap<>(c.accessParams));
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.getAccessParams());
        }

        /**
         * Parse header strings into a WacAllow object.
         *
         * @param headers the header strings
         * @return WAC-Allow object containig Access Parameters from headers
         */
        public static WacAllow parse(final String... headers) {
            return ServiceProvider.getHeaderParser().parseWacAllow(Arrays.asList(headers));
        }
    }
}
