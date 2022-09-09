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
package com.inrupt.client.core;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class InputStreamBodySubscribersTest {

    static final ObjectMapper objectMapper = new ObjectMapper();
    static final MockHttpService httpService = new MockHttpService();
    static final Config config = new Config();

    @BeforeAll
    static void setup() {
        config.baseUri = httpService.start();
        objectMapper.findAndRegisterModules();
    }

    @AfterAll
    static void teardown() {
        httpService.stop();
    }

    @Test
    void testSyncMapping() throws Exception {
        final var client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();

        final var req = HttpRequest.newBuilder()
            .uri(config.baseUri)
            .GET()
            .build();

        final var res = client.send(req, ofCustomType());
        final var obj = res.body();
        assertEquals("identifier", obj.id);
        assertTrue(obj.type.contains("Foo"));
        assertEquals(URI.create("https://issuer.test"), obj.issuer);
        assertEquals(Instant.parse("2022-09-08T00:00:00Z"), obj.date);
    }

    static class Config {
        public URI baseUri;
    }

    static HttpResponse.BodyHandler<CustomType> ofCustomType() {
        return responseInfo -> InputStreamBodySubscribers.mapping(input -> {
            try (final var stream = input) {
                return objectMapper.readValue(stream, CustomType.class);
            } catch (final IOException ex) {
                throw new UncheckedIOException("Could not map data to CustomType", ex);
            }
        });
    }
}
