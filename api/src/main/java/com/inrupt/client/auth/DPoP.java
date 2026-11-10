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
package com.inrupt.client.auth;

import com.inrupt.client.spi.ServiceProvider;

import java.net.URI;
import java.security.KeyPair;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * An abstraction for working with OAuth 2.0 Demonstrating Proof-of-Possession at the Application Layer (DPoP).
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc9449">RFC 9449: OAuth 2.0 Demonstrating Proof of Possession
 * (DPoP)</a>
 */
public interface DPoP {

    /**
     * Generate a DPoP proof for a given URI and method pair.
     *
     * @param algorithm the algorithm to use
     * @param uri the HTTP URI
     * @param method the HTTP method
     * @return the DPoP Proof, serialized as a Base64-encoded string, suitable for use with HTTP headers
     * @deprecated The Access Token should be included in the proof.
     */
    String generateProof(String algorithm, URI uri, String method);

    /**
     * Generate a DPoP proof for a given URI and method pair, bound to an access token.
     *
     * <p>When presenting a DPoP-bound access token to a protected resource, the proof must include
     * an {@code ath} claim containing the hash of that access token
     * (<a href="https://www.rfc-editor.org/rfc/rfc9449#section-4.2">RFC 9449, section 4.2</a>).
     *
     * @implSpec The default implementation ignores the access token and delegates to
     *     {@link #generateProof(String, URI, String)}. Implementations should override this method
     *     in order to include the {@code ath} claim.
     * @param algorithm the algorithm to use
     * @param uri the HTTP URI
     * @param method the HTTP method
     * @param accessToken the access token sent alongside the proof, may be {@code null}
     * @return the DPoP Proof, serialized as a Base64-encoded string, suitable for use with HTTP headers
     */
    default String generateProof(final String algorithm, final URI uri, final String method,
            final String accessToken) {
        return generateProof(algorithm, uri, method);
    }

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

