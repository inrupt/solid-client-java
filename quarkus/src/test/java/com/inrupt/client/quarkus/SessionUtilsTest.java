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
package com.inrupt.client.quarkus;

import static org.junit.jupiter.api.Assertions.*;

import com.inrupt.client.auth.Session;
import com.inrupt.client.openid.OpenIdSession;

import io.smallrye.jwt.algorithm.SignatureAlgorithm;
import io.smallrye.jwt.auth.principal.DefaultJWTParser;
import io.smallrye.jwt.auth.principal.JWTAuthContextInfo;
import io.smallrye.jwt.auth.principal.ParseException;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.util.ResourceUtils;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.Optional;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.lang.JoseException;
import org.jose4j.lang.UncheckedJoseException;
import org.junit.jupiter.api.Test;

class SessionUtilsTest {

    static final String WEBID = "https://example.com/user";
    static final String ISSUER = "https://issuer.example";

    @Test
    void testSession() {
        final var token = generateIdToken("user", ISSUER, WEBID, Instant.now().plusSeconds(30));
        final var session = SessionUtils.asSession(token, OpenIdSession::ofIdToken);
        assertTrue(session.isPresent());
        session.ifPresent(s ->
            assertEquals(Optional.of(URI.create(WEBID)), s.getPrincipal()));
    }

    @Test
    void testExpiredSession() {
        final var token = generateIdToken("user", ISSUER, WEBID, Instant.now().minusSeconds(30));
        final var session = SessionUtils.asSession(token, OpenIdSession::ofIdToken);
        assertFalse(session.isPresent());
    }

    @Test
    void testAnonymousSession() {
        final var token = generateIdToken("user", ISSUER, WEBID, Instant.now().plusSeconds(30));
        final var session = SessionUtils.asSession(token, t -> Session.anonymous());
        assertTrue(session.isPresent());
        session.ifPresent(s ->
            assertEquals(Optional.empty(), s.getPrincipal()));
    }

    @Test
    void testSessionNoMapper() {
        final var token = generateIdToken("user", ISSUER, WEBID, Instant.now().plusSeconds(30));
        final var session = SessionUtils.asSession(token);
        assertTrue(session.isPresent());
        session.ifPresent(s ->
            assertEquals(Optional.of(URI.create(WEBID)), s.getPrincipal()));
    }

    static JsonWebToken generateIdToken(final String sub, final String issuer, final String webid, final Instant exp) {
        try {
            final var jwk = PublicJsonWebKey.Factory
                   .newPublicJwk(ResourceUtils.readResource("testKey.json"));

            final var authContext = new JWTAuthContextInfo(jwk.getPublicKey(), issuer);
            authContext.setSignatureAlgorithm(SignatureAlgorithm.ES256);
            final var parser = new DefaultJWTParser(authContext);

            final var issued = Instant.now().isBefore(exp) ? Instant.now() : exp.minusSeconds(30);
            final var token = Jwt.claims()
                    .subject(sub)
                    .issuer(issuer)
                    .claim("webid", webid)
                    .issuedAt(issued)
                    .expiresAt(exp)
                    .audience("solid").jws()
                    .keyId("76GJ30ywXmKAxKdhJ1yPLA").sign(jwk.getPrivateKey());
            return parser.parse(token);
        } catch (final IOException | JoseException ex) {
            throw new UncheckedJoseException("Could not build token", ex);
        } catch (final ParseException ex) {
            throw new IllegalArgumentException("Could not parse JWT", ex);
        }
    }
}

