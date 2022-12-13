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

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * A Solid Resource Object.
 */
public class SolidResource {

    private final URI id;
    private final Optional<URI> storage;
    private final Dataset dataset;
    private final Set<URI> type = new HashSet<>();
    private final Map<String, Set<String>> wacAllow = new HashMap<>();
    private final Set<String> allowedMethods = new HashSet<>();
    private final Set<String> allowedPatchSyntaxes = new HashSet<>();
    private final Set<String> allowedPostSyntaxes = new HashSet<>();
    private final Set<String> allowedPutSyntaxes = new HashSet<>();

    /**
     * Create a Solid resource.
     *
     * @param id the Solid Resource identifier
     * @param storage the storage to which this resource belongs, may be {@code null}
     */
    protected SolidResource(final URI id, final Dataset dataset, final URI storage) {
        this.id = id;
        if (dataset != null) {
            this.dataset = dataset;
        } else {
            this.dataset = new EmptyDataset();
        }
        this.storage = Optional.ofNullable(storage);
    }

    /**
     * Create a new SolidResource.
     *
     * @param id the SolidResource's unique identifier
     * @return the new {@link SolidResource} object
     */
    public static SolidResource of(final URI id) {
        return of(id, null, null);
    }

    /**
     * Create a new SolidResource.
     *
     * @param id the SolidResource's unique identifier
     * @param dataset the SolidResource's dataset, may be {@code null}
     * @param storage the SolidResource's storage URI, may be {@code null}
     * @return the new {@link SolidResource} object
     */
    public static SolidResource of(final URI id, final Dataset dataset, final URI storage) {
        return new SolidResource(id, dataset, storage);
    }

    /**
     * Retrieve SolidResource's unique identifier.
     *
     * @return the id
     */
    public URI getId() {
        return id;
    }

    /**
     * Retrieve storage URI from this SolidResource.
     *
     * @return the storage URIs
     */
    public Optional<URI> getStorage() {
        return storage;
    }

    /**
     * Retrieve type associated with this SolidResource.
     *
     * @return the type
     */
    public Set<URI> getType() {
        return type;
    }

    /**
     * Retrieve WAC-Allow access parameters associated with this SolidResource.
     *
     * @return the access parameters
     */
    public Map<String, Set<String>> getWacAllow() {
        return wacAllow;
    }

    /**
     * Retrieve set of allowed methods associated with this SolidResource.
     *
     * @return the allowed methods
     */
    public Set<String> getAllowedMethods() {
        return allowedMethods;
    }

    /**
     * Retrieve set of allowed patch syntaxes associated with this SolidResource.
     *
     * @return the allowed patch syntaxes
     */
    public Set<String> getAllowedPatchSyntaxes() {
        return allowedPatchSyntaxes;
    }

    /**
     * Retrieve set of allowed post syntaxes associated with this SolidResource.
     *
     * @return the allowed post syntaxes
     */
    public Set<String> getAllowedPostSyntaxes() {
        return allowedPostSyntaxes;
    }

    /**
     * Retrieve set of allowed put syntaxes associated with this SolidResource.
     *
     * @return the allowed put syntaxes
     */
    public Set<String> getAllowedPutSyntaxes() {
        return allowedPutSyntaxes;
    }

    /**
     * Retrieve list of RDF Quads associated with this SolidResource.
     *
     * @return the RDF statements
     */
    public Dataset getDataset() {
        return dataset;
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
         * @param id the SolidResource's unique identifier
         * @return the Solid resource
         */
        public SolidResource build(final URI id) {
            final SolidResource resource = SolidResource.of(id, builderDataset, builderStorage);
            resource.wacAllow.putAll(builderWacAllow);
            resource.type.addAll(builderType);
            resource.allowedMethods.addAll(builderAllowedMethods);
            resource.allowedPatchSyntaxes.addAll(builderAllowedPatchSyntaxes);
            resource.allowedPostSyntaxes.addAll(builderAllowedPostSyntaxes);
            resource.allowedPutSyntaxes.addAll(builderAllowedPutSyntaxes);
            return resource;
        }

        private Builder() {
            // Prevent instantiations
        }
    }

    class EmptyDataset implements Dataset {
        @Override
        public Stream<Quad> stream(final Optional<RDFNode> graph, final RDFNode subject, final RDFNode predicate,
                final RDFNode object) {
            return Stream.empty();
        }

        @Override
        public Stream<Quad> stream() {
            return Stream.empty();
        }
    }
}
