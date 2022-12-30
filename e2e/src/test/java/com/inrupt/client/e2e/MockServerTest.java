package com.inrupt.client.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.inrupt.client.Request;
import com.inrupt.client.Resource;
import com.inrupt.client.Response;
import com.inrupt.client.Session;
import com.inrupt.client.solid.SolidClient;
import com.inrupt.client.solid.SolidResourceException;
import com.inrupt.client.solid.SolidResourceHandlers;
import com.inrupt.client.util.IOUtils;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.rdf.api.RDFSyntax;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class MockServerTest {
    
    private static final MockSolidServer mockHttpServer = new MockSolidServer();
    private static final Map<String, String> config = new HashMap<>();
    private static final SolidClient client = SolidClient.getClient().session(Session.anonymous());

    private static final String ACCEPT = "Accept";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String IF_NONE_MATCH = "If-None-Match";
    private static final String TEXT_TURTLE = "text/turtle";
    private static final String WILDCARD = "*";

    @BeforeAll
    static void setup() {
        config.putAll(mockHttpServer.start());
    }

    @AfterAll
    static void teardown() {
        mockHttpServer.stop();
    }

    @Test
    void testMockServerCRUD() {
        final var resourceUri = URI.create(config.get("solid_server") + "/playlist");

        final var playlist = new Playlist(resourceUri, null, null);

        final var req =
                Request.newBuilder(playlist.getIdentifier()).header(CONTENT_TYPE, TEXT_TURTLE)
                        .header(IF_NONE_MATCH, WILDCARD).PUT(cast(playlist)).build();

        final var res =
                client.send(req, Response.BodyHandlers.discarding()).toCompletableFuture().join();

        assertEquals(204, res.statusCode());

        final var reqGet =
                Request.newBuilder().uri(resourceUri).header(ACCEPT, TEXT_TURTLE).GET().build();

        final var resGet = client.send(reqGet, SolidResourceHandlers.ofSolidResource())
                .toCompletableFuture().join();

        assertEquals(200, resGet.statusCode());

        final var reqDelete =
                Request.newBuilder().uri(resourceUri).header(ACCEPT, TEXT_TURTLE).DELETE().build();

        final var resDelete = client.send(reqDelete, Response.BodyHandlers.discarding())
                .toCompletableFuture().join();

        assertEquals(204, resDelete.statusCode());
    }
    
    @Test
    void test412() {
        final var resourceUri = URI.create(config.get("solid_server") + "/playlist");

        final var playlist = new Playlist(resourceUri, null, null);

        final var req = Request.newBuilder(playlist.getIdentifier())
            .header(CONTENT_TYPE, TEXT_TURTLE)
            .header(IF_NONE_MATCH, WILDCARD)
            .PUT(cast(playlist))
            .build();

        final var res = client.send(req, Response.BodyHandlers.discarding()).toCompletableFuture().join();

        assertEquals(204, res.statusCode());

        final var reqPut = Request.newBuilder(playlist.getIdentifier())
            .header(CONTENT_TYPE, TEXT_TURTLE)
            .header(IF_NONE_MATCH, WILDCARD)
            .PUT(cast(playlist))
            .build();

        final var resPut = client.send(reqPut, Response.BodyHandlers.discarding()).toCompletableFuture().join();

        assertEquals(412, resPut.statusCode());
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
