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

import com.inrupt.client.VerifiableCredential;
import com.inrupt.client.spi.HttpProcessor;
import com.inrupt.client.spi.JsonProcessor;
import com.inrupt.client.spi.ServiceProvider;
import com.inrupt.client.vc.Issuer.StatusRequest;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class IssuerTest {

    private static final VerifiableCredentialMockService vcMockService = new VerifiableCredentialMockService();
    private static final HttpProcessor client = ServiceProvider.getHttpProcessor();
    private static final Map<String, String> config = new HashMap<>();
    private static Issuer issuer;
    private static JsonProcessor processor;
    private static VerifiableCredential expectedVC;

    @BeforeAll
    static void setup() throws IOException {
        config.putAll(vcMockService.start());
        issuer = new Issuer(URI.create(config.get("vc_uri")), client);
        processor = ServiceProvider.getJsonProcessor();
        try (final var res = IssuerTest.class.getResourceAsStream("/__files/verifiableCredential.json")) {
            expectedVC = processor.fromJson(res, VerifiableCredential.class);
        }
    }

    @AfterAll
    static void teardown() {
        vcMockService.stop();
    }

    @Test
    void issueTest() throws IOException {
        final var vc = issuer.issue(expectedVC);

        assertEquals(expectedVC.context, vc.context);
        assertEquals(expectedVC.id, vc.id);
        assertEquals(expectedVC.type, vc.type);
        assertEquals(expectedVC.issuer, vc.issuer);
        assertEquals(expectedVC.issuanceDate, vc.issuanceDate);
        assertEquals(expectedVC.expirationDate, vc.expirationDate);
        assertEquals(expectedVC.credentialSubject, vc.credentialSubject);
        assertEquals(expectedVC.credentialStatus, vc.credentialStatus);
        assertEquals(expectedVC.proof, vc.proof);
    }

    @Test
    void issueAsyncTest() throws IOException {
        final var vc = issuer.issueAsync(expectedVC).toCompletableFuture().join();

        assertEquals(expectedVC.context, vc.context);
        assertEquals(expectedVC.id, vc.id);
        assertEquals(expectedVC.type, vc.type);
        assertEquals(expectedVC.issuer, vc.issuer);
        assertEquals(expectedVC.issuanceDate, vc.issuanceDate);
        assertEquals(expectedVC.expirationDate, vc.expirationDate);
        assertEquals(expectedVC.credentialSubject, vc.credentialSubject);
        assertEquals(expectedVC.credentialStatus, vc.credentialStatus);
        assertEquals(expectedVC.proof, vc.proof);
    }

    @Test
    void issueStatusCodesTest() {
        assertAll("Bad request because of empty VC",
            () -> {
                final CompletionException exception = assertThrows(CompletionException.class,
                    () -> issuer.issue(new VerifiableCredential())
                );
                assertTrue(exception.getCause() instanceof VerifiableCredentialException);
                final var cause = (VerifiableCredentialException) exception.getCause();
                assertEquals("Unexpected error response when handling a verifiable credential.", cause.getMessage());
                assertEquals(Optional.of(400), cause.getStatus());
            });
    }

    @Test
    void statusTest() {
        final var statusRequest = StatusRequest.Builder.newBuilder()
            .credentialStatus(URI.create("CredentialStatusList2017"), true)
            .build("http://example.test/credentials/1872");

        issuer.status(statusRequest);

    }

    @Test
    void statusAsyncTest() {
        final var statusRequest = StatusRequest.Builder.newBuilder()
                .credentialStatus(URI.create("CredentialStatusList2017"), true)
                .build("http://example.test/credentials/1872");

        issuer.statusAsync(statusRequest);
    }

    @Test
    void statusAsyncTestStatusCodesTest() {
        final var statusRequest = StatusRequest.Builder.newBuilder()
                .credentialStatus(URI.create("CredentialStatusList2017"), true)
                .build("http://example.test/credentials/0000");
        assertAll("Not found because of non existent credentialID",
            () -> {
                final CompletionException exception = assertThrows(CompletionException.class,
                    () -> issuer.status(statusRequest)
                );
                assertTrue(exception.getCause() instanceof VerifiableCredentialException);
                final var cause = (VerifiableCredentialException) exception.getCause();
                assertEquals("Unexpected error response when updating status.", cause.getMessage());
                assertEquals(Optional.of(404), cause.getStatus());
            });
    }

}
