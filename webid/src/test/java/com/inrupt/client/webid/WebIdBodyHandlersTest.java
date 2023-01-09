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
package com.inrupt.client.webid;

import static org.junit.jupiter.api.Assertions.*;

import com.inrupt.client.Request;
import com.inrupt.client.Response;
import com.inrupt.client.solid.SolidClient;
import com.inrupt.client.spi.HttpService;
import com.inrupt.client.spi.ServiceProvider;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class WebIdBodyHandlersTest {

    private static final WebIdMockHttpService mockHttpServer = new WebIdMockHttpService();
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
    void testGetOfWebIdProfile() throws IOException, InterruptedException {
        final URI webid = URI.create(config.get("webid_uri") + "/webId");
        final Request request = Request.newBuilder()
            .uri(webid)
            .header("Accept", "text/turtle")
            .GET()
            .build();

        final Response<WebIdProfile> response = client.send(request,
                WebIdBodyHandlers.ofWebIdProfile())
            .toCompletableFuture().join();

        assertEquals(200, response.statusCode());

        try (final WebIdProfile profile = response.body()) {
            assertEquals(webid, profile.getIdentifier());
            assertTrue(profile.getStorage().contains(URI.create("https://storage.example.test/storage-id/")));
            assertTrue(profile.getOidcIssuer().contains(URI.create("https://login.example.test")));
            assertTrue(profile.getType().contains(URI.create("http://xmlns.test/foaf/0.1/Agent")));
            assertTrue(profile.getSeeAlso()
                    .contains(URI.create("https://storage.example.test/storage-id/extendedProfile")));
        }
    }

    @Test
    void testHighLevelClient() throws Exception {
        final URI webid = URI.create(config.get("webid_uri") + "/webId");
        final SolidClient solidClient = SolidClient.getClient();

        solidClient.read(webid, WebIdProfile.class).thenAccept(profile -> {
            try (final WebIdProfile p = profile) {
                assertEquals(webid, p.getIdentifier());
                assertTrue(p.getStorage().contains(URI.create("https://storage.example.test/storage-id/")));
                assertTrue(p.getOidcIssuer().contains(URI.create("https://login.example.test")));
                assertTrue(p.getType().contains(URI.create("http://xmlns.test/foaf/0.1/Agent")));
                assertTrue(p.getSeeAlso()
                        .contains(URI.create("https://storage.example.test/storage-id/extendedProfile")));
            }
        }).toCompletableFuture().join();
    }

    @Test
    void testHighLevelClientHash() throws Exception {
        final URI webid = URI.create(config.get("webid_uri") + "/webIdHash#me");
        final SolidClient solidClient = SolidClient.getClient();

        solidClient.read(webid, WebIdProfile.class).thenAccept(profile -> {
            try (final WebIdProfile p = profile) {
                assertEquals(webid, p.getIdentifier());
                assertTrue(p.getStorage().contains(URI.create("https://storage.example.test/storage-id/")));
                assertTrue(p.getOidcIssuer().contains(URI.create("https://login.example.test")));
                assertTrue(p.getType().contains(URI.create("http://xmlns.test/foaf/0.1/Agent")));
                assertTrue(p.getSeeAlso()
                        .contains(URI.create("https://storage.example.test/storage-id/extendedProfile")));
            }
        }).toCompletableFuture().join();
    }
}
