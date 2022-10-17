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
package com.inrupt.client.api;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * A data object for a Verifiable Credential.
 */
public class VerifiableCredential {

    /**
     * The JSON-LD Context values.
     */
    public List<String> context;

    /**
     * The credential identifier.
     */
    public String id;

    /**
     * The credential types.
     */
    public List<String> type;

    /**
     * The credential issuer.
     */
    public String issuer;

    /**
     * The credential issuance date.
     */
    public Instant issuanceDate;

    /**
     * The credential expiration date.
     */
    public Instant expirationDate;

    /**
     * The credential subject.
     */
    public Map<String, Object> credentialSubject;

    /**
     * The credential status.
     */
    public Map<String, Object> credentialStatus;

    /**
     * The credential signature.
     */
    public Map<String, Object> proof;
}
