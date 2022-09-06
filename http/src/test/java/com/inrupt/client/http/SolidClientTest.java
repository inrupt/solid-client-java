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
import com.inrupt.client.rdf4j.RDF4JBodyHandlers;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpResponse.PushPromiseHandler;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.function.Supplier;
import org.apache.hc.client5.http.impl.Operations.CompletedFuture;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SolidClientTest {

    private final SolidClient client = SolidClient.Builder.newBuilder().authenticator(new SolidAuthenticator()).build();
    private static final MockHttpServer mockHttpServer = new MockHttpServer();
    private static final Map<String, String> config = new HashMap<>();
    private static List<CompletableFuture<Void>> pushPromisesMap = new ArrayList<CompletableFuture<Void>>();

    @BeforeAll
    static void setup() {
        config.putAll(mockHttpServer.start());
    }

    @AfterAll
    static void teardown() {
        mockHttpServer.stop();
    }

    @Test
    void testSend() throws IOException, InterruptedException {

        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.get("http_uri") + "/oneTriple"))
                .header("Authorization", getBasicAuthenticationHeader("username", "password"))
                .GET()
                .build();

        final var response = client.send(request, RDF4JBodyHandlers.ofModel());

        assertEquals(200, response.statusCode());

    }

    @Test
    void testSendAsync() throws IOException, InterruptedException {

        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.get("http_uri") + "/oneTriple"))
                .GET()
                .build();

        final var asyncResponse = client.sendAsync(request, RDF4JBodyHandlers.ofModel());

        final var statusCode = asyncResponse.thenApply(HttpResponse::statusCode).join();
        assertEquals(200, statusCode);

        final var responseBody = asyncResponse.thenApply(HttpResponse::body).join().get();
        assertEquals(1, responseBody.size());
        assertTrue(responseBody.contains(
            (Resource)SimpleValueFactory.getInstance().createIRI("http://example.com/s"),
            null,
            null,
            (Resource)null)
        );
    }

    @Test
    void testSendAsyncWithPriomise() throws IOException, InterruptedException {

        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.get("http_uri") + "/oneTriple"))
                .GET()
                .version(HttpClient.Version.HTTP_2)
                .build();

        final var asyncResponse = client.sendAsync(request, RDF4JBodyHandlers.ofModel(), pushPromiseHandler());

        //final var statusCode = asyncResponse.thenApply(HttpResponse::statusCode).join();
        //assertEquals(200, statusCode);
        
        pushPromisesMap.forEach(CompletableFuture::join);
        assertEquals(1, pushPromisesMap.size());

    }

    private static PushPromiseHandler<Model> pushPromiseHandler() {
    return (HttpRequest initiatingRequest, 
        HttpRequest pushPromiseRequest, 
        Function<HttpResponse.BodyHandler<Model>, 
        CompletableFuture<HttpResponse<Model>>> acceptor) -> {
            CompletableFuture<Void> pushcf =
            acceptor.apply(RDF4JBodyHandlers.ofModel())
            .thenApply(HttpResponse::body)
            .thenAccept(body -> {});

            pushPromisesMap.add(pushcf);

    };
}

    private static final String getBasicAuthenticationHeader(String username, String password) {
        String valueToEncode = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());
    }
}
