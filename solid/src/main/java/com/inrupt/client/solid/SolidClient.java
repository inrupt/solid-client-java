/*
 * Copyright Inrupt Inc.
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

import com.inrupt.client.*;
import com.inrupt.client.auth.Session;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import org.apache.commons.rdf.api.Dataset;

/**
 * A high-level client for interacting with Solid resources.
 */
public class SolidClient {

    static final Headers EMPTY_HEADERS = Headers.of(Collections.emptyMap());

    private static final String USER_AGENT = "User-Agent";
    private static final String ACCEPT = "Accept";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String IF_NONE_MATCH = "If-None-Match";
    private static final String TEXT_TURTLE = "text/turtle";
    private static final String WILDCARD = "*";

    private final Client client;
    private final Headers defaultHeaders;
    private final boolean fetchAfterWrite;

    SolidClient(final Client client, final Headers headers, final boolean fetchAfterWrite) {
        this.client = Objects.requireNonNull(client, "Client may not be null!");
        this.defaultHeaders = Objects.requireNonNull(headers, "Headers may not be null!");
        this.fetchAfterWrite = fetchAfterWrite;
    }

    /**
     * Create a session-scoped client.
     *
     * @param session the session
     * @return a session-scoped client
     */
    public SolidClient session(final Session session) {
        Objects.requireNonNull(session, "Session may not be null!");
        return new SolidClient(client.session(session), defaultHeaders, fetchAfterWrite);
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
        return read(identifier, EMPTY_HEADERS, clazz);
    }

