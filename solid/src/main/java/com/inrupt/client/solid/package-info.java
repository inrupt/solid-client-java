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
 *
 * <h2>Solid Resource and Container support for the Inrupt client libraries.</h2>
 *
 * <p>This module contains a BodyHandler which consumes the response body
 *  and converts it into a {@link SolidResource} or a {@link SolidContainer} Java object.
 *
 * <p>The following example reads a Solid Container and presents it as a {@link SolidContainer} Java object:
 *
 * <pre>{@code
        Request request = Request.newBuilder()
            .uri(URI.create("https://solid.example/storage/"))
            .header("Accept", "text/turtle")
            .GET()
            .build();

        Response<SolidContainer> response = client.send(
            request,
            SolidResourceHandlers.ofSolidContainer()
        );
        System.out.println("HTTP status: " + response.statusCode());
        System.out.println("Resource URI: " + response.body().getId());}
 * </pre>
 */
package com.inrupt.client.solid;
