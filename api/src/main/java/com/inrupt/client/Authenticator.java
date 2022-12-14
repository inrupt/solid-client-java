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

import com.inrupt.client.spi.ServiceProvider;

import java.net.URI;
import java.security.KeyPair;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
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
     * Perform a synchronous authentication process, resulting in an access token.
     *
     * @param session the client session
     * @param request the HTTP request
     * @return the access token
     */
    AccessToken authenticate(Session session, Request request);

    /**
     * Perform an ansynchronous authentication process, resulting in an access token.
     *
     * @param session the client session
     * @param request the HTTP request
     * @return the next stage of completion, containing the access token
     */
    CompletionStage<AccessToken> authenticateAsync(Session session, Request request);

    /**
     * A class containing information about an OAuth 2.0 access token.
     *
     * @see <a href="https://www.rfc-editor.org/rfc/rfc6749#section-1.4">OAuth 2.0 Authorization Framework.
     * Section 1.4: Access Token</a>
     */
    class AccessToken {

        private final Instant expiration;
        private final List<String> scopes;
        private final String token;
        private final String type;
        private final String algorithm;
        private final URI issuer;

        /**
         * Create a new {@link AccessToken}.
         *
         * @param token the access token value
         * @param type the access token type, e.g. Bearer or DPoP
         * @param expiration the access token expiration
         * @param issuer the access token issuer
         * @param scopes a list of scopes for this access token
         * @param algorithm the proofing algorithm used for this access token, may be {@code null}
         */
        public AccessToken(final String token, final String type, final Instant expiration,
                    final URI issuer, final List<String> scopes, final String algorithm) {
            this.token = Objects.requireNonNull(token);
            this.type = Objects.requireNonNull(type);
            this.expiration = Objects.requireNonNull(expiration);
            this.scopes = Objects.requireNonNull(scopes);
            this.issuer = Objects.requireNonNull(issuer);
            this.algorithm = algorithm;
        }

        /**
         * Retrieve a list of scope values for this token.
         *
         * @return the scope
         */
        public List<String> getScope() {
            return scopes;
        }

        /**
         * Retrieve the issuer for this token.
         *
         * @return the issuer
         */
        public URI getIssuer() {
            return issuer;
        }

        /**
         * Retrieve the expriation time for this token.
         *
         * @return the expiration time
         */
        public Instant getExpiration() {
            return expiration;
        }

        /**
         * Retrieve the token value.
         *
         * @return the token value
         */
        public String getToken() {
            return token;
        }

        /**
         * Retrieve the token type (e.g., Bearer or DPoP).
         *
         * @return the type
         */
        public String getType() {
            return type;
        }

        /**
         * Retrieve the proofing algorithm, if present.
         *
         * @return the proofing algorithm
         */
        public Optional<String> getProofAlgorithm() {
            return Optional.ofNullable(algorithm);
        }
    }


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
         * Create a DPoP manager with a default algorithm and keypair.
         *
         * @return the DPoP manager
         */
        static DPoP of() {
            return of(Collections.emptyMap());
        }

        /**
         * Create a DPoP manager that supports a single algorithm and keypair.
         *
         * @param algorithm the algorithm
         * @param keypair the keypair
         * @return the DPoP manager
         */
        static DPoP of(final String algorithm, final KeyPair keypair) {
            return of(Collections.singletonMap(algorithm, keypair));
        }

        /**
         * Create a DPoP manager that supports a collection of algorithms and keypairs.
         *
         * @param keypairs the algorithm-keypair combinations
         * @return the DPoP manager
         */
        static DPoP of(final Map<String, KeyPair> keypairs) {
            return ServiceProvider.getDpopService().ofKeyPairs(keypairs);
        }
    }
}

