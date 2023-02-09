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
package com.inrupt.client.examples.springboot;

import com.inrupt.client.Request;
import com.inrupt.client.Response;
import com.inrupt.client.solid.SolidClient;
import com.inrupt.client.solid.SolidContainer;
import com.inrupt.client.solid.SolidResource;
import com.inrupt.client.solid.SolidResourceHandlers;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class SolidController {

    final SolidClient client = SolidClient.getClient();

    @Value("${environment.pod.uri}")
    private String uriString;

    @GetMapping("/")
    public String index() {
        return "index";
    }
    @GetMapping("/solid")
    public List<URI> solid() {

        final Request request =
                Request.newBuilder().uri(URI.create(uriString)).header("Accept", "text/turtle").GET().build();

        final Response<SolidContainer> response =
                client.send(request, SolidResourceHandlers.ofSolidContainer()).toCompletableFuture().join();
        final SolidContainer container = response.body();

        return container.getContainedResources()
                .map(SolidResource::getIdentifier).toList();
    }
}
