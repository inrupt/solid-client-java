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

import java.net.URI;
import java.security.KeyPair;

/**
 * An implementation of OAuth 2.0 Demonstrating Proof-of-Possession at the Application Layer (DPoP).
 *
 * @see <a href="https://datatracker.ietf.org/doc/html/draft-ietf-oauth-dpop">DPoP draft specification</a>
 */
public class DPoP {

    private final KeyPair keypair;

    /**
     * Create a DPoP instance with a default keypair.
     */
    public DPoP() {
        // TODO implement
        this(null);
    }

    /**
     * Create a DPoP instance with a user-supplied keypair.
     *
     * @param keypair a keypair
     */
    public DPoP(final KeyPair keypair) {
        this.keypair = keypair;
    }

    /**
     * Generate a DPoP proof for a given URI and method pair.
     *
     * @param htu the HTTP URI
     * @param htm the HTTP method
     * @return the DPoP Proof, serialized as a Base64-encoded string, suitable for use with HTTP headers
     */
    public String generateProof(final URI htu, final String htm) {
        // TODO implement
        return "PROOF-placeholder";
    }
}
