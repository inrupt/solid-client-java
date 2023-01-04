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

import com.inrupt.client.Authenticator;
import com.inrupt.client.Credential;
import com.inrupt.client.Request;
import com.inrupt.client.Session;
import com.inrupt.client.spi.AuthenticationProvider;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * An authentication mechanism that makes use of OpenID Tokens.
 */
public class OpenIdAuthenticationProvider implements AuthenticationProvider {

    private static final String BEARER = "Bearer";

    private final int priorityLevel;

    public OpenIdAuthenticationProvider() {
        this(50);
    }

    /**
     * Create an {@link OpenIdAuthenticationProvider} with a defined priority.
     *
     * @param priority the priority of this authentication mechanism
     */
    public OpenIdAuthenticationProvider(final int priority) {
        this.priorityLevel = priority;
    }

    @Override
    public String getScheme() {
        return BEARER;
    }

    @Override
    public Authenticator getAuthenticator(final Authenticator.Challenge challenge) {
        validate(challenge);
        return new OpenIdAuthenticator(challenge, priorityLevel);
    }

    static void validate(final Authenticator.Challenge challenge) {
        if (challenge == null ||
                !BEARER.equalsIgnoreCase(challenge.getScheme())) {
            throw new OpenIdException("Invalid challenge for OpenID authentication");
        }
    }

    /**
     * A mechanism capable of retrieving an access token from an OpenId Provider.
     */
    public class OpenIdAuthenticator implements Authenticator {

        private final Authenticator.Challenge challenge;
        private final int priorityLevel;

        /**
         * The OpenIdAuthenticator with a defined challenge and priority.
         *
         * @param challenge the resource server challenge
         * @param priority the priority of this authentication mechanism
         */
        protected OpenIdAuthenticator(final Authenticator.Challenge challenge, final int priority) {
            this.priorityLevel = priority;
            this.challenge = challenge;
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
            final String thumbprint = session.selectThumbprint(algorithms).orElse(null);

            final Optional<CompletionStage<Credential>> token = session.getCredential(OpenIdSession.ID_TOKEN)
                .map(CompletableFuture::completedFuture);
            return token
                .orElseGet(() -> session.authenticate(request, algorithms)
                    .thenApply(credential -> credential
                        .orElseThrow(() -> new OpenIdException("Unable to perform OpenID authentication"))));
        }
    }
}

