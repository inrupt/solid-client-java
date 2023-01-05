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

    public Credential(final String scheme, final URI issuer, final String token, final Instant expiration,
            final URI principal, final String jkt) {
        this.scheme = scheme;
        this.issuer = issuer;
        this.token = token;
        this.expiration = expiration;
        this.principal = principal;
        this.jkt = jkt;
    }

    public String getScheme() {
        return scheme;
    }

    public Optional<URI> getPrincipal() {
        return Optional.ofNullable(principal);
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

    public Optional<String> getProofThumbprint() {
        return Optional.ofNullable(jkt);
    }
}

