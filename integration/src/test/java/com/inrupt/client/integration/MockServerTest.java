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
import com.inrupt.client.auth.Session;
import com.inrupt.client.solid.SolidResourceException;
import com.inrupt.client.solid.SolidResourceHandlers;
import com.inrupt.client.solid.SolidSyncClient;
import com.inrupt.client.util.IOUtils;

import java.io.IOException;
import java.net.URI;

import org.apache.commons.rdf.api.RDFSyntax;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class MockServerTest {

    private static final String ACCEPT = "Accept";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String IF_NONE_MATCH = "If-None-Match";
    private static final String TEXT_TURTLE = "text/turtle";
    private static final String WILDCARD = "*";

    @BeforeAll
    static void setup() {
        Utils.initMockServer();
    }

    @AfterAll
    static void teardown() {
        Utils.stopMockServer();
    }

    @Test
    void testMockServerAnonymousUserCRUD() {
        final SolidSyncClient client = SolidSyncClient.getClient().session(Session.anonymous());
        final var resourceUri = URI.create(Utils.getMockServerUrl() + "/playlist");

        final var playlist = new Playlist(resourceUri, null, null);

        final var req =
                Request.newBuilder(playlist.getIdentifier()).header(CONTENT_TYPE, TEXT_TURTLE)
                        .header(IF_NONE_MATCH, WILDCARD).PUT(cast(playlist)).build();

        final var res =
                client.send(req, Response.BodyHandlers.discarding());

        assertTrue(Utils.isSuccessful(res.statusCode()));

        final var reqGet =
                Request.newBuilder().uri(resourceUri).header(ACCEPT, TEXT_TURTLE).GET().build();

        final var resGet = client.send(reqGet, SolidResourceHandlers.ofSolidResource());

        assertEquals(Utils.UNAUTHORIZED, resGet.statusCode());
        //assertTrue(Utils.isSuccessful(resGet.statusCode()));

        final var reqDelete =
                Request.newBuilder().uri(resourceUri).header(ACCEPT, TEXT_TURTLE).DELETE().build();

        final var resDelete = client.send(reqDelete, Response.BodyHandlers.discarding());

        assertTrue(Utils.isSuccessful(resDelete.statusCode()));
    }

    @Test
    void test412() {
        final SolidSyncClient client = SolidSyncClient.getClient().session(Session.anonymous());
        final var resourceUri = URI.create(Utils.getMockServerUrl() + "/playlist");

        final var playlist = new Playlist(resourceUri, null, null);

        final var req = Request.newBuilder(playlist.getIdentifier())
            .header(CONTENT_TYPE, TEXT_TURTLE)
            .header(IF_NONE_MATCH, WILDCARD)
            .PUT(cast(playlist))
            .build();

        final var res = client.send(req, Response.BodyHandlers.discarding());

        assertTrue(Utils.isSuccessful(res.statusCode()));

        final var reqPut = Request.newBuilder(playlist.getIdentifier())
            .header(CONTENT_TYPE, TEXT_TURTLE)
            .header(IF_NONE_MATCH, WILDCARD)
            .PUT(cast(playlist))
            .build();

        final var resPut = client.send(reqPut, Response.BodyHandlers.discarding());

        assertEquals(Utils.PRECONDITION_FAILED, resPut.statusCode());
    }

    static Request.BodyPublisher cast(final Resource resource) {
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
