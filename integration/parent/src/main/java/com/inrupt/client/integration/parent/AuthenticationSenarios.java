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
package com.inrupt.client.integration.parent;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.jose4j.jwx.HeaderParameterNames.TYPE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.inrupt.client.auth.Session;
import com.inrupt.client.openid.OpenIdSession;
import com.inrupt.client.solid.SolidClientException;
import com.inrupt.client.solid.SolidResource;
import com.inrupt.client.solid.SolidSyncClient;
import com.inrupt.client.webid.WebIdProfile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;
import org.jose4j.lang.UncheckedJoseException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class AuthenticationSenarios {

    private static MockSolidServer mockHttpServer;
    private static MockOpenIDProvider identityProviderServer;
    private static MockUMAAuthorizationServer authServer;
    private static MockWebIdSevice webIdService;
    private static String podUrl;
    private static String issuer;
    private static String webidUrl;
    private static final String MOCK_USERNAME = "someuser";

    private static String testResourceName = "resource.ttl";
    private static URI publicResourceURL;
    private static URI privateResourceURL;
    private static final Config config = ConfigProvider.getConfig();

    private static final String CLIENT_ID = config.getValue("inrupt.test.client-id", String.class);
    private static final String CLIENT_SECRET = config.getValue("inrupt.test.client-secret", String.class);
    private static final String AUTH_METHOD = config
        .getOptionalValue("inrupt.test.auth-method", String.class)
        .orElse("client_secret_basic");
    private static final String PRIVATE_RESOURCE_PATH = config
        .getOptionalValue("inrupt.test.privateResourcePath", String.class)
        .orElse("private");

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

        webidUrl = config
            .getOptionalValue("inrupt.test.webid", String.class)
            .orElse(webIdService.getMockServerUrl() + Utils.FOLDER_SEPARATOR + MOCK_USERNAME);

        State.WEBID = URI.create(webidUrl);
        final SolidSyncClient client = SolidSyncClient.getClient();
        try (final WebIdProfile profile = client.read(URI.create(webidUrl), WebIdProfile.class)) {
            issuer = profile.getOidcIssuer().iterator().next().toString();
            podUrl = profile.getStorage().iterator().next().toString();
        }

        publicResourceURL = URI.create(podUrl + Utils.FOLDER_SEPARATOR + testResourceName);
        privateResourceURL =
                URI.create(podUrl + Utils.FOLDER_SEPARATOR +
                State.PRIVATE_RESOURCE_PATH + Utils.FOLDER_SEPARATOR + testResourceName);
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
            Arguments.of(OpenIdSession.ofIdToken(setupIdToken(webidUrl, MOCK_USERNAME, issuer)), //OpenId token
            Arguments.of(OpenIdSession.ofClientCredentials
                (URI.create(issuer), //Client credentials
                CLIENT_ID,
                CLIENT_SECRET,
                AUTH_METHOD)
            )));
    }

    private static String setupIdToken(final String webid, final String username, final String issuer) {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", webid);
        claims.put("sub", username);
        claims.put("iss", issuer);
        claims.put("azp", State.AZP);

        return generateIdToken(claims);
    }

    private static String generateIdToken(final Map<String, Object> claims) {
        try (final InputStream resource = Utils.class.getResourceAsStream("/signing-key.json")) {
            final String jwks = IOUtils.toString(resource, UTF_8);
            final PublicJsonWebKey jwk = PublicJsonWebKey.Factory
                .newPublicJwk(jwks);

            final JsonWebSignature jws = new JsonWebSignature();
            jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256);
            jws.setHeader(TYPE, "JWT");
            jws.setKey(jwk.getPrivateKey());
            final JwtClaims jwtClaims = new JwtClaims();
            jwtClaims.setJwtId(UUID.randomUUID().toString());
            jwtClaims.setExpirationTimeMinutesInTheFuture(5);
            jwtClaims.setIssuedAtToNow();
            // override/set claims
            claims.entrySet().forEach(entry -> jwtClaims.setClaim(entry.getKey(), entry.getValue()));
            jws.setPayload(jwtClaims.toJson());

            return jws.getCompactSerialization();
        } catch (final IOException ex) {
            throw new UncheckedIOException("Unable to read JWK", ex);
        } catch (final JoseException ex) {
            throw new UncheckedJoseException("Unable to generate DPoP token", ex);
        }
    }
}
