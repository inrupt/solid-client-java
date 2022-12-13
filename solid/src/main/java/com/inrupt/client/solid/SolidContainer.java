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

import com.inrupt.client.rdf.Quad;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

/**
 * A Solid Container Object.
 */
public final class SolidContainer extends SolidResource {

    private final List<SolidResource> containedResources = new ArrayList<>();

    private SolidContainer(final URI id, final Optional<URI> storage) {
        super(id, storage);
    }

    /**
     * Create a new SolidContainer.
     *
     * @param id the container's unique identifier
     * @return the new {@link SolidContainer} object
     */
    public static SolidContainer of(final URI id) {
        return of(id, null);
    }

    /**
     * Create a new SolidContainer.
     *
     * @param id the container's unique identifier
     * @param storage the container's storage URI
     * @return the new {@link SolidContainer} object
     */
    public static SolidContainer of(final URI id, final Optional<URI> storage) {
        return new SolidContainer(id, storage);
    }

    /**
     * Retrieve the resources contained in this SolidContainer.
     *
     * @return the contained resources
     */
    public List<SolidResource> getContainedResources() {
        return containedResources;
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

        private Optional<URI> builderStorage = Optional.empty();
        private Set<URI> builderType = new HashSet<>();
        private Map<String, Set<String>> builderWacAllow = new HashMap<>();
        private Set<String> builderAllowedMethods = new HashSet<>();
        private Set<String> builderAllowedPatchSyntaxes = new HashSet<>();
        private Set<String> builderAllowedPostSyntaxes = new HashSet<>();
        private Set<String> builderAllowedPutSyntaxes = new HashSet<>();
        private List<Quad> builderStatements = new ArrayList<>();
        private List<SolidResource> builderContainedResources = new ArrayList<>();

        /**
         * Add a storage property.
         *
         * @param uri the storage URI
         * @return this builder
         */
        public Builder storage(final URI uri) {
            builderStorage = Optional.of(uri);
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
        public Builder wacAllow(final Entry<String, Set<String>> accessParam) {
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
         * Add a statement property.
         *
         * @param quad the RDF quad
         * @return this builder
         */
        public Builder statements(final Quad quad) {
            builderStatements.add(quad);
            return this;
        }

        /**
         * Add a resource to container.
         *
         * @param resource the solid resource
         * @return this builder
         */
        public Builder containedResource(final SolidResource resource) {
            builderContainedResources.add(resource);
            return this;
        }

        /**
         * Build the SolidContainer object.
         *
         * @param id the Solid container's unique identifier
         * @return the Solid container
         */
        public SolidContainer build(final URI id) {
            final var container = SolidContainer.of(id, builderStorage);
            container.containedResources.addAll(builderContainedResources);
            container.type.addAll(builderType);
            container.wacAllow.putAll(builderWacAllow);
            container.statements.addAll(builderStatements);
            container.allowedMethods.addAll(builderAllowedMethods);
            container.allowedPatchSyntaxes.addAll(builderAllowedPatchSyntaxes);
            container.allowedPostSyntaxes.addAll(builderAllowedPostSyntaxes);
            container.allowedPutSyntaxes.addAll(builderAllowedPutSyntaxes);

            return container;
        }

        private Builder() {
            // Prevent instantiations
        }
    }
}
