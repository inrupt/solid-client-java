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

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A class containing information about an OAuth 2.0 access token.
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc6749#section-1.4">OAuth 2.0 Authorization Framework.
 * Section 1.4: Access Token</a>
 */
public class AccessToken {

    private final Instant expiration;
    private final List<String> scopes;
    private final String token;
    private final String type;
    private final String algorithm;

    /**
     * Create a new {@link AccessToken}.
     *
     * @param token the access token value
     * @param type the access token type, e.g. Bearer or DPoP
     * @param expiration the access token expiration
     * @param scopes a list of scopes for this access token
     * @param algorithm the proofing algorithm used for this access token, may be {@code null}
     */
    public AccessToken(final String token, final String type, final Instant expiration,
                final List<String> scopes, final String algorithm) {
        this.token = Objects.requireNonNull(token);
        this.type = Objects.requireNonNull(type);
        this.expiration = Objects.requireNonNull(expiration);
        this.scopes = Objects.requireNonNull(scopes);
        this.algorithm = algorithm;
    }

    /**
     * Retrieve a list of scopes for this token.
     *
     * @return the scopes
     */
    public List<String> getScopes() {
        return scopes;
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
