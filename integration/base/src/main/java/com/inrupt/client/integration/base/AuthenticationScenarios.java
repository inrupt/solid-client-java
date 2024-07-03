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

import com.inrupt.client.auth.Credential;
import com.inrupt.client.auth.Session;
import com.inrupt.client.openid.OpenIdException;
import com.inrupt.client.openid.OpenIdSession;
import com.inrupt.client.solid.*;
import com.inrupt.client.util.URIBuilder;
import com.inrupt.client.webid.WebIdProfile;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test class for authentication integration scenarios.
 */
public class AuthenticationScenarios {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationScenarios.class);

    private static MockSolidServer mockHttpServer;
    private static MockOpenIDProvider identityProviderServer;
    private static MockUMAAuthorizationServer authServer;
    private static MockWebIdService webIdService;
    private static String podUrl;
    private static String issuer;
    private static String webidUrl;
    private static final String MOCK_USERNAME = "someuser";

    private static String testResourceName = "resource.ttl";
    private static URI publicResourceURI;
    private static URI publicContainerURI;
    protected static URI privateResourceURI;
    private static URI privateContainerURI;
    private static final Config config = ConfigProvider.getConfig();

    private static final String CLIENT_ID = config.getValue("inrupt.test.client-id", String.class);
    private static final String CLIENT_SECRET = config.getValue("inrupt.test.client-secret", String.class);
    private static final String AUTH_METHOD = config
            .getOptionalValue("inrupt.test.auth-method", String.class)
            .orElse("client_secret_basic");
    private static SolidSyncClient localAuthClient;

    @BeforeAll
    static void setup() {
        LOGGER.info("Setup AuthenticationScenarios test");
        if (config.getOptionalValue("inrupt.test.webid", String.class).isPresent()) {
            LOGGER.info("Running AuthenticationScenarios on live server");
            webidUrl = config.getOptionalValue("inrupt.test.webid", String.class).get();
        } else {
            LOGGER.info("Running AuthenticationScenarios on Mock services");
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
            webidUrl = URIBuilder.newBuilder(URI.create(webIdService.getMockServerUrl()))
                    .path(MOCK_USERNAME)
                    .build()
                    .toString();
        }

        State.WEBID = URI.create(webidUrl);
        final SolidSyncClient client = SolidSyncClient.getClient();
        try (final WebIdProfile profile = client.read(URI.create(webidUrl), WebIdProfile.class)) {
            issuer = profile.getOidcIssuers().iterator().next().toString();
            podUrl = profile.getStorages().iterator().next().toString();
        }
        if (!podUrl.endsWith(Utils.FOLDER_SEPARATOR)) {
            podUrl += Utils.FOLDER_SEPARATOR;
        }

        final Session session = OpenIdSession.ofClientCredentials(
                URI.create(issuer), //Client credentials
                CLIENT_ID,
                CLIENT_SECRET,
                AUTH_METHOD);

        localAuthClient = SolidSyncClient.getClient().session(session);

        publicContainerURI = URIBuilder.newBuilder(URI.create(podUrl))
                .path("public-auth-test-" + UUID.randomUUID() + "/")
                .build();
        publicResourceURI = URIBuilder.newBuilder(publicContainerURI)
                .path(testResourceName)
                .build();

        Utils.createPublicContainer(localAuthClient, publicContainerURI);

        privateContainerURI = URIBuilder.newBuilder(URI.create(podUrl))
                .path(State.PRIVATE_RESOURCE_PATH + "-auth-test-" + UUID.randomUUID() + "/")
                .build();

        privateResourceURI = URIBuilder.newBuilder(privateContainerURI)
            .path(testResourceName)
            .build();

        Utils.createContainer(localAuthClient, privateContainerURI);

        LOGGER.info("Integration Test Issuer: [{}]", issuer);
        LOGGER.info("Integration Test Pod Host: [{}]", URI.create(podUrl).getHost());
    }

    @AfterAll
    static void teardown() {
        //cleanup pod
        Utils.deleteContentsRecursively(localAuthClient, publicContainerURI);
        Utils.deleteContentsRecursively(localAuthClient, privateContainerURI);

        if (config.getOptionalValue("inrupt.test.webid", String.class).isEmpty()) {
            mockHttpServer.stop();
            identityProviderServer.stop();
            authServer.stop();
            webIdService.stop();
        }
    }

    @Test
    @DisplayName("https://w3id.org/inrupt/qa/manifest/solid-client-java/unauthenticatedPublicNode " +
            "Unauthenticated fetch of public resource succeeds")
    void fetchPublicResourceUnauthenticatedTest() {
        LOGGER.info("Integration Test - Unauthenticated fetch of public resource");
        //create a public resource
        try (final SolidRDFSource testResource = new SolidRDFSource(publicResourceURI)) {
            final SolidSyncClient client = SolidSyncClient.getClient();
            assertDoesNotThrow(() -> client.create(testResource));
            assertDoesNotThrow(() -> client.read(publicResourceURI, SolidRDFSource.class));
            assertDoesNotThrow(() -> client.delete(testResource));
        }
    }

    @ParameterizedTest
    @MethodSource("provideSessions")
    @DisplayName("https://w3id.org/inrupt/qa/manifest/solid-client-java/unauthenticatedPrivateNode" +
            " Unauthenticated fetch of a private resource fails")
    void fetchPrivateResourceUnauthenticatedTest(final Session session) {
        LOGGER.info("Integration Test - Unauthenticated fetch of a private resource");
        //create private resource
        final SolidSyncClient authClient = SolidSyncClient.getClient().session(session);

        try (final SolidRDFSource testResource = new SolidRDFSource(privateResourceURI)) {
            assertDoesNotThrow(() -> authClient.create(testResource));

            final SolidSyncClient client = SolidSyncClient.getClient();
            final var err = assertThrows(UnauthorizedException.class,
                    () -> client.read(privateResourceURI, SolidRDFSource.class));
            assertEquals(Utils.UNAUTHORIZED, err.getStatusCode());
            Utils.checkProblemDetails(err);

            assertDoesNotThrow(() -> authClient.delete(testResource));
        }
    }

    @ParameterizedTest
    @MethodSource("provideSessions")
    @DisplayName("https://w3id.org/inrupt/qa/manifest/solid-client-java/authenticatedPublicNode " +
            "Authenticated fetch of public resource succeeds")
    void fetchPublicResourceAuthenticatedTest(final Session session) {
        LOGGER.info("Integration Test - Authenticated fetch of public resource");
        //create public resource
        final SolidSyncClient client = SolidSyncClient.getClient();
        try (final SolidRDFSource testResource = new SolidRDFSource(publicResourceURI)) {
            assertDoesNotThrow(() -> client.create(testResource));

            final SolidSyncClient authClient = SolidSyncClient.getClient().session(session);
            assertDoesNotThrow(() -> authClient.read(publicResourceURI, SolidRDFSource.class));

            assertDoesNotThrow(() -> client.delete(testResource));
        }
    }

    @ParameterizedTest
    @MethodSource("provideSessions")
    @DisplayName("https://w3id.org/inrupt/qa/manifest/solid-client-java/authenticatedPrivateNode " +
            "Authenticated fetch of private resource succeeds")
    void fetchPrivateResourceAuthenticatedTest(final Session session) {
        LOGGER.info("Integration Test - Authenticated fetch of private resource");
        //create private resource
        final SolidSyncClient authClient = SolidSyncClient.getClient().session(session);
        try (final SolidRDFSource testResource = new SolidRDFSource(privateResourceURI)) {
            assertDoesNotThrow(() -> authClient.create(testResource));

            assertDoesNotThrow(() -> authClient.read(privateResourceURI, SolidRDFSource.class));

            assertDoesNotThrow(() -> authClient.delete(testResource));
        }
    }

    @ParameterizedTest
    @MethodSource("provideSessions")
    @DisplayName("https://w3id.org/inrupt/qa/manifest/solid-client-java/authenticatedPrivateNodeAfterLogin " +
            "Unauthenticated, then auth fetch of private resource")
    void fetchPrivateResourceUnauthAuthTest(final Session session) {
        LOGGER.info("Integration Test - Unauthenticated, then auth fetch of private resource");
        //create private resource
        final SolidSyncClient authClient = SolidSyncClient.getClient().session(session);
        try (final SolidRDFSource testResource = new SolidRDFSource(privateResourceURI)) {
            assertDoesNotThrow(() -> authClient.create(testResource));

            final SolidSyncClient client = SolidSyncClient.getClient();
            final var err = assertThrows(UnauthorizedException.class,
                    () -> client.read(privateResourceURI, SolidRDFSource.class));
            assertEquals(Utils.UNAUTHORIZED, err.getStatusCode());
            Utils.checkProblemDetails(err);

            final SolidSyncClient authClient2 = client.session(session);
            assertDoesNotThrow(() -> authClient2.read(privateResourceURI, SolidRDFSource.class));

            assertDoesNotThrow(() -> authClient2.delete(testResource));
        }
    }

    @ParameterizedTest
    @MethodSource("provideSessions")
    @DisplayName("https://w3id.org/inrupt/qa/manifest/solid-client-java/authenticatedMultisessionNode " +
            "Multiple sessions authenticated in parallel")
    void multiSessionTest(final Session session) {
        LOGGER.info("Integration Test - Multiple sessions authenticated in parallel");
        //create private resource
        try (final SolidRDFSource testResource = new SolidRDFSource(privateResourceURI)) {
            final SolidSyncClient authClient1 = SolidSyncClient.getClient().session(session);
            assertDoesNotThrow(() -> authClient1.create(testResource));

            //create another private resource with another client
            final URI privateResourceURL2 = URIBuilder.newBuilder(privateContainerURI)
                .path("resource2.ttl")
                .build();
            try (final SolidRDFSource testResource2 = new SolidRDFSource(privateResourceURL2)) {
                final SolidSyncClient authClient2 =
                        SolidSyncClient.getClient().session(session);
                assertDoesNotThrow(() -> authClient2.create(testResource2));

                //read the other resource created with the other client
                assertDoesNotThrow(() -> authClient1.read(privateResourceURL2, SolidRDFSource.class));
                assertDoesNotThrow(() -> authClient2.read(privateResourceURI, SolidRDFSource.class));

                final SolidSyncClient client = SolidSyncClient.getClient();
                final var err = assertThrows(UnauthorizedException.class,
                        () -> client.read(privateResourceURI, SolidRDFSource.class));
                assertEquals(Utils.UNAUTHORIZED, err.getStatusCode());
                Utils.checkProblemDetails(err);

                //delete both resources with whichever client
                assertDoesNotThrow(() -> authClient1.delete(testResource2));
            }
            assertDoesNotThrow(() -> authClient1.delete(testResource));
        }
    }

    private static Stream<Arguments> provideSessions() throws SolidClientException {
        final Session session = OpenIdSession.ofClientCredentials(
            URI.create(issuer), //Client credentials
            CLIENT_ID,
            CLIENT_SECRET,
            AUTH_METHOD);
        final Optional<Credential> credential = session.getCredential(OpenIdSession.ID_TOKEN, null);
        final var token = credential.map(Credential::getToken)
            .orElseThrow(() -> new OpenIdException("We could not get a token"));
        return Stream.of(
            Arguments.of(OpenIdSession.ofIdToken(token), //OpenId token
            Arguments.of(session)));
    }
}
