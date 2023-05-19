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

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Utility classes for the Access Grant module. **/
final class Utils {

    private static final String TYPE = "type";
    private static final String REVOCATION_LIST_2020_STATUS = "RevocationList2020Status";

    public static Status asRevocationList2020(final Map credentialStatus) {
        try {
            int idx = -1;
            final Object index = credentialStatus.get("revocationListIndex");
            if (index instanceof String) {
                idx = Integer.parseInt((String) index);
            } else if (index instanceof Integer) {
                idx = (Integer) index;
            }

            final Object id = credentialStatus.get("id");
            final Object credential = credentialStatus.get("revocationListCredential");
            if (id instanceof String && credential instanceof String && idx >= 0) {
                final URI uri = URI.create((String) credential);
                return new Status(URI.create((String) id), REVOCATION_LIST_2020_STATUS, uri, idx);
            }
            throw new IllegalArgumentException("Unable to process credential status as Revocation List 2020");
        } catch (final Exception ex) {
            throw new IllegalArgumentException("Unable to process credential status data", ex);
        }
    }

    public static Optional<Instant> asInstant(final Object value) {
        if (value instanceof String) {
            return Optional.of(Instant.parse((String) value));
        }
        return Optional.empty();
    }

    public static Optional<URI> asUri(final Object value) {
        if (value instanceof String) {
            return Optional.of(URI.create((String) value));
        }
        return Optional.empty();
    }

    public static Optional<Map> asMap(final Object value) {
        if (value instanceof Map) {
            return Optional.of((Map) value);
        }
        return Optional.empty();
    }

    public static Optional<Set<String>> asSet(final Object value) {
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

    public static List<Map> getCredentialsFromPresentation(final Map<String, Object> data,
            final Set<String> supportedTypes) {
        final List<Map> credentials = new ArrayList<>();
        if (data.get("verifiableCredential") instanceof Collection) {
            for (final Object item : (Collection) data.get("verifiableCredential")) {
                if (item instanceof Map) {
                    final Map vc = (Map) item;
                    if (asSet(vc.get(TYPE)).filter(types ->
                                types.stream().anyMatch(supportedTypes::contains)).isPresent()) {
                        credentials.add(vc);
                    }
                }
            }
        }
        return credentials;
    }


    private Utils() {
        // prevent instantiation
    }
}
