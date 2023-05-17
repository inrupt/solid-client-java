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
import java.util.Optional;
import java.util.Set;

public interface AccessCredential {

    /**
     * Get the types of the access credential.
     *
     * @return the access credential types
     */
    Set<String> getTypes();

    /**
     * Get the access modes of the access credential.
     *
     * @return the access credential types
     */
    Set<String> getModes();

    /**
     * Get the revocation status of the access credential.
     *
     * @return the revocation status, if present
     */
    Optional<Status> getStatus();

    /**
     * Get the expiration time of the access credential.
     *
     * @return the expiration time
     */
    Instant getExpiration();

    /**
     * Get the issuer of the access credential.
     *
     * @return the issuer
     */
    URI getIssuer();

    /**
     * Get the identifier of the access credential.
     *
     * @return the identifier
     */
    URI getIdentifier();

    /**
     * Get the collection of purposes associated with the access credential.
     *
     * @return the purposes
     */
    Set<String> getPurposes();

    /**
     * Get the resources associated with the access credential.
     *
     * @return the associated resource identifiers
     */
    Set<URI> getResources();

    /**
     * Get the creator of this access credential.
     *
     * @return the creator
     */
    URI getCreator();

    /**
     * Get the recipient of this access credential.
     *
     * @return the recipient, if present
     */
    Optional<URI> getRecipient();

    /**
     * Serialize this access credential as a String.
     *
     * @return a serialized form of the credential
     */
    String serialize();
}
