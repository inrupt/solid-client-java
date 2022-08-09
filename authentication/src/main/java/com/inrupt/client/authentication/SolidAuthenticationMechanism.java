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
package com.inrupt.client.authentication;

import java.util.concurrent.CompletionStage;

/**
 * An authentication mechanism that knows how to authenticate network connections.
 */
public interface SolidAuthenticationMechanism {

    /**
     * Return the authorization scheme, such as Bearer or DPoP.
     *
     * @return the authorization scheme
     */
    String getScheme();

    /**
     * Return an authenticator for the supplied challenge.
     *
     * @param challenge the HTTP challenge value
     * @return an authenticator
     */
    Authenticator getAuthenticator(Challenge challenge);

    /**
     * An interface for performing authentication over a network connection.
     */
    interface Authenticator {

        /**
         * Return the authorization scheme, such as Bearer or DPoP.
         *
         * @return the authorization scheme
         */
        String getScheme();

        /**
         * The priority of the authentication mechanism.
         *
         * <p>A higher value relates to a higher priority
         *
         * @return the priority value
         */
        int priority();

        /**
         * Perform a synchronous authentication process, resulting in an access token.
         *
         * @return the access token
         */
        AccessToken authenticate();

        /**
         * Perform an ansynchronous authentication process, resulting in an access token.
         *
         * @return the next stage of completion, containing the access token
         */
        CompletionStage<AccessToken> authenticateAsync();
    }
}
