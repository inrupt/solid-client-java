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

import com.inrupt.client.uma.TokenRequest;
import com.inrupt.client.uma.TokenResponse;
import com.inrupt.client.uma.UmaClient;

import java.net.URI;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * An authentication mechanism that makes use of User Managed Access (UMA) authorization servers.
 *
 * <p>UMA defines an OAuth 2.0 profile by which applications can negotiate for an access token
 * through an iterative claims gathering process.
 *
 * @see <a href="https://docs.kantarainitiative.org/uma/wg/rec-oauth-uma-grant-2.0.html">User
 * Managed Access (UMA) 2.0 Grant for OAuth 2.0 Authorization</a>
 */
public class UmaAuthenticationMechanism implements SolidAuthenticationMechanism {

    private static final String UMA = "UMA";
    private static final String AS_URI = "as_uri";
    private static final String TICKET = "ticket";

    private String token;
    private final int priorityLevel;
    private final UmaClient umaClient;

    /**
     * Create a {@link UmaAuthenticationMechanism} with a defined priority.
     *
     * @param priority the priority of this authentication mechanism
     */
    public UmaAuthenticationMechanism(final int priority) {
        this(priority, new UmaClient());
    }

    /**
     * Create a {@link UmaAuthenticationMechanism} with a defined priority.
     *
     * @param priority the priority of this authentication mechanism
     * @param umaClient an UMA HTTP client
     */
    public UmaAuthenticationMechanism(final int priority, final UmaClient umaClient) {
        this.priorityLevel = priority;
        this.umaClient = umaClient;
    }

    @Override
    public String getScheme() {
        return UMA;
    }

    @Override
    public SolidAuthenticationMechanism.Authenticator getAuthenticator(final Challenge challenge) {
        validate(challenge);
        return new UmaAuthenticator(umaClient, challenge, priorityLevel);
    }

    static void validate(final Challenge challenge) {
        if (challenge == null ||
                !UMA.equalsIgnoreCase(challenge.getScheme()) ||
                challenge.getParameter(AS_URI) == null ||
                challenge.getParameter(TICKET) == null) {
            throw new AuthenticationException("Invalid challenge for UMA authentication");
        }
    }

    /**
     * A mechanism capable of retrieving an access token from an UMA authorization server.
     */
    public class UmaAuthenticator implements SolidAuthenticationMechanism.Authenticator {

        private final UmaClient umaClient;
        private final Challenge challenge;
        private final int priorityLevel;

        /**
         * The UmaAuthenticator with a defined challenge and priority.
         *
         * @param challenge the resource server challenge
         * @param priority the priority of this authentication mechanism
         */
        protected UmaAuthenticator(final UmaClient umaClient, final Challenge challenge, final int priority) {
            this.priorityLevel = priority;
            this.umaClient = umaClient;
            this.challenge = challenge;
        }

        @Override
        public int priority() {
            return priorityLevel;
        }

        @Override
        public String getScheme() {
            return UMA;
        }

        @Override
        public AccessToken authenticate() {
            final URI as = URI.create(challenge.getParameter(AS_URI));
            final String ticket = challenge.getParameter(TICKET);
            // TODO populate scopes
            final List<String> requestScopes = Collections.emptyList();

            final var metadata = umaClient.metadata(as);
            final var request = new TokenRequest(ticket, null, null, null, requestScopes);
            // TODO implement the mapping function
            final var token = umaClient.token(metadata.tokenEndpoint, request, needInfo -> null);
            return new UmaAccessToken(token.accessToken, token.tokenType,
                    Instant.now().plusSeconds(token.expiresIn), getScopes(token));
        }

        @Override
        public CompletionStage<AccessToken> authenticateAsync() {
            final URI as = URI.create(challenge.getParameter(AS_URI));
            final String ticket = challenge.getParameter(TICKET);

            // TODO populate scopes
            final List<String> requestScopes = Collections.emptyList();
            final var request = new TokenRequest(ticket, null, null, null, requestScopes);

            return umaClient.metadataAsync(as)
                // TODO implement the mapping function
                .thenCompose(metadata -> umaClient.tokenAsync(metadata.tokenEndpoint, request, needInfo ->
                            CompletableFuture.completedFuture(null)))
                .thenApply(token -> new UmaAccessToken(token.accessToken, token.tokenType,
                            Instant.now().plusSeconds(token.expiresIn), getScopes(token)));
        }
    }

    static List<String> getScopes(final TokenResponse token) {
        if (token.scope != null) {
            return List.of(token.scope.split(" "));
        }
        return Collections.emptyList();
    }

    public class UmaAccessToken implements AccessToken {
        private final Instant expiration;
        private final List<String> scopes;
        private final String token;
        private final String type;

        protected UmaAccessToken(final String token, final String type, final Instant expiration,
                final List<String> scopes) {
            this.token = Objects.requireNonNull(token);
            this.type = Objects.requireNonNull(type);
            this.expiration = Objects.requireNonNull(expiration);
            this.scopes = Objects.requireNonNull(scopes);
        }

        public String getScheme() {
            return type;
        }

        public String getToken() {
            return token;
        }

        public Instant getExpiration() {
            return expiration;
        }

        public List<String> getScopes() {
            return scopes;
        }
    }
}

