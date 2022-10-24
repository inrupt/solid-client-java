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
package com.inrupt.client.http;

import static org.junit.jupiter.api.Assertions.*;

import com.inrupt.client.Request;
import com.inrupt.client.Response;
import com.inrupt.client.authentication.SolidAuthenticator;
import com.inrupt.client.authentication.UmaAuthenticationMechanism;
import com.inrupt.client.jena.JenaBodyHandlers;
import com.inrupt.client.jena.JenaBodyPublishers;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SolidClientTest {

    private static final SolidAuthenticator authenticator = new SolidAuthenticator();
    private static final SolidClient client = new SolidClient(authenticator);
    private static final MockHttpServer mockHttpServer = new MockHttpServer();
    private static final Map<String, String> config = new HashMap<>();

    @BeforeAll
    static void setup() {
        config.putAll(mockHttpServer.start());
        authenticator.register(new UmaAuthenticationMechanism(10));
    }

    @AfterAll
    static void teardown() {
        mockHttpServer.stop();
    }

    @Test
    void testSendOfString() throws IOException, InterruptedException {
        final Request request = Request.newBuilder()
                .uri(URI.create(config.get("http_uri") + "/file"))
                .GET()
                .build();

        final var response = client.send(request, Response.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Julie C. Sparks and David Widger"));
    }

    @Test
    void testSendOfStringAsync() throws IOException, InterruptedException {
        final Request request = Request.newBuilder()
                .uri(URI.create(config.get("http_uri") + "/file"))
                .GET()
                .build();

        final var asyncResponse = client.sendAsync(request, Response.BodyHandlers.ofString());

        final var statusCode = asyncResponse.thenApply(Response::statusCode).toCompletableFuture().join();
        assertEquals(200, statusCode);

        final var body = asyncResponse.thenApply(Response::body).toCompletableFuture().join();
        assertTrue(body.contains("Julie C. Sparks and David Widger"));
    }

    @Test
    void testSendOfModelProtectedResource() throws IOException, InterruptedException {

        final Request request = Request.newBuilder()
                .uri(URI.create(config.get("http_uri") + "/protected/resource"))
                .header("Accept", "text/turtle")
                .GET()
                .build();

        final var response = client.send(request, JenaBodyHandlers.ofModel());
        assertEquals(200, response.statusCode());

        final var responseBody = response.body();
        assertEquals(7, responseBody.size());
        assertTrue(responseBody.contains(
            ResourceFactory.createResource("http://example.test/me"),
            null)
        );
    }

    @Test
    void testSendOfModel() throws IOException, InterruptedException {

        final Request request = Request.newBuilder()
                .uri(URI.create(config.get("http_uri") + "/example"))
                .header("Accept", "text/turtle")
                .GET()
                .build();

        final var response = client.send(request, JenaBodyHandlers.ofModel());
        assertEquals(200, response.statusCode());

        final var responseBody = response.body();
        assertEquals(7, responseBody.size());
        assertTrue(responseBody.contains(
            ResourceFactory.createResource("http://example.test/me"),
            null)
        );
    }

    @Test
    void testSendOfModelAsync() throws IOException {
        final Request request = Request.newBuilder()
                .uri(URI.create(config.get("http_uri") + "/example"))
                .header("Accept", "text/turtle")
                .GET()
                .build();

        final var asyncResponse = client.sendAsync(request, JenaBodyHandlers.ofModel());

        final int statusCode = asyncResponse.thenApply(Response::statusCode).toCompletableFuture().join();
        assertEquals(200, statusCode);

        final var responseBody = asyncResponse.thenApply(Response::body).toCompletableFuture().join();
        assertEquals(7, responseBody.size());
        assertTrue(responseBody.contains(
            null,
            null,
            ResourceFactory.createResource("http://example.test//settings/prefs.ttl"))
        );
    }

    @Test
    void testOfModelPublisher() throws IOException, InterruptedException {
        final var model = ModelFactory.createDefaultModel();

        model.add(
            model.createResource("http://example.test/s"),
            model.createProperty("http://example.test/p"),
            model.createLiteral("object")
        );

        final Request request = Request.newBuilder()
                .uri(URI.create(config.get("http_uri") + "/postOneTriple"))
                .header("Content-Type", "text/turtle")
                .POST(JenaBodyPublishers.ofModel(model))
                .build();

        final var response = client.sendAsync(request, Response.BodyHandlers.discarding()).toCompletableFuture().join();

        assertEquals(401, response.statusCode());
        assertEquals(Optional.of("UMA ticket=\"ticket-12345\", as_uri=\"" + config.get("http_uri") + "\""),
                response.headers().firstValue("WWW-Authenticate"));
    }

    @Test
    void testSendRequestImage() throws IOException, InterruptedException {
        final Request request = Request.newBuilder()
                .uri(URI.create(config.get("http_uri") + "/solid.png"))
                .GET()
                .build();

        final var response = client.send(request, Response.BodyHandlers.ofByteArray());

        assertEquals(200, response.statusCode());
    }
}
