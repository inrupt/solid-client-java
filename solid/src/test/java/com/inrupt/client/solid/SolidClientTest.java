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
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.inrupt.client.*;
import com.inrupt.client.auth.Session;
import com.inrupt.client.jackson.JacksonService;
import com.inrupt.client.spi.JsonService;
import com.inrupt.client.spi.RDFFactory;
import com.inrupt.client.util.URIBuilder;
import com.inrupt.client.vocabulary.PIM;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

    private static final String TEXT_PLAIN = "text/plain";
    private static final SolidMockHttpService mockHttpServer = new SolidMockHttpService();
    private static final Map<String, String> config = new HashMap<>();
    private static final RDF rdf = RDFFactory.getInstance();
    private static final SolidClient client = SolidClient.getClient().session(Session.anonymous());
    private static final JsonService jsonService = new JacksonService();

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

                assertEquals(Optional.of("user=\"read write\",public=\"read\""),
                        p.getHeaders().firstValue("WAC-Allow"));
                assertEquals(Optional.of("user=\"read write\",public=\"read\""),
                        p.getHeaders().firstValue("wac-allow"));
                assertTrue(p.getHeaders().allValues("Link")
                        .contains("<http://storage.example/>; rel=\"" + PIM.storage + "\""));

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
        final URI uri = URI.create(config.get("solid_resource_uri") + "/playlists/");

        client.read(uri, SolidContainer.class).thenAccept(container -> {
            try (final SolidContainer c = container) {
                assertEquals(uri, c.getIdentifier());
                assertEquals(0, c.getResources().size());
                assertEquals(4, c.size());
                assertEquals(2, c.stream(Optional.empty(), rdf.createIRI(uri.toString()),
                            rdf.createIRI("https://example.com/song"), null).count());

                assertEquals(Optional.of("user=\"read write\",public=\"read\""),
                        c.getHeaders().firstValue("WAC-Allow"));
                assertEquals(Optional.of("user=\"read write\",public=\"read\""),
                        c.getHeaders().firstValue("wac-allow"));
                assertTrue(c.getHeaders().allValues("Link")
                        .contains("<http://storage.example/>; rel=\"" + PIM.storage + "\""));

                assertDoesNotThrow(client.update(c).toCompletableFuture()::join);
                assertDoesNotThrow(client.create(c).toCompletableFuture()::join);
                assertDoesNotThrow(client.delete(c).toCompletableFuture()::join);
                assertDoesNotThrow(client.delete(c.getIdentifier()).toCompletableFuture()::join);
            }
        }).toCompletableFuture().join();

    }

    @Test
    void testGetDeprecatedBinaryFetch() {
        final URI uri = URI.create(config.get("solid_resource_uri") + "/binary");

        client.read(uri, DeprecatedBinary.class).thenAccept(binary -> {
            try (final DeprecatedBinary b = binary) {
                assertEquals(uri, b.getIdentifier());
                assertEquals(TEXT_PLAIN, b.getContentType());

                assertTrue(b.getHeaders().asMap().isEmpty());
            }
        }).toCompletableFuture().join();
    }

    @Test
    void testGetBinaryFetch() {
        final URI uri = URI.create(config.get("solid_resource_uri") + "/binary");

        client.read(uri, BasicBinary.class).thenAccept(binary -> {
            try (final BasicBinary b = binary) {
                assertEquals(uri, b.getIdentifier());
                assertEquals(TEXT_PLAIN, b.getContentType());

                assertTrue(b.getHeaders().asMap().isEmpty());
            }
        }).toCompletableFuture().join();
    }

    @Test
    void testGetBinaryUpdate() {
        final URI uri = URI.create(config.get("solid_resource_uri") + "/binary");

        client.read(uri, SolidNonRDFSource.class).thenAccept(binary -> {
            try (final SolidNonRDFSource b = binary) {
                assertEquals(uri, b.getIdentifier());
                assertEquals(TEXT_PLAIN, b.getContentType());

                assertEquals(Optional.of(TEXT_PLAIN), b.getHeaders().firstValue("content-type"));

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
                assertEquals(TEXT_PLAIN, b.getContentType());

                assertEquals(Optional.of(TEXT_PLAIN), b.getHeaders().firstValue("Content-Type"));
                assertEquals(Optional.of(TEXT_PLAIN), b.getHeaders().firstValue("content-type"));

                assertDoesNotThrow(client.create(b).toCompletableFuture()::join);
                assertDoesNotThrow(client.delete(b).toCompletableFuture()::join);
                assertDoesNotThrow(client.delete(b.getIdentifier()).toCompletableFuture()::join);
            }
        }).toCompletableFuture().join();
    }

    @Test
    void testSolidContainerWithInvalidData() {
        final URI uri = URI.create(config.get("solid_resource_uri") + "/container/");
        final CompletionException err = assertThrows(CompletionException.class,
                client.read(uri, SolidContainer.class).toCompletableFuture()::join);
        assertInstanceOf(DataMappingException.class, err.getCause());
    }

    @Test
    void testLowLevelSolidContainer() {
        final URI uri = URI.create(config.get("solid_resource_uri") + "/container/");

        final Set<URI> expected = new HashSet<>();
        expected.add(URIBuilder.newBuilder(uri).path("newContainer/").build());
        expected.add(URIBuilder.newBuilder(uri).path("test.txt").build());
        expected.add(URIBuilder.newBuilder(uri).path("test2.txt").build());
        expected.add(URIBuilder.newBuilder(uri).path("test3").build());
        expected.add(URIBuilder.newBuilder(uri).path("test4").build());

        client.send(Request.newBuilder(uri).build(), SolidResourceHandlers.ofSolidContainer())
            .thenAccept(response -> {
                final SolidContainer container = response.body();
                assertEquals(expected, container.getResources().stream()
                        .map(SolidResource::getIdentifier).collect(Collectors.toSet()));
            }).toCompletableFuture().join();
    }

    @Test
    void testBinaryCreate() throws IOException {
        final URI uri = URI.create(config.get("solid_resource_uri") + "/binary");
        final InputStream entity = new ByteArrayInputStream("This is a plain text document.".getBytes(UTF_8));

        final SolidNonRDFSource binary = new SolidNonRDFSource(uri, TEXT_PLAIN, entity);
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
                assertEquals(Optional.of("POST, PUT, PATCH"), r.getHeaders().firstValue("allow"));
            }
        })
        .toCompletableFuture().join();
    }

    @Test
    void testGetDeprecatedType() {
        final URI uri = URI.create(config.get("solid_resource_uri") + "/recipe");

        client.read(uri, DeprecatedType.class).thenAccept(recipe -> {
            try (final DeprecatedType r = recipe) {
                assertEquals(uri, r.getIdentifier());
                assertEquals("Molasses Cookies", r.getTitle());
                assertEquals(11, r.getIngredients().size());
                assertEquals(7, r.getSteps().size());
                assertFalse(r.getHeaders().firstValue("allow").isPresent());
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
            arguments(URI.create(config.get("solid_resource_uri") + "/unauthorized"), 401, UnauthorizedException.class),
            arguments(URI.create(config.get("solid_resource_uri") + "/forbidden"), 403, ForbiddenException.class),
            arguments(URI.create(config.get("solid_resource_uri") + "/missing"), 404, NotFoundException.class)
        );
    }

    @ParameterizedTest
    @MethodSource
    <T extends SolidClientException> void testLegacyExceptions(
            final Class<T> clazz,
            final int statusCode
    ) {
        final Headers headers = Headers.of(Collections.singletonMap("x-key", Arrays.asList("value")));
        final SolidClient solidClient = new SolidClient(ClientProvider.getClient(), headers, false);
        final SolidContainer resource = new SolidContainer(URI.create("http://example.com"));

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
        // The following assertions check that in absence of an RFC9457 compliant response, we properly initialize the
        // default values for the attached Problem Details.
        assertEquals(ProblemDetails.DEFAULT_TYPE, exception.getProblemDetails().getType().toString());
        assertEquals(statusCode, exception.getProblemDetails().getStatus());
        assertNull(exception.getProblemDetails().getTitle());
        assertNull(exception.getProblemDetails().getDetails());
        assertNull(exception.getProblemDetails().getInstance());
    }

    private static Stream<Arguments> testLegacyExceptions() {
        return Stream.of(
            arguments(BadRequestException.class, 400),
            arguments(UnauthorizedException.class, 401),
            arguments(ForbiddenException.class, 403),
            arguments(NotFoundException.class, 404),
            arguments(MethodNotAllowedException.class, 405),
            arguments(NotAcceptableException.class, 406),
            arguments(ConflictException.class, 409),
            arguments(GoneException.class, 410),
            arguments(PreconditionFailedException.class, 412),
            arguments(UnsupportedMediaTypeException.class, 415),
            arguments(TooManyRequestsException.class, 429),
            arguments(InternalServerErrorException.class, 500),
            arguments(SolidClientException.class, 418),
            arguments(SolidClientException.class,599),
            arguments(SolidClientException.class,600)
        );
    }

    @ParameterizedTest
    @MethodSource
    <T extends SolidClientException> void testRfc9457Exceptions(
            final Class<T> clazz,
            final ProblemDetails problemDetails
    ) {
        final Headers headers = Headers.of(Collections.singletonMap("x-key", Arrays.asList("value")));
        final SolidClient solidClient = new SolidClient(ClientProvider.getClient(), headers, false);
        final SolidContainer resource = new SolidContainer(URI.create("http://example.com"));

        final SolidClientException exception = assertThrows(
                clazz,
                () -> solidClient.handleResponse(resource, headers, "message")
                        .apply(new Response<byte[]>() {
                            @Override
                            public byte[] body() {
                                try (ByteArrayOutputStream bos = new ByteArrayOutputStream()){
                                    jsonService.toJson(problemDetails, bos);
                                    return bos.toByteArray();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }

                            @Override
                            public Headers headers() {
                                final List<String> headerValues = new ArrayList<>();
                                headerValues.add("application/problem+json");
                                final Map<String, List<String>> headerMap = new HashMap<>();
                                headerMap.put("Content-Type", headerValues);
                                return Headers.of(headerMap);
                            }

                            @Override
                            public URI uri() {
                                return null;
                            }

                            @Override
                            public int statusCode() {
                                return problemDetails.getStatus();
                            }
                        })
        );
        assertEquals(problemDetails.getStatus(), exception.getStatusCode());
        assertEquals(problemDetails.getType(), exception.getProblemDetails().getType());
        assertEquals(problemDetails.getTitle(), exception.getProblemDetails().getTitle());
        assertEquals(problemDetails.getStatus(), exception.getProblemDetails().getStatus());
        assertEquals(problemDetails.getDetails(), exception.getProblemDetails().getDetails());
        assertEquals(problemDetails.getInstance(), exception.getProblemDetails().getInstance());
    }

    private static ProblemDetails mockProblemDetails(final String title, final String details, final int status) {
        return new ProblemDetails(URI.create("https://example.org/type"),
            title,
            details,
            status,
            URI.create("https://example.org/instance")
        );
    }

    private static Stream<Arguments> testRfc9457Exceptions() {
        return Stream.of(
                arguments(
                    BadRequestException.class,
                    mockProblemDetails("Bad Request", "Some details", 400)
                ),
                arguments(
                    UnauthorizedException.class,
                    mockProblemDetails("Unauthorized", "Some details", 401)
                ),
                arguments(
                    ForbiddenException.class,
                    mockProblemDetails("Forbidden", "Some details", 403)
                ),
                arguments(
                    NotFoundException.class,
                    mockProblemDetails("Not Found", "Some details", 404)
                ),
                arguments(
                    MethodNotAllowedException.class,
                    mockProblemDetails("Method Not Allowed", "Some details", 405)
                ),
                arguments(
                    NotAcceptableException.class,
                    mockProblemDetails("Not Acceptable", "Some details", 406)
                ),
                arguments(
                    ConflictException.class,
                    mockProblemDetails("Conflict", "Some details", 409)
                ),
                arguments(
                    GoneException.class,
                    mockProblemDetails("Gone", "Some details", 410)
                ),
                arguments(
                    PreconditionFailedException.class,
                    mockProblemDetails("Precondition Failed", "Some details", 412)
                ),
                arguments(
                    UnsupportedMediaTypeException.class,
                    mockProblemDetails("Unsupported Media Type", "Some details", 415)
                ),
                arguments(
                    TooManyRequestsException.class,
                    mockProblemDetails("Too Many Requests", "Some details", 429)
                ),
                arguments(
                    InternalServerErrorException.class,
                    mockProblemDetails("Internal Server Error", "Some details", 500)
                ),
                arguments(
                    // Custom errors that do not map to a predefined Exception class
                    // default to the generic SolidClientException
                    SolidClientException.class,
                    mockProblemDetails("I'm a Teapot", "Some details", 418)
                ),
                arguments(
                    // Custom errors that do not map to a predefined Exception class
                    // default to the generic SolidClientException.
                    SolidClientException.class,
                    mockProblemDetails("Custom server error", "Some details", 599)
                )
        );
    }

    @Test
    void testMalformedProblemDetails() {
        // The specific error code is irrelevant to this test.
        final int statusCode = 400;
        final Headers headers = Headers.of(Collections.singletonMap("x-key", Arrays.asList("value")));
        final SolidClient solidClient = new SolidClient(ClientProvider.getClient(), headers, false);
        final SolidContainer resource = new SolidContainer(URI.create("http://example.com"));

        final SolidClientException exception = assertThrows(
                BadRequestException.class,
                () -> solidClient.handleResponse(resource, headers, "message")
                        .apply(new Response<byte[]>() {
                            // Pretend we return RFC9457 content...
                            @Override
                            public Headers headers() {
                                final List<String> headerValues = new ArrayList<>();
                                headerValues.add("application/problem+json");
                                final Map<String, List<String>> headerMap = new HashMap<>();
                                headerMap.put("Content-Type", headerValues);
                                return Headers.of(headerMap);
                            }

                            // ... but actually return malformed JSON.
                            @Override
                            public byte[] body() {
                                return "This isn't valid application/problem+json.".getBytes();
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
        // On malformed response, the ProblemDetails should fall back to defaults.
        assertEquals(ProblemDetails.DEFAULT_TYPE, exception.getProblemDetails().getType().toString());
        assertNull(exception.getProblemDetails().getTitle());
        assertEquals(statusCode, exception.getProblemDetails().getStatus());
        assertNull(exception.getProblemDetails().getDetails());
        assertNull(exception.getProblemDetails().getInstance());
    }

    @Test
    void testMinimalProblemDetails() {
        // The specific error code is irrelevant to this test.
        final int statusCode = 400;
        final Headers headers = Headers.of(Collections.singletonMap("x-key", Arrays.asList("value")));
        final SolidClient solidClient = new SolidClient(ClientProvider.getClient(), headers, false);
        final SolidContainer resource = new SolidContainer(URI.create("http://example.com"));

        final SolidClientException exception = assertThrows(
                BadRequestException.class,
                () -> solidClient.handleResponse(resource, headers, "message")
                        .apply(new Response<byte[]>() {
                            @Override
                            public Headers headers() {
                                final List<String> headerValues = new ArrayList<>();
                                headerValues.add("application/problem+json");
                                final Map<String, List<String>> headerMap = new HashMap<>();
                                headerMap.put("Content-Type", headerValues);
                                return Headers.of(headerMap);
                            }

                            // Return minimal problem details..
                            @Override
                            public byte[] body() {
                                return "{\"status\":400}".getBytes();
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
        // On malformed response, the ProblemDetails should fall back to defaults.
        assertEquals(ProblemDetails.DEFAULT_TYPE, exception.getProblemDetails().getType().toString());
        assertNull(exception.getProblemDetails().getTitle());
        assertEquals(statusCode, exception.getProblemDetails().getStatus());
        assertNull(exception.getProblemDetails().getDetails());
        assertNull(exception.getProblemDetails().getInstance());
    }
}
