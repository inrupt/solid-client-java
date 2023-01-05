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
import com.inrupt.client.spi.HttpService;
import com.inrupt.client.spi.JsonService;
import com.inrupt.client.spi.ServiceProvider;
import com.inrupt.client.vc.Issuer.StatusRequest;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class IssuerTest {

    private static final VerifiableCredentialMockService vcMockService = new VerifiableCredentialMockService();
    private static final HttpService client = ServiceProvider.getHttpService();
    private static final Map<String, String> config = new HashMap<>();
    private static Issuer issuer;
    private static JsonService jsonService;
    private static VerifiableCredential expectedVC;

    @BeforeAll
    static void setup() throws IOException {
        config.putAll(vcMockService.start());
        issuer = new Issuer(URI.create(config.get("vc_uri")), client);
        jsonService = ServiceProvider.getJsonService();
        try (final var res = IssuerTest.class.getResourceAsStream("/__files/verifiableCredential.json")) {
            expectedVC = jsonService.fromJson(res, VerifiableCredential.class);
        }
    }

    @AfterAll
    static void teardown() {
        vcMockService.stop();
    }

    @Test
    void issueAsyncTest() throws IOException {
        final var vc = issuer.issue(expectedVC).toCompletableFuture().join();

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
    void statusAsyncTest() {
        final var statusRequest = StatusRequest.Builder.newBuilder()
                .credentialStatus(URI.create("CredentialStatusList2017"), true)
                .build("http://example.test/credentials/1872");

        issuer.status(statusRequest).toCompletableFuture().join();
    }
}
