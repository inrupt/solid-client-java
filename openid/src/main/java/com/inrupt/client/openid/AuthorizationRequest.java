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

public final class AuthorizationRequest {

    private String responseType;
    private List<String> scope;
    private String codeChallenge;
    private String codeChallengeMethod;
    private String clientId;
    private URI redirectUri;

    public String getResponseType() {
        return responseType;
    }

    public String getScope() {
        return String.join(" ", scope);
    }

    public String getCodeChallenge() {
        return codeChallenge;
    }

    public String getCodeChallengeMethod() {
        return codeChallengeMethod;
    }

    public String getClientId() {
        return clientId;
    }

    public URI getRedirectUri() {
        return redirectUri;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    private AuthorizationRequest() {
        // Prevent external instantiation
    }

    public static class Builder {

        private String builderResponseType = "code";
        private List<String> builderScope = new ArrayList<>();
        private String builderCodeChallenge;
        private String builderCodeChallengeMethod;

        public Builder scope(final String scope) {
            builderScope.add(scope);
            return this;
        }

        public Builder responseType(final String responseType) {
            builderResponseType = responseType;
            return this;
        }

        public Builder codeChallenge(final String codeChallenge) {
            builderCodeChallenge = codeChallenge;
            return this;
        }

        public Builder codeChallengeMethod(final String codeChallengeMethod) {
            builderCodeChallengeMethod = codeChallengeMethod;
            return this;
        }

        public AuthorizationRequest build(final String clientId, final URI redirectUri) {
            final var req = new AuthorizationRequest();
            req.redirectUri = Objects.requireNonNull(redirectUri);
            req.clientId = Objects.requireNonNull(clientId);
            req.responseType = builderResponseType;
            req.scope = builderScope;
            req.codeChallenge = builderCodeChallenge;
            req.codeChallengeMethod = builderCodeChallengeMethod;
            return req;
        }
    }
}
