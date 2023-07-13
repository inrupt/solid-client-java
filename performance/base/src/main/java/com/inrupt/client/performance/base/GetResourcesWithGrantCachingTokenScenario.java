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
package com.inrupt.client.performance.base;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.inrupt.client.accessgrant.*;
import com.inrupt.client.auth.Session;
import com.inrupt.client.openid.OpenIdSession;
import com.inrupt.client.solid.*;
import com.inrupt.client.util.URIBuilder;
import com.inrupt.client.vocabulary.ACL;
import com.inrupt.client.webid.WebIdProfile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

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

public class GetResourcesWithGrantCachingTokenScenario {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetResourcesWithGrantCachingTokenScenario.class);
    private static final int NUMBER_OF_RESOURCES = 2000;
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
    private static final URI PURPOSE1 = URI.create("https://purpose.example/212efdf4-e1a4-4dcd-9d3b-d6eb92e0205f");
    private static final URI PURPOSE2 = URI.create("https://purpose.example/de605b08-76c7-4f04-9cec-a438810b0c03");
    protected static final Set<URI> PURPOSES = new HashSet<>(Arrays.asList(PURPOSE1, PURPOSE2));
    protected static final String GRANT_EXPIRATION = "2024-04-03T12:00:00Z";
    private static String sharedTextFileName = "testResource";
    protected static URI sharedTextFileURI;
    private static URI privateContainerURI;
    private static Session requesterSession;
    private static Session resourceOwnerSession;

    private static SolidSyncClient authResourceOwnerClient;

    @BeforeAll
    static void setup() {
        authServer = new MockUMAAuthorizationServer();
        authServer.start();

        mockHttpServer = new MockSolidServer();
        mockHttpServer.start();

        identityProviderServer = new MockOpenIDProvider(MOCK_RESOURCE_OWNER_USERNAME);
        identityProviderServer.start();

        webIdService = new MockWebIdService(
                mockHttpServer.getMockServerUrl(),
                identityProviderServer.getMockServerUrl(),
                MOCK_RESOURCE_OWNER_USERNAME);
        webIdService.start();

        webidUrl = config
                .getOptionalValue("inrupt.test.webid", String.class)
                .orElse(URIBuilder.newBuilder(URI.create(webIdService.getMockServerUrl()))
                        .path(MOCK_RESOURCE_OWNER_USERNAME)
                        .build()
                        .toString());

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
                .path(State.PRIVATE_RESOURCE_PATH + "-performance-test-" + UUID.randomUUID() + "/")
                .build();

        Utils.createContainer(authResourceOwnerClient, privateContainerURI);
        prepareAcpOfResource(authResourceOwnerClient, privateContainerURI, SolidRDFSource.class);

        requesterWebidUrl = config
                .getOptionalValue("inrupt.test.requester.webid", String.class)
                .orElse(URIBuilder.newBuilder(URI.create(webIdService.getMockServerUrl()))
                        .path(MOCK_REQUESTER_USERNAME)
                        .build()
                        .toString());

        sharedTextFileURI = URIBuilder.newBuilder(privateContainerURI)
                .path(sharedTextFileName)
                .build();

        accessGrantServer = new MockAccessGrantServer(
                URI.create(webidUrl),
                URI.create(requesterWebidUrl),
                sharedTextFileURI,
                authServer.getMockServerUrl()
        );
        accessGrantServer.start();

        ACCESS_GRANT_PROVIDER = config
                .getOptionalValue("inrupt.test.access-grant.provider", String.class)
                .orElse(accessGrantServer.getMockServerUrl());

        LOGGER.info("Performance Test Issuer: [{}]", issuer);
        LOGGER.info("Performance Test Pod Host: [{}]", podUrl);
        LOGGER.info("Performance Test Access Grant server: [{}]", ACCESS_GRANT_PROVIDER);
    }

    @AfterAll
    static void teardown() {
        //cleanup pod
        Utils.deleteContentsRecursively(authResourceOwnerClient, privateContainerURI);

        mockHttpServer.stop();
        identityProviderServer.stop();
        authServer.stop();
        webIdService.stop();
        accessGrantServer.stop();
    }

    @ParameterizedTest
    @MethodSource("provideSessions")
    @DisplayName("Measure GET time of resources with grant access and cached token")
    void measureGetOfResources(final Session resourceOwnerSession, final Session requesterSession) {

        LOGGER.info("Performance Test - Measure GET time of resources with grant access with cached token (after 2nd run)");

        createResources(authResourceOwnerClient, privateContainerURI);

        final AccessGrantClient requesterAccessGrantClient = new AccessGrantClient(
                URI.create(ACCESS_GRANT_PROVIDER)
        ).session(requesterSession);

        final AccessGrantClient resourceOwnerAccessGrantClient = new AccessGrantClient(
                URI.create(ACCESS_GRANT_PROVIDER)
        ).session(resourceOwnerSession);

        final Set<String> modes = new HashSet<>(Arrays.asList(GRANT_MODE_READ));
        final Instant expiration = Instant.parse(GRANT_EXPIRATION);
        final AccessRequest request = requesterAccessGrantClient.requestAccess(privateContainerURI,
                        new HashSet<>(Arrays.asList(privateContainerURI)), modes, PURPOSES, expiration)
                .toCompletableFuture().join();
        final AccessGrant grant = resourceOwnerAccessGrantClient.grantAccess(request)
                .toCompletableFuture().join();

        final Session accessSession = AccessGrantSession.ofAccessGrant(requesterSession, grant);
        final SolidSyncClient requesterAuthClient =
                SolidSyncClient.getClientBuilder().build().session(accessSession);

        LOGGER.info("Time of performance test, using cached token");
        for (int j = 0; j < 9; j++) {
            final long[] result = new long[NUMBER_OF_RESOURCES];
            for (int i = 0; i < NUMBER_OF_RESOURCES; i++) {

                //authorized request test
                final URI resourceURI = URIBuilder.newBuilder(privateContainerURI)
                        .path(sharedTextFileName + i)
                        .build();

                final long start = System.nanoTime();
                requesterAuthClient.read(resourceURI, SolidNonRDFSource.class);
                final long end = System.nanoTime();
                result[i] = end - start;
            }
            LOGGER.info("Average time of all {} GETs : {} in nanosec", NUMBER_OF_RESOURCES,
                    Arrays.stream(result).average().getAsDouble());
        }

        for (int i = 0; i < NUMBER_OF_RESOURCES; i++) {
            assertDoesNotThrow(resourceOwnerAccessGrantClient.revoke(grant).toCompletableFuture()::join);
        }
    }

    private static void createResources(final SolidSyncClient client, final URI privateContainerURI) {
        for (int i = 0; i < NUMBER_OF_RESOURCES; i++) {
            final URI resourceURI = URIBuilder.newBuilder(privateContainerURI)
                    .path(sharedTextFileName + i)
                    .build();
            //create test file in access grant enabled container
            try (final InputStream is =
                         new ByteArrayInputStream(StandardCharsets.UTF_8.encode("Test text").array())) {
                final SolidNonRDFSource testResource =
                        new SolidNonRDFSource(resourceURI, Utils.PLAIN_TEXT, is, null);
                assertDoesNotThrow(() -> client.create(testResource));
            } catch (IOException e) {
                LOGGER.warn("Could not create performance test resource " + resourceURI);
            }
        }
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
        resourceOwnerSession = OpenIdSession.ofClientCredentials(
                URI.create(issuer),
                RESOURCE_OWNER_CLIENT_ID,
                RESOURCE_OWNER_CLIENT_SECRET,
                AUTH_METHOD);

        requesterSession = OpenIdSession.ofClientCredentials(
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
