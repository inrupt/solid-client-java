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
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.inrupt.client.Request;
import com.inrupt.client.jena.JenaBodyHandlers;
import com.inrupt.client.openid.OpenIdSession;
import com.inrupt.client.solid.SolidResource;
import com.inrupt.client.solid.SolidSyncClient;
import com.inrupt.client.vocabulary.PIM;

import java.net.URI;
import java.util.concurrent.CompletionException;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ClientCredentialsAuthenticationTest {

    private static final Config config = ConfigProvider.getConfig();

    private static String testEnv = config.getValue("inrupt.test.environment", String.class);
    private static String podUrl = config.getValue("inrupt.test.storage", String.class);
    private static String username = config.getValue("inrupt.test.username", String.class);
    private static String iss = config.getValue("inrupt.test.idp", String.class);
    private static String clientId = config.getValue("inrupt.test.clientId", String.class);
    private static String clientSecrete = config.getValue("inrupt.test.clientSecret", String.class);
    private static String authMethod = config.getValue("inrupt.test.authMethod", String.class);

    private static String testResourceName = "resource.ttl";

    @BeforeAll
    static void setup() {
        
        if (testEnv.contains("MockSolidServer")) {
            Utils.initMockServer();
            podUrl = Utils.getMockServerUrl();
        }
        final var webid = URI.create(podUrl + "/" + username);

        final var session = OpenIdSession.ofClientCredentials(URI.create(iss), clientId, clientSecrete, authMethod);
        final SolidSyncClient authClient = SolidSyncClient.getClient().session(session);

        final Request requestRdf = Request.newBuilder(webid).GET().build();
        final var responseRdf = authClient.send(requestRdf, JenaBodyHandlers.ofModel());
        final var storages = responseRdf.body()
                .listSubjectsWithProperty(createProperty(PIM.storage.toString())).toList();

        if (!storages.isEmpty()) {
            podUrl = storages.get(0).toString();
        }
        if (!podUrl.endsWith("/")) {
            podUrl += "/";
        }
        testResourceName = podUrl + testResourceName;
    }


    @Test
    @DisplayName(":unauthenticatedPrivateNode Unauth fetch of a private resource fails")
    void fetchPrivateResourceUnauthenticatedTest() {
        //create private resource
        final var session = OpenIdSession.ofClientCredentials(URI.create(iss), clientId, clientSecrete, authMethod);
        final SolidSyncClient authClient = SolidSyncClient.getClient().session(session);

        final URI resourceURL = URI.create(testResourceName);
        final SolidResource testResource = new SolidResource(resourceURL, null, null);
        assertDoesNotThrow(() -> authClient.create(testResource));
        
        final SolidSyncClient client = SolidSyncClient.getClient();
        final CompletionException err = assertThrows(CompletionException.class,
                () -> client.read(resourceURL, SolidResource.class));
        //assertTrue(err.getCause() instanceof ...);
    }

    @Test
    @DisplayName(":unauthenticatedPrivateNodeAfterLogout Unauth fetch of a private resource fails")
    void fetchPrivateResourceAfterLogoutTest() {
        //create private resource
        final var session = OpenIdSession.ofClientCredentials(URI.create(iss), clientId, clientSecrete, authMethod);
        final SolidSyncClient authClient = SolidSyncClient.getClient().session(session);
        final URI resourceURL = URI.create(testResourceName);
        final SolidResource testResource = new SolidResource(resourceURL, null, null);
        assertDoesNotThrow(() -> authClient.create(testResource));

        //TODO end seesion
                
        final CompletionException err = assertThrows(CompletionException.class,
                () -> authClient.read(resourceURL, SolidResource.class));
        //assertTrue(err.getCause() instanceof ...);
    }
    
    @Test
    @DisplayName(":authenticatedPublicNode Auth fetch of public resource succeeds")
    void fetchPublicResourceAuthenticatedTest() {
        final SolidSyncClient client = SolidSyncClient.getClient();
        final URI resourceURL = URI.create(testResourceName);
        final SolidResource testResource = new SolidResource(resourceURL, null, null);
        assertDoesNotThrow(() -> client.create(testResource));

        final var session = OpenIdSession.ofClientCredentials(URI.create(iss), clientId, clientSecrete, authMethod);
        final SolidSyncClient authClient = SolidSyncClient.getClient().session(session);
        assertDoesNotThrow(() -> authClient.read(resourceURL, SolidResource.class));
    }
    
    @Test
    @DisplayName(":authenticatedPrivateNode Auth fetch of private resource succeeds")
    void fetchPrivateResourceAuthenticatedTest() {
        //create private resource
        final var session = OpenIdSession.ofClientCredentials(URI.create(iss), clientId, clientSecrete, authMethod);
        final SolidSyncClient authClient = SolidSyncClient.getClient().session(session);
        final URI resourceURL = URI.create(testResourceName);
        final SolidResource testResource = new SolidResource(resourceURL, null, null);
        assertDoesNotThrow(() -> authClient.create(testResource));

        assertDoesNotThrow(() -> authClient.read(resourceURL, SolidResource.class));
    }
    
    @Test
    @DisplayName(":authenticatedPrivateNodeAfterLogin Unauth, then auth fetch of private resource")
    void fetchPrivateResourceUnauthAuthTest() {
        //create private resource
        final var session = OpenIdSession.ofClientCredentials(URI.create(iss), clientId, clientSecrete, authMethod);
        final SolidSyncClient authClient = SolidSyncClient.getClient().session(session);
        final URI resourceURL = URI.create(testResourceName);
        final SolidResource testResource = new SolidResource(resourceURL, null, null);
        assertDoesNotThrow(() -> authClient.create(testResource));

        final SolidSyncClient client = SolidSyncClient.getClient();
        final CompletionException err = assertThrows(CompletionException.class,
                () -> client.read(resourceURL, SolidResource.class));
        //assertTrue(err.getCause() instanceof ...);

        client.session(session);
        assertDoesNotThrow(() -> authClient.read(resourceURL, SolidResource.class));
    }
    
    @Test
    @DisplayName(":authenticatedMultisessionNode Multiple sessions authenticated in parallel")
    void multiSessionTest() {
        //create private resource
        final var session = OpenIdSession.ofClientCredentials(URI.create(iss), clientId, clientSecrete, authMethod);
        final SolidSyncClient authClient1 = SolidSyncClient.getClient().session(session);
        final URI resourceURL = URI.create(testResourceName);
        final SolidResource testResource = new SolidResource(resourceURL, null, null);

        assertDoesNotThrow(() -> authClient1.create(testResource));

        //create private another resource
        final SolidSyncClient authClient2 = SolidSyncClient.getClient().session(session);
        final URI resourceURL2 = URI.create(podUrl + "resource2.ttl");
        final SolidResource testResource2 = new SolidResource(resourceURL2, null, null);

        assertDoesNotThrow(() -> authClient2.create(testResource2));

        final CompletionException err = assertThrows(CompletionException.class,
                () -> authClient1.read(resourceURL2, SolidResource.class));
        //assertTrue(err.getCause() instanceof ...);

        final CompletionException err2 = assertThrows(CompletionException.class,
                () -> authClient2.read(resourceURL, SolidResource.class));
        //assertTrue(err.getCause() instanceof ...);
    }
}
