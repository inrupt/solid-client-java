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

import com.inrupt.client.Dataset;
import com.inrupt.client.Quad;
import com.inrupt.client.RDFNode;
import com.inrupt.client.vocabulary.LDP;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * A Solid Container Object.
 */
public final class SolidContainer extends SolidResource {

    private SolidContainer(final URI id, final Dataset dataset, final URI storage) {
        super(id, dataset, storage);
    }

    /**
     * Create a new SolidContainer.
     *
     * @param id the container's unique identifier
     * @return the new {@link SolidContainer} object
     */
    public static SolidContainer of(final URI id) {
        return of(id, null, null);
    }

    /**
     * Create a new SolidContainer.
     *
     * @param id the container's unique identifier
     * @param dataset the dataset for this container, may be {@code null}
     * @param storage the container's storage URI, may be {@code null}
     * @return the new {@link SolidContainer} object
     */
    public static SolidContainer of(final URI id, final Dataset dataset, final URI storage) {
        return new SolidContainer(id, dataset, storage);
    }

    /**
     * Retrieve the resources contained in this SolidContainer.
     *
     * @return the contained resources
     */
    public Stream<SolidResource> getContainedResources() {
        return getDataset().stream(Optional.empty(), RDFNode.namedNode(getId()), RDFNode.namedNode(LDP.contains), null)
            .map(Quad::getObject)
            .filter(RDFNode::isNamedNode)
            .map(RDFNode::getURI)
            .map(child -> SolidResource.of(child, null, getStorage().orElse(null)));
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

        private URI builderStorage;
        private Dataset builderDataset;
        private Set<URI> builderType = new HashSet<>();
        private Map<String, Set<String>> builderWacAllow = new HashMap<>();
        private Set<String> builderAllowedMethods = new HashSet<>();
        private Set<String> builderAllowedPatchSyntaxes = new HashSet<>();
        private Set<String> builderAllowedPostSyntaxes = new HashSet<>();
        private Set<String> builderAllowedPutSyntaxes = new HashSet<>();

        /**
         * Add a storage property.
         *
         * @param uri the storage URI
         * @return this builder
         */
        public Builder storage(final URI uri) {
            builderStorage = uri;
            return this;
        }

        /**
         * Add a type property.
         *
         * @param uri the type URI
         * @return this builder
         */
        public Builder type(final URI uri) {
            builderType.add(uri);
            return this;
        }

        /**
         * Add a wacAllow property.
         *
         * @param accessParam the Access Parameter
         * @return this builder
         */
        public Builder wacAllow(final Map.Entry<String, Set<String>> accessParam) {
            builderWacAllow.put(accessParam.getKey(), accessParam.getValue());
            return this;
        }

        /**
         * Add an allowedMethod property.
         *
         * @param method the method
         * @return this builder
         */
        public Builder allowedMethod(final String method) {
            builderAllowedMethods.add(method);
            return this;
        }

        /**
         * Add a allowedPatchSyntax property.
         *
         * @param syntax the syntax
         * @return this builder
         */
        public Builder allowedPatchSyntax(final String syntax) {
            builderAllowedPatchSyntaxes.add(syntax);
            return this;
        }

        /**
         * Add a allowedPostSyntax property.
         *
         * @param syntax the syntax
         * @return this builder
         */
        public Builder allowedPostSyntax(final String syntax) {
            builderAllowedPostSyntaxes.add(syntax);
            return this;
        }

        /**
         * Add a allowedPutSyntax property.
         *
         * @param syntax the syntax
         * @return this builder
         */
        public Builder allowedPutSyntax(final String syntax) {
            builderAllowedPutSyntaxes.add(syntax);
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
         * @param id the Solid container's unique identifier
         * @return the Solid container
         */
        public SolidContainer build(final URI id) {
            final SolidContainer container = SolidContainer.of(id, builderDataset, builderStorage);
            container.getType().addAll(builderType);
            container.getWacAllow().putAll(builderWacAllow);
            container.getAllowedMethods().addAll(builderAllowedMethods);
            container.getAllowedPatchSyntaxes().addAll(builderAllowedPatchSyntaxes);
            container.getAllowedPostSyntaxes().addAll(builderAllowedPostSyntaxes);
            container.getAllowedPutSyntaxes().addAll(builderAllowedPutSyntaxes);

            return container;
        }

        private Builder() {
            // Prevent instantiations
        }
    }
}
