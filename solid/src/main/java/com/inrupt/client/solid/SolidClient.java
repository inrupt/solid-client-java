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
import com.inrupt.client.ValidationResult;
import com.inrupt.client.auth.Session;
import com.inrupt.client.util.IOUtils;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CompletionStage;

import org.apache.commons.rdf.api.Dataset;
import org.apache.commons.rdf.api.RDFSyntax;

/**
 * A high-level client for interacting with Solid resources.
 */
public class SolidClient {

    private static final String ACCEPT = "Accept";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String IF_NONE_MATCH = "If-None-Match";
    private static final String TEXT_TURTLE = "text/turtle";
    private static final String WILDCARD = "*";

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
    public <T extends Resource> CompletionStage<T> read(final URI identifier, final Class<T> clazz) {
        final Request req = Request.newBuilder(identifier)
            .header(ACCEPT, TEXT_TURTLE)
            .GET().build();

        return client.send(req, Response.BodyHandlers.ofByteArray())
            .thenApply(response -> {
                if (response.statusCode() >= 400) {
                    throw new SolidClientException("Unable to read resource at " + identifier, identifier,
                            response.statusCode(), response.headers(), new String(response.body()));
                } else {
                    final String contentType = response.headers().firstValue("Content-Type")
                        .orElse("application/octet-stream");
                    final Metadata metadata = SolidResourceHandlers.buildMetadata(response.uri(),
                            response.headers());
                    final Dataset dataset = SolidResourceHandlers.buildDataset(contentType, response.body(),
                            identifier.toString()).orElse(null);
                    try {
                        final T obj = construct(identifier, clazz, dataset, metadata);
                        final ValidationResult res = obj.validate();
                        if (!res.isValid()) {
                            throw new DataMappingException(
                                "Unable to map resource into type: [" + clazz.getSimpleName() + "]", res.getResults());
                        }
                        return obj;
                    } catch (final ReflectiveOperationException | DataMappingException ex) {
                        throw new SolidResourceException("Unable to read resource into type " + clazz.getName(), ex);
                    }
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
    public <T extends Resource> CompletionStage<Response<Void>> create(final T resource) {
        final Request req = Request.newBuilder(resource.getIdentifier())
            .header(CONTENT_TYPE, TEXT_TURTLE)
            .header(IF_NONE_MATCH, WILDCARD)
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
    public <T extends Resource> CompletionStage<Response<Void>> update(final T resource) {
        final Request req = Request.newBuilder(resource.getIdentifier())
            .header(CONTENT_TYPE, TEXT_TURTLE)
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
    public <T extends Resource> CompletionStage<Response<Void>> delete(final T resource) {
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

    static <T extends Resource> T construct(final URI identifier, final Class<T> clazz,
            final Dataset dataset, final Metadata metadata) throws ReflectiveOperationException {
        try {
            // First try an arity-3 ctor
            return clazz.getConstructor(URI.class, Dataset.class, Metadata.class)
                        .newInstance(identifier, dataset, metadata);
        } catch (final NoSuchMethodException ex) {
            // Fall back to an arity-2 ctor
            return clazz.getConstructor(URI.class, Dataset.class)
                        .newInstance(identifier, dataset);
        }
    }

    static Request.BodyPublisher cast(final Resource resource) {
        return IOUtils.buffer(out -> {
            try {
                resource.serialize(RDFSyntax.TURTLE, out);
            } catch (final IOException ex) {
                throw new SolidResourceException("Unable to serialize " + resource.getClass().getName() +
                        " into Solid Resource", ex);
            }
        });
    }
}
