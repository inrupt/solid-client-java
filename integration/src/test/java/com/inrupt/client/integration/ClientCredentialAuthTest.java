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
package com.inrupt.client.integration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.inrupt.client.auth.Session;
import com.inrupt.client.openid.OpenIdSession;
import com.inrupt.client.solid.SolidClientException;
import com.inrupt.client.solid.SolidResource;
import com.inrupt.client.solid.SolidSyncClient;
import com.inrupt.client.webid.WebIdProfile;

import java.net.URI;
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

public class ClientCredentialAuthTest {

    private static MockSolidServer mockHttpServer;
    private static MockOpenIDProvider identityProviderServer;
    private static MockUMAAuthorizationServer authServer;
    private static MockWebIdSevice webIdService;

    private static final Config config = ConfigProvider.getConfig();

    private static String issuer;
    private static String podUrl;
    private static final String MOCK_USERNAME = "someuser";
    private static final String PRIVATE_RESOURCE_PATH = config
        .getOptionalValue("inrupt.test.private-resource-path", String.class)
        .orElse("private");
    private static final String CLIENT_ID = config.getValue("inrupt.test.client-id", String.class);
    private static final String CLIENT_SECRET = config.getValue("inrupt.test.client-secret", String.class);
    private static final String AUTH_METHOD = config
        .getOptionalValue("inrupt.test.auth-method", String.class)
        .orElse("client_secret_basic");

    private static String testResourceName = "resource.ttl";
    private static URI publicResourceURL;
    private static URI privateResourceURL;

    @BeforeAll
    static void setup() {
        authServer = new MockUMAAuthorizationServer();
        authServer.start();

        mockHttpServer = new MockSolidServer(authServer.getMockServerUrl());
        mockHttpServer.start();

        identityProviderServer = new MockOpenIDProvider(MOCK_USERNAME);
        identityProviderServer.start();

        webIdService = new MockWebIdSevice(
            mockHttpServer.getMockServerUrl(),
            identityProviderServer.getMockServerUrl(),
            MOCK_USERNAME);
        webIdService.start();

        State.PRIVATE_RESOURCE_PATH = PRIVATE_RESOURCE_PATH;

        final String webidUrl = config
            .getOptionalValue("inrupt.test.webid", String.class)
            .orElse(webIdService.getMockServerUrl() + "/" + MOCK_USERNAME);

        State.WEBID = URI.create(webidUrl);
        final SolidSyncClient client = SolidSyncClient.getClient();
        try (final WebIdProfile profile = client.read(URI.create(webidUrl), WebIdProfile.class)) {
            issuer = profile.getOidcIssuer().iterator().next().toString();
            podUrl = profile.getStorage().iterator().next().toString();
        }

        publicResourceURL = URI.create(podUrl + "/" + testResourceName);
        privateResourceURL =
                URI.create(podUrl + "/" + PRIVATE_RESOURCE_PATH + "/" + testResourceName);
    }

    @AfterAll
    static void teardown() {
        mockHttpServer.stop();
        identityProviderServer.stop();
        authServer.stop();
        webIdService.stop();
    }

    @Test
    @DisplayName(":unauthenticatedPublicNode Unauth fetch of public resource succeeds")
    void fetchPublicResourceUnauthenticatedTest() {
        //create a public resource
        final SolidResource testResource = new SolidResource(publicResourceURL, null, null);
        final SolidSyncClient client = SolidSyncClient.getClient();
        assertDoesNotThrow(() -> client.create(testResource));
        assertDoesNotThrow(() -> client.read(publicResourceURL, SolidResource.class));
        assertDoesNotThrow(() -> client.delete(testResource));
    }

    @ParameterizedTest
    @MethodSource("provideSessions")
    @DisplayName(":unauthenticatedPrivateNode Unauth fetch of a private resource fails")
    void fetchPrivateResourceUnauthenticatedTest(final Session session) {
        //create private resource
        final SolidSyncClient authClient = SolidSyncClient.getClient().session(session);

        final SolidResource testResource = new SolidResource(privateResourceURL, null, null);
        assertDoesNotThrow(() -> authClient.create(testResource));

        final SolidSyncClient client = SolidSyncClient.getClient();
        final SolidClientException err = assertThrows(SolidClientException.class,
                () -> client.read(privateResourceURL, SolidResource.class));
        assertEquals(Utils.UNAUTHORIZED, err.getStatusCode());

        assertDoesNotThrow(() -> authClient.delete(testResource));
    }

