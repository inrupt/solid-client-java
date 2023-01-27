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

import com.inrupt.client.Headers.WwwAuthenticate;
import com.inrupt.client.Request;
import com.inrupt.client.Resource;
import com.inrupt.client.Response;
import com.inrupt.client.auth.Session;
import com.inrupt.client.openid.OpenIdSession;
import com.inrupt.client.solid.SolidResourceException;
import com.inrupt.client.solid.SolidResourceHandlers;
import com.inrupt.client.solid.SolidSyncClient;
import com.inrupt.client.uma.UmaSession;
import com.inrupt.client.util.IOUtils;

import java.io.IOException;
import java.net.URI;

import org.apache.commons.rdf.api.RDFSyntax;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class MockServerTest {
    private static final MockSolidServer mockHttpServer = new MockSolidServer();
    private static final MockOpenIDProvider identityProviderServer = new MockOpenIDProvider();
    static final String mock_username = "someuser";
    private static final String mock_azp = "https://localhost:8080";
    private static final String mock_webid = "https://localhost:8080/someuser";

    @BeforeAll
    static void setup() {
        Utils.WEBID = URI.create(mock_webid);
        Utils.USERNAME = mock_username;
        Utils.AZP = mock_azp;
        Utils.PRIVATE_RESOURCE_PATH = "private";
        mockHttpServer.start();
        identityProviderServer.start();
        Utils.POD_URL = mockHttpServer.getMockServerUrl();
        Utils.ISS = identityProviderServer.getMockServerUrl();
        Utils.AS_URI = Utils.POD_URL;
    }

    @AfterAll
    static void teardown() {
        mockHttpServer.stop();
    }

    @Test
    void testAnonymousUserCRUD() {
        //create an authenticated client
        final SolidSyncClient client = SolidSyncClient.getClient().session(Session.anonymous());
        //create a public resource
        final var resourceUri = URI.create(Utils.POD_URL + "/playlist");
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
        //create an authenticated client
        final SolidSyncClient client = SolidSyncClient.getClient().session(Session.anonymous());
        //create a public resource
        final var resourceUri = URI.create(Utils.POD_URL + "/playlist");
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
        //create an authenticated client
        final SolidSyncClient client = SolidSyncClient.getClient();
        //create a private resource
        final var resourceUri =
                URI.create(Utils.POD_URL + "/" + Utils.PRIVATE_RESOURCE_PATH + "/playlist");
        final var playlist = new Playlist(resourceUri, null, null);
        final var req = Request.newBuilder(playlist.getIdentifier())
                .header(Utils.CONTENT_TYPE, Utils.TEXT_TURTLE)
                .header(Utils.IF_NONE_MATCH, Utils.WILDCARD).PUT(cast(playlist)).build();
        final var res = client.send(req, Response.BodyHandlers.discarding());

        assertEquals(Utils.UNAUTHORIZED, res.statusCode());

        final var reqGet = Request.newBuilder().uri(resourceUri)
                .header(Utils.ACCEPT, Utils.TEXT_TURTLE).GET().build();
        final var resGet = client.send(reqGet, SolidResourceHandlers.ofSolidResource());

        assertEquals(Utils.UNAUTHORIZED, resGet.statusCode());
        final var challenges = WwwAuthenticate.parse(
                    resGet.headers().firstValue("WWW-Authenticate").get())
                .getChallenges();
        assertTrue(challenges.toString().contains(Utils.AS_URI));

        final var reqDelete = Request.newBuilder().uri(resourceUri)
                .header(Utils.ACCEPT, Utils.TEXT_TURTLE).DELETE().build();
        final var resDelete = client.send(reqDelete, Response.BodyHandlers.discarding());

        assertEquals(Utils.UNAUTHORIZED, resDelete.statusCode());
    }

    @Test
    void testAuthenticatedBearerCRUD() {
        //authenticate with Bearer token
        final var session = OpenIdSession.ofIdToken(Utils.setupIdToken());
        final SolidSyncClient client = SolidSyncClient.getClient().session(UmaSession.of(session));
        //create a private resource
        final var resourceUri =
                URI.create(Utils.POD_URL + "/" + Utils.PRIVATE_RESOURCE_PATH + "/playlist");
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
