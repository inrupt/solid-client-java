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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * A class for holding registered authentication mechanisms.
 */
public class SolidAuthenticator {

    static final Comparator<SolidAuthenticationMechanism.Authenticator> comparator = Comparator
        .comparing(SolidAuthenticationMechanism.Authenticator::priority)
        .reversed();

    private final Map<String, SolidAuthenticationMechanism> registry = new HashMap<>();
    private final HeaderParser parser;
    private final DPoP dpop;

    /**
     * Create a {@link SolidAuthenticator}.
     */
    public SolidAuthenticator() {
        this(new DPoP());
    }

    /**
     * Create a {@link SolidAuthenticator} with an externally defined DPoP manager.
     *
     * @param dpop a DPoP manager
     */
    public SolidAuthenticator(final DPoP dpop) {
        this(dpop, new DefaultHeaderParser());
    }

    /**
     * Create a {@link SolidAuthenticator} with an externally defined DPoP manager and a custom header parser.
     *
     * @param dpop a DPoP manager
     * @param parser a header parser
     */
    public SolidAuthenticator(final DPoP dpop, final HeaderParser parser) {
        this.parser = Objects.requireNonNull(parser);
        this.dpop = Objects.requireNonNull(dpop);
    }

    /**
     * Generate a DPoP proof for an HTTP request.
     *
     * @param algorithm the algorithm to use
     * @param uri the HTTP URI
     * @param method the HTTP method
     * @return the DPoP proof
     */
    public String generateProof(final String algorithm, final URI uri, final String method) {
        return dpop.generateProof(algorithm, uri, method);
    }

    /**
     * Register an authentication mechansim.
     *
     * @param authMechanism the authentication mechanism
     */
    public void register(final SolidAuthenticationMechanism authMechanism) {
        registry.put(authMechanism.getScheme().toLowerCase(Locale.ENGLISH), authMechanism);
    }

    /**
     * Parse a WWW-Authenticate header and convert the challenges into callable authentication mechanisms.
     *
     * @param headers the WWW-Authenticate headers to be evaluated
     * @return a sorted list of viable authentication mechanisms
     */
    public List<SolidAuthenticationMechanism.Authenticator> challenge(final String... headers) {
        return challenge(List.of(headers));
    }

    /**
     * Parse a collection of WWW-Authenticate headers and convert the challenges into callable
     * authentication mechanisms.
     *
     * @param headers the WWW-Authenticate headers to be evaluated
     * @return a sorted list of viable authentication mechanisms
     */
    public List<SolidAuthenticationMechanism.Authenticator> challenge(final Collection<String> headers) {

        final var mechanisms = new ArrayList<SolidAuthenticationMechanism.Authenticator>();

        for (final var header : headers) {
            final var challenges = parser.wwwAuthenticate(header);
            for (final var challenge : challenges) {
                final var scheme = challenge.getScheme().toLowerCase(Locale.ENGLISH);
                if (registry.containsKey(scheme)) {
                    mechanisms.add(registry.get(scheme).getAuthenticator(challenge));
                }
            }
        }

        mechanisms.sort(comparator);
        return mechanisms;
    }
}

