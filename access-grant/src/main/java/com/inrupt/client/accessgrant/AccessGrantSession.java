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
package com.inrupt.client.accessgrant;

import com.inrupt.client.Request;
import com.inrupt.client.auth.Credential;
import com.inrupt.client.auth.Session;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * A session implementation that makes use of Access Grants.
 */
public final class AccessGrantSession implements Session {

    /**
     * The VerifiableCredential format URI, for use with UMA.
     */
    public static final URI VERIFIABLE_CREDENTIAL = URI.create("https://www.w3.org/TR/vc-data-model/#json-ld");

    private final String id;
    private final Session session;
    private final NavigableMap<URI, AccessGrant> grants = new ConcurrentSkipListMap<>();

    private AccessGrantSession(final Session session, final List<AccessGrant> grants) {
        this.id = UUID.randomUUID().toString();
        this.session = session;

        for (final AccessGrant grant : grants) {
            for (final URI uri : grant.getResources()) {
                this.grants.put(uri, grant);
            }
        }
    }

    /**
     * Create a session with a collection of known access grants.
     *
     * @param session the OpenID Session
     * @param accessGrants the access grants
     * @return the Access Grant-based session
     */
    public static AccessGrantSession ofAccessGrant(final Session session, final AccessGrant... accessGrants) {
        return new AccessGrantSession(session, Arrays.asList(accessGrants));
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Optional<URI> getPrincipal() {
        return session.getPrincipal();
    }

    @Override
    public Set<String> supportedSchemes() {
        return session.supportedSchemes();
    }

    @Override
    public Optional<Credential> getCredential(final URI name, final URI uri) {
        if (VERIFIABLE_CREDENTIAL.equals(name)) {
            final NavigableMap<URI, AccessGrant> descending = grants.headMap(uri, true).descendingMap();
            for (final Map.Entry<URI, AccessGrant> entry : descending.entrySet()) {
                if (isAncestor(entry.getKey(), uri)) {
                    final AccessGrant grant = entry.getValue();
                    return Optional.of(new Credential("", grant.getIssuer(), grant.getRawGrant(),
                                grant.getExpiration(), session.getPrincipal().orElse(null), null));
                }
            }
        }
        return session.getCredential(name, uri);
    }

    @Override
    public Optional<String> selectThumbprint(final Collection<String> algorithms) {
        return session.selectThumbprint(algorithms);
    }

    @Override
    public Optional<String> generateProof(final String jkt, final Request request) {
        return session.generateProof(jkt, request);
    }

    @Override
    public CompletionStage<Optional<Credential>> authenticate(final Request request,
            final Set<String> algorithms) {
        final Optional<Credential> grant = getCredential(VERIFIABLE_CREDENTIAL, request.uri());
        if (grant.isPresent()) {
            return CompletableFuture.completedFuture(grant);
        }
        return session.authenticate(request, algorithms);
    }

    @Override
    public Optional<Credential> fromCache(final Request request) {
        // TODO add cache
        return Optional.empty();
    }

    static boolean isAncestor(final URI parent, final URI resource) {
        return !parent.relativize(resource).isAbsolute();
    }
}
