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
package com.inrupt.client.uma;

import java.util.Objects;

/**
 * This class represents an UMA claim token and the associated type value.
 *
 * @see <a href="https://docs.kantarainitiative.org/uma/wg/rec-oauth-uma-grant-2.0.html#uma-grant-type">
 * User-Managed Access 2.0: Client Request to Authorization Server for RPT</a>
 */
public final class ClaimToken {

    private final String token;
    private final String tokenType;

    private ClaimToken(final String token, final String tokenType) {
        this.token = Objects.requireNonNull(token);
        this.tokenType = Objects.requireNonNull(tokenType);
    }

    /**
     * Get the claim token value.
     *
     * @return the claim token value
     */
    public String getClaimToken() {
        return token;
    }

    /**
     * Get the claim token type.
     *
     * @return the claim token type
     */
    public String getClaimTokenType() {
        return tokenType;
    }

    /**
     * Create a new {@link ClaimToken}.
     *
     * @param token the claim token value
     * @param tokenType the claim token type
     * @return the new claim token
     */
    public static ClaimToken of(final String token, final String tokenType) {
        return new ClaimToken(token, tokenType);
    }
}
