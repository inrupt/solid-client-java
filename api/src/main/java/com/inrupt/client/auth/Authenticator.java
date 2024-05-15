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
package com.inrupt.client.auth;

import com.inrupt.client.Request;

import java.util.Set;
import java.util.concurrent.CompletionStage;

/**
 * An interface for handling authentication challenges.
 */
public interface Authenticator {

    /**
     * Gets the authenticator name (e.g. UMA, OpenID).
     *
     * @return the name
     */
    String getName();

    /**
     * Gets the priority of the authenticator.
     *
     * @return the priority
     */
    int getPriority();

    /**
     * Perform an ansynchronous authentication process, resulting in an access token.
     *
     * @param session the client session
     * @param request the HTTP request
     * @param algorithms the supported DPoP algorithms
     * @return the next stage of completion, containing the access token
     */
    CompletionStage<Credential> authenticate(Session session, Request request, Set<String> algorithms);
}
