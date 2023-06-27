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

import java.net.URI;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * Configuration properties for the Access Grant Client.
 */
// This class may be made public at a later point
class AccessGrantConfiguration {

    /** The gConsent-based schema. **/
    public static final URI GCONSENT = URI.create("https://w3id.org/GConsent");

    private static final Set<URI> SUPPORTED_SCHEMA = Collections.singleton(GCONSENT);

    private final URI issuer;

    private URI schema = GCONSENT;

    /**
     * Create an access grant configuration.
     *
     * @param issuer the issuer
     */
    public AccessGrantConfiguration(final URI issuer) {
        this.issuer = Objects.requireNonNull(issuer, "Issuer may not be null!");
    }

    /**
     * Set the schema used by the client.
     *
     * @param schema the schema in use by the client
     */
    public void setSchema(final URI schema) {
        if (schema != null && SUPPORTED_SCHEMA.contains(schema)) {
            this.schema = schema;
        } else {
            throw new IllegalArgumentException("Invalid schema: [" + schema + "]");
        }
    }

    /**
     * Get the schema used by the client.
     *
     * @return the schema URI
     */
    public URI getSchema() {
        return schema;
    }

    /**
     * Get the issuer for this client.
     *
     * @return the access grant issuer
     */
    public URI getIssuer() {
        return issuer;
    }
}
