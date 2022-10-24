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
package com.inrupt.client.http;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.inrupt.client.Request;
import com.inrupt.client.Response;
import com.inrupt.client.authentication.AccessToken;
import com.inrupt.client.authentication.SolidAuthenticator;
import com.inrupt.client.spi.HttpService;
import com.inrupt.client.spi.ServiceProvider;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An HTTP client for interacting with Solid Resources.
 *
 * <p>This client extends the native Java client by introducing a {@link SolidAuthenticator} class
 * for performing Solid-conforming authentication.
 */
public class SolidClient {

    private static final int UNAUTHORIZED = 401;
    private static final Logger LOGGER = LoggerFactory.getLogger(SolidClient.class);

    private final SolidAuthenticator solidAuthenticator;
    private final HttpService client;
    private final Cache<URI, AccessToken> tokenCache;

    /**
     * Create a new SolidClient.
     *
     * @param authenticator the Solid authenticator
     */
    public SolidClient(final SolidAuthenticator authenticator) {
        this.client = ServiceProvider.getHttpService();
        this.solidAuthenticator = Objects.requireNonNull(authenticator);
        this.tokenCache = Caffeine.newBuilder()
            .expireAfter(new AccessTokenExpiry())
            // TODO -- make this value configurable
            .maximumSize(10000)
            .build();
    }

    public <T> Response<T> send(final Request request, final Response.BodyHandler<T> responseBodyHandler)
            throws IOException, InterruptedException {
        return sendAsync(request, responseBodyHandler).toCompletableFuture().join();
    }

    public <T> CompletionStage<Response<T>> sendAsync(final Request request,
            final Response.BodyHandler<T> responseBodyHandler) {
        // if there is already an auth header, just pass the request directly through
        if (request.headers().firstValue("Authorization").isPresent()) {
            LOGGER.debug("Sending user-supplied authorization, skipping Solid authorization handling");
            return client.sendAsync(request, responseBodyHandler);
        }

        // Check the internal token cache, using that if available
        final var cachedToken = tokenCache.getIfPresent(request.uri());
        if (cachedToken != null) {
            LOGGER.debug("Using cached access token for request URI: {}", request.uri());
            return client.sendAsync(upgradeRequest(request, cachedToken), responseBodyHandler);
        }

        // First, send a downgraded request (no body)
        return client.sendAsync(downgradeRequest(request), responseBodyHandler)
            .thenCompose(res -> {
                if (res.statusCode() == UNAUTHORIZED) {
                    final var mechanisms = solidAuthenticator.challenge(res.headers().allValues("WWW-Authenticate"));
                    if (!mechanisms.isEmpty()) {
                        // Use the first mechanism
                        final var authenticator = mechanisms.get(0);
                        LOGGER.debug("Using authenticator with {} scheme", authenticator.getScheme());
                        final var token = authenticator.authenticate();
                        tokenCache.put(request.uri(), token);
                        return client.sendAsync(upgradeRequest(request, token), responseBodyHandler);
                    }
                }
                return CompletableFuture.completedFuture(res);
            });
    }

    Request downgradeRequest(final Request request) {
        final var builder = Request.newBuilder()
            .uri(request.uri())
            .method(request.method(), Request.BodyPublishers.noBody());

        LOGGER.debug("Sending downgraded request: {}", request.uri());
        request.timeout().ifPresent(builder::timeout);
        request.headers().asMap().forEach((name, values) -> {
            for (var value : values) {
                builder.header(name, value);
            }
        });

        return builder.build();
    }

    Request upgradeRequest(final Request request, final AccessToken token) {
        final var builder = Request.newBuilder()
            .uri(request.uri())
            .method(request.method(), request.bodyPublisher().orElseGet(Request.BodyPublishers::noBody));

        LOGGER.debug("Sending upgraded request: {}", request.uri());
        request.timeout().ifPresent(builder::timeout);
        request.headers().asMap().forEach((name, values) -> {
            for (var value : values) {
                builder.header(name, value);
            }
        });

        // Use setHeader to overwrite any possible existing authorization header
        builder.setHeader("Authorization", String.join(" ", token.getType(), token.getToken()));
        token.getProofAlgorithm().ifPresent(algorithm -> {
            if ("DPoP".equalsIgnoreCase(token.getType())) {
                builder.setHeader("DPoP",
                        solidAuthenticator.generateProof(algorithm, request.uri(), request.method()));
            }
        });

        return builder.build();
    }

    static class AccessTokenExpiry implements Expiry<URI, AccessToken> {
        @Override
        public long expireAfterCreate(final URI key, final AccessToken value, final long currentTime) {
            final var expiration = value.getExpiration().minusMillis(Instant.now().toEpochMilli()).getEpochSecond();
            return TimeUnit.SECONDS.toNanos(expiration);
        }

        @Override
        public long expireAfterRead(final URI key, final AccessToken value, final long currentTime,
                final long currentDuration) {
            return currentDuration;
        }

        @Override
        public long expireAfterUpdate(final URI key, final AccessToken value, final long currentTime,
                final long currentDuration) {
            return currentDuration;
        }
    }
}
