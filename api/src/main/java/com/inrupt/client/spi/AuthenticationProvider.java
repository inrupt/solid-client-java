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
package com.inrupt.client.spi;

import com.inrupt.client.auth.Authenticator;
import com.inrupt.client.auth.Challenge;

import java.util.Set;

/**
 * An authentication mechanism that knows how to authenticate over network connections.
 */
public interface AuthenticationProvider {

    /**
     * Return the authorization scheme, such as Bearer or DPoP.
     *
     * @return the authorization scheme
     * @deprecated as of Beta3, please use the {@link #getSchemes()} method
     */
    @Deprecated
    String getScheme();

    /**
     * Return the set of supported authorization schemes, such as Bearer or DPoP.
     *
     * @return the authorization schemes
     */
    Set<String> getSchemes();

    /**
     * Return an authenticator for the supplied challenge.
     *
     * @param challenge the HTTP challenge value
     * @return an authenticator
     */
    Authenticator getAuthenticator(Challenge challenge);
}


