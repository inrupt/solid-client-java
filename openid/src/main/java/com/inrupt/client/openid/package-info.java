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
/**
 * <h2>OpenID support for the Inrupt client libraries.</h2>
 *
 * <p>This modules uses the {@code OpenIdProvider} to interact with a OpenId Provider
 * as described in the <a href="https://solidproject.org/TR/oidc-primer"> Solid-OIDC Primer v 0.1.0</a>
 * 
 * <p>To retrieve the OpenId Provider configuration one can make use of the methadata() method as in the next example:
 *
 * <pre>{@code
    DPoP dpop = new DPoP();
    HttpProcessor client = ServiceProvider.getHttpProcessor();
    OpenIdProvider openIdProvider = new OpenIdProvider(URI.create("https:/example.example/issuer")), dpop, client);

    Metadata opConfig = openIdProvider.metadata();

    System.out.println("Token endpoint is: " + opConfig.tokenEndpoint.toString());
    System.out.println("Revocation endpoint is: " + opConfig.revocationEndpoint.toString());
 * }</pre>
 * 
 * <p>Generating a PKCE code challenge and verifier is as simple as in the next example:
 * 
 * <pre>{@code
    String challenge = PKCE.createChallenge("example verifier");
    String verifier = PKCE.createVerifier();
 * }</pre>
 * 
 * <p>At some point we need to request a token with a code and a verifier. This is achieved
 * as in the following example:
 * 
 * <pre>{@code
    TokenRequest tokenReq = TokenRequest.newBuilder()
        .code("someCode")
        .codeVerifier("myCodeverifier")
        .build(
            "authorization_code",
            "myClientId",
            URI.create("https://example.example/redirectUri")
        );

    TokenResponse token = openIdProvider.token(tokenReq);

    System.out.println("Token id is: " + token.idToken);
    System.out.println("Token type is: " + token.tokenType);
 * }</pre>
 */
package com.inrupt.client.openid;
