/*
 * Copyright 2023 Inrupt Inc.
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
package com.inrupt.client.accessgrant;

import java.net.URI;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * An object to represent an access credential query.
 *
 * @param <T> The access credential type
 */
public class AccessCredentialQuery<T extends AccessCredential> {

    private static final URI SOLID_ACCESS_GRANT = URI.create("SolidAccessGrant");
    private static final URI SOLID_ACCESS_REQUEST = URI.create("SolidAccessRequest");
    private static final URI SOLID_ACCESS_DENIAL = URI.create("SolidAccessDenial");

    private final Set<URI> purposes;
    private final Set<String> modes;
    private final URI resource;
    private final URI creator;
    private final URI recipient;
    private final Class<T> clazz;

    /**
     * Create an access credential query.
     *
     * @param resource the resource, may be {@code null}
     * @param creator the creator, may be {@code null}
     * @param recipient the recipient, may be {@code null}
     * @param purposes the purposes, never {@code null}
     * @param modes the access modes, never {@code null}
     * @param clazz the credential type, never {@code null}
     */
    AccessCredentialQuery(final URI resource, final URI creator, final URI recipient,
            final Set<URI> purposes, final Set<String> modes, final Class<T> clazz) {
        this.clazz = Objects.requireNonNull(clazz, "The clazz parameter must not be null!");
        this.resource = resource;
        this.creator = creator;
        this.recipient = recipient;
        this.purposes = purposes;
        this.modes = modes;
    }

    /**
     * Get the requested resource.
     *
     * @return the resource, may be {@code null}
     */
    public URI getResource() {
        return resource;
    }

    /**
     * Get the requested creator.
     *
     * @return the creator, may be {@code null}
     */
    public URI getCreator() {
        return creator;
    }

    /**
     * Get the requested recipient.
     *
     * @return the recipient, may be {@code null}
     */
    public URI getRecipient() {
        return recipient;
    }

    /**
     * Get the requested purposes.
     *
     * @return the purpose identifiers, never {@code null}
     */
    public Set<URI> getPurposes() {
        return purposes;
    }

    /**
     * Get the requested access modes.
     *
     * @return the access modes, never {@code null}
     */
    public Set<String> getModes() {
        return modes;
    }

    /* package private */
    Class<T> getAccessCredentialType() {
        return clazz;
    }

    /**
     * Create a new access credential query builder.
     *
     * @return the builder
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * A builder class for access credential queries.
     */
    public static class Builder {

        private final Set<URI> purposes = new HashSet<>();
        private final Set<String> modes = new HashSet<>();
        private URI builderResource;
        private URI builderCreator;
        private URI builderRecipient;

        /**
         * Set the resource identifier.
         *
         * @param resource the resource identifier, may be {@code null}
         * @return this builder
         */
        public Builder resource(final URI resource) {
            builderResource = resource;
            return this;
        }

        /**
         * Add a purpose identifier.
         *
         * @param purpose a purpose identifier; {@code null} values have no effect.
         * @return this builder
         */
        public Builder purpose(final URI purpose) {
            if (purpose != null) {
                purposes.add(purpose);
            }
            return this;
        }

        /**
         * Add an access mode value.
         *
         * @param mode a mode value; {@code null} values have no effect.
         * @return this builder
         */
        public Builder mode(final String mode) {
            if (mode != null) {
                modes.add(mode);
            }
            return this;
        }

        /**
         * Set the creator identifier.
         *
         * @param creator the creator identifier, may be {@code null}
         * @return this builder
         */
        public Builder creator(final URI creator) {
            builderCreator = creator;
            return this;
        }

        /**
         * Set the recipient identifier.
         *
         * @param recipient the recipient identifier, may be {@code null}
         * @return this builder
         */
        public Builder recipient(final URI recipient) {
            builderRecipient = recipient;
            return this;
        }

        /**
         * Build the access credential query.
         *
         * @param <T> the credential type
         * @param clazz the credential type
         * @return the query object
         */
        public <T extends AccessCredential> AccessCredentialQuery build(final Class<T> clazz) {
            return new AccessCredentialQuery<T>(builderResource, builderCreator, builderRecipient, purposes, modes,
                    clazz);
        }
    }
}
