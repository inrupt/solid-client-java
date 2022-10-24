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

import com.inrupt.client.Request;
import com.inrupt.client.Response;
import com.inrupt.client.URIBuilder;
import com.inrupt.client.VerifiableCredential;
import com.inrupt.client.VerifiablePresentation;
import com.inrupt.client.core.IOUtils;
import com.inrupt.client.spi.HttpService;
import com.inrupt.client.spi.JsonService;
import com.inrupt.client.spi.ServiceProvider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * A class for interacting with a VC-API Holder endpoint.
 *
 * @see <a href="https://w3c-ccg.github.io/vc-api/holder.html">VC-API: Holder</a>
 */
public class Holder {

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
    private static final String CREDENTIALS = "credentials";
    private static final String PRESENTATIONS = "presentations";
    private static final String EXCHANGES = "exchanges";

    private final URI baseUri;
    private final HttpService httpClient;
    private final JsonService jsonService;

    /**
     * Create a new Holder object for interacting with a VC-API.
     *
     * @param baseUri the base URI for the VC-API
     */
    public Holder(final URI baseUri) {
        this(baseUri, ServiceProvider.getHttpService());
    }

    /**
     * Create a new Holder object for interacting with a VC-API.
     *
     * @param baseUri the base URI for the VC-API
     * @param httpClient an HTTP client
     */
    public Holder(final URI baseUri, final HttpService httpClient) {
        this.baseUri = baseUri;
        this.httpClient = httpClient;
        this.jsonService = ServiceProvider.getJsonService();
    }

    /**
     * List all verifiable credentials.
     *
     * @return a list of verifiable credentials
     */
    public List<VerifiableCredential> listCredentials() {
        return listCredentials(Collections.emptyList());
    }

    /**
     * List all verifiable credentials, filtered by a list of types.
     *
     * @param types the VC types on which to filter
     * @return a list of verifiable credentials
     */
    public List<VerifiableCredential> listCredentials(final List<URI> types) {
        return awaitAsync(listCredentialsAsync(Objects.requireNonNull(types)));
    }

    /**
     * List all verifiable credentials.
     *
     * @return the next stage of completion, including the list of verifiable credentials
     */
    public CompletionStage<List<VerifiableCredential>> listCredentialsAsync() {
        return listCredentialsAsync(Collections.emptyList());
    }

    /**
     * List all verifiable credentials, filtered by a list of types.
     *
     * @param types the VC types on which to filter
     * @return the next stage of completion, including the list of verifiable credentials
     */
    public CompletionStage<List<VerifiableCredential>> listCredentialsAsync(final List<URI> types) {
        final var req = Request.newBuilder(getCredentialListEndpoint(Objects.requireNonNull(types))).build();
        return httpClient.sendAsync(req, Response.BodyHandlers.ofInputStream())
            .thenApply(res -> {
                try {
                    final int httpStatus = res.statusCode();
                    if (httpStatus >= 200 && httpStatus < 300) {
                        return jsonService.fromJson(res.body(),
                            new ArrayList<VerifiableCredential>(){}.getClass().getGenericSuperclass());
                    }
                    throw new VerifiableCredentialException(
                        "Unexpected error response while listing credentials",
                        httpStatus);
                } catch (final IOException ex) {
                    throw new VerifiableCredentialException(
                        "Unexpected I/O exception while listing credentials",
                        ex);
                }
            });
    }

    /**
     * Retrieve a verifiable credential.
     *
     * @param credentialId the credential identifier
     * @return the verifiable credential
     */
    public VerifiableCredential getCredential(final String credentialId) {
        return getCredentialAsync(Objects.requireNonNull(credentialId)).toCompletableFuture().join();
    }

    /**
     * Retrieve a verifiable credential.
     *
     * @param credentialId the credential identifier
     * @return the next stage of completion, including a verifiable credential
     */
    public CompletionStage<VerifiableCredential> getCredentialAsync(final String credentialId) {
        final var req = Request
                .newBuilder(getCredentialEndpoint(Objects.requireNonNull(credentialId))).build();
        return httpClient.sendAsync(req, VerifiableCredentialBodyHandlers.ofVerifiableCredential())
                .thenApply(Response::body);
    }

