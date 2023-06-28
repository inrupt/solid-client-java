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

import com.inrupt.client.RDFSource;

import java.net.URI;

import org.apache.commons.rdf.api.Dataset;

/**
 * A Solid Resource Object.
 */
public class SolidRDFSource extends RDFSource implements SolidResource {

    private final Metadata metadata;

    /**
     * Create a Solid resource.
     *
     * @param identifier the Solid Resource identifier
     */
    public SolidRDFSource(final URI identifier) {
        this(identifier, null);
    }

    /**
     * Create a Solid resource.
     *
     * @param identifier the Solid Resource identifier
     * @param dataset the resource dataset, may be {@code null}
     */
    public SolidRDFSource(final URI identifier, final Dataset dataset) {
        this(identifier, dataset, null);
    }

    /**
     * Create a Solid resource.
     *
     * @param identifier the Solid Resource identifier
     * @param dataset the resource dataset, may be {@code null}
     * @param metadata metadata associated with this resource, may be {@code null}
     */
    public SolidRDFSource(final URI identifier, final Dataset dataset, final Metadata metadata) {
        super(identifier, dataset);
        if (metadata == null) {
            this.metadata = Metadata.newBuilder().build();
        } else {
            this.metadata = metadata;
        }
    }

    @Override
    public Metadata getMetadata() {
        return metadata;
    }
}
