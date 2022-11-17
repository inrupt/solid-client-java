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
package com.inrupt.client.core;

import static org.junit.jupiter.api.Assertions.*;

import com.inrupt.client.Client;
import com.inrupt.client.ClientProvider;
import com.inrupt.client.Request;
import com.inrupt.client.Response;
import com.inrupt.client.jena.JenaBodyHandlers;
import com.inrupt.client.jena.JenaBodyPublishers;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class DefaultClientTest {

    static final MockHttpService mockHttpServer = new MockHttpService();
    static final Client client = ClientProvider
            .getClientBuilder()
            .withConfig(new DefaultClientTest.CustomConfig())
            .build();
    static final AtomicReference<String> baseUri = new AtomicReference<>();

    @BeforeAll
    static void setup() {
        baseUri.set(mockHttpServer.start());
    }

    @AfterAll
    static void teardown() {
        mockHttpServer.stop();
    }

    @Test
    void clientLoader() {
        assertNotNull(client);
    }

    @Test
    void testSendOfString() throws IOException, InterruptedException {
        final Request request = Request.newBuilder()
                .uri(URI.create(baseUri.get() + "/file"))
                .GET()
                .build();

        final Response<String> response = client.send(request, Response.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Julie C. Sparks and David Widger"));
    }

    @Test
    void testSendOfStringAsync() throws IOException, InterruptedException {
        final Request request = Request.newBuilder()
                .uri(URI.create(baseUri.get() + "/file"))
                .GET()
                .build();

        final Response<String> response = client.sendAsync(request, Response.BodyHandlers.ofString())
            .toCompletableFuture().join();

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Julie C. Sparks and David Widger"));
    }

    @Test
    void testSendOfModelProtectedResource() throws IOException, InterruptedException {

        final Request request = Request.newBuilder()
                .uri(URI.create(baseUri.get() + "/protected/resource"))
                .header("Accept", "text/turtle")
                .GET()
                .build();

        final Response<Model> response = client.send(request, JenaBodyHandlers.ofModel());
        assertEquals(200, response.statusCode());

        final Model responseBody = response.body();
        assertEquals(7, responseBody.size());
        assertTrue(responseBody.contains(
            ResourceFactory.createResource("http://example.test/me"),
            null)
        );
    }

    @Test
    void testSendOfModel() throws IOException, InterruptedException {

        final Request request = Request.newBuilder()
                .uri(URI.create(baseUri.get() + "/example"))
                .header("Accept", "text/turtle")
                .GET()
                .build();

        final Response<Model> response = client.send(request, JenaBodyHandlers.ofModel());
        assertEquals(200, response.statusCode());

        final Model responseBody = response.body();
        assertEquals(7, responseBody.size());
        assertTrue(responseBody.contains(
            ResourceFactory.createResource("http://example.test/me"),
            null)
        );
    }

    @Test
    void testSendOfModelAsync() throws IOException {
        final Request request = Request.newBuilder()
                .uri(URI.create(baseUri.get() + "/example"))
                .header("Accept", "text/turtle")
                .GET()
                .build();

        final Response<Model> response = client.sendAsync(request, JenaBodyHandlers.ofModel())
            .toCompletableFuture().join();

        assertEquals(200, response.statusCode());

        final Model model = response.body();
        assertEquals(7, model.size());
        assertTrue(model.contains(
            null,
            null,
            ResourceFactory.createResource("http://example.test//settings/prefs.ttl"))
        );
    }

    @Test
    void testOfModelPublisher() throws IOException, InterruptedException {
        final Model model = ModelFactory.createDefaultModel();

        model.add(
            model.createResource("http://example.test/s"),
            model.createProperty("http://example.test/p"),
            model.createLiteral("object")
        );

        final Request request = Request.newBuilder()
                .uri(URI.create(baseUri.get() + "/postOneTriple"))
                .header("Content-Type", "text/turtle")
                .POST(JenaBodyPublishers.ofModel(model))
                .build();

        final Response<Void> response = client.sendAsync(request, Response.BodyHandlers.discarding())
            .toCompletableFuture().join();

        assertEquals(401, response.statusCode());
        assertEquals(Optional.of("UMA ticket=\"ticket-12345\", as_uri=\"" + baseUri.get() + "\""),
                response.headers().firstValue("WWW-Authenticate"));
    }

    @Test
    void testSendRequestImage() throws IOException, InterruptedException {
        final Request request = Request.newBuilder()
                .uri(URI.create(baseUri.get() + "/solid.png"))
                .GET()
                .build();

        final Response<byte[]> response = client.send(request, Response.BodyHandlers.ofByteArray());

        assertEquals(200, response.statusCode());
    }

    public static class CustomConfig implements Client.Config {
        public long getRetryRedirects() {
            return 10;
        }
    }
}
