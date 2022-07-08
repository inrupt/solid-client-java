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
package com.inrupt.client.openid;

import com.inrupt.client.authentication.DPoP;
import com.inrupt.client.spi.JsonProcessor;
import com.inrupt.client.spi.ServiceLoadingException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.concurrent.CompletionStage;

public class OpenIdProvider {

    private final URI issuer;
    private final DPoP dpop;
    private final HttpClient client;
    private final JsonProcessor processor;

    public OpenIdProvider(final URI issuer) {
        this(issuer, new DPoP());
    }

    public OpenIdProvider(final URI issuer, final DPoP dpop) {
        this(issuer, dpop, HttpClient.newHttpClient());
    }

    public OpenIdProvider(final URI issuer, final DPoP dpop, final HttpClient client) {
        this.issuer = Objects.requireNonNull(issuer);
        this.dpop = Objects.requireNonNull(dpop);
        this.client = Objects.requireNonNull(client);
        this.processor = ServiceLoader.load(JsonProcessor.class).findFirst()
            .orElseThrow(() -> new ServiceLoadingException(
                        "Unable to load JSON processor. " +
                        "Please ensure that a JSON processor is available on the classpath"));
    }

    public Metadata metadata() {
        final var req = HttpRequest.newBuilder(getMetadataUrl()).header("Accept", "application/json").build();
        try {
            final var res = client.send(req, HttpResponse.BodyHandlers.ofInputStream());
            return processor.fromJson(res.body(), Metadata.class);
        } catch (final InterruptedException | IOException ex) {
            throw new OpenIdException("Error fetching OpenID metadata resource", ex);
        }
    }

    public CompletionStage<Metadata> metadataAsync() {
        final var req = HttpRequest.newBuilder(getMetadataUrl()).header("Accept", "application/json").build();
        return client.sendAsync(req, HttpResponse.BodyHandlers.ofInputStream())
            .thenApply(res -> processor.fromJson(res.body(), Metadata.class));
    }

    private URI getMetadataUrl() {
        if (issuer.toString().endsWith("/")) {
            return URI.create(issuer + ".well-known/openid-configuration");
        }
        return URI.create(issuer + "/.well-known/openid-configuration");
    }
}
