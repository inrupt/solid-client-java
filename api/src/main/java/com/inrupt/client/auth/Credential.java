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
package com.inrupt.client.auth;

import java.net.URI;
import java.time.Instant;
import java.util.Optional;

/**
 * A credential that can be used with Solid resource servers.
 */
public class Credential {
    private final String scheme;
    private final URI issuer;
    private final String token;
    private final Instant expiration;
    private final URI principal;
    private final String jkt;

    /**
     * Create a credential.
     *
     * @param scheme the scheme
     * @param issuer the issuer
     * @param token the token
     * @param expiration the time after which the credential is no longer valid
     * @param principal the identifier for the principal, may be {@code null}
     * @param jkt the json key thumbprint, may be {@code null}
     */
    public Credential(final String scheme, final URI issuer, final String token, final Instant expiration,
            final URI principal, final String jkt) {
        this.scheme = Objects.requireNonNull(scheme, "scheme may not be null!");
        this.issuer = Objects.requireNonNull(issuer, "issuer may not be null!");
        this.token = Objects.requireNonNull(token, "token may not be null!");
        this.expiration = Objects.requireNonNull(expiration, "expiration may not be null!");
        this.principal = principal;
        this.jkt = jkt;
    }

    /**
     * Get the scheme for this credential.
     *
     * @return the scheme
     */
    public String getScheme() {
        return scheme;
    }

    /**
     * Get the principal, if available.
     *
     * @return the principal
     */
    public Optional<URI> getPrincipal() {
        return Optional.ofNullable(principal);
    }

    /**
     * Get the issuer for this credential.
     *
     * @return the issuer
     */
    public URI getIssuer() {
        return issuer;
    }

    /**
     * Get the token for this credential.
     *
     * @return the raw token
     */
    public String getToken() {
        return token;
    }

    /**
     * Get the expiration time for this credential.
     *
     * @return the expiration time
     */
    public Instant getExpiration() {
        return expiration;
    }

    /**
     * Get the thumbprint for an associated proof, if present.
     *
     * @return the proof thumbprint
     */
    public Optional<String> getProofThumbprint() {
        return Optional.ofNullable(jkt);
    }
}

