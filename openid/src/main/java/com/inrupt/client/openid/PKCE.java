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
package com.inrupt.client.openid;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;

/**
 * A class for generating values for Proof Key for Code Exchange (PKCE) interactions.
 *
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc7636">Proof Key for Code Exchange</a>
 */
public final class PKCE {

    /**
     * Create a PKCE challenge value using the S256 algorithm.
     *
     * <p>Note: the {@code none} algorithm is not supported by this library.
     *
     * @param verifier the PKCE verifier, may not be {@code null}
     * @return the Base64URL-encoded challenge value
     */
    static String createChallenge(final String verifier) {
        return createChallenge(verifier, "SHA-256");
    }

    /**
     * Create a PKCE challenge value using a given algorithm.
     *
     * <p>Note: the {@code none} algorithm is not supported by this library.
     *
     * @param verifier the PKCE verifier, may not be {@code null}
     * @param alg the algorithm used to encode challenge value
     * @return the Base64URL-encoded challenge value
     */
    static String createChallenge(final String verifier, final String alg) {
        Objects.requireNonNull(verifier, "PKCE Verifier cannot be null");
        try {
            final MessageDigest digest = MessageDigest.getInstance(alg);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest.digest(verifier.getBytes(UTF_8)));
        } catch (final NoSuchAlgorithmException ex) {
            throw new OpenIdException("Error generating PKCE challenge", ex);
        }
    }

    /**
     * Create a PKCE verifier.
     *
     * @return the Base64URL-encoded verifier
     */
    static String createVerifier() {
        final byte[] rand = new BigInteger(32 * 8, new SecureRandom()).toByteArray();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(rand);
    }

    private PKCE() {
        // Prevent instantiation
    }
}


