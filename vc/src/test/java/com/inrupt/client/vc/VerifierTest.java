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

import static org.junit.jupiter.api.Assertions.*;

import com.inrupt.client.spi.JsonProcessor;
import com.inrupt.client.spi.ServiceProvider;
import com.inrupt.client.spi.VerifiableCredential;
import com.inrupt.client.spi.VerifiablePresentation;
import com.inrupt.client.vc.Verifier.VerificationResponse;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class VerifierTest {

    private static final VerifiableCredentialMockService vcMockService = new VerifiableCredentialMockService();
    private static final Map<String, String> config = new HashMap<>();
    private static final HttpClient client = HttpClient.newBuilder().version(Version.HTTP_1_1).build();
    private static Verifier verifier;
    private static JsonProcessor processor;
    private static VerificationResponse verificationResponse;

    @BeforeAll
    static void setup() throws IOException {
        config.putAll(vcMockService.start());
        verifier = new Verifier(URI.create(config.get("vc_uri")), client);
        processor = ServiceProvider.getJsonProcessor();
        try (final var res = IssuerTest.class.getResourceAsStream("/__files/verificationResponse.json")) {
            verificationResponse = processor.fromJson(res, VerificationResponse.class);
        }
    }

    @AfterAll
    static void teardown() {
        vcMockService.stop();
    }

    @Test
    void verifyTest() {
        final var verificationResponse = verifier.verify(VCtestData.VC);

        assertEquals(verificationResponse.checks, verificationResponse.checks);
        assertEquals(verificationResponse.warnings, verificationResponse.warnings);
        assertNull(verificationResponse.errors);
    }


    @Test
    void verifyStatusCodesTest() {
        assertAll("Invalid or malformed input because of empty VC",
            () -> {
                final CompletionException exception = assertThrows(CompletionException.class,
                    () -> verifier.verify(new VerifiableCredential())
                );
                assertTrue(exception.getCause() instanceof VerifiableCredentialException);
                final var cause = (VerifiableCredentialException) exception.getCause();
                assertEquals("Unexpected error response when verifying a resource.", cause.getMessage());
                assertEquals(Optional.of(400), cause.getStatus());
            });
    }

    @Test
    void verifyAsyncTest() {
        final var verificationResponse = verifier.verifyAsync(VCtestData.VC).toCompletableFuture().join();

        assertEquals(verificationResponse.checks, verificationResponse.checks);
        assertEquals(verificationResponse.warnings, verificationResponse.warnings);
        assertNull(verificationResponse.errors);
    }

    @Test
    void verifyPresentationTest() {
        final var verificationResponse = verifier.verify(VCtestData.VP);

        assertEquals(verificationResponse.checks, verificationResponse.checks);
        assertEquals(verificationResponse.warnings, verificationResponse.warnings);
        assertNull(verificationResponse.errors);
    }

    @Test
    void verifyPresentationStatusCodesTest() {
        assertAll("Invalid of malformed input because of empty VP",
            () -> {
                final CompletionException exception = assertThrows(CompletionException.class,
                    () -> verifier.verify(new VerifiablePresentation())
                );
                assertTrue(exception.getCause() instanceof VerifiableCredentialException);
                final var cause = (VerifiableCredentialException) exception.getCause();
                assertEquals("Unexpected error response when verifying a resource.", cause.getMessage());
                assertEquals(Optional.of(400), cause.getStatus());
            });
    }

    @Test
    void verifyPresentationAsyncTest() {
        final var verificationResponse = verifier.verifyAsync(VCtestData.VP).toCompletableFuture().join();

        assertEquals(verificationResponse.checks, verificationResponse.checks);
        assertEquals(verificationResponse.warnings, verificationResponse.warnings);
        assertNull(verificationResponse.errors);
    }

}
