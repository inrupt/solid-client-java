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
package com.inrupt.client.uma;

import com.inrupt.client.Authenticator;
import com.inrupt.client.Credential;
import com.inrupt.client.Request;
import com.inrupt.client.Session;
import com.inrupt.client.spi.AuthenticationProvider;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
public class UmaAuthenticationProvider implements AuthenticationProvider {

    public static final String ID_TOKEN = "http://openid.net/specs/openid-connect-core-1_0.html#IDToken";

    private static final String UMA = "UMA";
    private static final String AS_URI = "as_uri";
    private static final String TICKET = "ticket";

    private final int priorityLevel;
    private final UmaClient umaClient;
    private final NeedInfoHandler claimHandler;

    public UmaAuthenticationProvider() {
        this(100);
    }

    /**
     * Create a {@link UmaAuthenticationProvider} with a defined priority.
     *
     * @param priority the priority of this authentication mechanism
     */
    public UmaAuthenticationProvider(final int priority) {
        this(priority, new UmaClient());
    }

    /**
     * Create a {@link UmaAuthenticationProvider} with a defined priority.
     *
     * @param priority the priority of this authentication mechanism
     * @param umaClient an UMA HTTP client
     */
    public UmaAuthenticationProvider(final int priority, final UmaClient umaClient) {
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
    public Authenticator getAuthenticator(final Authenticator.Challenge challenge) {
        validate(challenge);
        return new UmaAuthenticator(umaClient, claimHandler, challenge, priorityLevel);
    }

    static void validate(final Authenticator.Challenge challenge) {
        if (challenge == null ||
                !UMA.equalsIgnoreCase(challenge.getScheme()) ||
                challenge.getParameter(AS_URI) == null ||
                challenge.getParameter(TICKET) == null) {
            throw new UmaException("Invalid challenge for UMA authentication");
        }
    }

    /**
     * A mechanism capable of retrieving an access token from an UMA authorization server.
     */
    public class UmaAuthenticator implements Authenticator {

        private final UmaClient umaClient;
        private final Authenticator.Challenge challenge;
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
                final Authenticator.Challenge challenge, final int priority) {
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
        public String getName() {
            return "UMA";
        }

        @Override
        public int getPriority() {
            return priorityLevel;
        }

        @Override
        public CompletionStage<Credential> authenticate(final Session session, final Request request,
                final Set<String> algorithms) {
            final URI as = URI.create(challenge.getParameter(AS_URI));
            final String ticket = challenge.getParameter(TICKET);

            final Optional<Credential> credential = session.getCredential(ID_TOKEN);

            final ClaimToken claimToken = credential.map(cred -> ClaimToken.of(cred.getToken(), ID_TOKEN))
                .orElse(null);
            final URI principal = credential.flatMap(Credential::getPrincipal).orElse(null);
            final String jkt = credential.flatMap(Credential::getProofThumbprint).orElse(null);

            // TODO add Access Grant support

            final TokenRequest req = new TokenRequest(ticket, null, null, claimToken, Collections.emptyList());
            return umaClient.metadata(as)
                .thenCompose(metadata ->
                    umaClient.token(metadata.tokenEndpoint, req, claimHandler::getToken)
                        .thenApply(token -> new Credential(token.tokenType, as, token.accessToken,
                            Instant.now().plusSeconds(token.expiresIn), principal, jkt)));
        }
    }

    static String getAlgorithm(final List<String> serverSupported, final Set<String> clientSupported) {
        if (serverSupported != null) {
            return serverSupported.stream().filter(clientSupported::contains).findFirst().orElse(null);
        }
        return null;
    }

    static List<String> getScopes(final TokenResponse token) {
        if (token.scope != null) {
            return Arrays.asList(token.scope.split(" "));
        }
        return Collections.emptyList();
    }

    static class NeedInfoHandler {

        private final List<ClaimGatheringHandler> handlers = new ArrayList<>();

        public NeedInfoHandler(final ClaimGatheringHandler... handlers) {
            this.handlers.addAll(Arrays.asList(handlers));
        }

        public void addHandler(final ClaimGatheringHandler handler) {
            this.handlers.add(handler);
        }

        public CompletionStage<ClaimToken> getToken(final NeedInfo needInfo) {
            for (final RequiredClaims requiredClaims : needInfo.getRequiredClaims()) {
                for (final ClaimGatheringHandler handler : handlers) {
                    if (handler.isCompatibleWith(requiredClaims)) {
                        return handler.gather();
                    }
                }
            }
            return CompletableFuture.completedFuture(null);
        }
    }
}

