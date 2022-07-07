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
package com.inrupt.client.webid;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

/**
 * A WebID Profile for use with Solid.
 */
public class WebIdProfile {

    private final URI id;

    private final Set<URI> seeAlso = new HashSet<>();
    private final Set<URI> oidcIssuer = new HashSet<>();
    private final Set<URI> storage = new HashSet<>();
    private final Set<URI> type = new HashSet<>();

    protected WebIdProfile(final URI id) {
        this.id = id;
    }

    /**
     * Retrieve the WebID URI.
     *
     * @return the WebID
     */
    public URI getId() {
        return id;
    }

    /**
     * Retrieve the RDF type values.
     *
     * @return the {@code rdf:type} values
     */
    public Set<URI> getType() {
        return type;
    }

    /**
     * Retrieve the list of OIDC issuers.
     *
     * @return the {@code solid:oidcIssuer} values
     */
    public Set<URI> getOidcIssuer() {
        return oidcIssuer;
    }

    /**
     * Retrieve the list of related profile resources.
     *
     * @return the {@code rdfs:seeAlso} values
     */
    public Set<URI> getSeeAlso() {
        return seeAlso;
    }

    /**
     * Retrieve the list of storage locations.
     *
     * @return the {@code pim:storage} values
     */
    public Set<URI> getStorage() {
        return storage;
    }

    /**
     * Create a new {@link WebIdProfile} builder.
     *
     * @return the builder
     */
    static Builder newBuilder() {
        return new Builder();
    }

    /**
     * A builder class for WebIdProfile objects.
     */
    public static final class Builder {

        private Set<URI> builderSeeAlso = new HashSet<>();
        private Set<URI> builderStorage = new HashSet<>();
        private Set<URI> builderOidcIssuer = new HashSet<>();
        private Set<URI> builderType = new HashSet<>();

        /**
         * Add a seeAlso property.
         *
         * @param uri the seeAlso URI
         * @return this builder
         */
        public Builder seeAlso(final URI uri) {
            builderSeeAlso.add(uri);
            return this;
        }

        /**
         * Add a storage property.
         *
         * @param uri the storage URI
         * @return this builder
         */
        public Builder storage(final URI uri) {
            builderStorage.add(uri);
            return this;
        }

        /**
         * Add an oidcIssuer property.
         *
         * @param uri the oidcIssuer URI
         * @return this builder
         */
        public Builder oidcIssuer(final URI uri) {
            builderOidcIssuer.add(uri);
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
         * Build the WebIdProfile object.
         *
         * @param id the WebID URI
         * @return the WebID profile
         */
        public WebIdProfile build(final URI id) {
            final var profile = new WebIdProfile(id);
            profile.oidcIssuer.addAll(builderOidcIssuer);
            profile.storage.addAll(builderStorage);
            profile.seeAlso.addAll(builderSeeAlso);
            profile.type.addAll(builderType);
            return profile;
        }

        private Builder() {
            // Prevent instantiation
        }
    }
}
