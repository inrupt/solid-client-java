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

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.Objects;

public class SolidEntity implements AutoCloseable {

    private final URI identifier;
    private final String contentType;
    private final InputStream entity;
    private final Metadata metadata;

    public static SolidEntity entity(final URI identifier, final String contentType, final InputStream entity) {
        return new SolidEntity(identifier, contentType, entity, null);
    }

    SolidEntity(final URI identifier, final String contentType, final InputStream entity,
            final Metadata metadata) {
        this.identifier = Objects.requireNonNull(identifier, "identifier may not be null!");
        this.contentType = Objects.requireNonNull(contentType, "contentType may not be null!");
        this.entity = Objects.requireNonNull(entity, "entity may not be null!");

        if (metadata == null) {
            this.metadata = Metadata.newBuilder().build();
        } else {
            this.metadata = metadata;
        }
    }

    public URI getIdentifier() {
        return identifier;
    }

    public String getContentType() {
        return contentType;
    }

    public InputStream getEntity() {
        return entity;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    @Override
    public void close() {
        try {
            entity.close();
        } catch (final IOException ex) {
            throw new UncheckedIOException("Error closing input stream", ex);
        }
    }
}
