/*
 * Copyright 2022 Inrupt Inc.
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

import java.util.Objects;

/**
 * A class representing successful responses from a token endpoint.
 */
public final class TokenResponse {

    private String accessToken;
    private String idToken;
    private String tokenType;
    private String refreshToken;
    private int expiresIn;

    public String getAccessToken() {
        return accessToken;
    }

    public String getIdToken() {
        return idToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    private TokenResponse() {
        // Prevent external instantiation
    }

    /**
     * A builder class for {@link TokenResponse} objects.
     */
    public static class Builder {

        private String builderTokenType = "Bearer";
        private String builderAccessToken;
        private String builderIdToken;
        private String builderRefreshToken;
        private int builderExpiresIn;

        public Builder tokenType(final String tokenType) {
            builderTokenType = tokenType;
            return this;
        }

        public Builder accessToken(final String accessToken) {
            builderAccessToken = accessToken;
            return this;
        }

        public Builder idToken(final String idToken) {
            builderIdToken = idToken;
            return this;
        }

        public Builder refreshToken(final String refreshToken) {
            builderRefreshToken = refreshToken;
            return this;
        }

        public Builder expiresIn(final int expiresIn) {
            builderExpiresIn = expiresIn;
            return this;
        }

        public TokenResponse build() {
            final var res = new TokenResponse();
            res.tokenType = Objects.requireNonNull(builderTokenType);
            res.accessToken = Objects.requireNonNull(builderAccessToken);
            res.idToken = Objects.requireNonNull(builderIdToken);
            res.refreshToken = builderRefreshToken;
            res.expiresIn = builderExpiresIn;
            return res;
        }
    }
}

