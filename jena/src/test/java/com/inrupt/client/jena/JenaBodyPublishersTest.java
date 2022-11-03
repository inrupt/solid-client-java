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

import com.inrupt.client.*;
import com.inrupt.client.spi.HttpService;
import com.inrupt.client.spi.ServiceProvider;
import com.inrupt.client.test.rdf.RdfMockService;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.jena.graph.Factory;
import org.apache.jena.graph.Graph;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.update.UpdateFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class JenaBodyPublishersTest {

    private static final RdfMockService mockHttpClient = new RdfMockService();
    private static final Map<String, String> config = new HashMap<>();
    private static final HttpService client = ServiceProvider.getHttpService();

    private static Dataset ds;
    private static Graph graph;

    private static Model model = ModelFactory.createDefaultModel();
    private static final Resource S_JENA = model.createResource(JenaTestModel.S_VALUE);
    private static final Literal O_JENA = model.createLiteral(JenaTestModel.O_VALUE);
    private static final Property P_JENA = model.createProperty(JenaTestModel.P_VALUE);

    private static final Resource S1_JENA = model.createResource(JenaTestModel.S1_VALUE);
    private static final Literal O1_JENA = model.createLiteral(JenaTestModel.O1_VALUE);

    @BeforeAll
    static void setup() {
        config.putAll(mockHttpClient.start());
    }

    @AfterAll
    static void teardown() {
        mockHttpClient.stop();
    }

    @Test
    void testOfModelPublisher() throws IOException, InterruptedException {
        model = ModelFactory.createDefaultModel();

        model.add(S_JENA, P_JENA, O_JENA);
        model.add(S1_JENA, P_JENA, O1_JENA);

        final Request request = Request.newBuilder()
                .uri(URI.create(config.get("rdf_uri") + "/postOneTriple"))
                .header("Content-Type", "text/turtle")
                .POST(JenaBodyPublishers.ofModel(model))
                .build();

        final var response = client.send(request, Response.BodyHandlers.discarding());

        assertEquals(204, response.statusCode());
    }

    @Test
    void testOfDatasetPublisher() throws IOException, InterruptedException {
        model = ModelFactory.createDefaultModel();

        model.add(S_JENA, P_JENA, O_JENA);
        model.add(S1_JENA, P_JENA, O1_JENA);

        ds = DatasetFactory.create(model);

        final Request request = Request.newBuilder()
                .uri(URI.create(config.get("rdf_uri") + "/postOneTriple"))
                .header("Content-Type", "text/turtle")
                .POST(JenaBodyPublishers.ofDataset(ds))
                .build();

        final var response = client.send(request, Response.BodyHandlers.discarding());

        assertEquals(204, response.statusCode());
    }

    @Test
    void testOfUpdateRequestPublisher() throws IOException, InterruptedException {

        final var ur = UpdateFactory.create(
            "INSERT DATA { <http://example.test/s1> <http://example.test/p1> <http://example.test/o1> }");

        final Request request = Request.newBuilder()
                    .uri(URI.create(config.get("rdf_uri") + "/sparqlUpdate"))
                    .header("Content-Type", "application/sparql-update")
                    .method("PATCH", JenaBodyPublishers.ofUpdateRequest(ur))
                    .build();
        final var response = client.send(request, Response.BodyHandlers.discarding());
        assertEquals(204, response.statusCode());
    }

    @Test
    void testOfGraphPublisher() throws IOException, InterruptedException {
        graph = Factory.createDefaultGraph();

        graph.add(JenaTestModel.S_NODE, JenaTestModel.P_NODE, JenaTestModel.O_NODE);
        graph.add(JenaTestModel.S1_NODE, JenaTestModel.P1_NODE, JenaTestModel.O1_NODE);

        final Request request = Request.newBuilder()
                .uri(URI.create(config.get("rdf_uri") + "/postOneTriple"))
                .header("Content-Type", "text/turtle")
                .POST(JenaBodyPublishers.ofGraph(graph))
                .build();

        final var response = client.send(request, Response.BodyHandlers.discarding());

        assertEquals(204, response.statusCode());
    }

}
