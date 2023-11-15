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
package com.inrupt.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.Objects;

/**
 * A base class for non-RDF-bearing resources.
 *
 * <p>This class can be used as a basis for object mapping with the high-level client operations.
 */
public class NonRDFSource implements Resource {

    private final URI identifier;
    private final String contentType;
    private final InputStream entity;
    private final Headers headers;

    /**
     * Create a new non-RDF-bearing resource.
     *
     * <p>Subclasses should have the same constructor signature to work with the provided object mapping mechanism.
     *
     * @param identifier the resource identifier
     * @param contentType the content type of the resource
     * @param entity the resource entity
     */
    protected NonRDFSource(final URI identifier, final String contentType, final InputStream entity) {
        this(identifier, contentType, entity, null);
    }

    /**
     * Create a new non-RDF-bearing resource.
     *
     * <p>Subclasses should have the same constructor signature to work with the provided object mapping mechanism.
     *
     * @param identifier the resource identifier
     * @param contentType the content type of the resource
     * @param entity the resource entity
     * @param headers header values associated with the resource, may be {@code null}
     */
    protected NonRDFSource(final URI identifier, final String contentType, final InputStream entity,
            final Headers headers) {
        this.identifier = Objects.requireNonNull(identifier, "identifier may not be null!");
        this.contentType = Objects.requireNonNull(contentType, "contentType may not be null!");
        this.entity = Objects.requireNonNull(entity, "entity may not be null!");
        this.headers = headers == null ? Headers.empty() : headers;
    }

    @Override
    public URI getIdentifier() {
        return identifier;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public Headers getHeaders() {
        return headers;
    }

    @Override
    public InputStream getEntity() throws IOException {
        return entity;
    }

    @Override
    public void close() {
        try {
            entity.close();
        } catch (final IOException ex) {
            throw new UncheckedIOException("Unable to close NonRDFSource entity", ex);
        }
    }
}
