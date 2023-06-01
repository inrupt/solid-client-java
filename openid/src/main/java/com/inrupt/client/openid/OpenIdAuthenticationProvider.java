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
package com.inrupt.client.openid;

import com.inrupt.client.Request;
import com.inrupt.client.auth.Authenticator;
import com.inrupt.client.auth.Challenge;
import com.inrupt.client.auth.Credential;
import com.inrupt.client.auth.Session;
import com.inrupt.client.spi.AuthenticationProvider;

import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * An authentication mechanism that makes use of OpenID Tokens.
 */
public class OpenIdAuthenticationProvider implements AuthenticationProvider {

    private static final String BEARER = "Bearer";
    private static final String DPOP = "DPoP";

    private final int priorityLevel;
    private final Set<String> schemes = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

    public OpenIdAuthenticationProvider() {
        this(50);
        schemes.add(BEARER);
        schemes.add(DPOP);
    }

    /**
     * Create an {@link OpenIdAuthenticationProvider} with a defined priority.
     *
     * @param priority the priority of this authentication mechanism
     */
    public OpenIdAuthenticationProvider(final int priority) {
        this.priorityLevel = priority;
    }

    /* deprecated */
    @Override
    public String getScheme() {
        return BEARER;
    }

    @Override
    public Set<String> getSchemes() {
        return schemes;
    }

    @Override
    public Authenticator getAuthenticator(final Challenge challenge) {
        validate(challenge);
        return new OpenIdAuthenticator(priorityLevel);
    }

    void validate(final Challenge challenge) {
        if (challenge == null ||
                !schemes.contains(challenge.getScheme())) {
            throw new OpenIdException("Invalid challenge for OpenID authentication");
        }
    }

    /**
     * A mechanism capable of retrieving an access token from an OpenId Provider.
     */
    public class OpenIdAuthenticator implements Authenticator {

        private final int priorityLevel;

        /**
         * The OpenIdAuthenticator with a defined priority.
         *
         * @param priority the priority of this authentication mechanism
         */
        protected OpenIdAuthenticator(final int priority) {
            this.priorityLevel = priority;
        }

        @Override
        public String getName() {
            return "OpenId";
        }

        @Override
        public int getPriority() {
            return priorityLevel;
        }

        @Override
        public CompletionStage<Credential> authenticate(final Session session, final Request request,
                final Set<String> algorithms) {
            return CompletableFuture.completedFuture(session
                    .getCredential(OpenIdSession.ID_TOKEN, request.uri()).orElse(null));
        }
    }
}

