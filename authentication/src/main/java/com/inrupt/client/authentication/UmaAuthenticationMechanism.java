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

public class UmaAuthenticationMechanism implements SolidAuthenticationMechanism {

    private String token;
    private final int priorityLevel;

    public UmaAuthenticationMechanism(final int priority) {
        this.priorityLevel = priority;
    }

    @Override
    public String getScheme() {
        return "Uma";
    }

    @Override
    public SolidAuthenticationMechanism.Authenticator getAuthenticator(final Challenge challenge) {
        return new UmaAuthenticator(challenge, priorityLevel);
    }

    public class UmaAuthenticator implements SolidAuthenticationMechanism.Authenticator {

        private final Challenge challenge;
        private final int priorityLevel;

        protected UmaAuthenticator(final Challenge challenge, final int priority) {
            this.priorityLevel = priority;
            this.challenge = challenge;
        }

        @Override
        public int priority() {
            return priorityLevel;
        }

        @Override
        public String getScheme() {
            return "Uma";
        }

        @Override
        public String authenticate() {
            // TODO implement, retrieve the token
            return null;
        }

        @Override
        public CompletionStage<String> authenticateAsync() {
            // TODO implement, retrieve the token
            return null;
        }
    }
}

