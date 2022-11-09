/*
 * Copyright 2022 Inrupt Inc.
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
package com.inrupt.client.jena;

import static org.junit.jupiter.api.Assertions.*;

import com.inrupt.client.Request;
import com.inrupt.client.Response;
import com.inrupt.client.spi.HttpService;
import com.inrupt.client.spi.ServiceProvider;
import com.inrupt.client.test.RdfMockService;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class JenaBodyHandlersTest {

    private static final RdfMockService mockHttpServer = new RdfMockService();
    private static final Map<String, String> config = new HashMap<>();
    private static final HttpService client = ServiceProvider.getHttpService();

    @BeforeAll
    static void setup() {
        config.putAll(mockHttpServer.start());
    }

    @AfterAll
    static void teardown() {
        mockHttpServer.stop();
    }

    @Test
    void testOfModelHandler() throws IOException,
            InterruptedException {
        final Request request = Request.newBuilder()
                .uri(URI.create(config.get("rdf_uri") + "/oneTriple"))
                .GET()
                .build();

        final var response = client.send(request, JenaBodyHandlers.ofModel());

        assertEquals(200, response.statusCode());
        final var responseBody = response.body();
        assertEquals(1, responseBody.size());
        assertTrue(responseBody.contains(
            null,
            null,
            ResourceFactory.createResource("http://example.test/o"))
        );
    }

    @Test
    void testOfModelHandlerAsync() throws IOException,
            InterruptedException, ExecutionException {
        final Request request = Request.newBuilder()
                .uri(URI.create(config.get("rdf_uri") + "/oneTriple"))
                .header("Accept", "text/turtle")
                .GET()
                .build();

        final var asyncResponse = client.sendAsync(request, JenaBodyHandlers.ofModel());

        final int statusCode = asyncResponse.thenApply(Response::statusCode).toCompletableFuture().join();
        assertEquals(200, statusCode);

        final var responseBody = asyncResponse.thenApply(Response::body).toCompletableFuture().join();
        assertEquals(1, responseBody.size());
        assertTrue(responseBody.contains(
            null,
            null,
            ResourceFactory.createResource("http://example.test/o"))
        );
    }

    @Test
    void testOfModelHandlerWithURL() throws IOException, InterruptedException {
        final Request request = Request.newBuilder()
                .uri(URI.create(config.get("rdf_uri") + "/example"))
                .GET()
                .build();

        final var response = client.send(request, JenaBodyHandlers.ofModel());

        assertEquals(200, response.statusCode());
        final var responseBody = response.body();
        assertEquals(7, responseBody.size());
        assertTrue(responseBody.contains(
            null,
            ResourceFactory.createProperty("http://www.w3.org/ns/pim/space#preferencesFile"))
        );
    }

    @Test
    void testOfDatasetHandler() throws IOException,
            InterruptedException {
        final Request request = Request.newBuilder()
                .uri(URI.create(config.get("rdf_uri") + "/oneTriple"))
                .GET()
                .build();

        final var response = client.send(request, JenaBodyHandlers.ofDataset());

        assertEquals(200, response.statusCode());
        final var responseBody = response.body();
        assertEquals(1, responseBody.asDatasetGraph().stream().count());
        assertTrue(responseBody.asDatasetGraph().contains(
            null,
            NodeFactory.createURI("http://example.test/s"),
            null,
            null)
        );
    }

    @Test
    void testOfDatasetHandlerWithURL() throws IOException, InterruptedException {
        final Request request = Request.newBuilder()
                .uri(URI.create(config.get("rdf_uri") + "/example"))
                .GET()
                .build();

        final var response = client.send(request, JenaBodyHandlers.ofDataset());

        assertEquals(200, response.statusCode());
        final var responseBody = response.body();
        assertEquals(7, responseBody.asDatasetGraph().stream().count());
        assertTrue(responseBody.asDatasetGraph().contains(
            null,
            null,
            NodeFactory.createURI("http://www.w3.org/ns/pim/space#preferencesFile"),
            null)
        );
    }

    @Test
    void testOfGraphHandlerAsync() throws IOException,
            InterruptedException, ExecutionException {
        final Request request = Request.newBuilder()
                .uri(URI.create(config.get("rdf_uri") + "/oneTriple"))
                .header("Accept", "text/turtle")
                .GET()
                .build();

        final var asyncResponse = client.sendAsync(request, JenaBodyHandlers.ofGraph());

        final int statusCode = asyncResponse.thenApply(Response::statusCode).toCompletableFuture().join();
        assertEquals(200, statusCode);

        final var responseBody = asyncResponse.thenApply(Response::body).toCompletableFuture().join();
        assertEquals(1, responseBody.size());
        assertTrue(responseBody.contains(
            NodeFactory.createURI("http://example.test/s"),
            null,
            null)
        );
    }

    @Test
    void testOfGraphHandler() throws IOException,
            InterruptedException {
        final Request request = Request.newBuilder()
                .uri(URI.create(config.get("rdf_uri") + "/oneTriple"))
                .GET()
                .build();

        final var response = client.send(request, JenaBodyHandlers.ofGraph());

        assertEquals(200, response.statusCode());
        final var responseBody = response.body();
        assertEquals(1, responseBody.size());
        assertTrue(responseBody.contains(
            NodeFactory.createURI("http://example.test/s"),
            null,
            null)
        );
    }

    @Test
    void testOfGraphHandlerWithURL() throws IOException, InterruptedException {
        final Request request = Request.newBuilder()
                .uri(URI.create(config.get("rdf_uri") + "/example"))
                .GET()
                .build();

        final var response = client.send(request, JenaBodyHandlers.ofGraph());

        assertEquals(200, response.statusCode());
        final var responseBody = response.body();
        assertEquals(7, responseBody.size());
        assertTrue(responseBody.contains(
            null,
            NodeFactory.createURI("http://www.w3.org/ns/pim/space#preferencesFile"),
            null)
        );
    }
}
