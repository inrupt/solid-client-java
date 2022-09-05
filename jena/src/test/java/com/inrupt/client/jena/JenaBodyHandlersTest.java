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

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class JenaBodyHandlersTest {

    private static final MockHttpService mockHttpClient = new MockHttpService();
    private static final Map<String, String> config = new HashMap<>();
    private static final HttpClient client = HttpClient.newHttpClient();

    private static final Model model = ModelFactory.createDefaultModel();
    private static Dataset ds;

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
    void testOfModelSubscriberAsync() throws IOException,
            InterruptedException, ExecutionException {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.get("httpMock_uri") + "/oneTriple"))
                .header("Accept", "text/turtle")
                .GET()
                .build();

        final var asyncResponse = client.sendAsync(request, JenaBodyHandlers.ofModel());

        final int statusCode = asyncResponse.thenApply(HttpResponse::statusCode).join();
        assertEquals(200, statusCode);

        final Model responseBody = asyncResponse.thenApply(HttpResponse::body).join().get();
        assertEquals(1, responseBody.size());
        assertTrue(responseBody.contains(
            ModelFactory.createDefaultModel().createResource("http://example.com/s"),
            (Property)null,
            (org.apache.jena.rdf.model.RDFNode)null)
        );
    }

    @Test
    void testOfModelSubscriber() throws IOException,
            InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.get("httpMock_uri") + "/oneTriple"))
                .GET()
                .build();

        final var response = client.send(request, JenaBodyHandlers.ofModel());

        assertEquals(200, response.statusCode());
        final var responseBody = response.body().get();
        assertEquals(1, responseBody.size());
        assertTrue(responseBody.contains(
            ModelFactory.createDefaultModel().createResource("http://example.com/s"),
            (Property)null,
            (org.apache.jena.rdf.model.RDFNode)null)
        );
    }

    @Test
    void testOfModelSubscriberWithURL() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.get("httpMock_uri") + "/example"))
                .GET()
                .build();

        final var response = client.send(request, JenaBodyHandlers.ofModel());

        assertEquals(200, response.statusCode());
        final var responseBody = response.body().get();
        assertEquals(7, responseBody.size());
        assertTrue(responseBody.contains(
            (Resource)null,
            (Property)ModelFactory.createDefaultModel()
                .createProperty("http://www.w3.org/ns/pim/space#preferencesFile"),
            (org.apache.jena.rdf.model.RDFNode)null)
        );
    }

    @Test
    void testOfModelPublisher() throws IOException, InterruptedException {

        model.add(S_JENA, P_JENA, O_JENA);
        model.add(S1_JENA, P_JENA, O1_JENA);

        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.get("httpMock_uri") + "/postOneTriple"))
                .header("Content-Type", "text/turtle")
                .POST(JenaBodyPublishers.ofModel(model))
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        final var response = client.send(request, HttpResponse.BodyHandlers.discarding());

        assertEquals(204, response.statusCode());
    }

    @Test
    void testOfRepositoryPublisher() throws IOException, InterruptedException {

        model.add(S_JENA, P_JENA, O_JENA);
        model.add(S1_JENA, P_JENA, O1_JENA);

        ds = DatasetFactory.create(model);

        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.get("httpMock_uri") + "/postOneTriple"))
                .header("Content-Type", "text/turtle")
                .POST(JenaBodyPublishers.ofDataset(ds))
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        final var response = client.send(request, HttpResponse.BodyHandlers.discarding());

        assertEquals(204, response.statusCode());
    }

    @Disabled("we receive 404")
    @Test
    void testOfUpdateRequestPublisher() throws IOException, InterruptedException {

        final var ur = UpdateFactory.create(
            "INSERT DATA { <http://example.com/s1> <http://example.com/p1> <http://example.com/o1> }");

        final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.get("httpMock_uri") + "/sparqlUpdate"))
                    .header("Content-Type", "application/sparql-update")
                    .method("PATCH", JenaBodyPublishers.ofUpdateRequest(ur))
                    .build();
        final var response = client.send(request, HttpResponse.BodyHandlers.discarding());
        assertEquals(204, response.statusCode());
    }

}
