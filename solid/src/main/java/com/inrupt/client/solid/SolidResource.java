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

import com.inrupt.client.Quad;

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
 * A Solid Resource Object.
 */
public class SolidResource {

    protected final URI id;

    protected final Optional<URI> storage;
    protected final Set<URI> type = new HashSet<>();
    protected final Map<String, Set<String>> wacAllow = new HashMap<>();
    protected final Set<String> allowedMethods = new HashSet<>();
    protected final List<Quad> statements = new ArrayList<>();
    protected final Set<String> allowedPatchSyntaxes = new HashSet<>();
    protected final Set<String> allowedPostSyntaxes = new HashSet<>();
    protected final Set<String> allowedPutSyntaxes = new HashSet<>();

    protected SolidResource(final URI id, final Optional<URI> storage) {
        this.id = id;
        this.storage = storage;
    }

    /**
     * Create a new SolidResource.
     *
     * @param id the SolidResource's unique identifier
     * @return the new {@link SolidResource} object
     */
    public static SolidResource of(final URI id) {
        return of(id, null);
    }

    /**
     * Create a new SolidResource.
     *
     * @param id the SolidResource's unique identifier
     * @param storage the SolidResource's storage URI
     * @return the new {@link SolidResource} object
     */
    public static SolidResource of(final URI id, final Optional<URI> storage) {
        return new SolidResource(id, storage);
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
    public List<Quad> getStatements() {
        return statements;
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

        private Optional<URI> builderStorage = Optional.empty();
        private Set<URI> builderType = new HashSet<>();
        private Map<String, Set<String>> builderWacAllow = new HashMap<>();
        private Set<String> builderAllowedMethods = new HashSet<>();
        private Set<String> builderAllowedPatchSyntaxes = new HashSet<>();
        private Set<String> builderAllowedPostSyntaxes = new HashSet<>();
        private Set<String> builderAllowedPutSyntaxes = new HashSet<>();
        private List<Quad> builderStatements = new ArrayList<>();

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
        public Builder statement(final Quad quad) {
            builderStatements.add(quad);
            return this;
        }

        /**
         * Build the SolidResource object.
         *
         * @param id the SolidResource's unique identifier
         * @return the Solid resource
         */
        public SolidResource build(final URI id) {
            final var resource = SolidResource.of(id, builderStorage);
            resource.statements.addAll(builderStatements);
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
}
