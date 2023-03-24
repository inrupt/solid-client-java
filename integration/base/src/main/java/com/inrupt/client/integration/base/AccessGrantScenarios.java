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

import com.inrupt.client.Request;
import com.inrupt.client.Response;
import com.inrupt.client.auth.Session;
import com.inrupt.client.openid.OpenIdSession;
import com.inrupt.client.solid.SolidSyncClient;
import com.inrupt.client.util.URIBuilder;
import com.inrupt.client.webid.WebIdProfile;

import java.net.URI;
import java.util.UUID;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccessGrantScenarios {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessGrantScenarios.class);

    private static MockSolidServer mockHttpServer;
    private static MockOpenIDProvider identityProviderServer;
    private static MockUMAAuthorizationServer authServer;
    private static MockWebIdService webIdService;
    private static String podUrl;
    private static String issuer;
    private static String webidUrl;
    private static final String MOCK_USERNAME = "someuser";

    private static final Config config = ConfigProvider.getConfig();

    private static final String CLIENT_ID = config.getValue("inrupt.test.client-id", String.class);
    private static final String CLIENT_SECRET = config.getValue("inrupt.test.client-secret", String.class);
    private static final String AUTH_METHOD = config
        .getOptionalValue("inrupt.test.auth-method", String.class)
        .orElse("client_secret_basic");

    private static URI testContainerURI;
    private static String sharedFileName = "sharedFile.txt";
    private static URI sharedFileURI;
    private static Session session;

    @BeforeAll
    static void setup() {
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

        State.WEBID = URI.create(webidUrl);
        final SolidSyncClient client = SolidSyncClient.getClient();
        try (final WebIdProfile profile = client.read(URI.create(webidUrl), WebIdProfile.class)) {
            issuer = profile.getOidcIssuer().iterator().next().toString();
            podUrl = profile.getStorage().iterator().next().toString();
        }
        if (!podUrl.endsWith(Utils.FOLDER_SEPARATOR)) {
            podUrl += Utils.FOLDER_SEPARATOR;
        }

        testContainerURI = URIBuilder.newBuilder(URI.create(podUrl))
            .path("accessgrant-test-" + UUID.randomUUID())
            .build();

        sharedFileURI = URIBuilder.newBuilder(URI.create(testContainerURI.toString()))
            .path(sharedFileName)
            .build();

        session = OpenIdSession.ofClientCredentials(
            URI.create(issuer), //Client credentials
            CLIENT_ID,
            CLIENT_SECRET,
            AUTH_METHOD);

        //create test file in test container
        final Request reqCreate =
            Request.newBuilder(sharedFileURI).header(Utils.CONTENT_TYPE, Utils.PLAIN_TEXT)
                    .PUT(Request.BodyPublishers.noBody()).build();
        client.send(reqCreate, Response.BodyHandlers.discarding());

        LOGGER.info("Integration Test Issuer: [{}]", issuer);
        LOGGER.info("Integration Test Pod Host: [{}]", URI.create(podUrl).getHost());
    }
    @AfterAll
    static void teardown() {
         //cleanup pod
        final SolidSyncClient client = SolidSyncClient.getClient().session(session);
        client.send(Request.newBuilder(sharedFileURI).DELETE().build(), Response.BodyHandlers.discarding());
        client.send(Request.newBuilder(testContainerURI).DELETE().build(), Response.BodyHandlers.discarding());

        mockHttpServer.stop();
        identityProviderServer.stop();
        authServer.stop();
        webIdService.stop();
    }

    @Test
    @DisplayName(":accessGrantLifecycle Access Grant issuance lifecycle")
    void accessGrantIssuanceLifecycleTest() {
        LOGGER.info("Integration Test - Access Grant issuance lifecycle");
    }
}