    /**
     * Read a Solid Resource into a particular defined type.
     *
     * @param identifier the identifier
     * @param headers headers to add to this request
     * @param clazz the desired resource type
     * @param <T> the resource type
     * @return the next stage of completion, including the new resource
     */
    public <T extends Resource> CompletionStage<T> read(final URI identifier, final Headers headers,
            final Class<T> clazz) {
        final Request.Builder builder = Request.newBuilder(identifier).GET();

        decorateHeaders(builder, defaultHeaders);
        decorateHeaders(builder, headers);

        if (RDFSource.class.isAssignableFrom(clazz)) {
            builder.setHeader(ACCEPT, TEXT_TURTLE);
        }

        defaultHeaders.firstValue(USER_AGENT).ifPresent(agent -> builder.setHeader(USER_AGENT, agent));
        headers.firstValue(USER_AGENT).ifPresent(agent -> builder.setHeader(USER_AGENT, agent));

        final Request request = builder.build();
        return client.send(
                request,
                Response.BodyHandlers.ofByteArray()
            ).thenApply(response -> {
                if (!Response.isSuccess(response.statusCode())) {
                    throw SolidClientException.handle(
                        "Reading resource failed.",
                        response.uri(),
                        response.statusCode(),
                        response.headers(),
                        new String(response.body(), StandardCharsets.UTF_8)
                    );
                }

                final String contentType = response.headers().firstValue(CONTENT_TYPE)
                    .orElse("application/octet-stream");
                try {
                    // Check that this is an RDFSoure
                    if (RDFSource.class.isAssignableFrom(clazz)) {
                        final Dataset dataset = SolidResourceHandlers.buildDataset(contentType, response.body(),
                                request.uri().toString()).orElse(null);
                        final T obj = construct(request.uri(), clazz, dataset, response.headers());
                        final ValidationResult res = RDFSource.class.cast(obj).validate();
                        if (!res.isValid()) {
                            throw new DataMappingException(
                                "Unable to map resource into type: [" + clazz.getSimpleName() + "] ",
                                 res.getResults());
                        }
                        return obj;
                    // Otherwise, create a non-RDF-bearing resource
                    } else {
                        return construct(request.uri(), clazz, contentType,
                                new ByteArrayInputStream(response.body()), response.headers());
                    }
                } catch (final ReflectiveOperationException ex) {
                    throw new SolidResourceException("Unable to read resource into type " + clazz.getName(),
                            ex);
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
    public <T extends Resource> CompletionStage<T> create(final T resource) {
        return create(resource, EMPTY_HEADERS);
    }

    /**
     * Create a new Solid Resource.
     *
     * @param resource the resource
     * @param headers headers to add to this request
     * @param <T> the resource type
     * @return the next stage of completion
     */
    public <T extends Resource> CompletionStage<T> create(final T resource, final Headers headers) {
        final Request.Builder builder = Request.newBuilder(resource.getIdentifier()).PUT(cast(resource));

        decorateHeaders(builder, defaultHeaders);
        decorateHeaders(builder, headers);

        builder.setHeader(CONTENT_TYPE, resource.getContentType()).setHeader(IF_NONE_MATCH, WILDCARD);
        defaultHeaders.firstValue(USER_AGENT).ifPresent(agent -> builder.setHeader(USER_AGENT, agent));
        headers.firstValue(USER_AGENT).ifPresent(agent -> builder.setHeader(USER_AGENT, agent));

        return client.send(builder.build(), Response.BodyHandlers.ofByteArray())
            .thenCompose(handleResponse(resource, headers, "Unable to create resource"));
    }

    /**
     * Update an existing Solid Resource.
     *
     * @param resource the resource
     * @param <T> the resource type
     * @return the next stage of completion
     */
    public <T extends Resource> CompletionStage<T> update(final T resource) {
        return update(resource, EMPTY_HEADERS);
    }

    /**
     * Update an existing Solid Resource.
     *
     * @param resource the resource
     * @param headers headers to add to this request
     * @param <T> the resource type
     * @return the next stage of completion
     */
    public <T extends Resource> CompletionStage<T> update(final T resource, final Headers headers) {
        final Request.Builder builder = Request.newBuilder(resource.getIdentifier()).PUT(cast(resource));

        decorateHeaders(builder, defaultHeaders);
        decorateHeaders(builder, headers);

        builder.setHeader(CONTENT_TYPE, resource.getContentType());
        defaultHeaders.firstValue(USER_AGENT).ifPresent(agent -> builder.setHeader(USER_AGENT, agent));
        headers.firstValue(USER_AGENT).ifPresent(agent -> builder.setHeader(USER_AGENT, agent));

        return client.send(builder.build(), Response.BodyHandlers.ofByteArray())
            .thenCompose(handleResponse(resource, headers, "Unable to update resource"));
    }

    /**
     * Delete an existing Solid Resource.
     *
     * @param resource the resource URI
     * @return the next stage of completion
     */
    public CompletionStage<Void> delete(final URI resource) {
        return delete(new SolidResourceReference(resource, null));
    }

    /**
     * Delete an existing Solid Resource.
     *
     * @param resource the resource URI
     * @param headers headers to add to this request
     * @return the next stage of completion
     */
    public CompletionStage<Void> delete(final URI resource, final Headers headers) {
        return delete(new SolidResourceReference(resource, null), headers);
    }

    /**
     * Delete an existing Solid Resource.
     *
     * @param resource the resource
     * @param <T> the resource type
     * @return the next stage of completion
     */
    public <T extends Resource> CompletionStage<Void> delete(final T resource) {
        return delete(resource, EMPTY_HEADERS);
    }

    /**
     * Delete an existing Solid Resource.
     *
     * @param resource the resource
     * @param headers headers to add to this request
     * @param <T> the resource type
     * @return the next stage of completion
     */
    public <T extends Resource> CompletionStage<Void> delete(final T resource, final Headers headers) {
        final Request.Builder builder = Request.newBuilder(resource.getIdentifier()).DELETE();

        decorateHeaders(builder, defaultHeaders);
        decorateHeaders(builder, headers);

        defaultHeaders.firstValue(USER_AGENT).ifPresent(agent -> builder.setHeader(USER_AGENT, agent));
        headers.firstValue(USER_AGENT).ifPresent(agent -> builder.setHeader(USER_AGENT, agent));


        return client.send(
            builder.build(),
            Response.BodyHandlers.ofByteArray()
        ).thenApply(response -> {
            if (!Response.isSuccess(response.statusCode())) {
                throw SolidClientException.handle(
                    "Deleting resource failed.",
                    response.uri(),
                    response.statusCode(),
                    response.headers(),
                    new String(response.body(), StandardCharsets.UTF_8)
                );
            }
            return null;
        });
    }

    /**
     * Get the {@link SolidClient} for the current application.
     *
     * @return the client instance
     */
    public static SolidClient getClient() {
        return getClientBuilder().build();
    }

    /**
     * Get a {@link SolidClient.Builder} for the current application.
     *
     * @return a client builder
     */
    public static SolidClient.Builder getClientBuilder() {
        return new Builder();
    }

    /**
     * A builder class for a {@link SolidClient}.
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
         * requests performed by the {@link SolidClient#send} method.
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
         * Build the {@link SolidClient}.
         *
         * @return the Solid client
         */
        public SolidClient build() {
            final Client c = builderClient == null ? ClientProvider.getClient() : builderClient;
            final Headers h = builderHeaders == null ? EMPTY_HEADERS : builderHeaders;
            return new SolidClient(c, h, builderFetchAfterWrite);
        }
    }

    <T extends Resource> Function<Response<byte[]>, CompletionStage<T>> handleResponse(final T resource,
            final Headers headers, final String message) {
        return res -> {
            if (!Response.isSuccess(res.statusCode())) {
                throw SolidClientException.handle(
                    message,
                    resource.getIdentifier(),
                    res.statusCode(),
                    res.headers(),
                    new String(res.body(), StandardCharsets.UTF_8)
                );
            }

            if (!fetchAfterWrite) {
                return CompletableFuture.completedFuture(resource);
            }

            @SuppressWarnings("unchecked")
            final Class<T> clazz = (Class<T>) resource.getClass();
            return read(resource.getIdentifier(), headers, clazz);
        };
    }

    static <T extends Resource> T construct(final URI identifier, final Class<T> clazz,
            final Dataset dataset, final Headers headers) throws ReflectiveOperationException {
        // First try an arity-3 ctor with headers
        try {
            return clazz.getConstructor(URI.class, Dataset.class, Headers.class)
                .newInstance(identifier, dataset, headers);
        } catch (final NoSuchMethodException ex) {
            // no-op
        }

        // Next, try an arity-3 ctor with metadata
        // TODO: this construct is deprecated and can be removed in a future version
        try {
            final Metadata metadata = Metadata.of(identifier, headers);
            return clazz.getConstructor(URI.class, Dataset.class, Metadata.class)
                        .newInstance(identifier, dataset, metadata);
        } catch (final NoSuchMethodException ex) {
            // no-op
        }

        // Fall back to an arity-2 ctor
        return clazz.getConstructor(URI.class, Dataset.class)
                    .newInstance(identifier, dataset);
    }

    static <T extends Resource> T construct(final URI identifier, final Class<T> clazz,
            final String contentType, final InputStream entity, final Headers headers)
            throws ReflectiveOperationException {
        // First try an arity-4 ctor with headers
        try {
            return clazz.getConstructor(URI.class, String.class, InputStream.class, Headers.class)
                .newInstance(identifier, contentType, entity, headers);
        } catch (final NoSuchMethodException ex) {
            // no-op
        }

        // Next try an arity-4 ctor with metadata
        // TODO: this construct is deprecated and can be removed in a future version
        try {
            final Metadata metadata = Metadata.of(identifier, headers);
            return clazz.getConstructor(URI.class, String.class, InputStream.class, Metadata.class)
                .newInstance(identifier, contentType, entity, metadata);
        } catch (final NoSuchMethodException ex) {
            // no-op
        }

        // Fall back to an arity-3 ctor
        return clazz.getConstructor(URI.class, String.class, InputStream.class)
            .newInstance(identifier, contentType, entity);
    }

    static void decorateHeaders(final Request.Builder builder, final Headers headers) {
        for (final Map.Entry<String, List<String>> entry : headers.asMap().entrySet()) {
            for (final String item : entry.getValue()) {
                builder.header(entry.getKey(), item);
            }
        }
    }

    static Request.BodyPublisher cast(final Resource resource) {
        try {
            return Request.BodyPublishers.ofInputStream(resource.getEntity());
        } catch (final IOException ex) {
            throw new SolidResourceException("Unable to serialize " + resource.getClass().getName() +
                    " into Solid Resource", ex);
        }
    }
}
