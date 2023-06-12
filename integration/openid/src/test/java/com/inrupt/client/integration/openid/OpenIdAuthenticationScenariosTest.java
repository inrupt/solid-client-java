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
package com.inrupt.client.integration.openid;

import static org.junit.jupiter.api.Assertions.*;

import com.inrupt.client.Request;
import com.inrupt.client.auth.Session;
import com.inrupt.client.integration.base.AuthenticationScenarios;
import com.inrupt.client.solid.SolidRDFSource;
import com.inrupt.client.solid.SolidSyncClient;

import java.net.URI;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class OpenIdAuthenticationScenariosTest extends AuthenticationScenarios {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenIdAuthenticationScenariosTest.class);

    @ParameterizedTest
    @MethodSource("provideSessions")
    void fetchPrivateResourceAuthenticatedTest(final Session session) {
        LOGGER.info("Integration Test - Authenticated fetch of private resource uses OpenID authenticator");
        //create private resource
        final SolidSyncClient authClient = SolidSyncClient.getClient().session(session);
        try (final SolidRDFSource testResource = new SolidRDFSource(privateResourceURL, null, null)) {
            assertDoesNotThrow(() -> authClient.create(testResource));

            assertDoesNotThrow(() -> authClient.read(privateResourceURL, SolidRDFSource.class));

            // Lookup the session cache.
            final Request dummyRequest = Request.newBuilder(privateResourceURL).build();
            final var token = session.fromCache(dummyRequest);
            final var credential = session.getCredential(
                    URI.create("http://openid.net/specs/openid-connect-core-1_0.html#IDToken"),
                    null
            );
            // If OpenID authentication was successful, both the token and the credential (i.e. the ID Token) are issued
            // by the OpenID provider.
            assertEquals(token.get().getIssuer(), credential.get().getIssuer());

            assertDoesNotThrow(() -> authClient.delete(testResource));
        }
    }
}
