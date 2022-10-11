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

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletionStage;

/**
 * A class for interacting with a VC-API Issuer endpoint.
 *
 * @see <a href="https://w3c-ccg.github.io/vc-api/issuer.html">VC-API: Issuer</a>
 */
public class Issuer {

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";

    private final URI baseUri;
    private final HttpClient httpClient;
    private final JsonProcessor processor;

    /**
     * Create a new Issuer object.
     *
     * @param baseUri the base URL for the VC-API
     */
    public Issuer(final URI baseUri) {
        this(baseUri, HttpClient.newHttpClient());
    }

    /**
     * Create a new Issuer object for interacting with a VC-API.
     *
     * @param baseUri the base URI for the VC-API
     * @param httpClient an HTTP client
     */
    public Issuer(final URI baseUri, final HttpClient httpClient) {
        this.baseUri = baseUri;
        this.httpClient = httpClient;
        this.processor = ServiceProvider.getJsonProcessor();
    }

    /**
     * Synchronously issue a new verifiable credential.
     *
     * @param credential the credential to be signed and issued
     * @return a new Verifiable Credential
     */
    public VerifiableCredential issue(final VerifiableCredential credential) {
        final var req = HttpRequest.newBuilder(getIssueUrl())
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .POST(VerifiableCredentialBodyPublishers.ofVerifiableCredential(credential))
            .build();
        try {
            return httpClient
                .send(req, VerifiableCredentialBodyHandlers.ofVerifiableCredential())
                .body();
        } catch (final InterruptedException | IOException ex) {
            //throw new VerifiableCredentialException("Error issuing verifiable credential", ex);
            throw VerifiableCredentialException.newBuilder()
                .message("Error issuing verifiable credential")
                .exception(ex)
                .build();
        }
    }

    /**
     * Asynchronously issue a new verifiable credential.
     *
     * @param credential the credential to be signed and issued
     * @return the next stage of completion, containing the new Verifiable Credential
     */
    public CompletionStage<VerifiableCredential> issueAsync(final VerifiableCredential credential) {
        final var req = HttpRequest.newBuilder(getIssueUrl())
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .POST(VerifiableCredentialBodyPublishers.ofVerifiableCredential(credential))
            .build();
        return httpClient
                .sendAsync(req, VerifiableCredentialBodyHandlers.ofVerifiableCredential())
                .thenApply(HttpResponse::body);
    }

    /**
     * Synchronously update the status of a verifiable credential.
     *
     * @param request the status request
     */
    public void status(final StatusRequest request) {
        final var req = HttpRequest.newBuilder(getStatusUrl())
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .POST(ofStatusRequest(request))
            .build();

        try {
            httpClient.send(req, HttpResponse.BodyHandlers.discarding());
        } catch (final InterruptedException | IOException ex) {
            //throw new VerifiableCredentialException("Error updating credential status.", ex);
            throw VerifiableCredentialException.newBuilder()
                .message("Error updating credential status.")
                .exception(ex)
                .build();
        }
    }

    /**
     * Asynchronously update the status of a verifiable credential.
     *
     * @param request the status request
     * @return the next stage of completion
     */
    public CompletionStage<Void> statusAsync(final StatusRequest request) {
        final var req = HttpRequest.newBuilder(getStatusUrl())
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .POST(ofStatusRequest(request))
            .build();

        return httpClient.sendAsync(req, HttpResponse.BodyHandlers.discarding())
            .thenApply(HttpResponse::body);
    }

    /**
     * A credential status request.
     */
    public static final class StatusRequest {
        private String credentialId;
        private List<CredentialStatus> credentialStatus;

        /**
         * Get the credential identifier.
         *
         * @return the credential identifier
         */
        public String getCredentialId() {
            return credentialId;
        }

        /**
         * Get the credential status values.
         *
         * @return the credential status values
         */
        public List<CredentialStatus> getCredentialStatus() {
            return credentialStatus;
        }

        private StatusRequest(final String credentialId, final List<CredentialStatus> credentialStatus) {
            this.credentialId = credentialId;
            this.credentialStatus = credentialStatus;
        }

        /**
         * A builder of {@link StatusRequest} objects.
         */
        public static final class Builder {

            private List<CredentialStatus> builderCredentialStatus = new ArrayList<>();

            /**
             * Create a status request builder.
             *
             * @return the builder object
             */
            public static Builder newBuilder() {
                return new Builder();
            }

            /**
             * Add a credential status declaration.
             *
             * @param type the type URI
             * @param revoked true if the credential is to be revoked; false otherwise
             * @return this builder object
             */
            public Builder credentialStatus(final URI type, final boolean revoked) {
                builderCredentialStatus.add(new CredentialStatus(type, revoked));
                return this;
            }

            /**
             * Build a {@link StatusRequest} object.
             *
             * @param credentialId the credential identifier
             * @return the status request
             */
            public StatusRequest build(final String credentialId) {
                return new StatusRequest(Objects.requireNonNull(credentialId), builderCredentialStatus);
            }
        }
    }

    /**
     * A credendial status data holder.
     */
    public static final class CredentialStatus {

        private final URI type;
        private final boolean status;

        /**
         * Get the credential status type.
         *
         * @return the type URI
         */
        public URI getType() {
            return type;
        }

        /**
         * Get the status value.
         *
         * <p>False is active, true is revoked
         *
         * @return true if the credential has been revoked; false otherwise
         */
        public Boolean getStatus() {
            return status;
        }

        private CredentialStatus(final URI type, final boolean status) {
            this.type = type;
            this.status = status;
        }
    }

    private HttpRequest.BodyPublisher ofStatusRequest(final StatusRequest request) {
        return HttpRequest.BodyPublishers.ofInputStream(() ->
                IOUtils.pipe(out -> {
                    try {
                        processor.toJson(request, out);
                    } catch (final IOException ex) {
                        //throw new VerifiableCredentialException("Error serializing status request", ex);
                        throw VerifiableCredentialException.newBuilder()
                            .message("Error serializing status request.")
                            .exception(ex)
                            .build();
                    }
                }));
    }

    private URI getIssueUrl() {
        return URIBuilder.newBuilder(baseUri).path("credentials/issue").build();
    }

    private URI getStatusUrl() {
        return URIBuilder.newBuilder(baseUri).path("credentials/status").build();
    }
}
