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
package com.inrupt.client.webid;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class WebIdBodyHandlersTest {

    private static final WebIdMockHttpService mockHttpClient = new WebIdMockHttpService();
    private static Map<String, String> config = new HashMap<>();
    private static final HttpClient client = HttpClient.newHttpClient();

    @BeforeAll
    static void setup() {
        config.putAll(mockHttpClient.start());
    }

    @AfterAll
    static void teardown() {
        mockHttpClient.stop();
    }

    @Test
    void testGetOfWebIdProfile() throws IOException, InterruptedException {
        final var request = HttpRequest.newBuilder()
            .uri(URI.create(config.get("webid_uri") + "/webId"))
            .header("Accept", "text/turtle")
            .GET()
            .build();

        final var response = client.send(
            request,
            WebIdBodyHandlers.ofWebIdProfile(URI.create("https://example.test/username"))
        );

        assertEquals(200, response.statusCode());

        final var responseBody = response.body();
        assertEquals("https://example.test/username", responseBody.getId().toString());
        assertTrue(responseBody.getStorage().contains(URI.create("https://storage.example.test/storage-id/")));
        assertTrue(responseBody.getOidcIssuer().contains(URI.create("https://login.example.test")));
        assertTrue(responseBody.getType().contains(URI.create("http://xmlns.test/foaf/0.1/Agent")));
        assertTrue(responseBody.getSeeAlso().contains(
            URI.create("https://storage.example.test/storage-id/extendedProfile"))
        );
    }

}
