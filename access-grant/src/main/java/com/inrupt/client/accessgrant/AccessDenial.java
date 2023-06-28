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
public class AccessDenial extends AccessCredential {

    private static final Set<String> supportedTypes = getSupportedTypes();
    private static final JsonService jsonService = ServiceProvider.getJsonService();

    /**
     * Read a verifiable presentation as an AccessDenial.
     *
     * @param identifier the credential identifier
     * @param credential the serialized form of an Access Denial
     * @param data the user-managed data associated with the credential
     * @param metadata the server-managed data associated with the credential
     */
    protected AccessDenial(final URI identifier, final String credential, final CredentialData data,
            final CredentialMetadata metadata) {
        super(identifier, credential, data, metadata);
    }

    /**
     * Create an AccessDenial object from a serialized form.
     *
     * @param serialization the serialized access denial
     * @return a parsed access denial
     */
    public static AccessDenial of(final String serialization) {
        try {
            return parse(serialization);
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

    static Set<String> getSupportedTypes() {
        final Set<String> types = new HashSet<>();
        types.add("SolidAccessDenial");
        types.add("http://www.w3.org/ns/solid/vc#SolidAccessDenial");
        return Collections.unmodifiableSet(types);
    }

    static AccessDenial parse(final String serialization) throws IOException {
        try (final InputStream in = new ByteArrayInputStream(serialization.getBytes())) {
            // TODO process as JSON-LD
            final Map<String, Object> data = jsonService.fromJson(in,
                    new HashMap<String, Object>(){}.getClass().getGenericSuperclass());

            final List<Map<String, Object>> vcs = getCredentialsFromPresentation(data, supportedTypes);
            if (vcs.size() != 1) {
                throw new IllegalArgumentException(
                        "Invalid Access Denial: ambiguous number of verifiable credentials");
            }
            final Map<String, Object> vc = vcs.get(0);

            if (asSet(data.get(TYPE)).orElseGet(Collections::emptySet).contains("VerifiablePresentation")) {
                final URI identifier = asUri(vc.get("id")).orElseThrow(() ->
                        new IllegalArgumentException("Missing or invalid id field"));

                // Extract metadata
                final CredentialMetadata credentialMetadata = extractMetadata(vc);


                // Extract V1 Access Denial data, using gConsent
                final Map<String, Object> consent = extractConsent(vc, "providedConsent");

                final Optional<URI> person = asUri(consent.get("isProvidedToPerson"));
                final Optional<URI> controller = asUri(consent.get("isProvidedToController"));
                final Optional<URI> other = asUri(consent.get("isProvidedTo"));

                final URI recipient = person.orElseGet(() -> controller.orElseGet(() -> other.orElse(null)));
                final Set<String> modes = asSet(consent.get("mode")).orElseGet(Collections::emptySet);
                final Set<URI> resources = asSet(consent.get("forPersonalData")).orElseGet(Collections::emptySet)
                    .stream().map(URI::create).collect(Collectors.toSet());
                final Set<URI> purposes = asSet(consent.get("forPurpose")).orElseGet(Collections::emptySet)
                    .stream().flatMap(AccessCredential::filterUris).collect(Collectors.toSet());
                final CredentialData credentialData = new CredentialData(resources, modes, purposes, recipient);

                return new AccessDenial(identifier, serialization, credentialData, credentialMetadata);
            } else {
                throw new IllegalArgumentException("Invalid Access Denial: missing VerifiablePresentation type");
            }
        }
    }
}
