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
package com.inrupt.client.solid;

import com.inrupt.client.*;
import com.inrupt.client.core.Link;
import com.inrupt.client.core.WacAllow;
import com.inrupt.client.spi.RdfService;
import com.inrupt.client.spi.ServiceProvider;
import com.inrupt.client.vocabulary.LDP;
import com.inrupt.client.vocabulary.PIM;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;

/**
 * Body handlers for WebID Profiles.
 */
public final class SolidResourceHandlers {

    private static final RdfService service = ServiceProvider.getRdfService();
    private static final String ERROR_MESSAGE = "Error parsing Solid resource";

    /**
     * Transform an HTTP response into a Solid Resource.
     *
     * @param id the Solid Resources's unique identifiers
     * @return an HTTP body handler
     */
    public static Response.BodyHandler<SolidResource> ofSolidResource(final URI id) {
        return responseInfo -> {
            try (final var input = new ByteArrayInputStream(responseInfo.body().array())) {
                final var dataSet = service.toDataset(Syntax.TURTLE, input);

                final var builder = builderAddBody(dataSet,
                                        builderAddHeaders(responseInfo.headers(), SolidResource.newBuilder()));

                return builder.build(id);

            } catch (final IOException ex) {
                throw new SolidResourceException(ERROR_MESSAGE, ex);
            }
        };
    }

    /**
     * Transform an HTTP response into a Solid Resource.
     *
     * @param id the Solid Container's unique identifiers
     * @return an HTTP body handler
     */
    public static Response.BodyHandler<SolidContainer> ofSolidContainer(final URI id) {
        return responseInfo -> {
            try (final var input = new ByteArrayInputStream(responseInfo.body().array())) {
                final var dataSet = service.toDataset(Syntax.TURTLE, input);

                final var builder = (SolidContainer.Builder) builderAddBody(dataSet,
                                            builderAddHeaders(responseInfo.headers(), new SolidContainer.Builder()));

                dataSet.stream(null, RDFNode.namedNode(id), RDFNode.namedNode(LDP.contains), null)
                    .map(Triple::getObject)
                    .filter(RDFNode::isNamedNode)
                    .map(RDFNode::getURI)
                    .map(SolidResource::new)
                    .forEach((builder)::containedResource);

                return builder.build(id);

            } catch (final IOException ex) {
                throw new SolidResourceException(ERROR_MESSAGE, ex);
            }
        };
    }

    private static SolidResource.Builder builderAddHeaders(final Headers headers, final SolidResource.Builder builder) {
        headers.allValues("Link").stream()
            .flatMap(l -> Link.parse(l).stream())
            .filter(l -> l.getParameter("rel").contains("type"))
            .map(Link::getUri)
            .forEach(builder::type);

        headers.allValues("WAC-Allow").stream()
            .map(WacAllow::parse)
            .map(WacAllow::getAccessParams)
            .flatMap(p -> p.entrySet().stream())
            .forEach(builder::wacAllow);

        headers.allValues("Allow").stream()
            .forEach(builder::allowedMethod);

        headers.allValues("Accept-Post").stream()
            .forEach(builder::allowedPostSyntax);

        headers.allValues("Accept-Patch").stream()
            .forEach(builder::allowedPatchSyntax);

        headers.allValues("Accept-Put").stream()
            .forEach(builder::allowedPutSyntax);

        return builder;
    }

    private static SolidResource.Builder builderAddBody(final Dataset dataSet, final SolidResource.Builder builder) {

        dataSet.stream(null, null, null, null)
            .forEach(builder::statement);

        dataSet.stream(null, null, RDFNode.namedNode(PIM.storage), null)
            .map(Quad::getObject)
            .filter(RDFNode::isNamedNode)
            .map(RDFNode::getURI)
            .forEach(builder::storage);
        return builder;
    }

    private SolidResourceHandlers() {
        // Prevent instantiation
    }
}
