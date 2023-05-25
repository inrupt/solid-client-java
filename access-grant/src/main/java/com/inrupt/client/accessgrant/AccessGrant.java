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
 * An Access Grant abstraction, for use when interacting with Solid resources.
 */
public class AccessGrant extends AccessCredential {

    private static final Set<String> supportedTypes = getSupportedTypes();
    private static final JsonService jsonService = ServiceProvider.getJsonService();

    /**
     * Read a verifiable presentation as an AccessGrant.
     *
     * @param grant the Access Grant serialized as a verifiable presentation
     */
    protected AccessGrant(final URI identifier, final String credential, final CredentialData data,
            final CredentialMetadata metadata) {
        super(identifier, credential, data, metadata);
    }

    /**
     * Create an AccessGrant object from a VerifiablePresentation.
     *
     * @param accessGrant the access grant
     * @return a parsed access grant
     * @deprecated As of Beta3, please use the {@link AccessGrant#of} method
     */
    @Deprecated
    public static AccessGrant ofAccessGrant(final String accessGrant) {
        return of(accessGrant);
    }

    /**
     * Create an AccessGrant object from a VerifiablePresentation.
     *
     * @param accessGrant the access grant
     * @return a parsed access grant
     * @deprecated As of Beta3, please use the {@link AccessGrant#of} method
     */
    @Deprecated
    public static AccessGrant ofAccessGrant(final InputStream accessGrant) {
        return of(accessGrant);
    }

    /**
     * Create an AccessGrant object from a serialized form.
     *
     * @param serialization the serialized access grant
     * @return a parsed access grant
     */
    public static AccessGrant of(final String serialization) {
        try {
            return parse(serialization);
        } catch (final IOException ex) {
            throw new IllegalArgumentException("Unable to read access grant", ex);
        }
    }

    /**
     * Create an AccessGrant object from a serialized form.
     *
     * @param serialization the serialized access grant
     * @return a parsed access grant
     */
    public static AccessGrant of(final InputStream serialization) {
        try {
            return of(IOUtils.toString(serialization, UTF_8));
        } catch (final IOException ex) {
            throw new IllegalArgumentException("Unable to read access grant", ex);
        }
    }

    /**
     * Get the purposes of the access grant.
     *
     * @return the access grant purposes
     * @deprecated as of Beta3, please use the {@link #getPurposes()} method
     */
    @Deprecated
    public Set<String> getPurpose() {
        return getPurposes();
    }

    /**
     * Get the agent to whom access is granted.
     *
     * @return the agent that was granted access
     * @deprecated As of Beta3, please use {@link #getRecipient}
     */
    @Deprecated
    public Optional<URI> getGrantee() {
        return getRecipient();
    }

    /**
     * Get the agent who granted access.
     *
     * @return the agent granting access
     * @deprecated As of Beta3, please use {@link #getCreator}
     */
    @Deprecated
    public URI getGrantor() {
        return getCreator();
    }

    /**
     * Get the raw access grant.
     *
     * @return the access grant
     * @deprecated as of Beta3, please use the {@link #serialize} method
     */
    @Deprecated
    public String getRawGrant() {
        return serialize();
    }

    static Set<String> getSupportedTypes() {
        final Set<String> types = new HashSet<>();
        types.add("SolidAccessGrant");
        types.add("http://www.w3.org/ns/solid/vc#SolidAccessGrant");
        return Collections.unmodifiableSet(types);
    }

    static AccessGrant parse(final String serialization) throws IOException {
        try (final InputStream in = new ByteArrayInputStream(serialization.getBytes())) {
            // TODO process as JSON-LD
            final Map<String, Object> data = jsonService.fromJson(in,
                    new HashMap<String, Object>(){}.getClass().getGenericSuperclass());

            final List<Map> vcs = getCredentialsFromPresentation(data, supportedTypes);
            if (vcs.size() != 1) {
                throw new IllegalArgumentException(
                        "Invalid Access Grant: ambiguous number of verifiable credentials");
            }
            final Map vc = vcs.get(0);

            if (asSet(data.get(TYPE)).orElseGet(Collections::emptySet).contains("VerifiablePresentation")) {
                final URI identifier = asUri(vc.get("id")).orElseThrow(() ->
                        new IllegalArgumentException("Missing or invalid id field"));

                // Extract metadata
                final CredentialMetadata credentialMetadata = extractMetadata(vc);

                // Extract V1 Access Grant data, using gConsent
                final Map consent = extractConsent(vc, "providedConsent");

                final Optional<URI> person = asUri(consent.get("isProvidedToPerson"));
                final Optional<URI> controller = asUri(consent.get("isProvidedToController"));
                final Optional<URI> other = asUri(consent.get("isProvidedTo"));

                final URI recipient = person.orElseGet(() -> controller.orElseGet(() -> other.orElse(null)));
                final Set<String> modes = asSet(consent.get("mode")).orElseGet(Collections::emptySet);
                final Set<URI> resources = asSet(consent.get("forPersonalData")).orElseGet(Collections::emptySet)
                    .stream().map(URI::create).collect(Collectors.toSet());
                final Set<String> purposes = asSet(consent.get("forPurpose")).orElseGet(Collections::emptySet);
                final CredentialData credentialData = new CredentialData(resources, modes, purposes, recipient);

                return new AccessGrant(identifier, serialization, credentialData, credentialMetadata);
            } else {
                throw new IllegalArgumentException("Invalid Access Grant: missing VerifiablePresentation type");
            }
        }
    }
}
