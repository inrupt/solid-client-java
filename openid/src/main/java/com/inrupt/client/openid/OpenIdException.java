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
package com.inrupt.client.openid;

import java.util.Optional;

/**
 * A runtime exception for use with OpenID-related errors.
 */
public class OpenIdException extends RuntimeException {

    private static final long serialVersionUID = 3892357293662737233L;
    private static int status;

    /**
     * Create an OpenID exception.
     *
     * @param message the message
     */
    public OpenIdException(final String message) {
        super(message);
    }

    /**
     * Create an OpenID exception.
     *
     * @param message the message
     * @param cause the cause
     */
    public OpenIdException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Create a OpenID exception.
     *
     * @param message the message
     * @param statusCode the HTTP status code
     */
    public OpenIdException(final String message, final int statusCode) {
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

}

