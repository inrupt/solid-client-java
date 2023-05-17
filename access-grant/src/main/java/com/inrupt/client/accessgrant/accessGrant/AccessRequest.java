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
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public interface AccessRequest {

    /**
     * Get the agent who granted access.
     *
     * @return the agent granting access
     */
    URI getGrantor();

    /**
     * Get the agent to whom access will granted.
     *
     * @return the agent that will be granted access
     */
    Optional<URI> getGrantee();

    /**
     * Get the purposes of the access request.
     *
     * @return the access request purposes
     */
    Set<String> getPurposes();
    
    /**
     * Get the inheritance of an access request.
     *
     * @return the access resources
     */
    Boolean getInherit();
    
    /**
     * Get the access request status information.
     *
     * @return the status information
     */
    Optional<Status> getStatus();

    /**
     * Get the modes of the access request.
     *
     * @return the access request modes
     */
    Set<String> getModes();

    /**
     * Get the resources associated with the access request.
     *
     * @return the access request resources
     */
    Set<URI> getResources();

    /**
     * Get the expiration date of the access request.
     *
     * @return the access request expiration
     */
    Instant getExpiration();

    /**
     * Get the types of the access request.
     *
     * @return the access request types
     */
    Set<String> getTypes();

    /**
     * Get the issuer of the access request.
     *
     * @return the access request issuer
     */
    URI getIssuer();

    /**
     * Get the identifier of the access request.
     *
     * @return the access request identifier
     */
    URI getIdentifier();

    static Builder newBuilder() {
        return new AccessRequestImpl.Builder();
    }

    default VerifiableCredential toVC() {
        VerifiableCredential vc = new VerifiableCredential();

        vc.context = Arrays.asList(Utils.VC_CONTEXT_URI, Utils.INRUPT_CONTEXT_URI);
        vc.issuer = getIssuer().toString();
        vc.id = getIdentifier().toString();
        vc.type = getTypes();
        
        vc.expirationDate = getExpiration();
        
        final Map<String, Object> hasConsent = new HashMap<>();
        
        hasConsent.put("mode", getModes());
        hasConsent.put("forPersonalData", getResources());
        hasConsent.put("hasStatus", "https://w3id.org/GConsent#ConsentStatusRequested");
        hasConsent.put("forPurpose", getPurposes());
        hasConsent.put("inherit", getInherit().toString());
        hasConsent.put("isConsentForDataSubject", getGrantor().toString());
        
        final Map<String, Object> credentialSubject = new HashMap<>();
        credentialSubject.put("hasConsent", hasConsent);
        
        vc.credentialSubject = credentialSubject;

        return vc;
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

        public Builder grantee(final URI value) {
            grantee = value;
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

        public Builder inherit(final Boolean value) {
            inherit = value;
            return this;
        }

        public Builder expiration(final Instant value) {
            expiration = value;
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

        public AccessRequest build() {
            return new AccessRequestImpl(grantor, grantee, issuer, types, identifier, purposes, modes, resources, status, expiration, inherit);
        }

        private Builder() {
        }
    }

    static AccessRequest fromVC(VerifiableCredential vc) {

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

        final Map consent = Utils.asMap(subject.get("hasConsent")).orElseThrow(() ->
                new IllegalArgumentException("Missing consent clause"));

        final Boolean inherit =  Utils.asBoolean(consent.get("inherit")).orElseThrow(() ->
            new IllegalArgumentException("Missing consent clause"));

        final Optional<URI> grantee = Utils.asUri(consent.get("isConsentForDataSubject"));

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
            .grantee(grantee.isPresent()? grantee.get() : null)
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

    /* default Map<String, Object> toMap() {
        final Map<String, Object> credentialSubject = new HashMap<>();

        final Map<String, Object> hasConsent = new HashMap<>();

        hasConsent.put("mode", getModes());
        hasConsent.put("forPersonalData", getResources());
        hasConsent.put("hasStatus", "https://w3id.org/GConsent#ConsentStatusRequested");
        hasConsent.put("forPurpose", getPurposes());
        hasConsent.put("inherit", getInherit());
        hasConsent.put("isConsentForDataSubject", getGrantor().toString());

        credentialSubject.put("hasConsent", hasConsent);

        final Map<String, Object> credential = new HashMap<>();
        credential.put("context", Arrays.asList(Utils.VC_CONTEXT_URI, Utils.INRUPT_CONTEXT_URI));
        
        credential.put("expirationDate", getExpiration().truncatedTo(ChronoUnit.SECONDS).toString());

        credential.put("credentialSubject", credentialSubject);

        final Map<String, Object> data = new HashMap<>();
        data.put("credential", credential);

        return data;
    } */

    /* static AccessRequest fromMap(VerifiableCredential verifiableCredential) {
        final Map consent = Utils.asMap(verifiableCredential.get("hasConsent")).orElseThrow(() ->
            new IllegalArgumentException("Invalid access request: missing consent clause"));

        final Set<String> modes = Utils.asSet(consent.get("mode")).orElseThrow(() ->
            new IllegalArgumentException("Invalid access request: missing mode clause"));

        final Set<String> purposes = Utils.asSet(consent.get("forPurpose")).orElseThrow(() ->
            new IllegalArgumentException("Invalid access request: missing forPurpose clause"));

        final URI grantor = Utils.asUri(consent.get("isConsentForDataSubject")).orElseThrow(() ->
            new IllegalArgumentException("Invalid access request: missing isConsentForDataSubject clause"));

        final Set<String> resources = Utils.asSet(consent.get("forPersonalData")).orElseThrow(() ->
            new IllegalArgumentException("Invalid access request: missing forPersonalData clause"));

        final Boolean inherit = Utils.asBoolean(consent.get("inherit")).orElseThrow(() ->
            new IllegalArgumentException("Invalid access request: missing inherit clause"));

        final Instant expiration = Utils.asInstant(verifiableCredential.get("expirationDate")).orElseThrow(() ->
        new IllegalArgumentException("Invalid access request: missing expirationDate clause"));

        final Status status = Utils.asMap(consent.get("credentialStatus"))
            .flatMap(credentialStatus -> Utils.asSet(credentialStatus.get(Utils.TYPE))
                .filter(statusTypes -> statusTypes.contains(Utils.REVOCATION_LIST_2020_STATUS))
                .map(x -> Utils.asRevocationList2020(credentialStatus)))
            .orElse(null);

        return newBuilder()
            .grantor(grantor)
            .purposes(purposes)
            .modes(modes.stream().map(URI::create).collect(Collectors.toSet()))
            .resources(resources.stream().map(URI::create).collect(Collectors.toSet()))
            .status(status)
            .expiration(expiration)
            .inherit(inherit)
            .build();
        } */
}

class AccessRequestImpl implements AccessRequest {

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

    AccessRequestImpl(final URI grantor, final URI grantee, final URI issuer, final Set<String> types, final URI identifier, final Set<String> purposes, final Set<String> modes,
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
