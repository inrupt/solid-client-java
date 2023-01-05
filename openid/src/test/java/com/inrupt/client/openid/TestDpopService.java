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
package com.inrupt.client.openid;

import static org.jose4j.jwx.HeaderParameterNames.TYPE;
import static org.jose4j.lang.HashUtil.SHA_256;

import com.inrupt.client.auth.DPoP;
import com.inrupt.client.spi.DpopService;

import java.net.URI;
import java.security.KeyPair;
import java.security.spec.ECParameterSpec;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
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
public class TestDpopService implements DpopService {

    @Override
    public DPoP ofKeyPairs(final Map<String, KeyPair> keypairs) {
        return new DPoPManager(keypairs);
    }

    public class DPoPManager implements DPoP {
        private final Map<String, KeyPair> keypairs = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        private final Map<String, String> thumbprints = new HashMap<>();

        public DPoPManager(final Map<String, KeyPair> keypairs) {
            super();
            try {
                this.keypairs.putAll(keypairs);
                if (this.keypairs.isEmpty()) {
                    this.keypairs.put("ES256", defaultKeyPair(EllipticCurves.P256));
                }

                // Populate the thumbprints
                for (final Map.Entry<String, KeyPair> item : this.keypairs.entrySet()) {
                    final PublicJsonWebKey jwk = PublicJsonWebKey.Factory.newPublicJwk(item.getValue().getPublic());
                    final String jkt = jwk.calculateBase64urlEncodedThumbprint(SHA_256);
                    this.thumbprints.put(jkt, item.getKey());
                }
            } catch (final JoseException ex) {
                throw new OpenIdException("Unable to process provided keypair", ex);
            }
        }

        @Override
        public Optional<String> lookupAlgorithm(final String jkt) {
            return Optional.ofNullable(thumbprints.get(jkt));
        }

        @Override
        public Optional<String> lookupThumbprint(final String algorithm) {
            if (algorithm != null) {
                return thumbprints.entrySet().stream().filter(e -> algorithm.equals(e.getValue()))
                    .map(Map.Entry::getKey).findFirst();
            }
            return Optional.empty();
        }

        @Override
        public String generateProof(final String algorithm, final URI uri, final String method) {
            final KeyPair keypair = keypairs.get(Objects.requireNonNull(algorithm));
            if (keypair == null) {
                throw new OpenIdException("Unsupported DPoP algorithm: " + algorithm);
            }

            final String htm = Objects.requireNonNull(method);
            final URI htu = Objects.requireNonNull(uri);

            try {
                final PublicJsonWebKey jwk = PublicJsonWebKey.Factory.newPublicJwk(keypair.getPublic());
                final JsonWebSignature jws = new JsonWebSignature();
                jws.setAlgorithmHeaderValue(algorithm);
                jws.setHeader(TYPE, "dpop+jwt");
                jws.setJwkHeader(jwk);
                jws.setKey(keypair.getPrivate());

                final JwtClaims claims = new JwtClaims();
                claims.setJwtId(UUID.randomUUID().toString());
                claims.setStringClaim("htm", htm);
                claims.setStringClaim("htu", htu.toString());
                claims.setIssuedAtToNow();
                jws.setPayload(claims.toJson());

                return jws.getCompactSerialization();
            } catch (final JoseException ex) {
                throw new OpenIdException("Unable to generate DPoP proof", ex);
            }
        }

        @Override
        public Set<String> algorithms() {
            return keypairs.keySet();
        }
    }

    static KeyPair defaultKeyPair(final ECParameterSpec spec) {
        try {
            final EcKeyUtil keyUtil = new EcKeyUtil();
            return keyUtil.generateKeyPair(spec);
        } catch (final JoseException ex) {
            throw new OpenIdException("Unable to generate default keypair", ex);
        }
    }
}

