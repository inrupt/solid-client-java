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

    protected AccessGrant(final String grant) {
        this.rawGrant = grant;
        try (final InputStream in = new ByteArrayInputStream(grant.getBytes())) {
            final Map<String, Object> data = jsonService.fromJson(in,
                    new HashMap<String, Object>(){}.getClass().getGenericSuperclass());
            this.issuer = asUri(data.get("issuer")).orElseThrow(() ->
                    new IllegalArgumentException("Missing or invalid issuer field"));
            this.identifier = asUri(data.get("id")).orElseThrow(() ->
                    new IllegalArgumentException("Missing or invalid id field"));

            this.types = asSet(data.get("type")).orElseGet(Collections::emptySet);
            this.expiration = asInstant(data.get("expirationDate")).orElse(Instant.MAX);

            final Map subject = asMap(data.get("credentialSubject")).orElseThrow(() ->
                    new IllegalArgumentException("Missing or invalid credentialSubject field"));

            this.grantor = asUri(subject.get("id")).orElseThrow(() ->
                    new IllegalArgumentException("Missing or invalid credentialSubject.id field"));

            // V1 Access Grant
            final Map<String, Object> consent = asMap(subject.get("providedConsent"))
                .orElseThrow(() -> new AccessGrantException("Invalid Access Grant: missing providedConsent clause"));
            final Optional<URI> person = asUri(consent.get("isProvidedToPerson"));
            final Optional<URI> controller = asUri(consent.get("isProvidedToController"));
            final Optional<URI> other = asUri(consent.get("isProvidedTo"));
            this.grantee = person.orElseGet(() -> controller.orElseGet(() -> other.orElse(null)));
            this.modes = asSet(consent.get("mode")).orElseGet(Collections::emptySet);
            this.resources = asSet(consent.get("forPersonalData")).orElseGet(Collections::emptySet)
                .stream().map(URI::create).collect(Collectors.toSet());
            this.purposes = asSet(consent.get("forPurpose")).orElseGet(Collections::emptySet);
        } catch (final IOException ex) {
            throw new IllegalArgumentException("Invalid access grant", ex);
        }
    }

    public static AccessGrant ofAccessGrant(final String accessGrant) {
        return new AccessGrant(accessGrant);
    }

    public static AccessGrant ofAccessGrant(final InputStream accessGrant) {
        try {
            return new AccessGrant(IOUtils.toString(accessGrant, UTF_8));
        } catch (final IOException ex) {
            throw new AccessGrantException("Unable to read access grant", ex);
        }
    }

    static Optional<Instant> asInstant(final Object value) {
        if (value != null && value instanceof String) {
            return Optional.of(Instant.parse((String) value));
        }
        return Optional.empty();
    }

    static Optional<URI> asUri(final Object value) {
        if (value != null && value instanceof String) {
            return Optional.of(URI.create((String) value));
        }
        return Optional.empty();
    }

    static Optional<Map> asMap(final Object value) {
        if (value != null && value instanceof Map) {
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

    public Set<String> getTypes() {
        return types;
    }

    public Set<String> getModes() {
        return modes;
    }

    public Instant getExpiration() {
        return expiration;
    }

    public URI getIssuer() {
        return issuer;
    }

    public URI getIdentifier() {
        return identifier;
    }

    public Set<String> getPurpose() {
        return purposes;
    }

    public Set<URI> getResources() {
        return resources;
    }

    public URI getGrantee() {
        return grantee;
    }

    public URI getGrantor() {
        return grantor;
    }

    public String getRawGrant() {
        return rawGrant;
    }
}
