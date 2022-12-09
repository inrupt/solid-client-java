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

import java.net.URI;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface Session {

    /**
     * Retrieve the identifier associated with this session.
     *
     * @return a session identifier
     */
    String getId();

    /**
     * Retrieve the authentication schemes supported by this session.
     *
     * @return the scheme identifiers
     */
    Set<String> supportedSchemes();

    /**
     * Retrieve a credential from this session.
     *
     * @param name the credential name
     * @return the credential, if present
     */
    Optional<Credential> getCredential(String name);

    /**
     * Retrieve an access token for a request from a cache.
     *
     * @param request the HTTP request
     * @return the access token, if present
     */
    Optional<Credential> fromCache(Request request);

    class Credential {
        private final String scheme;
        private final URI issuer;
        private final String token;
        private final Instant expiration;

        public Credential(final String scheme, final URI issuer, final String token, final Instant expiration) {
            this.scheme = scheme;
            this.issuer = issuer;
            this.token = token;
            this.expiration = expiration;
        }

        public String getScheme() {
            return scheme;
        }

        public URI getIssuer() {
            return issuer;
        }

        public String getToken() {
            return token;
        }

        public Instant getExpiration() {
            return expiration;
        }
    }

    /**
     * Create a new anonymous session.
     *
     * @implNote This {@link Session} does not keep a cache of access tokens.
     * @return the session
     */
    static Session anonymous() {
        final String sessionId = UUID.randomUUID().toString();
        return new Session() {
            @Override
            public String getId() {
                return sessionId;
            }

            @Override
            public Set<String> supportedSchemes() {
                return Collections.singleton("UMA");
            }

            @Override
            public Optional<Credential> getCredential(final String name) {
                return Optional.empty();
            }

            @Override
            public Optional<Credential> fromCache(final Request request) {
                return Optional.empty();
            }
        };
    }
}
