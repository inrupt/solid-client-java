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

import com.inrupt.client.common.IOUtils;
import com.inrupt.client.common.URIBuilder;
import com.inrupt.client.spi.JsonProcessor;
import com.inrupt.client.spi.ServiceProvider;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletionStage;

/**
 * A class for interacting with a VC-API Holder endpoint.
 *
 * @see <a href="https://w3c-ccg.github.io/vc-api/holder.html">VC-API: Holder</a>
 */
public class Holder {

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";

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

    public List<VerifiableCredential> listCredentials(final List<URI> types) {
        // TODO - implement
        return null;
    }

    public CompletionStage<List<VerifiableCredential>> listCredentialsAsync(final List<URI> types) {
        // TODO - implement
        return null;
    }

    public VerifiableCredential getCredential(final String credentialId) {
        // TODO - implement
        return null;
    }

    public CompletionStage<VerifiableCredential> getCredentialAsync(final String credentialId) {
        // TODO - implement
        return null;
    }

    public void deleteCredential(final String credentialId) {
        // TODO - implement
    }

    public CompletionStage<Void> deleteCredentialAsync(final String credentialId) {
        // TODO - implement
        return null;
    }

    public VerifiableCredential derive(final DerivationRequest request) {
        final var req = HttpRequest.newBuilder(getDeriveEndpoint())
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .POST(ofDerivationRequest(request))
            .build();

        try {
            return httpClient.send(req, VerifiableCredentialBodyHandlers.ofVerifiableCredential()).body();
        } catch (final InterruptedException | IOException ex) {
            throw new VerifiableCredentialException("Error deriving credential.", ex);
        }
    }

    public CompletionStage<VerifiableCredential> deriveAsync(final DerivationRequest request) {
        final var req = HttpRequest.newBuilder(getDeriveEndpoint())
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .POST(ofDerivationRequest(request))
            .build();

        return httpClient.sendAsync(req, VerifiableCredentialBodyHandlers.ofVerifiableCredential())
            .thenApply(HttpResponse::body);
    }

    public List<VerifiablePresentation> listPresentations(final List<URI> types) {
        // TODO - implement
        return null;
    }

    public CompletionStage<List<VerifiablePresentation>> listPresentationsAsync(final List<URI> types) {
        // TODO - implement
        return null;
    }

    public VerifiablePresentation getPresentation(final String presentationId) {
        // TODO - implement
        return null;
    }

    public CompletionStage<VerifiablePresentation> getPresenationAsync(final String presentationId) {
        // TODO - implement
        return null;
    }

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

    public CompletionStage<Void> deletePresentationAsync(final String presentationId) {
        final var uri = URIBuilder.newBuilder(baseUri).path("credentials")
            .path(Objects.requireNonNull(presentationId)).build();
        final var req = HttpRequest.newBuilder(uri).DELETE().build();

        return httpClient.sendAsync(req, HttpResponse.BodyHandlers.discarding())
            .thenApply(HttpResponse::body);
    }

    public VerifiablePresentation prove(final ProveRequest request) {
        final var req = HttpRequest.newBuilder(getProveEndpoint())
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .POST(ofProveRequest(request))
            .build();

        try {
            return httpClient.send(req, VerifiableCredentialBodyHandlers.ofVerifiablePresentation()).body();
        } catch (final InterruptedException | IOException ex) {
            throw new VerifiableCredentialException("Error proving presentation.", ex);
        }
    }

    public CompletionStage<VerifiablePresentation> proveAsync(final ProveRequest request) {
        final var req = HttpRequest.newBuilder(getProveEndpoint())
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .POST(ofProveRequest(request))
            .build();

        return httpClient.sendAsync(req, VerifiableCredentialBodyHandlers.ofVerifiablePresentation())
            .thenApply(HttpResponse::body);
    }

    public VerifiablePresentationRequest initiateExchange(final String exchangeId, final ExchangeRequest request) {
        // TODO - implement
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
        //   "domain": "credentials.example.com",
        //   "challenge": "3182bdea-63d9-11ea-b6de-3b7c1404d57f"
        // }
        return null;
    }

    public CompletionStage<VerifiablePresentationRequest> initiateExchangeAsync(final String exchangeId,
            final ExchangeRequest request) {
        // TODO - implement
        return null;
    }

    public void continueExchange(final String exchangeId, final String transactionId,
            final VerifiablePresentation presentation) {
        // TODO - implement
    }

    public CompletionStage<Void> continueExchangeAsync(final String exchangeId, final String transactionId,
            final VerifiablePresentation presentation) {
        // TODO - implement
        return null;
    }

    public static class ExchangeRequest {
        public Query query;
    }

    public static class Query {
        public URI type;
        public Map<String, Object> credentialSubject;
    }

    public static class DerivationRequest {
        // TODO - implement

    }

    public static class ProveRequest {
        // TODO - implement

    }

    private HttpRequest.BodyPublisher ofDerivationRequest(final DerivationRequest request) {
        return serialize(request);
    }

    private HttpRequest.BodyPublisher ofProveRequest(final ProveRequest request) {
        return serialize(request);
    }

    private <T> HttpRequest.BodyPublisher serialize(final T request) {
        return HttpRequest.BodyPublishers.ofInputStream(() ->
                IOUtils.pipe(out -> processor.toJson(request, out)));
    }

    private URI getDeriveEndpoint() {
        return URIBuilder.newBuilder(baseUri).path("credentials/derive").build();
    }

    private URI getProveEndpoint() {
        return URIBuilder.newBuilder(baseUri).path("presentations/prove").build();
    }
}
