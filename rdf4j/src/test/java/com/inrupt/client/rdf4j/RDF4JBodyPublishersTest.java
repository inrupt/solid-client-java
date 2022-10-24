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

import com.inrupt.client.Request;
import com.inrupt.client.Response;
import com.inrupt.client.spi.HttpProcessor;
import com.inrupt.client.spi.ServiceProvider;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.rdf4j.http.client.SPARQLProtocolSession;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sparql.query.SPARQLUpdate;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class RDF4JBodyPublishersTest {

    private static final RDF4JMockHttpService mockHttpService = new RDF4JMockHttpService();
    private static final Map<String, String> config = new HashMap<>();
    private static final HttpProcessor client = ServiceProvider.getHttpProcessor();

    @BeforeAll
    static void setup() {
        config.putAll(mockHttpService.start());
    }

    @AfterAll
    static void teardown() {
        mockHttpService.stop();
    }

    @Test
    void testOfModelPublisher() throws IOException, InterruptedException {

        final ModelBuilder builder = new ModelBuilder();
        builder.namedGraph(RDF4JTestModel.G_RDF4J)
                .subject(RDF4JTestModel.S_VALUE)
                    .add(RDF4JTestModel.P_VALUE, RDF4JTestModel.O_VALUE);
        builder.defaultGraph().subject(RDF4JTestModel.S1_VALUE).add(RDF4JTestModel.P_VALUE, RDF4JTestModel.O1_VALUE);
        final Model model = builder.build();

        final Request request = Request.newBuilder()
                .uri(URI.create(config.get("rdf4j_uri") + "/postOneTriple"))
                .header("Content-Type", "text/turtle")
                .POST(RDF4JBodyPublishers.ofModel(model))
                .build();

        final Response<Void> response = client.send(request, Response.BodyHandlers.discarding());

        assertEquals(204, response.statusCode());
    }

    @Test
    void testOfRepositoryPublisher() throws IOException, InterruptedException {

        final Statement st = RDF4JTestModel.VF.createStatement(
            RDF4JTestModel.S_RDF4J,
            RDF4JTestModel.P_RDF4J,
            RDF4JTestModel.O_RDF4J,
            RDF4JTestModel.G_RDF4J
        );
        final Statement st1 = RDF4JTestModel.VF.createStatement(
            RDF4JTestModel.S1_RDF4J,
            RDF4JTestModel.P1_RDF4J,
            RDF4JTestModel.O1_RDF4J
        );
        final Repository repository = new SailRepository(new MemoryStore());
        try (final RepositoryConnection conn = repository.getConnection()) {
            conn.add(st);
            conn.add(st1);
        }

        final Request request = Request.newBuilder()
                .uri(URI.create(config.get("rdf4j_uri") + "/postOneTriple"))
                .header("Content-Type", "text/turtle")
                .POST(RDF4JBodyPublishers.ofRepository(repository))
                .build();

        final Response<Void> response = client.send(request, Response.BodyHandlers.discarding());

        assertEquals(204, response.statusCode());
    }

    @Test
    void testOfSPARQLUpdatePublisher() throws IOException, InterruptedException {

        final String updateString =
            "INSERT DATA { <http://example.test/s1> <http://example.test/p1> <http://example.test/o1> .}";

        final ExecutorService executorService = Executors.newFixedThreadPool(2);

        try (final CloseableHttpClient httpclient = HttpClients.createDefault()) {
            final SPARQLProtocolSession sparqlProtocolSession = new SPARQLProtocolSession(httpclient, executorService);
            final SPARQLUpdate sU = new SPARQLUpdate(
                sparqlProtocolSession,
                "http://example.test",
                updateString
            );
            final Request request = Request.newBuilder()
                    .uri(URI.create(config.get("rdf4j_uri") + "/sparqlUpdate"))
                    .header("Content-Type", "application/sparql-update")
                    .method("PATCH", RDF4JBodyPublishers.ofSparqlUpdate(sU))
                    .build();
            final Response<Void> response = client.send(request, Response.BodyHandlers.discarding());
            assertEquals(204, response.statusCode());
        }
    }

}
