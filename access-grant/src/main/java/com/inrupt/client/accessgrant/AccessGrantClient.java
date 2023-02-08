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
package com.inrupt.client.accessgrant;

import com.inrupt.client.Request;
import com.inrupt.client.Response;
import com.inrupt.client.spi.HttpService;
import com.inrupt.client.spi.JsonService;
import com.inrupt.client.spi.ServiceProvider;
import com.inrupt.client.util.URIBuilder;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * A client for interacting with Access Grant Resources.
 */
public class AccessGrantClient {

    public static final URI GCONSENT = URI.create("https://w3id.org/GConsent");
    public static final URI ODRL = URI.create("http://www.w3.org/ns/odrl/2/");
    private static final Set<URI> SUPPORTED_SCHEMA = Collections
        .unmodifiableSet(new HashSet<>(Arrays.asList(GCONSENT, ODRL)));

    private final HttpService httpClient;
    private final JsonService jsonService;
    private final URI issuer;
    private final URI schema;

    public AccessGrantClient(final URI issuer, final URI schema) {
        this(issuer, schema, ServiceProvider.getHttpService());
    }

    public AccessGrantClient(final URI issuer, final URI schema, final HttpService httpClient) {
        this.issuer = Objects.requireNonNull(issuer);
        this.schema = Objects.requireNonNull(schema);
        this.httpClient = Objects.requireNonNull(httpClient);
        this.jsonService = ServiceProvider.getJsonService();

        if (!SUPPORTED_SCHEMA.contains(schema)) {
            throw new IllegalArgumentException("Unsupported schema: " + schema);
        }
    }

    private CompletionStage<Metadata> v2Metadata() {
        final Metadata metadata = new Metadata();
        metadata.issueEndpoint = URIBuilder.newBuilder(issuer).path("credentials").path("issue").build();
        metadata.queryEndpoint = URIBuilder.newBuilder(issuer).path("query").build();
        metadata.verifyEndpoint = URIBuilder.newBuilder(issuer).path("credentials").path("verify").build();
        metadata.statusEndpoint = URIBuilder.newBuilder(issuer).path("credentials").path("status").build();
        return CompletableFuture.completedFuture(metadata);
    }

    private CompletionStage<Metadata> v1Metadata() {
        final URI uri = URIBuilder.newBuilder(issuer).path(".well-known/vc-configuration").build();
        final Request req = Request.newBuilder(uri).header("Accept", "application/json").build();
        return httpClient.send(req, Response.BodyHandlers.ofInputStream())
            .thenApply(res -> {
                try {
                    final int httpStatus = res.statusCode();
                    if (httpStatus >= 200 && httpStatus < 300) {
                        return jsonService.fromJson(res.body(), V1Metadata.class);
                    }
                    throw new AccessGrantException(
                            "Unexpected error while fetching the Access Grant metadata.", httpStatus);
                } catch (final IOException ex) {
                    throw new AccessGrantException(
                            "Unexpected I/O exception while fetching the Access Grant metadata resource.", ex);
                }
            })
            .thenApply(metadata -> {
                final Metadata m = new Metadata();
                m.queryEndpoint = metadata.derivationService;
                m.issueEndpoint = metadata.issuerService;
                m.verifyEndpoint = metadata.verifierService;
                m.statusEndpoint = metadata.statusService;
                return m;
            });
    }

    public CompletionStage<AccessGrant> issue(final URI agent, final Set<URI> resources, final Instant expiration,
            final Set<String> purposes) {
        // TODO implement
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public CompletionStage<Void> revoke(final AccessGrant accessGrant) {
        // TODO implement
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public CompletionStage<Void> delete(final AccessGrant accessGrant) {
        // TODO implement
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public CompletionStage<List<AccessGrant>> search(final URI agent, final URI type, final URI resource) {
        // TODO implement
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
