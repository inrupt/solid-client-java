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

import com.inrupt.client.NonRDFSource;

import java.io.InputStream;
import java.net.URI;

/**
 * A non-RDF-bearing Solid Resource.
 */
public class SolidNonRDFSource extends NonRDFSource implements SolidResource {

    private final Metadata metadata;

    /**
     * Create a non-RDF-bearing Solid Resource.
     *
     * @param identifier the resource identifier
     * @param contentType the content type
     * @param entity the entity
     * @param metadata the metadata, may be {@code null}
     */
    public SolidNonRDFSource(final URI identifier, final String contentType, final InputStream entity,
            final Metadata metadata) {
        super(identifier, contentType, entity);
        if (metadata == null) {
            this.metadata = Metadata.newBuilder().build();
        } else {
            this.metadata = metadata;
        }
    }

    /**
     * Get the metadata for this resource.
     *
     * @return the metadata
     */
    public Metadata getMetadata() {
        return metadata;
    }
}
