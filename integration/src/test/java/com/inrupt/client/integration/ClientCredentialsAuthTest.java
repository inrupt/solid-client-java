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
import com.inrupt.client.jena.JenaBodyHandlers;
import com.inrupt.client.openid.OpenIdSession;
import com.inrupt.client.solid.SolidClientException;
import com.inrupt.client.solid.SolidResource;
import com.inrupt.client.solid.SolidSyncClient;
import com.inrupt.client.vocabulary.PIM;

import java.net.URI;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ClientCredentialsAuthTest {

    private static final Config config = ConfigProvider.getConfig();

    private static final String testEnv = config.getValue("inrupt.test.environment", String.class);
    private static final String iss = config.getValue("inrupt.test.idp", String.class);
    private static final String clientId = config.getValue("inrupt.test.clientId", String.class);
    private static final String clientSecrete = config.getValue("inrupt.test.clientSecret", String.class);
    private static final String authMethod = config.getValue("inrupt.test.authMethod", String.class);

    private static URI publicResourceURL;
    private static URI privateResourceURL;

    private static String testResourceName = "resource.ttl";

    @BeforeAll
    static void setup() {

        if (testEnv.contains("MockSolidServer")) {
            Utils.initMockServer();
        }

        final var session = OpenIdSession.ofClientCredentials(URI.create(iss), clientId,
                clientSecrete, authMethod);
        final SolidSyncClient authClient = SolidSyncClient.getClient().session(session);

        final Request requestRdf = Request.newBuilder(Utils.WEBID).GET().build();
        final var responseRdf = authClient.send(requestRdf, JenaBodyHandlers.ofModel());
        final var storages = responseRdf.body()
                .listSubjectsWithProperty(createProperty(PIM.storage.toString())).toList();

        if (!storages.isEmpty()) {
            Utils.POD_URL = storages.get(0).toString();
        }

        publicResourceURL = URI.create(Utils.POD_URL + "/" + testResourceName);
        privateResourceURL =
                URI.create(Utils.POD_URL+ "/" + Utils.PRIVATE_RESOURCE_PATH + "/" + testResourceName);
    }
    
    @AfterAll
    static void teardown() {
        if (testEnv.equals("MockSolidServer")) {
            Utils.stopMockServer();
        }
    }

    @Test
    @DisplayName(":unauthenticatedPrivateNode Unauth fetch of a private resource fails")
    void fetchPrivateResourceUnauthenticatedTest() {
        //create private resource
        final var session = OpenIdSession.ofClientCredentials(URI.create(iss), clientId, clientSecrete, authMethod);
        final SolidSyncClient authClient = SolidSyncClient.getClient().session(session);

        final SolidResource testResource = new SolidResource(privateResourceURL, null, null);
        assertDoesNotThrow(() -> authClient.create(testResource));
        
        final SolidSyncClient client = SolidSyncClient.getClient();
        final SolidClientException err = assertThrows(SolidClientException.class,
                () -> client.read(privateResourceURL, SolidResource.class));
        assertEquals(Utils.UNAUTHORIZED, err.getStatusCode());

        assertDoesNotThrow(() -> authClient.delete(testResource));
    }

    @Test
    @Disabled
    @DisplayName(":unauthenticatedPrivateNodeAfterLogout Unauth fetch of a private resource fails")
    void fetchPrivateResourceAfterLogoutTest() {
        //create private resource
        final var session = OpenIdSession.ofClientCredentials(URI.create(iss), clientId, clientSecrete, authMethod);
        final SolidSyncClient authClient = SolidSyncClient.getClient().session(session);
        final SolidResource testResource = new SolidResource(privateResourceURL, null, null);
        assertDoesNotThrow(() -> authClient.create(testResource));

        //TODO end seesion
                
        final SolidClientException err = assertThrows(SolidClientException.class,
                () -> authClient.read(privateResourceURL, SolidResource.class));
        assertEquals(Utils.UNAUTHORIZED, err.getStatusCode());

        //TODO add session
        authClient.session(OpenIdSession.ofIdToken(Utils.setupIdToken()));
        assertDoesNotThrow(() -> authClient.delete(testResource));
    }
    
    @Test
    @DisplayName(":authenticatedPublicNode Auth fetch of public resource succeeds")
    void fetchPublicResourceAuthenticatedTest() {
        final SolidSyncClient client = SolidSyncClient.getClient();
        final SolidResource testResource = new SolidResource(publicResourceURL, null, null);
        assertDoesNotThrow(() -> client.create(testResource));

        final var session = OpenIdSession.ofClientCredentials(URI.create(iss), clientId, clientSecrete, authMethod);
        final SolidSyncClient authClient = SolidSyncClient.getClient().session(session);
        assertDoesNotThrow(() -> authClient.read(publicResourceURL, SolidResource.class));

        assertDoesNotThrow(() -> client.delete(testResource));
    }
    
    @Test
    @DisplayName(":authenticatedPrivateNode Auth fetch of private resource succeeds")
    void fetchPrivateResourceAuthenticatedTest() {
        //create private resource
        final var session = OpenIdSession.ofClientCredentials(URI.create(iss), clientId, clientSecrete, authMethod);
        final SolidSyncClient authClient = SolidSyncClient.getClient().session(session);
        final SolidResource testResource = new SolidResource(privateResourceURL, null, null);
        assertDoesNotThrow(() -> authClient.create(testResource));

        assertDoesNotThrow(() -> authClient.read(privateResourceURL, SolidResource.class));

        assertDoesNotThrow(() -> authClient.delete(testResource));
    }
    
    @Test
    @Disabled
    @DisplayName(":authenticatedPrivateNodeAfterLogin Unauth, then auth fetch of private resource")
    void fetchPrivateResourceUnauthAuthTest() {
        //create private resource
        final var session = OpenIdSession.ofClientCredentials(URI.create(iss), clientId, clientSecrete, authMethod);
        final SolidSyncClient authClient = SolidSyncClient.getClient().session(session);
        final SolidResource testResource = new SolidResource(privateResourceURL, null, null);
        assertDoesNotThrow(() -> authClient.create(testResource));

        final SolidSyncClient client = SolidSyncClient.getClient();
        final SolidClientException err = assertThrows(SolidClientException.class,
                () -> client.read(privateResourceURL, SolidResource.class));
        assertEquals(Utils.UNAUTHORIZED, err.getStatusCode());

        //TODO a more flexible way to add session
        client.session(OpenIdSession.ofIdToken(Utils.setupIdToken()));
        assertDoesNotThrow(() -> client.read(privateResourceURL, SolidResource.class));

        assertDoesNotThrow(() -> client.delete(testResource));
    }
    
    @Test
    @DisplayName(":authenticatedMultisessionNode Multiple sessions authenticated in parallel")
    void multiSessionTest() {
        //create private resource
        final SolidResource testResource = new SolidResource(privateResourceURL, null, null);
        final var session = OpenIdSession.ofClientCredentials(URI.create(iss), clientId, clientSecrete, authMethod);
        final SolidSyncClient authClient1 = SolidSyncClient.getClient().session(session);
        assertDoesNotThrow(() -> authClient1.create(testResource));

        //create another private resource with another client
        final URI privateResourceURL2 = URI.create(Utils.POD_URL + "/" + Utils.PRIVATE_RESOURCE_PATH + "/" + "resource2.ttl");
        final SolidResource testResource2 = new SolidResource(privateResourceURL2, null, null);
        final SolidSyncClient authClient2 = SolidSyncClient.getClient().session(session);
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
}
