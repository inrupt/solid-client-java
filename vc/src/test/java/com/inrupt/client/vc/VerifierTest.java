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

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class VerifierTest {

    private static final VerifiableCredentialMockService vcMockService = new VerifiableCredentialMockService();
    private static final Map<String, String> config = new HashMap<>();
    private static final HttpClient client = HttpClient.newBuilder().version(Version.HTTP_1_1).build();
    private static Verifier verifier;

    @BeforeAll
    static void setup() {
        config.putAll(vcMockService.start());
        verifier = new Verifier(URI.create(config.get("vc_uri")), client);
    }

    @AfterAll
    static void teardown() {
        vcMockService.stop();
    }

    @Test
    void verifyTest() {
        final var verificationResponse = verifier.verify(VCtestData.VC);

        assertEquals("[good1, good2]", verificationResponse.checks.toString());
        assertEquals("[could not check did]", verificationResponse.warnings.toString());
        assertNull(verificationResponse.errors);
    }


    /*  @Test
    void verifyStatusCodesTest() {
        assertAll("Empty VC",
        () -> {
            final CompletionException exception = assertThrows(CompletionException.class,
            () -> verifier.verify(new VerifiableCredential())
            );
            assertTrue(exception.getCause() instanceof VerifiableCredentialException);
            assertEquals("com.inrupt.client.vc.VerifiableCredentialException: Invalid input", exception.getMessage());
            assertEquals(400, ((VerifiableCredentialException)exception.getCause()).getStatus().get());
        });
    }
    */

    @Test
    void verifyAsyncTest() {
        final var verificationResponse = verifier.verifyAsync(VCtestData.VC).toCompletableFuture().join();

        assertEquals("[good1, good2]", verificationResponse.checks.toString());
        assertEquals("[could not check did]", verificationResponse.warnings.toString());
        assertNull(verificationResponse.errors);
    }

    @Test
    void verifyPresentationTest() {
        final var verificationResponse = verifier.verify(VCtestData.VP);

        assertEquals("[good1, good2]", verificationResponse.checks.toString());
        assertEquals("[could not check did]", verificationResponse.warnings.toString());
        assertNull(verificationResponse.errors);
    }

    /* @Test
    void verifyStatusCodesTest() {
        assertAll("Empty VC",
        () -> {
            final CompletionException exception = assertThrows(CompletionException.class,
            () -> verifier.verify(new VerifiablePresentation())
            );
            assertTrue(exception.getCause() instanceof VerifiableCredentialException);
            assertEquals("com.inrupt.client.vc.VerifiableCredentialException: Invalid input", exception.getMessage());
            assertEquals(400, ((VerifiableCredentialException)exception.getCause()).getStatus().get());
        });
    } */

    @Test
    void verifyPresentationAsyncTest() {
        final var verificationResponse = verifier.verifyAsync(VCtestData.VP).toCompletableFuture().join();

        assertEquals("[good1, good2]", verificationResponse.checks.toString());
        assertEquals("[could not check did]", verificationResponse.warnings.toString());
        assertNull(verificationResponse.errors);
    }

}
