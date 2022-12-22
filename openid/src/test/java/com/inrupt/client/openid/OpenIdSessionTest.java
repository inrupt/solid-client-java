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

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

import com.inrupt.client.Authenticator;
import com.inrupt.client.Request;
import com.inrupt.client.Session;

import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.apache.commons.codec.digest.DigestUtils;
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
        final OpenIdVerificationConfig config = new OpenIdVerificationConfig();
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
        final OpenIdVerificationConfig config = new OpenIdVerificationConfig();
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
        final OpenIdVerificationConfig config = new OpenIdVerificationConfig();
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
        final OpenIdVerificationConfig config = new OpenIdVerificationConfig();
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
        assertTrue(session.fromCache(null).isPresent());
        final Optional<Session.Credential> credential = session.authenticate(null)
            .toCompletableFuture().join();
        assertEquals(Optional.of(URI.create(WEBID)), credential.flatMap(Session.Credential::getPrincipal));
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
        final OpenIdVerificationConfig config = new OpenIdVerificationConfig();
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
        final OpenIdVerificationConfig config = new OpenIdVerificationConfig();
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
        final OpenIdVerificationConfig config = new OpenIdVerificationConfig();
        config.setExpGracePeriodSecs(0);
        assertThrows(OpenIdException.class, () -> OpenIdSession.ofIdToken(token, config));
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
        final OpenIdVerificationConfig config = new OpenIdVerificationConfig();
        config.setExpGracePeriodSecs(0);
        final Session s = OpenIdSession.ofIdToken(token, config);
        assertTrue(s.getCredential(OpenIdSession.ID_TOKEN).isPresent());
        assertFalse(s.getCredential("unknown").isPresent());
        await().atMost(5, SECONDS).until(() -> !s.getCredential(OpenIdSession.ID_TOKEN).isPresent());
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
        final OpenIdVerificationConfig config = new OpenIdVerificationConfig();
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
        public Authenticator.AccessToken authenticate(final Session session, final Request request) {
            return new Authenticator.AccessToken(this.token, "Bearer", Instant.now().plusSeconds(3600),
                    URI.create(ISS), Arrays.asList("openid", "webid"), null);
        }

        @Override
        public CompletionStage<Authenticator.AccessToken> authenticateAsync(final Session session,
                final Request request) {
            return CompletableFuture.completedFuture(authenticate(session, request));
        }
    }

}
