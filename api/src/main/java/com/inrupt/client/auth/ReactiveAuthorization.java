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
package com.inrupt.client.auth;

import com.inrupt.client.Request;
import com.inrupt.client.spi.AuthenticationProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class for negotiating for a supported {@link AuthenticationProvider} based on the {@code WWW-Authenticate}
 * headers received from a resource server.
 *
 * <p>In general, any authorization mechanism loaded via the {@link ServiceLoader} will be available for use
 * during the challenge-response negotiation with a server. There are, however, certain known weak mechanisms
 * such as Basic auth and Digest auth that are explicitly excluded.
 */
public class ReactiveAuthorization {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReactiveAuthorization.class);
    private static final String BEARER = "Bearer";

    private static final Comparator<Authenticator> comparator = Comparator
        .comparing(Authenticator::getPriority)
        .reversed();

    private final Map<String, AuthenticationProvider> registry = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);


    /**
     * Create a new authorization handler, loading any {@link AuthenticationProvider} implementations
     * via the {@link ServiceLoader}.
     *
     * <p>Known weak authorization mechanisms such as {@code Basic} and {@code Digest} are explicitly omitted.
     */
    public ReactiveAuthorization() {
        final ServiceLoader<AuthenticationProvider> loader = ServiceLoader.load(AuthenticationProvider.class,
                ReactiveAuthorization.class.getClassLoader());

        final Set<String> prohibited = getProhibitedSchemes();
        for (final AuthenticationProvider provider : loader) {
            for (final String scheme : provider.getSchemes()) {
                if (!prohibited.contains(scheme)) {
                    LOGGER.debug("Registering {} scheme via {} authentication provider", scheme,
                            provider.getClass().getSimpleName());
                    registry.put(scheme, provider);
                } else {
                    LOGGER.debug("Omitting {} scheme via {} authentication provider", scheme,
                            provider.getClass().getSimpleName());
                }
            }
        }
    }

    /**
     * Negotiate for an authorization credential.
     *
     * @param session the agent session
     * @param request the HTTP request
     * @param challenges the HTTP challenge schemes
     * @return the next stage of completion, possibly containing a credential
     */
    public CompletionStage<Optional<Credential>> negotiate(final Session session, final Request request,
            final Collection<Challenge> challenges) {
        final List<Authenticator> authenticators = new ArrayList<>();
        final Set<String> algorithms = new HashSet<>();
        for (final Challenge challenge : challenges) {
            if (challenge.getParameter("algs") != null) {
                Collections.addAll(algorithms, challenge.getParameter("algs").split(" "));
            }
            final String scheme = challenge.getScheme();
            if (registry.containsKey(scheme) && sessionSupportsScheme(session, scheme)) {
                authenticators.add(registry.get(scheme).getAuthenticator(challenge));
            }
        }

        if (authenticators.isEmpty()) {
            // Fallback in case of missing or poorly formed www-authenticate header
            if (registry.containsKey(BEARER)) {
                final Authenticator auth = registry.get(BEARER).getAuthenticator(Challenge.of(BEARER));
                LOGGER.debug("Using fallback Bearer authenticator");
                return session.authenticate(auth, request, algorithms);
            }
        } else {
            // Use the first authenticator, sorted by priority
            authenticators.sort(comparator);
            final Authenticator auth = authenticators.get(0);
            LOGGER.debug("Using {} authenticator", auth.getName());
            return session.authenticate(auth, request, algorithms);
        }
        return CompletableFuture.completedFuture(Optional.empty());
    }

    static boolean sessionSupportsScheme(final Session session, final String scheme) {
        // special case for UMA, since anonymous sessions are possible with UMA
        if ("UMA".equalsIgnoreCase(scheme)) {
            return true;
        }
        return session.supportedSchemes().contains(scheme);
    }

    static Set<String> getProhibitedSchemes() {
        final Set<String> prohibited = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        prohibited.add("Basic");
        prohibited.add("Digest");
        return prohibited;
    }
}

