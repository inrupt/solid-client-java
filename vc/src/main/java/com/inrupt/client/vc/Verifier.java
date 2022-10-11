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

import com.inrupt.client.core.URIBuilder;
import com.inrupt.client.spi.JsonProcessor;
import com.inrupt.client.spi.ServiceProvider;
import com.inrupt.client.spi.VerifiableCredential;
import com.inrupt.client.spi.VerifiablePresentation;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * A class for interacting with a VC-API Verifier endpoint.
 *
 * @see <a href="https://w3c-ccg.github.io/vc-api/verifier.html">VC-API: Verifier</a>
 */
public class Verifier {

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
    private static final int SUCCESS = 200;
    private static final int INVALID_INPUT = 400;
    private static final int ERROR = 500;

    private final URI baseUri;
    private final HttpClient httpClient;
    private final JsonProcessor processor;

    /**
     * Create a new Verifier object for interacting with a VC-API.
     *
     * @param baseUri the base URI for the VC-API
     */
    public Verifier(final URI baseUri) {
        this(baseUri, HttpClient.newHttpClient());
    }

    /**
     * Create a new Verifier object for interacting with a VC-API.
     *
     * @param baseUri the base URI for the VC-API
     * @param httpClient an HTTP client
     */
    public Verifier(final URI baseUri, final HttpClient httpClient) {
        this.baseUri = baseUri;
        this.httpClient = httpClient;
        this.processor = ServiceProvider.getJsonProcessor();
    }

    /**
     * Synchronously verify a credential.
     *
     * @param credential the verifiable credential
     * @return a verification response
     */
    public VerificationResponse verify(final VerifiableCredential credential) {
        return verifyAsync(credential).toCompletableFuture().join();
    }

    /**
     * Asynchronously verify a credential.
     *
     * @param credential the verifiable credential
     * @return the next stage of completion, containing a verification response
     */
    public CompletionStage<VerificationResponse> verifyAsync(final VerifiableCredential credential) {
        final var req = HttpRequest.newBuilder(getCredentialVerifierUrl())
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .POST(VerifiableCredentialBodyPublishers.ofVerifiableCredential(credential))
            .build();

        return httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofInputStream())
            .thenCompose(res -> {
                try {
                    if (SUCCESS == res.statusCode()) {
                        return CompletableFuture.completedFuture(
                            processor.fromJson(res.body(), VerificationResponse.class));
                    }
                    if (INVALID_INPUT == res.statusCode()) {
                        throw VerifiableCredentialException.newBuilder()
                            .message("Invalid input")
                            .status(res.statusCode())
                            .build();
                    }
                    if (ERROR == res.statusCode()) {
                        throw VerifiableCredentialException.newBuilder()
                            .message("Internal server error")
                            .status(res.statusCode())
                            .build();
                    }
                    throw VerifiableCredentialException.newBuilder()
                        .message("Unexpected error response while verifying a credential")
                        .build();
                } catch (final IOException ex) {
                    throw VerifiableCredentialException.newBuilder()
                        .message("Unexpected I/O exception while verifying a credential")
                        .exception(ex)
                        .build();
                }
            });
    }


    /**
     * Synchronously verify a presentation.
     *
     * @param presentation the verifiable presentation
     * @return a verification response
     */
    public VerificationResponse verify(final VerifiablePresentation presentation) {
        return verifyAsync(presentation).toCompletableFuture().join();
    }

    /**
     * Asynchronously verify a presentation.
     *
     * @param presentation the verifiable presentation
     * @return the next stage of completion, containing a verification response
     */
    public CompletionStage<VerificationResponse> verifyAsync(final VerifiablePresentation presentation) {
        final var req = HttpRequest.newBuilder(getPresentationVerifierUrl())
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .POST(VerifiableCredentialBodyPublishers.ofVerifiablePresentation(presentation))
            .build();

        return httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofInputStream())
            .thenCompose( res -> {
                try {
                    if (SUCCESS == res.statusCode()) {
                        return CompletableFuture.completedFuture(
                            processor.fromJson(res.body(), VerificationResponse.class));
                    }
                    if (INVALID_INPUT == res.statusCode()) {
                        throw VerifiableCredentialException.newBuilder()
                            .message("Invalid input")
                            .status(res.statusCode())
                            .build();
                    }
                    if (ERROR == res.statusCode()) {
                        throw VerifiableCredentialException.newBuilder()
                            .message("Internal server error")
                            .status(res.statusCode())
                            .build();
                    }
                    throw VerifiableCredentialException.newBuilder()
                        .message("Unexpected error response while verifying a presentation")
                        .build();
                } catch (final IOException ex) {
                    throw VerifiableCredentialException.newBuilder()
                        .message("Unexpected I/O exception while verifying a presentation")
                        .exception(ex)
                        .build();
                }
            });
    }

    /**
     * A data objects for verification responses.
     */
    public static class VerificationResponse {
        /**
         * The verification checks that were performed.
         */
        public List<String> checks;

        /**
         * The verification warnings that were discovered.
         */
        public List<String> warnings;

        /**
         * The verification errors that were discovered.
         */
        public List<String> errors;
    }

    private URI getCredentialVerifierUrl() {
        return URIBuilder.newBuilder(baseUri).path("credentials/verify").build();
    }

    private URI getPresentationVerifierUrl() {
        return URIBuilder.newBuilder(baseUri).path("presentations/verify").build();
    }
}
