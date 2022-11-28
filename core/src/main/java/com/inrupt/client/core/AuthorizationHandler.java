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
package com.inrupt.client.core;

import com.inrupt.client.Authenticator;
import com.inrupt.client.Headers.WwwAuthenticate;
import com.inrupt.client.Request;
import com.inrupt.client.Session;
import com.inrupt.client.spi.AuthenticationProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AuthorizationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationHandler.class);
    private static final Comparator<Authenticator> comparator = Comparator
        .comparing(Authenticator::getPriority)
        .reversed();

    private final Map<String, AuthenticationProvider> registry = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);


    public AuthorizationHandler() {
        final ServiceLoader<AuthenticationProvider> loader = ServiceLoader.load(AuthenticationProvider.class,
                AuthorizationHandler.class.getClassLoader());

        for (final AuthenticationProvider provider : loader) {
            registry.put(provider.getScheme(), provider);
        }
    }

    public CompletionStage<Optional<Session.Credential>> negotiate(final Session session, final Request request,
            final Collection<String> headers) {
        final List<Authenticator> authenticators = new ArrayList<>();
        for (final String header : headers) {
            final WwwAuthenticate wwwAuthenticate = WwwAuthenticate.parse(header);
            for (final Authenticator.Challenge challenge : wwwAuthenticate.getChallenges()) {
                final String scheme = challenge.getScheme();
                if (registry.containsKey(scheme) && sessionSupportsScheme(session, scheme)) {
                    authenticators.add(registry.get(scheme).getAuthenticator(challenge));
                }
            }
        }

        if (!authenticators.isEmpty()) {
            // Use the first authenticator, sorted by priority
            authenticators.sort(comparator);
            final Authenticator auth = authenticators.get(0);
            LOGGER.debug("Using {} authenticator", auth.getName());
            return auth.authenticateAsync(session, request)
                .thenApply(token -> new Session.Credential(token.getType(), token.getIssuer(),
                            token.getToken(), token.getExpiration()))
                .thenApply(Optional::of);
        }
        return CompletableFuture.completedFuture(Optional.empty());
    }

    static boolean sessionSupportsScheme(final Session session, final String scheme) {
        // special case for UMA, since anonymous sessions are possible here, too
        if ("UMA".equals(scheme)) {
            return true;
        }
        return session.supportedSchemes().contains(scheme);
    }
}

