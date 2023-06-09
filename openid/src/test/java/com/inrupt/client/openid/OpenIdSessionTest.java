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

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.jose4j.lang.HashUtil.SHA_256;
import static org.junit.jupiter.api.Assertions.*;

import com.inrupt.client.Request;
import com.inrupt.client.auth.Authenticator;
import com.inrupt.client.auth.Challenge;
import com.inrupt.client.auth.Credential;
import com.inrupt.client.auth.DPoP;
import com.inrupt.client.auth.Session;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.security.KeyPair;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.lang.JoseException;
import org.jose4j.lang.UncheckedJoseException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class OpenIdSessionTest {

    private static final OpenIdMockHttpService mockHttpService = new OpenIdMockHttpService();
    private static final String WEBID = "https://id.example/username";
    private static final String SUB = "username";
    private static final String ISS = "https://iss.example";
    private static final String AZP = "https://app.example";
    private static String baseUrl;

    @BeforeAll
    static void setup() {
        baseUrl = mockHttpService.start();
    }

    @AfterAll
    static void teardown() {
        mockHttpService.stop();
    }

    @Test
    void testSessionTokenNoValidation() {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", WEBID);
        claims.put("sub", SUB);
        claims.put("iss", ISS);
        claims.put("azp", AZP);

        final String token = OpenIdTestUtils.generateIdToken(claims);
        assertDoesNotThrow(() -> OpenIdSession.ofIdToken(token));
    }

    @Test
    void testSessionTokenSignatureValidation() {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", WEBID);
        claims.put("sub", SUB);
        claims.put("iss", ISS);
        claims.put("azp", AZP);
        claims.put("aud", Arrays.asList("solid", AZP));

        final String token = OpenIdTestUtils.generateIdToken(claims);
        final OpenIdConfig config = new OpenIdConfig();
        config.setPublicKeyLocation(URI.create(baseUrl + "/jwks"));
        assertDoesNotThrow(() -> OpenIdSession.ofIdToken(token, config));
    }

    @Test
    void testSessionIncorrectTokenSignatureValidation() {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", WEBID);
        claims.put("sub", SUB);
        claims.put("iss", ISS);
        claims.put("azp", AZP);
        claims.put("aud", Arrays.asList("solid", AZP));

        final String token = OpenIdTestUtils.generateIdToken(claims);
        final OpenIdConfig config = new OpenIdConfig();
        config.setPublicKeyLocation(URI.create(baseUrl + "/jwks-other"));
        assertThrows(OpenIdException.class, () -> OpenIdSession.ofIdToken(token, config));
    }

    @Test
    void testSessionTokenCorrectAudience() {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", WEBID);
        claims.put("sub", SUB);
        claims.put("iss", ISS);
        claims.put("azp", AZP);
        claims.put("aud", Arrays.asList("solid", AZP));

        final String token = OpenIdTestUtils.generateIdToken(claims);
        final OpenIdConfig config = new OpenIdConfig();
        config.setExpectedAudience("https://app.example");
        assertDoesNotThrow(() -> OpenIdSession.ofIdToken(token, config));
    }

    @Test
    void testSessionTokenWrongAudience() {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", WEBID);
        claims.put("sub", SUB);
        claims.put("iss", ISS);
        claims.put("azp", AZP);
        claims.put("aud", Arrays.asList("solid", AZP));

        final String token = OpenIdTestUtils.generateIdToken(claims);
        final OpenIdConfig config = new OpenIdConfig();
        config.setExpectedAudience("https://app10.example");
        assertThrows(OpenIdException.class, () -> OpenIdSession.ofIdToken(token, config));
    }

    @Test
    void testClientCredentials() {
        final URI issuer = URI.create(baseUrl);
        final String clientId = "app1";
        final String clientSecret = "secret";
        final String authMethod = "client_secret_basic";
        final Session session = OpenIdSession.ofClientCredentials(issuer, clientId, clientSecret, authMethod);
        assertFalse(session.fromCache(null).isPresent());
        final Optional<URI> principal = session.getPrincipal();
        assertEquals(Optional.of(URI.create(WEBID)), principal);
        assertFalse(session.fromCache(null).isPresent());
        final Authenticator auth = new OpenIdAuthenticationProvider().getAuthenticator(Challenge.of("Bearer"));
        final Request req = Request.newBuilder(URI.create("https://storage.example/")).build();
        final Optional<Credential> credential = session.authenticate(auth, req, Collections.emptySet())
            .toCompletableFuture().join();
        assertEquals(Optional.of(URI.create(WEBID)), credential.flatMap(Credential::getPrincipal));
        assertTrue(session.fromCache(req).isPresent());

        final List<String> hashUris = Arrays.asList("https://storage.example/#hash1",
                "https://storage.example/#hash2",
                "https://storage.example/#hash3");
        for (final String uri : hashUris) {
            final Request r = Request.newBuilder(URI.create(uri)).build();
            assertTrue(session.fromCache(r).isPresent());
        }
        final List<String> queryUris = Arrays.asList("https://storage.example/?q=1",
                "https://storage.example/?a=b",
                "https://storage.example/?foo=bar&q=1");
        for (final String uri : queryUris) {
            final Request r = Request.newBuilder(URI.create(uri)).build();
            assertFalse(session.fromCache(r).isPresent());
        }

        session.reset();

        assertFalse(session.fromCache(req).isPresent());
        for (final String uri : hashUris) {
            final Request r = Request.newBuilder(URI.create(uri)).build();
            assertFalse(session.fromCache(r).isPresent());
        }
        for (final String uri : queryUris) {
            final Request r = Request.newBuilder(URI.create(uri)).build();
            assertFalse(session.fromCache(r).isPresent());
        }
    }

    @Test
    void testClientCredentialsWithConfig() {
        final URI issuer = URI.create(baseUrl);
        final DPoP dpop = DPoP.of();
        final OpenIdProvider provider = new OpenIdProvider(issuer, dpop);
        final OpenIdConfig config = new OpenIdConfig();
        final String clientId = "app1";
        final String clientSecret = "secret";
        final String authMethod = "client_secret_basic";
        final Session session = OpenIdSession.ofClientCredentials(provider, clientId, clientSecret, authMethod, config);
        assertFalse(session.fromCache(null).isPresent());
        final Optional<URI> principal = session.getPrincipal();
        assertEquals(Optional.of(URI.create(WEBID)), principal);
        assertFalse(session.fromCache(null).isPresent());
        final Optional<Credential> credential = session.authenticate(null, Collections.emptySet())
            .toCompletableFuture().join();
        assertEquals(Optional.of(URI.create(WEBID)), credential.flatMap(Credential::getPrincipal));
    }

    @Test
    void testSessionExpiry() {
        assertEquals(Instant.MAX, OpenIdSession.toInstant(0));
        assertTrue(OpenIdSession.toInstant(1).isBefore(Instant.MAX));
    }

    @Test
    void testSessionIdentifier() {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", WEBID);
        claims.put("sub", SUB);
        claims.put("iss", ISS);
        claims.put("azp", AZP);

        final String token = OpenIdTestUtils.generateIdToken(claims);
        final Session session = OpenIdSession.ofIdToken(token);

        final String id = DigestUtils.sha256Hex(WEBID);
        assertEquals(id, session.getId());
    }

    @Test
    void testSessionMissingIssuer() {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("sub", SUB);

        final String token = OpenIdTestUtils.generateIdToken(claims);
        assertThrows(OpenIdException.class, () -> OpenIdSession.ofIdToken(token));
    }

    @Test
    void testSessionMissingSubject() {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("iss", ISS);

        final String token = OpenIdTestUtils.generateIdToken(claims);
        assertThrows(OpenIdException.class, () -> OpenIdSession.ofIdToken(token));
    }

    @Test
    void testSessionIdentifierNoWebId() {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("sub", SUB);
        claims.put("iss", ISS);
        claims.put("azp", AZP);

        final String token = OpenIdTestUtils.generateIdToken(claims);
        final Session session = OpenIdSession.ofIdToken(token);

        final String id = DigestUtils.sha256Hex(ISS + "|" + SUB);
        assertEquals(id, session.getId());
    }

    @Test
    void testSessionDisallowClockSkew() {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", WEBID);
        claims.put("sub", SUB);
        claims.put("iss", ISS);
        claims.put("azp", AZP);

        final String token = OpenIdTestUtils.generateIdToken(claims);
        final OpenIdConfig config = new OpenIdConfig();
        config.setExpGracePeriodSecs(0);
        assertDoesNotThrow(() -> OpenIdSession.ofIdToken(token, config));
    }

    @Test
    void testSessionFromCache() {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", WEBID);
        claims.put("sub", SUB);
        claims.put("iss", ISS);
        claims.put("azp", AZP);

        final String token = OpenIdTestUtils.generateIdToken(claims);
        final OpenIdConfig config = new OpenIdConfig();
        config.setExpGracePeriodSecs(0);
        final Session session = OpenIdSession.ofIdToken(token, config);
        final Request req = Request.newBuilder(URI.create("https://storage.example")).build();
        assertFalse(session.fromCache(req).isPresent());
    }

    @Test
    void testExpiredToken() {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", WEBID);
        claims.put("sub", SUB);
        claims.put("iss", ISS);
        claims.put("azp", AZP);
        claims.put("exp", Instant.now().minusSeconds(1).getEpochSecond());
        claims.put("iat", Instant.now().minusSeconds(61).getEpochSecond());

        final String token = OpenIdTestUtils.generateIdToken(claims);
        final OpenIdConfig config = new OpenIdConfig();
        config.setExpGracePeriodSecs(0);
        assertThrows(OpenIdException.class, () -> OpenIdSession.ofIdToken(token, config));
    }

    @Test
    void testThumbprint() {
        final PublicJsonWebKey ecJwk = getDpopKey("/ec-key.json");
        final PublicJsonWebKey rsaJwk = getDpopKey("/rsa-key.json");
        final OpenIdConfig config = new OpenIdConfig();
        final Map<String, KeyPair> keypairs = new HashMap<>();
        keypairs.put("ES256", new KeyPair(ecJwk.getPublicKey(), ecJwk.getPrivateKey()));
        keypairs.put("RS256", new KeyPair(rsaJwk.getPublicKey(), rsaJwk.getPrivateKey()));
        config.setProofKeyPairs(keypairs);

        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", WEBID);
        claims.put("sub", SUB);
        claims.put("iss", ISS);
        claims.put("azp", AZP);
        claims.put("cnf", Collections.singletonMap("jkt", ecJwk.calculateBase64urlEncodedThumbprint(SHA_256)));

        final String token = OpenIdTestUtils.generateIdToken(claims);
        final Session session = OpenIdSession.ofIdToken(token, config);

        assertTrue(session.selectThumbprint(Arrays.asList("HS256", "RS256", "ES256")).isPresent());
        assertTrue(session.selectThumbprint(Arrays.asList("ES256")).isPresent());
        assertTrue(session.selectThumbprint(Arrays.asList("RS256")).isPresent());
        assertFalse(session.selectThumbprint(Arrays.asList("HS256")).isPresent());
    }

    @Test
    void testNearlyExpiredToken() {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", WEBID);
        claims.put("sub", SUB);
        claims.put("iss", ISS);
        claims.put("azp", AZP);
        claims.put("exp", Instant.now().plusSeconds(2).getEpochSecond());
        claims.put("iat", Instant.now().minusSeconds(61).getEpochSecond());

        final String token = OpenIdTestUtils.generateIdToken(claims);
        final OpenIdConfig config = new OpenIdConfig();
        config.setExpGracePeriodSecs(0);
        final Session s = OpenIdSession.ofIdToken(token, config);
        assertTrue(s.getCredential(OpenIdSession.ID_TOKEN, null).isPresent());
        assertFalse(s.getCredential(URI.create("unknown"), URI.create("https://example.com/")).isPresent());
        await().atMost(5, SECONDS).until(() -> !s.getCredential(OpenIdSession.ID_TOKEN, null).isPresent());
    }

    @Test
    void testExpiredTokenWithGrace() {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", WEBID);
        claims.put("sub", SUB);
        claims.put("iss", ISS);
        claims.put("azp", AZP);
        claims.put("exp", Instant.now().minusSeconds(1).getEpochSecond());
        claims.put("iat", Instant.now().minusSeconds(61).getEpochSecond());

        final String token = OpenIdTestUtils.generateIdToken(claims);
        final OpenIdConfig config = new OpenIdConfig();
        config.setExpGracePeriodSecs(60);
        assertDoesNotThrow(() -> OpenIdSession.ofIdToken(token, config));
    }

    static class TestAuthenticator implements Authenticator {
        private final String token;

        public TestAuthenticator(final String token) {
            this.token = token;
        }

        @Override
        public String getName() {
            return "TEST";
        }

        @Override
        public int getPriority() {
            return 1;
        }

        @Override
        public CompletionStage<Credential> authenticate(final Session session,
                final Request request, final Set<String> algorithms) {
            final Credential token = new Credential("Bearer", URI.create(ISS), this.token,
                    Instant.now().plusSeconds(3600), URI.create(WEBID), null);
            return CompletableFuture.completedFuture(token);
        }
    }

    static PublicJsonWebKey getDpopKey(final String resource) {
        try (final InputStream stream = OpenIdSessionTest.class.getResourceAsStream(resource)) {
            final String jwks = IOUtils.toString(stream, UTF_8);
            return PublicJsonWebKey.Factory.newPublicJwk(jwks);
        } catch (final IOException ex) {
            throw new UncheckedIOException("Unable to read JWK", ex);
        } catch (final JoseException ex) {
            throw new UncheckedJoseException("Unable to generate DPoP token", ex);
        }
    }
}
