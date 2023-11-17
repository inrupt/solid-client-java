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
package com.inrupt.client.quarkus;

import com.inrupt.client.auth.Session;
import com.inrupt.client.openid.OpenIdSession;

import java.util.Optional;
import java.util.function.Function;

import org.eclipse.microprofile.jwt.JsonWebToken;

public final class SessionUtils {

    /**
     * Convert a Quarkus (Microprofile) {@link JsonWebToken} to a {@link Session} object.
     *
     * <p>This method uses the {@link OpenIdSession} library to create a Session
     *
     * @param jwt a JSON Web Token object
     * @return the session, if present and unexpired
     */
    public static Optional<Session> asSession(final JsonWebToken jwt) {
        return asSession(jwt, OpenIdSession::ofIdToken);
    }

    /**
     * Convert a Quarkus (Microprofile) {@link JsonWebToken} to a {@link Session} object.
     *
     * @param jwt a JSON Web Token object
     * @param mapping a mapping function for creating a Session from an ID token
     * @return the session, if present and unexpired
     */
    public static Optional<Session> asSession(final JsonWebToken jwt, final Function<String, Session> mapping) {
        return Optional.ofNullable(mapping.apply(jwt.getRawToken()));
    }

    private SessionUtils() {
        // Prevent instantiation
    }
}

