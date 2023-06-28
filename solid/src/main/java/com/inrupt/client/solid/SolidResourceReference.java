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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;

/**
 * A reference to a Solid Resource without any corresponding data.
 */
public class SolidResourceReference implements SolidResource {

    private final InputStream entity;
    private final Metadata metadata;
    private final URI identifier;

    /**
     * Create a reference to a Solid resource.
     *
     * @param identifier the resource identifier
     * @param metadata the resource metadata
     */
    public SolidResourceReference(final URI identifier, final Metadata metadata) {
        this.identifier = identifier;
        if (metadata == null) {
            this.metadata = Metadata.newBuilder().build();
        } else {
            this.metadata = metadata;
        }
        this.entity = new ByteArrayInputStream(new byte[0]);
    }

    @Override
    public URI getIdentifier() {
        return identifier;
    }

    @Override
    public Metadata getMetadata() {
        return metadata;
    }

    @Override
    public InputStream getEntity() throws IOException {
        return entity;
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public void close() {
        try {
            entity.close();
        } catch (final IOException ex) {
            throw new UncheckedIOException("Unable to close empty entity", ex);
        }
    }
}
