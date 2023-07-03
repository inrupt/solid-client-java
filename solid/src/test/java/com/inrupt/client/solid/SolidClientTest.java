/*
 * Copyright Inrupt Inc.
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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

import com.inrupt.client.ClientProvider;
import com.inrupt.client.Headers;
import com.inrupt.client.Response;
import com.inrupt.client.auth.Session;
import com.inrupt.client.spi.RDFFactory;
import com.inrupt.client.util.URIBuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;
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
    void testCustomClient() throws Exception {

        final SolidClient customClient = SolidClient.getClientBuilder()
            .headers(Headers.of(Collections.singletonMap("User-Agent", Arrays.asList("TestClient/1.0")))).build();

        final URI uri = URI.create(config.get("solid_resource_uri") + "/custom-agent");
        final URI song1 = URI.create("https://library.test/12345/song1.mp3");
        final URI song2 = URI.create("https://library.test/12345/song2.mp3");

        customClient.read(uri, Playlist.class).thenAccept(playlist -> {
            try (final Playlist p = playlist) {
                assertEquals(uri, p.getIdentifier());
                assertEquals("My playlist", p.getTitle());
                assertEquals(2, p.getSongs().size());
                assertTrue(p.getSongs().contains(song1));
                assertTrue(p.getSongs().contains(song2));
                assertTrue(p.validate().isValid());

                assertDoesNotThrow(customClient.create(p).toCompletableFuture()::join);
                assertDoesNotThrow(customClient.update(p).toCompletableFuture()::join);
                assertDoesNotThrow(customClient.delete(p).toCompletableFuture()::join);
                assertDoesNotThrow(customClient.delete(p.getIdentifier()).toCompletableFuture()::join);
            }
        }).toCompletableFuture().join();
    }

    @Test
    void testCustomHeaders() throws Exception {

        final URI uri = URI.create(config.get("solid_resource_uri") + "/custom-agent");
        final URI song1 = URI.create("https://library.test/12345/song1.mp3");
        final URI song2 = URI.create("https://library.test/12345/song2.mp3");
        final Headers headers = Headers.of(Collections.singletonMap("User-Agent", Arrays.asList("TestClient/1.0")));

        client.read(uri, headers, Playlist.class).thenAccept(playlist -> {
            try (final Playlist p = playlist) {
                assertEquals(uri, p.getIdentifier());
                assertEquals("My playlist", p.getTitle());
                assertEquals(2, p.getSongs().size());
                assertTrue(p.getSongs().contains(song1));
                assertTrue(p.getSongs().contains(song2));
                assertTrue(p.validate().isValid());

                assertDoesNotThrow(client.create(p, headers).toCompletableFuture()::join);
                assertDoesNotThrow(client.update(p, headers).toCompletableFuture()::join);
                assertDoesNotThrow(client.delete(p, headers).toCompletableFuture()::join);
                assertDoesNotThrow(client.delete(p.getIdentifier(), headers).toCompletableFuture()::join);
            }
        }).toCompletableFuture().join();
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
                assertDoesNotThrow(client.delete(p.getIdentifier()).toCompletableFuture()::join);
            }
        }).toCompletableFuture().join();
    }

    @Test
    void testGetResource() throws IOException, InterruptedException {
        final URI uri = URI.create(config.get("solid_resource_uri") + "/playlist");

        client.read(uri, SolidRDFSource.class).thenAccept(resource -> {
            try (final SolidRDFSource r = resource) {
                assertEquals(uri, r.getIdentifier());
                assertEquals(4, r.size());
                assertEquals(2, r.stream(Optional.empty(), rdf.createIRI(uri.toString()),
                            rdf.createIRI("https://example.com/song"), null).count());

                assertDoesNotThrow(client.create(r).toCompletableFuture()::join);
                assertDoesNotThrow(client.update(r).toCompletableFuture()::join);
                assertDoesNotThrow(client.delete(r).toCompletableFuture()::join);
                assertDoesNotThrow(client.delete(r.getIdentifier()).toCompletableFuture()::join);
            }
        }).toCompletableFuture().join();

    }

    @Test
    void testGetContainer() throws IOException, InterruptedException {
        final URI uri = URI.create(config.get("solid_resource_uri") + "/playlist");

        client.read(uri, SolidContainer.class).thenAccept(container -> {
            try (final SolidContainer c = container) {
                assertEquals(uri, c.getIdentifier());
                assertEquals(0, c.getResources().size());
                assertEquals(4, c.size());
                assertEquals(2, c.stream(Optional.empty(), rdf.createIRI(uri.toString()),
                            rdf.createIRI("https://example.com/song"), null).count());

                @SuppressWarnings("deprecation")
                final long count = c.getContainedResources().count();
                assertEquals(0, count);

                assertDoesNotThrow(client.update(c).toCompletableFuture()::join);
                assertDoesNotThrow(client.create(c).toCompletableFuture()::join);
                assertDoesNotThrow(client.delete(c).toCompletableFuture()::join);
                assertDoesNotThrow(client.delete(c.getIdentifier()).toCompletableFuture()::join);
            }
        }).toCompletableFuture().join();

    }

    @Test
    void testGetBinaryUpdate() {
        final URI uri = URI.create(config.get("solid_resource_uri") + "/binary");

        client.read(uri, SolidNonRDFSource.class).thenAccept(binary -> {
            try (final SolidNonRDFSource b = binary) {
                assertEquals(uri, b.getIdentifier());
                assertEquals("text/plain", b.getContentType());

                assertDoesNotThrow(client.update(b).toCompletableFuture()::join);
                assertDoesNotThrow(client.delete(b).toCompletableFuture()::join);
                assertDoesNotThrow(client.delete(b.getIdentifier()).toCompletableFuture()::join);
            }
        }).toCompletableFuture().join();
    }

    @Test
    void testGetBinaryCreate() {
        final URI uri = URI.create(config.get("solid_resource_uri") + "/binary");

        client.read(uri, SolidNonRDFSource.class).thenAccept(binary -> {
            try (final SolidNonRDFSource b = binary) {
                assertEquals(uri, b.getIdentifier());
                assertEquals("text/plain", b.getContentType());

                assertDoesNotThrow(client.create(b).toCompletableFuture()::join);
                assertDoesNotThrow(client.delete(b).toCompletableFuture()::join);
                assertDoesNotThrow(client.delete(b.getIdentifier()).toCompletableFuture()::join);
            }
        }).toCompletableFuture().join();
    }

    @Test
    void testSolidContainer() {
        final URI uri = URI.create(config.get("solid_resource_uri") + "/container/");
        final Set<URI> expected = new HashSet<>();
        expected.add(URIBuilder.newBuilder(uri).path("newContainer/").build());
        expected.add(URIBuilder.newBuilder(uri).path("test.txt").build());
        expected.add(URIBuilder.newBuilder(uri).path("test2.txt").build());

        client.read(uri, SolidContainer.class).thenAccept(container -> {
            try (final SolidContainer c = container) {
                assertEquals(expected,
                        c.getResources().stream().map(SolidResource::getIdentifier).collect(Collectors.toSet()));
            }
        }).toCompletableFuture().join();
    }

    @Test
    void testBinaryCreate() throws IOException {
        final URI uri = URI.create(config.get("solid_resource_uri") + "/binary");
        final InputStream entity = new ByteArrayInputStream("This is a plain text document.".getBytes(UTF_8));

        final SolidNonRDFSource binary = new SolidNonRDFSource(uri, "text/plain", entity);
        assertDoesNotThrow(client.create(binary).toCompletableFuture()::join);
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
    <T extends SolidClientException> void testExceptionalResources(
            final URI uri,
            final int expectedStatusCode,
            final Class<T> clazz) {

        final CompletableFuture<Recipe> future =
            client.read(uri, Recipe.class)
            .toCompletableFuture();
        final CompletionException err = assertThrows(CompletionException.class, () ->
            future.join());
        assertTrue(err.getCause() instanceof SolidClientException);
        assertInstanceOf(clazz, err.getCause());
        final SolidClientException ex = (SolidClientException) err.getCause();
        assertEquals(expectedStatusCode, ex.getStatusCode());
        assertEquals(uri, ex.getUri());
        assertEquals(Optional.of("application/json"), ex.getHeaders().firstValue("Content-Type"));
        assertNotNull(ex.getBody());
    }

    private static Stream<Arguments> testExceptionalResources() {
        return Stream.of(
                Arguments.of(
                    URI.create(config.get("solid_resource_uri") + "/unauthorized"), 401,
                        UnauthorizedException.class),
                Arguments.of(
                    URI.create(config.get("solid_resource_uri") + "/forbidden"), 403,
                        ForbiddenException.class),
                Arguments.of(
                    URI.create(config.get("solid_resource_uri") + "/missing"), 404,
                        NotFoundException.class));
    }

    @ParameterizedTest
    @MethodSource
    <T extends SolidClientException> void testSpecialisedExceptions(
            final Class<T> clazz,
            final int statusCode
    ) {
        final Headers headers = Headers.of(Collections.singletonMap("x-key", Arrays.asList("value")));
        final SolidClient solidClient = new SolidClient(ClientProvider.getClient(), headers, false);
        final SolidContainer resource = new SolidContainer(URI.create("http://example.com"), null, null);

        final SolidClientException exception = assertThrows(
                clazz,
                () -> solidClient.handleResponse(resource, headers, "message")
                        .apply(new Response<byte[]>() {
                            @Override
                            public byte[] body() {
                                return new byte[0];
                            }

                            @Override
                            public Headers headers() {
                                return null;
                            }

                            @Override
                            public URI uri() {
                                return null;
                            }

                            @Override
                            public int statusCode() {
                                return statusCode;
                            }
                        })
        );
        assertEquals(statusCode, exception.getStatusCode());
    }

    private static Stream<Arguments> testSpecialisedExceptions() {
        return Stream.of(
                Arguments.of(BadRequestException.class, 400),
                Arguments.of(UnauthorizedException.class, 401),
                Arguments.of(ForbiddenException.class, 403),
                Arguments.of(NotFoundException.class, 404),
                Arguments.of(MethodNotAllowedException.class, 405),
                Arguments.of(NotAcceptableException.class, 406),
                Arguments.of(ConflictException.class, 409),
                Arguments.of(GoneException.class, 410),
                Arguments.of(PreconditionFailedException.class, 412),
                Arguments.of(UnsupportedMediaTypeException.class, 415),
                Arguments.of(TooManyRequestsException.class, 429),
                Arguments.of(InternalServerErrorException.class, 500),
                Arguments.of(SolidClientException.class, 418)
        );
    }
}
