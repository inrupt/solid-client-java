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
 * <h2>Authentication and Authorization classes for the Inrupt Client Libraries.</h2>
 * 
 * <h3>The Session interface</h3>
 * 
 * <p>In the libraries we make use of the {@link Session} interface to share authentication and
 * authorization information when working with HTTP clients.
 * 
 * <p>The annonymous session, in comparison, does not keep a cache of access tokens.
 * 
* <pre>{@code
    SolidClient client = SolidClient.getClient().session(Session.anonymous());
 * }</pre>
 * 
 * <p>The session is also used in the authentication/authorization modules and help create a dedicated session
 * for each implementation. Some examples:
 * 
 * <pre>{@code
    Session session = OpenIdSession.ofIdToken(token);
    Session sessionWithConfig = OpenIdSession.ofIdToken(token, config);
    Session umaSession = UmaSession.of(session);
 * }</pre>
 * 
 * <h3>HTTP challenges</h3>
 * 
 * <p>As part of the HTTP Challenge and Response authentication framework, the {@link Challenge} class represents a
 * challenge object as represented in a WWW-Authenticate Response Header. An example code is shown next.
 * 
 * <pre>{@code
    List<Challenge> challenges = WwwAuthenticate.parse(response.headers()
                                .firstValue("WWW-Authenticate").get()).getChallenges();
    System.out.println("The Resource Server uses following authentication options: " + challenges);
    System.out.println("The scheme of the first challenge is: " + challenges.get(0).getScheme());
    System.out.println("The realm (or ID provider) of the first challenge is: "
        + challenges.get(0).getParameter("realm"));
    System.out.println("Authorization server: " + challenges.get(0).getParameter("as_uri");
 * }</pre>
 * 
 * <h3>Client credentials</h3>
 * 
 * <p>We make use of the {@link Credential} class when working with OIDC ID Tokens. Example code is presented next.
 * 
 * <pre>{@code
    Credential token = new Credential("Bearer", URI.create(ISS), this.token,
        Instant.now().plusSeconds(3600), URI.create(WEBID), null);
    ...
    final Optional<Credential> credential = session.authenticate(null, Collections.emptySet())
        .toCompletableFuture().join();
    ....
    Session session = OpenIdSession.ofIdToken(token, config);
    System.out.println("The token is an OpenID token " + session.getCredential(OpenIdSession.ID_TOKEN).isPresent());
 * }</pre>
 * 
 * <h3>Authentication</h3>
 * 
 * <p>The {@link Authenticator} is the interface to call if one wants to develope an own authenticate logic.
 * 
 * <pre>{@code
    class TestAuthenticator implements Authenticator {
        {@literal @}Override
        public String getName() {
            return "TEST";
        }

        {@literal @}Override
        public int getPriority() {
            return 1;
        }

        {@literal @}Override
        public CompletionStage<Credential> authenticate(Session session,
            Request request, Set<String> algorithms) {
                ...
        }
    }
 * }</pre>
 * 
 * <p>If one want to make use of DPoP, the {@link DPoP} interface makes available the basic
 * methods for generating a proof or creating a DPoP manager for example.
 * 
 * <p>{@link ReactiveAuthorization} is the class which will negotiate for a token based on the WWW-Authenticate header
 * and the Authenticator loaded on the classpath.
 * 
 */
package com.inrupt.client.auth;
