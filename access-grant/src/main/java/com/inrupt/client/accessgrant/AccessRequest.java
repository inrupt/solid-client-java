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
import static java.nio.charset.StandardCharsets.UTF_8;

import com.inrupt.client.spi.JsonService;
import com.inrupt.client.spi.ServiceProvider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;

/**
 * An Access Request abstraction, for use with interacting with Solid resources.
 */
public class AccessRequest implements AccessCredential {

    private static final String TYPE = "type";
    private static final String REVOCATION_LIST_2020_STATUS = "RevocationList2020Status";
    private static final JsonService jsonService = ServiceProvider.getJsonService();
    private static final Set<String> supportedTypes = getSupportedTypes();

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
    private final Status status;

    /**
     * Read a verifiable presentation as an AccessRequest.
     *
     * @param grant the serialized form of an Access Request
     */
    protected AccessRequest(final String grant) throws IOException {
        try (final InputStream in = new ByteArrayInputStream(grant.getBytes())) {
            // TODO process as JSON-LD
            final Map<String, Object> data = jsonService.fromJson(in,
                    new HashMap<String, Object>(){}.getClass().getGenericSuperclass());

            final Map vc = getCredentialFromPresentation(data, supportedTypes).orElseThrow(() ->
                    new IllegalArgumentException("Invalid Access Grant: missing verifiable credential"));

            if (asSet(data.get(TYPE)).orElseGet(Collections::emptySet).contains("VerifiablePresentation")) {
                this.rawGrant = grant;
                this.issuer = asUri(vc.get("issuer")).orElseThrow(() ->
                        new IllegalArgumentException("Missing or invalid issuer field"));
                this.identifier = asUri(vc.get("id")).orElseThrow(() ->
                        new IllegalArgumentException("Missing or invalid id field"));

                this.types = asSet(vc.get(TYPE)).orElseGet(Collections::emptySet);
                this.expiration = asInstant(vc.get("expirationDate")).orElse(Instant.MAX);

                final Map subject = asMap(vc.get("credentialSubject")).orElseThrow(() ->
                        new IllegalArgumentException("Missing or invalid credentialSubject field"));

                this.grantor = asUri(subject.get("id")).orElseThrow(() ->
                        new IllegalArgumentException("Missing or invalid credentialSubject.id field"));

                // V1 Access Request, using gConsent
                final Map consent = asMap(subject.get("hasConsent")).orElseThrow(() ->
                        // Unsupported structure
                        new IllegalArgumentException("Invalid Access Request: missing consent clause"));

                final Optional<URI> dataSubject = asUri(consent.get("isConsentForDataSubject"));
                this.grantee = dataSubject.orElse(null);
                this.modes = asSet(consent.get("mode")).orElseGet(Collections::emptySet);
                this.resources = asSet(consent.get("forPersonalData")).orElseGet(Collections::emptySet)
                    .stream().map(URI::create).collect(Collectors.toSet());
                this.purposes = asSet(consent.get("forPurpose")).orElseGet(Collections::emptySet);
                this.status = asMap(vc.get("credentialStatus")).flatMap(credentialStatus ->
                        asSet(credentialStatus.get(TYPE)).filter(statusTypes ->
                            statusTypes.contains(REVOCATION_LIST_2020_STATUS)).map(x ->
                                asRevocationList2020(credentialStatus))).orElse(null);
            } else {
                throw new IllegalArgumentException("Invalid Access Request: missing VerifiablePresentation type");
            }
        }
    }


    /**
     * Create an AccessRequest object from a serialized form.
     *
     * @param serialization the serialized access grant
     * @return a parsed access grant
     */
    public static AccessRequest of(final String serialization) {
        try {
            return new AccessRequest(serialization);
        } catch (final IOException ex) {
            throw new IllegalArgumentException("Unable to read access grant", ex);
        }
    }

    /**
     * Create an AccessRequest object from a serialized form.
     *
     * @param serialization the access request
     * @return a parsed access grant
     */
    public static AccessRequest of(final InputStream serialization) {
        try {
            return of(IOUtils.toString(serialization, UTF_8));
        } catch (final IOException ex) {
            throw new IllegalArgumentException("Unable to read access grant", ex);
        }
    }

    @Override
    public Set<String> getTypes() {
        return types;
    }

    @Override
    public Set<String> getModes() {
        return modes;
    }

    @Override
    public Optional<Status> getStatus() {
        return Optional.ofNullable(status);
    }

    @Override
    public Instant getExpiration() {
        return expiration;
    }

    @Override
    public URI getIssuer() {
        return issuer;
    }

    @Override
    public URI getIdentifier() {
        return identifier;
    }

    @Override
    public Set<String> getPurposes() {
        return purposes;
    }

    @Override
    public Set<URI> getResources() {
        return resources;
    }

    @Override
    public URI getCreator() {
        return grantor;
    }

    @Override
    public Optional<URI> getRecipient() {
        return Optional.ofNullable(grantee);
    }

    @Override
    public String serialize() {
        return rawGrant;
    }

    static Set<String> getSupportedTypes() {
        final Set<String> types = new HashSet<>();
        types.add("SolidAccessRequest");
        types.add("http://www.w3.org/ns/solid/vc#SolidAccessRequest");
        return Collections.unmodifiableSet(types);
    }
}
