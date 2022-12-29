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
package com.inrupt.client.e2e;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.inrupt.client.Headers.Link;
import com.inrupt.client.vocabulary.LDP;
import com.inrupt.client.vocabulary.PIM;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

public class SolidServerTransformer extends ResponseDefinitionTransformer {

    private final Set<String> storage = new HashSet<>();

    @Override
    public String getName() {
        return "Solid Server";
    }

    @Override
    public ResponseDefinition transform(final Request request, final ResponseDefinition responseDefinition,
            final FileSource files, final Parameters parameters) {

        if (request.getMethod().isOneOf(RequestMethod.GET)) {
            if (storage.contains(request.getUrl())) {
                if (request.getUrl().contains("/resource/e2e-test-subject")) {
                    new ResponseDefinitionBuilder()
                    .withStatus(200)
                    .withHeader("Content-Type", "text/turtle")
                    .withHeader("Link", Link.of(LDP.BasicContainer, "type").toString())
                    .withHeader("Link", Link.of(URI.create("http://storage.example/"),
                            PIM.storage).toString())
                    .withHeader("Link", Link.of(URI.create("https://history.test/"), "timegate").toString())
                    .withHeader("WAC-Allow", "user=\"read write\",public=\"read\"")
                    .withHeader("Allow", "POST, PUT, PATCH")
                    .withHeader("Accept-Post", "application/ld+json, text/turtle")
                    .withHeader("Accept-Put", "application/ld+json, text/turtle")
                    .withHeader("Accept-Patch", "application/sparql-update, text/n3")
                    .withBodyFile("solidResourceExample.ttl")
                                .build();
                }
            }
        }

        if (request.getMethod().isOneOf(RequestMethod.PUT)) {
            storage.add(request.getUrl());
        }

        if (request.getMethod().isOneOf(RequestMethod.DELETE)) {
            storage.remove(request.getUrl());
        }
        return null;
    }

}
