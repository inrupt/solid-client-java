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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class Utils {

    private static final URI ACCESS_GRANT = URI.create("http://www.w3.org/ns/solid/vc#SolidAccessGrant");
    private static final URI ACCESS_REQUEST = URI.create("http://www.w3.org/ns/solid/vc#SolidAccessRequest");
    public static final String REVOCATION_LIST_2020_STATUS = "RevocationList2020Status";
    public static final String VC_CONTEXT_URI = "https://www.w3.org/2018/credentials/v1";
    public static final String INRUPT_CONTEXT_URI = "https://schema.inrupt.com/credentials/v1.jsonld";
    public static final String TYPE = "type";
    public static final String VERIFIABLE_CREDENTIAL = "verifiableCredential";

    public static boolean isSuccess(final int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }

    private Utils() {
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

    static Optional<Boolean> asBoolean(final Object value) {
        if (value instanceof String) {
            return Optional.of(Boolean.parseBoolean((String) value));
        }
        return Optional.empty();
    }

    static Optional<Instant> asInstant(final Object value) {
        if (value instanceof String) {
            return Optional.of(Instant.parse((String) value));
        }
        return Optional.empty();
    }

    static Status asRevocationList2020(final Map credentialStatus) {
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

    static Set<String> getAccessGrantTypes() {
        final Set<String> types = new HashSet<>();
        types.add("SolidAccessGrant");
        types.add(ACCESS_GRANT.toString());
        types.add("SolidAccessRequest");
        types.add(ACCESS_REQUEST.toString());
        return Collections.unmodifiableSet(types);
    }

    static boolean isAccessGrant(final URI type) {
        return "SolidAccessGrant".equals(type.toString()) || ACCESS_GRANT.equals(type);
    }

    static boolean isAccessRequest(final URI type) {
        return "SolidAccessRequest".equals(type.toString()) || ACCESS_REQUEST.equals(type);

    }
}
