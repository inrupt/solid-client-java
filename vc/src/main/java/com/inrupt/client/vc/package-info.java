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
 * <h2>Verifiable Credential support for the Inrupt client libraries.</h2>
 *
 * <p>This module facilitates an easy interaction with Verifiable Credentials.
 * 
 * <p>Reading a {@code VerifiableCredential} (or similar a {@code VerifiablePresentation})
 * can be done using the {@code VerifiableCredentialBodyHandlers} as in the
 * following example:
 * 
 * <pre>{@code
    HttpProcessor client = ServiceProvider.getHttpProcessor();
    Request request = Request.newBuilder()
        .uri(URI.create("https://example.example/vc"))
        .GET()
        .build();

    Response<VerifiableCredential> response = client.send(request, VerifiableCredentialBodyHandlers.ofVerifiableCredential());

    System.out.println("HTTP status code: " + response.statusCode());
    System.out.println("Verifiable Credential issuer is: " + response.body().issuer);
 * }</pre>
 * 
 * <p>This module also contains dedicated Java Objects for: 
 * <ul>
 * <li>interacting with a VC-API Issuer endpoint through {@link Issuer};</li>
 * <li>interacting with a VC-API Verifier endpoint through {@link Verifier};</li>
 * <li>interacting with a VC-API Holder endpoint through {@link Holder}.</li>
 * </ul>
 * 
 * <h3>Interacting with the VC-API Issuer endpoint</h3>
 *
 * <p>Reading along the <a href="https://w3c-ccg.github.io/vc-api/">Verifiable Credential API spec v0.3</a>
 * a few code examples can be seen as follows.
 * 
 * <p>Issuing a Verifiable Credential asynchronously is shown in the next example:
 * <pre>{@code
    HttpProcessor client = ServiceProvider.getHttpProcessor();
    Issuer issuer = new Issuer(URI.create("https://example.example"), client);
    VerifiableCredential vc = issuer.issueAsync(myVC).toCompletableFuture().join();
 * }</pre>
 * 
 * <p>To update the status of a Verifiable Crednetial we would call:
 * <pre>{@code
    StatusRequest statusRequest = StatusRequest.Builder.newBuilder()
        .credentialStatus(URI.create("CredentialStatusList2017"), true)
        .build("http://example.example/credentials/1872");

    issuer.statusAsync(statusRequest);
 * }</pre>
 * 
 * <h3>Interacting with the VC-API Verifier endpoint</h3>
 * 
 * <h3>Interacting with the VC-API Holder endpoint</h3>
 */
package com.inrupt.client.vc;
