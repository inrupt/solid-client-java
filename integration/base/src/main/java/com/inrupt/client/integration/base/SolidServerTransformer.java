/*
 * Copyright Inrupt Inc.
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
package com.inrupt.client.integration.base;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformerV2;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.inrupt.client.Headers;
import com.inrupt.client.ProblemDetails;
import com.inrupt.client.integration.base.MockSolidServer.ServerBody;
import com.inrupt.client.vocabulary.PIM;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

class SolidServerTransformer implements ResponseDefinitionTransformerV2 {

    private static final String SLASH = "/";

    private final Map<String, ServerBody> storage;
    private String asUri;

    public SolidServerTransformer(final Map<String, ServerBody> storage, final String asUri) {
        this.storage = storage;
        this.asUri = asUri;
    }

    @Override
    public String getName() {
        return "Solid Server";
    }

    @Override
    public ResponseDefinition transform(final ServeEvent event) {

        final var res = new ResponseDefinitionBuilder();
        final var request = event.getRequest();

        //determine if authenticated on private resources
        if (Utils.isPrivateResource(request.getUrl()) &&
            request.getHeader("Authorization") == null) {
            res.withStatus(Utils.UNAUTHORIZED);
            res.withHeader("WWW-Authenticate",
                        "Bearer, DPoP algs=\"ES256\", UMA ticket=token-67890, as_uri=\""
                        + this.asUri + "\"");
            res.withHeader(Utils.CONTENT_TYPE, ProblemDetails.MIME_TYPE);
            res.withBody(problemDetails(Utils.UNAUTHORIZED, "Unauthorized",
                        "The client does not have a valid access token", UUID.randomUUID()));
            return res.build();
        }

        if (request.getMethod().isOneOf(RequestMethod.GET)) {
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
                return res.withStatus(Utils.NOT_FOUND)
                    .withHeader(Utils.CONTENT_TYPE, ProblemDetails.MIME_TYPE)
                    .withBody(problemDetails(Utils.NOT_FOUND, "Not Found", "The resource does not exist",
                                UUID.randomUUID()))
                    .build();
            }
        }

        if (request.getMethod().isOneOf(RequestMethod.POST)) {
            if (request.getUrl().endsWith(Utils.FOLDER_SEPARATOR)) {
                if (this.storage.containsKey(request.getUrl())) {
                    final String slug = request.getHeader("Slug");
                    final String location = request.getUrl() + (slug != null ? slug : UUID.randomUUID());
                    this.storage.put(location, new ServerBody(request.getBody(),
                        request.contentTypeHeader().mimeTypePart()));
                    res.withStatus(Utils.CREATED);
                    res.withHeader("Location", location);
                } else {
                    res.withStatus(Utils.NOT_FOUND);
                    res.withHeader(Utils.CONTENT_TYPE, ProblemDetails.MIME_TYPE);
                    res.withBody(problemDetails(Utils.NOT_FOUND, "Not Found", "The resource does not exist",
                                UUID.randomUUID()));
                }
            } else {
                res.withStatus(Utils.NOT_ALLOWED);
                res.withHeader(Utils.CONTENT_TYPE, ProblemDetails.MIME_TYPE);
                res.withBody(problemDetails(Utils.NOT_ALLOWED, "Method Not Allowed",
                            "The provided HTTP method is not allowed at this URL", UUID.randomUUID()));
            }
            return res.build();
        }

        if (request.getMethod().isOneOf(RequestMethod.HEAD)) {
            if (this.storage.containsKey(request.getUrl())) {
                res.withStatus(Utils.SUCCESS);
            } else {
                res.withStatus(Utils.NOT_FOUND);
                res.withHeader(Utils.CONTENT_TYPE, ProblemDetails.MIME_TYPE);
                res.withBody(problemDetails(Utils.NOT_FOUND, "Not Found", "The resource does not exist",
                            UUID.randomUUID()));
            }
            res.build();
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
                res.withHeader(Utils.CONTENT_TYPE, ProblemDetails.MIME_TYPE);
                res.withBody(problemDetails(Utils.PRECONDITION_FAILED, "Precondition Failed",
                            "The expected preconditions could not be fulfilled", UUID.randomUUID()));
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
                    final var contentType = serverBody != null ? serverBody.contentType : Utils.TEXT_TURTLE;
                    this.storage.put(request.getUrl(),
                            new ServerBody(newBody, contentType));
                    addSubContainersToStorage(request.getUrl(), request.contentTypeHeader().mimeTypePart());
                    res.withStatus(Utils.NO_CONTENT);
                } catch (IOException e) {
                    res.withStatus(Utils.ERROR);
                    res.withHeader(Utils.CONTENT_TYPE, ProblemDetails.MIME_TYPE);
                    res.withBody(problemDetails(Utils.ERROR, "Internal Server Error",
                                "The server encountered an internal problem", UUID.randomUUID()));
                }
            } else {
                res.withStatus(Utils.ERROR);
                res.withHeader(Utils.CONTENT_TYPE, ProblemDetails.MIME_TYPE);
                res.withBody(problemDetails(Utils.ERROR, "Internal Server Error",
                                "The server encountered an internal problem", UUID.randomUUID()));
            }
            return res.build();
        }

        if (request.getMethod().isOneOf(RequestMethod.DELETE)) {
            if (this.storage.containsKey(request.getUrl())) {
                this.storage.remove(request.getUrl());
                res.withStatus(Utils.NO_CONTENT);
            } else {
                res.withStatus(Utils.NOT_FOUND);
                res.withHeader(Utils.CONTENT_TYPE, ProblemDetails.MIME_TYPE);
                res.withBody(problemDetails(Utils.NOT_FOUND, "Not Found", "The resource was not found",
                        UUID.randomUUID()));
            }
            return res.build();
        }

        return res.withStatus(Utils.NOT_ALLOWED)
            .withHeader(Utils.CONTENT_TYPE, ProblemDetails.MIME_TYPE)
            .withBody(problemDetails(Utils.NOT_ALLOWED, "Method Not Allowed",
                        "The HTTP method is not allowed at this URL", UUID.randomUUID()))
            .build();
    }

    private void addSubContainersToStorage(final String path, final String mimeType) {
        final var newPath = path.startsWith(SLASH) ? path.substring(1, path.length()) : path;
        var containers = newPath.split(SLASH);
        while (containers.length > 0) {
            final var uri = SLASH + String.join(SLASH, containers) + SLASH;
            if (!this.storage.containsKey(uri)) {
                this.storage.put(uri, new ServerBody(new byte[0], mimeType));
            }
            containers = Arrays.copyOf(containers, containers.length - 1);
        }
    }

    static String problemDetails(final int status, final String title, final String description,
            final UUID instance) {
        final var builder = new StringBuilder();
        builder.append("{");
        builder.append("\"status\":" + status);
        builder.append(",\"title\":\"" + (title == null ? "(title)" : title) + "\"");
        builder.append(",\"instance\":\"urn:uuid:" + (instance == null ? UUID.randomUUID() : instance) + "\"");
        builder.append(",\"description\":\"" + (description == null ? "(description)" : description) + "\"");
        builder.append("}");
        return builder.toString();
    }
}
