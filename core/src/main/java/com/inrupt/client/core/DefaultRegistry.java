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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class DefaultRegistry implements Authenticator.Registry {

    static final Comparator<Authenticator> comparator = Comparator
        .comparing(Authenticator::getPriority)
        .reversed();

    private final Map<String, Authenticator.Provider> registry = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public DefaultRegistry() {
        registry.put("UMA", new UmaAuthenticationProvider(100));
    }

    @Override
    public List<Authenticator> challenge(final Collection<String> headers) {
        final List<Authenticator> authenticators = new ArrayList<>();
        for (final String header : headers) {
            final WwwAuthenticate wwwAuthenticate = WwwAuthenticate.parse(header);
            for (final Authenticator.Challenge challenge : wwwAuthenticate.getChallenges()) {
                final String scheme = challenge.getScheme();
                if (registry.containsKey(scheme)) {
                    authenticators.add(registry.get(scheme).getAuthenticator(challenge));
                }
            }
        }

        authenticators.sort(comparator);
        return authenticators;
    }
}
