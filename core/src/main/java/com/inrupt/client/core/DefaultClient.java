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
package com.inrupt.client.core;

import com.inrupt.client.Authenticator;
import com.inrupt.client.Client;
import com.inrupt.client.Request;
import com.inrupt.client.Response;
import com.inrupt.client.spi.HttpService;
import com.inrupt.client.spi.ServiceProvider;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DefaultClient implements Client {

    private static final int UNAUTHORIZED = 401;
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultClient.class);

    private final HttpService httpClient;
    private final Authenticator.Registry registry;
    private final Client.Session session;
    private final Client.Config config;

    private DefaultClient(final HttpService httpClient) {
        this(httpClient, new DefaultRegistry(), Client.Session.anonymous(), Client.Config.buildDefault());
    }

    private DefaultClient(final HttpService httpClient, final Authenticator.Registry registry,
            final Client.Session session, final Client.Config config) {
        this.httpClient = httpClient;
        this.registry = registry;
        this.session = session;
        this.config = config;
    }

    @Override
    public Client config(Config config) {
        return new DefaultClient(this.httpClient, this.registry, this.session, config);
    }

    @Override
    public Client session(final Session session) {
        return new DefaultClient(this.httpClient, this.registry, session, this.config);
    }

    @Override
    public <T> Response<T> send(final Request request, final Response.BodyHandler<T> responseBodyHandler) {
        return sendAsync(request, responseBodyHandler).toCompletableFuture().join();
    }

    @Override
    public <T> CompletionStage<Response<T>> sendAsync(final Request request,
            final Response.BodyHandler<T> responseBodyHandler) {
        // if there is already an auth header, just pass the request directly through
        if (request.headers().firstValue("Authorization").isPresent()) {
            LOGGER.debug("Sending user-supplied authorization, skipping Solid authorization handling");
            return httpClient.sendAsync(request, responseBodyHandler);
        }

        // Check session cache for a relevant access token
        return session.fromCache(request)
            // Use that token, if present
            .map(token -> httpClient.sendAsync(upgradeRequest(request, token), responseBodyHandler))
            // Otherwise perform the regular HTTP authorization dance
            .orElseGet(() -> httpClient.sendAsync(request, responseBodyHandler)
                .thenCompose(res -> {
                    if (res.statusCode() == UNAUTHORIZED) {
                        final List<Authenticator> authenticators = registry
                            .challenge(res.headers().allValues("WWW-Authenticate"));
                        if (!authenticators.isEmpty()) {
                            // Use the first mechanism
                            final Authenticator authenticator = authenticators.get(0);
                            LOGGER.debug("Using {} authenticator", authenticator.getName());
                            return session.negotiate(authenticator, request)
                                .thenCompose(token ->
                                        httpClient.sendAsync(upgradeRequest(request, token), responseBodyHandler));
                        }
                    }
                    return CompletableFuture.completedFuture(res);
                }));
    }

    Request upgradeRequest(final Request request, final Authenticator.AccessToken token) {
        final Request.Builder builder = Request.newBuilder()
            .uri(request.uri())
            .method(request.method(), request.bodyPublisher().orElseGet(Request.BodyPublishers::noBody));

        LOGGER.debug("Sending upgraded request: {}", request.uri());
        request.timeout().ifPresent(builder::timeout);
        request.headers().asMap().forEach((name, values) -> {
            for (final String value : values) {
                builder.header(name, value);
            }
        });

        // Use setHeader to overwrite any possible existing authorization header
        builder.setHeader("Authorization", String.join(" ", token.getType(), token.getToken()));
        token.getProofAlgorithm().ifPresent(algorithm -> {
            if ("DPoP".equalsIgnoreCase(token.getType())) {
                // TODO - Support DPoP proofs, if relevant
                //builder.setHeader("DPoP",
                        //authenticator.generateProof(algorithm, request.uri(), request.method()));
            }
        });

        return builder.build();
    }

    public static Client.Builder newBuilder() {
        return new Builder();
    }

    public static class Builder implements Client.Builder {

        private HttpService instance;
        private Client.Config config;

        @Override
        public Client.Builder withInstance(final HttpService instance) {
            this.instance = instance;
            return this;
        }

        @Override
        public Client build() {
            if (instance == null) {
                return new DefaultClient(ServiceProvider.getHttpService());
            }
            return new DefaultClient(instance);
        }

        @Override
        public Client.Builder withConfig(final Client.Config config) {
            this.config = config;
            return this;
        }
    }

}
