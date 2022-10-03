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

import com.inrupt.client.authentication.SolidAuthenticator;
import com.inrupt.client.authentication.UmaAuthenticationMechanism;
import com.inrupt.client.jena.JenaBodyHandlers;
import com.inrupt.client.jena.JenaBodyPublishers;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.PushPromiseHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SolidClientTest {

    private static final SolidAuthenticator authenticator = new SolidAuthenticator();
    private static final SolidClient client = SolidClient.Builder.newBuilder()
        .authenticator(authenticator)
        .build();
    private static final MockHttpServer mockHttpServer = new MockHttpServer();
    private static final Map<String, String> config = new HashMap<>();
    private static final List<CompletableFuture<Void>> pushPromisesMap = new ArrayList<>();
    private static final List<CompletableFuture<Void>> asyncPushRequests = new CopyOnWriteArrayList<>();

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
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.get("http_uri") + "/file"))
                .GET()
                .build();

        final var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Julie C. Sparks and David Widger"));
    }

    @Test
    void testSendOfStringAsync() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.get("http_uri") + "/file"))
                .GET()
                .build();

        final var asyncResponse = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());

        final var statusCode = asyncResponse.thenApply(HttpResponse::statusCode).join();
        assertEquals(200, statusCode);

        final var body = asyncResponse.thenApply(HttpResponse::body).join();
        assertTrue(body.contains("Julie C. Sparks and David Widger"));
    }

    @Test
    void testSendOfStringAsyncWithPromise() throws IOException, InterruptedException {

        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.get("http_uri") + "/file"))
                .GET()
                .build();

        final var asyncResponse = client.sendAsync(request,
            HttpResponse.BodyHandlers.ofString(),
            stringPromiseHandler()
        );
        final var statusCode = asyncResponse.thenApply(HttpResponse::statusCode).join();
        assertEquals(200, statusCode);

        final var body = asyncResponse.thenApply(HttpResponse::body).join();
        assertTrue(body.contains("Julie C. Sparks and David Widger"));

        asyncPushRequests.forEach(CompletableFuture::join);
        assertEquals(0, asyncPushRequests.size());
    }

    private static PushPromiseHandler<String> stringPromiseHandler() {
        return (HttpRequest initiatingRequest,
            HttpRequest pushPromiseRequest,
            Function<HttpResponse.BodyHandler<String>,
            CompletableFuture<HttpResponse<String>>> acceptor) -> {
            final CompletableFuture<Void> pushcf =
                acceptor.apply(HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept((b) -> { });

            asyncPushRequests.add(pushcf);
        };
    }

    @Test
    void testSendOfModelProtectedResource() throws IOException, InterruptedException {

        final HttpRequest request = HttpRequest.newBuilder()
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

        final HttpRequest request = HttpRequest.newBuilder()
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
    void testSendOfModelAsync() throws IOException,
            InterruptedException, ExecutionException {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.get("http_uri") + "/example"))
                .header("Accept", "text/turtle")
                .GET()
                .build();

        final var asyncResponse = client.sendAsync(request, JenaBodyHandlers.ofModel());

        final int statusCode = asyncResponse.thenApply(HttpResponse::statusCode).join();
        assertEquals(200, statusCode);

        final var responseBody = asyncResponse.thenApply(HttpResponse::body).join();
        assertEquals(7, responseBody.size());
        assertTrue(responseBody.contains(
            null,
            null,
            ResourceFactory.createResource("http://example.test//settings/prefs.ttl"))
        );
    }

    @Test
    void testSendOfModelAsyncWithPromise() throws IOException, InterruptedException {

        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.get("http_uri") + "/example"))
                .GET()
                .build();

        final var asyncResponse = client.sendAsync(request, JenaBodyHandlers.ofModel(), pushPromiseHandler());

        final var statusCode = asyncResponse.thenApply(HttpResponse::statusCode).join();
        assertEquals(200, statusCode);

        final var responseBody = asyncResponse.thenApply(HttpResponse::body).join();
        assertEquals(7, responseBody.size());
        assertTrue(responseBody.contains(
            null,
            null,
            ResourceFactory.createResource("http://example.test//settings/prefs.ttl"))
        );

        pushPromisesMap.forEach(CompletableFuture::join);
        assertEquals(0, pushPromisesMap.size());

    }

    private static PushPromiseHandler<Model> pushPromiseHandler() {
        return (HttpRequest initiatingRequest,
            HttpRequest pushPromiseRequest,
            Function<HttpResponse.BodyHandler<Model>,
            CompletableFuture<HttpResponse<Model>>> acceptor) -> {
                final CompletableFuture<Void> pushcf =
                    acceptor.apply(JenaBodyHandlers.ofModel())
                        .thenApply(HttpResponse::body)
                        .thenAccept(body -> { });

                pushPromisesMap.add(pushcf);
            };
    }

    @Test
    void testOfModelPublisher() throws IOException, InterruptedException {
        final var model = ModelFactory.createDefaultModel();

        model.add(
            model.createResource("http://example.test/s"),
            model.createProperty("http://example.test/p"),
            model.createLiteral("object")
        );

        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.get("http_uri") + "/postOneTriple"))
                .header("Content-Type", "text/turtle")
                .POST(JenaBodyPublishers.ofModel(model))
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        final var response = client.sendAsync(request, HttpResponse.BodyHandlers.discarding()).join();

        assertEquals(401, response.statusCode());
        assertEquals(Optional.of("UMA ticket=\"ticket-12345\", as_uri=\"" + config.get("http_uri") + "\""),
                response.headers().firstValue("WWW-Authenticate"));
    }

    @Test
    void testSendRequestImage() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.get("http_uri") + "/solid.png"))
                .GET()
                .build();

        final var response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

        assertEquals(200, response.statusCode());
    }
}
