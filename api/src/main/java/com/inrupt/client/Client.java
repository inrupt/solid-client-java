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
package com.inrupt.client;

import com.inrupt.client.spi.HttpService;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public interface Client {

    /**
     * Perform an HTTP request.
     *
     * @param request the request
     * @param responseBodyHandler the response body handler
     * @param <T> the response handler type
     * @return the response
     */
    <T> Response<T> send(Request request, Response.BodyHandler<T> responseBodyHandler);

    /**
     * Perform an HTTP request.
     *
     * @param request the request
     * @param responseBodyHandler the response body handler
     * @param <T> the response handler type
     * @return the next stage of completion, containing the response
     */
    <T> CompletionStage<Response<T>> sendAsync(Request request, Response.BodyHandler<T> responseBodyHandler);

    /**
     * Create a session-scoped client.
     *
     * @param session the session manager
     * @return the session-scoped client
     */
    Client session(Session session);

    /**
     * A session abstraction for managing access tokens for an agent.
     */
    interface Session {

        /**
         * Retrieve the identifier associated with this session.
         *
         * @return a session identifier
         */
        String getId();

        /**
         * Retrieve an access token for a request from a cache.
         *
         * @param request the HTTP request
         * @return the access token or {@code null}
         */
        Optional<Authenticator.AccessToken> fromCache(Request request);

        /**
         * Negotiate for an access token.
         *
         * @param authenticator the authenticator
         * @param request the HTTP request
         * @return the next stage of completion, containing the access token or {@code null}
         */
        CompletionStage<Authenticator.AccessToken> negotiate(Authenticator authenticator, Request request);

        /**
         * Create a new anonymous session.
         *
         * @implNote This {@link Session} does not keep a cache of access tokens.
         * @return the session
         */
        static Session anonymous() {
            final String sessionId = UUID.randomUUID().toString();
            return new Session() {
                @Override
                public String getId() {
                    return sessionId;
                }

                @Override
                public Optional<Authenticator.AccessToken> fromCache(final Request request) {
                    return Optional.empty();
                }

                @Override
                public CompletionStage<Authenticator.AccessToken> negotiate(final Authenticator authenticator,
                        final Request request) {
                    return authenticator.authenticateAsync();
                }
            };
        }
    }

    interface Builder {

        /**
         * Add a specific {@link HttpService} instance to the builder.
         *
         * @param instance the http service
         * @return this builder
         */
        Builder withInstance(HttpService instance);

        /**
         * Build the client.
         *
         * @return the client
         */
        Client build();
    }
}
