/*
 * Copyright 2022 Inrupt Inc.
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
package com.inrupt.client.uma;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * A class representing the required claims that are part of an UMA interactive exchange.
 */
public class RequiredClaims {

    private final Map<String, Object> data;

    /**
     * Create an object representing an UMA {@code required_claims} data structure.
     *
     * @param data the data formatted as a {@link Map}
     */
    public RequiredClaims(final Map<String, Object> data) {
        this.data = data;
    }

    /**
     * Get a set of required {@code claim_token_format} values.
     *
     * @return the required claim token formats
     */
    public Set<String> getClaimTokenFormats() {
        return getValues("claim_token_format");
    }

    /**
     * Get a set of required {@code issuer} values.
     *
     * @return the required issuer values
     */
    public Set<String> getIssuers() {
        return getValues("issuer");
    }

    /**
     * Get an optional {@code claim_type} value.
     *
     * @return an optional claim type value
     */
    public Optional<String> getClaimType() {
        return Optional.ofNullable(getValue("claim_type"));
    }

    /**
     * Get an optional {@code friendly_name} value.
     *
     * @return an optional friendly name
     */
    public Optional<String> getFriendlyName() {
        return Optional.ofNullable(getValue("friendly_name"));
    }

    /**
     * Get an optional {@code name} value.
     *
     * @return an optional name
     */
    public Optional<String> getName() {
        return Optional.ofNullable(getValue("name"));
    }

    /**
     * Get an arbitrary String-based data property.
     *
     * @param name the property name
     * @return a data property, if present
     */
    public Optional<String> getProperty(final String name) {
        return Optional.ofNullable(getValue(name));
    }

    /**
     * Get an arbitrary Array-based data property.
     *
     * @param name the property name
     * @return a data properties
     */
    public Set<String> getProperties(final String name) {
        return getValues(name);
    }

    private String getValue(final String key) {
        final Object value = data.get(key);
        if (value instanceof String) {
            return (String) value;
        }
        return null;
    }

    private Set<String> getValues(final String key) {
        final Object values = data.get(key);

        final Set<String> results = new HashSet<>();
        if (values instanceof Collection) {
            for (final Object item : (Collection) values) {
                if (item instanceof String) {
                    results.add((String) item);
                }
            }
        }
        return results;
    }
}
