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
package com.inrupt.client.integration;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.inrupt.client.Headers;
import com.inrupt.client.integration.MockSolidServer.ServerBody;
import com.inrupt.client.vocabulary.PIM;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

public class SolidServerTransformer extends ResponseDefinitionTransformer {

    private final Map<String, ServerBody> storage;
    private String as_uri;

    public SolidServerTransformer(final Map<String, ServerBody> storage, final String asUri) {
        this.storage = storage;
        this.as_uri = asUri;
    }

    @Override
    public String getName() {
        return "Solid Server";
    }

    @Override
    public ResponseDefinition transform(final Request request, final ResponseDefinition responseDefinition,
            final FileSource files, final Parameters parameters) {

        final var res = new ResponseDefinitionBuilder();

        //determine if authenticated on private resouces
        if (Utils.isPrivateResource(request.getUrl()) &&
            request.getHeader("Authorization") == null) {
            res.withHeader("WWW-Authenticate",
                        "Bearer, DPoP algs=\"ES256\", UMA ticket=token-67890, as_uri=\""
                        + this.as_uri + "\"");
            res.withStatus(Utils.UNAUTHORIZED);
            return res.build();
        }

        if (request.getMethod().isOneOf(RequestMethod.GET, RequestMethod.HEAD)) {
            if (this.storage.containsKey(request.getUrl())) {
                final var serverBody = this.storage.get(request.getUrl());
                res.withStatus(Utils.SUCCESS)
                    .withHeader(Utils.CONTENT_TYPE, serverBody.contentType)
                    .withBody(serverBody.body);

                if (Utils.isPodRoot(request.getUrl())) {
                    //we assume the root is publicly accessible
                    res.withHeader("Link", Headers.Link
                            .of(URI.create(PIM.getNamespace() + "Storage"), "type").toString());
                }
                return res.build();
            } else {
                return res.withStatus(Utils.NOT_FOUND).build();
            }
        }

        if (request.getMethod().isOneOf(RequestMethod.POST)) {
            if (request.getUrl().endsWith("/")) {
                if (!this.storage.containsKey(request.getUrl())) {
                    final String slug = request.getHeader("Slug");
                    final String location = request.getUrl() + (slug != null ? slug : UUID.randomUUID());
                    final boolean exists = this.storage.containsKey(request.getUrl());
                    if (!Utils.WILDCARD.equals(request.getHeader(Utils.IF_NONE_MATCH)) || !exists) {
                        this.storage.put(request.getUrl(), new ServerBody(request.getBody(),
                                request.contentTypeHeader().mimeTypePart()));
                        addSubContainersToStorage(request.getUrl(), request.contentTypeHeader().mimeTypePart());
                        res.withStatus(Utils.CREATED);
                        res.withHeader("Location", location);
                    } else {
                        res.withStatus(Utils.PRECONDITION_FAILED);
                    }
                } else {
                    res.withStatus(Utils.NOT_FOUND);
                }
            } else {
                res.withStatus(Utils.NOT_ALLOWED);
            }
            return res.build();
        }

        if (request.getMethod().isOneOf(RequestMethod.PUT)) {
            final boolean exists = this.storage.containsKey(request.getUrl());
            if (!Utils.WILDCARD.equals(request.getHeader(Utils.IF_NONE_MATCH)) || !exists) {
                this.storage.put(request.getUrl(), new ServerBody(request.getBody(),
                        request.contentTypeHeader().mimeTypePart()));
                addSubContainersToStorage(request.getUrl(), request.contentTypeHeader().mimeTypePart());
                res.withStatus(Utils.NO_CONTENT);
            } else {
                res.withStatus(Utils.PRECONDITION_FAILED);
            }
            return res.build();
        }

        if (request.getMethod().isOneOf(RequestMethod.PATCH)) {
            if (request.contentTypeHeader().containsValue(Utils.SPARQL_UPDATE)) {
                final var serverBody = this.storage.get(request.getUrl());
                final var body = serverBody != null ? serverBody.body : new byte[0];
                try {
                    final byte[] newBody =
                            Utils.modifyBody(body, request.getBodyAsString());
                    this.storage.put(request.getUrl(),
                            new ServerBody(newBody, serverBody.contentType));
                    addSubContainersToStorage(request.getUrl(), request.contentTypeHeader().mimeTypePart());
                    res.withStatus(Utils.NO_CONTENT);
                } catch (IOException e) {
                    res.withStatus(Utils.ERROR);
                }
            } else {
                res.withStatus(Utils.ERROR);
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

        return res.withStatus(Utils.NOT_ALLOWED).build();
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
