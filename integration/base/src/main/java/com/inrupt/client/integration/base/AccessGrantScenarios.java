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
package com.inrupt.client.integration.base;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.inrupt.client.Request;
import com.inrupt.client.Response;
import com.inrupt.client.accessgrant.AccessGrant;
import com.inrupt.client.accessgrant.AccessGrantClient;
import com.inrupt.client.accessgrant.AccessGrantSession;
import com.inrupt.client.accessgrant.AccessGrantUtils;
import com.inrupt.client.accessgrant.AccessRequest;
import com.inrupt.client.auth.Credential;
import com.inrupt.client.auth.Session;
import com.inrupt.client.openid.OpenIdException;
import com.inrupt.client.openid.OpenIdSession;
import com.inrupt.client.solid.*;
import com.inrupt.client.spi.RDFFactory;
import com.inrupt.client.util.URIBuilder;
import com.inrupt.client.vocabulary.ACL;
import com.inrupt.client.webid.WebIdProfile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Literal;
import org.apache.commons.rdf.api.RDF;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccessGrantScenarios {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessGrantScenarios.class);
    private static final RDF rdf = RDFFactory.getInstance();
    private static final Config config = ConfigProvider.getConfig();

    private static MockSolidServer mockHttpServer;
    private static MockOpenIDProvider identityProviderServer;
    private static MockUMAAuthorizationServer authServer;
    private static MockWebIdService webIdService;
    private static MockAccessGrantServer accessGrantServer;

    private static String podUrl;
    private static String issuer;
    protected static String webidUrl;
    private static final String MOCK_USERNAME = "someuser";

    private static final String CLIENT_ID = config.getValue("inrupt.test.client-id", String.class);
    private static final String CLIENT_SECRET = config.getValue("inrupt.test.client-secret", String.class);
    private static final String AUTH_METHOD = config
        .getOptionalValue("inrupt.test.auth-method", String.class)
        .orElse("client_secret_basic");
    protected static String ACCESS_GRANT_PROVIDER;
    private static final String PRIVATE_RESOURCE_PATH = config
        .getOptionalValue("inrupt.test.private-resource-path", String.class)
        .orElse("private");

    private static final URI ACCESS_GRANT = URI.create("http://www.w3.org/ns/solid/vc#SolidAccessGrant");
    private static final URI ACCESS_REQUEST = URI.create("http://www.w3.org/ns/solid/vc#SolidAccessRequest");
    protected static final String GRANT_MODE_READ = "Read";
    private static final String GRANT_MODE_APPEND = "Append";
    private static final String GRANT_MODE_WRITE = "Write";
    private static final URI PURPOSE1 = URI.create("https://purpose.example/212efdf4-e1a4-4dcd-9d3b-d6eb92e0205f");
    private static final URI PURPOSE2 = URI.create("https://purpose.example/de605b08-76c7-4f04-9cec-a438810b0c03");
    protected static final Set<URI> PURPOSES = new HashSet<>(Arrays.asList(PURPOSE1, PURPOSE2));
    protected static final String GRANT_EXPIRATION = "2024-04-03T12:00:00Z";

    private static URI testContainerURI;
    private static String testRDFresourceName = "resource.ttl";
    private static URI testRDFresourceURI;
    private static String sharedTextFileName = "sharedFile.txt";
    private static String sharedResourceName = "sharedResource";
    protected static URI sharedTextFileURI;
    private static URI sharedResource;
    private static Session session;

    @BeforeAll
    static void setup() throws IOException {
        authServer = new MockUMAAuthorizationServer();
        authServer.start();

        mockHttpServer = new MockSolidServer(authServer.getMockServerUrl());
        mockHttpServer.start();

        identityProviderServer = new MockOpenIDProvider(MOCK_USERNAME);
        identityProviderServer.start();

        webIdService = new MockWebIdService(
            mockHttpServer.getMockServerUrl(),
            identityProviderServer.getMockServerUrl(),
            MOCK_USERNAME);
        webIdService.start();

        webidUrl = config
            .getOptionalValue("inrupt.test.webid", String.class)
            .orElse(URIBuilder.newBuilder(URI.create(webIdService.getMockServerUrl()))
                .path(MOCK_USERNAME)
                .build()
                .toString());

        State.PRIVATE_RESOURCE_PATH = PRIVATE_RESOURCE_PATH;
        State.WEBID = URI.create(webidUrl);
        final SolidSyncClient client = SolidSyncClient.getClientBuilder().build();
        try (final WebIdProfile profile = client.read(URI.create(webidUrl), WebIdProfile.class)) {
            issuer = profile.getOidcIssuers().iterator().next().toString();
            podUrl = profile.getStorages().iterator().next().toString();
        }
        if (!podUrl.endsWith(Utils.FOLDER_SEPARATOR)) {
            podUrl += Utils.FOLDER_SEPARATOR;
        }

        testContainerURI = URIBuilder.newBuilder(URI.create(podUrl))
            .path(State.PRIVATE_RESOURCE_PATH)
            .path("accessgrant-test-" + UUID.randomUUID())
            .build();

        sharedTextFileURI = URIBuilder.newBuilder(URI.create(testContainerURI.toString()))
            .path(sharedTextFileName)
            .build();
        sharedResource = URIBuilder.newBuilder(URI.create(testContainerURI.toString()))
            .path(sharedResourceName).build();

        testRDFresourceURI = URIBuilder.newBuilder(testContainerURI)
            .path(testRDFresourceName)
            .build();

        session = OpenIdSession.ofClientCredentials(
            URI.create(issuer), //Client credentials
            CLIENT_ID,
            CLIENT_SECRET,
            AUTH_METHOD);

        //create test file in test container
        try (final InputStream is = new ByteArrayInputStream(StandardCharsets.UTF_8.encode("Test text").array())) {
            final SolidNonRDFSource testResource = new SolidNonRDFSource(sharedTextFileURI, Utils.PLAIN_TEXT, is, null);
            final SolidSyncClient authClient = client.session(session);
            assertDoesNotThrow(() -> authClient.create(testResource));

            prepareACPofResource(authClient, sharedTextFileURI);
        }

        accessGrantServer = new MockAccessGrantServer(
                State.WEBID,
                sharedTextFileURI,
                sharedResource,
                authServer.getMockServerUrl()
        );
        accessGrantServer.start();

        ACCESS_GRANT_PROVIDER = config
            .getOptionalValue("inrupt.test.access-grant.provider", String.class)
            .orElse(accessGrantServer.getMockServerUrl());

        final AccessGrantClient accessGrantClient = new AccessGrantClient(
                URI.create(ACCESS_GRANT_PROVIDER)
        ).session(session);
        final Set<String> modes = new HashSet<>(Arrays.asList(GRANT_MODE_READ, GRANT_MODE_APPEND));
        final Instant expiration = Instant.parse(GRANT_EXPIRATION);

        final AccessRequest request = accessGrantClient.requestAccess(URI.create(webidUrl),
            new HashSet<>(Arrays.asList(sharedResource)), modes, PURPOSES, expiration)
            .toCompletableFuture().join();
        final AccessGrant grant = accessGrantClient.grantAccess(request)
            .toCompletableFuture().join();

        LOGGER.info("Integration Test Issuer: [{}]", issuer);
        LOGGER.info("Integration Test Pod Host: [{}]", podUrl);
        LOGGER.info("Integration Test Access Grant server: [{}]", ACCESS_GRANT_PROVIDER);
    }

    @AfterAll
    static void teardown() {
         //cleanup pod
        session = OpenIdSession.ofClientCredentials(
            URI.create(issuer), //Client credentials
            CLIENT_ID,
            CLIENT_SECRET,
            AUTH_METHOD);
        final SolidSyncClient client = SolidSyncClient.getClientBuilder()
            .build().session(session);
        client.send(Request.newBuilder(sharedTextFileURI).DELETE().build(), Response.BodyHandlers.discarding());
        client.send(Request.newBuilder(testRDFresourceURI).DELETE().build(), Response.BodyHandlers.discarding());
        client.send(Request.newBuilder(sharedTextFileURI.resolve(".")).DELETE().build(),
            Response.BodyHandlers.discarding());

        mockHttpServer.stop();
        identityProviderServer.stop();
        authServer.stop();
        webIdService.stop();
        accessGrantServer.stop();
    }

    @ParameterizedTest
    @MethodSource("provideSessions")
    @DisplayName(":accessGrantLifecycle Access Grant issuance lifecycle")
    void accessGrantIssuanceLifecycleTest(final Session session) {
        LOGGER.info("Integration Test - Access Grant issuance lifecycle");

        final AccessGrantClient accessGrantClient = new AccessGrantClient(
                URI.create(ACCESS_GRANT_PROVIDER)
        ).session(session);

        final Set<String> modes = new HashSet<>(Arrays.asList(GRANT_MODE_READ));
        final Instant expiration = Instant.parse(GRANT_EXPIRATION);
        final AccessRequest request = accessGrantClient.requestAccess(URI.create(webidUrl),
            new HashSet<>(Arrays.asList(sharedTextFileURI)), modes, PURPOSES, expiration)
            .toCompletableFuture().join();
        final AccessGrant grant = accessGrantClient.grantAccess(request)
            .toCompletableFuture().join();

        //2. call verify endpoint to verify grant
        final var grantVerification = accessGrantClient.verify(grant).toCompletableFuture().join();
        assertTrue(grantVerification.getChecks().size() > 0);
        assertEquals(grantVerification.getErrors().size(), 0);
        assertEquals(grantVerification.getWarnings().size(), 0);

        final AccessGrant grantFromVcProvider = accessGrantClient.fetch(grant.getIdentifier(), AccessGrant.class)
            .toCompletableFuture().join();
        assertEquals(grant.getPurposes(), grantFromVcProvider.getPurposes());

        //unauthorized request test
        final SolidSyncClient client = SolidSyncClient.getClientBuilder().build();
        final var err = assertThrows(UnauthorizedException.class,
                () -> client.read(sharedTextFileURI, SolidNonRDFSource.class));
        assertEquals(Utils.UNAUTHORIZED, err.getStatusCode());
        final Request reqRead =
                Request.newBuilder(sharedTextFileURI).header(Utils.CONTENT_TYPE, Utils.PLAIN_TEXT)
                        .GET().build();
        assertDoesNotThrow(() -> client.send(reqRead, Response.BodyHandlers.discarding()));

        //authorized request test
        final Session accessSession = AccessGrantSession.ofAccessGrant(session, grant);
        final SolidSyncClient authClient = client.session(accessSession);

        try (final SolidNonRDFSource resource = authClient.read(sharedTextFileURI, SolidNonRDFSource.class)) {
            assertTrue(resource.getMetadata().getContentType().contains(Utils.PLAIN_TEXT));
        }
        final Request reqReadAgain =
                Request.newBuilder(sharedTextFileURI).header(Utils.CONTENT_TYPE, Utils.PLAIN_TEXT)
                        .GET().build();
        assertDoesNotThrow(() -> authClient.send(reqReadAgain, Response.BodyHandlers.discarding()));

        assertDoesNotThrow(accessGrantClient.revoke(grant).toCompletableFuture()::join);

        //6. call verify endpoint to check the grant is not valid
        final var revokedGrantVerification = accessGrantClient.verify(grant).toCompletableFuture().join();
        assertTrue(grantVerification.getChecks().size() > 0);
        assertEquals(revokedGrantVerification.getErrors().size(), 1);
        assertEquals(grantVerification.getWarnings().size(), 0);
    }

    @ParameterizedTest
    @MethodSource("provideSessions")
    @DisplayName(":accessGrantOverride Access Grant with request overrides")
    void accessGrantWithRequestOverridesTest(final Session session) {
        LOGGER.info("Integration Test - Access Grant with request overrides");

        final AccessGrantClient accessGrantClient = new AccessGrantClient(
                URI.create(ACCESS_GRANT_PROVIDER)
        ).session(session);

        final Set<String> modes = new HashSet<>(Arrays.asList(GRANT_MODE_READ, GRANT_MODE_APPEND));
        final Instant expiration = Instant.now().plus(90, ChronoUnit.DAYS);
        final AccessRequest request = accessGrantClient.requestAccess(URI.create(webidUrl),
            new HashSet<>(Arrays.asList(sharedTextFileURI)), modes, PURPOSES, expiration)
            .toCompletableFuture().join();
        final AccessGrant grant = accessGrantClient.grantAccess(request)
            .toCompletableFuture().join();

        //2. call verify endpoint to verify grant
        final var grantVerification = accessGrantClient.verify(grant).toCompletableFuture().join();
        assertTrue(grantVerification.getChecks().size() > 0);
        assertEquals(grantVerification.getErrors().size(), 0);
        assertEquals(grantVerification.getWarnings().size(), 0);
    }

    @ParameterizedTest
    @MethodSource("provideSessions")
    @DisplayName(":accessGrantNonRecursive Issue a non-recursive access grant")
    void accessGrantNonRecursiveTest(final Session session) {
        LOGGER.info("Integration Test - Issue a non-recursive access grant");

        final AccessGrantClient accessGrantClient = new AccessGrantClient(
                URI.create(ACCESS_GRANT_PROVIDER)
        ).session(session);

        final Set<String> modes = new HashSet<>(Arrays.asList(GRANT_MODE_READ));
        final Instant expiration = Instant.parse(GRANT_EXPIRATION);
        final AccessRequest request = accessGrantClient.requestAccess(URI.create(webidUrl),
            new HashSet<>(Arrays.asList(sharedTextFileURI)), modes, PURPOSES, expiration)
            .toCompletableFuture().join();
        final AccessGrant grant = accessGrantClient.grantAccess(request)
            .toCompletableFuture().join();
        //Steps

        //1. call verify endpoint to verify grant
        final var grantVerification = accessGrantClient.verify(grant).toCompletableFuture().join();
        assertTrue(grantVerification.getChecks().size() > 0);
        assertEquals(grantVerification.getErrors().size(), 0);
        assertEquals(grantVerification.getWarnings().size(), 0);

        //2. use the grant to access the target container.
        //3. try using the grant to access a child resource.
    }

    // Query access grant related tests
    @ParameterizedTest
    @MethodSource("provideSessions")
    @DisplayName(":accessGrantQueryByRequestor Lookup Access Grants by requestor")
    void accessGrantQueryByRequestorTest(final Session session) {
        LOGGER.info("Integration Test - Lookup Access Grants by requestor");

        final AccessGrantClient accessGrantClient = new AccessGrantClient(
                URI.create(ACCESS_GRANT_PROVIDER)
        ).session(session);

        //query for all grants issued by the user
        final List<AccessRequest> grants = accessGrantClient.query(sharedResource, null, URI.create(webidUrl),
                PURPOSE1, GRANT_MODE_READ, AccessRequest.class)
            .toCompletableFuture().join();
        // result is 4 because we retrieve the grants for each path
        // sharedTextFileURI =
        // http://localhost:57577/private/accessgrant-test-2c82772f-7c0a-4e39-9466-abf9756b59c7/sharedFile.txt
        assertEquals(1, grants.size());

        //query for all grants issued by a random user
        final List<AccessRequest> randomGrants = accessGrantClient.query(sharedResource, null,
                URI.create("https://someuser.test"), PURPOSE1, GRANT_MODE_READ, AccessRequest.class)
            .toCompletableFuture().join();
        assertEquals(0, randomGrants.size());
    }

    @ParameterizedTest
    @MethodSource("provideSessions")
    @DisplayName(":accessGrantQueryByResource Lookup Access Grants by resource")
    void accessGrantQueryByResourceTest(final Session session) {
        LOGGER.info("Integration Test - Lookup Access Grants by resource");

        final AccessGrantClient accessGrantClient = new AccessGrantClient(
                URI.create(ACCESS_GRANT_PROVIDER)
        ).session(session);

        //query for all grants of a dedicated resource
        final List<AccessRequest> requests = accessGrantClient.query(sharedResource, null, URI.create(webidUrl),
                PURPOSE1, GRANT_MODE_READ, AccessRequest.class)
            .toCompletableFuture().join();
        assertEquals(1, requests.size());

        //query for all grants of a random resource
        final List<AccessRequest> randomGrants = accessGrantClient.query(URI.create("https://somerandom.test"),
                null, URI.create(webidUrl), PURPOSE1, GRANT_MODE_READ, AccessRequest.class)
            .toCompletableFuture().join();
        assertEquals(0, randomGrants.size());
    }

    @ParameterizedTest
    @MethodSource("provideSessions")
    @DisplayName(":accessGrantQueryByPurpose Lookup Access Grants by purpose")
    void accessGrantQueryByPurposeTest(final Session session) {
        LOGGER.info("Integration Test - Lookup Access Grants by purpose");

        final AccessGrantClient accessGrantClient = new AccessGrantClient(
                URI.create(ACCESS_GRANT_PROVIDER)
        ).session(session);

        //query for all grants with a dedicated purpose
        final List<AccessRequest> requests = accessGrantClient.query(sharedResource, null, URI.create(webidUrl),
                PURPOSE1, GRANT_MODE_READ, AccessRequest.class)
            .toCompletableFuture().join();
        assertEquals(1, requests.size());

        //query for all grants of dedicated purpose combinations
        final List<AccessGrant> randomGrants = accessGrantClient.query(sharedResource, null, URI.create(webidUrl),
                PURPOSE1, GRANT_MODE_WRITE, AccessGrant.class)
            .toCompletableFuture().join();
        assertEquals(0, randomGrants.size()); //our grant is actually a Read
    }

    //Interacting with resource related tests
    @ParameterizedTest
    @MethodSource("provideSessions")
    @DisplayName(":accessGrantGetRdf Fetching RDF using Access Grant")
    void accessGrantGetRdfTest(final Session session) {
        LOGGER.info("Integration Test - Fetching RDF using Access Grant");

        final SolidSyncClient client = SolidSyncClient.getClientBuilder()
            .build().session(session);

        try (final SolidRDFSource resource = new SolidRDFSource(testRDFresourceURI, null, null)) {
            assertDoesNotThrow(() -> client.create(resource));
        }

        final AccessGrantClient accessGrantClient = new AccessGrantClient(
                URI.create(ACCESS_GRANT_PROVIDER)
        ).session(session);

        final Set<String> modes = new HashSet<>(Arrays.asList(GRANT_MODE_READ, GRANT_MODE_WRITE, GRANT_MODE_APPEND));
        final Instant expiration = Instant.parse(GRANT_EXPIRATION);
        final AccessRequest request = accessGrantClient.requestAccess(URI.create(webidUrl),
            new HashSet<>(Arrays.asList(testRDFresourceURI)), modes, PURPOSES, expiration)
            .toCompletableFuture().join();
        final AccessGrant grant = accessGrantClient.grantAccess(request)
            .toCompletableFuture().join();
        final Session newSession = AccessGrantSession.ofAccessGrant(session, grant);
        final SolidSyncClient newClient = SolidSyncClient.getClientBuilder()
            .build().session(newSession);

        try (final SolidRDFSource resource = newClient.read(testRDFresourceURI, SolidRDFSource.class)) {
            assertTrue(resource.getMetadata().getContentType().contains(Utils.TEXT_TURTLE));
        }

        newClient.delete(testRDFresourceURI);
        assertDoesNotThrow(accessGrantClient.revoke(grant).toCompletableFuture()::join);
    }

    @ParameterizedTest
    @MethodSource("provideSessions")
    @DisplayName(":accessGrantSetRdf Appending RDF using Access Grant")
    void accessGrantSetRdfTest(final Session session) {
        LOGGER.info("Integration Test - Appending RDF using Access Grant");

        final SolidSyncClient client = SolidSyncClient.getClientBuilder()
            .build().session(session);

        try (final SolidRDFSource resource = new SolidRDFSource(testRDFresourceURI)) {
            assertDoesNotThrow(() -> client.create(resource));

            final AccessGrantClient accessGrantClient = new AccessGrantClient(
                    URI.create(ACCESS_GRANT_PROVIDER)
            ).session(session);

            final Set<String> modes = new HashSet<>(Arrays.asList(
                GRANT_MODE_READ, GRANT_MODE_WRITE, GRANT_MODE_APPEND));
            final Instant expiration = Instant.parse(GRANT_EXPIRATION);
            final AccessRequest request = accessGrantClient.requestAccess(URI.create(webidUrl),
                new HashSet<>(Arrays.asList(testRDFresourceURI)), modes, PURPOSES, expiration)
                .toCompletableFuture().join();
            final AccessGrant grant = accessGrantClient.grantAccess(request)
                .toCompletableFuture().join();
            final Session newSession = AccessGrantSession.ofAccessGrant(session, grant);
            final SolidSyncClient authClient = SolidSyncClient.getClientBuilder()
                .build().session(newSession);

            final String newResourceName = testRDFresourceURI.toString();
            final String newPredicateName = "https://example.example/predicate";
            final IRI booleanType = rdf.createIRI("http://www.w3.org/2001/XMLSchema#boolean");

            final IRI newResourceNode = rdf.createIRI(newResourceName);
            final IRI newPredicateNode = rdf.createIRI(newPredicateName);
            final Literal object = rdf.createLiteral("true", booleanType);

            resource.add(rdf.createQuad(newResourceNode, newResourceNode, newPredicateNode, object));

            assertDoesNotThrow(() -> authClient.update(resource));

            authClient.delete(testRDFresourceURI);

            assertDoesNotThrow(accessGrantClient.revoke(grant).toCompletableFuture()::join);
        }
    }

    @ParameterizedTest
    @MethodSource("provideSessions")
    @DisplayName(":accessGrantCreateRdf Creating RDF using Access Grant")
    void accessGrantCreateRdfTest(final Session session) {
        LOGGER.info("Integration Test - Creating RDF using Access Grant");

        final AccessGrantClient accessGrantClient = new AccessGrantClient(
                URI.create(ACCESS_GRANT_PROVIDER)
        ).session(session);

        final URI newTestFileURI = URIBuilder.newBuilder(testContainerURI)
            .path("newRdf.ttl")
            .build();

        final Set<String> modes = new HashSet<>(Arrays.asList(GRANT_MODE_READ, GRANT_MODE_WRITE, GRANT_MODE_APPEND));
        final Instant expiration = Instant.parse(GRANT_EXPIRATION);
        final AccessRequest request = accessGrantClient.requestAccess(URI.create(webidUrl),
            new HashSet<>(Arrays.asList(newTestFileURI)), modes, PURPOSES, expiration)
            .toCompletableFuture().join();
        final AccessGrant grant = accessGrantClient.grantAccess(request)
            .toCompletableFuture().join();

        final Session newSession = AccessGrantSession.ofAccessGrant(session, grant);
        final SolidSyncClient authClient = SolidSyncClient.getClientBuilder()
            .build().session(newSession);

        try (final SolidRDFSource resource = new SolidRDFSource(newTestFileURI)) {
            assertDoesNotThrow(() -> authClient.create(resource));
        }

        authClient.delete(newTestFileURI);
        assertDoesNotThrow(accessGrantClient.revoke(grant).toCompletableFuture()::join);
    }

    @ParameterizedTest
    @MethodSource("provideSessions")
    @DisplayName(":accessGrantGetNonRdf Fetching non-RDF using Access Grant")
    void accessGrantGetNonRdfTest(final Session session) throws IOException {
        LOGGER.info("Integration Test - Fetching non-RDF using Access Grant");

        final SolidSyncClient client = SolidSyncClient.getClientBuilder().build().session(session);

        final URI newTestFileURI = URIBuilder.newBuilder(testContainerURI)
            .path("newFile.txt")
            .build();

        try (final InputStream is = new ByteArrayInputStream(
            StandardCharsets.UTF_8.encode("Test test test text").array())) {
            final SolidNonRDFSource testResource =
                new SolidNonRDFSource(newTestFileURI, Utils.PLAIN_TEXT, is, null);
            assertDoesNotThrow(() -> client.create(testResource));
        }

        final AccessGrantClient accessGrantClient = new AccessGrantClient(
                URI.create(ACCESS_GRANT_PROVIDER)
        ).session(session);

        final Set<String> modes = new HashSet<>(Arrays.asList(GRANT_MODE_READ, GRANT_MODE_WRITE, GRANT_MODE_APPEND));
        final Instant expiration = Instant.parse(GRANT_EXPIRATION);
        final AccessRequest request = accessGrantClient.requestAccess(URI.create(webidUrl),
            new HashSet<>(Arrays.asList(newTestFileURI)), modes, PURPOSES, expiration)
            .toCompletableFuture().join();
        final AccessGrant grant = accessGrantClient.grantAccess(request)
            .toCompletableFuture().join();
        final Session newSession = AccessGrantSession.ofAccessGrant(session, grant);
        final SolidSyncClient authClient = SolidSyncClient.getClientBuilder()
            .build().session(newSession);

        try (final SolidNonRDFSource resource = authClient.read(newTestFileURI, SolidNonRDFSource.class)) {
            assertTrue(resource.getMetadata().getContentType().contains(Utils.PLAIN_TEXT));
        }

        final Request reqCreate =
                Request.newBuilder(newTestFileURI).header(Utils.CONTENT_TYPE, Utils.PLAIN_TEXT)
                        .GET().build();
        assertDoesNotThrow(() -> authClient.send(reqCreate, Response.BodyHandlers.discarding()));

        authClient.delete(newTestFileURI);
        assertDoesNotThrow(accessGrantClient.revoke(grant).toCompletableFuture()::join);
    }

    @ParameterizedTest
    @MethodSource("provideSessions")
    @DisplayName(":accessGrantSetNonRdf Overwriting non-RDF using Access Grant")
    void accessGrantSetNonRdfTest(final Session session) throws IOException {
        LOGGER.info("Integration Test - Overwriting non-RDF using Access Grant");

        final SolidSyncClient client = SolidSyncClient.getClientBuilder().build().session(session);

        final URI newTestFileURI = URIBuilder.newBuilder(testContainerURI)
            .path("newFile.txt")
            .build();

        try (final InputStream is = new ByteArrayInputStream(
            StandardCharsets.UTF_8.encode("Test test test text").array())) {
            final SolidNonRDFSource testResource =
                new SolidNonRDFSource(newTestFileURI, Utils.PLAIN_TEXT, is, null);
            assertDoesNotThrow(() -> client.create(testResource));
        }

        final AccessGrantClient accessGrantClient = new AccessGrantClient(
                URI.create(ACCESS_GRANT_PROVIDER)
        ).session(session);

        final Set<String> modes = new HashSet<>(Arrays.asList(GRANT_MODE_READ, GRANT_MODE_WRITE, GRANT_MODE_APPEND));
        final Instant expiration = Instant.parse(GRANT_EXPIRATION);
        final AccessRequest request = accessGrantClient.requestAccess(URI.create(webidUrl),
            new HashSet<>(Arrays.asList(newTestFileURI)), modes, PURPOSES, expiration)
            .toCompletableFuture().join();
        final AccessGrant grant = accessGrantClient.grantAccess(request)
            .toCompletableFuture().join();
        final Session newSession = AccessGrantSession.ofAccessGrant(session, grant);
        final SolidSyncClient authClient = SolidSyncClient.getClientBuilder()
            .build().session(newSession);

        try (final SolidNonRDFSource resource = authClient.read(newTestFileURI, SolidNonRDFSource.class)) {
            try (final InputStream newis = new ByteArrayInputStream(
                StandardCharsets.UTF_8.encode("Test text").array())) {
                final SolidNonRDFSource testResource =
                    new SolidNonRDFSource(newTestFileURI, Utils.PLAIN_TEXT, newis, resource.getMetadata());
                assertDoesNotThrow(() -> authClient.update(testResource));
            }
        }

        try (final InputStream newis = new ByteArrayInputStream(
            StandardCharsets.UTF_8.encode("Test text").array())) {
            final Request reqCreate =
                Request.newBuilder(newTestFileURI).header(Utils.CONTENT_TYPE, Utils.PLAIN_TEXT)
                    .PUT(Request.BodyPublishers.ofInputStream(newis)).build();
            assertDoesNotThrow(() -> authClient.send(reqCreate, Response.BodyHandlers.discarding()));
        }

        authClient.delete(newTestFileURI);
        assertDoesNotThrow(accessGrantClient.revoke(grant).toCompletableFuture()::join);
    }

    @ParameterizedTest
    @MethodSource("provideSessions")
    @DisplayName(":accessGrantCreateNonRdf Creating non-RDF using Access Grant")
    void accessGrantCreateNonRdfTest(final Session session) throws IOException {
        LOGGER.info("Integration Test - Creating non-RDF using Access Grant");

        final URI newTestFileURI = URIBuilder.newBuilder(testContainerURI)
            .path("newFile.txt")
            .build();

        final AccessGrantClient accessGrantClient = new AccessGrantClient(
                URI.create(ACCESS_GRANT_PROVIDER)
        ).session(session);

        final Set<String> modes = new HashSet<>(Arrays.asList(GRANT_MODE_READ, GRANT_MODE_WRITE, GRANT_MODE_APPEND));
        final Instant expiration = Instant.parse(GRANT_EXPIRATION);
        final AccessRequest request = accessGrantClient.requestAccess(URI.create(webidUrl),
            new HashSet<>(Arrays.asList(newTestFileURI)), modes, PURPOSES, expiration)
            .toCompletableFuture().join();
        final AccessGrant grant = accessGrantClient.grantAccess(request)
            .toCompletableFuture().join();
        final Session newSession = AccessGrantSession.ofAccessGrant(session, grant);
        final SolidSyncClient authClient = SolidSyncClient.getClient().session(newSession);

        try (final InputStream is = new ByteArrayInputStream(
            StandardCharsets.UTF_8.encode("Test test test text").array())) {
            final SolidNonRDFSource testResource =
                new SolidNonRDFSource(newTestFileURI, Utils.PLAIN_TEXT, is, null);
            assertDoesNotThrow(() -> authClient.create(testResource));
        }

        authClient.delete(newTestFileURI);
        assertDoesNotThrow(accessGrantClient.revoke(grant).toCompletableFuture()::join);
    }

    private static void prepareACPofResource(final SolidSyncClient authClient, final URI resourceURI) {

        // find the acl Link in the header of the resource
        try (final SolidNonRDFSource resource = authClient.read(resourceURI, SolidNonRDFSource.class)) {
            resource.getMetadata().getAcl().ifPresent(acl -> {
                try (final SolidRDFSource acr = authClient.read(acl, SolidRDFSource.class)) {
                    AccessGrantUtils.accessControlPolicyTriples(acl, ACL.Read, ACL.Write)
                        .forEach(acr.getGraph()::add);
                    authClient.update(acr);
                }
            });
        }
    }

    private static Stream<Arguments> provideSessions() throws SolidClientException {
        session = OpenIdSession.ofClientCredentials(
            URI.create(issuer), //Client credentials
            CLIENT_ID,
            CLIENT_SECRET,
            AUTH_METHOD);
        final Optional<Credential> credential = session.getCredential(OpenIdSession.ID_TOKEN, null);
        final var token = credential.map(Credential::getToken)
            .orElseThrow(() -> new OpenIdException("We could not get a token"));
        return Stream.of(
            Arguments.of(OpenIdSession.ofIdToken(token)), //OpenId token
            Arguments.of(session)
            );
    }
}
