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

import com.inrupt.client.Request;
import com.inrupt.client.Response;
import com.inrupt.client.VerifiableCredential;
import com.inrupt.client.VerifiablePresentation;
import com.inrupt.client.spi.HttpProcessor;
import com.inrupt.client.spi.JsonProcessor;
import com.inrupt.client.spi.ServiceProvider;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class VerifiableCredentialBodyPublishersTest {

    private static final VerifiableCredentialMockService vcMockService = new VerifiableCredentialMockService();
    private static final Map<String, String> config = new HashMap<>();
    private static final HttpProcessor client = ServiceProvider.getHttpProcessor();
    private static JsonProcessor processor;
    private static VerifiableCredential expectedVC;
    private static VerifiablePresentation expectedVP;

    @BeforeAll
    static void setup() throws IOException {
        config.putAll(vcMockService.start());
        processor = ServiceProvider.getJsonProcessor();
        try (final var res = VerifiableCredentialBodyPublishersTest.class
                .getResourceAsStream("/__files/verifiableCredential.json")) {
            expectedVC = processor.fromJson(res, VerifiableCredential.class);
        }
        try (final var res = VerifiableCredentialBodyPublishersTest.class
            .getResourceAsStream("/__files/verifiablePresentation.json")) {
            expectedVP = processor.fromJson(res, VerifiablePresentation.class);
        }
    }

    @AfterAll
    static void teardown() {
        vcMockService.stop();
    }

    @Test
    void ofVerifiableCredentialPublisherTest() throws IOException, InterruptedException {
        final var request = Request.newBuilder()
            .uri(URI.create(config.get("vc_uri") + "/postVc"))
            .header("Content-Type", "application/json")
            .POST(VerifiableCredentialBodyPublishers.ofVerifiableCredential(expectedVC))
            .build();

        final var response = client.send(request, Response.BodyHandlers.discarding());

        assertEquals(201, response.statusCode());
    }

    @Test
    void ofVerifiablePresentationPublisherTest() throws IOException, InterruptedException {
        final var request = Request.newBuilder()
            .uri(URI.create(config.get("vc_uri") + "/postVp"))
            .header("Content-Type", "application/json")
            .POST(VerifiableCredentialBodyPublishers.ofVerifiablePresentation(expectedVP))
            .build();

        final var response = client.send(request, Response.BodyHandlers.discarding());

        assertEquals(201, response.statusCode());
    }

}
