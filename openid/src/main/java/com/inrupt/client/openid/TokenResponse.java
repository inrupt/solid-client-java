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

    /**
     * Get the access_token value from the token response.
     *
     * @return the access token
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * Get the id_token value from the token response.
     *
     * @return the id token
     */
    public String getIdToken() {
        return idToken;
    }

    /**
     * Get the token_type value from the token response.
     *
     * @return the token type.
     */
    public String getTokenType() {
        return tokenType;
    }

    /**
     * Get the refresh_token value from the token response.
     *
     * @return the refresh token, may be {@code null}
     */
    public String getRefreshToken() {
        return refreshToken;
    }

    /**
     * Get the expires_id value from the token response.
     *
     * @return the number of seconds for which the token is valid
     */
    public int getExpiresIn() {
        return expiresIn;
    }

    /**
     * Create a new token response builder.
     *
     * @return the new builder
     */
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

        /**
         * Set the token_type in the token response.
         *
         * @param tokenType the token type
         * @return this builder
         */
        public Builder tokenType(final String tokenType) {
            builderTokenType = tokenType;
            return this;
        }

        /**
         * Set an access_token value in the token response.
         *
         * @param accessToken the access token
         * @return this builder
         */
        public Builder accessToken(final String accessToken) {
            builderAccessToken = accessToken;
            return this;
        }

        /**
         * Set an id_token value in the token response.
         *
         * @param idToken the id token
         * @return this builder
         */
        public Builder idToken(final String idToken) {
            builderIdToken = idToken;
            return this;
        }

        /**
         * Set a refresh_token value in the token response.
         *
         * @param refreshToken the refresh token
         * @return this builder
         */
        public Builder refreshToken(final String refreshToken) {
            builderRefreshToken = refreshToken;
            return this;
        }

        /**
         * Set an expires_in value in the token response.
         *
         * @param expiresIn the number of seconds in which the token expires
         * @return this builder
         */
        public Builder expiresIn(final int expiresIn) {
            builderExpiresIn = expiresIn;
            return this;
        }

        /**
         * Build the token response.
         *
         * @return the token response
         */
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

