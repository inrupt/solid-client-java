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
package com.inrupt.client.auth;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.inrupt.client.Request;
import com.inrupt.client.spi.AuthenticationProvider;

import java.net.URI;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class BasicAuthProvider implements AuthenticationProvider {

    private static final String BASIC = "Basic";

    @Override
    public String getScheme() {
        return BASIC;
    }

    @Override
    public Set<String> getSchemes() {
        return Collections.singleton(BASIC);
    }

    @Override
    public Authenticator getAuthenticator(final Challenge challenge) {
        return new BasicAuthenticator();
    }

    public class BasicAuthenticator implements Authenticator {
        @Override
        public String getName() {
            return "BasicAuth";
        }

        @Override
        public int getPriority() {
            return 100;
        }

        @Override
        public CompletionStage<Credential> authenticate(final Session session, final Request request,
                final Set<String> algorithms) {
            final URI issuer = URI.create("https://issuer.test");
            final URI agent = URI.create("https://id.test/username");
            final Instant expiration = Instant.now().plusSeconds(300);
            final String token = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("username:password".getBytes(UTF_8));
            return CompletableFuture.completedFuture(new Credential("Basic", issuer, token, expiration, agent, null));
        }
    }
}
