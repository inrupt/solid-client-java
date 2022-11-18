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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A class representing an authorization request at an OpenID provider.
 */
public final class AuthorizationRequest {

    private final String responseType;
    private final List<String> scope;
    private final String codeChallenge;
    private final String codeChallengeMethod;
    private final String clientId;
    private final URI redirectUri;
    private final String state;
    private final String nonce;

    /**
     * Get the OAuth 2.0 response type.
     *
     * @return the response type
     */
    public String getResponseType() {
        return responseType;
    }

    /**
     * Get the OAuth 2.0 scope value.
     *
     * <p>The response will be a space-delimited string of scope values.
     *
     * @return the scope value
     */
    public String getScope() {
        return String.join(" ", scope);
    }

    /**
     * Get the PKCE code challenge.
     *
     * @return the code challenge value.
     */
    public String getCodeChallenge() {
        return codeChallenge;
    }

    /**
     * Get the PKCE code challenge method.
     *
     * @return the code challenge method
     */
    public String getCodeChallengeMethod() {
        return codeChallengeMethod;
    }

    /**
     * Get the OAuth 2.0 client id.
     *
     * @return the client id
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Get the OAuth 2.0 redirect URI.
     *
     * @return the redirect URI
     */
    public URI getRedirectUri() {
        return redirectUri;
    }

    /**
     * Get the OAuth 2.0 state value.
     *
     * @return the state value
     */
    public String getState() {
        return state;
    }

    /**
     * Get the OpenID Connect nonce value.
     *
     * @return the nonce value
     */
    public String getNonce() {
        return nonce;
    }

    /**
     * Create a new builder object for an authorization request.
     *
     * @return a new builder
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /* package-private */
    AuthorizationRequest(final String clientId, final URI redirectUri, final String responseType,
            final List<String> scope, final String codeChallenge, final String codeChallengeMethod,
            final String state, final String nonce) {
        this.clientId = clientId;
        this.redirectUri = redirectUri;
        this.responseType = responseType;
        this.scope = scope;
        this.codeChallenge = codeChallenge;
        this.codeChallengeMethod = codeChallengeMethod;
        this.state = state;
        this.nonce = nonce;
    }

    /**
     * A class for building {@link AuthorizationRequest} objects.
     */
    public static class Builder {

        private String builderResponseType = "code";
        private List<String> builderScope = new ArrayList<>();
        private String builderNonce;
        private String builderState;
        private String builderCodeChallenge;
        private String builderCodeChallengeMethod;

        /**
         * Add a scope value to the builder.
         *
         * @param scope the scope value
         * @return this builder
         */
        public Builder scope(final String scope) {
            builderScope.add(scope);
            return this;
        }

        /**
         * Add a state value to the builder.
         *
         * @param state the state value
         * @return this builder
         */
        public Builder state(final String state) {
            builderState = state;
            return this;
        }

        /**
         * Add a nonce value to the builder.
         *
         * @param nonce the nonce value
         * @return this builder
         */
        public Builder nonce(final String nonce) {
            builderNonce = nonce;
            return this;
        }

        /**
         * Add a response type to the builder.
         *
         * @param responseType the response type
         * @return this builder
         */
        public Builder responseType(final String responseType) {
            builderResponseType = responseType;
            return this;
        }

        /**
         * Add a code challenge to the builder.
         *
         * @param codeChallenge the code challenge
         * @return this builder
         */
        public Builder codeChallenge(final String codeChallenge) {
            builderCodeChallenge = codeChallenge;
            return this;
        }

        /**
         * Add a code challenge method to the builder.
         *
         * @param codeChallengeMethod the code challenge method
         * @return this builder
         */
        public Builder codeChallengeMethod(final String codeChallengeMethod) {
            builderCodeChallengeMethod = codeChallengeMethod;
            return this;
        }

        /**
         * Build the authorization request.
         *
         * @param clientId the client id
         * @param redirectUri the redirect URI
         * @return the authorization request
         */
        public AuthorizationRequest build(final String clientId, final URI redirectUri) {
            Objects.requireNonNull(redirectUri);
            Objects.requireNonNull(clientId);
            return new AuthorizationRequest(clientId, redirectUri, builderResponseType, builderScope,
                    builderCodeChallenge, builderCodeChallengeMethod, builderState, builderNonce);
        }
    }
}
