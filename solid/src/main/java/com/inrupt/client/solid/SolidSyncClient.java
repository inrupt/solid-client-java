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
import com.inrupt.client.Request;
import com.inrupt.client.Resource;
import com.inrupt.client.Response;
import com.inrupt.client.Session;

import java.net.URI;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

/**
 * A high-level synchronous client for interacting with Solid resources.
 */
public class SolidSyncClient {

    private final SolidClient client;

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
     */
    public <T extends Resource> void create(final T resource) {
        awaitAsync(client.create(resource));
    }

    /**
     * Update an existing Solid Resource.
     *
     * @param resource the resource
     * @param <T> the resource type
     * @return the response
     */
    public <T extends Resource> Response<Void> update(final T resource) {
        return awaitAsync(client.update(resource));
    }

    /**
     * Delete an existing Solid Resource.
     *
     * @param resource the resource
     * @param <T> the resource type
     * @return the next stage of completion
     */
    public <T extends Resource> Response<Void> delete(final T resource) {
        return awaitAsync(client.delete(resource));
    }

    /**
     * Create a new SolidClient from an underlying {@link Client} instance.
     *
     * @param client the client
     * @return the new solid client
     */
    public static SolidSyncClient of(final Client client) {
        return new SolidSyncClient(new SolidClient(client));
    }

    /**
     * Get the {@link SolidSyncClient} for the current application.
     *
     * @return the client instance
     */
    public static SolidSyncClient getClient() {
        return SolidSyncClient.of(ClientProvider.getClient());
    }

    @SuppressWarnings("unchecked")
    static <T, R extends Throwable> T awaitAsync(final CompletionStage<T> future) throws R {
        try {
            return future.toCompletableFuture().join();
        } catch (final CompletionException ex) {
            throw (R) ex.getCause();
        }
    }
}
