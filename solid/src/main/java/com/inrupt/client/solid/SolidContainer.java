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

import com.inrupt.client.rdf.Dataset;
import com.inrupt.client.rdf.Quad;
import com.inrupt.client.rdf.RDFNode;
import com.inrupt.client.vocabulary.LDP;
import com.inrupt.client.vocabulary.RDF;

import java.net.URI;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A Solid Container Object.
 */
public final class SolidContainer extends SolidResource {

    /**
     * Create a new SolidContainer.
     *
     * @param identifier the container's unique identifier
     * @param dataset the dataset for this container, may be {@code null}
     * @param metadata the container's metadata, may be {@code null}
     */
    public SolidContainer(final URI identifier, final Dataset dataset, final Metadata metadata) {
        super(identifier, dataset, metadata);
    }

    /**
     * Retrieve the resources contained in this SolidContainer.
     *
     * @return the contained resources
     */
    public Stream<SolidResource> getContainedResources() {
        return getDataset().stream(Optional.empty(), RDFNode.namedNode(getIdentifier()),
                RDFNode.namedNode(LDP.contains), null)
            .map(Quad::getObject)
            .filter(RDFNode::isNamedNode)
            .map(RDFNode::getURI)
            .map(child -> {
                final SolidResource.Builder builder = SolidResource.newResourceBuilder();
                final Metadata.Builder metadata = Metadata.newBuilder();
                getMetadata().getStorage().ifPresent(metadata::storage);
                getDataset().stream(Optional.empty(), RDFNode.namedNode(child), RDFNode.namedNode(RDF.type), null)
                    .map(Quad::getObject)
                    .filter(RDFNode::isNamedNode)
                    .map(RDFNode::getURI)
                    .forEach(metadata::type);
                builder.metadata(metadata.build());
                return builder.build(child);
            });
    }

    /**
     * Create a new {@link SolidContainer} builder.
     *
     * @return the builder
     */
    public static Builder newContainerBuilder() {
        return new Builder();
    }

    /**
     * A builder class for SolidContainer objects.
     */
    public static final class Builder{

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
         * @param dataset the RDF dataset
         * @return this builder
         */
        public Builder dataset(final Dataset dataset) {
            builderDataset = dataset;
            return this;
        }

        /**
         * Build the SolidContainer object.
         *
         * @param identifier the Solid container's unique identifier
         * @return the Solid container
         */
        public SolidContainer build(final URI identifier) {
            return new SolidContainer(identifier, builderDataset, builderMetadata);
        }

        Builder() {
            // Prevent instantiations
        }
    }
}
