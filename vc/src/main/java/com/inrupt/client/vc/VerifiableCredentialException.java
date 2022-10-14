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
package com.inrupt.client.vc;

import com.inrupt.client.core.InruptClientException;

import java.util.Optional;

/**
 * A runtime exception for use with Verifiable Credential-related errors.
 */
public class VerifiableCredentialException extends InruptClientException {

    private static final long serialVersionUID = 4828374653830284474L;
    private static int status;
    private static String body;

    /**
     * Create a Verifiable Credential exception.
     *
     * @param message the message
     */
    public VerifiableCredentialException(final String message) {
        super(message);
    }

    /**
     * Create a Verifiable Credential exception.
     *
     * @param message the message
     * @param cause the cause
     */
    public VerifiableCredentialException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Create a Verifiable Credential exception.
     *
     * @param message the message
     * @param bodyContent the body content
     */
    public VerifiableCredentialException(final String message, final String bodyContent) {
        super(message);
        body = bodyContent;
    }


    /**
     * Create a Verifiable Credential exception.
     *
     * @param message the message
     * @param bodyContent the body content
     * @param statusCode the HTTP status code
     */
    public VerifiableCredentialException(final String message, final String bodyContent, final int statusCode) {
        super(message);
        body = bodyContent;
        status = statusCode;
    }

    /**
     * Create a Verifiable Credential exception.
     *
     * @param message the message
     * @param bodyContent the body content
     * @param cause the cause
     */
    public VerifiableCredentialException(final String message, final String bodyContent, final Throwable cause) {
        super(message, cause);
        body = bodyContent;
    }

    /**
     * Create a Verifiable Credential exception.
     *
     * @param message the message
     * @param statusCode the HTTP status code
     */
    public VerifiableCredentialException(final String message, final int statusCode) {
        super(message);
        status = statusCode;
    }

    /**
     * Get the HTTP status code of the response if there is one.
     *
     * @return the HTTP status code
     */
    public Optional<Integer> getStatus() {
        return Optional.ofNullable(status);
    }

    /**
     * Get the body content of the response if there is one.
     *
     * @return the body content
     */
    public Optional<String> getBody() {
        return Optional.ofNullable(body);
    }

}

