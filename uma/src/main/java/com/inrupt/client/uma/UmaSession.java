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
package com.inrupt.client.uma;

import com.inrupt.client.Request;
import com.inrupt.client.Session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * A session implementation for use with UMA Authorization Servers.
 *
 * <p>This session implementation can be used to wrap other session objects, such as
 * ones that use OpenID Connect tokens.
 *
 * <pre>{@code
 *   Client client = ClientProvider.getClient();
 *   Session session = client.session(UmaSession.ofSession(OpenIdSession.ofIdToken(jwt)));
 *   Response res = session.send(req, bodyHandler);
 * }</pre>
 */
public final class UmaSession implements Session {

    private final String id;
    private final Set<String> schemes;
    private final List<Session> internalSessions = new ArrayList<>();

    private UmaSession(final Session... sessions) {
        this.id = UUID.randomUUID().toString();
        // TODO use a better data structure
        final Set<String> schemeTypes = new HashSet<>();
        for (final Session session : sessions) {
            this.internalSessions.add(session);
            schemeTypes.addAll(session.supportedSchemes());
        }
        schemeTypes.add("UMA");
        this.schemes = Collections.unmodifiableSet(schemeTypes);
    }

    /**
     * Create a session by wrapping other sessions.
     *
     * @param sessions the wrapped sessions
     * @return the session
     */
    public static Session ofSessions(final Session... sessions) {
        return new UmaSession(sessions);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Set<String> supportedSchemes() {
        return schemes;
    }

    @Override
    public Optional<Session.Credential> getCredential(final String name) {
        for (final Session session : internalSessions) {
            final Optional<Credential> credential = session.getCredential(name);
            if (credential.isPresent()) {
                return credential;
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Session.Credential> fromCache(final Request request) {
        return Optional.empty();
    }
}
