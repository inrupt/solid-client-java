/*
 * Copyright Inrupt Inc.
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
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class for configuring an Open ID session.
 *
 * <p>This includes verification rules for OpenID Connect ID Tokens.
 */
public class OpenIdConfig {

    private int graceSecs = 60; /* default: one minute */
    private URI publicKeyLocation; /* default: null */
    private String audience; /* default: null */
    private Map<String, KeyPair> keypairs = new HashMap<>();
    private List<String> scopes = new ArrayList<>(Arrays.asList("openid", "webid"));

    /**
     * Get the expiration grace period for an ID token in seconds.
     *
     * @return the expiration grace period, default is 60.
     */
    public int getExpGracePeriodSecs() {
        return graceSecs;
    }

    /**
     * Set an expiration grace period for an ID token in seconds.
     *
     * @param graceSecs the expiration grace period
     */
    public void setExpGracePeriodSecs(final int graceSecs) {
        this.graceSecs = graceSecs;
    }

    /**
     * Get the expected audience of an ID token.
     *
     * @return the expected audience, default is {@code null}
     */
    public String getExpectedAudience() {
        return audience;
    }

    /**
     * Set the expected audience of an ID token.
     *
     * @param audience the expected audience
     */
    public void setExpectedAudience(final String audience) {
        this.audience = audience;
    }

    /**
     * Get the public signing key location of an ID token.
     *
     * <p>If the public signing key is {@code null}, the ID Token signature is not verified
     *
     * @return the public key location, default is {@code null}
     */
    public URI getPublicKeyLocation() {
        return publicKeyLocation;
    }

    /**
     * Set the public signing key location of an ID token.
     *
     * <p>If the public signing key location is {@code null}, the ID Token signature is not verified
     *
     * @param publicKeyLocation the public signing key location
     */
    public void setPublicKeyLocation(final URI publicKeyLocation) {
        this.publicKeyLocation = publicKeyLocation;
    }

    /**
     * Set any externally-defined Proofing (DPoP) keypairs.
     *
     * <p>Note: this will remove any previously set keypairs
     *
     * @param keypairs the keypair
     */
    public void setProofKeyPairs(final Map<String, KeyPair> keypairs) {
        this.keypairs.clear();
        this.keypairs.putAll(keypairs);
    }

    /**
     * Add a Proofing (DPoP) keypair.
     *
     * @param algorithm the algorithm
     * @param keypair the keypair
     */
    public void addProofKeyPair(final String algorithm, final KeyPair keypair) {
        this.keypairs.put(algorithm, keypair);
    }

    /**
     * Get any externally-defined Proofing (DPoP) keypairs.
     *
     * @return the keypairs
     */
    public Map<String, KeyPair> getProofKeyPairs() {
        return keypairs;
    }

    /**
     * Set any OAuth 2.0 scope values.
     *
     * <p>Note: by default, the scopes are "webid" and "openid". Setting new values will clear
     * any existing values.
     *
     * @param scopes the scope values
     */
    public void setScopes(final String... scopes) {
        this.scopes.clear();
        Collections.addAll(this.scopes, scopes);
    }

    /**
     * Get any OAuth 2.0 scope values.
     *
     * @return the scope values
     */
    public List<String> getScopes() {
        return scopes;
    }
}
