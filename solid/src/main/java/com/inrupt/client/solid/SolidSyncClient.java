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
package com.inrupt.client.solid;

import com.inrupt.client.Client;
import com.inrupt.client.ClientProvider;
import com.inrupt.client.Headers;
import com.inrupt.client.InruptClientException;
import com.inrupt.client.Request;
import com.inrupt.client.Resource;
import com.inrupt.client.Response;
import com.inrupt.client.auth.Session;

import java.net.URI;
import java.util.Objects;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

/**
 * A high-level synchronous client for interacting with Solid resources.
 */
public class SolidSyncClient {

    private final SolidClient client;

    SolidSyncClient(final Client client, final Headers headers, final boolean fetchAfterWrite) {
        this(new SolidClient(client, headers, fetchAfterWrite));
    }

    SolidSyncClient(final SolidClient client) {
        this.client = client;
    }

    /**
     * Create a session-scoped client.
     *
     * @param session the session
     * @return a session-scoped client
     */
    public SolidSyncClient session(final Session session) {
        Objects.requireNonNull(session, "Session may not be null!");
        return new SolidSyncClient(client.session(session));
    }

    /**
     * Perform a low-level HTTP request.
     *
     * @param request the request
     * @param responseBodyHandler the body handler
     * @param <T> the response handler type
     * @return the response
     */
    public <T> Response<T> send(final Request request,
            final Response.BodyHandler<T> responseBodyHandler) {
        return awaitAsync(client.send(request, responseBodyHandler));
    }

    /**
     * Read a Solid Resource into a particular defined type.
     *
     * @param identifier the identifier
     * @param clazz the desired resource type
     * @param <T> the resource type
     * @return the resource
     */
    public <T extends Resource> T read(final URI identifier, final Class<T> clazz) {
        return awaitAsync(client.read(identifier, clazz));
    }

    /**
     * Create a new Solid Resource.
     *
     * @param resource the resource
     * @param <T> the resource type
     * @return the created resource
     */
    public <T extends Resource> T create(final T resource) {
        return awaitAsync(client.create(resource));
    }

    /**
     * Update an existing Solid Resource.
     *
     * @param resource the resource
     * @param <T> the resource type
     * @return the updated resource
     */
    public <T extends Resource> T update(final T resource) {
        return awaitAsync(client.update(resource));
    }

    /**
     * Delete an existing Solid Resource.
     *
     * @param resource the resource URI
     */
    public void delete(final URI resource) {
        awaitAsync(client.delete(new SolidResourceReference(resource, null)));
    }

    /**
     * Delete an existing Solid Resource.
     *
     * @param resource the resource
     * @param <T> the resource type
     */
    public <T extends Resource> void delete(final T resource) {
        awaitAsync(client.delete(resource));
    }

    /**
     * Get the {@link SolidSyncClient} for the current application.
     *
     * @return the client instance
     */
    public static SolidSyncClient getClient() {
        return getClientBuilder().build();
    }

    public static Builder getClientBuilder() {
        return new Builder();
    }

    /**
     * A builder class for a {@link SolidSyncClient}.
     */
    public static class Builder {
        private Client builderClient;
        private Headers builderHeaders;
        private boolean builderFetchAfterWrite = true;

        Builder() {
        }

        /**
         * Set a pre-configured {@link Client}.
         *
         * @param client the client
         * @return this builder
         */
        public Builder client(final Client client) {
            this.builderClient = client;
            return this;
        }

        /**
         * Set a collection of headers to be used with each high-level client request.
         *
         * <p>Note that any headers set here will not be automatically added to any
         * requests performed by the {@link SolidSyncClient#send} method.
         *
         * @param headers the headers
         * @return this builder
         */
        public Builder headers(final Headers headers) {
            this.builderHeaders = headers;
            return this;
        }

        /**
         * Set whether to fetch a resource after a write operation.
         *
         * @param fetch whether to fetch the remote resource after a write operation
         * @return this builder
         */
        public Builder fetchAfterWrite(final boolean fetch) {
            this.builderFetchAfterWrite = fetch;
            return this;
        }

        /**
         * Build the {@link SolidSyncClient}.
         *
         * @return the Solid client
         */
        public SolidSyncClient build() {
            final Client c = builderClient == null ? ClientProvider.getClient() : builderClient;
            final Headers h = builderHeaders == null ? SolidClient.EMPTY_HEADERS : builderHeaders;
            return new SolidSyncClient(c, h, builderFetchAfterWrite);
        }
    }

    @SuppressWarnings("unchecked")
    static <T, R extends Throwable> T awaitAsync(final CompletionStage<T> future) throws R {
        try {
            return future.toCompletableFuture().join();
        } catch (final CompletionException ex) {
            if (ex.getCause() != null) {
                throw (R) ex.getCause();
            }
            throw new InruptClientException("Error performing SolidClient operation", ex);
        }
    }
}
