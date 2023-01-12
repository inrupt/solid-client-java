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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.inrupt.client.Request;
import com.inrupt.client.Resource;
import com.inrupt.client.Response;
import com.inrupt.client.Headers.WwwAuthenticate;
import com.inrupt.client.auth.Session;
import com.inrupt.client.openid.OpenIdConfig;
import com.inrupt.client.openid.OpenIdSession;
import com.inrupt.client.solid.SolidResourceException;
import com.inrupt.client.solid.SolidResourceHandlers;
import com.inrupt.client.solid.SolidSyncClient;
import com.inrupt.client.uma.UmaSession;
import com.inrupt.client.util.IOUtils;

import java.io.IOException;
import java.net.URI;
import java.security.KeyPair;
import java.util.Collections;
import org.apache.commons.rdf.api.RDFSyntax;
import org.jose4j.jwk.PublicJsonWebKey;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class MockServerTest {

    @BeforeAll
    static void setup() {
        Utils.initMockServer();
        Utils.POD_URL = Utils.getMockServerUrl();
    }

    @AfterAll
    static void teardown() {
        Utils.stopMockServer();
    }

    @Test
    void testAnonymousUserCRUD() {
        final SolidSyncClient client = SolidSyncClient.getClient().session(Session.anonymous());
        //create a public resource
        final var resourceUri = URI.create(Utils.getMockServerUrl() + "/playlist");
        final var playlist = new Playlist(resourceUri, null, null);

        final var req =
                Request.newBuilder(playlist.getIdentifier()).header(Utils.CONTENT_TYPE, Utils.TEXT_TURTLE)
                        .header(Utils.IF_NONE_MATCH, Utils.WILDCARD).PUT(cast(playlist)).build();

        final var res =
                client.send(req, Response.BodyHandlers.discarding());

        assertTrue(Utils.isSuccessful(res.statusCode()));

        final var reqGet =
                Request.newBuilder().uri(resourceUri).header(Utils.ACCEPT, Utils.TEXT_TURTLE).GET().build();

        final var resGet = client.send(reqGet, SolidResourceHandlers.ofSolidResource());

        assertTrue(Utils.isSuccessful(resGet.statusCode()));

        final var reqDelete =
                Request.newBuilder().uri(resourceUri).header(Utils.ACCEPT, Utils.TEXT_TURTLE).DELETE().build();

        final var resDelete = client.send(reqDelete, Response.BodyHandlers.discarding());

        assertTrue(Utils.isSuccessful(resDelete.statusCode()));
    }

    @Test
    void test412() {
        final SolidSyncClient client = SolidSyncClient.getClient().session(Session.anonymous());
        //create a private resource
        final var resourceUri = URI.create(Utils.getMockServerUrl() + "/playlist");
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
    void testUnuthenticatedCRUD() {
        final SolidSyncClient client = SolidSyncClient.getClient().session(Session.anonymous());
        //create a private resource
        final var resourceUri =
                URI.create(Utils.getMockServerUrl() + Utils.PRIVATE_RESOURCE_PATH + "/playlist");
        final var playlist = new Playlist(resourceUri, null, null);
        final var req = Request.newBuilder(playlist.getIdentifier())
                .header(Utils.CONTENT_TYPE, Utils.TEXT_TURTLE)
                .header(Utils.IF_NONE_MATCH, Utils.WILDCARD).PUT(cast(playlist)).build();
        final var res = client.send(req, Response.BodyHandlers.discarding());

        assertTrue(Utils.isSuccessful(res.statusCode()));

        final var reqGet = Request.newBuilder().uri(resourceUri)
                .header(Utils.ACCEPT, Utils.TEXT_TURTLE).GET().build();

        final var resGet = client.send(reqGet, SolidResourceHandlers.ofSolidResource());

        assertEquals(Utils.UNAUTHORIZED, resGet.statusCode());
        final var challenges = WwwAuthenticate.parse(resGet.headers().firstValue("WWW-Authenticate").get()).getChallenges();

        assertTrue(challenges.toString().contains(Utils.AS_URI));


        final var reqDelete = Request.newBuilder().uri(resourceUri)
                .header(Utils.ACCEPT, Utils.TEXT_TURTLE).DELETE().build();

        final var resDelete = client.send(reqDelete, Response.BodyHandlers.discarding());

        assertTrue(Utils.isSuccessful(resDelete.statusCode()));
    }
    
    @Test
    void testAuthenticatedBearerCRUD() {
        //authenticate with Bearer token
        final PublicJsonWebKey jwk = Utils.getDpopKey("/rsa-key.json");
        final OpenIdConfig config = new OpenIdConfig();
        config.setProofKeyPairs(Collections.singletonMap("RS256",
                new KeyPair(jwk.getPublicKey(), jwk.getPrivateKey())));
                    
        final var session = OpenIdSession.ofIdToken(Utils.setupIdToken(), config);
        SolidSyncClient client = SolidSyncClient.getClient().session(UmaSession.of(session));
        //create a private resource
        final var resourceUri =
                URI.create(Utils.getMockServerUrl() + Utils.PRIVATE_RESOURCE_PATH + "/playlist");
        final var playlist = new Playlist(resourceUri, null, null);
        final var req = Request.newBuilder(playlist.getIdentifier())
                .header(Utils.CONTENT_TYPE, Utils.TEXT_TURTLE)
                .header(Utils.IF_NONE_MATCH, Utils.WILDCARD).PUT(cast(playlist)).build();
        final var res = client.send(req, Response.BodyHandlers.discarding());

        assertTrue(Utils.isSuccessful(res.statusCode()));

        final var reqGet = Request.newBuilder().uri(resourceUri)
                .header(Utils.ACCEPT, Utils.TEXT_TURTLE).GET().build();

        final var resGet = client.send(reqGet, SolidResourceHandlers.ofSolidResource());

        assertTrue(Utils.isSuccessful(resGet.statusCode()));

        final var reqDelete = Request.newBuilder().uri(resourceUri)
                .header(Utils.ACCEPT, Utils.TEXT_TURTLE).DELETE().build();

        final var resDelete = client.send(reqDelete, Response.BodyHandlers.discarding());

        assertTrue(Utils.isSuccessful(resDelete.statusCode()));
    }

    private Request.BodyPublisher cast(final Resource resource) {
        return IOUtils.buffer(out -> {
            try {
                resource.serialize(RDFSyntax.TURTLE, out);
            } catch (final IOException ex) {
                throw new SolidResourceException("Unable to serialize " + resource.getClass().getName() +
                        " into Solid Resource", ex);
            }
        });
    }
}
