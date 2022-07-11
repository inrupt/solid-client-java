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
import java.util.Objects;

/**
 * A class for representing a Token request.
 */
public final class TokenRequest {

    private String grantType;
    private String code;
    private String codeVerifier;
    private String clientId;
    private String clientSecret;
    private String authMethod;
    private URI redirectUri;

    public String getGrantType() {
        return grantType;
    }

    public String getAuthMethod() {
        return authMethod;
    }

    public String getCode() {
        return code;
    }

    public String getCodeVerifier() {
        return codeVerifier;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public URI getRedirectUri() {
        return redirectUri;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    private TokenRequest() {
        // Prevent external instantiation
    }

    /**
     * A builder class for {@link TokenRequest} objects.
     */
    public static class Builder {

        private String builderClientSecret;
        private String builderAuthMethod;
        private String builderCode;
        private String builderCodeVerifier;

        public Builder clientSecret(final String clientSecret) {
            builderClientSecret = clientSecret;
            return this;
        }

        public Builder codeVerifier(final String codeVerifier) {
            builderCodeVerifier = codeVerifier;
            return this;
        }

        public Builder authMethod(final String authMethod) {
            builderAuthMethod = authMethod;
            return this;
        }

        public Builder code(final String code) {
            builderCode = code;
            return this;
        }

        public TokenRequest build(final String grantType, final String clientId, final URI redirectUri) {

            final var grant = Objects.requireNonNull(grantType);

            switch(grantType) {
                case "authorization_code":
                    if (builderCode == null) {
                        throw new IllegalArgumentException(
                                "Missing code parameter for authorization_code grant type");
                    }
                    break;

                case "client_credential":
                    if (builderClientSecret == null) {
                        throw new IllegalArgumentException(
                                "Missing client_secret parameter for client_credential grant type");
                    }
                    break;
            }

            final var req = new TokenRequest();
            req.redirectUri = Objects.requireNonNull(redirectUri);
            req.clientId = Objects.requireNonNull(clientId);
            req.grantType = grant;
            req.code = builderCode;
            req.clientSecret = builderClientSecret;
            req.codeVerifier = builderCodeVerifier;
            req.authMethod = builderAuthMethod;

            return req;
        }
    }
}

