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
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.rdf4j.http.client.SPARQLProtocolSession;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sparql.query.SPARQLUpdate;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class RDF4JBodyHandlersTest {

    private static final MockHttpService mockHttpService = new MockHttpService();
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
    void testGetOfModelAsync() throws IOException,
            InterruptedException, ExecutionException, TimeoutException {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.get("httpMock_uri") + "/oneTriple"))
                .GET()
                .build();

        final var asyncResponse = client.sendAsync(request, RDF4JBodyHandlers.ofModel());

        final var statusCode = asyncResponse.thenApply(HttpResponse::statusCode).join();
        final var responseBody = asyncResponse.thenApply(HttpResponse::body).join().get();

        assertEquals(200, statusCode);
        assertEquals(1, responseBody.size());
        assertTrue(responseBody.contains(
            (Resource)SimpleValueFactory.getInstance().createIRI("http://example.com/s"),
            null,
            null,
            (Resource)null)
        );
    }

    @Test
    void testGetOfModel() throws IOException,
            InterruptedException, ExecutionException, TimeoutException {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.get("httpMock_uri") + "/oneTriple"))
                .GET()
                .build();

        final var response = client.send(request, RDF4JBodyHandlers.ofModel());

        assertEquals(200, response.statusCode());
        final var responseBody = response.body().get();
        assertEquals(1, responseBody.size());
        assertTrue(responseBody.contains(
            (Resource)SimpleValueFactory.getInstance().createIRI("http://example.com/s"),
            null,
            null,
            (Resource)null)
        );
    }

    @Test
    void testGetOfModel2() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.get("httpMock_uri") + "/example"))
                .GET()
                .build();

        final var response = client.send(request, RDF4JBodyHandlers.ofModel());

        assertEquals(200, response.statusCode());
        final var responseBody = response.body().get();
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
    void testGetOfRepositoryAsync() throws IOException,
            InterruptedException, ExecutionException, TimeoutException {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.get("httpMock_uri") + "/oneTriple"))
                .GET()
                .build();

        final var asyncResponse = client.sendAsync(request, RDF4JBodyHandlers.ofRepository());

        final var statusCode = asyncResponse.thenApply(HttpResponse::statusCode).join();
        assertEquals(200, statusCode);

        final var responseBody = asyncResponse.thenApply(HttpResponse::body).join().get();
        assertTrue(responseBody instanceof Repository);
        try (final var conn = responseBody.getConnection()) {
            assertTrue(conn.hasStatement(
                (Resource)SimpleValueFactory.getInstance().createIRI("http://example.com/s"),
                null,
                null,
                false,
                (Resource)null
            )
            );
        }
    }

    @Test
    void testGetOfRepository() throws IOException,
            InterruptedException, ExecutionException, TimeoutException {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.get("httpMock_uri") + "/oneTriple"))
                .GET()
                .build();

        final var response = client.send(request, RDF4JBodyHandlers.ofRepository());
        assertEquals(200, response.statusCode());

        final var responseBody = response.body().get();
        assertTrue(responseBody instanceof Repository);
        try (final var conn = responseBody.getConnection()) {
            assertTrue(conn.hasStatement(
                (Resource)SimpleValueFactory.getInstance().createIRI("http://example.com/s"),
                null,
                null,
                false,
                (Resource)null
            )
            );
        }
    }

    @Test
    void testGetOfRepository2() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.get("httpMock_uri") + "/example"))
                .GET()
                .build();

        final var response = client.send(request, RDF4JBodyHandlers.ofRepository());
        assertEquals(200, response.statusCode());

        final var responseBody = response.body().get();
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

    @Test
    void testPostOfModel() throws IOException, InterruptedException {

        final var builder = new ModelBuilder();
        builder.namedGraph(TestModel.G_RDF4J)
                .subject(TestModel.S_VALUE)
                    .add(TestModel.P_VALUE, TestModel.O_VALUE);
        builder.defaultGraph().subject(TestModel.S1_VALUE).add(TestModel.P_VALUE, TestModel.O1_VALUE);
        final var model = builder.build();

        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.get("httpMock_uri") + "/postOneTriple"))
                .header("Content-Type", "text/turtle")
                .POST(RDF4JBodyPublishers.ofModel(model))
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        final var response = client.send(request, HttpResponse.BodyHandlers.discarding());

        assertEquals(204, response.statusCode());
    }

    @Test
    void testPostOfRepository() throws IOException, InterruptedException {

        final var st = TestModel.VF.createStatement(
            TestModel.S_RDF4J,
            TestModel.P_RDF4J,
            TestModel.O_RDF4J,
            TestModel.G_RDF4J
        );
        final var st1 = TestModel.VF.createStatement(
            TestModel.S1_RDF4J,
            TestModel.P1_RDF4J,
            TestModel.O1_RDF4J
        );
        final var repository = new SailRepository(new MemoryStore());
        try (final var conn = repository.getConnection()) {
            conn.add(st);
            conn.add(st1);
        }

        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.get("httpMock_uri") + "/postOneTriple"))
                .header("Content-Type", "text/turtle")
                .POST(RDF4JBodyPublishers.ofRepository(repository))
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        final var response = client.send(request, HttpResponse.BodyHandlers.discarding());

        assertEquals(204, response.statusCode());
    }

    @Test
    void testGetOfSPARQLUpdate() throws IOException, InterruptedException {

        final var updateString =
            "INSERT DATA { <http://example.com/s1> <http://example.com/p1> <http://example.com/o1> .}";

        final var executorService = Executors.newFixedThreadPool(2);

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            final var sparqlProtocolSession = new SPARQLProtocolSession(httpclient, executorService);
            final SPARQLUpdate sU = new SPARQLUpdate(
                sparqlProtocolSession,
                "http://example.com",
                updateString
            );
            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.get("httpMock_uri") + "/sparqlUpdate"))
                    .header("Content-Type", "application/sparql-update")
                    .method("PATCH", RDF4JBodyPublishers.ofSparqlUpdate(sU))
                    .build();
            final var response = client.send(request, HttpResponse.BodyHandlers.discarding());
            assertEquals(204, response.statusCode());
        }
    }
}
