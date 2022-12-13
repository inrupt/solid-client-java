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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SolidResourceTest {

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
    void testGetOfSolidResource() throws IOException, InterruptedException {
        final URI uri = URI.create(config.get("solid_resource_uri") + "/solid/");
        final Request request = Request.newBuilder()
            .uri(uri)
            .header("Accept", "text/turtle")
            .GET()
            .build();

        final Response<SolidResource> response = client.send(
            request,
            SolidResourceHandlers.ofSolidResource()
        );

        assertEquals(200, response.statusCode());

        final SolidResource resource = response.body();
        assertEquals(uri, resource.getId());
        assertTrue(resource.getType().contains(LDP.BasicContainer));
        assertEquals(Optional.of(URI.create("http://storage.example/")), resource.getStorage());
        assertTrue(resource.getWacAllow().get("user").containsAll(Arrays.asList("read", "write")));
        assertEquals(resource.getWacAllow().get("public"), Collections.singleton("read"));
        assertEquals(13, resource.getDataset().stream().count());
        assertEquals(3, resource.getAllowedMethods().size());
        assertTrue(resource.getAllowedMethods().containsAll(Arrays.asList("PUT", "POST", "PATCH")));
        assertTrue(resource.getAllowedPatchSyntaxes()
                .containsAll(Arrays.asList("application/sparql-update", "text/n3")));
        assertTrue(resource.getAllowedPostSyntaxes()
                .containsAll(Arrays.asList("application/ld+json", "text/turtle")));
        assertTrue(resource.getAllowedPutSyntaxes()
                .containsAll(Arrays.asList("application/ld+json", "text/turtle")));
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
        );

        assertEquals(200, response.statusCode());

        final SolidContainer container = response.body();
        assertEquals(resource, container.getId());
        assertEquals(3, container.getContainedResources().count());

        final Set<URI> uris = new HashSet<>();
        uris.add(URIBuilder.newBuilder(resource).path("test.txt").build());
        uris.add(URIBuilder.newBuilder(resource).path("test2.txt").build());
        uris.add(URIBuilder.newBuilder(resource).path("newContainer/").build());

        container.getContainedResources().forEach(child ->
            assertTrue(uris.contains(child.getId())));
    }
}
