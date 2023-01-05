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
package com.inrupt.client.vc;

import static org.junit.jupiter.api.Assertions.*;

import com.inrupt.client.VerifiableCredential;
import com.inrupt.client.VerifiablePresentation;
import com.inrupt.client.spi.HttpService;
import com.inrupt.client.spi.JsonService;
import com.inrupt.client.spi.ServiceProvider;
import com.inrupt.client.vc.Verifier.VerificationResponse;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class VerifierTest {

    private static final VerifiableCredentialMockService vcMockService = new VerifiableCredentialMockService();
    private static final Map<String, String> config = new HashMap<>();
    private static final HttpService client = ServiceProvider.getHttpService();
    private static Verifier verifier;
    private static JsonService service;
    private static VerificationResponse expectedVerificationResponse;
    private static VerifiableCredential expectedVC;
    private static VerifiablePresentation expectedVP;

    @BeforeAll
    static void setup() throws IOException {
        config.putAll(vcMockService.start());
        verifier = new Verifier(URI.create(config.get("vc_uri")), client);
        service = ServiceProvider.getJsonService();
        try (final var res = VerifierTest.class.getResourceAsStream("/__files/verificationResponse.json")) {
            expectedVerificationResponse = service.fromJson(res, VerificationResponse.class);
        }
        try (final var res =
                VerifierTest.class.getResourceAsStream("/__files/verifiableCredential.json")) {
            expectedVC = service.fromJson(res, VerifiableCredential.class);
        }
        try (final var res = VerifierTest.class.getResourceAsStream("/__files/verifiablePresentation.json")) {
            expectedVP = service.fromJson(res, VerifiablePresentation.class);
        }
    }

    @AfterAll
    static void teardown() {
        vcMockService.stop();
    }

    @Test
    void verifyAsyncTest() {
        final var verificationResponse = verifier.verify(expectedVC).toCompletableFuture().join();

        assertEquals(expectedVerificationResponse.checks, verificationResponse.checks);
        assertEquals(expectedVerificationResponse.warnings, verificationResponse.warnings);
        assertNull(verificationResponse.errors);
    }

    @Test
    void verifyPresentationAsyncTest() {
        final var verificationResponse = verifier.verify(expectedVP).toCompletableFuture().join();

        assertEquals(expectedVerificationResponse.checks, verificationResponse.checks);
        assertEquals(expectedVerificationResponse.warnings, verificationResponse.warnings);
        assertNull(verificationResponse.errors);
    }

}
