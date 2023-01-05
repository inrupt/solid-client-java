/*
 * Copyright 2023 Inrupt Inc.
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

import com.inrupt.client.spi.ServiceProvider;

import java.net.URI;
import java.security.KeyPair;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * An interface for handling authentication challenges.
 */
public interface Authenticator {

    /**
     * Gets the authenticator name (e.g. UMA, OpenID).
     *
     * @return the name
     */
    String getName();

    /**
     * Gets the priority of the authenticator.
     *
     * @return the priority
     */
    int getPriority();

    /**
     * Perform an ansynchronous authentication process, resulting in an access token.
     *
     * @param session the client session
     * @param request the HTTP request
     * @param algorithms the supported DPoP algorithms
     * @return the next stage of completion, containing the access token
     */
    CompletionStage<Credential> authenticate(Session session, Request request, Set<String> algorithms);

    /**
     * Part of the HTTP Challenge and Response authentication framework, this class represents a
     * challenge object as represented in a WWW-Authenticate Response Header.
     *
     * @see <a href="https://httpwg.org/specs/rfc7235.html#challenge.and.response">RFC 7235 2.1</a>
     */
    final class Challenge {

        private final String scheme;
        private final Map<String, String> parameters;

        /**
         * Create a new Challenge object with a specific authentication scheme and no parameters.
         *
         * @param scheme the authentication scheme
         * @return the challenge
         */
        public static Challenge of(final String scheme) {
            return of(scheme, Collections.emptyMap());
        }

        /**
         * Create a new Challenge object with a specific authentication scheme and parameters.
         *
         * @param scheme the authentication scheme
         * @param parameters the authentication parameters
         * @return the challenge
         */
        public static Challenge of(final String scheme, final Map<String, String> parameters) {
            return new Challenge(scheme, parameters);
        }

        private Challenge(final String scheme, final Map<String, String> parameters) {
            this.scheme = Objects.requireNonNull(scheme);
            this.parameters = Objects.requireNonNull(parameters);
        }

        /**
         * Get the authentication scheme for this challenge.
         *
         * @return the scheme name
         */
        public String getScheme() {
            return scheme;
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
         * Get all the parameters for this challenge.
         *
         * @return the complete collection of parameters
         */
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
            return Objects.hash(scheme.toLowerCase(Locale.ENGLISH), parameters);
        }
    }

    /**
     * An abstraction for working with OAuth 2.0 Demonstrating Proof-of-Possession at the Application Layer (DPoP).
     *
     * @see <a href="https://datatracker.ietf.org/doc/html/draft-ietf-oauth-dpop">DPoP draft specification</a>
     */
    interface DPoP {

        /**
         * Generate a DPoP proof for a given URI and method pair.
         *
         * @param algorithm the algorithm to use
         * @param uri the HTTP URI
         * @param method the HTTP method
         * @return the DPoP Proof, serialized as a Base64-encoded string, suitable for use with HTTP headers
         */
        String generateProof(String algorithm, URI uri, String method);

        /**
         * Return a collection of the supported algorithm names.
         *
         * @return the algorithm names
         */
        Set<String> algorithms();

        /**
         * Retrieve the algorithm for the given thumbprint, if available.
         *
         * @param jkt the JSON Key Thumbprint
         * @return the algorithm, if present
         */
        Optional<String> lookupAlgorithm(String jkt);

        /**
         * Retrieve the thumbprint for a given algorithm, if available.
         *
         * @param algorithm the algorithm
         * @return the thumbprint, if present
         */
        Optional<String> lookupThumbprint(String algorithm);

        /**
         * Create a DPoP manager that supports a default keypair.
         *
         * @return the DPoP manager
         */
        static DPoP of() {
            return of(Collections.emptyMap());
        }

        /**
         * Create a DPoP manager that supports some number of keypairs.
         *
         * @param keypairs the keypairs
         * @return the DPoP manager
         */
        static DPoP of(final Map<String, KeyPair> keypairs) {
            return ServiceProvider.getDpopService().ofKeyPairs(keypairs);
        }
    }
}

