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
 * <h2>Service interfaces for the Inrupt client libraries.</h2>
 *
 * <p>This module provides a puggable service provider interface, which allows
 * the HTTP server, JSON processing and RDF processing to be replaced with concrete implementations.
 *
 * <p>One can make use of available implementation such as:
 * <ul>
 * <li> for HTTP server: {@code OkHttpProcessor} or the java.net {@code HttpProcessor};</li>
 * <li> for JSON processing: {@code JsonbProcessor} or the {@code JacksonProcessor};</li>
 * <li> for RDF processing: {@code JenaRdfProcessor} or the {@code RDF4JRdfProcessor}.</li>
 * </ul>
 *
 * <p>To make use of a concrete implementation make sure, first, to
 * add the needed modules to the pom of your module. Example:
 *
 * <pre>{@code
        &lt;dependency&gt;
            &lt;groupId&gt;com.inrupt&lt;/groupId&gt;
            &lt;artifactId&gt;inrupt-client-xxxConcreteImplementationNamexxx&lt;/artifactId&gt;
            &lt;version&gt;${project.version}&lt;/version&gt;
        &lt;/dependency&gt;
 * }</pre>
 *
 * <h3>Example of using the JSON processor fromJson methog to read a {@code VerifiableCredential}:</h3>
 *
 * <pre>{@code
        JsonProcessor processor = ServiceProvider.getJsonProcessor();
        VerifiableCredential vc;
        try (InputStream is =
                Test.class.getResourceAsStream("verifiableCredential.json")) {
            vc = processor.fromJson(is, VerifiableCredential.class);
        }
        System.out.println("The Verifiable Credential Id is: " + vc.id);
 * }</pre>
 * 
 * <h3>Example of using the RDF processor toDataser method to read triples from a trig file into a {@code Dataset}:</h3>
 *
 * <pre>{@code
        RdfProcessor processor = ServiceProvider.getRdfProcessor();
        Dataset dataset;
        try (InputStream input = Test.class.getResourceAsStream("/oneTriple.trig")) {
            dataset = processor.toDataset(Syntax.TRIG, input);
        }
        System.out.println("Number of triples in file: " + dataset.stream().count());
 * }</pre>
 * 
 * <h3>Example of using the HTTP server send method to request the Solid logo:</h3>
 *
 * <pre>{@code
        HttpProcessor processor = ServiceProvider.getHttpProcessor();
        Request request = Request.newBuilder()
                .uri("https://example.example/solid.png")
                .GET()
                .build();
        Response<byte[]> response = client.send(request, Response.BodyHandlers.ofByteArray());

        System.out.println("HTTP status code: " + response.statusCode());
        System.out.println("Response uri: " + response.uri());
        System.out.println("Content type: " + response.headers().get("Content-Type"));
 * }</pre>
 */
package com.inrupt.client.spi;
