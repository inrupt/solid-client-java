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
package com.inrupt.client.accessgrant;

import static com.inrupt.client.accessgrant.Utils.*;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.inrupt.client.spi.JsonService;
import com.inrupt.client.spi.ServiceProvider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;

/**
 * An Access Request abstraction, for use when interacting with Solid resources.
 */
public class AccessRequest extends AccessCredential {

    private static final JsonService jsonService = ServiceProvider.getJsonService();
    private static final Set<String> supportedTypes = getSupportedTypes();

    /**
     * Read a verifiable presentation as an AccessRequest.
     *
     * @param identifier the credential identifier
     * @param credential the serialized form of an Access Request
     * @param data the user-managed data associated with the credential
     * @param metadata the server-managed data associated with the credential
     */
    protected AccessRequest(final URI identifier, final String credential, final CredentialData data,
            final CredentialMetadata metadata) {
        super(identifier, credential, data, metadata);
    }

    /**
     * Create an AccessRequest object from a serialized form.
     *
     * @param serialization the serialized access request
     * @return a parsed access request
     */
    public static AccessRequest of(final String serialization) {
        try {
            return parse(serialization);
        } catch (final IOException ex) {
            throw new IllegalArgumentException("Unable to read access request", ex);
        }
    }

    /**
     * Create an AccessRequest object from a serialized form.
     *
     * @param serialization the access request
     * @return a parsed access request
     */
    public static AccessRequest of(final InputStream serialization) {
        try {
            return of(IOUtils.toString(serialization, UTF_8));
        } catch (final IOException ex) {
            throw new IllegalArgumentException("Unable to read access request", ex);
        }
    }

    static Set<String> getSupportedTypes() {
        final Set<String> types = new HashSet<>();
        types.add("SolidAccessRequest");
        types.add("http://www.w3.org/ns/solid/vc#SolidAccessRequest");
        return Collections.unmodifiableSet(types);
    }

    static AccessRequest parse(final String serialization) throws IOException {
        try (final InputStream in = new ByteArrayInputStream(serialization.getBytes())) {
            // TODO process as JSON-LD
            final Map<String, Object> data = jsonService.fromJson(in,
                    new HashMap<String, Object>(){}.getClass().getGenericSuperclass());

            final List<Map<String, Object>> vcs = getCredentialsFromPresentation(data, supportedTypes);
            if (vcs.size() != 1) {
                throw new IllegalArgumentException(
                        "Invalid Access Request: ambiguous number of verifiable credentials");
            }
            final Map<String, Object> vc = vcs.get(0);

            if (asSet(data.get(TYPE)).orElseGet(Collections::emptySet).contains("VerifiablePresentation")) {
                final URI identifier = asUri(vc.get("id")).orElseThrow(() ->
                        new IllegalArgumentException("Missing or invalid id field"));

                // Extract metadata
                final CredentialMetadata credentialMetadata = extractMetadata(vc);

                // V1 Access Request, using gConsent
                final Map<String, Object> consent = extractConsent(vc, "hasConsent");

                final URI recipient = asUri(consent.get("isConsentForDataSubject")).orElse(null);
                final Set<String> modes = asSet(consent.get("mode")).orElseGet(Collections::emptySet);
                final Set<URI> resources = asSet(consent.get("forPersonalData")).orElseGet(Collections::emptySet)
                    .stream().map(URI::create).collect(Collectors.toSet());
                final Set<URI> purposes = asSet(consent.get("forPurpose")).orElseGet(Collections::emptySet)
                    .stream().flatMap(AccessCredential::filterUris).collect(Collectors.toSet());
                final CredentialData credentialData = new CredentialData(resources, modes, purposes, recipient);

                return new AccessRequest(identifier, serialization, credentialData, credentialMetadata);
            } else {
                throw new IllegalArgumentException("Invalid Access Request: missing VerifiablePresentation type");
            }
        }
    }

    /**
     * A collection of parameters used for creating access requests.
     *
     * <p>See, in particular, the {@link AccessGrantClient#requestAccess(RequestParameters)} method.
     */
    public static class RequestParameters {

        private final URI recipient;
        private final Set<URI> resources;
        private final Set<String> modes;
        private final Set<URI> purposes;
        private final Instant expiration;
        private final Instant issuedAt;

        /* package private */
        RequestParameters(final URI recipient, final Set<URI> resources,
                final Set<String> modes, final Set<URI> purposes, final Instant expiration, final Instant issuedAt) {
            this.recipient = recipient;
            this.resources = resources;
            this.modes = modes;
            this.purposes = purposes;
            this.expiration = expiration;
            this.issuedAt = issuedAt;
        }

