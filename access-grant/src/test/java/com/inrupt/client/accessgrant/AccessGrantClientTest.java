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
import static org.jose4j.jwx.HeaderParameterNames.TYPE;
import static org.junit.jupiter.api.Assertions.*;
import com.inrupt.client.accessgrant.accessGrant.Access;
import com.inrupt.client.accessgrant.accessGrant.AccessGrantClient;
import com.inrupt.client.accessgrant.accessGrant.AccessRequest;
import com.inrupt.client.auth.Session;
import com.inrupt.client.openid.OpenIdSession;
import com.inrupt.client.util.URIBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletionException;

import org.apache.commons.io.IOUtils;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;
import org.jose4j.lang.UncheckedJoseException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class AccessGrantClientTest {

    private static final String WEBID = "https://id.test/username";
    private static final String SUB = "username";
    private static final String ISS = "https://issuer.test";
    private static final String AZP = "https://app.test";
    private static final URI ACCESS_GRANT = URI.create("http://www.w3.org/ns/solid/vc#SolidAccessGrant");
    private static final URI ACCESS_REQUEST = URI.create("http://www.w3.org/ns/solid/vc#SolidAccessRequest");

    private static final MockAccessGrantServer mockServer = new MockAccessGrantServer();
    private static AccessGrantClient agClient;
    private static URI baseUri;

    @BeforeAll
    static void setup() {
        baseUri = URI.create(mockServer.start());
        agClient = new AccessGrantClient(baseUri);
    }

    @AfterAll
    static void teardown() {
        mockServer.stop();
    }

    @Test
    void testSession() {
        final URI issuer = URI.create("https://access.example");
        final AccessGrantClient client = new AccessGrantClient(issuer);
        assertNotEquals(client, client.session(Session.anonymous()));
    }

    @Test
    void testIssueGrant() {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", WEBID);
        claims.put("sub", SUB);
        claims.put("iss", ISS);
        claims.put("azp", AZP);
        final String token = generateIdToken(claims);
        final AccessGrantClient client = agClient.session(OpenIdSession.ofIdToken(token));

        final URI agent = URI.create("https://id.test/agent");
        final Instant expiration = Instant.parse("2022-08-27T12:00:00Z");
        final Set<String> modes = new HashSet<>(Arrays.asList("Read", "Append"));
        final Set<String> purposes = Collections.singleton("https://purpose.test/Purpose1");

        final Set<URI> resources = Collections.singleton(URI.create("https://storage.test/data/"));
        
        final Access access = Access.newBuilder()
            .grantor(grantor)
            .grantee(grantee)
            .types(types)
            .identifier(identifier)
            .issuer(issuer)
            .purposes(purposes)
            .modes(modes)
            .resources(resources)
            .status(status)
            .expiration(expiration)
            .inherit(inherit)
            .build();

        final AccessRequest grant = client.requestAccess(access)
            .toCompletableFuture().join();

        final AccessGrant grant = client.issue(ACCESS_GRANT, agent, resources, modes, purposes, expiration)
            .toCompletableFuture().join();

        assertTrue(grant.getTypes().contains("SolidAccessGrant"));
        assertEquals(Optional.of(agent), grant.getGrantee());
        assertEquals(modes, grant.getModes());
        assertEquals(expiration, grant.getExpiration());
        assertEquals(baseUri, grant.getIssuer());
        assertEquals(purposes, grant.getPurpose());
        assertEquals(resources, grant.getResources());
    }

    @Test
    void testIssueRequest() {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", WEBID);
        claims.put("sub", SUB);
        claims.put("iss", ISS);
        claims.put("azp", AZP);
        final String token = generateIdToken(claims);
        final AccessGrantClient client = agClient.session(OpenIdSession.ofIdToken(token));

        final URI agent = URI.create("https://id.test/agent");
        final Instant expiration = Instant.parse("2022-08-27T12:00:00Z");
        final Set<String> modes = new HashSet<>(Arrays.asList("Read", "Append"));
        final Set<String> purposes = Collections.singleton("https://purpose.test/Purpose1");

        final Set<URI> resources = Collections.singleton(URI.create("https://storage.test/data/"));
        final AccessGrant request = client.issue(ACCESS_REQUEST, agent, resources, modes, purposes, expiration)
            .toCompletableFuture().join();

        assertTrue(request.getTypes().contains("SolidAccessRequest"));
        assertEquals(Optional.of(agent), request.getGrantee());
        assertEquals(modes, request.getModes());
        assertEquals(expiration, request.getExpiration());
        assertEquals(baseUri, request.getIssuer());
        assertEquals(purposes, request.getPurpose());
        assertEquals(resources, request.getResources());
    }

    @Test
    void testIssueNoAuth() {
        final URI agent = URI.create("https://id.test/agent");
        final Instant expiration = Instant.parse("2022-08-27T12:00:00Z");
        final Set<String> modes = new HashSet<>(Arrays.asList("Read", "Append"));
        final Set<String> purposes = Collections.singleton("https://purpose.test/Purpose1");

        final Set<URI> resources = Collections.singleton(URI.create("https://storage.test/data/"));
        final CompletionException err = assertThrows(CompletionException.class, () ->
                agClient.issue(ACCESS_GRANT, agent, resources, modes, purposes, expiration)
                    .toCompletableFuture().join());
        assertTrue(err.getCause() instanceof AccessGrantException);
    }

    @Test
    void testIssueOther() {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", WEBID);
        claims.put("sub", SUB);
        claims.put("iss", ISS);
        claims.put("azp", AZP);
        final String token = generateIdToken(claims);
        final AccessGrantClient client = agClient.session(OpenIdSession.ofIdToken(token));

        final URI agent = URI.create("https://id.test/agent");
        final Instant expiration = Instant.parse("2022-08-27T12:00:00Z");
        final Set<String> modes = new HashSet<>(Arrays.asList("Read", "Append"));
        final Set<String> purposes = Collections.singleton("https://purpose.test/Purpose1");

        final Set<URI> resources = Collections.singleton(URI.create("https://storage.test/data/"));
        final CompletionException err = assertThrows(CompletionException.class, () ->
                client.issue(URI.create("https://vc.test/Type"), agent, resources, modes, purposes, expiration)
                    .toCompletableFuture().join());
        assertTrue(err.getCause() instanceof AccessGrantException);
    }

    @Disabled
    @Test
    void testQueryGrant() {
        /* final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", WEBID);
        claims.put("sub", SUB);
        claims.put("iss", ISS);
        claims.put("azp", AZP);
        final String token = generateIdToken(claims);
        final AccessGrantClient client = agClient.session(OpenIdSession.ofIdToken(token));

        final List<AccessGrant> grants = client.query(URI.create("SolidAccessGrant"), null,
                URI.create("https://storage.example/e973cc3d-5c28-4a10-98c5-e40079289358/a/b/c"), "Read")
                    .toCompletableFuture().join();
        assertEquals(1, grants.size()); */
    }

    @Test
    void testQueryRequest() {
        /*
        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", WEBID);
        claims.put("sub", SUB);
        claims.put("iss", ISS);
        claims.put("azp", AZP);
        final String token = generateIdToken(claims);
        final AccessGrantClient client = agClient.session(OpenIdSession.ofIdToken(token));

        final List<AccessGrant> grants = client.query(URI.create("SolidAccessRequest"), null,
                URI.create("https://storage.example/f1759e6d-4dda-4401-be61-d90d070a5474/a/b/c"), "Read")
                    .toCompletableFuture().join();
        assertEquals(1, grants.size());
        */
    }

    @Test
    void testQueryInvalidAuth() {
        /* final CompletionException err = assertThrows(CompletionException.class,
                agClient.query(URI.create("SolidAccessGrant"), null, null, null).toCompletableFuture()::join);

        assertTrue(err.getCause() instanceof AccessGrantException);
        */
    }

    static String generateIdToken(final Map<String, Object> claims) {
        try (final InputStream resource = AccessGrantClientTest.class.getResourceAsStream("/signing-key.json")) {
            final String jwks = IOUtils.toString(resource, UTF_8);
            final PublicJsonWebKey jwk = PublicJsonWebKey.Factory
                .newPublicJwk(jwks);

            final JsonWebSignature jws = new JsonWebSignature();
            jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256);
            jws.setHeader(TYPE, "JWT");
            jws.setKey(jwk.getPrivateKey());
            final JwtClaims jwtClaims = new JwtClaims();
            jwtClaims.setJwtId(UUID.randomUUID().toString());
            jwtClaims.setExpirationTimeMinutesInTheFuture(5);
            jwtClaims.setIssuedAtToNow();
            // override/set claims
            claims.entrySet().forEach(entry -> jwtClaims.setClaim(entry.getKey(), entry.getValue()));
            jws.setPayload(jwtClaims.toJson());

            return jws.getCompactSerialization();
        } catch (final IOException ex) {
            throw new UncheckedIOException("Unable to read JWK", ex);
        } catch (final JoseException ex) {
            throw new UncheckedJoseException("Unable to generate DPoP token", ex);
        }
    }
}
