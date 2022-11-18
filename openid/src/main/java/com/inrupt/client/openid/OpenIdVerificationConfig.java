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
package com.inrupt.client.openid;

import java.net.URI;

/**
 * A class for configuring the verification rules for OpenID Connect ID Tokens.
 */
public class OpenIdVerificationConfig {

    private int graceSecs = 60; /* default: one minute */
    private URI publicKeyLocation; /* default: null */
    private String audience; /* default: null */

    /**
     * Get the expiration grace period for the token in seconds.
     *
     * @return the expiration grace period, default is 60.
     */
    public int getExpGracePeriodSecs() {
        return graceSecs;
    }

    /**
     * Set an expiration grace period for the token in seconds.
     *
     * @param graceSecs the expiration grace period
     */
    public void setExpGracePeriodSecs(final int graceSecs) {
        this.graceSecs = graceSecs;
    }

    /**
     * Get the expected audience.
     *
     * @return the expected audience, default is {@code null}
     */
    public String getExpectedAudience() {
        return audience;
    }

    /**
     * Set the expected audience.
     *
     * @param audience the expected audience
     */
    public void setExpectedAudience(final String audience) {
        this.audience = audience;
    }

    /**
     * Get the public signing key location.
     *
     * <p>If the public signing key is {@code null}, the ID Token signature is not verified
     *
     * @return the public key location, default is {@code null}
     */
    public URI getPublicKeyLocation() {
        return publicKeyLocation;
    }

    /**
     * Set the public signing key location.
     *
     * <p>If the public signing key location is {@code null}, the ID Token signature is not verified
     *
     * @param publicKeyLocation the public signing key location
     */
    public void setPublicKeyLocation(final URI publicKeyLocation) {
        this.publicKeyLocation = publicKeyLocation;
    }
}
