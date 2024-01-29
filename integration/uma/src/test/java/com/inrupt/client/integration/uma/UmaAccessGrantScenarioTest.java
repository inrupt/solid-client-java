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
package com.inrupt.client.integration.uma;

import static org.junit.jupiter.api.Assertions.*;

import com.inrupt.client.Request;
import com.inrupt.client.accessgrant.AccessGrantClient;
import com.inrupt.client.integration.base.AccessGrantScenarios;

import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class UmaAccessGrantScenarioTest extends AccessGrantScenarios {

    private static final Logger LOGGER = LoggerFactory.getLogger(UmaAccessGrantScenarioTest.class);

    @Test
    void accessGrantUmaAuthentication() {
        LOGGER.info("Integration Test - UMA Authentication to VC endpoint");

        final AccessGrantClient requesterAccessGrantClient = new AccessGrantClient(
                URI.create(AccessGrantScenarios.ACCESS_GRANT_PROVIDER)
        ).session(requesterSession);

        // Make an authenticated request to the VC provider /issue endpoint to enforce a token is cached by the session.
        requesterAccessGrantClient.requestAccess(URI.create(webidUrl),
                        new HashSet<>(Arrays.asList(sharedTextFileURI)),
                        new HashSet<>(Arrays.asList(GRANT_MODE_READ)),
                        PURPOSES,
                        Instant.parse(GRANT_EXPIRATION))
                .toCompletableFuture().join();
        // Lookup the session cache.
        final Request dummyRequest = Request.newBuilder(
            URI.create(AccessGrantScenarios.ACCESS_GRANT_PROVIDER).resolve("/issue")
        ).POST(Request.BodyPublishers.ofString("Not relevant")).build();
        final var token = requesterSession.fromCache(dummyRequest);
        final var credential = requesterSession.getCredential(
                URI.create("http://openid.net/specs/openid-connect-core-1_0.html#IDToken"),
                null
        );
        // If UMA authentication was successful, the token issuer will be the UMA server, while the credential (i.e. the
        // ID Token) is issued by the OpenID provider.
        assertNotEquals(token.get().getIssuer(), credential.get().getIssuer());
    }
}
