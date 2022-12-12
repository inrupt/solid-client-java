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

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
        final var request = Request.newBuilder()
            .uri(URI.create(config.get("solid_resource_uri") + "/solid"))
            .header("Accept", "text/turtle")
            .GET()
            .build();

        final Response<SolidResource> response = client.send(
            request,
            SolidResourceHandlers.ofSolidResource(URI.create("https://example.test/resource/"))
        );

        assertEquals(200, response.statusCode());

        final var responseBody = response.body();
        assertEquals("https://example.test/resource/", responseBody.getId().toString());
        assertTrue(responseBody.getType().contains(URI.create("http://www.w3.org/ns/ldp#Container")));
        assertTrue(responseBody.getStorage().isPresent());
        assertEquals("http://storage/example", responseBody.getStorage().get().toString());
        assertEquals(responseBody.getWacAllow().get("user"), Set.of("read", "write"));
        assertEquals(responseBody.getWacAllow().get("public"), Set.of("read"));
        assertEquals(24, responseBody.getStatements().size());
        assertEquals(3, responseBody.getAllowedMethods().size());
        assertTrue(responseBody.getAllowedMethods().containsAll(Set.of("PUT", "POST", "PATCH")));
        assertTrue(responseBody.getAllowedPatchSyntaxes().containsAll(Set.of("application/example", "text/example")));
        assertTrue(responseBody.getAllowedPostSyntaxes().containsAll(Set.of("application/example", "text/example")));
        assertTrue(responseBody.getAllowedPutSyntaxes().containsAll(Set.of("application/example", "text/example")));
    }

    @Test
    void testGetOfSolidContainer() throws IOException, InterruptedException {
        final var request = Request.newBuilder()
            .uri(URI.create(config.get("solid_resource_uri") + "/solid"))
            .header("Accept", "text/turtle")
            .GET()
            .build();

        final Response<SolidContainer> response = client.send(
            request,
            SolidResourceHandlers.ofSolidContainer(URI.create("https://example.test/resource/"))
        );

        assertEquals(200, response.statusCode());

        final var responseBody = response.body();
        assertEquals("https://example.test/resource/", responseBody.getId().toString());
        assertEquals(3, responseBody.getContainedResources().size());
        assertEquals("https://example.test/resource/test.txt",
                        responseBody.getContainedResources().get(1).getId().toString());
        assertEquals("https://example.test/resource/test2.txt",
                        responseBody.getContainedResources().get(2).getId().toString());
        assertFalse(responseBody.getContainedResources().stream()
                        .map(SolidResource::getId)
                        .map(URI::toString)
                        .collect(Collectors.toSet())
                        .contains("https://example.test/resource/test3.txt"));
        //commit sign test
    }
}