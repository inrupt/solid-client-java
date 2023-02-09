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
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;

/**
 * An Access Grant abstraction, for use with interacting with Solid resources.
 */
public class AccessGrant {

    private static final JsonService jsonService = ServiceProvider.getJsonService();

    private final String rawGrant;
    private final URI issuer;
    private final URI identifier;
    private final Set<String> types;
    private final Set<String> purposes;
    private final Set<String> modes;
    private final Set<URI> resources;
    private final URI grantee;
    private final URI grantor;
    private final Instant expiration;

    /**
     * Read a verifiable presentation as an AccessGrant.
     *
     * @param grant the Access Grant serialized as a verifiable presentation
     */
    protected AccessGrant(final String grant) throws IOException {
        try (final InputStream in = new ByteArrayInputStream(grant.getBytes())) {
            // TODO process as JSON-LD
            final Map<String, Object> data = jsonService.fromJson(in,
                    new HashMap<String, Object>(){}.getClass().getGenericSuperclass());

            final Map vc = getCredentialFromPresentation(data).orElseThrow(() ->
                    new IllegalArgumentException("Invalid Access Grant: missing verifiable credential"));

            if (asSet(data.get("type")).orElseGet(Collections::emptySet).contains("VerifiablePresentation")) {
                this.rawGrant = grant;
                this.issuer = asUri(vc.get("issuer")).orElseThrow(() ->
                        new IllegalArgumentException("Missing or invalid issuer field"));
                this.identifier = asUri(vc.get("id")).orElseThrow(() ->
                        new IllegalArgumentException("Missing or invalid id field"));

                this.types = asSet(vc.get("type")).orElseGet(Collections::emptySet);
                this.expiration = asInstant(vc.get("expirationDate")).orElse(Instant.MAX);

                final Map subject = asMap(vc.get("credentialSubject")).orElseThrow(() ->
                        new IllegalArgumentException("Missing or invalid credentialSubject field"));

                this.grantor = asUri(subject.get("id")).orElseThrow(() ->
                        new IllegalArgumentException("Missing or invalid credentialSubject.id field"));

                // V1 Access Grant, using gConsent
                final Map<String, Object> consent = asMap(subject.get("providedConsent")).orElseThrow(() ->
                            new IllegalArgumentException("Invalid Access Grant: missing providedConsent clause"));
                final Optional<URI> person = asUri(consent.get("isProvidedToPerson"));
                final Optional<URI> controller = asUri(consent.get("isProvidedToController"));
                final Optional<URI> other = asUri(consent.get("isProvidedTo"));
                this.grantee = person.orElseGet(() -> controller.orElseGet(() -> other.orElseThrow(() ->
                                new IllegalArgumentException("Missing or invalid grantee value"))));
                this.modes = asSet(consent.get("mode")).orElseGet(Collections::emptySet);
                this.resources = asSet(consent.get("forPersonalData")).orElseGet(Collections::emptySet)
                    .stream().map(URI::create).collect(Collectors.toSet());
                this.purposes = asSet(consent.get("forPurpose")).orElseGet(Collections::emptySet);
            } else {
                throw new IllegalArgumentException("Invalid Access Grant: missing VerifiablePresentation type");
            }
        }
    }

    /**
     * Create an AccessGrant object from a VerifiablePresentation.
     *
     * @param accessGrant the access grant
     * @return a parsed access grant
     */
    public static AccessGrant ofAccessGrant(final String accessGrant) {
        try {
            return new AccessGrant(accessGrant);
        } catch (final IOException ex) {
            throw new IllegalArgumentException("Unable to read access grant", ex);
        }
    }

    /**
     * Create an AccessGrant object from a VerifiablePresentation.
     *
     * @param accessGrant the access grant
     * @return a parsed access grant
     */
    public static AccessGrant ofAccessGrant(final InputStream accessGrant) {
        try {
            return ofAccessGrant(IOUtils.toString(accessGrant, UTF_8));
        } catch (final IOException ex) {
            throw new IllegalArgumentException("Unable to read access grant", ex);
        }
    }

    /**
     * Get the types of the access grant.
     *
     * @return the access grant types
     */
    public Set<String> getTypes() {
        return types;
    }

    /**
     * Get the modes of the access grant.
     *
     * @return the access grant modes
     */
    public Set<String> getModes() {
        return modes;
    }

    /**
     * Get the expiration date of the access grant.
     *
     * @return the access grant expiration
     */
    public Instant getExpiration() {
        return expiration;
    }

    /**
     * Get the issuer of the access grant.
     *
     * @return the access grant issuer
     */
    public URI getIssuer() {
        return issuer;
    }

    /**
     * Get the identifier of the access grant.
     *
     * @return the access grant identifier
     */
    public URI getIdentifier() {
        return identifier;
    }

    /**
     * Get the purposes of the access grant.
     *
     * @return the access grant purposes
     */
    public Set<String> getPurpose() {
        return purposes;
    }

    /**
     * Get the resources associated with the access grant.
     *
     * @return the access grant resources
     */
    public Set<URI> getResources() {
        return resources;
    }

    /**
     * Get the agent to whom access is granted.
     *
     * @return the agent that was granted access
     */
    public URI getGrantee() {
        return grantee;
    }

    /**
     * Get the agent who granted access.
     *
     * @return the agent granting access
     */
    public URI getGrantor() {
        return grantor;
    }

    /**
     * Get the raw access grant.
     *
     * @return the access grant
     */
    public String getRawGrant() {
        return rawGrant;
    }

    static Optional<Instant> asInstant(final Object value) {
        if (value instanceof String) {
            return Optional.of(Instant.parse((String) value));
        }
        return Optional.empty();
    }

    static Optional<URI> asUri(final Object value) {
        if (value instanceof String) {
            return Optional.of(URI.create((String) value));
        }
        return Optional.empty();
    }

    static Optional<Map> asMap(final Object value) {
        if (value instanceof Map) {
            return Optional.of((Map) value);
        }
        return Optional.empty();
    }

    static Optional<Set<String>> asSet(final Object value) {
        if (value != null) {
            final Set<String> data = new HashSet<>();
            if (value instanceof String) {
                data.add((String) value);
            } else if (value instanceof Collection) {
                for (final Object item : (Collection) value) {
                    if (item instanceof String) {
                        data.add((String) item);
                    }
                }
            }
            return Optional.of(data);
        }
        return Optional.empty();
    }

    static Optional<Map> getCredentialFromPresentation(final Map<String, Object> data) {
        if (data.get("verifiableCredential") instanceof Collection) {
            for (final Object item : (Collection) data.get("verifiableCredential")) {
                if (item instanceof Map) {
                    final Map vc = (Map) item;
                    if (asSet(vc.get("type")).filter(types -> types.contains("SolidAccessGrant") ||
                                types.contains("http://www.w3.org/ns/solid/vc#SolidAccessGrant")).isPresent()) {
                        return Optional.of(vc);
                    }
                }
            }
        }
        return Optional.empty();
    }

}
