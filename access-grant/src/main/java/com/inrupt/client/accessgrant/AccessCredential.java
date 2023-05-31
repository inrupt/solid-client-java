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

import static com.inrupt.client.accessgrant.Utils.*;

import java.net.URI;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A base class for access credentials. **/
public class AccessCredential {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessCredential.class);

    protected static final String TYPE = "type";
    protected static final String REVOCATION_LIST_2020_STATUS = "RevocationList2020Status";

    private final String credential;
    private final URI issuer;
    private final URI identifier;
    private final Set<String> types;
    private final Set<String> modes;
    private final Set<URI> purposes;
    private final Set<URI> resources;
    private final URI recipient;
    private final URI creator;
    private final Instant expiration;
    private final Status status;

    /**
     * Create a base class for credential types.
     * @param identifier the credential identifier
     * @param credential the full credential
     * @param data items pertaining to user-defined credential data
     * @param metadata items pertaining to server-managed credential data
     */
    protected AccessCredential(final URI identifier, final String credential,
            final CredentialData data, final CredentialMetadata metadata) {
        this.identifier = Objects.requireNonNull(identifier, "identifier may not be null");
        this.credential = Objects.requireNonNull(credential, "credential may not be null!");
        Objects.requireNonNull(data, "credential data may not be null!");
        Objects.requireNonNull(metadata, "credential metadata may not be null!");

        this.purposes = data.getPurposes();
        this.resources = data.getResources();
        this.modes = data.getModes();
        this.recipient = data.getRecipient();

        this.issuer = metadata.getIssuer();
        this.creator = metadata.getCreator();
        this.types = metadata.getTypes();
        this.status = metadata.getStatus();
        this.expiration = metadata.getExpiration();
    }

    /**
     * Get the types of the access credential.
     *
     * @return the access credential types
     */
    public Set<String> getTypes() {
        return types;
    }

    /**
     * Get the access modes of the access credential.
     *
     * @return the access credential types
     */
    public Set<String> getModes() {
        return modes;
    }

    /**
     * Get the revocation status of the access credential.
     *
     * @return the revocation status, if present
     */
    public Optional<Status> getStatus() {
        return Optional.ofNullable(status);
    }

    /**
     * Get the expiration time of the access credential.
     *
     * @return the expiration time
     */
    public Instant getExpiration() {
        return expiration != null ? expiration : Instant.MAX;
    }

    /**
     * Get the issuer of the access credential.
     *
     * @return the issuer
     */
    public URI getIssuer() {
        return issuer;
    }

    /**
     * Get the identifier of the access credential.
     *
     * @return the identifier
     */
    public URI getIdentifier() {
        return identifier;
    }

    /**
     * Get the collection of purposes associated with the access credential.
     *
     * @implNote as of Beta3, this method returns a set of URIs. Any non-URI purpose values encountered
     *           during access credential parsing will be discarded.
     * @return the purposes
     */
    public Set<URI> getPurposes() {
        return purposes;
    }

    /**
     * Get the resources associated with the access credential.
     *
     * @return the associated resource identifiers
     */
    public Set<URI> getResources() {
        return resources;
    }

    /**
     * Get the creator of this access credential.
     *
     * @return the creator
     */
    public URI getCreator() {
        return creator;
    }

    /**
     * Get the recipient of this access credential.
     *
     * @return the recipient, if present
     */
    public Optional<URI> getRecipient() {
        return Optional.ofNullable(recipient);
    }

    /**
     * Serialize this access credential as a String.
     *
     * @return a serialized form of the credential
     */
    public String serialize() {
        return credential;
    }

    /** Server-managed credential data. */
    public static class CredentialMetadata {
        private final URI issuer;
        private final URI creator;
        private final Set<String> types;
        private final Status status;
        private final Instant expiration;

        /**
         * A collection of server-managed credential metadata.
         *
         * @param issuer the issuer
         * @param creator the agent who created the credential
         * @param types the credential types
         * @param expiration the credential expiration
         * @param status the credential status
         */
        public CredentialMetadata(final URI issuer, final URI creator, final Set<String> types,
                final Instant expiration, final Status status) {
            this.issuer = Objects.requireNonNull(issuer, "issuer may not be null!");
            this.creator = Objects.requireNonNull(creator, "creator may not be null!");
            this.types = Objects.requireNonNull(types, "types may not be null!");
            this.expiration = expiration;
            this.status = status;
        }

        /**
         * Get the issuer of the credential.
         *
         * @return the issuer
         */
        public URI getIssuer() {
            return issuer;
        }

        /**
         * Get the creator of the credential.
         *
         * @return the creator
         */
        public URI getCreator() {
            return creator;
        }

        /**
         * Get the types of the credential.
         *
         * @return the credential types
         */
        public Set<String> getTypes() {
            return types;
        }

        /**
         * Get the expiration time of the credential.
         *
         * @return the expiration time
         */
        public Instant getExpiration() {
            return expiration;
        }

        /**
         * Get the status of the credential.
         *
         * @return the credential status
         */
        public Status getStatus() {
            return status;
        }
    }

    /**  User-managed credential data. */
    public static class CredentialData {
        private final Set<String> modes;
        private final Set<URI> purposes;
        private final Set<URI> resources;
        private final URI recipient;

        /**
         * Create a collection of user-managed credential data.
         *
         * @param resources the resources referenced by the credential
         * @param modes the access modes defined by this credential
         * @param purposes the purposes associated with this credential
         * @param recipient the recipient for this credential, may be {@code null}
         */
        public CredentialData(final Set<URI> resources, final Set<String> modes,
                final Set<URI> purposes, final URI recipient) {
            this.modes = Objects.requireNonNull(modes, "modes may not be null!");
            this.purposes = Objects.requireNonNull(purposes, "purposes may not be null!");
            this.resources = Objects.requireNonNull(resources, "resources may not be null!");
            this.recipient = recipient;
        }

        /**
         * Get the purposes associated with the credential.
         *
         * @return the purpose definitions
         */
        public Set<URI> getPurposes() {
            return purposes;
        }

        /**
         * Get the access modes associated with the credential.
         *
         * @return the access modes
         */
        public Set<String> getModes() {
            return modes;
        }

        /**
         * Get the resource URIs associated with this credential.
         *
         * @return the resource URIs
         */
        public Set<URI> getResources() {
            return resources;
        }

        /**
         * Get the recipient associated with this credential.
         *
         * @return the credential, may be {@code null}
         */
        public URI getRecipient() {
            return recipient;
        }
    }

    static CredentialMetadata extractMetadata(final Map<String, Object> data) {
        final URI issuer = asUri(data.get("issuer")).orElseThrow(() ->
                new IllegalArgumentException("Missing or invalid issuer field"));
        final Set<String> types = asSet(data.get(TYPE)).orElseGet(Collections::emptySet);
        final Instant expiration = asInstant(data.get("expirationDate")).orElse(Instant.MAX);
        final Status status = asMap(data.get("credentialStatus")).flatMap(credentialStatus ->
                asSet(credentialStatus.get(TYPE)).filter(statusTypes ->
                    statusTypes.contains(REVOCATION_LIST_2020_STATUS)).map(x ->
                        asRevocationList2020(credentialStatus))).orElse(null);

        final Map<String, Object> subject = asMap(data.get("credentialSubject")).orElseThrow(() ->
                new IllegalArgumentException("Missing or invalid credentialSubject field"));

        final URI creator = asUri(subject.get("id")).orElseThrow(() ->
                new IllegalArgumentException("Missing or invalid credentialSubject.id field"));

        return new CredentialMetadata(issuer, creator, types, expiration, status);
    }

    static Map<String, Object> extractConsent(final Map<String, Object> data, final String property) {
        final Map<String, Object> subject = asMap(data.get("credentialSubject")).orElseThrow(() ->
                new IllegalArgumentException("Missing or invalid credentialSubject field"));
        return asMap(subject.get(property)).orElseThrow(() ->
                // Unsupported structure
                new IllegalArgumentException("Invalid Access Request: missing consent clause"));
    }

    static Stream<URI> filterUris(final String uri) {
        try {
            return Stream.of(URI.create(uri));
        } catch (final IllegalArgumentException ex) {
            LOGGER.debug("Ignoring non-URI purpose: {}", ex.getMessage());
        }
        return Stream.empty();
    }
}
