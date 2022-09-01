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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.rdf4j.http.client.SPARQLProtocolSession;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sparql.query.SPARQLUpdate;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class RDF4JBodyHandlerTest {

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
    void testBasic() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.get("httpMock_uri") + "/test"))
                .GET()
                .build();

        final var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("testResponse", response.body());
    }

    @Test
    void testBasicTTL() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.get("httpMock_uri") + "/oneTriple"))
                .GET()
                .build();

        final var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("<http://example.com/s> <http://example.com/p> <http://example.com/o> .", response.body());
    }

    @Test
    void testBasicModel() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.get("httpMock_uri") + "/oneTriple"))
                .GET()
                .build();
        final var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        final var responseBody = response.body();
        final var reader = new StringReader(responseBody);
        final var model = Rio.parse(reader, RDFFormat.TURTLE);
        assertEquals(1, model.predicates().size());
    }

    @Test
    void testBasicModelStream() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.get("httpMock_uri") + "/oneTriple"))
                .GET()
                .build();
        final var response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

        assertEquals(200, response.statusCode());
        final var responseBody = response.body();
        final var model = Rio.parse(responseBody, RDFFormat.TURTLE);
        assertEquals(1, model.predicates().size());
    }

    @Test
    void testOfModelSubscriberAsync() throws IOException,
            InterruptedException, ExecutionException, TimeoutException {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.get("httpMock_uri") + "/oneTriple"))
                .GET()
                .build();

        final var asyncResponse = client.sendAsync(request, RDF4JBodyHandler.ofModel());

        final var statusCode = asyncResponse.thenApply(HttpResponse::statusCode).join();
        final var responseBody = asyncResponse.thenApply(HttpResponse::body).join();

        assertEquals(200, statusCode);
    }

    @Test
    void testOfModelSubscriberAsyncWithExecutor() throws IOException,
            InterruptedException, ExecutionException, TimeoutException {

        final ExecutorService executorService = Executors.newFixedThreadPool(2);

        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.get("httpMock_uri") + "/oneTriple"))
                .header("Accept", "text/turtle")
                .GET()
                .build();

        final var asyncResponse = HttpClient.newBuilder()
            .executor(executorService)
            .build()
            .sendAsync(request, RDF4JBodyHandler.ofModel());

        final var statusCode = asyncResponse.thenApply(HttpResponse::statusCode).join();
        final var responseBody = asyncResponse.thenApply(HttpResponse::body).join();

        executorService.shutdownNow();

        assertEquals(200, statusCode);
    }

    @Test
    void testOfModelSubscriber() throws IOException,
            InterruptedException, ExecutionException, TimeoutException {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.get("httpMock_uri") + "/oneTriple"))
                .GET()
                .build();

        final var response = client.send(request, RDF4JBodyHandler.ofModel());

        assertEquals(200, response.statusCode());
        final var body = response.body();
    }

    @Test
    void testOfModelSubscriberWithURL() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://id.inrupt.com/langsamu.ttl"))
                .GET()
                .build();

        final var response = client.send(request, RDF4JBodyHandler.ofModel());

        assertEquals(200, response.statusCode());
        final var responseBody = response.body();
    }

    //POST
    @Test
    void testOfModelPublisher() throws IOException, InterruptedException {

        final var builder = new ModelBuilder();
        builder.namedGraph(TestModel.G_RDF4J)
                .subject(TestModel.S_VALUE)
                    .add(TestModel.P_VALUE, TestModel.O_VALUE);
        builder.defaultGraph().subject(TestModel.S1_VALUE).add(TestModel.P_VALUE, TestModel.O1_VALUE);
        final var model = builder.build();

        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.get("httpMock_uri") + "/postOneTriple"))
                .header("Content-Type", "text/turtle")
                .POST(RDF4JBodyPublisher.ofModel(model))
                .build();

        final var response = client.send(request, HttpResponse.BodyHandlers.discarding());

        assertEquals(204, response.statusCode());
    }

    @Test
    void testOfRepositoryPublisher() throws IOException, InterruptedException {

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
                .POST(RDF4JBodyPublisher.ofRepository(repository))
                .build();

        final var response = client.send(request, HttpResponse.BodyHandlers.discarding());

        assertEquals(204, response.statusCode());
    }

    @Test
    void testOfSPARQLUpdatePublisher() throws IOException, InterruptedException {

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
                    .method("PATCH", RDF4JBodyPublisher.ofSparqlUpdate(sU))
                    .build();
            final var response = client.send(request, HttpResponse.BodyHandlers.discarding());
            assertEquals(204, response.statusCode());
        }
    }
}
