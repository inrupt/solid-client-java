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
 * <h2>User Managed Access support for the Inrupt client libraries</h2>
 *
 * <p>UMA builds on the OAuth 2.0 authorization framework, defining a mechanism by which
 * a client can iteratively negotiate for an access token.
 * 
 * <p>{@code UmaClient} helps in the interaction with different endpoints, to construct helper
 * requests for authentication and to negociate for a token.
 * 
 * <h3>Using a UMA session</h3>
 *
 * <p>This module has a session implementation, {@code UmaSession}, for use with UMA Authorization Servers.
 *
 * <p>This session implementation can be used to wrap other session objects, such as
 * ones that use OpenID Connect tokens.
 *
 * <pre>{@code
 *   Client client = ClientProvider.getClient();
 *   Session session = client.session(UmaSession.ofSession(OpenIdSession.ofIdToken(jwt)));
 *   Response res = session.send(req, bodyHandler);
 * }</pre>
 * 
 * <h3>Discovering the UMA configuration</h3>
 * 
 * <pre>{@code
    URI asUri = URI.create("https://example.example/as_uri");
    UmaClient client = new UmaClient();
    Metadata metadata = client.metadata(asUri).toCompletableFuture().join();

    System.out.println("Token endpoint is: " + metadata.tokenEndpoint);
    System.out.println("JWKs endpoint is: " + metadata.jwksUri);
 * }</pre>
 * 
 * <h3>Negotiating for a token</h3>
 * 
 * <p>UMA defines an OAuth 2.0 profile by which applications can negotiate for an access token
 * through an iterative claims gathering process.
 * 
* <pre>{@code
    URI asUri = URI.create("https://example.example/as_uri");
    UmaClient client = new UmaClient();
    Metadata metadata = client.metadata(asUri).toCompletableFuture().join();
    String idToken = "oidc-id-token";
    String ticket = "ticket-need-info-oidc-requirement";
    TokenRequest req = new TokenRequest(ticket, null, null, null, null);

    TokenResponse token = client.metadata(asUri)
        .thenCompose(metadata ->
            client.token(metadata.tokenEndpoint, req, needInfo ->
                CompletableFuture.completedFuture(ClaimToken.of(idToken, ID_TOKEN_CLAIM_TOKEN_FORMAT))))
        .toCompletableFuture().join();

    System.out.println("Access token is:" + token.accessToken);
    System.out.println("Token type is:" + token.tokenType);
 * }</pre>
 * 
 * <h3>Interpreting different token negotiation problems</h3>
 * 
* <pre>{@code
    URI asUri = URI.create("https://example.example/as_uri");
    UmaClient client = new UmaClient();
    Metadata metadata = client.metadata(asUri).toCompletableFuture().join();
    String idToken = "oidc-id-token";
    String ticket = "ticket-need-info-oidc-requirement";
    TokenRequest req = new TokenRequest(ticket, null, null, null, null);

    final CompletionException err = assertThrows(CompletionException.class, client.metadata(asUri)
                .thenCompose(metadata ->
                    client.token(metadata.tokenEndpoint, req, needInfo -> {
                            throw new UmaException("Unable to negotiate a simple token");
                        }))
                .toCompletableFuture()::join);

    if (err.getCause() instanceof RequestDeniedException) {
        System.out.println("Encountered a request denied");
    }
    if (err.getCause() instanceof InvalidScopeException) {
        System.out.println("An invalid scope was provided");
    }
 * }</pre>
 */
package com.inrupt.client.uma;
