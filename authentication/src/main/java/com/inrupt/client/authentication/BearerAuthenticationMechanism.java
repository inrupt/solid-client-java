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
 * An authentication mechanism that makes use of bearer tokens.
 */
public class BearerAuthenticationMechanism implements SolidAuthenticationMechanism {

    private final int priorityLevel;

    /**
     * Create a {@link BearerAuthenticationMechanism} with a defined priority.
     *
     * @param priority the priority of this authentication mechanism
     */
    public BearerAuthenticationMechanism(final int priority) {
        this.priorityLevel = priority;
    }

    @Override
    public String getScheme() {
        return "Bearer";
    }

    @Override
    public SolidAuthenticationMechanism.Authenticator getAuthenticator(final Challenge challenge) {
        return new BearerAuthenticator(priorityLevel);
    }

    /**
     * A mechanism capable of retrieving a bearer token for use with Solid resources.
     */
    public class BearerAuthenticator implements SolidAuthenticationMechanism.Authenticator {

        private final int priorityLevel;

        /**
         * The BearerAuthenticator with a defined priority.
         *
         * @param priority the priority of this mechanism
         */
        protected BearerAuthenticator(final int priority) {
            this.priorityLevel = priority;
        }

        @Override
        public int priority() {
            return priorityLevel;
        }

        @Override
        public String getScheme() {
            return "Bearer";
        }

        @Override
        public AccessToken authenticate() {
            // TODO implement, retrieve token
            return null;
        }

        @Override
        public CompletionStage<AccessToken> authenticateAsync() {
            // TODO implement, retrieve token
            return null;
        }
    }
}