    /**
     * Delete a credential.
     *
     * @param credentialId the credential identifier
     */
    public void deleteCredential(final String credentialId) {
        awaitAsync(deleteCredentialAsync(Objects.requireNonNull(credentialId)));
    }

    /**
     * Delete a credential.
     *
     * @param credentialId the credential identifier
     * @return the next stage of compltion
     */
    public CompletionStage<Void> deleteCredentialAsync(final String credentialId) {
        final var req = Request.newBuilder(getCredentialEndpoint(Objects.requireNonNull(credentialId)))
            .DELETE().build();

        return httpClient.sendAsync(req, Response.BodyHandlers.discarding())
            .thenApply(res -> {
                final int httpStatus = res.statusCode();
                if (httpStatus >= 200 && httpStatus < 300) {
                    return res.body();
                }
                throw new VerifiableCredentialException(
                    "Unexpected error while deleting credential",
                    httpStatus);
            });
    }

    /**
     * Derive a credential.
     *
     * @param request the selective disclosure request
     * @return the derived verifiable credential
     */
    public VerifiableCredential derive(final DerivationRequest request) {
        return awaitAsync(deriveAsync(request));
    }

    /**
     * Derive a credential.
     *
     * @param request the selective disclosure request
     * @return the next stage of completion, including the derived verifiable credential
     */
    public CompletionStage<VerifiableCredential> deriveAsync(final DerivationRequest request) {
        final var req = Request.newBuilder(getDeriveEndpoint())
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .POST(serialize(request))
            .build();

        return httpClient.sendAsync(req, VerifiableCredentialBodyHandlers.ofVerifiableCredential())
                .thenApply(Response::body);
    }

    /**
     * Retrieve a list of presentations.
     *
     * @return a list of presentations
     */
    public List<VerifiablePresentation> listPresentations() {
        return listPresentations(Collections.emptyList());
    }

    /**
     * Retrieve a list of presentations.
     *
     * @param types presentation type filters
     * @return a list of presentations
     */
    public List<VerifiablePresentation> listPresentations(final List<URI> types) {
        return awaitAsync(listPresentationsAsync(Objects.requireNonNull(types)));
    }

    /**
     * Retrieve a list of presentations.
     *
     * @return the next stage of completion, including a list of presentations
     */
    public CompletionStage<List<VerifiablePresentation>> listPresentationsAsync() {
        return listPresentationsAsync(Collections.emptyList());
    }

    /**
     * Retrieve a list of presentations.
     *
     * @param types presentation type filters
     * @return the next stage of completion, including a list of presentations
     */
    public CompletionStage<List<VerifiablePresentation>> listPresentationsAsync(final List<URI> types) {
        final var req = Request.newBuilder(getPresentationListEndpoint(Objects.requireNonNull(types))).build();
        return httpClient.sendAsync(req, Response.BodyHandlers.ofInputStream())
            .thenApply(res -> {
                try {
                    final int httpStatus = res.statusCode();
                    if (httpStatus >= 200 && httpStatus < 300) {
                        return jsonService.fromJson(res.body(),
                            new ArrayList<VerifiablePresentation>(){}.getClass().getGenericSuperclass());
                    }
                    throw new VerifiableCredentialException(
                        "Unexpected error response while listing presentations.",
                        httpStatus);
                } catch (final IOException ex) {
                    throw new VerifiableCredentialException(
                        "Unexpected I/O exception while listing presentations",
                        ex);
                }
            });
    }

    /**
     * Retrieve a verifiable presentation.
     *
     * @param presentationId the presentation identifier
     * @return the verifiable presentation
     */
    public VerifiablePresentation getPresentation(final String presentationId) {
        return awaitAsync(getPresentationAsync(Objects.requireNonNull(presentationId)));
    }

    /**
     * Retrieve a verifiable presentation.
     *
     * @param presentationId the presentation identifier
     * @return the next stage of completion, including the verifiable presentation
     */
    public CompletionStage<VerifiablePresentation> getPresentationAsync(final String presentationId) {
        final var req = Request
            .newBuilder(getPresentationEndpoint(Objects.requireNonNull(presentationId)))
                .build();
        return httpClient.sendAsync(req, VerifiableCredentialBodyHandlers.ofVerifiablePresentation())
                .thenApply(Response::body);
    }