        /**
         * Get the recipient used with an access request operation.
         *
         * <p>Note: the recipient will typically be the resource owner
         *
         * @return the recipient's identifier
         */
        public URI getRecipient() {
            return recipient;
        }

        /**
         * Get the resources used with an access request operation.
         *
         * @return the resource idnetifiers
         */
        public Set<URI> getResources() {
            return resources;
        }

        /**
         * Get the access modes used with an access request operation.
         *
         * @return the access modes
         */
        public Set<String> getModes() {
            return modes;
        }

        /**
         * Get the purpose identifiers used with an access request operation.
         *
         * @return the purpose identifiers
         */
        public Set<URI> getPurposes() {
            return purposes;
        }

        /**
         * Get the requested expiration date used with an access request operation.
         *
         * <p>Note: an access grant server may select a different expiration date
         *
         * @return the requested expiration date
         */
        public Instant getExpiration() {
            return expiration;
        }

        /**
         * Get the requested issuance date used with an access request operation.
         *
         * <p>Note: an access grant server may select a different issuance date
         *
         * @return the requested issuance date
         */
        public Instant getIssuedAt() {
            return issuedAt;
        }

        /**
         * A class for building access request parameters.
         */
        public static class Builder {

            private final Set<URI> builderResources = new HashSet<>();
            private final Set<String> builderModes = new HashSet<>();
            private final Set<URI> builderPurposes = new HashSet<>();
            private URI builderRecipient;
            private Instant builderExpiration;
            private Instant builderIssuedAt;

            /* package-private */
            Builder() {
                // Prevent external instantiation
            }

            /**
             * Set a recipient for the access request operation.
             *
             * <p>Note: this will typically be the identifier of resource owner
             *
             * @param recipient the recipient identifier, may be {@code null}
             * @return this builder
             */
            public Builder recipient(final URI recipient) {
                builderRecipient = recipient;
                return this;
            }

            /**
             * Set a single resource for the access request operation.
             *
             * @param resource the resource identifier, not {@code null}
             * @return this builder
             */
            public Builder resource(final URI resource) {
                builderResources.add(resource);
                return this;
            }

            /**
             * Set multiple resources for the access request operation.
             *
             * <p>Note: A null value will clear all existing resource values
             *
             * @param resources the resource identifiers, may be {@code null}
             * @return this builder
             */
            public Builder resources(final Collection<URI> resources) {
                if (resources != null) {
                    builderResources.addAll(resources);
                } else {
                    builderResources.clear();
                }
                return this;
            }

            /**
             * Set a single access mode for the access request operation.
             *
             * @param mode the access mode, not {@code null}
             * @return this builder
             */
            public Builder mode(final String mode) {
                builderModes.add(mode);
                return this;
            }

            /**
             * Set multiple access modes for the access request operation.
             *
             * <p>Note: A null value will clear all existing mode values
             *
             * @param modes the access modes, may be {@code null}
             * @return this builder
             */
            public Builder modes(final Collection<String> modes) {
                if (modes != null) {
                    builderModes.addAll(modes);
                } else {
                    builderModes.clear();
                }
                return this;
            }

            /**
             * Set a single purpose for the access request operation.
             *
             * @param purpose the purpose identifier, not {@code null}
             * @return this builder
             */
            public Builder purpose(final URI purpose) {
                builderPurposes.add(purpose);
                return this;
            }

            /**
             * Set multiple purposes for the access request operation.
             *
             * <p>Note: A null value will clear all existing purpose values
             *
             * @param purposes the purpose identifiers, may be {@code null}
             * @return this builder
             */
            public Builder purposes(final Collection<URI> purposes) {
                if (purposes != null) {
                    builderPurposes.addAll(purposes);
                } else {
                    builderPurposes.clear();
                }
                return this;
            }

            /**
             * Set a preferred expiration time for the access request operation.
             *
             * <p>Note: an access grant server may select a different expiration value
             *
             * @param expiration the expiration time, may be {@code null}.
             * @return this builder
             */
            public Builder expiration(final Instant expiration) {
                builderExpiration = expiration;
                return this;
            }

            /**
             * Set a preferred issuance time for the access request operation, likely at a time in the future.
             *
             * <p>Note: an access grant server may select a different issuance value
             *
             * @param issuedAt the issuance time, may be {@code null}.
             * @return this builder
             */
            public Builder issuedAt(final Instant issuedAt) {
                builderIssuedAt = issuedAt;
                return this;
            }

            /**
             * Build the {@link RequestParameters} object.
             *
             * @return the access request parameters
             */
            public RequestParameters build() {
                return new RequestParameters(builderRecipient, builderResources, builderModes, builderPurposes,
                        builderExpiration, builderIssuedAt);
            }
        }
    }
}
