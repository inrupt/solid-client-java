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

import com.inrupt.client.auth.DPoP;

import java.net.URI;
import java.util.Optional;

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.jwt.consumer.Validator;
import org.jose4j.keys.resolvers.EmbeddedJwkVerificationKeyResolver;
import org.junit.jupiter.api.Test;

class DPoPManagerTest {

    static final String[] algorithms = new String[] {"ES256", "RS256"};

    @Test
    void testDefaultDpop() {
        final DPoP dpop = DPoP.of();
        assertTrue(dpop.algorithms().contains("ES256"));
    }

    @Test
    void testGenerateProof() {
        final DPoP dpop = DPoP.of();
        final String method = "GET";
        final URI uri = URI.create("https://storage.example/resource");
        final String proof = dpop.generateProof("ES256", uri, method);

        assertDoesNotThrow(() -> verifyDpop(proof, uri, method));
    }

    @Test
    void testInvalidAlgorithm() {
        final DPoP dpop = DPoP.of();
        final String method = "GET";
        final URI uri = URI.create("https://storage.example/resource");
        assertThrows(AuthenticationException.class, () -> dpop.generateProof("RS256", uri, method));
    }

    @Test
    void testLookupAlgorithm() {
        final DPoP dpop = DPoP.of();
        assertEquals(Optional.of("ES256"), dpop.lookupThumbprint("ES256").flatMap(dpop::lookupAlgorithm));
        assertFalse(dpop.lookupThumbprint("RS256").isPresent());
        assertFalse(dpop.lookupThumbprint(null).isPresent());
        assertFalse(dpop.lookupAlgorithm("not-a-thumbprint").isPresent());
        assertFalse(dpop.lookupAlgorithm(null).isPresent());
    }

    static void verifyDpop(final String proof, final URI uri, final String method) {
        try {
            new JwtConsumerBuilder()
                .setRequireJwtId()
                .setExpectedType(true, "dpop+jwt")
                .setJwsAlgorithmConstraints(PERMIT, algorithms)
                .setVerificationKeyResolver(new EmbeddedJwkVerificationKeyResolver())
                .setRequireIssuedAt()
                .setExpectedIssuer(false, null)
                .registerValidator(htuValidator(uri.toString()))
                .registerValidator(htmValidator(method))
                .build()
                .process(proof);
        } catch (final InvalidJwtException ex) {
            throw new AuthenticationException("Invalid DPoP proof: " + ex.getMessage());
        }
    }

    static Validator htuValidator(final String url) {
        return ctx -> {
            final JwtClaims claims = ctx.getJwtClaims();
            if (!claims.hasClaim("htu")) {
                return "Missing required htu claim in DPoP token";
            }
            if (!url.equalsIgnoreCase(claims.getClaimValueAsString("htu"))) {
                return "Incorrect htu claim";
            }
            return null;
        };
    }

    static Validator htmValidator(final String method) {
        return ctx -> {
            final JwtClaims claims = ctx.getJwtClaims();
            if (!claims.hasClaim("htm")) {
                return "Missing required htm claim in DPoP token";
            }

            if (!method.equalsIgnoreCase(claims.getClaimValueAsString("htm"))) {
                return "Incorrect htm claim";
            }
            return null;
        };
    }
}
