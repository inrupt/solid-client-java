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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RequiredClaims {

    private final Map<String, Object> data;

    public RequiredClaims(final Map<String, Object> data) {
        this.data = data;
    }

    public List<String> getClaimTokenFormats() {
        return getValues("claim_token_format");
    }

    public List<String> getIssuers() {
        return getValues("issuer");
    }

    public Optional<String> getClaimType() {
        return Optional.ofNullable(getValue("claim_type"));
    }

    public Optional<String> getFriendlyName() {
        return Optional.ofNullable(getValue("friendly_name"));
    }

    public Optional<String> getName() {
        return Optional.ofNullable(getValue("name"));
    }

    public Optional<String> getProperty(final String name) {
        return Optional.ofNullable(getValue(name));
    }

    public List<String> getProperties(final String name) {
        return getValues(name);
    }

    private String getValue(final String key) {
        final var value = data.get(key);
        if (value instanceof String) {
            return (String) value;
        }
        return null;
    }

    private List<String> getValues(final String key) {
        final var values = data.get(key);

        final var results = new ArrayList<String>();
        if (values instanceof Collection) {
            for (final var item : (Collection) values) {
                if (item instanceof String) {
                    results.add((String) item);
                }
            }
        }
        return results;
    }
}
