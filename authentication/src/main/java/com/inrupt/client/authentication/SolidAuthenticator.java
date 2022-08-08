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

import java.util.ArrayList;
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

    /**
     * Create a {@link SolidAuthenticator} with the default header parser.
     */
    public SolidAuthenticator() {
        this(new DefaultHeaderParser());
    }

    /**
     * Create a {@link SolidAuthenticator} with a custom header parser.
     *
     * @param parser a header parser
     */
    public SolidAuthenticator(final HeaderParser parser) {
        this.parser = Objects.requireNonNull(parser);
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
     * @param header the WWW-Authenticate header
     * @return a sorted list of viable authentication mechanisms
     */
    public List<SolidAuthenticationMechanism.Authenticator> challenge(final String header) {
        final var challenges = parser.wwwAuthenticate(header);

        final var mechanisms = new ArrayList<SolidAuthenticationMechanism.Authenticator>();
        for (final var challenge : challenges) {
            final var scheme = challenge.getScheme().toLowerCase(Locale.ENGLISH);
            if (registry.containsKey(scheme)) {
                mechanisms.add(registry.get(scheme).getAuthenticator(challenge));
            }
        }

        mechanisms.sort(comparator);
        return mechanisms;
    }
}

