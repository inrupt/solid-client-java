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

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.junit.jupiter.api.*;

public class RDF4JBodyHandlerTest {

    private static final MockHttpClient mockHttpClient = new MockHttpClient();
    private static final Map<String, String> config = new HashMap<>();
    private static final HttpClient client = HttpClient.newHttpClient();

    @BeforeAll
    static void setup() {
        config.putAll(mockHttpClient.start());
    }

    @AfterAll
    static void teardown() {
        mockHttpClient.stop();
    }

    @Test
    void testBasic() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.get("httpMock_uri") + "/test"))
                .GET()
                .build();

        final var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        System.out.println(response.body());
    }

    @Test
    void testTTL() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.get("httpMock_uri") + "/oneTriple"))
                .GET()
                .build();

        final var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        System.out.println(response.body());
    }

    @Test
    void testModel() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.get("httpMock_uri") + "/oneTriple"))
                .GET()
                .build();
        final var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        final var responseBody = response.body();
        final var reader = new StringReader(responseBody);
        final var model = Rio.parse(reader, RDFFormat.TURTLE);
        System.out.println("RUNS");
    }

    @Test
    void testModelStream() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.get("httpMock_uri") + "/oneTriple"))
                .GET()
                .build();
        final var response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

        assertEquals(200, response.statusCode());
        final var responseBody = response.body();
        final var model = Rio.parse(responseBody, RDFFormat.TURTLE);
        System.out.println("RUNS");
    }

    @Disabled("Hangs on parsing the body (Rio.parse()) particularly when it tries to exit the thread")
    @Test
    void testModelMyBodyhandler() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://id.inrupt.com/langsamu.ttl"))
                .GET()
                .build();

        final var response = client.send(request, RDF4JBodyHandler.ofModel());

        assertEquals(200, response.statusCode());
        final var responseBody = response.body();
        System.out.println(responseBody);
    }

    @Disabled("Hangs on parsing the body (Rio.parse()) particularly when it tries to exit the thread")
    @Test
    void testOfModelPublisher() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.get("httpMock_uri") + "/oneTriple"))
                .header("Accept", "text/turtle; charset=UTF-8")
                .GET()
                .build();

        final HttpResponse<Model> response = client.send(request, RDF4JBodyHandler.ofModel());

        assertEquals(200, response.statusCode());
    }

    @Disabled("Hangs on parsing the body (Rio.parse()) particularly when it tries to exit the thread")
    @Test
    void testAsyncOfModelPublisher() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
                //.uri(URI.create(config.get("httpMock_uri") + "/example"))
                .uri(URI.create(config.get("httpMock_uri") + "/oneTriple"))
                .header("Accept", "text/turtle; charset=UTF-8")
                //.header("Content-Type", "text/turtle; charset=UTF-8")
                .GET()
                //.POST(RDF4JBodyPublisher.ofModel(expectedModel))
                .build();

        final var responseFuture = client.sendAsync(request, RDF4JBodyHandler.ofModel());

        responseFuture.whenComplete((response, error) -> {
            if (response != null) {
                assertEquals(200, response.statusCode());
            }
        });
        //System.out.println(responseFuture.join().body());
    }

    @Disabled("Hangs on adding to repository particularly when it tries to exit the thread")
    @Test
    void testOfRepositoryPublisher() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
                //.uri(URI.create(config.get("httpMock_uri") + "/example"))
                .uri(URI.create(config.get("httpMock_uri") + "/oneTriple"))
                .header("Accept", "text/turtle; charset=UTF-8")
                .GET()
                .build();

        final HttpResponse<Repository> response = client.send(request, RDF4JBodyHandler.ofRepository());

        assertEquals(200, response.statusCode());
    }

}