    /**
     * Delete a presentation.
     *
     * @param presentationId the presentation identifier
     */
    public void deletePresentation(final String presentationId) {
        awaitAsync(deletePresentationAsync(Objects.requireNonNull(presentationId)));
    }

    /**
     * Delete a presentation.
     *
     * @param presentationId the presentation identifier
     * @return the next stage of completion
     */
    public CompletionStage<Void> deletePresentationAsync(final String presentationId) {
        final var req = Request.newBuilder(getPresentationEndpoint(Objects.requireNonNull(presentationId)))
            .DELETE().build();

        return httpClient.sendAsync(req, Response.BodyHandlers.discarding())
            .thenApply(res -> {
                final int httpStatus = res.statusCode();
                if (httpStatus >= 200 && httpStatus < 300) {
                    return res.body();
                }
                throw new VerifiableCredentialException(
                    "Unexpected error while deleting presentation ",
                    httpStatus);
            });
    }

    /**
     * Prove a presentation.
     *
     * @param request the prove request
     * @return the verifiable presentation
     */
    public VerifiablePresentation prove(final ProveRequest request) {
        return awaitAsync(proveAsync(request));
    }

    <T, R extends Throwable> T awaitAsync(final CompletionStage<T> future) throws R {
        try {
            return future.toCompletableFuture().join();
        } catch (final CompletionException ex) {
            throw (R) ex.getCause();
        }
    }

    /**
     * Prove a presentation.
     *
     * @param request the prove request
     * @return the next stage of completion, including the verifiable presentation
     */
    public CompletionStage<VerifiablePresentation> proveAsync(final ProveRequest request) {
        final var req = Request.newBuilder(getProveEndpoint())
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .POST(serialize(request))
            .build();

        return httpClient.sendAsync(req, VerifiableCredentialBodyHandlers.ofVerifiablePresentation())
            .thenApply(Response::body);
    }

    /**
     * Initiate a presentation exchange.
     *
     * @param exchangeId a unique identifier for a shared exchange
     * @param request the exchange request
     * @return the server-generated VP Request
     */
    // Request payload example:
    // {
    //   "query": {
    //     "type": "QueryByExample",
    //     "credentialQuery": {
    //       "type": ["VerifiableCredential", "SolidAccessGrant"],
    //       "reason": "Access to a Solid Resource"
    //     }
    //   }
    // }
    //
    // Response payload example:
    // {
    //   "query": [
    //     {
    //       "type": ["QueryByExample"],
    //       "credentialQuery": {
    //         "frame": {
    //           "@context": [...],
    //           "type": ["VerifiableCredential", "SolidAccessGrant"],
    //           "credentialSubject": { ... }
    //         }
    //       }
    //     }
    //   ],
    //   "domain": "credentials.example",
    //   "challenge": "3182bdea-63d9-11ea-b6de-3b7c1404d57f"
    // }
    public VerifiablePresentationRequest initiateExchange(final String exchangeId, final ExchangeRequest request) {
        return initiateExchangeAsync(Objects.requireNonNull(exchangeId), request).toCompletableFuture().join();
    }

    /**
     * Initiate a presentation exchange.
     *
     * @param exchangeId a unique identifier for a shared exchange
     * @param request the exchange request
     * @return the next stage of completion, including a server-generated VP Request
     */
    public CompletionStage<VerifiablePresentationRequest> initiateExchangeAsync(final String exchangeId,
            final ExchangeRequest request) {
        final var req = Request.newBuilder(getExchangeEndpoint(Objects.requireNonNull(exchangeId)))
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .POST(serialize(request))
            .build();

        return httpClient.sendAsync(req, ofVerifiablePresentationRequest())
            .thenApply(Response::body);
    }

