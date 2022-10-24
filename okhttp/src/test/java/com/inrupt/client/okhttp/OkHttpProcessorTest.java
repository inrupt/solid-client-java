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
package com.inrupt.client.okhttp;

import static org.junit.jupiter.api.Assertions.*;

import com.inrupt.client.Request;
import com.inrupt.client.Response;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class OkHttpProcessorTest {

    private static final MockHttpServer mockHttpServer = new MockHttpServer();
    private static final Map<String, String> config = new HashMap<>();
    private static final OkHttpProcessor client = new OkHttpProcessor();

    @BeforeAll
    static void setup() {
        config.putAll(mockHttpServer.start());
    }

    @AfterAll
    static void teardown() {
        mockHttpServer.stop();
    }

    @Test
    void testSendOfString() throws IOException {
        final URI uri = URI.create(config.get("http_uri") + "/file");
        final Request request = Request.newBuilder()
            .uri(uri)
            .GET()
            .build();

        final Response<String> response = client.send(request, Response.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals(Optional.of("text/plain"), response.headers().firstValue("content-type"));
        assertEquals(Optional.of("text/plain"), response.headers().firstValue("Content-Type"));
        assertEquals(Arrays.asList("text/plain"), response.headers().asMap().get("content-type"));
        assertEquals(Arrays.asList("text/plain"), response.headers().asMap().get("Content-Type"));
        assertEquals(uri, response.uri());
        assertTrue(response.body().contains("Julie C. Sparks and David Widger"));
    }

    @Test
    void testSendOfStringAsync() throws IOException {
        final URI uri = URI.create(config.get("http_uri") + "/file");
        final Request request = Request.newBuilder()
            .uri(uri)
            .GET()
            .build();

        final Response<String> response = client.sendAsync(request,
                Response.BodyHandlers.ofString()).toCompletableFuture().join();

        assertEquals(200, response.statusCode());
        assertEquals(uri, response.uri());
        assertEquals(Optional.of("text/plain"), response.headers().firstValue("content-type"));
        assertEquals(Optional.of("text/plain"), response.headers().firstValue("Content-Type"));
        assertEquals(Arrays.asList("text/plain"), response.headers().asMap().get("content-type"));
        assertEquals(Arrays.asList("text/plain"), response.headers().asMap().get("Content-Type"));
        assertTrue(response.body().contains("Julie C. Sparks and David Widger"));
    }

    @Test
    void testSendRequestImage() throws IOException {
        final URI uri = URI.create(config.get("http_uri") + "/solid.png");
        final Request request = Request.newBuilder()
                .uri(uri)
                .GET()
                .build();

        final Response<byte[]> response = client.send(request, Response.BodyHandlers.ofByteArray());

        assertEquals(200, response.statusCode());
        assertEquals(uri, response.uri());
        assertEquals(Optional.of("image/png"), response.headers().firstValue("content-type"));
        assertEquals(Optional.of("image/png"), response.headers().firstValue("Content-Type"));
        assertEquals(Arrays.asList("image/png"), response.headers().asMap().get("content-type"));
        assertEquals(Arrays.asList("image/png"), response.headers().asMap().get("Content-Type"));
    }

    @Test
    void testPostTriple() throws IOException {
        final URI uri = URI.create(config.get("http_uri") + "/rdf");
        final String triple = "<http://example.test/s> <http://example.test/p> \"object\" .";
        final Request request = Request.newBuilder()
                .uri(uri)
                .header("Content-Type", "text/turtle")
                .POST(Request.BodyPublishers.ofString(triple))
                .build();

        final Response<Void> response = client.send(request, Response.BodyHandlers.discarding());

        assertEquals(201, response.statusCode());
        assertEquals(uri, response.uri());
        assertFalse(response.headers().firstValue("Content-Type").isPresent());
    }

    @Test
    void testPatchTriple() throws IOException {
        final URI uri = URI.create(config.get("http_uri") + "/rdf");
        final String triple = "INSERT DATA { <http://example.test/s> <http://example.test/p> \"data\" . }";
        final Request request = Request.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/sparql-update")
                .PATCH(Request.BodyPublishers.ofString(triple))
                .build();

        final Response<Void> response = client.sendAsync(request, Response.BodyHandlers.discarding())
            .toCompletableFuture().join();

        assertEquals(204, response.statusCode());
        assertEquals(uri, response.uri());
        assertFalse(response.headers().firstValue("Content-Type").isPresent());
    }

    @Test
    void testDeleteResource() throws IOException {
        final URI uri = URI.create(config.get("http_uri") + "/rdf");
        final Request request = Request.newBuilder()
                .uri(uri)
                .DELETE()
                .build();

        final Response<Void> response = client.send(request, Response.BodyHandlers.discarding());

        assertEquals(204, response.statusCode());
        assertEquals(uri, response.uri());
        assertFalse(response.headers().firstValue("Content-Type").isPresent());
    }
}
