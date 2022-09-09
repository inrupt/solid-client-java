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
import com.inrupt.client.core.InputStreamBodySubscribers;
import com.inrupt.client.core.URIBuilder;
import com.inrupt.client.spi.JsonProcessor;
import com.inrupt.client.spi.ServiceProvider;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
        final var req = HttpRequest.newBuilder(getCredentialListEndpoint(types)).build();
        try {
            final var res = httpClient.send(req, HttpResponse.BodyHandlers.ofInputStream());
            return processor.fromJson(res.body(), VerifiableCredentialList.class);
        } catch (final InterruptedException | IOException ex) {
            throw new VerifiableCredentialException("Error listing credentials.", ex);
        }
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
            .thenApply(res -> {
                try {
                    return processor.fromJson(res.body(), VerifiableCredentialList.class);
                } catch (final IOException ex) {
                    throw new VerifiableCredentialException("Error serializing credential list", ex);
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
        final var req = HttpRequest.newBuilder(getCredentialEndpoint(Objects.requireNonNull(credentialId))).build();
        try {
            return httpClient.send(req, VerifiableCredentialBodyHandlers.ofVerifiableCredential()).body();
        } catch (final InterruptedException | IOException ex) {
            throw new VerifiableCredentialException("Error retrieving credential.", ex);
        }
    }

    /**
     * Retrieve a verifiable credential.
     *
     * @param credentialId the credential identifier
     * @return the next stage of completion, including a verifiable credential
     */
    public CompletionStage<VerifiableCredential> getCredentialAsync(final String credentialId) {
        final var req = HttpRequest.newBuilder(getCredentialEndpoint(Objects.requireNonNull(credentialId))).build();
        return httpClient.sendAsync(req, VerifiableCredentialBodyHandlers.ofVerifiableCredential())
            .thenApply(HttpResponse::body);
    }

    /**
     * Delete a credential.
     *
     * @param credentialId the credential identifier
     */
    public void deleteCredential(final String credentialId) {
        final var req = HttpRequest.newBuilder(getCredentialEndpoint(Objects.requireNonNull(credentialId)))
            .DELETE().build();

        try {
            httpClient.send(req, HttpResponse.BodyHandlers.discarding());
        } catch (final InterruptedException | IOException ex) {
            throw new VerifiableCredentialException("Error retrieving credential.", ex);
        }
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
            .thenApply(HttpResponse::body);
    }

    /**
     * Derive a credential.
     *
     * @param request the selective disclosure request
     * @return the derived verifiable credential
     */
    public VerifiableCredential derive(final DerivationRequest request) {
        final var req = HttpRequest.newBuilder(getDeriveEndpoint())
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .POST(serialize(request))
            .build();

        try {
            return httpClient.send(req, VerifiableCredentialBodyHandlers.ofVerifiableCredential()).body();
        } catch (final InterruptedException | IOException ex) {
            throw new VerifiableCredentialException("Error deriving credential.", ex);
        }
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

        return httpClient.sendAsync(req, VerifiableCredentialBodyHandlers.ofVerifiableCredential())
            .thenApply(HttpResponse::body);
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
        final var req = HttpRequest.newBuilder(getPresentationListEndpoint(types)).build();
        try {
            final var res = httpClient.send(req, HttpResponse.BodyHandlers.ofInputStream());
            return processor.fromJson(res.body(), VerifiablePresentationList.class);
        } catch (final InterruptedException | IOException ex) {
            throw new VerifiableCredentialException("Error listing credentials.", ex);
        }
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
        final var req = HttpRequest.newBuilder(getPresentationListEndpoint(types)).build();
        return httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofInputStream())
            .thenApply(res -> {
                try {
                    return processor.fromJson(res.body(), VerifiablePresentationList.class);
                } catch (final IOException ex) {
                    throw new VerifiableCredentialException("Error serializing presentation list", ex);
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
        final var req = HttpRequest.newBuilder(getPresentationEndpoint(Objects.requireNonNull(presentationId))).build();
        try {
            return httpClient.send(req, VerifiableCredentialBodyHandlers.ofVerifiablePresentation()).body();
        } catch (final InterruptedException | IOException ex) {
            throw new VerifiableCredentialException("Error retrieving presentation.", ex);
        }
    }

    /**
     * Retrieve a verifiable presentation.
     *
     * @param presentationId the presentation identifier
     * @return the next stage of completion, including the verifiable presentation
     */
    public CompletionStage<VerifiablePresentation> getPresentationAsync(final String presentationId) {
        final var req = HttpRequest.newBuilder(getPresentationEndpoint(Objects.requireNonNull(presentationId))).build();
        return httpClient.sendAsync(req, VerifiableCredentialBodyHandlers.ofVerifiablePresentation())
            .thenApply(HttpResponse::body);
    }

    /**
     * Delete a presentation.
     *
     * @param presentationId the presentation identifier
     */
    public void deletePresentation(final String presentationId) {
        final var uri = URIBuilder.newBuilder(baseUri).path("credentials")
            .path(Objects.requireNonNull(presentationId)).build();
        final var req = HttpRequest.newBuilder(uri).DELETE().build();

        try {
            httpClient.send(req, HttpResponse.BodyHandlers.discarding());
        } catch (final InterruptedException | IOException ex) {
            throw new VerifiableCredentialException("Error deleting credential.", ex);
        }
    }

    /**
     * Delete a presentation.
     *
     * @param presentationId the presentation identifier
     * @return the next stage of completion
     */
    public CompletionStage<Void> deletePresentationAsync(final String presentationId) {
        final var uri = URIBuilder.newBuilder(baseUri).path("credentials")
            .path(Objects.requireNonNull(presentationId)).build();
        final var req = HttpRequest.newBuilder(uri).DELETE().build();

        return httpClient.sendAsync(req, HttpResponse.BodyHandlers.discarding())
            .thenApply(HttpResponse::body);
    }

    /**
     * Prove a presentation.
     *
     * @param request the prove request
     * @return the verifiable presentation
     */
    public VerifiablePresentation prove(final ProveRequest request) {
        final var req = HttpRequest.newBuilder(getProveEndpoint())
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .POST(serialize(request))
            .build();

        try {
            return httpClient.send(req, VerifiableCredentialBodyHandlers.ofVerifiablePresentation()).body();
        } catch (final InterruptedException | IOException ex) {
            throw new VerifiableCredentialException("Error proving presentation.", ex);
        }
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

        return httpClient.sendAsync(req, VerifiableCredentialBodyHandlers.ofVerifiablePresentation())
            .thenApply(HttpResponse::body);
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
    //       "type": "QueryByExample",
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
        final var req = HttpRequest.newBuilder(getExchangeEndpoint(exchangeId))
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .POST(serialize(request))
            .build();

        try {
            return httpClient.send(req, ofVerifiablePresentationRequest()).body();
        } catch (final InterruptedException | IOException ex) {
            throw new VerifiableCredentialException("Error proving presentation.", ex);
        }
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
        final var req = HttpRequest.newBuilder(getExchangeEndpoint(exchangeId))
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .POST(serialize(request))
            .build();

        return httpClient.sendAsync(req, ofVerifiablePresentationRequest())
            .thenApply(HttpResponse::body);
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
        // TODO - implement
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

    private HttpResponse.BodyHandler<VerifiablePresentationRequest> ofVerifiablePresentationRequest() {
        return responseInfo ->
            InputStreamBodySubscribers.mapping(input -> {
                try {
                    return processor.fromJson(input, VerifiablePresentationRequest.class);
                } catch (final IOException ex) {
                    throw new VerifiableCredentialException("Error parsing presentation request", ex);
                }
            });
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

    interface VerifiablePresentationList extends List<VerifiablePresentation> {
    }

    interface VerifiableCredentialList extends List<VerifiableCredential> {
    }
}
