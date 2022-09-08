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
package com.inrupt.client.rdf4j;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class RDF4JBodyHandlersTest {

    private static final RDF4JMockHttpService mockHttpService = new RDF4JMockHttpService();
    private static final Map<String, String> config = new HashMap<>();
    private static final HttpClient client = HttpClient.newHttpClient();

    @BeforeAll
    static void setup() {
        config.putAll(mockHttpService.start());
    }

    @AfterAll
    static void teardown() {
        mockHttpService.stop();
    }

    @Test
    void testOfModelHandlerAsync() throws IOException,
            InterruptedException, ExecutionException, TimeoutException {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.get("rdf4j_uri") + "/oneTriple"))
                .GET()
                .build();

        final var asyncResponse = client.sendAsync(request, RDF4JBodyHandlers.ofModel());

        final var statusCode = asyncResponse.thenApply(HttpResponse::statusCode).join();
        final var responseBody = asyncResponse.thenApply(HttpResponse::body).join();

        assertEquals(200, statusCode);
        assertEquals(1, responseBody.size());
        assertTrue(responseBody.contains(
            (Resource)SimpleValueFactory.getInstance().createIRI("http://example.test/s"),
            null,
            null,
            (Resource)null)
        );
    }

    @Test
    void testOfModelHandler() throws IOException,
            InterruptedException, ExecutionException, TimeoutException {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.get("rdf4j_uri") + "/oneTriple"))
                .GET()
                .build();

        final var response = client.send(request, RDF4JBodyHandlers.ofModel());

        assertEquals(200, response.statusCode());
        final var responseBody = response.body();
        assertEquals(1, responseBody.size());
        assertTrue(responseBody.contains(
            (Resource)SimpleValueFactory.getInstance().createIRI("http://example.test/s"),
            null,
            null,
            (Resource)null)
        );
    }

    @Test
    void testOfModelHandlerWithURL() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.get("rdf4j_uri") + "/example"))
                .GET()
                .build();

        final var response = client.send(request, RDF4JBodyHandlers.ofModel());

        assertEquals(200, response.statusCode());
        final var responseBody = response.body();
        assertEquals(7, responseBody.size());
        assertTrue(responseBody.contains(
                null,
                SimpleValueFactory.getInstance().createIRI("http://www.w3.org/ns/pim/space#preferencesFile"),
                null,
                (Resource)null
                )
        );
    }

    @Test
    void testOfRepositoryHandlerAsync() throws IOException,
            InterruptedException, ExecutionException, TimeoutException {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.get("rdf4j_uri") + "/oneTriple"))
                .GET()
                .build();

        final var asyncResponse = client.sendAsync(request, RDF4JBodyHandlers.ofRepository());

        final var statusCode = asyncResponse.thenApply(HttpResponse::statusCode).join();
        assertEquals(200, statusCode);

        final var responseBody = asyncResponse.thenApply(HttpResponse::body).join();
        assertTrue(responseBody instanceof Repository);
        try (final var conn = responseBody.getConnection()) {
            assertTrue(conn.hasStatement(
                (Resource)SimpleValueFactory.getInstance().createIRI("http://example.test/s"),
                null,
                null,
                false,
                (Resource)null
            )
            );
        }
    }

    @Test
    void testOfRepositoryHandler() throws IOException,
            InterruptedException, ExecutionException, TimeoutException {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.get("rdf4j_uri") + "/oneTriple"))
                .GET()
                .build();

        final var response = client.send(request, RDF4JBodyHandlers.ofRepository());
        assertEquals(200, response.statusCode());

        final var responseBody = response.body();
        assertTrue(responseBody instanceof Repository);
        try (final var conn = responseBody.getConnection()) {
            assertTrue(conn.hasStatement(
                (Resource)SimpleValueFactory.getInstance().createIRI("http://example.test/s"),
                null,
                null,
                false,
                (Resource)null
            )
            );
        }
    }

    @Test
    void testOfRepositoryHandlerWithURL() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.get("rdf4j_uri") + "/example"))
                .GET()
                .build();

        final var response = client.send(request, RDF4JBodyHandlers.ofRepository());
        assertEquals(200, response.statusCode());

        final var responseBody = response.body();
        assertTrue(responseBody instanceof Repository);
        try (final var conn = responseBody.getConnection()) {
            assertTrue(conn.hasStatement(
                null,
                SimpleValueFactory.getInstance().createIRI("http://www.w3.org/ns/pim/space#preferencesFile"),
                null,
                false,
                (Resource)null
            )
            );
        }
    }

}
