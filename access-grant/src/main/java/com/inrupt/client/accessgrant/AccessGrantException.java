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

import com.inrupt.client.InruptClientException;

import java.util.OptionalInt;

/**
 * A runtime exception for use with Access Grants.
 */
public class AccessGrantException extends InruptClientException {

    private static final long serialVersionUID = 1683211587796884322L;

    private final int status;

    /**
     * Create an AccessGrant exception.
     *
     * @param message the message
     */
    public AccessGrantException(final String message) {
        this(message, 0);
    }

    /**
     * Create an AccessGrant exception.
     *
     * @param message the message
     * @param cause the cause
     */
    public AccessGrantException(final String message, final Throwable cause) {
        this(message, 0, cause);
    }

    /**
     * Create an AccessGrant exception.
     *
     * @param message the message
     * @param statusCode the HTTP status code
     */
    public AccessGrantException(final String message, final int statusCode) {
        super(message);
        this.status = statusCode;
    }

    /**
     * Create an AccessGrant exception.
     *
     * @param message the message
     * @param cause the cause
     * @param statusCode the HTTP status code
     */
    public AccessGrantException(final String message, final int statusCode, final Throwable cause) {
        super(message, cause);
        this.status = statusCode;
    }

    /**
     * Get the status code.
     *
     * @return the status code, if available
     */
    public OptionalInt getStatusCode() {
        return status >= 100 ? OptionalInt.of(status) : OptionalInt.empty();
    }
}
