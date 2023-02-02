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

import static org.apache.jena.rdf.model.ResourceFactory.createProperty;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.inrupt.client.Request;
import com.inrupt.client.auth.Session;
import com.inrupt.client.jena.JenaBodyHandlers;
import com.inrupt.client.openid.OpenIdSession;
import com.inrupt.client.solid.SolidClientException;
import com.inrupt.client.solid.SolidResource;
import com.inrupt.client.solid.SolidSyncClient;
import com.inrupt.client.vocabulary.PIM;

import java.net.URI;
import java.util.Optional;
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

public class OpenIdTokenAndClientCredentialAuthTest {

    private static final MockSolidServer mockHttpServer = new MockSolidServer();
    private static final Config config = ConfigProvider.getConfig();

    private static final String testEnv = config.getValue("inrupt.test.environment", String.class);
    private static final Optional<String> iss = config.getOptionalValue("inrupt.test.idp", String.class);
    private static final Optional<String> clientId = config.getOptionalValue("inrupt.test.clientId", String.class);
    private static final Optional<String> clientSecret = config.getOptionalValue("inrupt.test.clientSecret",
            String.class);
    private static final Optional<String> authMethod = config.getOptionalValue("inrupt.test.authMethod", String.class);
    private static String testResourceName = "resource.ttl";
    private static URI publicResourceURL;
    private static URI privateResourceURL;

    @BeforeAll
    static void setup() {

        if (testEnv.contains("MockSolidServer")) {
            mockHttpServer.start();
            Utils.POD_URL = mockHttpServer.getMockServerUrl();
            Utils.AS_URI = Utils.POD_URL + "/uma";
            Utils.WEBID = URI.create(Utils.POD_URL + "/" + Utils.USERNAME);
        }

        final SolidSyncClient session =
                SolidSyncClient.getClient().session(OpenIdSession.ofIdToken(Utils.setupIdToken()));
        final Request requestRdf = Request.newBuilder(Utils.WEBID).GET().build();
        final var responseRdf = session.send(requestRdf, JenaBodyHandlers.ofModel());
        final var storages = responseRdf.body()
                .listSubjectsWithProperty(createProperty(PIM.storage.toString())).toList();

        if (!storages.isEmpty()) {
            Utils.POD_URL = storages.get(0).toString();
        }
        publicResourceURL = URI.create(Utils.POD_URL + "/" + testResourceName);
        privateResourceURL =
                URI.create(Utils.POD_URL + "/" + Utils.PRIVATE_RESOURCE_PATH + "/" + testResourceName);
    }
    @AfterAll
    static void teardown() {
        if (testEnv.equals("MockSolidServer")) {
            mockHttpServer.stop();
        }
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
                .create(Utils.POD_URL + "/" + Utils.PRIVATE_RESOURCE_PATH + "/" + "resource2.ttl");
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
        final Stream.Builder builder = Stream.builder();

        builder.accept(OpenIdSession.ofIdToken(Utils.setupIdToken()));
        iss.ifPresent(issuer ->
            clientId.ifPresent(id ->
                clientSecret.ifPresent(secret ->
                    authMethod.ifPresent(method ->
                        builder.accept(OpenIdSession.ofClientCredentials(URI.create(issuer),
                                id, secret, method))))));
        return builder.build();
    }
}
