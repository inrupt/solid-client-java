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
package com.inrupt.client.accessgrant.accessGrant;

import java.net.URI;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public interface AccessGrant {

    /**
     * Get the agent who granted access.
     *
     * @return the agent granting access
     */
    URI getGrantor();

    /**
     * Get the agent to whom access is granted.
     *
     * @return the agent that was granted access
     */
    Optional<URI> getGrantee();

    /**
     * Get the purposes of the access grant.
     *
     * @return the access grant purposes
     */
    Set<String> getPurposes();
    
    /**
     * Get the inheritance of an access grant.
     *
     * @return the access resources
     */
    Boolean getInherit();
    
    /**
     * Get the access grant status information.
     *
     * @return the status information
     */
    Optional<Status> getStatus();

    /**
     * Get the modes of the access grant.
     *
     * @return the access grant modes
     */
    Set<String> getModes();

    /**
     * Get the resources associated with the access grant.
     *
     * @return the access grant resources
     */
    Set<URI> getResources();

    /**
     * Get the expiration date of the access grant.
     *
     * @return the access grant expiration
     */
    Instant getExpiration();

    /**
     * Get the types of the access grant.
     *
     * @return the access grant types
     */
    Set<String> getTypes();

    /**
     * Get the issuer of the access grant.
     *
     * @return the access grant issuer
     */
    URI getIssuer();

    /**
     * Get the identifier of the access grant.
     *
     * @return the access grant identifier
     */
    URI getIdentifier();    

    static Builder newBuilder() {
        return new AccessGrantImpl.Builder();
    }

    class Builder {
        private URI issuer;
        private URI identifier;
        private Set<String> types;
        private Set<String> purposes;
        private Set<String> modes;
        private Set<URI> resources;
        private URI grantee;
        private URI grantor;
        private Instant expiration;
        private Status status;
        private Boolean inherit;

        public Builder grantor(final URI value) {
            grantor = value;
            return this;
        }

        public Builder purposes(final Set<String> value) {
            purposes = value;
            return this;
        }

        public Builder modes(final Set<String> value) {
            modes = value;
            return this;
        }

        public Builder resources(final Set<URI> value) {
            resources = value;
            return this;
        }

        public Builder status(final Status value) {
            status = value;
            return this;
        }

        public Builder expiration(final Instant value) {
            expiration = value;
            return this;
        }

        public Builder inherit(final Boolean value) {
            inherit = value;
            return this;
        }

        public Builder issuer(final URI value) {
            issuer = value;
            return this;
        }

        public Builder types(final Set<String> value) {
            types = value;
            return this;
        }

        public Builder identifier(final URI value) {
            identifier = value;
            return this;
        }

        public Builder grantee(final URI value) {
            grantee = value;
            return this;
        }

        public AccessGrant build() {
            return new AccessGrantImpl(grantor, grantee, issuer, types, identifier, purposes, modes, resources, status, expiration, inherit);
        }

        private Builder() {
        }
    }

    static AccessGrant fromVC(VerifiableCredential vc) {
        final URI issuer = Utils.asUri(vc.issuer).orElseThrow(() ->
            new IllegalArgumentException("Missing or invalid issuer field"));
        final URI identifier = Utils.asUri(vc.id).orElseThrow(() ->
                new IllegalArgumentException("Missing or invalid id field"));

        final Set<String> types = Utils.asSet(vc.type).orElseGet(Collections::emptySet);
        final Instant expiration = Utils.asInstant(vc.expirationDate).orElse(Instant.MAX);

        final Map subject = Utils.asMap(vc.credentialSubject).orElseThrow(() ->
                new IllegalArgumentException("Missing or invalid credentialSubject field"));

        final URI grantor = Utils. asUri(subject.get("id")).orElseThrow(() ->
                new IllegalArgumentException("Missing or invalid credentialSubject.id field"));

        final Map consent = Utils.asMap(subject.get("providedConsent")).orElseThrow(() ->
                new IllegalArgumentException("Invalid Access Grant: missing consent clause"));

        final Boolean inherit =  Utils.asBoolean(consent.get("inherit")).orElseThrow(() ->
            new IllegalArgumentException("Invalid Access Grant: missing consent clause"));

        final Optional<URI> person = Utils.asUri(consent.get("isProvidedToPerson"));
        final Optional<URI> controller = Utils.asUri(consent.get("isProvidedToController"));
        final Optional<URI> other = Utils.asUri(consent.get("isProvidedTo"));

        final URI grantee = person.orElseGet(() -> controller.orElseGet(() -> other.orElse(null)));

        final Set<String> modes = Utils.asSet(consent.get("mode")).orElseGet(Collections::emptySet);
        final Set<URI> resources = Utils.asSet(consent.get("forPersonalData")).orElseGet(Collections::emptySet)
            .stream().map(URI::create).collect(Collectors.toSet());
        final Set<String> purposes = Utils.asSet(consent.get("forPurpose")).orElseGet(Collections::emptySet);
        
        final Status status = Utils.asMap(vc.credentialStatus).flatMap(credentialStatus ->
            Utils.asSet(credentialStatus.get("type")).filter(statusTypes ->
                statusTypes.contains(Utils.REVOCATION_LIST_2020_STATUS)).map(x ->
                    Utils.asRevocationList2020(credentialStatus))).orElse(null);
        return newBuilder()
            .grantor(grantor)
            .grantee(grantee)
            .types(types)
            .identifier(identifier)
            .issuer(issuer)
            .purposes(purposes)
            .modes(modes)
            .resources(resources)
            .status(status)
            .expiration(expiration)
            .inherit(inherit)
            .build();
    }
}

class AccessGrantImpl implements AccessGrant {

    private final URI issuer;
    private final URI identifier;
    private final Set<String> types;
    private final Set<String> purposes;
    private final Set<String> modes;
    private final Set<URI> resources;
    private final URI grantee;
    private final URI grantor;
    private final Instant expiration;
    private final Status status;
    private final Boolean inherit;

    AccessGrantImpl(final URI grantor, final URI grantee, final URI issuer, final Set<String> types, final URI identifier, final Set<String> purposes, final Set<String> modes,
    final Set<URI> resources, final Status status, final Instant expiration, final Boolean inherit) {
        this.grantor = grantor;
        this.grantee = grantee;
        this.issuer = issuer;
        this.types = types;
        this.identifier = identifier;
        this.purposes = purposes;
        this.modes = modes;
        this.resources = resources;
        this.status = status;
        this.expiration = expiration;
        this.inherit = inherit;
    }

    @Override
    public URI getGrantor() {
        return grantor;
    }

    @Override
    public Set<String> getPurposes() {
        return purposes;
    }

    @Override
    public Boolean getInherit() {
        return inherit;
    }

    @Override
    public Optional<Status> getStatus() {
        return Optional.ofNullable(status);
    }

    @Override
    public Set<String> getModes() {
        return modes;
    }

    @Override
    public Set<URI> getResources() {
        return resources;
    }

    @Override
    public Instant getExpiration() {
        return expiration;
    }

    @Override
    public Optional<URI> getGrantee() {
        return Optional.ofNullable(grantee);
    }

    @Override
    public URI getIdentifier() {
        return identifier;
    }

    @Override
    public Set<String> getTypes() {
        return types;
    }

    @Override
    public URI getIssuer() {
        return issuer;
    }
}
