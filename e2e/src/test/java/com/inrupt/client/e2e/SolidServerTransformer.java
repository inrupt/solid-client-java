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
import com.inrupt.client.Headers;
import com.inrupt.client.e2e.MockSolidServer.ServerBody;
import com.inrupt.client.vocabulary.PIM;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

public class SolidServerTransformer extends ResponseDefinitionTransformer {

    private final Map<String, ServerBody> storage;

    public SolidServerTransformer(final Map<String, ServerBody> storage) {
        this.storage = storage;
    }

    @Override
    public String getName() {
        return "Solid Server";
    }

    @Override
    public ResponseDefinition transform(final Request request, final ResponseDefinition responseDefinition,
            final FileSource files, final Parameters parameters) {

        final var res = new ResponseDefinitionBuilder();

        if (request.getMethod().isOneOf(RequestMethod.GET)) {
            if (this.storage.containsKey(request.getUrl())) {
                final var serverBody = this.storage.get(request.getUrl());
                res
                    .withStatus(Utils.SUCCESS)
                    .withHeader(Utils.CONTENT_TYPE, serverBody.contentType)
                    .withBody(serverBody.body);

                if (("/").equals(request.getUrl())) { //we found the storage and asume it is the root
                    res.withHeader("Link", Headers.Link.of(PIM.storage, "type").toString());
                }

            } else {
                res.withStatus(Utils.NOT_FOUND);
            }
            return res.build();
        }

        if (request.getMethod().isOneOf(RequestMethod.POST, RequestMethod.PUT)) {
            if (!this.storage.containsKey(request.getUrl())) {
                this.storage.put(request.getUrl(), new ServerBody(request.getBody(),
                        request.contentTypeHeader().mimeTypePart()));
                addSubContainersToStorage(request.getUrl(), request.contentTypeHeader().mimeTypePart());
                res.withStatus(Utils.NO_CONTENT);
            } else {
                //should create the resource with new URI?
                res.withStatus(Utils.PRECONDITION_FAILED);
            }
            return res.build();
        }

        if (request.getMethod().isOneOf(RequestMethod.PATCH)) {
            if (this.storage.containsKey(request.getUrl())) {
                if (request.contentTypeHeader().containsValue(Utils.SPARQL_UPDATE)) {
                    final var serverBody = this.storage.get(request.getUrl());
                    try {
                        final byte[] newBody = Utils.modifyBody(serverBody.body, request.getBodyAsString());
                        this.storage.remove(request.getUrl());
                        this.storage.put(request.getUrl(), new ServerBody(newBody,
                                serverBody.contentType));

                        res.withStatus(Utils.NO_CONTENT);
                    } catch (IOException e) {
                        res.withStatus(Utils.ERROR);
                    }
                } else {
                    res.withStatus(Utils.ERROR);
                }
            } else {
                res.withStatus(Utils.PRECONDITION_FAILED);
            }
            return res.build();
        }

        if (request.getMethod().isOneOf(RequestMethod.DELETE)) {
            if (this.storage.containsKey(request.getUrl())) {
                this.storage.remove(request.getUrl());
                res.withStatus(Utils.NO_CONTENT);
            } else {
                res.withStatus(Utils.NOT_FOUND);
            }
            return res.build();
        }

        return null;
    }

    private void addSubContainersToStorage(final String path, final String mimeType) {
        final var newPath = path.startsWith("/") ? path.substring(1, path.length()) : path;
        var containers = newPath.split("/");
        while (containers.length > 0) {
            final var uri = "/" + String.join("/", containers);
            if (!this.storage.containsKey(uri)) {
                this.storage.put(uri, new ServerBody(new byte[0], mimeType));
            }
            containers = Arrays.copyOf(containers, containers.length - 1);
        }
    }

}
