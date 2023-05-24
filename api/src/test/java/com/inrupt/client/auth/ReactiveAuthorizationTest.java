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

import static org.junit.jupiter.api.Assertions.*;

import com.inrupt.client.Request;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.junit.jupiter.api.Test;

class ReactiveAuthorizationTest {

    @Test
    void testProhibitedAuth() {
        final ReactiveAuthorization auth = new ReactiveAuthorization();
        final Session session = new BasicAuthSession();
        final Request req = Request.newBuilder(URI.create("https://storage.example")).build();
        final Optional<Credential> credential = auth.negotiate(session, req,
                Collections.singleton(Challenge.of("Basic"))).toCompletableFuture().join();
        assertFalse(credential.isPresent());
    }

    static class BasicAuthSession implements Session {

        @Override
        public String getId() {
            return UUID.randomUUID().toString();
        }

        @Override
        public Optional<URI> getPrincipal() {
            return Optional.empty();
        }

        @Override
        public Set<String> supportedSchemes() {
            return Collections.singleton("Basic");
        }

        @Override
        public Optional<Credential> getCredential(final URI name, final URI uri) {
            return Optional.empty();
        }

        @Override
        public Optional<Credential> fromCache(final Request request) {
            return Optional.empty();
        }

        @Override
        public Optional<String> generateProof(final String jkt, final Request request) {
            return Optional.empty();
        }

        @Override
        public Optional<String> selectThumbprint(final Collection<String> algorithms) {
            return Optional.empty();
        }

        @Override
        public CompletionStage<Optional<Credential>> authenticate(final Authenticator authenticator,
                final Request request, final Set<String> algorithms) {
            return authenticator.authenticate(this, request, algorithms).thenApply(Optional::ofNullable);
        }

        /* deprecated */
        @Override
        public CompletionStage<Optional<Credential>> authenticate(final Request request,
                final Set<String> algorithms) {
            return CompletableFuture.completedFuture(Optional.empty());
        }
    }

}