    @ParameterizedTest
    @MethodSource("provideSessions")
    @DisplayName(":unauthenticatedPrivateNodeAfterLogout Unauth fetch of a private resource fails")
    void fetchPrivateResourceAfterLogoutTest(final Session session) {
        //create private resource
        final SolidSyncClient authClient = SolidSyncClient.getClient().session(session);
        final SolidResource testResource = new SolidResource(privateResourceURL, null, null);
        assertDoesNotThrow(() -> authClient.create(testResource));

        final SolidSyncClient unauthClient = SolidSyncClient.getClient();
        final SolidClientException err = assertThrows(SolidClientException.class,
                () -> unauthClient.read(privateResourceURL, SolidResource.class));
        assertEquals(Utils.UNAUTHORIZED, err.getStatusCode());

        assertDoesNotThrow(() -> authClient.delete(testResource));
    }

    @ParameterizedTest
    @MethodSource("provideSessions")
    @DisplayName(":authenticatedPublicNode Auth fetch of public resource succeeds")
    void fetchPublicResourceAuthenticatedTest(final Session session) {
        final SolidSyncClient client = SolidSyncClient.getClient();
        final SolidResource testResource = new SolidResource(publicResourceURL, null, null);
        assertDoesNotThrow(() -> client.create(testResource));

        final SolidSyncClient authClient = SolidSyncClient.getClient().session(session);
        assertDoesNotThrow(() -> authClient.read(publicResourceURL, SolidResource.class));

        assertDoesNotThrow(() -> client.delete(testResource));
    }

    @ParameterizedTest
    @MethodSource("provideSessions")
    @DisplayName(":authenticatedPrivateNode Auth fetch of private resource succeeds")
    void fetchPrivateResourceAuthenticatedTest(final Session session) {
        //create private resource
        final SolidSyncClient authClient = SolidSyncClient.getClient().session(session);
        final SolidResource testResource = new SolidResource(privateResourceURL, null, null);
        assertDoesNotThrow(() -> authClient.create(testResource));

        assertDoesNotThrow(() -> authClient.read(privateResourceURL, SolidResource.class));

        assertDoesNotThrow(() -> authClient.delete(testResource));
    }

    @ParameterizedTest
    @MethodSource("provideSessions")
    @DisplayName(":authenticatedPrivateNodeAfterLogin Unauth, then auth fetch of private resource")
    void fetchPrivateResourceUnauthAuthTest(final Session session) {
        //create private resource
        final SolidSyncClient authClient = SolidSyncClient.getClient().session(session);
        final SolidResource testResource = new SolidResource(privateResourceURL, null, null);
        assertDoesNotThrow(() -> authClient.create(testResource));

        final SolidSyncClient client = SolidSyncClient.getClient();
        final SolidClientException err = assertThrows(SolidClientException.class,
                () -> client.read(privateResourceURL, SolidResource.class));
        assertEquals(Utils.UNAUTHORIZED, err.getStatusCode());

        final SolidSyncClient authClient2 = client.session(session);
        assertDoesNotThrow(() -> authClient2.read(privateResourceURL, SolidResource.class));

        assertDoesNotThrow(() -> authClient2.delete(testResource));
    }

    @ParameterizedTest
    @MethodSource("provideSessions")
    @DisplayName(":authenticatedMultisessionNode Multiple sessions authenticated in parallel")
    void multiSessionTest(final Session session) {
        //create private resource
        final SolidResource testResource = new SolidResource(privateResourceURL, null, null);
        final SolidSyncClient authClient1 = SolidSyncClient.getClient().session(session);
        assertDoesNotThrow(() -> authClient1.create(testResource));

        //create another private resource with another client
        final URI privateResourceURL2 = URI
                .create(podUrl + "/" + State.PRIVATE_RESOURCE_PATH + "/" + "resource2.ttl");
        final SolidResource testResource2 = new SolidResource(privateResourceURL2, null, null);
        final SolidSyncClient authClient2 =
                SolidSyncClient.getClient().session(session);
        assertDoesNotThrow(() -> authClient2.create(testResource2));

        //read the other resource created with the other client
        assertDoesNotThrow(() -> authClient1.read(privateResourceURL2, SolidResource.class));
        assertDoesNotThrow(() -> authClient2.read(privateResourceURL, SolidResource.class));

        final SolidSyncClient client = SolidSyncClient.getClient();
        final SolidClientException err = assertThrows(SolidClientException.class,
                () -> client.read(privateResourceURL, SolidResource.class));
        assertEquals(Utils.UNAUTHORIZED, err.getStatusCode());

        //delete both resources with whichever client
        assertDoesNotThrow(() -> authClient1.delete(testResource2));
        assertDoesNotThrow(() -> authClient1.delete(testResource));
    }

    private static Stream<Arguments> provideSessions() {
        return Stream.of(
            Arguments.of(OpenIdSession.ofClientCredentials
                (URI.create(issuer), //Client credentials
                CLIENT_ID,
                CLIENT_SECRET,
                AUTH_METHOD)
            )
        );
    }
}
