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

import com.inrupt.client.uma.*;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
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

    private final int priorityLevel;
    private final UmaClient umaClient;
    private final NeedInfoHandler claimHandler;

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
        this.umaClient = Objects.requireNonNull(umaClient);
        // TODO add specific handlers for VC and OpenId once those modules are ready to be integrated
        this.claimHandler = new NeedInfoHandler();
    }

    @Override
    public String getScheme() {
        return UMA;
    }

    @Override
    public SolidAuthenticationMechanism.Authenticator getAuthenticator(final Challenge challenge) {
        validate(challenge);
        return new UmaAuthenticator(umaClient, claimHandler, challenge, priorityLevel);
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
        private final NeedInfoHandler claimHandler;

        /**
         * The UmaAuthenticator with a defined challenge and priority.
         *
         * @param umaClient the UMA client
         * @param challenge the resource server challenge
         * @param priority the priority of this authentication mechanism
         */
        protected UmaAuthenticator(final UmaClient umaClient, final NeedInfoHandler claimHandler,
                final Challenge challenge, final int priority) {
            this.priorityLevel = priority;
            this.umaClient = umaClient;
            this.challenge = challenge;
            this.claimHandler = claimHandler;
        }

        /**
         * Add a claim gathering handler.
         *
         * @param handler the claim gathering handler
         */
        public void addHandler(final ClaimGatheringHandler handler) {
            claimHandler.addHandler(Objects.requireNonNull(handler));
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
            return authenticateAsync().toCompletableFuture().join();
        }

        @Override
        public CompletionStage<AccessToken> authenticateAsync() {
            final URI as = URI.create(challenge.getParameter(AS_URI));
            final String ticket = challenge.getParameter(TICKET);

            final var request = new TokenRequest(ticket, null, null, null, Collections.emptyList());
            // TODO add the dpop algorithm
            final String proofAlgorithm = null;
            return umaClient.metadataAsync(as)
                .thenCompose(metadata -> umaClient.tokenAsync(metadata.tokenEndpoint, request,
                            claimHandler::async))
                .thenApply(token -> new AccessToken(token.accessToken, token.tokenType,
                            Instant.now().plusSeconds(token.expiresIn), getScopes(token), proofAlgorithm));
        }
    }

    static List<String> getScopes(final TokenResponse token) {
        if (token.scope != null) {
            return List.of(token.scope.split(" "));
        }
        return Collections.emptyList();
    }

    static class NeedInfoHandler {

        private final List<ClaimGatheringHandler> handlers = new ArrayList<>();

        public NeedInfoHandler(final ClaimGatheringHandler... handlers) {
            this.handlers.addAll(List.of(handlers));
        }

        public void addHandler(final ClaimGatheringHandler handler) {
            this.handlers.add(handler);
        }

        public ClaimToken sync(final NeedInfo needInfo) {
            for (final var requiredClaims : needInfo.getRequiredClaims()) {
                for (final var handler : handlers) {
                    if (handler.isCompatibleWith(requiredClaims)) {
                        return handler.gather();
                    }
                }
            }
            return null;
        }

        public CompletionStage<ClaimToken> async(final NeedInfo needInfo) {
            for (final var requiredClaims : needInfo.getRequiredClaims()) {
                for (final var handler : handlers) {
                    if (handler.isCompatibleWith(requiredClaims)) {
                        return handler.gatherAsync();
                    }
                }
            }
            return CompletableFuture.completedFuture(null);
        }
    }
}

