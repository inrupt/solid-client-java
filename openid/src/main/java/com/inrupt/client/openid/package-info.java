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
/**
 * <h2>OpenID support for the Inrupt Java Client Libraries.</h2>
 *
 * <p>This module uses the {@code OpenIdProvider} to interact with a OpenId Provider
 * as described in the <a href="https://solidproject.org/TR/oidc-primer"> Solid-OIDC Primer v 0.1.0</a>
 *
 * <p>{@code OpenIdProvider} helps in the interaction with the token endpoint and to construct helper
 * requests for authentication.
 *
 * <h3>Discovering the openID configuration</h3>
 *
 * <pre>{@code
   Client client = ClientProvider.getClient();
   OpenIdProvider openIdProvider = new OpenIdProvider(URI.create("https://issuer.example"), client));

   Metadata opConfig = openIdProvider.metadata();

   System.out.println("Authorization endpoint is: " + opConfig.authorizationEndpoint.toString());
   System.out.println("Token endpoint is: " + opConfig.tokenEndpoint.toString());
 * }</pre>
 *
 * <h3>Creating an AuthorizationRequest (needed for browser-based authorization code flow)</h3>
 *
 * <pre>{@code
   Client client = ClientProvider.getClient();

   AuthorizationRequest authReq = AuthorizationRequest.newBuilder()
      .responseType("code") //required response_type
      .scope(List.of("openid", "webid", "offline_access")) //required scope
      .build(
            "s6BhdRkqt3", //required client_id
            URI.create("https://example.example/callback"); //required redirect_uri

   Request authorizationRequest = Request.newBuilder()
            .uri(URI.create(openIdProvider.authorize(authReq)))
            .GET()
            .build();

   Response authorizationResponse = client.send(authorizationRequest, Response.BodyHandlers.ofString())
                                    .toCompletableFuture().join();
 * }</pre>
 *
 * <h3>Obtaining an ID token</h3>
 *
 * <pre>{@code
   TokenRequest tokenReq = TokenRequest.newBuilder()
            .code("code")
            .codeVerifier("myCodeverifier")
            .redirectUri(URI.create("https://app.example/callback"))
            .clientSecret("myClientSecret")
            .authMethod("client_secret_basic")
            .build(
                "authorization_code",
                "s6BhdRkqt3") //same client_id as from the authorization
            );
   TokenResponse token = openIdProvider.token(tokenReq).toCompletableFuture().join();

   System.out.println("ID Token: " + token.idToken);
   System.out.println("Token type: " + token.tokenType);
 * }</pre>
 *
 * <h3>If we already have a session, we can use it with the ID Tokens serialized as signed
 * JWTs. This abstraction can be used to make use of identity-based authorization
 * in Solid.</h3>
 *
 * <pre>{@code
   Client client = ClientProvider.getClient();
   Session session = client.session(OpenIdSession.ofIdToken(jwt));
   Response res = session.send(req, bodyHandler).toCompletableFuture().join();
 * }</pre>
 *
 * <p>A developer can configure aspects of the ID Token validation.
 * All tokens require the presence of subject ({@code sub}) and issuer ({@code iss}) claims as well as
 * issued at ({@code iat}) and expiration ({@code exp}) claims. By default, signature verification
 * is not enabled, but it can be turned on via configuration, as can audience verification.
 *
 * <pre>{@code
   Client client = ClientProvider.getClient();

   OpenIdConfig config = new OpenIdConfig();
   config.setExpectedAudience("https://app.example/id");
   config.setPublicKeyLocation("https://issuer.example/jwks");
   config.setExpGracePeriodSecs(60);

   Session session = client.session(OpenIdSession.ofIdToken(jwt, config));

   Response res = session.send(req, bodyHandler).toCompletableFuture().join();
 * }</pre>
 *
 * <p>An invalid token will throw an {@link OpenIdException} during session creation.
 *
 * <h3>Ending the session.</h3>
 *
 * <pre>{@code
   EndSessionRequest endReq = EndSessionRequest.Builder.newBuilder()
         .postLogoutRedirectUri(URI.create("https://example.example/callback")) //redirect_uri
         .clientId("s6BhdRkqt3") //client_id
         .state("code")
         .build();

   URI uri = openIdProvider.endSession(endReq).toCompletableFuture().join();
 * }</pre>
 *
 * <h3>Generating a PKCE code challenge and verifier.</h3>
 *
 * <p>The default challenge encoding is SHA-256.
 * Pass an algorithm to createChallenge if another encoding is required.
 *
 * <pre>{@code
   String verifier = PKCE.createVerifier();
   String challenge = PKCE.createChallenge(verifier);
 * }</pre>
 *
 */
package com.inrupt.client.openid;