    /**
     * Continue an existing exchange.
     *
     * @param exchangeId the exchange identifier
     * @param transactionId the transaction identifier
     * @param presentation the verifiable presentation
     */
    public void continueExchange(final String exchangeId, final String transactionId,
            final VerifiablePresentation presentation) {
        continueExchangeAsync(
                Objects.requireNonNull(exchangeId),
                Objects.requireNonNull(transactionId),
                Objects.requireNonNull(presentation))
                    .toCompletableFuture().join();
    }

    /**
     * Continue an existing exchange.
     *
     * @param exchangeId the exchange identifier
     * @param transactionId the transaction identifier
     * @param presentation the verifiable presentation
     * @return the next stage of completion
     */
    public CompletionStage<Void> continueExchangeAsync(final String exchangeId, final String transactionId,
            final VerifiablePresentation presentation) {
        // TODO - implement
        return null;
    }

    /**
     * A data structure for exchange requests when interacting with a VC Holder API.
     */
    public static class ExchangeRequest {
        /**
         * The exchange query.
         */
        public Query query;
    }

    /**
     * A data structure for query specifications when interacting with a VC Holder API.
     */
    public static class Query {
        /**
         * The query type.
         */
        public URI type;

        /**
         * The credential query.
         */
        public Map<String, Object> credentialQuery;
    }

    /**
     * A data structure for derive requests when interacting with a VC Holder API.
     */
    public static class DerivationRequest {
        /**
         * The credential to derive.
         */
        public VerifiableCredential verifiableCredential;

        /**
         * A frame for the derived credential.
         */
        public Map<String, Object> frame;

        /**
         * Options for the derive request.
         */
        public Map<String, Object> options;
    }

    /**
     * A data structure for prove requests when interacting with a VC Holder API.
     */
    public static class ProveRequest {
        /**
         * The presentation to prove.
         */
        public VerifiablePresentation presentation;

        /**
         * Options for the prove request.
         */
        public Map<String, Object> options;
    }

    private Response.BodyHandler<VerifiablePresentationRequest> ofVerifiablePresentationRequest() {
        return responseInfo -> {
            final int httpStatus = responseInfo.statusCode();
            if (httpStatus >= 200 && httpStatus < 300) {
                try (final InputStream input = new ByteArrayInputStream(responseInfo.body().array())) {
                    return jsonService.fromJson(input, VerifiablePresentationRequest.class);
                } catch (final IOException ex) {
                    throw new VerifiableCredentialException("Error parsing presentation request", ex);
                }
            }
            throw new VerifiableCredentialException(
                    "Unexpected error response when handling a verifiable presentation.",
                    httpStatus);
        };
    }

    private <T> Request.BodyPublisher serialize(final T request) {
        return IOUtils.buffer(out -> {
            try {
                jsonService.toJson(request, out);
            } catch (final IOException ex) {
                throw new VerifiableCredentialException("Error serializing JSON", ex);
            }
        });
    }

    private URI getPresentationListEndpoint(final List<URI> types) {
        return URIBuilder.newBuilder(baseUri).path(PRESENTATIONS)
            .queryParam("type", types.stream().map(URI::toString).collect(Collectors.joining(",")))
            .build();
    }

    private URI getCredentialListEndpoint(final List<URI> types) {
        return URIBuilder.newBuilder(baseUri).path(CREDENTIALS)
            .queryParam("type", types.stream().map(URI::toString).collect(Collectors.joining(",")))
            .build();
    }

    private URI getPresentationEndpoint(final String presentationId) {
        return URIBuilder.newBuilder(baseUri).path(PRESENTATIONS).path(presentationId).build();
    }

    private URI getCredentialEndpoint(final String credentialId) {
        return URIBuilder.newBuilder(baseUri).path(CREDENTIALS).path(credentialId).build();
    }

    private URI getDeriveEndpoint() {
        return URIBuilder.newBuilder(baseUri).path(CREDENTIALS).path("derive").build();
    }

    private URI getProveEndpoint() {
        return URIBuilder.newBuilder(baseUri).path(PRESENTATIONS).path("prove").build();
    }

    private URI getExchangeEndpoint(final String exchangeId) {
        return URIBuilder.newBuilder(baseUri).path(EXCHANGES).path(exchangeId).build();
    }
}
