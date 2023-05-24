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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;

/**
 * An Access Denial abstraction, for use when interacting with Solid resources.
 */
public class AccessDenial implements AccessCredential {

    private static final String TYPE = "type";
    private static final String REVOCATION_LIST_2020_STATUS = "RevocationList2020Status";
    private static final Set<String> supportedTypes = getSupportedTypes();
    private static final JsonService jsonService = ServiceProvider.getJsonService();

    private final String credential;
    private final URI issuer;
    private final URI identifier;
    private final Set<String> types;
    private final Set<String> purposes;
    private final Set<String> modes;
    private final Set<URI> resources;
    private final URI recipient;
    private final URI creator;
    private final Instant expiration;
    private final Status status;

    /**
     * Read a verifiable presentation as an AccessDenial.
     *
     * @param serialization the Access Denial serialized as a verifiable presentation
     */
    protected AccessDenial(final String serialization) throws IOException {
        try (final InputStream in = new ByteArrayInputStream(serialization.getBytes())) {
            // TODO process as JSON-LD
            final Map<String, Object> data = jsonService.fromJson(in,
                    new HashMap<String, Object>(){}.getClass().getGenericSuperclass());

            final List<Map> vcs = getCredentialsFromPresentation(data, supportedTypes);
            if (vcs.size() != 1) {
                throw new IllegalArgumentException(
                        "Invalid Access Denial: ambiguous number of verifiable credentials");
            }
            final Map vc = vcs.get(0);

            if (asSet(data.get(TYPE)).orElseGet(Collections::emptySet).contains("VerifiablePresentation")) {
                this.credential = serialization;
                this.issuer = asUri(vc.get("issuer")).orElseThrow(() ->
                        new IllegalArgumentException("Missing or invalid issuer field"));
                this.identifier = asUri(vc.get("id")).orElseThrow(() ->
                        new IllegalArgumentException("Missing or invalid id field"));

                this.types = asSet(vc.get(TYPE)).orElseGet(Collections::emptySet);
                this.expiration = asInstant(vc.get("expirationDate")).orElse(Instant.MAX);

                final Map subject = asMap(vc.get("credentialSubject")).orElseThrow(() ->
                        new IllegalArgumentException("Missing or invalid credentialSubject field"));

                this.creator = asUri(subject.get("id")).orElseThrow(() ->
                        new IllegalArgumentException("Missing or invalid credentialSubject.id field"));

                // V1 Access Denial, using gConsent
                final Map consent = asMap(subject.get("providedConsent")).orElseThrow(() ->
                        // Unsupported structure
                        new IllegalArgumentException("Invalid Access Denial: missing consent clause"));

                final Optional<URI> person = asUri(consent.get("isProvidedToPerson"));
                final Optional<URI> controller = asUri(consent.get("isProvidedToController"));
                final Optional<URI> other = asUri(consent.get("isProvidedTo"));

                this.recipient = person.orElseGet(() -> controller.orElseGet(() -> other.orElse(null)));
                this.modes = asSet(consent.get("mode")).orElseGet(Collections::emptySet);
                this.resources = asSet(consent.get("forPersonalData")).orElseGet(Collections::emptySet)
                    .stream().map(URI::create).collect(Collectors.toSet());
                this.purposes = asSet(consent.get("forPurpose")).orElseGet(Collections::emptySet);
                this.status = asMap(vc.get("credentialStatus")).flatMap(credentialStatus ->
                        asSet(credentialStatus.get(TYPE)).filter(statusTypes ->
                            statusTypes.contains(REVOCATION_LIST_2020_STATUS)).map(x ->
                                asRevocationList2020(credentialStatus))).orElse(null);
            } else {
                throw new IllegalArgumentException("Invalid Access Denial: missing VerifiablePresentation type");
            }
        }
    }

    /**
     * Create an AccessDenial object from a serialized form.
     *
     * @param serialization the serialized access denial
     * @return a parsed access denial
     */
    public static AccessDenial of(final String serialization) {
        try {
            return new AccessDenial(serialization);
        } catch (final IOException ex) {
            throw new IllegalArgumentException("Unable to read access denial", ex);
        }
    }

    /**
     * Create an AccessDenial object from a serialized form.
     *
     * @param serialization the serialized access denial
     * @return a parsed access denial
     */
    public static AccessDenial of(final InputStream serialization) {
        try {
            return of(IOUtils.toString(serialization, UTF_8));
        } catch (final IOException ex) {
            throw new IllegalArgumentException("Unable to read access denial", ex);
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
        return creator;
    }

    @Override
    public Optional<URI> getRecipient() {
        return Optional.ofNullable(recipient);
    }

    @Override
    public String serialize() {
        return credential;
    }

    static Set<String> getSupportedTypes() {
        final Set<String> types = new HashSet<>();
        types.add("SolidAccessDenial");
        types.add("http://www.w3.org/ns/solid/vc#SolidAccessDenial");
        return Collections.unmodifiableSet(types);
    }
}
