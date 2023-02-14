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
 * <h2>{@link java.net.http.HttpClient} bindings for the Inrupt Java Client Libraries.</h2>
 * 
 * <p>A user of the {@code HttpClientService} should ensure that this implementation is
 * available on the classpath by adding the following dependency:
 *
 * <pre>
 *     &lt;dependency&gt;
 *            &lt;groupId&gt;com.inrupt&lt;/groupId&gt;
 *            &lt;artifactId&gt;inrupt-client-httpclient&lt;/artifactId&gt;
 *            &lt;version&gt;${project.version}&lt;/version&gt;
 *     &lt;/dependency&gt;
 * </pre>
 * 
 * <h3>Example of using the HTTP service send() method to request the Solid logo:</h3>
 *
 * <pre>{@code
    HttpService client = ServiceProvider.getHttpService();
    Request request = Request.newBuilder()
        .uri("https://example.example/solid.png")
        .GET()
        .build();
    Response<byte[]> response = client.send(request, Response.BodyHandlers.ofByteArray()).toCompletableFuture().join();

    System.out.println("HTTP status code: " + response.statusCode());
    System.out.println("Response uri: " + response.uri());
    System.out.println("Content type: " + response.headers().asMap().get(CONTENT_TYPE));
 * }</pre>
 */
package com.inrupt.client.httpclient;
