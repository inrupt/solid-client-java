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

    Response response = client.send(request, VerifiableCredentialBodyHandlers.ofVerifiableCredential())
                        .toCompletableFuture().join();
    System.out.println( HTTP status code: " + response.statusCode());
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
 * a few code examples can be seen as follows:
 * 
 * <p>Issuing a Verifiable Credential asynchronously is shown in the next example.
 * 
 * <pre>{@code
    HttpProcessor client = ServiceProvider.getHttpProcessor();
    Issuer issuer = new Issuer(URI.create("https://example.example"), client);
    VerifiableCredential vc = issuer.issue(myVC).toCompletableFuture().join();
 * }</pre>
 * 
 * <p>To update the status of a Verifiable Credential we would call:
 * 
 * <pre>{@code
    StatusRequest statusRequest = StatusRequest.Builder.newBuilder()
        .credentialStatus(URI.create("CredentialStatusList2017"), true)
        .build("http://example.example/credentials/1872");

    issuer.status(statusRequest).toCompletableFuture().join();
 * }</pre>
 * 
 * <h3>Interacting with the VC-API Verifier endpoint</h3>
 * 
 * <p>To verify a credential (or similar a presentation) we can use the following:
 * <pre>{@code
   HttpService client = ServiceProvider.getHttpService();
   Verifier verifier = new Verifier(URI.create("https://example.example"), client);

   Verifier.VerificationResponse verificationResponse = verifier.verify(myVC).toCompletableFuture().join();
   System.out.println("The verification checks are: " + verificationResponse.checks);
   System.out.println("The verification warnings are: " + verificationResponse.warnings);
   System.out.println("The verification errors are: " + verificationResponse.errors);
 * }</pre>
 * 
 * <h3>Interacting with the VC-API Holder endpoint</h3>
 * 
 * <p>To list Verifiable Credentials (and similar Verifiable Presentations) of a certain type we use:
 * 
 * <pre>{@code
   HttpService client = ServiceProvider.getHttpService();
   Holder holder = new Holder(URI.create("https://example.example"), client);

   var vcList = holder.listCredentials(List.of(URI.create("VerifiableCredential"),
                                               URI.create("UniversityDegreeCredential")))
                    .toCompletableFuture().join();
   System.out.println("We found exactly " + vcList.size() + " VCs");
 * }</pre>
 * 
 * <p>To retrieve a certain credential (and similar a presentation) one needs a credentialId:
 * 
 * <pre>{@code
   HttpService client = ServiceProvider.getHttpService();
   Holder holder = new Holder(URI.create("https://example.example"), client);

   var vc = holder.getCredential(aVCid).toCompletableFuture().join();
   System.out.println("The retrieved Verifiable Credential's issuer is: " + vc.issuer);
 * }</pre>
 * 
 * <p>To delete a credential (and similar a presentation) asynchronously one can call:
 * <pre>{@code
   holder.deleteCredential(aVCid)).toCompletableFuture().join();
 * }</pre>
 * 
 * <p>To derive a credential one can call:
 * <pre>{@code
   DerivationRequest derivationReq = new Holder.DerivationRequest();
   derivationReq.verifiableCredential = myVC;
   derivationReq.frame = Collections.emptyMap();
   derivationReq.options = Map.of("nonce",
            "lEixQKDQvRecCifKl789TQj+Ii6YWDLSwn3AxR0VpPJ1QV5htod/0VCchVf1zVM0y2E=");

   var vc = holder.derive(derivationReq).toCompletableFuture().join();
   System.out.println("The retrieved Verifiable Credential's issuer is: " + vc.issuer);
 * }</pre>
 * 
 * <p>To prove a presentation asynchronously, one would call:
 * <pre>{@code
   ProveRequest derivationReq = new Holder.ProveRequest();
   derivationReq.presentation = myVP;
   derivationReq.options = Map.of("nonce",
            "lEixQKDQvRecCifKl789TQj+Ii6YWDLSwn3AxR0VpPJ1QV5htod/0VCchVf1zVM0y2E=");

   var vp = holder.prove(derivationReq).toCompletableFuture().join();
   System.out.println("The retrieved Verifiable Presentation's holder is: " + vp.holder);
 * }</pre>
 * 
 * <p>To initiate a presentation exchange, one would use:
 * <pre>{@code
   ExchangeRequest exchangeReq = new Holder.ExchangeRequest();
   exchangeReq.query = new Holder.Query();
   exchangeReq.query.type = URI.create("QueryByExample");
   exchangeReq.query.credentialQuery = Map.of(
            "reason", "We need to see your existing University Degree credential.",
            "example", Map.of(
               "@context", List.of(
                           "https://www.w3.org/2018/credentials/v1",
                           "https://www.w3.org/2018/credentials/examples/v1"),
               "type", "UniversityDegreeCredential"));

   var vpr = holder.initiateExchange("credential-refresh", exchangeReq).toCompletableFuture().join();
   System.out.println("The Verifiable Presentation Request domain: " + vpr.domain);
   System.out.println("The Verifiable Presentation Request challenge: " + vpr.challenge);
 * }</pre>
 */
package com.inrupt.client.vc;
