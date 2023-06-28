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

import static org.junit.jupiter.api.Assertions.*;

import com.inrupt.client.Request;
import com.inrupt.client.Response;
import com.inrupt.client.spi.HttpService;
import com.inrupt.client.spi.ServiceProvider;
import com.inrupt.client.util.URIBuilder;
import com.inrupt.client.vocabulary.LDP;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SolidRDFSourceTest {

    private static final SolidMockHttpService mockHttpServer = new SolidMockHttpService();
    private static Map<String, String> config = new HashMap<>();
    private static final HttpService client = ServiceProvider.getHttpService();

    @BeforeAll
    static void setup() {
        config.putAll(mockHttpServer.start());
    }

    @AfterAll
    static void teardown() {
        mockHttpServer.stop();
    }

    @Test
    void testGetOfSolidRDFSource() throws IOException, InterruptedException {
        final URI uri = URI.create(config.get("solid_resource_uri") + "/solid/");
        final Request request =
                Request.newBuilder().uri(uri).header("Accept", "text/turtle").GET().build();

        final Response<SolidRDFSource> response =
                client.send(request, SolidResourceHandlers.ofSolidRDFSource()).toCompletableFuture()
                        .join();

        assertEquals(200, response.statusCode());

        try (final SolidRDFSource resource = response.body()) {
            assertEquals(uri, resource.getIdentifier());
            assertTrue(resource.getMetadata().getType().contains(LDP.BasicContainer));
            assertTrue(resource.getMetadata().getTypes().contains(LDP.BasicContainer));
            assertEquals(Optional.of(URI.create("http://acl.example/solid/")),
                    resource.getMetadata().getAcl());
            assertEquals(Optional.of(URI.create("http://storage.example/")),
                    resource.getMetadata().getStorage());
            assertTrue(resource.getMetadata().getWacAllow().get("user")
                    .containsAll(Arrays.asList("read", "write")));
            assertEquals(Collections.singleton("read"), resource.getMetadata().getWacAllow().get("public"));
            assertEquals(13, resource.size());
            assertEquals(3, resource.getMetadata().getAllowedMethods().size());
            assertTrue(resource.getMetadata().getAllowedMethods()
                    .containsAll(Arrays.asList("PUT", "POST", "PATCH")));
            assertTrue(resource.getMetadata().getAllowedPatchSyntaxes()
                    .containsAll(Arrays.asList("application/sparql-update", "text/n3")));
            assertTrue(resource.getMetadata().getAllowedPostSyntaxes()
                    .containsAll(Arrays.asList("application/ld+json", "text/turtle")));
            assertTrue(resource.getMetadata().getAllowedPutSyntaxes()
                    .containsAll(Arrays.asList("application/ld+json", "text/turtle")));
            assertEquals("text/turtle", resource.getMetadata().getContentType());
        }
    }

    @Test
    void testCheckRootOfSolidRDFSource() throws IOException, InterruptedException {
        final URI uri = URI.create(config.get("solid_resource_uri") + "/");
        final Request request = Request.newBuilder()
            .uri(uri)
            .header("Accept", "text/turtle")
            .GET()
            .build();

        final Response<SolidRDFSource> response = client.send(
            request,
            SolidResourceHandlers.ofSolidRDFSource()
        ).toCompletableFuture().join();

        assertEquals(200, response.statusCode());

        try (final SolidRDFSource resource = response.body()) {
            assertEquals(uri, resource.getIdentifier());
            assertTrue(resource.getMetadata().getType().contains(LDP.BasicContainer));
            assertTrue(resource.getMetadata().getTypes().contains(LDP.BasicContainer));
            assertEquals(Optional.of(URI.create("http://acl.example/")),
                    resource.getMetadata().getAcl());
            assertEquals(Optional.of(uri), resource.getMetadata().getStorage());
        }
    }

    @Test
    void testGetOfSolidContainer() throws IOException, InterruptedException {
        final URI resource = URI.create(config.get("solid_resource_uri") + "/solid/");
        final Request request = Request.newBuilder()
            .uri(resource)
            .header("Accept", "text/turtle")
            .GET()
            .build();

        final Response<SolidContainer> response = client.send(
            request,
            SolidResourceHandlers.ofSolidContainer()
        ).toCompletableFuture().join();

        assertEquals(200, response.statusCode());

        try (final SolidContainer container = response.body()) {
            assertEquals(resource, container.getIdentifier());
            assertEquals(3, container.getResources().size());

            @SuppressWarnings("deprecation")
            final long count = container.getContainedResources().count();
            assertEquals(3, count);

            final Set<URI> uris = new HashSet<>();
            uris.add(URIBuilder.newBuilder(resource).path("test.txt").build());
            uris.add(URIBuilder.newBuilder(resource).path("test2.txt").build());
            uris.add(URIBuilder.newBuilder(resource).path("newContainer/").build());

            container.getResources().forEach(child ->
                assertTrue(uris.contains(child.getIdentifier())));
        }
    }

    @Test
    void testEmptyResourceBuilder() {
        final URI id = URI.create("https://resource.example/");
        try (final SolidRDFSource res = new SolidRDFSource(id, null, null)) {
            assertFalse(res.getMetadata().getStorage().isPresent());
            assertFalse(res.getMetadata().getAcl().isPresent());
            assertTrue(res.getMetadata().getAllowedPatchSyntaxes().isEmpty());
            assertEquals(0, res.size());
            assertEquals(id, res.getIdentifier());
        }
    }

    @Test
    void testEmptyContainerBuilder() {
        final URI id = URI.create("https://resource.example/");
        try (final SolidContainer res = new SolidContainer(id, null, null)) {
            assertFalse(res.getMetadata().getStorage().isPresent());
            assertFalse(res.getMetadata().getAcl().isPresent());
            assertTrue(res.getMetadata().getAllowedPatchSyntaxes().isEmpty());
            assertEquals(0, res.size());
            assertEquals(id, res.getIdentifier());
        }
    }

    @Test
    void testEmptyContentType() {
        final URI uri = URI.create(config.get("solid_resource_uri") + "/noContentType");
        final Request request =
                Request.newBuilder().uri(uri).header("Accept", "text/turtle").GET().build();

        final Response<SolidRDFSource> response =
                client.send(request, SolidResourceHandlers.ofSolidRDFSource()).toCompletableFuture()
                        .join();

        assertEquals(200, response.statusCode());

        try (final SolidRDFSource resource = response.body()) {
            assertEquals(uri, resource.getIdentifier());
            assertEquals("application/octet-stream", resource.getMetadata().getContentType());
        }
    }

    @Test
    void testInvalidRdf() {
        final URI resource = URI.create(config.get("solid_resource_uri") + "/nonRDF");
        final Request request = Request.newBuilder()
            .uri(resource)
            .header("Accept", "text/turtle")
            .GET()
            .build();

        final CompletableFuture<Response<SolidContainer>> containerFuture =
                client.send(request, SolidResourceHandlers.ofSolidContainer())
                .toCompletableFuture();
        final CompletionException err1 =
                assertThrows(CompletionException.class, () -> containerFuture.join());

        assertTrue(err1.getCause() instanceof SolidResourceException);

        final CompletableFuture<Response<SolidContainer>> resourceFuture =
                client.send(request, SolidResourceHandlers.ofSolidContainer())
                .toCompletableFuture();
        final CompletionException err2 =
                assertThrows(CompletionException.class, () -> resourceFuture.join());

        assertTrue(err2.getCause() instanceof SolidResourceException);
    }
}
