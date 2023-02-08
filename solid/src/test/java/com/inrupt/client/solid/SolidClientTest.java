/*
 * Copyright 2023 Inrupt Inc.
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
package com.inrupt.client.solid;

import static org.junit.jupiter.api.Assertions.*;

import com.inrupt.client.auth.Session;
import com.inrupt.client.spi.RDFFactory;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Stream;

import org.apache.commons.rdf.api.RDF;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SolidClientTest {

    private static final SolidMockHttpService mockHttpServer = new SolidMockHttpService();
    private static final Map<String, String> config = new HashMap<>();
    private static final RDF rdf = RDFFactory.getInstance();
    private static final SolidClient client = SolidClient.getClient().session(Session.anonymous());

    @BeforeAll
    static void setup() {
        config.putAll(mockHttpServer.start());
    }

    @AfterAll
    static void teardown() {
        mockHttpServer.stop();
    }

    @Test
    void testGetPlaylist() throws IOException, InterruptedException {
        final URI uri = URI.create(config.get("solid_resource_uri") + "/playlist");
        final URI song1 = URI.create("https://library.test/12345/song1.mp3");
        final URI song2 = URI.create("https://library.test/12345/song2.mp3");

        client.read(uri, Playlist.class).thenAccept(playlist -> {
            try (final Playlist p = playlist) {
                assertEquals(uri, p.getIdentifier());
                assertEquals("My playlist", p.getTitle());
                assertEquals(2, p.getSongs().size());
                assertTrue(p.getSongs().contains(song1));
                assertTrue(p.getSongs().contains(song2));
                assertTrue(p.validate().isValid());

                assertDoesNotThrow(client.create(p).toCompletableFuture()::join);
                assertDoesNotThrow(client.update(p).toCompletableFuture()::join);
                assertDoesNotThrow(client.delete(p).toCompletableFuture()::join);
            }
        }).toCompletableFuture().join();
    }

    @Test
    void testGetResource() throws IOException, InterruptedException {
        final URI uri = URI.create(config.get("solid_resource_uri") + "/playlist");

        client.read(uri, SolidResource.class).thenAccept(resource -> {
            try (final SolidResource r = resource) {
                assertEquals(uri, r.getIdentifier());
                assertEquals(4, r.size());
                assertEquals(2, r.stream(Optional.empty(), rdf.createIRI(uri.toString()),
                            rdf.createIRI("https://example.com/song"), null).count());

                assertDoesNotThrow(client.create(r).toCompletableFuture()::join);
                assertDoesNotThrow(client.update(r).toCompletableFuture()::join);
                assertDoesNotThrow(client.delete(r).toCompletableFuture()::join);
            }
        }).toCompletableFuture().join();

    }

    @Test
    void testGetContainer() throws IOException, InterruptedException {
        final URI uri = URI.create(config.get("solid_resource_uri") + "/playlist");

        client.read(uri, SolidContainer.class).thenAccept(container -> {
            try (final SolidContainer c = container) {
                assertEquals(uri, c.getIdentifier());
                assertEquals(0, c.getContainedResources().count());
                assertEquals(4, c.size());
                assertEquals(2, c.stream(Optional.empty(), rdf.createIRI(uri.toString()),
                            rdf.createIRI("https://example.com/song"), null).count());

                assertDoesNotThrow(client.update(c).toCompletableFuture()::join);
                assertDoesNotThrow(client.create(c).toCompletableFuture()::join);
                assertDoesNotThrow(client.delete(c).toCompletableFuture()::join);
            }
        }).toCompletableFuture().join();

    }

    @Test
    void testGetInvalidType() {
        final URI uri = URI.create(config.get("solid_resource_uri") + "/playlist");
        final CompletionException err1 = assertThrows(CompletionException.class,
                client.read(uri, InvalidType.class).toCompletableFuture()::join);
        assertTrue(err1.getCause() instanceof SolidResourceException);

        final InvalidType type = new InvalidType(uri);
        assertThrows(SolidResourceException.class, () -> client.update(type));
    }

    @Test
    void testGetRecipeType() {
        final URI uri = URI.create(config.get("solid_resource_uri") + "/recipe");

        client.read(uri, Recipe.class).thenAccept(recipe -> {
            try (final Recipe r = recipe) {
                assertEquals(uri, r.getIdentifier());
                assertEquals("Molasses Cookies", r.getTitle());
                assertEquals(11, r.getIngredients().size());
                assertEquals(7, r.getSteps().size());
            }
        })
        .toCompletableFuture().join();
    }

    @ParameterizedTest
    @MethodSource
    void testExceptionalResources(final URI uri, final int expectedStatusCode) {

        final CompletableFuture<Recipe> future =
            client.read(uri, Recipe.class)
            .toCompletableFuture();
        final CompletionException err = assertThrows(CompletionException.class, () ->
            future.join());
        assertTrue(err.getCause() instanceof SolidClientException);
        final SolidClientException ex = (SolidClientException) err.getCause();
        assertEquals(expectedStatusCode, ex.getStatusCode());
        assertEquals(uri, ex.getUri());
        assertEquals(Optional.of("application/json"), ex.getHeaders().firstValue("Content-Type"));
        assertNotNull(ex.getBody());
    }

    private static Stream<Arguments> testExceptionalResources() {
        return Stream.of(
                Arguments.of(
                    URI.create(config.get("solid_resource_uri") + "/unauthorized"), 401),
                Arguments.of(
                    URI.create(config.get("solid_resource_uri") + "/forbidden"), 403),
                Arguments.of(
                    URI.create(config.get("solid_resource_uri") + "/missing"), 404));
    }
}
