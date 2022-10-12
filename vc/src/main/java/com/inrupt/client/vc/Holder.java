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

import com.inrupt.client.core.IOUtils;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
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
    private final HttpClient httpClient;
    private final JsonProcessor processor;

    private static final int SUCCESS = 200;
    private static final int OTHER_SUCCESS = 201;
    private static final int BAD_REQUEST = 400;
    private static final int NOT_AUTHORIZED = 401;
    private static final int NOT_FOUND = 404;
    private static final int THERE_IS_NO_DATA_HERE = 410;
    private static final int TEAPOT = 418;
    private static final int ERROR = 500;
    private static final int NOT_IMPLEMENTED = 501;

    private static final String BAD_REQUEST_MESSAGE = "Bad Request";
    private static final String INVALID_REQUEST_MESSAGE = "Invalid Request";
    private static final String REQUEST_MALFORMED_MESSAGE = "Request is malformed";
    private static final String NOT_AUTHORIZED_MESSAGE = "Not Authorized";
    private static final String THERE_IS_NO_DATA_HERE_MESSAGE = "Gone! There is no data here";
    private static final String TEAPOT_MESSAGE =
        "I'm a teapot - MUST not be returned outside of pre-arranged scenarios between both parties";
    private static final String ERROR_MESSAGE = "Internal Error";
    private static final String NOT_IMPLEMENTED_MESSAGE = "Not Implemented";
    private static final String SERVICE_NOT_IMPLEMENTED_MESSAGE = "Service not implemented";

    /**
     * Create a new Holder object for interacting with a VC-API.
     *
     * @param baseUri the base URI for the VC-API
     */
    public Holder(final URI baseUri) {
        this(baseUri, HttpClient.newHttpClient());
    }

    /**
     * Create a new Holder object for interacting with a VC-API.
     *
     * @param baseUri the base URI for the VC-API
     * @param httpClient an HTTP client
     */
    public Holder(final URI baseUri, final HttpClient httpClient) {
        this.baseUri = baseUri;
        this.httpClient = httpClient;
        this.processor = ServiceProvider.getJsonProcessor();
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
        return listCredentialsAsync(Objects.requireNonNull(types)).toCompletableFuture().join();
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
        final var req = HttpRequest.newBuilder(getCredentialListEndpoint(Objects.requireNonNull(types))).build();
        return httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofInputStream())
            .thenCompose(res -> {
                try {
                    if (SUCCESS == res.statusCode()) {
                        return CompletableFuture.completedFuture(processor.fromJson(res.body(),
                            new ArrayList<VerifiableCredential>(){}.getClass().getGenericSuperclass()));
                    }
                    if (BAD_REQUEST == res.statusCode()) {
                        throw new VerifiableCredentialException(BAD_REQUEST_MESSAGE, res.statusCode());
                    }
                    if (NOT_AUTHORIZED == res.statusCode()) {
                        throw new VerifiableCredentialException(NOT_AUTHORIZED_MESSAGE, res.statusCode());
                    }
                    if (THERE_IS_NO_DATA_HERE == res.statusCode()) {
                        throw new VerifiableCredentialException(THERE_IS_NO_DATA_HERE_MESSAGE, res.statusCode());
                    }
                    if (ERROR == res.statusCode()) {
                        throw new VerifiableCredentialException(ERROR_MESSAGE, res.statusCode());
                    }
                    if (NOT_IMPLEMENTED == res.statusCode()) {
                        throw new VerifiableCredentialException(NOT_IMPLEMENTED_MESSAGE, res.statusCode());
                    }
                    throw new VerifiableCredentialException("Unexpected error response while listing credentials");
                } catch (final IOException ex) {
                    throw new VerifiableCredentialException(
                        "Unexpected I/O exception while listing credentials",
                        "Types were: " + Arrays.toString(types.toArray()),
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
        final var req = HttpRequest.newBuilder(getCredentialEndpoint(Objects.requireNonNull(credentialId))).build();
        return httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofInputStream())
            .thenCompose(res -> {
                try {
                    if (SUCCESS == res.statusCode()) {
                        return CompletableFuture.completedFuture(
                            processor.fromJson(res.body(), VerifiableCredential.class));
                    }
                    if (BAD_REQUEST == res.statusCode()) {
                        throw new VerifiableCredentialException(BAD_REQUEST_MESSAGE, res.statusCode());
                    }
                    if (NOT_AUTHORIZED == res.statusCode()) {
                        throw new VerifiableCredentialException(NOT_AUTHORIZED_MESSAGE, res.statusCode());
                    }
                    if (THERE_IS_NO_DATA_HERE == res.statusCode()) {
                        throw new VerifiableCredentialException(THERE_IS_NO_DATA_HERE_MESSAGE, res.statusCode());
                    }
                    if (TEAPOT == res.statusCode()) {
                        throw new VerifiableCredentialException(
                            TEAPOT_MESSAGE,
                            res.statusCode());
                    }
                    if (ERROR == res.statusCode()) {
                        throw new VerifiableCredentialException(ERROR_MESSAGE, res.statusCode());
                    }
                    if (NOT_IMPLEMENTED == res.statusCode()) {
                        throw new VerifiableCredentialException(NOT_AUTHORIZED_MESSAGE, res.statusCode());
                    }
                    throw new VerifiableCredentialException("Unexpected error response while getting a credential");
                } catch (final IOException ex) {
                    throw new VerifiableCredentialException(
                        "Unexpected I/O exception while getting a credential",
                        "Credential identifier was: " + credentialId,
                        ex);
                }
            });
    }

    /**
     * Delete a credential.
     *
     * @param credentialId the credential identifier
     */
    public void deleteCredential(final String credentialId) {
        deleteCredentialAsync(Objects.requireNonNull(credentialId)).toCompletableFuture().join();
    }

    /**
     * Delete a credential.
     *
     * @param credentialId the credential identifier
     * @return the next stage of compltion
     */
    public CompletionStage<Void> deleteCredentialAsync(final String credentialId) {
        final var req = HttpRequest.newBuilder(getCredentialEndpoint(Objects.requireNonNull(credentialId)))
            .DELETE().build();

        return httpClient.sendAsync(req, HttpResponse.BodyHandlers.discarding())
            .thenCompose(res -> {
                if (SUCCESS == res.statusCode()) {
                    return CompletableFuture.completedFuture(res.body());
                }
                if (NOT_FOUND == res.statusCode()) {
                    throw new VerifiableCredentialException(
                        "Credential not found",
                        "Credential identifier was: " + credentialId,
                        res.statusCode());
                }
                if (ERROR == res.statusCode()) {
                    throw new VerifiableCredentialException(ERROR_MESSAGE, res.statusCode());
                }
                throw new VerifiableCredentialException(
                    "Unexpected error while deleting credential",
                    "Credential identifier was: " + credentialId,
                    res.statusCode());
            });
    }

    /**
     * Derive a credential.
     *
     * @param request the selective disclosure request
     * @return the derived verifiable credential
     */
    public VerifiableCredential derive(final DerivationRequest request) {
        return deriveAsync(request).toCompletableFuture().join();
    }

    /**
     * Derive a credential.
     *
     * @param request the selective disclosure request
     * @return the next stage of completion, including the derived verifiable credential
     */
    public CompletionStage<VerifiableCredential> deriveAsync(final DerivationRequest request) {
        final var req = HttpRequest.newBuilder(getDeriveEndpoint())
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .POST(serialize(request))
            .build();

        return httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofInputStream())
            .thenCompose(res -> {
                try {
                    if (OTHER_SUCCESS == res.statusCode()) {
                        return CompletableFuture.completedFuture(
                            processor.fromJson(res.body(),
                            VerifiableCredential.class));
                    }
                    if (BAD_REQUEST == res.statusCode()) {
                        throw new VerifiableCredentialException(INVALID_REQUEST_MESSAGE, res.statusCode());
                    }
                    if (ERROR == res.statusCode()) {
                        throw new VerifiableCredentialException(ERROR_MESSAGE, res.statusCode());
                    }
                    if (NOT_IMPLEMENTED == res.statusCode()) {
                        throw new VerifiableCredentialException(NOT_AUTHORIZED_MESSAGE, res.statusCode());
                    }
                    throw new VerifiableCredentialException(
                        "Unexpected error response while deriving a credential");
                } catch (final IOException ex) {
                    throw new VerifiableCredentialException(
                        "Unexpected I/O exception while deriving a credential",
                        ex);
                }
            });
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
        return listPresentationsAsync(Objects.requireNonNull(types)).toCompletableFuture().join();
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
        final var req = HttpRequest.newBuilder(getPresentationListEndpoint(Objects.requireNonNull(types))).build();
        return httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofInputStream())
            .thenCompose(res -> {
                try {
                    if (SUCCESS == res.statusCode()) {
                        return CompletableFuture.completedFuture(processor.fromJson(res.body(),
                            new ArrayList<VerifiablePresentation>(){}.getClass().getGenericSuperclass()));
                    }
                    if (BAD_REQUEST == res.statusCode()) {
                        throw new VerifiableCredentialException(BAD_REQUEST_MESSAGE, res.statusCode());
                    }
                    if (NOT_AUTHORIZED == res.statusCode()) {
                        throw new VerifiableCredentialException(NOT_AUTHORIZED_MESSAGE, res.statusCode());
                    }
                    if (THERE_IS_NO_DATA_HERE == res.statusCode()) {
                        throw new VerifiableCredentialException(THERE_IS_NO_DATA_HERE_MESSAGE, res.statusCode());
                    }
                    if (ERROR == res.statusCode()) {
                        throw new VerifiableCredentialException(ERROR_MESSAGE, res.statusCode());
                    }
                    if (NOT_IMPLEMENTED == res.statusCode()) {
                        throw new VerifiableCredentialException(NOT_IMPLEMENTED_MESSAGE, res.statusCode());
                    }
                    throw new VerifiableCredentialException("Unexpected error response while listing presentations");
                } catch (final IOException ex) {
                    throw new VerifiableCredentialException(
                        "Unexpected I/O exception while listing presentations",
                        "Types were: " + Arrays.toString(types.toArray()),
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
        return getPresentationAsync(Objects.requireNonNull(presentationId))
            .toCompletableFuture()
            .join();
    }

    /**
     * Retrieve a verifiable presentation.
     *
     * @param presentationId the presentation identifier
     * @return the next stage of completion, including the verifiable presentation
     */
    public CompletionStage<VerifiablePresentation> getPresentationAsync(final String presentationId) {
        final var req = HttpRequest
            .newBuilder(getPresentationEndpoint(Objects.requireNonNull(presentationId)))
            .build();
        return httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofInputStream())
            .thenCompose(res -> {
                try {
                    if (SUCCESS == res.statusCode()) {
                        return CompletableFuture.completedFuture(
                            processor.fromJson(res.body(),
                            VerifiablePresentation.class));
                    }
                    if (BAD_REQUEST == res.statusCode()) {
                        throw new VerifiableCredentialException(BAD_REQUEST_MESSAGE, res.statusCode());
                    }
                    if (NOT_AUTHORIZED == res.statusCode()) {
                        throw new VerifiableCredentialException(NOT_AUTHORIZED_MESSAGE, res.statusCode());
                    }
                    if (THERE_IS_NO_DATA_HERE == res.statusCode()) {
                        throw new VerifiableCredentialException(THERE_IS_NO_DATA_HERE_MESSAGE, res.statusCode());
                    }
                    if (TEAPOT == res.statusCode()) {
                        throw new VerifiableCredentialException(
                            TEAPOT_MESSAGE,
                            res.statusCode());
                    }
                    if (ERROR == res.statusCode()) {
                        throw new VerifiableCredentialException(ERROR_MESSAGE, res.statusCode());
                    }
                    if (NOT_IMPLEMENTED == res.statusCode()) {
                        throw new VerifiableCredentialException(NOT_IMPLEMENTED_MESSAGE, res.statusCode());
                    }
                    throw new VerifiableCredentialException(
                        "Unexpected error response while getting a presentation",
                        "Presentation identifier was: " + presentationId);
                } catch (final IOException ex) {
                    throw new VerifiableCredentialException(
                        "Unexpected I/O exception while getting a presentation",
                        "Presentation identifier was: " + presentationId,
                        ex);
                }
            });
    }

    /**
     * Delete a presentation.
     *
     * @param presentationId the presentation identifier
     */
    public void deletePresentation(final String presentationId) {
        deleteCredentialAsync(Objects.requireNonNull(presentationId)).toCompletableFuture().join();
    }

    /**
     * Delete a presentation.
     *
     * @param presentationId the presentation identifier
     * @return the next stage of completion
     */
    public CompletionStage<Void> deletePresentationAsync(final String presentationId) {
        final var req = HttpRequest.newBuilder(getPresentationEndpoint(Objects.requireNonNull(presentationId)))
            .DELETE().build();

        return httpClient.sendAsync(req, HttpResponse.BodyHandlers.discarding())
            .thenCompose(res -> {
                if (SUCCESS == res.statusCode()) {
                    return CompletableFuture.completedFuture(res.body());
                }
                if (NOT_FOUND == res.statusCode()) {
                    throw new VerifiableCredentialException(
                        "Presentation not found",
                        "Presentation identifier was: " + presentationId,
                        res.statusCode());
                }
                if (ERROR == res.statusCode()) {
                    throw new VerifiableCredentialException(ERROR_MESSAGE, res.statusCode());
                }
                throw new VerifiableCredentialException(
                    "Unexpected error while deleting presentation",
                    "Presentation identifier was: " + presentationId,
                    res.statusCode());
            });
    }

    /**
     * Prove a presentation.
     *
     * @param request the prove request
     * @return the verifiable presentation
     */
    public VerifiablePresentation prove(final ProveRequest request) {
        return proveAsync(request).toCompletableFuture().join();
    }

    /**
     * Prove a presentation.
     *
     * @param request the prove request
     * @return the next stage of completion, including the verifiable presentation
     */
    public CompletionStage<VerifiablePresentation> proveAsync(final ProveRequest request) {
        final var req = HttpRequest.newBuilder(getProveEndpoint())
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .POST(serialize(request))
            .build();

        return httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofInputStream())
            .thenCompose(res -> {
                try {
                    if (OTHER_SUCCESS == res.statusCode()) {
                        return CompletableFuture.completedFuture(
                            processor.fromJson(res.body(), VerifiablePresentation.class));
                    }
                    if (BAD_REQUEST == res.statusCode()) {
                        throw new VerifiableCredentialException(INVALID_REQUEST_MESSAGE, res.statusCode());
                    }
                    if (ERROR == res.statusCode()) {
                        throw new VerifiableCredentialException(ERROR_MESSAGE, res.statusCode());
                    }
                    throw new VerifiableCredentialException(
                        "Unexpected error response while proving a presentation");
                } catch (final IOException ex) {
                    throw new VerifiableCredentialException(
                        "Unexpected I/O exception while proving a presentation",
                        ex);
                }
            });
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
        final var req = HttpRequest.newBuilder(getExchangeEndpoint(Objects.requireNonNull(exchangeId)))
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .POST(serialize(request))
            .build();

        return httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofInputStream())
            .thenCompose(res -> {
                try {
                    if (SUCCESS == res.statusCode()) {
                        return CompletableFuture.completedFuture(
                            processor.fromJson(res.body(), VerifiablePresentationRequest.class));
                    }
                    if (BAD_REQUEST == res.statusCode()) {
                        throw new VerifiableCredentialException(REQUEST_MALFORMED_MESSAGE, res.statusCode());
                    }
                    if (ERROR == res.statusCode()) {
                        throw new VerifiableCredentialException(ERROR_MESSAGE, res.statusCode());
                    }
                    if (NOT_IMPLEMENTED == res.statusCode()) {
                        throw new VerifiableCredentialException(SERVICE_NOT_IMPLEMENTED_MESSAGE, res.statusCode());
                    }
                    throw new VerifiableCredentialException(
                        "Unexpected error response while initiating an exchnage of information");
                } catch (final IOException ex) {
                    throw new VerifiableCredentialException(
                        "Unexpected I/O exception while initiating an exchnage of information",
                        ex);
                }
            });
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

    private <T> HttpRequest.BodyPublisher serialize(final T request) {
        return HttpRequest.BodyPublishers.ofInputStream(() ->
                IOUtils.pipe(out -> {
                    try {
                        processor.toJson(request, out);
                    } catch (final IOException ex) {
                        throw new VerifiableCredentialException("Error serializing JSON", ex);
                    }
                }));
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
