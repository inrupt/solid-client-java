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

import com.inrupt.client.Authenticator;
import com.inrupt.client.Credential;
import com.inrupt.client.Request;
import com.inrupt.client.Session;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class for negotiating for a supported {@link AuthenticationProvider} based on the {@code WWW-Authenticate}
 * headers received from a resource server.
 */
public class ReactiveAuthorization {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReactiveAuthorization.class);

    private static final Comparator<Authenticator> comparator = Comparator
        .comparing(Authenticator::getPriority)
        .reversed();

    private final Map<String, AuthenticationProvider> registry = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);


    /**
     * Create a new authorization handler, loading any {@link AuthenticationProvider} implementations
     * via the {@link ServiceLoader}.
     */
    public ReactiveAuthorization() {
        final ServiceLoader<AuthenticationProvider> loader = ServiceLoader.load(AuthenticationProvider.class,
                ReactiveAuthorization.class.getClassLoader());

        for (final AuthenticationProvider provider : loader) {
            registry.put(provider.getScheme(), provider);
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
            final Collection<Authenticator.Challenge> challenges) {
        final List<Authenticator> authenticators = new ArrayList<>();
        final Set<String> algorithms = new HashSet<>();
        for (final Authenticator.Challenge challenge : challenges) {
            if (challenge.getParameter("algs") != null) {
                for (final String alg : challenge.getParameter("algs").split(" ")) {
                    algorithms.add(alg);
                }
            }
            final String scheme = challenge.getScheme();
            if (registry.containsKey(scheme) && sessionSupportsScheme(session, scheme)) {
                authenticators.add(registry.get(scheme).getAuthenticator(challenge));
            }
        }

        if (!authenticators.isEmpty()) {
            // Use the first authenticator, sorted by priority
            authenticators.sort(comparator);
            final Authenticator auth = authenticators.get(0);
            LOGGER.debug("Using {} authenticator", auth.getName());
            return auth.authenticate(session, request, algorithms).thenApply(Optional::of);
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
}

