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

import com.inrupt.client.accessgrant.Status;
import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface Access {
    /**
     * Get the agent who requests access.
     *
     * @return the agent requesting access
     */
    URI getGrantor();

    /**
     * Get the agent to whom access will be granted.
     *
     * @return the agent that will be granted access
     */
    Optional<URI> getGrantee();

    /**
     * Get the purposes of the access.
     *
     * @return the access purposes
     */
    Set<String> getPurposes();
    
    /**
     * Get the access status information.
     *
     * @return the status information
     */
    Optional<Status> getStatus();
    
    /**
     * Get the modes of the access.
     *
     * @return the access modes
     */
    Set<String> getModes();

    /**
     * Get the resources associated with the access.
     *
     * @return the access resources
     */
    Set<URI> getResources();

    /**
     * Get the inheritance of an access.
     *
     * @return the access resources
     */
    Boolean getInherit();

    /**
     * Get the expiration date of the access.
     *
     * @return the access expiration
     */
    Instant getExpiration();

    /**
     * Get the types of the access.
     *
     * @return the access types
     */
    Set<String> getTypes();

    /**
     * Get the issuer of the access.
     *
     * @return the access issuer
     */
    URI getIssuer();

    /**
     * Get the identifier of the access.
     *
     * @return the access identifier
     */
    URI getIdentifier();

    static Builder newBuilder() {
        return new AccessImpl.Builder();
    }

    default VerifiableCredential toVC() {

        VerifiableCredential unsignedVC = new VerifiableCredential();

        unsignedVC.context = Arrays.asList(Utils.VC_CONTEXT_URI, Utils.INRUPT_CONTEXT_URI);
        unsignedVC.issuer = getIssuer().toString();
        unsignedVC.id = getIdentifier().toString();
        unsignedVC.type = getTypes();
        
        unsignedVC.expirationDate = getExpiration();
        
        final Map<String, Object> hasConsent = new HashMap<>();
        
        hasConsent.put("mode", getModes());
        hasConsent.put("forPersonalData", getResources());
        hasConsent.put("hasStatus", "https://w3id.org/GConsent#ConsentStatusRequested");
        hasConsent.put("forPurpose", getPurposes());
        hasConsent.put("inherit", getInherit().toString());
        hasConsent.put("isConsentForDataSubject", getGrantor().toString());
        
        final Map<String, Object> credentialSubject = new HashMap<>();
        credentialSubject.put("hasConsent", hasConsent);
        
        unsignedVC.credentialSubject = credentialSubject;

        return unsignedVC;
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

        public Access build() {
            return new AccessImpl(grantor, grantee, issuer, types, identifier, purposes, modes, resources, status, expiration, inherit);
        }

        private Builder() {
        }
    }
}

class AccessImpl implements Access {

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

    AccessImpl(final URI grantor, final URI grantee, final URI issuer, final Set<String> types, final URI identifier, final Set<String> purposes, final Set<String> modes,
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
