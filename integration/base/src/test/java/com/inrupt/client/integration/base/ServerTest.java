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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.jose4j.jwx.HeaderParameterNames.TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.inrupt.client.Headers.WwwAuthenticate;
import com.inrupt.client.Request;
import com.inrupt.client.Resource;
import com.inrupt.client.Response;
import com.inrupt.client.auth.Session;
import com.inrupt.client.openid.OpenIdSession;
import com.inrupt.client.solid.SolidResourceException;
import com.inrupt.client.solid.SolidResourceHandlers;
import com.inrupt.client.solid.SolidSyncClient;
import com.inrupt.client.webid.WebIdProfile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;
import org.jose4j.lang.UncheckedJoseException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ServerTest {
    private static MockSolidServer mockHttpServer;
    private static MockOpenIDProvider identityProviderServer;
    private static MockUMAAuthorizationServer authServer;
    private static MockWebIdService webIdService;
    private static String podUrl;
    private static String issuer;
    private static String webidUrl;
    private static final String MOCK_USERNAME = "someuser";
    private static final String PRIVATE_RESOURCE_PATH = "private";

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

        State.PRIVATE_RESOURCE_PATH = PRIVATE_RESOURCE_PATH;

        webidUrl = webIdService.getMockServerUrl() + "/" + MOCK_USERNAME;

        State.WEBID = URI.create(webidUrl);
        final SolidSyncClient client = SolidSyncClient.getClient();
        try (final WebIdProfile profile = client.read(URI.create(webidUrl), WebIdProfile.class)) {
            issuer = profile.getOidcIssuers().iterator().next().toString();
            podUrl = profile.getStorages().iterator().next().toString();
        }
    }

    @AfterAll
    static void teardown() {
        mockHttpServer.stop();
        identityProviderServer.stop();
        authServer.stop();
        webIdService.stop();
    }

    @Test
    void testAnonymousUserCRUD() {
        //create an authenticated client
        final SolidSyncClient client = SolidSyncClient.getClient().session(Session.anonymous());
        //create a public resource
        final var resourceUri = URI.create(podUrl + "/playlist");
        final var playlist = new Playlist(resourceUri, null, null);

        final var req =
                Request.newBuilder(playlist.getIdentifier()).header(Utils.CONTENT_TYPE, Utils.TEXT_TURTLE)
                        .header(Utils.IF_NONE_MATCH, Utils.WILDCARD).PUT(cast(playlist)).build();
        final var res =
                client.send(req, Response.BodyHandlers.discarding());

        assertTrue(Utils.isSuccessful(res.statusCode()));

        final var reqGet =
                Request.newBuilder().uri(resourceUri).header(Utils.ACCEPT, Utils.TEXT_TURTLE).GET().build();
        final var resGet = client.send(reqGet, SolidResourceHandlers.ofSolidRDFSource());

        assertTrue(Utils.isSuccessful(resGet.statusCode()));

        final var reqDelete =
                Request.newBuilder().uri(resourceUri).header(Utils.ACCEPT, Utils.TEXT_TURTLE).DELETE().build();
        final var resDelete = client.send(reqDelete, Response.BodyHandlers.discarding());

        assertTrue(Utils.isSuccessful(resDelete.statusCode()));
    }

    @Test
    void test412() {
        //create an authenticated client
        final SolidSyncClient client = SolidSyncClient.getClient().session(Session.anonymous());
        //create a public resource
        final var resourceUri = URI.create(podUrl + "/playlist");
        final var playlist = new Playlist(resourceUri, null, null);

        final var req = Request.newBuilder(playlist.getIdentifier())
                .header(Utils.CONTENT_TYPE, Utils.TEXT_TURTLE)
                .header(Utils.IF_NONE_MATCH, Utils.WILDCARD).PUT(cast(playlist)).build();
        final var res = client.send(req, Response.BodyHandlers.discarding());

        assertTrue(Utils.isSuccessful(res.statusCode()));

        //we try to create the exact same resource again
        final var reqPut = Request.newBuilder(playlist.getIdentifier())
                .header(Utils.CONTENT_TYPE, Utils.TEXT_TURTLE)
                .header(Utils.IF_NONE_MATCH, Utils.WILDCARD).PUT(cast(playlist)).build();
        final var resPut = client.send(reqPut, Response.BodyHandlers.discarding());

        assertEquals(Utils.PRECONDITION_FAILED, resPut.statusCode());

        final var reqDelete =
                Request.newBuilder().uri(resourceUri).header(Utils.ACCEPT, Utils.TEXT_TURTLE).DELETE().build();
        final var resDelete = client.send(reqDelete, Response.BodyHandlers.discarding());

        assertTrue(Utils.isSuccessful(resDelete.statusCode()));
    }

    @Test
    void testUnauthenticatedCRUD() {
        //create an authenticated client
        final SolidSyncClient client = SolidSyncClient.getClient();
        //create a private resource
        final var resourceUri =
                URI.create(podUrl + "/" + State.PRIVATE_RESOURCE_PATH + "/playlist");
        final var playlist = new Playlist(resourceUri, null, null);
        final var req = Request.newBuilder(playlist.getIdentifier())
                .header(Utils.CONTENT_TYPE, Utils.TEXT_TURTLE)
                .header(Utils.IF_NONE_MATCH, Utils.WILDCARD).PUT(cast(playlist)).build();
        final var res = client.send(req, Response.BodyHandlers.discarding());

        assertEquals(Utils.UNAUTHORIZED, res.statusCode());

        final var reqGet = Request.newBuilder().uri(resourceUri)
                .header(Utils.ACCEPT, Utils.TEXT_TURTLE).GET().build();
        final var resGet = client.send(reqGet, SolidResourceHandlers.ofSolidRDFSource());

        assertEquals(Utils.UNAUTHORIZED, resGet.statusCode());
        final var challenges = WwwAuthenticate.parse(
                    resGet.headers().firstValue("WWW-Authenticate").get())
                .getChallenges();
        assertTrue(challenges.toString().contains("Bearer"));

        final var reqDelete = Request.newBuilder().uri(resourceUri)
                .header(Utils.ACCEPT, Utils.TEXT_TURTLE).DELETE().build();
        final var resDelete = client.send(reqDelete, Response.BodyHandlers.discarding());

        assertEquals(Utils.UNAUTHORIZED, resDelete.statusCode());
    }

    @Test
    void testAuthenticatedBearerCRUD() {
        //authenticate with Bearer token
        final var session = OpenIdSession.ofIdToken(setupIdToken(webidUrl, MOCK_USERNAME, issuer));
        final SolidSyncClient client = SolidSyncClient.getClient().session(session);
        //create a private resource
        final var resourceUri =
                URI.create(podUrl + "/" + State.PRIVATE_RESOURCE_PATH + "/playlist");
        final var playlist = new Playlist(resourceUri, null, null);
        final var req = Request.newBuilder(playlist.getIdentifier())
                .header(Utils.CONTENT_TYPE, Utils.TEXT_TURTLE)
                .header(Utils.IF_NONE_MATCH, Utils.WILDCARD).PUT(cast(playlist)).build();
        final var res = client.send(req, Response.BodyHandlers.discarding());

        assertTrue(Utils.isSuccessful(res.statusCode()));

        final var reqGet = Request.newBuilder().uri(resourceUri)
                .header(Utils.ACCEPT, Utils.TEXT_TURTLE).GET().build();
        final var resGet = client.send(reqGet, SolidResourceHandlers.ofSolidRDFSource());

        assertTrue(Utils.isSuccessful(resGet.statusCode()));

        final var reqDelete = Request.newBuilder().uri(resourceUri)
                .header(Utils.ACCEPT, Utils.TEXT_TURTLE).DELETE().build();

        final var resDelete = client.send(reqDelete, Response.BodyHandlers.discarding());

        assertTrue(Utils.isSuccessful(resDelete.statusCode()));
    }

    private Request.BodyPublisher cast(final Resource resource) {
        try {
            return Request.BodyPublishers.ofInputStream(resource.getEntity());
        } catch (final IOException ex) {
            throw new SolidResourceException("Unable to serialize " + resource.getClass().getName() +
                    " into Solid Resource", ex);
        }
    }

    static String setupIdToken(final String webid, final String username, final String issuer) {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", webid);
        claims.put("sub", username);
        claims.put("iss", issuer);
        claims.put("azp", State.AZP);

        return generateIdToken(claims);
    }

    static String generateIdToken(final Map<String, Object> claims) {
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
