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
import java.security.spec.ECParameterSpec;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.keys.EcKeyUtil;
import org.jose4j.keys.EllipticCurves;
import org.jose4j.lang.JoseException;

/**
 * An implementation of OAuth 2.0 Demonstrating Proof-of-Possession at the Application Layer (DPoP).
 *
 * @see <a href="https://datatracker.ietf.org/doc/html/draft-ietf-oauth-dpop">DPoP draft specification</a>
 */
public class DPoP {

    private final Map<String, KeyPair> keypairs;

    /**
     * Create a DPoP instance with a default ES256 keypair.
     */
    public DPoP() {
        this("ES256", defaultKeyPair(EllipticCurves.P256));
    }

    /**
     * Create a DPoP instance with a user-supplied keypair.
     *
     * @param algorithm an algorithm name, such as ES256 or RS256
     * @param keypair a keypair
     */
    public DPoP(final String algorithm, final KeyPair keypair) {
        this(Map.of(Objects.requireNonNull(algorithm), Objects.requireNonNull(keypair)));
    }

    /**
     * Create a DPoP instance with multiple user-supplied keypairs.
     *
     * @param keypairs the algorithm-keypair mapping
     */
    public DPoP(final Map<String, KeyPair> keypairs) {
        this.keypairs = Objects.requireNonNull(keypairs);
    }

    /**
     * Return a collection of the supported algorithm names.
     *
     * @return the supported algorithm names
     */
    public Set<String> algorithms() {
        return keypairs.keySet();
    }

    /**
     * Generate a DPoP proof for a given URI and method pair, using the default algorithm (EC256).
     *
     * @param uri the HTTP URI
     * @param method the HTTP method
     * @return the DPoP Proof, serialized as a Base64-encoded string, suitable for use with HTTP headers
     */
    public String generateProof(final URI uri, final String method) {
        return generateProof("EC256", uri, method);
    }

    /**
     * Generate a DPoP proof for a given URI and method pair.
     *
     * @param algorithm the algorithm to use
     * @param uri the HTTP URI
     * @param method the HTTP method
     * @return the DPoP Proof, serialized as a Base64-encoded string, suitable for use with HTTP headers
     */
    public String generateProof(final String algorithm, final URI uri, final String method) {
        final var keypair = keypairs.get(Objects.requireNonNull(algorithm));
        if (keypair == null) {
            throw new AuthenticationException("Unsupported DPoP algorithm: " + algorithm);
        }

        final var htm = Objects.requireNonNull(method);
        final var htu = Objects.requireNonNull(uri);

        try {
            final var jwk = PublicJsonWebKey.Factory.newPublicJwk(keypair.getPublic());
            final var jws = new JsonWebSignature();
            jws.setAlgorithmHeaderValue(algorithm);
            jws.setHeader("type", "dpop+jwt");
            jws.setJwkHeader(jwk);
            jws.setKey(keypair.getPrivate());

            final var claims = new JwtClaims();
            claims.setJwtId(UUID.randomUUID().toString());
            claims.setStringClaim("htm", htm);
            claims.setStringClaim("htu", htu.toString());
            claims.setIssuedAtToNow();
            jws.setPayload(claims.toJson());

            return jws.getCompactSerialization();
        } catch (final JoseException ex) {
            throw new AuthenticationException("Unable to generate DPoP proof", ex);
        }
    }

    static KeyPair defaultKeyPair(final ECParameterSpec spec) {
        try {
            final var keyUtil = new EcKeyUtil();
            return keyUtil.generateKeyPair(spec);
        } catch (final JoseException ex) {
            throw new AuthenticationException("Unable to generate default keypair", ex);
        }

    }
}
