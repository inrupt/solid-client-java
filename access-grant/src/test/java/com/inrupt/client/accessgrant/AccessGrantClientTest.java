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
package com.inrupt.client.accessgrant;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.jose4j.jwx.HeaderParameterNames.TYPE;
import static org.junit.jupiter.api.Assertions.*;

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
import java.util.concurrent.CompletableFuture;
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
    void testFetch1() {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", WEBID);
        claims.put("sub", SUB);
        claims.put("iss", ISS);
        claims.put("azp", AZP);
        final String token = generateIdToken(claims);
        final URI uri = URIBuilder.newBuilder(baseUri).path("access-grant-1").build();
        final AccessGrantClient client = agClient.session(OpenIdSession.ofIdToken(token));
        final AccessGrant grant = client.fetch(uri, AccessGrant.class).toCompletableFuture().join();

        assertEquals(uri, grant.getIdentifier());
        assertEquals(baseUri, grant.getIssuer());

        final AccessCredentialVerification response = client.verify(grant).toCompletableFuture().join();
        assertTrue(response.getChecks().contains("expirationDate"));
        assertTrue(response.getWarnings().isEmpty());
        assertTrue(response.getErrors().isEmpty());

        // Revoke
        assertDoesNotThrow(client.revoke(grant).toCompletableFuture()::join);
    }

    @Test
    void testFetchUnsupportedType() {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", WEBID);
        claims.put("sub", SUB);
        claims.put("iss", ISS);
        claims.put("azp", AZP);
        final String token = generateIdToken(claims);
        final URI uri = URIBuilder.newBuilder(baseUri).path("access-grant-1").build();
        final AccessGrantClient client = agClient.session(OpenIdSession.ofIdToken(token));
        final CompletableFuture<AccessCredential> future = client.fetch(uri, AccessCredential.class)
            .toCompletableFuture();
        final CompletionException err = assertThrows(CompletionException.class, future::join);
        assertInstanceOf(AccessGrantException.class, err.getCause());
    }

    @Test
    void testFetch2() {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", WEBID);
        claims.put("sub", SUB);
        claims.put("iss", ISS);
        claims.put("azp", AZP);
        final String token = generateIdToken(claims);
        final URI uri = URIBuilder.newBuilder(baseUri).path("access-grant-2").build();
        final AccessGrantClient client = agClient.session(OpenIdSession.ofIdToken(token));
        final AccessGrant grant = client.fetch(uri, AccessGrant.class).toCompletableFuture().join();

        assertEquals(uri, grant.getIdentifier());
        assertEquals(baseUri, grant.getIssuer());

        // Revoke
        final CompletionException err1 = assertThrows(CompletionException.class,
                client.revoke(grant).toCompletableFuture()::join);
        assertInstanceOf(AccessGrantException.class, err1.getCause());
    }

    @Test
    void testFetch5() {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", WEBID);
        claims.put("sub", SUB);
        claims.put("iss", ISS);
        claims.put("azp", AZP);
        final String token = generateIdToken(claims);
        final URI uri = URIBuilder.newBuilder(baseUri).path("access-request-5").build();
        final AccessGrantClient client = agClient.session(OpenIdSession.ofIdToken(token));
        final AccessRequest request = client.fetch(uri, AccessRequest.class).toCompletableFuture().join();

        assertEquals(uri, request.getIdentifier());
        assertEquals(baseUri, request.getIssuer());

        final AccessCredentialVerification response = client.verify(request).toCompletableFuture().join();
        assertTrue(response.getChecks().contains("expirationDate"));
        assertTrue(response.getWarnings().isEmpty());
        assertTrue(response.getErrors().isEmpty());

        // Revoke
        final CompletableFuture<Void> future = client.revoke(request).toCompletableFuture();
        final CompletionException err1 = assertThrows(CompletionException.class, future::join);
        assertInstanceOf(AccessGrantException.class, err1.getCause());
    }


    @Test
    void testFetch6() {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", WEBID);
        claims.put("sub", SUB);
        claims.put("iss", ISS);
        claims.put("azp", AZP);
        final String token = generateIdToken(claims);
        final URI uri = URIBuilder.newBuilder(baseUri).path("access-grant-6").build();
        final AccessGrantClient client = agClient.session(OpenIdSession.ofIdToken(token));
        final AccessGrant grant = client.fetch(uri, AccessGrant.class).toCompletableFuture().join();

        assertEquals(uri, grant.getIdentifier());
        assertEquals(baseUri, grant.getIssuer());

        // Expected failure
        final CompletableFuture<AccessCredentialVerification> future1 = client.verify(grant).toCompletableFuture();
        final CompletionException err0 = assertThrows(CompletionException.class, future1::join);
        assertInstanceOf(AccessGrantException.class, err0.getCause());

        // Revoke
        final CompletableFuture<Void> future2 = client.revoke(grant).toCompletableFuture();
        final CompletionException err1 = assertThrows(CompletionException.class, future2::join);
        assertInstanceOf(AccessGrantException.class, err1.getCause());
    }

    @Test
    void testNotAccessGrant() {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", WEBID);
        claims.put("sub", SUB);
        claims.put("iss", ISS);
        claims.put("azp", AZP);
        final String token = generateIdToken(claims);
        final URI uri = URIBuilder.newBuilder(baseUri).path("vc-3").build();
        final AccessGrantClient client = agClient.session(OpenIdSession.ofIdToken(token));
        final CompletionException err = assertThrows(CompletionException.class,
                client.fetch(uri, AccessGrant.class).toCompletableFuture()::join);
    }

    @Test
    void testFetchInvalid() {
        final URI uri = URIBuilder.newBuilder(baseUri).path(".well-known/vc-configuration").build();
        final CompletionException err = assertThrows(CompletionException.class,
                agClient.fetch(uri, AccessRequest.class).toCompletableFuture()::join);

        assertInstanceOf(AccessGrantException.class, err.getCause());
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

        final URI recipient = URI.create("https://id.test/agent");
        final Instant expiration = Instant.parse("2022-08-27T12:00:00Z");
        final Set<String> modes = new HashSet<>(Arrays.asList("Read", "Append"));
        final Set<URI> purposes = Collections.singleton(URI.create("https://purpose.test/Purpose1"));

        final Set<URI> resources = Collections.singleton(URI.create("https://storage.test/data/"));
        final AccessRequest request = client.requestAccess(recipient, resources, modes, purposes, expiration)
            .toCompletableFuture().join();

        assertTrue(request.getTypes().contains("SolidAccessRequest"));
        assertEquals(Optional.of(recipient), request.getRecipient());
        assertEquals(modes, request.getModes());
        assertEquals(expiration, request.getExpiration());
        assertEquals(baseUri, request.getIssuer());
        assertEquals(purposes, request.getPurposes());
        assertEquals(resources, request.getResources());
    }

    @Test
    void testIssueRequestBuilder() {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", WEBID);
        claims.put("sub", SUB);
        claims.put("iss", ISS);
        claims.put("azp", AZP);
        final String token = generateIdToken(claims);
        final AccessGrantClient client = agClient.session(OpenIdSession.ofIdToken(token));

        final URI recipient = URI.create("https://id.test/agent");
        final Instant expiration = Instant.parse("2022-08-27T12:00:00Z");
        final Set<String> modes = new HashSet<>(Arrays.asList("Read", "Append"));
        final Set<URI> purposes = Collections.singleton(URI.create("https://purpose.test/Purpose1"));

        final Set<URI> resources = Collections.singleton(URI.create("https://storage.test/data/"));
        final AccessRequest.RequestParameters params = AccessRequest.RequestParameters.newBuilder()
            .recipient(recipient)
            .resources(resources)
            .modes(modes)
            .purposes(purposes)
            .expiration(expiration)
            .issuedAt(Instant.now()).build();
        final AccessRequest request = client.requestAccess(params).toCompletableFuture().join();

        assertTrue(request.getTypes().contains("SolidAccessRequest"));
        assertEquals(Optional.of(recipient), request.getRecipient());
        assertEquals(modes, request.getModes());
        assertEquals(expiration, request.getExpiration());
        assertEquals(baseUri, request.getIssuer());
        assertEquals(purposes, request.getPurposes());
        assertEquals(resources, request.getResources());
    }

    @Test
    void testRequestAccessNoAuth() {
        final URI recipient = URI.create("https://id.test/agent");
        final Instant expiration = Instant.parse("2022-08-27T12:00:00Z");
        final Set<String> modes = new HashSet<>(Arrays.asList("Read", "Append"));
        final Set<URI> purposes = Collections.singleton(URI.create("https://purpose.test/Purpose1"));

        final Set<URI> resources = Collections.singleton(URI.create("https://storage.test/data/"));

        final CompletableFuture<AccessRequest> future = agClient.requestAccess(recipient, resources, modes, purposes,
                expiration).toCompletableFuture();
        final CompletionException err = assertThrows(CompletionException.class, future::join);
        assertInstanceOf(AccessGrantException.class, err.getCause());
    }

    @Test
    void testGrantAccess() {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", WEBID);
        claims.put("sub", SUB);
        claims.put("iss", ISS);
        claims.put("azp", AZP);
        final String token = generateIdToken(claims);
        final AccessGrantClient client = agClient.session(OpenIdSession.ofIdToken(token));

        final URI recipient = URI.create("https://id.test/agent");
        final Instant expiration = Instant.parse("2022-08-27T12:00:00Z");
        final Set<String> modes = new HashSet<>(Arrays.asList("Read", "Append"));
        final Set<URI> purposes = Collections.singleton(URI.create("https://purpose.test/Purpose1"));

        final Set<URI> resources = Collections.singleton(URI.create("https://storage.test/data/"));
        final AccessRequest request = client.requestAccess(recipient, resources, modes, purposes, expiration)
            .toCompletableFuture().join();

        final AccessGrant grant = client.grantAccess(request).toCompletableFuture().join();

        assertTrue(grant.getTypes().contains("SolidAccessGrant"));
        assertEquals(Optional.of(recipient), grant.getRecipient());
        assertEquals(modes, grant.getModes());
        assertEquals(expiration, grant.getExpiration());
        assertEquals(baseUri, grant.getIssuer());
        assertEquals(purposes, grant.getPurposes());
        assertEquals(resources, grant.getResources());
    }

    @Test
    void testDenyAccess() {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", WEBID);
        claims.put("sub", SUB);
        claims.put("iss", ISS);
        claims.put("azp", AZP);
        final String token = generateIdToken(claims);
        final AccessGrantClient client = agClient.session(OpenIdSession.ofIdToken(token));

        final URI recipient = URI.create("https://id.test/agent");
        final Instant expiration = Instant.parse("2022-09-12T12:00:00Z");
        final Set<String> modes = new HashSet<>(Arrays.asList("Read", "Append"));
        final Set<URI> purposes = Collections.singleton(URI.create("https://purpose.test/Purpose1"));

        final Set<URI> resources = Collections.singleton(URI.create("https://storage.test/data/"));
        final AccessRequest request = client.requestAccess(recipient, resources, modes, purposes, expiration)
            .toCompletableFuture().join();

        final AccessDenial denial = client.denyAccess(request).toCompletableFuture().join();

        assertTrue(denial.getTypes().contains("SolidAccessDenial"));
        assertEquals(Optional.of(recipient), denial.getRecipient());
        assertEquals(modes, denial.getModes());
        assertEquals(expiration, denial.getExpiration());
        assertEquals(baseUri, denial.getIssuer());
        assertEquals(purposes, denial.getPurposes());
        assertEquals(resources, denial.getResources());

        final CompletableFuture<AccessDenial> future = client.session(Session.anonymous()).denyAccess(request)
            .toCompletableFuture();
        final CompletionException err = assertThrows(CompletionException.class, future::join);
        assertInstanceOf(AccessGrantException.class, err.getCause());
    }

    @Test
    void testGrantAccessNoAuth() {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", WEBID);
        claims.put("sub", SUB);
        claims.put("iss", ISS);
        claims.put("azp", AZP);
        final String token = generateIdToken(claims);
        final AccessGrantClient client = agClient.session(OpenIdSession.ofIdToken(token));

        final URI recipient = URI.create("https://id.test/agent");
        final Instant expiration = Instant.parse("2022-08-27T12:00:00Z");
        final Set<String> modes = new HashSet<>(Arrays.asList("Read", "Append"));
        final Set<URI> purposes = Collections.singleton(URI.create("https://purpose.test/Purpose1"));

        final Set<URI> resources = Collections.singleton(URI.create("https://storage.test/data/"));
        final AccessRequest request = client.requestAccess(recipient, resources, modes, purposes, expiration)
            .toCompletableFuture().join();

        final CompletableFuture<AccessGrant> future = agClient.grantAccess(request).toCompletableFuture();
        final CompletionException err = assertThrows(CompletionException.class, future::join);
        assertInstanceOf(AccessGrantException.class, err.getCause());
    }

    @Test
    void testQueryGrant() {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", WEBID);
        claims.put("sub", SUB);
        claims.put("iss", ISS);
        claims.put("azp", AZP);
        final String token = generateIdToken(claims);
        final AccessGrantClient client = agClient.session(OpenIdSession.ofIdToken(token));

        final URI resource = URI.create("https://storage.example/e973cc3d-5c28-4a10-98c5-e40079289358/a/b/c");
        final List<AccessGrant> grants = client.query(resource, null, null, null, "Read", AccessGrant.class)
                    .toCompletableFuture().join();
        assertEquals(1, grants.size());
    }

    @Test
    void testQueryGrantBuilder() {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", WEBID);
        claims.put("sub", SUB);
        claims.put("iss", ISS);
        claims.put("azp", AZP);
        final String token = generateIdToken(claims);
        final AccessGrantClient client = agClient.session(OpenIdSession.ofIdToken(token));

        final URI resource = URI.create("https://storage.example/e973cc3d-5c28-4a10-98c5-e40079289358/a/b/c");
        final AccessCredentialQuery<AccessGrant> query = AccessCredentialQuery.newBuilder()
            .resource(resource).mode("Read").build(AccessGrant.class);
        final List<AccessGrant> grants = client.query(query).toCompletableFuture().join();
        assertEquals(1, grants.size());
    }

    @Test
    void testQueryGrantRecipient() {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", WEBID);
        claims.put("sub", SUB);
        claims.put("iss", ISS);
        claims.put("azp", AZP);
        final String token = generateIdToken(claims);
        final AccessGrantClient client = agClient.session(OpenIdSession.ofIdToken(token));

        final List<AccessGrant> grants = client.query(null, null, URI.create("https://id.test/user"),
                null, "Read", AccessGrant.class).toCompletableFuture().join();
        assertEquals(1, grants.size());
    }

    @Test
    void testQueryGrantCreator() {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", WEBID);
        claims.put("sub", SUB);
        claims.put("iss", ISS);
        claims.put("azp", AZP);
        final String token = generateIdToken(claims);
        final AccessGrantClient client = agClient.session(OpenIdSession.ofIdToken(token));
        // A query is always done with the agent making the query as the creator.
        final List<AccessGrant> grants = client.query(
                null, URI.create("https://id.test/user"), null, null, "Read", AccessGrant.class
        ).toCompletableFuture().join();
        assertEquals(1, grants.size());
    }

    @Test
    void testQueryGrantModesPurposesBuilder() {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", WEBID);
        claims.put("sub", SUB);
        claims.put("iss", ISS);
        claims.put("azp", AZP);
        final String token = generateIdToken(claims);
        final AccessGrantClient client = agClient.session(OpenIdSession.ofIdToken(token));

        final AccessCredentialQuery<AccessGrant> query = AccessCredentialQuery.newBuilder()
            .resource(URI.create("https://storage.example/"))
            .mode("Read").mode("Write").purpose(URI.create("https://id.example/Purpose8"))
            .purpose(URI.create("https://id.example/Purpose9")).build(AccessGrant.class);
        final List<AccessGrant> grants = client.query(query).toCompletableFuture().join();
        assertEquals(1, grants.size());
    }

    @Test
    void testQueryGrantModesPurposesNoMatchBuilder() {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", WEBID);
        claims.put("sub", SUB);
        claims.put("iss", ISS);
        claims.put("azp", AZP);
        final String token = generateIdToken(claims);
        final AccessGrantClient client = agClient.session(OpenIdSession.ofIdToken(token));

        final AccessCredentialQuery<AccessGrant> query1 = AccessCredentialQuery.newBuilder()
            .resource(URI.create("https://storage.example/"))
            .mode("Read").mode(null).purpose(URI.create("https://id.example/Purpose8"))
            .purpose(URI.create("https://id.example/Purpose9")).purpose(null).build(AccessGrant.class);
        final List<AccessGrant> grants1 = client.query(query1).toCompletableFuture().join();
        assertEquals(0, grants1.size());

        final AccessCredentialQuery<AccessGrant> query2 = AccessCredentialQuery.newBuilder()
            .resource(URI.create("https://storage.example/")).mode("Read").mode("Write")
            .purpose(URI.create("https://id.example/Purpose9")).build(AccessGrant.class);
        final List<AccessGrant> grants2 = client.query(query2).toCompletableFuture().join();
        assertEquals(0, grants2.size());
    }

    @Test
    void testQueryRequestRecipient() {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", WEBID);
        claims.put("sub", SUB);
        claims.put("iss", ISS);
        claims.put("azp", AZP);
        final String token = generateIdToken(claims);
        final AccessGrantClient client = agClient.session(OpenIdSession.ofIdToken(token));

        final List<AccessRequest> requests = client.query(null, null, URI.create("https://id.test/user"),
                null, "Read", AccessRequest.class).toCompletableFuture().join();
        assertEquals(1, requests.size());
    }

    @Test
    void testQueryRequestRecipientBuilder() {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", WEBID);
        claims.put("sub", SUB);
        claims.put("iss", ISS);
        claims.put("azp", AZP);
        final String token = generateIdToken(claims);
        final AccessGrantClient client = agClient.session(OpenIdSession.ofIdToken(token));

        final AccessCredentialQuery<AccessRequest> query = AccessCredentialQuery.newBuilder()
            .recipient(URI.create("https://id.test/user")).mode("Read").build(AccessRequest.class);
        final List<AccessRequest> requests = client.query(query).toCompletableFuture().join();
        assertEquals(1, requests.size());
    }

    @Test
    void testQueryRequest() {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", WEBID);
        claims.put("sub", SUB);
        claims.put("iss", ISS);
        claims.put("azp", AZP);
        final String token = generateIdToken(claims);
        final AccessGrantClient client = agClient.session(OpenIdSession.ofIdToken(token));

        final URI resource = URI.create("https://storage.example/f1759e6d-4dda-4401-be61-d90d070a5474/a/b/c");
        final List<AccessRequest> requests = client.query(resource, null, null, null, "Read", AccessRequest.class)
            .toCompletableFuture().join();
        assertEquals(1, requests.size());
    }

    @Test
    void testQueryRequestBuilder() {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", WEBID);
        claims.put("sub", SUB);
        claims.put("iss", ISS);
        claims.put("azp", AZP);
        final String token = generateIdToken(claims);
        final AccessGrantClient client = agClient.session(OpenIdSession.ofIdToken(token));

        final URI resource = URI.create("https://storage.example/f1759e6d-4dda-4401-be61-d90d070a5474/a/b/c");
        final AccessCredentialQuery<AccessRequest> query = AccessCredentialQuery.newBuilder()
            .resource(resource).mode("Read").build(AccessRequest.class);
        final List<AccessRequest> requests = client.query(query).toCompletableFuture().join();
        assertEquals(1, requests.size());
    }

    @Test
    void testQueryDenial() {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", WEBID);
        claims.put("sub", SUB);
        claims.put("iss", ISS);
        claims.put("azp", AZP);
        final String token = generateIdToken(claims);
        final AccessGrantClient client = agClient.session(OpenIdSession.ofIdToken(token));

        final URI resource = URI.create("https://storage.example/ef9c4b90-0459-408d-bfa9-1c61d46e1eaf/e/f/g");
        final List<AccessDenial> grants = client.query(resource, null, null, null, "Read", AccessDenial.class)
            .toCompletableFuture().join();
        assertEquals(1, grants.size());
    }

    @Test
    void testQueryDenialBuilder() {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", WEBID);
        claims.put("sub", SUB);
        claims.put("iss", ISS);
        claims.put("azp", AZP);
        final String token = generateIdToken(claims);
        final AccessGrantClient client = agClient.session(OpenIdSession.ofIdToken(token));

        final URI resource = URI.create("https://storage.example/ef9c4b90-0459-408d-bfa9-1c61d46e1eaf/e/f/g");
        final AccessCredentialQuery<AccessDenial> query = AccessCredentialQuery.newBuilder()
            .resource(resource).mode("Read").build(AccessDenial.class);
        final List<AccessDenial> grants = client.query(query).toCompletableFuture().join();
        assertEquals(1, grants.size());
    }

    @Test
    void testQueryInvalidType() {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", WEBID);
        claims.put("sub", SUB);
        claims.put("iss", ISS);
        claims.put("azp", AZP);
        final String token = generateIdToken(claims);
        final AccessGrantClient client = agClient.session(OpenIdSession.ofIdToken(token));

        final URI uri = URI.create("https://storage.example/f1759e6d-4dda-4401-be61-d90d070a5474/a/b/c");
        assertThrows(AccessGrantException.class, () ->
                client.query(uri, null, null, null, "Read", AccessCredential.class));
    }

    @Test
    void testParentUri() {
        final URI root = URI.create("https://storage.test/");
        final URI a = URI.create("https://storage.test/a/");
        final URI b = URI.create("https://storage.test/a/b/");
        final URI c = URI.create("https://storage.test/a/b/c/");
        final URI d = URI.create("https://storage.test/a/b/c/d");
        assertNull(AccessGrantClient.getParent(null));
        assertNull(AccessGrantClient.getParent(URI.create("https://storage.test")));
        assertNull(AccessGrantClient.getParent(root));
        assertEquals(root, AccessGrantClient.getParent(a));
        assertEquals(a, AccessGrantClient.getParent(b));
        assertEquals(b, AccessGrantClient.getParent(c));
        assertEquals(c, AccessGrantClient.getParent(d));
    }

    @Test
    void testSuccessfulResponse() {
        assertFalse(AccessGrantClient.isSuccess(100));
        assertTrue(AccessGrantClient.isSuccess(200));
        assertFalse(AccessGrantClient.isSuccess(300));
        assertFalse(AccessGrantClient.isSuccess(400));
        assertFalse(AccessGrantClient.isSuccess(500));
    }

    @Test
    void isAccessGrantType() {
        assertTrue(AccessGrantClient.isAccessGrant(URI.create("SolidAccessGrant")));
        assertTrue(AccessGrantClient.isAccessGrant(URI.create("http://www.w3.org/ns/solid/vc#SolidAccessGrant")));
        assertFalse(AccessGrantClient.isAccessGrant(URI.create("SolidAccessRequest")));
        assertFalse(AccessGrantClient.isAccessGrant(URI.create("http://www.w3.org/ns/solid/vc#SolidAccessRequest")));
    }

    @Test
    void isAccessRequestType() {
        assertTrue(AccessGrantClient.isAccessRequest(URI.create("SolidAccessRequest")));
        assertTrue(AccessGrantClient.isAccessRequest(URI.create("http://www.w3.org/ns/solid/vc#SolidAccessRequest")));
        assertFalse(AccessGrantClient.isAccessRequest(URI.create("SolidAccessGrant")));
        assertFalse(AccessGrantClient.isAccessRequest(URI.create("http://www.w3.org/ns/solid/vc#SolidAccessGrant")));
    }

    @Test
    void checkAsUri() {
        final String uri = "https://example.com/";
        assertNull(AccessGrantClient.asUri(null));
        assertNull(AccessGrantClient.asUri(5));
        assertNull(AccessGrantClient.asUri(Arrays.asList(uri)));
        assertEquals(URI.create(uri), AccessGrantClient.asUri(uri));
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
