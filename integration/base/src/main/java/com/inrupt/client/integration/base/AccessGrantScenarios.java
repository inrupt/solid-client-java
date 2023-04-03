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
import com.inrupt.client.Request;
import com.inrupt.client.Response;
import com.inrupt.client.accessgrant.AccessGrant;
import com.inrupt.client.accessgrant.AccessGrantClient;
import com.inrupt.client.auth.Credential;
import com.inrupt.client.auth.Session;
import com.inrupt.client.openid.OpenIdException;
import com.inrupt.client.openid.OpenIdSession;
import com.inrupt.client.solid.SolidClientException;
import com.inrupt.client.solid.SolidSyncClient;
import com.inrupt.client.util.URIBuilder;
import com.inrupt.client.webid.WebIdProfile;

import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
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
    private static final String VC_PROVIDER = config
        .getOptionalValue("inrupt.test.vc.provider", String.class)
        .orElse("https://vc.inrupt.com");

    private static final URI ACCESS_GRANT = URI.create("http://www.w3.org/ns/solid/vc#SolidAccessGrant");
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

    @ParameterizedTest
    @MethodSource("provideSessions")
    @DisplayName(":accessGrantLifecycle Access Grant issuance lifecycle")
    void accessGrantIssuanceLifecycleTest(final Session session) {
        LOGGER.info("Integration Test - Access Grant issuance lifecycle");

        final AccessGrantClient client = new AccessGrantClient(URI.create(VC_PROVIDER)).session(session);

        //Steps
        //1. issue & approve access grant (request)
        final Set<String> modes = new HashSet<>(Arrays.asList("Read"));
        final Set<String> purposes = new HashSet<>(Arrays.asList(
            "https://some.purpose/not-a-nefarious-one/i-promise",
            "https://some.other.purpose/"));
        final Instant expiration = Instant.parse("2023-04-03T12:00:00Z");
        final AccessGrant grant = client.issue(ACCESS_GRANT, URI.create(webidUrl), 
            new HashSet<>(Arrays.asList(sharedFileURI)), modes, purposes, expiration)
            .toCompletableFuture().join();

        //2. call verify endpoint to verify grant

        //3. get access grant from vcProvider - check that they match on proof at least
        final URI uri = URIBuilder.newBuilder(URI.create(VC_PROVIDER)).path(grant.getIdentifier().toString()).build();
        final AccessGrant grantFromVcProvider = client.fetch(uri).toCompletableFuture().join();
        assertEquals(grant.getPurpose(), grantFromVcProvider.getPurpose());

        //4. revoke access grant
        assertDoesNotThrow(client.revoke(grant).toCompletableFuture()::join);

        //5. call verify endpoint to check the grant is not valid
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
            Arguments.of(OpenIdSession.ofIdToken(token), //OpenId token
            Arguments.of(session)));
    }
}
