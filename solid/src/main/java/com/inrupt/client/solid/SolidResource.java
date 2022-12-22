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

import com.inrupt.client.Resource;
import com.inrupt.client.rdf.Dataset;

import java.net.URI;

/**
 * A Solid Resource Object.
 */
public class SolidResource extends Resource {

    private final Metadata metadata;

    /**
     * Create a Solid resource.
     *
     * @param identifier the Solid Resource identifier
     * @param dataset the resource dataset, may be {@code null}
     * @param metadata metadata associated with this resource, may be {@code null}
     */
    public SolidResource(final URI identifier, final Dataset dataset, final Metadata metadata) {
        super(identifier, dataset);
        if (metadata == null) {
            this.metadata = Metadata.newBuilder().build();
        } else {
            this.metadata = metadata;
        }
    }

    public Metadata getMetadata() {
        return metadata;
    }

    /**
     * Create a new {@link SolidResource} builder.
     *
     * @return the builder
     */
    public static Builder newResourceBuilder() {
        return new Builder();
    }

    /**
     * A builder class for SolidResource objects.
     */
    public static final class Builder {

        private Metadata builderMetadata;
        private Dataset builderDataset;

        /**
         * Add a metadata property.
         *
         * @param metadata the resource metadata
         * @return this builder
         */
        public Builder metadata(final Metadata metadata) {
            builderMetadata = metadata;
            return this;
        }

        /**
         * Add a dataset property.
         *
         * @param dataset the RDF Dataset
         * @return this builder
         */
        public Builder dataset(final Dataset dataset) {
            builderDataset = dataset;
            return this;
        }

        /**
         * Build the SolidResource object.
         *
         * @param identifier the SolidResource's unique identifier
         * @return the Solid resource
         */
        public SolidResource build(final URI identifier) {
            return new SolidResource(identifier, builderDataset, builderMetadata);
        }

        Builder() {
            // Prevent instantiations
        }
    }
}
