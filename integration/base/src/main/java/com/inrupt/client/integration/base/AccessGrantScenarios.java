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
package com.inrupt.client.integration.base;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import com.inrupt.client.Request;
import com.inrupt.client.Response;
import com.inrupt.client.accessgrant.*;
import com.inrupt.client.auth.Session;
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
    protected static String requesterWebidUrl;
    private static final String MOCK_RESOURCE_OWNER_USERNAME = "someuser";
    private static final String MOCK_REQUESTER_USERNAME = "requester";

    private static final String REQUESTER_CLIENT_ID = config.getValue("inrupt.test.requester.client-id", String.class);
    private static final String REQUESTER_CLIENT_SECRET =
        config.getValue("inrupt.test.requester.client-secret", String.class);

    private static final String RESOURCE_OWNER_CLIENT_ID =
        config.getValue("inrupt.test.client-id", String.class);
    private static final String RESOURCE_OWNER_CLIENT_SECRET =
        config.getValue("inrupt.test.client-secret", String.class);
    private static final String AUTH_METHOD = config
        .getOptionalValue("inrupt.test.auth-method", String.class)
        .orElse("client_secret_basic");

    protected static String ACCESS_GRANT_PROVIDER;
    protected static final String GRANT_MODE_READ = "Read";
    private static final String GRANT_MODE_APPEND = "Append";
    private static final String GRANT_MODE_WRITE = "Write";
    private static final URI PURPOSE1 = URI.create("https://purpose.example/212efdf4-e1a4-4dcd-9d3b-d6eb92e0205f");
    private static final URI PURPOSE2 = URI.create("https://purpose.example/de605b08-76c7-4f04-9cec-a438810b0c03");
    protected static final Set<URI> PURPOSES = new HashSet<>(Arrays.asList(PURPOSE1, PURPOSE2));
    protected static final String GRANT_EXPIRATION = Instant.now().plus(1, ChronoUnit.HOURS)
        .truncatedTo(ChronoUnit.SECONDS).toString();
    private static final String sharedTextFileName = "sharedFile.txt";
    protected static URI sharedTextFileURI;
    private static URI privateContainerURI;

    private static SolidSyncClient authResourceOwnerClient;

    @BeforeAll
    static void setup() throws IOException {
        LOGGER.info("Setup AccessGrantScenarios test");
        if (config.getOptionalValue("inrupt.test.webid", String.class).isPresent()) {
            LOGGER.info("Running AccessGrantScenarios on live server");
            webidUrl = config.getOptionalValue("inrupt.test.webid", String.class).get();
            requesterWebidUrl = config.getOptionalValue("inrupt.test.requester.webid", String.class).get();
        } else {
            LOGGER.info("Running AccessGrantScenarios on Mock services");
            authServer = new MockUMAAuthorizationServer();
            authServer.start();

            mockHttpServer = new MockSolidServer(authServer.getMockServerUrl());
            mockHttpServer.start();

            identityProviderServer = new MockOpenIDProvider(MOCK_RESOURCE_OWNER_USERNAME);
            identityProviderServer.start();

            webIdService = new MockWebIdService(
                    mockHttpServer.getMockServerUrl(),
                    identityProviderServer.getMockServerUrl(),
                    MOCK_RESOURCE_OWNER_USERNAME);
            webIdService.start();

            webidUrl = URIBuilder.newBuilder(URI.create(webIdService.getMockServerUrl()))
                    .path(MOCK_RESOURCE_OWNER_USERNAME)
                    .build()
                    .toString();
            requesterWebidUrl = URIBuilder.newBuilder(URI.create(webIdService.getMockServerUrl()))
                    .path(MOCK_REQUESTER_USERNAME)
                    .build()
                    .toString();
        }

        State.WEBID = URI.create(webidUrl);
        final SolidSyncClient client = SolidSyncClient.getClientBuilder().build();
        try (final WebIdProfile profile = client.read(URI.create(webidUrl), WebIdProfile.class)) {
            issuer = profile.getOidcIssuers().iterator().next().toString();
            podUrl = profile.getStorages().iterator().next().toString();
        }
        if (!podUrl.endsWith(Utils.FOLDER_SEPARATOR)) {
            podUrl += Utils.FOLDER_SEPARATOR;
        }

        authResourceOwnerClient = createAuthenticatedClient();
        privateContainerURI = URIBuilder.newBuilder(URI.create(podUrl))
                .path(State.PRIVATE_RESOURCE_PATH + "-access-test-" + UUID.randomUUID() + "/")
                .build();

        Utils.createContainer(authResourceOwnerClient, privateContainerURI);
        prepareAcpOfResource(authResourceOwnerClient, privateContainerURI, SolidRDFSource.class);

        sharedTextFileURI = URIBuilder.newBuilder(privateContainerURI)
                .path(sharedTextFileName)
                .build();
        //create test file in access grant enabled container
        try (final InputStream is = new ByteArrayInputStream(StandardCharsets.UTF_8.encode("Test text").array())) {
            final SolidNonRDFSource testResource = new SolidNonRDFSource(sharedTextFileURI, Utils.PLAIN_TEXT, is, null);
            assertDoesNotThrow(() -> authResourceOwnerClient.create(testResource));
            prepareAcpOfResource(authResourceOwnerClient, sharedTextFileURI, SolidNonRDFSource.class);
        }

        if (config.getOptionalValue("inrupt.test.access-grant.provider", String.class).isPresent()) {
            ACCESS_GRANT_PROVIDER = config.getOptionalValue("inrupt.test.access-grant.provider", String.class).get();
        } else {
            accessGrantServer = new MockAccessGrantServer(
                    URI.create(webidUrl),
                    URI.create(requesterWebidUrl),
                    sharedTextFileURI,
                    authServer.getMockServerUrl()
            );
            accessGrantServer.start();
            ACCESS_GRANT_PROVIDER = accessGrantServer.getMockServerUrl();
        }

        LOGGER.info("Integration Test Issuer: [{}]", issuer);
        LOGGER.info("Integration Test Pod Host: [{}]", podUrl);
        LOGGER.info("Integration Test Access Grant server: [{}]", ACCESS_GRANT_PROVIDER);
    }

    @AfterAll
    static void teardown() {
        //cleanup pod
        Utils.deleteContentsRecursively(authResourceOwnerClient, privateContainerURI);

        if (config.getOptionalValue("inrupt.test.webid", String.class).isEmpty()) {
            mockHttpServer.stop();
            identityProviderServer.stop();
            authServer.stop();
            webIdService.stop();
        }
        if (config.getOptionalValue("inrupt.test.access-grant.provider", String.class).isEmpty()) {
            accessGrantServer.stop();
        }
    }

    @ParameterizedTest
    @MethodSource("provideSessions")
    @DisplayName("https://w3id.org/inrupt/qa/manifest/solid-client-java/accessGrantLifecycle " +
            "Access Grant issuance lifecycle")
    void accessGrantIssuanceLifecycleTest(final Session resourceOwnerSession, final Session requesterSession) {
        //test is NOT run locally, AccessGrantServerMock needs to be aware of grant statuses.
        //We do not do this for now.
        assumeFalse(ACCESS_GRANT_PROVIDER.contains("localhost"));

        LOGGER.info("Integration Test - Access Grant issuance lifecycle");

        final AccessGrantClient requesterAccessGrantClient = new AccessGrantClient(
                URI.create(ACCESS_GRANT_PROVIDER)
        ).session(requesterSession);

        final Set<String> modes = new HashSet<>(Arrays.asList(GRANT_MODE_READ));
        final Instant expiration = Instant.parse(GRANT_EXPIRATION);
        final AccessRequest request = requesterAccessGrantClient.requestAccess(URI.create(requesterWebidUrl),
            new HashSet<>(Arrays.asList(sharedTextFileURI)), modes, PURPOSES, expiration)
            .toCompletableFuture().join();

        final AccessGrantClient resourceOwnerAccessGrantClient = new AccessGrantClient(
            URI.create(ACCESS_GRANT_PROVIDER)
        ).session(resourceOwnerSession);
        final AccessGrant grant = resourceOwnerAccessGrantClient.grantAccess(request)
            .toCompletableFuture().join();

        //2. call verify endpoint to verify grant
        final var grantVerification = resourceOwnerAccessGrantClient.verify(grant).toCompletableFuture().join();
        assertTrue(grantVerification.getChecks().size() > 0);
        assertEquals(grantVerification.getErrors().size(), 0);
        assertEquals(grantVerification.getWarnings().size(), 0);

        final AccessGrant grantFromVcProvider =
            resourceOwnerAccessGrantClient.fetch(grant.getIdentifier(), AccessGrant.class)
                .toCompletableFuture().join();
        assertEquals(grant.getPurposes(), grantFromVcProvider.getPurposes());

        //unauthorized request test
        final SolidSyncClient requesterClient = SolidSyncClient.getClientBuilder().build();
        final var err = assertThrows(UnauthorizedException.class,
                () -> requesterClient.read(sharedTextFileURI, SolidNonRDFSource.class));
        assertEquals(Utils.UNAUTHORIZED, err.getStatusCode());

        //authorized request test
        final Session accessSession = AccessGrantSession.ofAccessGrant(requesterSession, grant);
        final SolidSyncClient requesterAuthClient = requesterClient.session(accessSession);

        assertDoesNotThrow(() -> requesterAuthClient.read(sharedTextFileURI, SolidNonRDFSource.class));

        assertDoesNotThrow(resourceOwnerAccessGrantClient.revoke(grant).toCompletableFuture()::join);

        //6. call verify endpoint to check the grant is not valid
        final var revokedGrantVerification = resourceOwnerAccessGrantClient.verify(grant).toCompletableFuture().join();
        assertTrue(grantVerification.getChecks().size() > 0);
        assertEquals(revokedGrantVerification.getErrors().size(), 1);
        assertEquals(grantVerification.getWarnings().size(), 0);

        // Once revoked, the Access Grant should no longer grant access to the resource. The previously issued access
        // token may still be valid, so cache is cleared for the test.
        accessSession.reset();
        assertThrows(
                UnauthorizedException.class,
                () -> requesterAuthClient.read(sharedTextFileURI, SolidNonRDFSource.class)
        );
    }

    @ParameterizedTest
    @MethodSource("provideSessions")
    @DisplayName("https://w3id.org/inrupt/qa/manifest/solid-client-java/accessGrantOverride " +
            "Access Grant with request overrides")
    void accessGrantWithRequestOverridesTest(final Session resourceOwnerSession, final Session requesterSession) {
        LOGGER.info("Integration Test - Access Grant with request overrides");

        final AccessGrantClient requesterAccessGrantClient = new AccessGrantClient(
                URI.create(ACCESS_GRANT_PROVIDER)
        ).session(requesterSession);

        final Set<String> modes = new HashSet<>(Arrays.asList(GRANT_MODE_READ, GRANT_MODE_APPEND));
        final Instant expiration = Instant.now().plus(90, ChronoUnit.DAYS);
        final AccessRequest request = requesterAccessGrantClient.requestAccess(URI.create(requesterWebidUrl),
            new HashSet<>(Arrays.asList(sharedTextFileURI)), modes, PURPOSES, expiration)
            .toCompletableFuture().join();

        final AccessGrantClient resourceOwnerAccessGrantClient = new AccessGrantClient(
            URI.create(ACCESS_GRANT_PROVIDER)
        ).session(resourceOwnerSession);
        final AccessGrant grant = resourceOwnerAccessGrantClient.grantAccess(request)
            .toCompletableFuture().join();

        //2. call verify endpoint to verify grant
        final var grantVerification = resourceOwnerAccessGrantClient.verify(grant).toCompletableFuture().join();
        assertTrue(grantVerification.getChecks().size() > 0);
        assertEquals(grantVerification.getErrors().size(), 0);
        assertEquals(grantVerification.getWarnings().size(), 0);

    }

    //Query access grant related tests
    @ParameterizedTest
    @MethodSource("provideSessions")
    @DisplayName("https://w3id.org/inrupt/qa/manifest/solid-client-java/accessGrantQueryByRequestor " +
            "Lookup Access Grants by requester")
    void accessGrantQueryByRequesterTest(final Session resourceOwnerSession, final Session requesterSession) {
        LOGGER.info("Integration Test - Lookup Access Grants by requester");

        final AccessGrantClient requesterAccessGrantClient = new AccessGrantClient(
                URI.create(ACCESS_GRANT_PROVIDER)
        ).session(requesterSession);

        final Set<String> modes = new HashSet<>(Arrays.asList(GRANT_MODE_READ, GRANT_MODE_WRITE, GRANT_MODE_APPEND));
        final Instant expiration = Instant.parse(GRANT_EXPIRATION);
        final AccessRequest request = requesterAccessGrantClient.requestAccess(URI.create(requesterWebidUrl),
                        new HashSet<>(Arrays.asList(sharedTextFileURI)), modes, PURPOSES, expiration)
                .toCompletableFuture().join();

        final AccessGrantClient resourceOwnerAccessGrantClient = new AccessGrantClient(
                URI.create(ACCESS_GRANT_PROVIDER)
        ).session(resourceOwnerSession);
        final AccessGrant grant = resourceOwnerAccessGrantClient.grantAccess(request)
                .toCompletableFuture().join();

        final AccessCredentialQuery<AccessGrant> query = AccessCredentialQuery.newBuilder()
                .recipient(URI.create(requesterWebidUrl)).resource(sharedTextFileURI)
                .mode(GRANT_MODE_READ).mode(GRANT_MODE_WRITE).mode(GRANT_MODE_APPEND).build(AccessGrant.class);
        final List<AccessGrant> grants = resourceOwnerAccessGrantClient.query(query).toCompletableFuture().join();
        assertEquals(1, grants.size());

        final AccessCredentialQuery<AccessGrant> query2 = AccessCredentialQuery.newBuilder()
                .recipient(URI.create("https://someuser.test")).resource(sharedTextFileURI).build(AccessGrant.class);
        final List<AccessGrant> randomGrants =
            resourceOwnerAccessGrantClient.query(query2).toCompletableFuture().join();

        assertEquals(0, randomGrants.size());

        //cleanup
        assertDoesNotThrow(resourceOwnerAccessGrantClient.revoke(grant).toCompletableFuture()::join);
    }

    @ParameterizedTest
    @MethodSource("provideSessions")
    @DisplayName("Given a grant id, approve it")
    void accessGrantGrantAccessByIdTest(final Session resourceOwnerSession, final Session requesterSession) {
        LOGGER.info("Integration Test - Grant access by id");

        //setup test
        final AccessGrantClient requesterAccessGrantClient = new AccessGrantClient(
                URI.create(ACCESS_GRANT_PROVIDER)
        ).session(requesterSession);

        final Set<String> modes = new HashSet<>(Arrays.asList(GRANT_MODE_READ));
        final Instant expiration = Instant.parse(GRANT_EXPIRATION);
        final AccessRequest request = requesterAccessGrantClient.requestAccess(URI.create(webidUrl),
                        new HashSet<>(Arrays.asList(sharedTextFileURI)), modes, PURPOSES, expiration)
                .toCompletableFuture().join();

        final var grantId = request.getIdentifier();

        //requester has access to see their own access requests
        final AccessRequest fetchedAccessRequest =
                requesterAccessGrantClient.fetch(
                        grantId,
                        AccessRequest.class).toCompletableFuture().join();

        assertEquals(grantId, fetchedAccessRequest.getIdentifier());

        //switching to owner
        final AccessGrantClient resourceOwnerAccessGrantClient = new AccessGrantClient(
                URI.create(ACCESS_GRANT_PROVIDER)
        ).session(resourceOwnerSession);

        //the owner SHOULD see an access request from requester
        //because it is a request concerning the owner's resource
        final AccessRequest fetchedAccessRequest1 = resourceOwnerAccessGrantClient.fetch(
                        grantId,
                        AccessRequest.class).toCompletableFuture().join();
        assertEquals(grantId, fetchedAccessRequest1.getIdentifier());

        //owner grants the request
        final AccessGrant grant = resourceOwnerAccessGrantClient.grantAccess(request)
                .toCompletableFuture().join();

        //owner can see the access grants just granted
        final AccessGrant accessGrant =
                resourceOwnerAccessGrantClient.fetch(
                        grant.getIdentifier(),
                        AccessGrant.class).toCompletableFuture().join();

        //check that the approved grant is the same we find when querying for all grants
        final AccessCredentialQuery<AccessGrant> query = AccessCredentialQuery.newBuilder()
                .recipient(URI.create(requesterWebidUrl)).resource(sharedTextFileURI)
                .mode(GRANT_MODE_READ).build(AccessGrant.class);
        final List<AccessGrant> grants = resourceOwnerAccessGrantClient.query(query).toCompletableFuture().join();
        assertEquals(1, grants.size());
        assertEquals(grant.getIdentifier(), grants.get(0).getIdentifier());
        assertEquals(accessGrant.getIdentifier(), grants.get(0).getIdentifier());

        //cleanup
        assertDoesNotThrow(resourceOwnerAccessGrantClient.revoke(grant).toCompletableFuture()::join);
    }

    @ParameterizedTest
    @MethodSource("provideSessions")
    @DisplayName("https://w3id.org/inrupt/qa/manifest/solid-client-java/accessGrantQueryByPurpose " +
            "Lookup Access Grants by purpose of a dedicated resource")
    void accessGrantQueryByPurposeTest(final Session resourceOwnerSession, final Session requesterSession) {
        LOGGER.info("Integration Test - Lookup Access Grants by purpose");

        final AccessGrantClient requesterAccessGrantClient = new AccessGrantClient(
                URI.create(ACCESS_GRANT_PROVIDER)
        ).session(requesterSession);

        final Set<String> modes = new HashSet<>(Arrays.asList(GRANT_MODE_APPEND));
        final Instant expiration = Instant.parse(GRANT_EXPIRATION);
        final AccessRequest request = requesterAccessGrantClient.requestAccess(URI.create(requesterWebidUrl),
                        new HashSet<>(Arrays.asList(sharedTextFileURI)), modes, PURPOSES, expiration)
                .toCompletableFuture().join();

        final AccessGrantClient resourceOwnerAccessGrantClient = new AccessGrantClient(
                URI.create(ACCESS_GRANT_PROVIDER)
        ).session(resourceOwnerSession);
        final AccessGrant grant = resourceOwnerAccessGrantClient.grantAccess(request)
                .toCompletableFuture().join();

        //query for all grants with a dedicated purpose
        final AccessCredentialQuery<AccessGrant> query = AccessCredentialQuery.newBuilder()
                .resource(sharedTextFileURI).purpose(PURPOSE1).mode(GRANT_MODE_APPEND).build(AccessGrant.class);
        final List<AccessGrant> grants = resourceOwnerAccessGrantClient.query(query).toCompletableFuture().join();
        assertEquals(1, grants.size());

        //query for all grants of dedicated purpose combinations
        final AccessCredentialQuery<AccessGrant> query2 = AccessCredentialQuery.newBuilder()
                .resource(sharedTextFileURI).purpose(PURPOSE1).mode(GRANT_MODE_WRITE).build(AccessGrant.class);
        final List<AccessGrant> randomGrants =
            resourceOwnerAccessGrantClient.query(query2).toCompletableFuture().join();
        assertEquals(0, randomGrants.size()); //our grant is actually a APPEND

        //cleanup
        assertDoesNotThrow(resourceOwnerAccessGrantClient.revoke(grant).toCompletableFuture()::join);
    }

    //Interacting with resource related tests
    @ParameterizedTest
    @MethodSource("provideSessions")
    @DisplayName("https://w3id.org/inrupt/qa/manifest/solid-client-java/accessGrantGetRdf " +
            "Fetching RDF using Access Grant")
    void accessGrantGetRdfTest(final Session resourceOwnerSession, final Session requesterSession) {
        LOGGER.info("Integration Test - Fetching RDF using Access Grant");

        final SolidSyncClient resourceOwnerClient = SolidSyncClient.getClientBuilder()
            .build().session(resourceOwnerSession);

        final URI testRDFresourceURI = URIBuilder.newBuilder(privateContainerURI)
                .path("resource-accessGrantGetRdfTest.ttl")
                .build();

        try (final SolidRDFSource resource = new SolidRDFSource(testRDFresourceURI, null, null)) {
            assertDoesNotThrow(() -> resourceOwnerClient.create(resource));

            prepareAcpOfResource(resourceOwnerClient, testRDFresourceURI, SolidRDFSource.class);
        }

        final AccessGrantClient requesterAccessGrantClient = new AccessGrantClient(
                URI.create(ACCESS_GRANT_PROVIDER)
        ).session(requesterSession);

        final Set<String> modes = new HashSet<>(Arrays.asList(GRANT_MODE_READ, GRANT_MODE_WRITE, GRANT_MODE_APPEND));
        final Instant expiration = Instant.parse(GRANT_EXPIRATION);
        final AccessRequest request = requesterAccessGrantClient.requestAccess(URI.create(requesterWebidUrl),
            new HashSet<>(Arrays.asList(testRDFresourceURI)), modes, PURPOSES, expiration)
            .toCompletableFuture().join();

        final AccessGrantClient resourceOwnerAccessGrantClient = new AccessGrantClient(
            URI.create(ACCESS_GRANT_PROVIDER)
        ).session(resourceOwnerSession);
        final AccessGrant grant = resourceOwnerAccessGrantClient.grantAccess(request)
            .toCompletableFuture().join();
        final Session newSession = AccessGrantSession.ofAccessGrant(requesterSession, grant);
        final SolidSyncClient requesterClient = SolidSyncClient.getClient().session(newSession);

        try (final SolidRDFSource resource = requesterClient.read(testRDFresourceURI, SolidRDFSource.class)) {
            assertTrue(resource.getMetadata().getContentType().contains(Utils.TEXT_TURTLE));
        }

        //cleanup
        resourceOwnerClient.delete(testRDFresourceURI);
        assertDoesNotThrow(resourceOwnerAccessGrantClient.revoke(grant).toCompletableFuture()::join);
    }

    @ParameterizedTest
    @MethodSource("provideSessions")
    @DisplayName("https://w3id.org/inrupt/qa/manifest/solid-client-java/accessGrantSetRdf " +
            "Appending RDF using Access Grant")
    void accessGrantSetRdfTest(final Session resourceOwnerSession, final Session requesterSession) {
        LOGGER.info("Integration Test - Appending RDF using Access Grant");

        final SolidSyncClient resourceOwnerClient = SolidSyncClient.getClientBuilder()
            .build().session(resourceOwnerSession);

        final URI testRDFresourceURI = URIBuilder.newBuilder(privateContainerURI)
                .path("resource-accessGrantSetRdfTest.ttl")
                .build();

        try (final SolidRDFSource resource = new SolidRDFSource(testRDFresourceURI)) {
            assertDoesNotThrow(() -> resourceOwnerClient.create(resource));

            prepareAcpOfResource(resourceOwnerClient, testRDFresourceURI, SolidRDFSource.class);
        }

        final AccessGrantClient requesterAccessGrantClient = new AccessGrantClient(
                URI.create(ACCESS_GRANT_PROVIDER)
        ).session(requesterSession);

        final Set<String> modes = new HashSet<>(Arrays.asList(
            GRANT_MODE_READ, GRANT_MODE_WRITE, GRANT_MODE_APPEND));
        final Instant expiration = Instant.parse(GRANT_EXPIRATION);
        final AccessRequest request = requesterAccessGrantClient.requestAccess(URI.create(requesterWebidUrl),
            new HashSet<>(Arrays.asList(testRDFresourceURI)), modes, PURPOSES, expiration)
            .toCompletableFuture().join();

        final AccessGrantClient resourceOwnerAccessGrantClient = new AccessGrantClient(
            URI.create(ACCESS_GRANT_PROVIDER)
        ).session(resourceOwnerSession);
        final AccessGrant grant = resourceOwnerAccessGrantClient.grantAccess(request)
            .toCompletableFuture().join();

        final Session newSession = AccessGrantSession.ofAccessGrant(requesterSession, grant);
        final SolidSyncClient requesterAuthClient = SolidSyncClient.getClientBuilder()
            .build().session(newSession);

        final String newResourceName = testRDFresourceURI.toString();
        final String newPredicateName = "https://example.example/predicate";
        final IRI booleanType = rdf.createIRI("http://www.w3.org/2001/XMLSchema#boolean");

        final IRI newResourceNode = rdf.createIRI(newResourceName);
        final IRI newPredicateNode = rdf.createIRI(newPredicateName);
        final Literal object = rdf.createLiteral("true", booleanType);

        try (final SolidRDFSource resource = new SolidRDFSource(testRDFresourceURI)) {
            resource.add(rdf.createQuad(newResourceNode, newResourceNode, newPredicateNode, object));

            assertDoesNotThrow(() -> requesterAuthClient.update(resource));
        }

        //cleanup
        resourceOwnerClient.delete(testRDFresourceURI);
        assertDoesNotThrow(resourceOwnerAccessGrantClient.revoke(grant).toCompletableFuture()::join);
    }

    @ParameterizedTest
    @MethodSource("provideSessions")
    @DisplayName("https://w3id.org/inrupt/qa/manifest/solid-client-java/accessGrantCreateRdf " +
            "Creating RDF using Access Grant")
    void accessGrantCreateRdfTest(final Session resourceOwnerSession, final Session requesterSession) {
        LOGGER.info("Integration Test - Creating RDF using Access Grant");

        final URI newTestFileURI = URIBuilder.newBuilder(privateContainerURI)
                .path("newRdf-accessGrantCreateRdfTest.ttl")
                .build();

        final SolidSyncClient resourceOwnerClient = SolidSyncClient.getClientBuilder()
                .build().session(resourceOwnerSession);

        final AccessGrantClient requesterAccessGrantClient = new AccessGrantClient(
            URI.create(ACCESS_GRANT_PROVIDER)
        ).session(requesterSession);

        final Set<String> modes = new HashSet<>(Arrays.asList(GRANT_MODE_READ, GRANT_MODE_WRITE, GRANT_MODE_APPEND));
        final Instant expiration = Instant.parse(GRANT_EXPIRATION);
        final AccessRequest request = requesterAccessGrantClient.requestAccess(URI.create(requesterWebidUrl),
            new HashSet<>(Arrays.asList(newTestFileURI)), modes, PURPOSES, expiration)
            .toCompletableFuture().join();

        final AccessGrantClient resourceOwnerAccessGrantClient = new AccessGrantClient(
            URI.create(ACCESS_GRANT_PROVIDER)
        ).session(resourceOwnerSession);
        final AccessGrant grant = resourceOwnerAccessGrantClient.grantAccess(request)
            .toCompletableFuture().join();

        final Session newSession = AccessGrantSession.ofAccessGrant(requesterSession, grant);
        final SolidSyncClient authClient = SolidSyncClient.getClientBuilder()
            .build().session(newSession);

        try (final SolidRDFSource resource = new SolidRDFSource(newTestFileURI)) {
            assertDoesNotThrow(() -> authClient.create(resource));
        }

        resourceOwnerClient.delete(newTestFileURI);
        assertDoesNotThrow(resourceOwnerAccessGrantClient.revoke(grant).toCompletableFuture()::join);
    }

    @ParameterizedTest
    @MethodSource("provideSessions")
    @DisplayName("https://w3id.org/inrupt/qa/manifest/solid-client-java/accessGrantGetNonRdf " +
            "Fetching non-RDF using Access Grant")
    void accessGrantGetNonRdfTest(final Session resourceOwnerSession, final Session requesterSession)
            throws IOException {
        LOGGER.info("Integration Test - Fetching non-RDF using Access Grant");

        final SolidSyncClient resourceOwnerClient =
            SolidSyncClient.getClientBuilder().build().session(resourceOwnerSession);

        final URI newTestFileURI = URIBuilder.newBuilder(privateContainerURI)
            .path("newFile-accessGrantGetNonRdfTest.txt")
            .build();

        try (final InputStream is = new ByteArrayInputStream(
            StandardCharsets.UTF_8.encode("Test test test text").array())) {
            final SolidNonRDFSource testResource =
                new SolidNonRDFSource(newTestFileURI, Utils.PLAIN_TEXT, is, null);
            assertDoesNotThrow(() -> resourceOwnerClient.create(testResource));
        }

        final AccessGrantClient requesterAccessGrantClient = new AccessGrantClient(
                URI.create(ACCESS_GRANT_PROVIDER)
        ).session(requesterSession);

        final Set<String> modes = new HashSet<>(Arrays.asList(GRANT_MODE_READ, GRANT_MODE_WRITE, GRANT_MODE_APPEND));
        final Instant expiration = Instant.parse(GRANT_EXPIRATION);
        final AccessRequest request = requesterAccessGrantClient.requestAccess(URI.create(requesterWebidUrl),
            new HashSet<>(Arrays.asList(newTestFileURI)), modes, PURPOSES, expiration)
            .toCompletableFuture().join();

        final AccessGrantClient resourceOwnerAccessGrantClient = new AccessGrantClient(
            URI.create(ACCESS_GRANT_PROVIDER)
        ).session(resourceOwnerSession);
        final AccessGrant grant = resourceOwnerAccessGrantClient.grantAccess(request)
            .toCompletableFuture().join();
        final Session newSession = AccessGrantSession.ofAccessGrant(requesterSession, grant);
        final SolidSyncClient authClient = SolidSyncClient.getClientBuilder()
            .build().session(newSession);

        try (final SolidNonRDFSource resource = authClient.read(newTestFileURI, SolidNonRDFSource.class)) {
            assertTrue(resource.getMetadata().getContentType().contains(Utils.PLAIN_TEXT));
        }

        resourceOwnerClient.delete(newTestFileURI);
        assertDoesNotThrow(resourceOwnerAccessGrantClient.revoke(grant).toCompletableFuture()::join);
    }

    @ParameterizedTest
    @MethodSource("provideSessions")
    @DisplayName("https://w3id.org/inrupt/qa/manifest/solid-client-java/accessGrantSetNonRdf " +
            "Overwriting non-RDF using Access Grant")
    void accessGrantSetNonRdfTest(final Session resourceOwnerSession, final Session requesterSession)
            throws IOException {
        LOGGER.info("Integration Test - Overwriting non-RDF using Access Grant");

        final SolidSyncClient resourceOwnerClient =
            SolidSyncClient.getClientBuilder().build().session(resourceOwnerSession);

        final URI newTestFileURI = URIBuilder.newBuilder(privateContainerURI)
            .path("newFile-accessGrantSetNonRdfTest.txt")
            .build();

        try (final InputStream is = new ByteArrayInputStream(
            StandardCharsets.UTF_8.encode("Test test test text").array())) {
            final SolidNonRDFSource testResource =
                new SolidNonRDFSource(newTestFileURI, Utils.PLAIN_TEXT, is, null);
            assertDoesNotThrow(() -> resourceOwnerClient.create(testResource));
        }

        final AccessGrantClient requesterAccessGrantClient = new AccessGrantClient(
                URI.create(ACCESS_GRANT_PROVIDER)
        ).session(requesterSession);

        final Set<String> modes = new HashSet<>(Arrays.asList(GRANT_MODE_READ, GRANT_MODE_WRITE, GRANT_MODE_APPEND));
        final Instant expiration = Instant.parse(GRANT_EXPIRATION);
        final AccessRequest request = requesterAccessGrantClient.requestAccess(URI.create(requesterWebidUrl),
            new HashSet<>(Arrays.asList(newTestFileURI)), modes, PURPOSES, expiration)
            .toCompletableFuture().join();

        final AccessGrantClient resourceOwnerAccessGrantClient = new AccessGrantClient(
            URI.create(ACCESS_GRANT_PROVIDER)
        ).session(resourceOwnerSession);
        final AccessGrant grant = resourceOwnerAccessGrantClient.grantAccess(request)
            .toCompletableFuture().join();

        final Session newSession = AccessGrantSession.ofAccessGrant(requesterSession, grant);
        final SolidSyncClient requesterAuthClient = SolidSyncClient.getClientBuilder()
            .build().session(newSession);

        try (final SolidNonRDFSource resource = requesterAuthClient.read(newTestFileURI, SolidNonRDFSource.class)) {
            try (final InputStream newis = new ByteArrayInputStream(
                StandardCharsets.UTF_8.encode("Test text").array())) {
                final SolidNonRDFSource testResource =
                    new SolidNonRDFSource(newTestFileURI, Utils.PLAIN_TEXT, newis, resource.getMetadata());
                assertDoesNotThrow(() -> requesterAuthClient.update(testResource));
            }
        }

        try (final InputStream newis = new ByteArrayInputStream(
            StandardCharsets.UTF_8.encode("Test text").array())) {
            final Request reqCreate =
                Request.newBuilder(newTestFileURI).header(Utils.CONTENT_TYPE, Utils.PLAIN_TEXT)
                    .PUT(Request.BodyPublishers.ofInputStream(newis)).build();
            assertDoesNotThrow(() -> requesterAuthClient.send(reqCreate, Response.BodyHandlers.discarding()));
        }

        resourceOwnerClient.delete(newTestFileURI);
        assertDoesNotThrow(resourceOwnerAccessGrantClient.revoke(grant).toCompletableFuture()::join);
    }

    @ParameterizedTest
    @MethodSource("provideSessions")
    @DisplayName("https://w3id.org/inrupt/qa/manifest/solid-client-java/accessGrantCreateNonRdf " +
            "Creating non-RDF using Access Grant")
    void accessGrantCreateNonRdfTest(final Session resourceOwnerSession, final Session requesterSession)
            throws IOException {
        LOGGER.info("Integration Test - Creating non-RDF using Access Grant");

        final URI newTestFileURI = URIBuilder.newBuilder(privateContainerURI)
            .path("newFile-accessGrantCreateNonRdfTest.txt")
            .build();

        final SolidSyncClient resourceOwnerClient = SolidSyncClient.getClientBuilder()
                .build().session(resourceOwnerSession);

        final AccessGrantClient requesterAccessGrantClient = new AccessGrantClient(
                URI.create(ACCESS_GRANT_PROVIDER)
        ).session(requesterSession);

        final Set<String> modes = new HashSet<>(Arrays.asList(GRANT_MODE_READ, GRANT_MODE_WRITE, GRANT_MODE_APPEND));
        final Instant expiration = Instant.parse(GRANT_EXPIRATION);
        final AccessRequest request = requesterAccessGrantClient.requestAccess(URI.create(requesterWebidUrl),
            new HashSet<>(Arrays.asList(newTestFileURI)), modes, PURPOSES, expiration)
            .toCompletableFuture().join();

        final AccessGrantClient resourceOwnerAccessGrantClient = new AccessGrantClient(
            URI.create(ACCESS_GRANT_PROVIDER)
        ).session(resourceOwnerSession);
        final AccessGrant grant = resourceOwnerAccessGrantClient.grantAccess(request)
            .toCompletableFuture().join();

        final Session newSession = AccessGrantSession.ofAccessGrant(requesterSession, grant);
        final SolidSyncClient requesterAuthClient = SolidSyncClient.getClient().session(newSession);

        try (final InputStream is = new ByteArrayInputStream(
            StandardCharsets.UTF_8.encode("Test test test text").array())) {
            final SolidNonRDFSource testResource =
                new SolidNonRDFSource(newTestFileURI, Utils.PLAIN_TEXT, is, null);
            assertDoesNotThrow(() -> requesterAuthClient.create(testResource));
        }

        resourceOwnerClient.delete(newTestFileURI);
        assertDoesNotThrow(resourceOwnerAccessGrantClient.revoke(grant).toCompletableFuture()::join);
    }

    private static <T extends SolidResource> void prepareAcpOfResource(final SolidSyncClient authClient,
                                                                       final URI resourceURI,
                                                                       final Class<T> clazz) {
        SolidResource resource = null;
        try {
            if (SolidNonRDFSource.class.isAssignableFrom(clazz)) {
                resource = authClient.read(resourceURI, SolidNonRDFSource.class);
            } else if (SolidRDFSource.class.isAssignableFrom(clazz)) {
                resource = authClient.read(resourceURI, SolidRDFSource.class);
            }
            if (resource != null) {
                // find the acl Link in the header of the resource
                resource.getMetadata().getAcl().ifPresent(acl -> {
                    try (final SolidRDFSource acr = authClient.read(acl, SolidRDFSource.class)) {
                        AccessGrantUtils.accessControlPolicyTriples(acl, ACL.Read, ACL.Write)
                                .forEach(acr.getGraph()::add);
                        authClient.update(acr);
                    }
                });
                resource.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Stream<Arguments> provideSessions() throws SolidClientException {
        final var resourceOwnerSession = OpenIdSession.ofClientCredentials(
            URI.create(issuer),
            RESOURCE_OWNER_CLIENT_ID,
            RESOURCE_OWNER_CLIENT_SECRET,
            AUTH_METHOD);

        final var requesterSession = OpenIdSession.ofClientCredentials(
            URI.create(issuer),
            REQUESTER_CLIENT_ID,
            REQUESTER_CLIENT_SECRET,
            AUTH_METHOD);

        return Stream.of(
            Arguments.of(resourceOwnerSession, requesterSession)
            );
    }

    private static SolidSyncClient createAuthenticatedClient() {
        final Session session = OpenIdSession.ofClientCredentials(
                URI.create(issuer), //Client credentials
                RESOURCE_OWNER_CLIENT_ID,
                RESOURCE_OWNER_CLIENT_SECRET,
                AUTH_METHOD);

        return SolidSyncClient.getClient().session(session);
    }
}
