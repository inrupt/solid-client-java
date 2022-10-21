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
package com.inrupt.client.vc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.inrupt.client.api.Request;
import com.inrupt.client.spi.HttpProcessor;
import com.inrupt.client.spi.ServiceProvider;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class VerifiableCredentialBodyHandlersTest {

    private static final VerifiableCredentialMockService vcMockService = new VerifiableCredentialMockService();
    private static final Map<String, String> config = new HashMap<>();
    private static final HttpProcessor client = ServiceProvider.getHttpProcessor();

    @BeforeAll
    static void setup() {
        config.putAll(vcMockService.start());
    }

    @AfterAll
    static void teardown() {
        vcMockService.stop();
    }

    @Test
    void ofVerifiableCredentialTest() throws IOException, InterruptedException {
        final var request = Request.newBuilder()
            .uri(URI.create(config.get("vc_uri") + "/vc"))
            .GET()
            .build();

        final var response = client.send(request, VerifiableCredentialBodyHandlers.ofVerifiableCredential());

        assertEquals(200, response.statusCode());
        final var responseBody = response.body();
        assertEquals("https://example.test/issuers/565049", responseBody.issuer);

    }

    @Test
    void ofVerifiablePresentationTest() throws IOException, InterruptedException {
        final Request request = Request.newBuilder()
            .uri(URI.create(config.get("vc_uri") + "/vp"))
            .GET()
            .build();

        final var response = client.send(request, VerifiableCredentialBodyHandlers.ofVerifiablePresentation());

        assertEquals(200, response.statusCode());
        final var responseBody = response.body();
        assertEquals("did:example:123", responseBody.holder);
    }
}
