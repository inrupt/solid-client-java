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

/**
 * A class for initiating a session termination flow with an OpenID Provider.
 */
public final class EndSessionRequest {

    private final String clientId;
    private final URI postLogoutRedirectUri;
    private final String state;
    private final String idTokenHint;

    /**
     * Return the {@code client_id} value.
     *
     * @return the {@code client_id}, may be {@code null}
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Return the {@code post_logout_redirect_uri} value.
     *
     * @return the {@code post_logout_redirect_uri} value, may be {@code null}
     */
    public URI getPostLogoutRedirectUri() {
        return postLogoutRedirectUri;
    }

    /**
     * Return the {@code state} value.
     *
     * @return the {@code state}, may be {@code null}
     */
    public String getState() {
        return state;
    }

    /**
     * Return the {@code id_token_hint} value.
     *
     * @return the {@code id_token_hint}, may be {@code null}
     */
    public String getIdTokenHint() {
        return idTokenHint;
    }

    private EndSessionRequest(final String clientId, final URI postLogoutRedirectUri, final String idTokenHint,
            final String state) {
        this.clientId = clientId;
        this.postLogoutRedirectUri = postLogoutRedirectUri;
        this.idTokenHint = idTokenHint;
        this.state = state;
    }

    /**
     * A builder class for {@link EndSessionRequest} objects.
     */
    public static final class Builder {
        private String builderClientId;
        private URI builderPostLogoutRedirectUri;
        private String builderState;
        private String builderIdTokenHint;

        /**
         * Create a new builder for end session requests.
         *
         * @return the new builder
         */
        public static Builder newBuilder() {
            return new Builder();
        }

        /**
         * Set a client id for the builder.
         *
         * @param clientId the client id
         * @return this builder
         */
        public Builder clientId(final String clientId) {
            builderClientId = clientId;
            return this;
        }

        /**
         * Set a post-logout redirect URI for the builder.
         *
         * @param postLogoutRedirectUri the post-logout redirect URI
         * @return this builder
         */
        public Builder postLogoutRedirectUri(final URI postLogoutRedirectUri) {
            builderPostLogoutRedirectUri = postLogoutRedirectUri;
            return this;
        }

        /**
         * Set a state for the builder.
         *
         * @param state the state value
         * @return this builder
         */
        public Builder state(final String state) {
            builderState = state;
            return this;
        }

        /**
         * Build the end session request.
         *
         * @return the end session request
         */
        public EndSessionRequest build() {
            return new EndSessionRequest(builderClientId, builderPostLogoutRedirectUri,
                    builderIdTokenHint, builderState);
        }

        private Builder() {
            // Prevent direct instantiation
        }
    }
}

