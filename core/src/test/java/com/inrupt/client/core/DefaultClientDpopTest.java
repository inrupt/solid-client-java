/*
 * Copyright Inrupt Inc.
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

import static org.jose4j.jwa.AlgorithmConstraints.ConstraintType.PERMIT;
import static org.junit.jupiter.api.Assertions.*;

import com.inrupt.client.Request;
import com.inrupt.client.auth.Authenticator;
import com.inrupt.client.auth.Credential;
import com.inrupt.client.auth.DPoP;
import com.inrupt.client.auth.Session;
import com.inrupt.client.spi.ServiceProvider;

import java.net.URI;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.resolvers.EmbeddedJwkVerificationKeyResolver;
import org.junit.jupiter.api.Test;

class DefaultClientDpopTest {

    // Example from RFC 9449, section 7.1
    static final String ACCESS_TOKEN = "Kz~8mXK1EalYznwH-LC-1fBAo.4Ljp~zsPE_NeO.gxU";
    static final String ATH = "fUHyO2r2Z3DZ53EsNrWBb0xWXoaNy59IiKCAqksmQEo";

    @Test
    void testUpgradeRequestWithDpopBoundToken() throws Exception {
        final DPoP dpop = DPoP.of();
        final String jkt = dpop.lookupThumbprint("ES256").orElseThrow();
        final DefaultClient client = new DefaultClient(ServiceProvider.getHttpService(), new DpopSession(dpop));
        final Credential credential = new Credential("DPoP", URI.create("https://issuer.example"),
                ACCESS_TOKEN, Instant.now().plusSeconds(300), null, jkt);
        final Request request = Request.newBuilder(URI.create("https://storage.example/resource?q=1#frag"))
            .header("DPoP", "stale-proof")
            .GET().build();

        final Request upgraded = client.upgradeRequest(request, credential);

        assertEquals(Collections.singletonList("DPoP " + ACCESS_TOKEN),
                upgraded.headers().allValues("Authorization"));
        assertEquals(1, upgraded.headers().allValues("DPoP").size());
        final String proof = upgraded.headers().firstValue("DPoP").orElseThrow();
        final JwtClaims claims = new JwtConsumerBuilder()
            .setVerificationKeyResolver(new EmbeddedJwkVerificationKeyResolver())
            .setJwsAlgorithmConstraints(PERMIT, "ES256")
            .setExpectedType(true, "dpop+jwt")
            .setRequireJwtId()
            .setRequireIssuedAt()
            .build().processToClaims(proof);
        assertEquals("GET", claims.getStringClaimValue("htm"));
        assertEquals("https://storage.example/resource", claims.getStringClaimValue("htu"));
        assertEquals(ATH, claims.getStringClaimValue("ath"));
    }

    static class DpopSession implements Session {
        private final DPoP dpop;
        private final String id = UUID.randomUUID().toString();

        DpopSession(final DPoP dpop) {
            this.dpop = dpop;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public Optional<URI> getPrincipal() {
            return Optional.empty();
        }

        @Override
        public Set<String> supportedSchemes() {
            return Collections.singleton("DPoP");
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
            return generateProof(jkt, request, null);
        }

        @Override
        public Optional<String> generateProof(final String jkt, final Request request, final String accessToken) {
            return dpop.lookupAlgorithm(jkt)
                .map(alg -> dpop.generateProof(alg, request.uri(), request.method(), accessToken));
        }

        @Override
        public Optional<String> selectThumbprint(final Collection<String> algorithms) {
            return Optional.empty();
        }

        @Override
        public void reset() {
            // no-op
        }

        @Override
        public CompletionStage<Optional<Credential>> authenticate(final Authenticator authenticator,
                final Request request, final Set<String> algorithms) {
            return CompletableFuture.completedFuture(Optional.empty());
        }
    }
}
