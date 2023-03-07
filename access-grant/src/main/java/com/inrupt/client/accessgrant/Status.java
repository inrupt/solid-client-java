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
import java.util.Objects;

/**
 * A class for representing status information of an Access Grant.
 *
 * @see <a href="https://www.w3.org/TR/vc-data-model/#status">W3C Verifiable Credential Data Model: Status</a>
 */
public class Status {

    private final URI identifier;
    private final String type;
    private final int index;
    private final URI credential;

    /**
     * Create a new Status object for an Access Grant.
     *
     * @param identifier the status identifier
     * @param type the credential status type
     * @param credential the identifier for the status list credential
     * @param index the index offset for the status list credential
     */
    public Status(final URI identifier, final String type, final URI credential, final int index) {
        this.identifier = Objects.requireNonNull(identifier, "Status identifier may not be null!");
        this.type = Objects.requireNonNull(type, "Status type may not be null!");
        this.credential = Objects.requireNonNull(credential, "Status credential may not be null!");
        this.index = index;
    }

    /**
     * Get the index value for this credential status.
     *
     * @return the index value
     */
    public int getIndex() {
        return index;
    }

    /**
     * Get the identifier for the status list credential.
     *
     * @return the status credential identifier
     */
    public URI getCredential() {
        return credential;
    }

    /**
     * Get the identifier for this credential status.
     *
     * @return the status identifier
     */
    public URI getIdentifier() {
        return identifier;
    }

    /**
     * Get the type of this credential status.
     *
     * @return the status type
     */
    public String getType() {
        return type;
    }
}
