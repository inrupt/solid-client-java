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
 * <h2>Core classes and utilities for the Inrupt client libraries.</h2>
 * 
 * <p>This module provides default implementation for some of the APIs of the library.
 * 
 * <h2>A default HTTP client</h2>
 * 
 * <p>The {@code DefaultClient} builds on top of the HTTP client laoded on the classpath. It adds
 * the reactive authorization functionality as a default on HTTP requests. To make use of it
 * check out the following code.
 * 
 * <pre>{@code
    //Creates an unauthenticated default client if there is no other on the classpath
    Client client = ClientProvider.getClient();

    //Creates an authenticated client
    Session session = OpenIdSession.ofIdToken(token, config);
    Client client = ClientProvider.getClient().session(session);

    //Send POST UMA authenticated request
    Request request = Request.newBuilder()
        .uri("https://example.example/postString"))
        .header("Content-Type", "text/plain")
        .POST(Request.BodyPublishers.ofString("Test String 1"))
        .build();

    Response<Void> response = client.session(UmaSession.of(s))
        .send(request, Response.BodyHandlers.discarding())
        .toCompletableFuture().join();
 * }</pre>
 * 
 * <p>If we have multiple HTTP clients on the classpath we can also still access
 * the DefaultClient through the {@code DefaultClientProviderResolver}.
 * 
 * <h2>A service to work with DPoP</h2>
 * 
 * <p>{@code DefaultDpopService} provides, as the name implies, an already implemented
 * DPoP service for you to make use of. The DPoP service creates a DPoP Manager which keeps
 * track of the keypars involved in the authentication.
 * By default, the manager creates a keypair based on the ES256 algorithm for which it generates a
 * SHA-256 public JWK. One can change the defaults and make use of the out of the box service to generate proofs.
 * 
 * <p>Next we exemplify how to generate a ES256 proof for the GET method.
 * 
 * <pre>{@code
    DPoP dpop = DPoP.of();
    String method = "GET";
    URI uri = URI.create("https://storage.example/resource");
    String proof = dpop.generateProof("ES256", uri, method);
 * }</pre>
 * 
 * <h2>Header parsing default</h2>
 * 
 * <p>The {@code DefaultHeaderParser} parses among the WWW-Authenticate header also some Solid useful headers like
 * WAC-Allow and Link headers.
 *
 */
package com.inrupt.client.core;
