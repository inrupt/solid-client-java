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

import com.inrupt.client.spi.VerifiableCredential;
import com.inrupt.client.vc.Issuer.StatusRequest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class IssuerTest {

    private static final VerifiableCredentialMockService vcMockService = new VerifiableCredentialMockService();
    private static final HttpClient client = HttpClient.newBuilder().version(Version.HTTP_1_1).build();
    private static final Map<String, String> config = new HashMap<>();
    private static Issuer issuer;

    @BeforeAll
    static void setup() {
        config.putAll(vcMockService.start());
        issuer = new Issuer(URI.create(config.get("vc_uri")), client);
    }

    @AfterAll
    static void teardown() {
        vcMockService.stop();
    }

    @Test
    void issueTest() {
        final var vc = issuer.issue(VCtestData.VC);

        assertEquals(VCtestData.VC.context, vc.context);
        assertEquals(VCtestData.VC.id, vc.id);
        assertEquals(VCtestData.VC.type, vc.type);
        assertEquals(VCtestData.VC.issuer, vc.issuer);
        assertEquals(VCtestData.VC.issuanceDate, vc.issuanceDate);
        assertEquals(VCtestData.VC.expirationDate, vc.expirationDate);
        final var vcCredentialSubject = "{alumniOf=" +
            "{\"id\":\"did:example:c276e12ec21ebfeb1f712ebc6f1\"," +
            "\"name\":\"Example University\"}," +
            " id=\"did:example:ebfeb1f712ebc6f1c276e12ec21\"}";
        assertEquals(vcCredentialSubject, vc.credentialSubject.toString());
        final var vcCredentialStatus = "{id=\"https://example.test/status/24\", type=\"CredentialStatusList2017\"}";
        assertEquals(vcCredentialStatus, vc.credentialStatus.toString());
        final var proof = "{created=\"2017-06-18T21:19:10Z\"," +
            " jws=\"eyJhbGciOiJSUzI1NiIsImI2NCI6ZmFsc2UsImNyaXQiOlsiYjY0Il19\"," +
            " proofPurpose=\"assertionMethod\"," +
            " type=\"RsaSignature2018\"," +
            " verificationMethod=\"https://example.test/issuers/565049#key-1\"" +
            "}";
        assertEquals(proof, vc.proof.toString());
    }

    @Test
    void issueAsyncTest() {
        final var vc = issuer.issueAsync(VCtestData.VC).toCompletableFuture().join();

        assertEquals(VCtestData.VC.context, vc.context);
        assertEquals(VCtestData.VC.id, vc.id);
        assertEquals(VCtestData.VC.type, vc.type);
        assertEquals(VCtestData.VC.issuer, vc.issuer);
        assertEquals(VCtestData.VC.issuanceDate, vc.issuanceDate);
        assertEquals(VCtestData.VC.expirationDate, vc.expirationDate);
        final var vcCredentialSubject =
                "{alumniOf=" + "{\"id\":\"did:example:c276e12ec21ebfeb1f712ebc6f1\","
                        + "\"name\":\"Example University\"},"
                        + " id=\"did:example:ebfeb1f712ebc6f1c276e12ec21\"}";
        assertEquals(vcCredentialSubject, vc.credentialSubject.toString());
        final var vcCredentialStatus =
                "{id=\"https://example.test/status/24\", type=\"CredentialStatusList2017\"}";
        assertEquals(vcCredentialStatus, vc.credentialStatus.toString());
        final var proof = "{created=\"2017-06-18T21:19:10Z\","
                + " jws=\"eyJhbGciOiJSUzI1NiIsImI2NCI6ZmFsc2UsImNyaXQiOlsiYjY0Il19\","
                + " proofPurpose=\"assertionMethod\"," + " type=\"RsaSignature2018\","
                + " verificationMethod=\"https://example.test/issuers/565049#key-1\"" + "}";
        assertEquals(proof, vc.proof.toString());
    }

    @Test
    void issueStatusCodesTest() {
        assertAll("Empty VC",
            () -> {
                final CompletionException exception = assertThrows(CompletionException.class,
                    () -> issuer.issue(new VerifiableCredential())
                );
                assertTrue(exception.getCause() instanceof VerifiableCredentialException);
                assertEquals(
                    "com.inrupt.client.vc.VerifiableCredentialException: " +
                    "Unexpected error response when handling a verifiable credential.",
                    exception.getMessage());
                assertEquals(400, ((VerifiableCredentialException)exception.getCause()).getStatus().get());
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
        assertAll("Empty VC",
            () -> {
                final CompletionException exception = assertThrows(CompletionException.class,
                    () -> issuer.status(statusRequest)
                );
                assertTrue(exception.getCause() instanceof VerifiableCredentialException);
                assertEquals(
                    "com.inrupt.client.vc.VerifiableCredentialException: " +
                    "Unexpected error response when updating status.",
                    exception.getMessage());
                assertEquals(404, ((VerifiableCredentialException)exception.getCause()).getStatus().get());
            });
    }

}
