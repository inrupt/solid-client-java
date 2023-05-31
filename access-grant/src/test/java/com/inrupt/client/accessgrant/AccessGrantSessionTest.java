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
package com.inrupt.client.accessgrant;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

import com.inrupt.client.Request;
import com.inrupt.client.auth.Authenticator;
import com.inrupt.client.auth.Challenge;
import com.inrupt.client.auth.Credential;
import com.inrupt.client.auth.Session;
import com.inrupt.client.openid.OpenIdAuthenticationProvider;
import com.inrupt.client.openid.OpenIdSession;
import com.inrupt.client.util.URIBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AccessGrantSessionTest {

    private static final String WEBID = "https://id.example/username";
    private static final String SUB = "username";
    private static final String ISS = "https://iss.example";
    private static final String AZP = "https://app.example";

    @Test
    void testEmptySessionToken() {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", WEBID);
        claims.put("sub", SUB);
        claims.put("iss", ISS);
        claims.put("azp", AZP);

        final String token = AccessGrantTestUtils.generateIdToken(claims);

        final Session session = AccessGrantSession.ofAccessGrant(OpenIdSession.ofIdToken(token));
        assertEquals(Optional.of(URI.create(WEBID)), session.getPrincipal());
        assertTrue(session.supportedSchemes().contains("Bearer"));
        assertEquals(Optional.of(token), session.getCredential(OpenIdSession.ID_TOKEN, null).map(Credential::getToken));
        assertEquals(Optional.of(URI.create(ISS)),
                session.getCredential(OpenIdSession.ID_TOKEN, null).map(Credential::getIssuer));
        assertNotNull(session.getId());
        session.selectThumbprint(Collections.singleton("ES256")).ifPresent(jkt -> {
            final Request req = Request.newBuilder(URI.create("https://storage.test/data")).build();
            assertTrue(session.generateProof(jkt, req).isPresent());
            assertFalse(session.generateProof(UUID.randomUUID().toString(), req).isPresent());
        });
    }

    @Test
    void testAccessGrantSession() throws IOException {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", WEBID);
        claims.put("sub", SUB);
        claims.put("iss", ISS);
        claims.put("azp", AZP);

        final String token = AccessGrantTestUtils.generateIdToken(claims);

        try (final InputStream resource = AccessGrantTest.class.getResourceAsStream("/access_grant1.json")) {
            final AccessGrant grant = AccessGrant.of(resource);
            final Session session = AccessGrantSession.ofAccessGrant(OpenIdSession.ofIdToken(token), grant);
            assertEquals(Optional.of(URI.create(WEBID)), session.getPrincipal());
            assertFalse(grant.getResources().isEmpty());
            for (final URI uri : grant.getResources()) {
                final String encoded = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(grant.serialize().getBytes(UTF_8));
                assertEquals(Optional.of(encoded),
                        session.getCredential(AccessGrantSession.VERIFIABLE_CREDENTIAL, uri).map(Credential::getToken));
                final URI child = URIBuilder.newBuilder(uri).path("a").path("b").build();
                assertEquals(Optional.of(encoded),
                        session.getCredential(AccessGrantSession.VERIFIABLE_CREDENTIAL,
                            child).map(Credential::getToken));
                assertFalse(session.getCredential(AccessGrantSession.VERIFIABLE_CREDENTIAL,
                            URI.create("https://random.test/a/b/d/c")).isPresent());
            }
        }
    }

    @Test
    void testProtectedResource() throws IOException {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", WEBID);
        claims.put("sub", SUB);
        claims.put("iss", ISS);
        claims.put("azp", AZP);

        final String token = AccessGrantTestUtils.generateIdToken(claims);

        try (final InputStream resource = AccessGrantTest.class.getResourceAsStream("/access_grant3.json")) {
            final AccessGrant grant = AccessGrant.of(resource);
            final Session session = AccessGrantSession.ofAccessGrant(OpenIdSession.ofIdToken(token), grant);
            final Request req = Request.newBuilder(URI.create("https://storage.example/protected-resource")).build();
            final Authenticator auth = new OpenIdAuthenticationProvider().getAuthenticator(Challenge.of("Bearer"));

            final Optional<Credential> credential = session.authenticate(auth, req, Collections.emptySet())
                .toCompletableFuture().join();

            assertTrue(credential.isPresent());
            assertTrue(session.fromCache(req).isPresent());
            session.reset();
            assertFalse(session.fromCache(req).isPresent());
        }
    }

    @ParameterizedTest
    @MethodSource
    void ancestors(final URI parent, final URI resource, final boolean expected) {
        assertEquals(expected, AccessGrantSession.isAncestor(parent, resource));
    }

    private static Stream<Arguments> ancestors() {
        return Stream.of(
                Arguments.of(URI.create("https://storage.example/"),
                    URI.create("https://storage.example/a/b/c"), true),
                Arguments.of(URI.create("http://storage.example/"),
                    URI.create("https://storage.example/a/b/c"), false),
                Arguments.of(URI.create("https://storage.example/"),
                    URI.create("https://storage.example/a/b/c"), true),
                Arguments.of(URI.create("https://storage.example/a/b"),
                    URI.create("https://storage.example/"), false),
                Arguments.of(URI.create("https://storage.example/"),
                    URI.create("https://storage.example/"), true),
                Arguments.of(URI.create("https://storage.example/a/b/c"),
                    URI.create("https://storage.example/a/b/d"), false),
                Arguments.of(URI.create("https://storage.example/a/b/c/d"),
                    URI.create("https://storage.example/a/a/c/d"), false),
                Arguments.of(URI.create("https://storage.example/a/b/c/d"),
                    URI.create("https://storage.example/a/b/c/d"), true)
            );
    }
}
