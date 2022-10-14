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

import com.inrupt.client.core.InputStreamBodySubscribers;
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
    private static final int CREATED = 201;
    private static final int NO_CONTENT = 204;

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

        return httpClient.sendAsync(req, ofVerificationResponse())
            .thenApply(HttpResponse::body);
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
    public CompletionStage<VerificationResponse> verifyAsync(
            final VerifiablePresentation presentation) {
        final var req = HttpRequest.newBuilder(getPresentationVerifierUrl())
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .POST(VerifiableCredentialBodyPublishers.ofVerifiablePresentation(presentation))
                .build();

        return httpClient.sendAsync(req, ofVerificationResponse()).thenApply(HttpResponse::body);

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

    private HttpResponse.BodyHandler<VerificationResponse> ofVerificationResponse() {
        return responseInfo -> {
            final HttpResponse.BodySubscriber<VerificationResponse> bodySubscriber;
            final int httpStatus = responseInfo.statusCode();
            if (SUCCESS == httpStatus || CREATED == httpStatus || NO_CONTENT == httpStatus ) {
                bodySubscriber = InputStreamBodySubscribers.mapping(input -> {
                    try {
                        return processor.fromJson(input, VerificationResponse.class);
                    } catch (final IOException ex) {
                        throw new VerifiableCredentialException(
                                "Error parsing verification request", ex);
                    }
                });
            } else {
                bodySubscriber = HttpResponse.BodySubscribers.replacing(null);
                bodySubscriber.onError(new VerifiableCredentialException(
                    "Unexpected error response when verifying a resource.",
                    httpStatus));
            }
            return bodySubscriber;
        };
    }

    private URI getCredentialVerifierUrl() {
        return URIBuilder.newBuilder(baseUri).path("credentials/verify").build();
    }

    private URI getPresentationVerifierUrl() {
        return URIBuilder.newBuilder(baseUri).path("presentations/verify").build();
    }
}
