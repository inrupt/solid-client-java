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
package com.inrupt.client.solid;

import com.inrupt.client.Client;
import com.inrupt.client.ClientProvider;
import com.inrupt.client.Request;
import com.inrupt.client.Response;
import com.inrupt.client.Session;
import com.inrupt.client.rdf.Dataset;
import com.inrupt.client.rdf.Syntax;
import com.inrupt.client.util.IOUtils;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CompletionStage;

/**
 * A high-level client for interacting with Solid resources.
 */
public class SolidClient {

    private final Client client;

    SolidClient(final Client client) {
        this.client = client;
    }

    /**
     * Create a session-scoped client.
     *
     * @param session the session
     * @return a session-scoped client
     */
    public SolidClient session(final Session session) {
        return new SolidClient(client.session(session));
    }

    /**
     * Perform a low-level HTTP request.
     *
     * @param request the request
     * @param responseBodyHandler the body handler
     * @param <T> the response handler type
     * @return the response
     */
    public <T> CompletionStage<Response<T>> send(final Request request,
            final Response.BodyHandler<T> responseBodyHandler) {
        return client.send(request, responseBodyHandler);
    }

    /**
     * Read a Solid Resource into a particular defined type.
     *
     * @param identifier the identifier
     * @param clazz the desired resource type
     * @param <T> the resource type
     * @return the next stage of completion, including the new resource
     */
    public <T extends SolidResource> CompletionStage<T> read(final URI identifier, final Class<T> clazz) {
        final Request req = Request.newBuilder(identifier)
            .header("Accept", "text/turtle")
            .GET().build();

        return client.send(req, SolidResourceHandlers.ofSolidResource())
            .thenApply(Response::body)
            .thenApply(resource -> {
                try {
                    return clazz.getConstructor(URI.class, Dataset.class, Metadata.class)
                            .newInstance(identifier, resource.getDataset(), resource.getMetadata());
                } catch (final ReflectiveOperationException ex) {
                    throw new SolidResourceException("Unable to read resource into type " + clazz.getName(), ex);
                }
            });
    }

    /**
     * Create a new Solid Resource.
     *
     * @param resource the resource
     * @param <T> the resource type
     * @return the next stage of completion
     */
    public <T extends SolidResource> CompletionStage<Response<Void>> create(final T resource) {
        final Request req = Request.newBuilder(resource.getIdentifier())
            .header("Content-Type", "text/turtle")
            .header("If-None-Match", "*")
            .PUT(cast(resource))
            .build();

        return client.send(req, Response.BodyHandlers.discarding());
    }

    /**
     * Update an existing Solid Resource.
     *
     * @param resource the resource
     * @param <T> the resource type
     * @return the next stage of completion
     */
    public <T extends SolidResource> CompletionStage<Response<Void>> update(final T resource) {
        final Request req = Request.newBuilder(resource.getIdentifier())
            .header("Content-Type", "text/turtle")
            .PUT(cast(resource))
            .build();

        return client.send(req, Response.BodyHandlers.discarding());
    }

    /**
     * Delete an existing Solid Resource.
     *
     * @param resource the resource
     * @param <T> the resource type
     * @return the next stage of completion
     */
    public <T extends SolidResource> CompletionStage<Response<Void>> delete(final T resource) {
        final Request req = Request.newBuilder(resource.getIdentifier())
            .DELETE()
            .build();
        return client.send(req, Response.BodyHandlers.discarding());
    }

    /**
     * Create a new SolidClient from an underlying {@link Client} instance.
     *
     * @param client the client
     * @return the new solid client
     */
    public static SolidClient of(final Client client) {
        return new SolidClient(client);
    }

    /**
     * Get the {@link SolidClient} for the current application.
     *
     * @return the client instance
     */
    public static SolidClient getClient() {
        return SolidClient.of(ClientProvider.getClient());
    }

    static Request.BodyPublisher cast(final SolidResource resource) {
        return IOUtils.buffer(out -> {
            try {
                resource.serialize(Syntax.TURTLE, out);
            } catch (final IOException ex) {
                throw new SolidResourceException("Unable to serialize " + resource.getClass().getName() +
                        " into Solid Resource", ex);
            }
        });
    }
}
