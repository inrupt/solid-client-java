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

import com.inrupt.client.Headers.Link;
import com.inrupt.client.Headers.WacAllow;
import com.inrupt.client.RDFNode;
import com.inrupt.client.Response;
import com.inrupt.client.Syntax;
import com.inrupt.client.Triple;
import com.inrupt.client.spi.RdfService;
import com.inrupt.client.spi.ServiceProvider;
import com.inrupt.client.vocabulary.LDP;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

/**
 * Body handlers for Solid Resources.
 */
public final class SolidResourceHandlers {

    private static final RdfService service = ServiceProvider.getRdfService();
    private static final String ERROR_MESSAGE = "Error parsing Solid resource";

    /**
     * Transform an HTTP response into a Solid Resource.
     *
     * @param id the Solid Resources's unique identifier
     * @return an HTTP body handler
     */
    public static Response.BodyHandler<SolidResource> ofSolidResource(final URI id) {
        return responseInfo -> {
            final var builder = SolidResource.newResourceBuilder();

            try (final var input = new ByteArrayInputStream(responseInfo.body().array())) {
                final var dataSet = service.toDataset(Syntax.TURTLE, input, id.toString());

                responseInfo.headers().allValues("Link").stream()
                    .flatMap(l -> Link.parse(l).stream())
                    .filter(l -> l.getParameter("rel").contains("type"))
                    .map(Link::getUri)
                    .forEach(builder::type);

                responseInfo.headers().allValues("Link").stream()
                    .flatMap(l -> Link.parse(l).stream())
                    .filter(l -> l.getParameter("rel").contains("http://www.w3.org/ns/pim/space#storage"))
                    .map(Link::getUri)
                    .forEach(builder::storage);

                responseInfo.headers().allValues("WAC-Allow").stream()
                    .map(WacAllow::parse)
                    .map(WacAllow::getAccessParams)
                    .flatMap(p -> p.entrySet().stream())
                    .forEach(builder::wacAllow);

                responseInfo.headers().allValues("Allow").stream()
                    .flatMap(s -> Arrays.stream(s.split(",")))
                    .map(String::trim)
                    .forEach(builder::allowedMethod);

                responseInfo.headers().allValues("Accept-Post").stream()
                    .forEach(builder::allowedPostSyntax);

                responseInfo.headers().allValues("Accept-Patch").stream()
                    .forEach(builder::allowedPatchSyntax);

                responseInfo.headers().allValues("Accept-Put").stream()
                    .forEach(builder::allowedPutSyntax);

                dataSet.stream(null, null, null, null)
                    .forEach(builder::statement);

            } catch (final IOException ex) {
                throw new SolidResourceException(ERROR_MESSAGE, ex);
            }

            return builder.build(id);
        };
    }

    /**
     * Transform an HTTP response into a Solid Container.
     *
     * @param id the Solid Container's unique identifier
     * @return an HTTP body handler
     */
    public static Response.BodyHandler<SolidContainer> ofSolidContainer(final URI id) {
        return responseInfo -> {
            final var builder = SolidContainer.newContainerBuilder();
            try (final var input = new ByteArrayInputStream(responseInfo.body().array())) {
                final var dataSet = service.toDataset(Syntax.TURTLE, input, id.toString());

                responseInfo.headers().allValues("Link").stream()
                    .flatMap(l -> Link.parse(l).stream())
                    .filter(l -> l.getParameter("rel").contains("type"))
                    .map(Link::getUri)
                    .forEach(builder::type);

                responseInfo.headers().allValues("Link").stream()
                    .flatMap(l -> Link.parse(l).stream())
                    .filter(l -> l.getParameter("rel").contains("http://www.w3.org/ns/pim/space#storage"))
                    .map(Link::getUri)
                    .forEach(builder::storage);

                responseInfo.headers().allValues("WAC-Allow").stream()
                    .map(WacAllow::parse)
                    .map(WacAllow::getAccessParams)
                    .flatMap(p -> p.entrySet().stream())
                    .forEach(builder::wacAllow);

                responseInfo.headers().allValues("Allow").stream()
                    .flatMap(s -> Arrays.stream(s.split(",")))
                    .map(String::trim)
                    .forEach(builder::allowedMethod);

                responseInfo.headers().allValues("Accept-Post").stream()
                    .forEach(builder::allowedPostSyntax);

                responseInfo.headers().allValues("Accept-Patch").stream()
                    .forEach(builder::allowedPatchSyntax);

                responseInfo.headers().allValues("Accept-Put").stream()
                    .forEach(builder::allowedPutSyntax);

                dataSet.stream(null, null, null, null)
                    .forEach(builder::statement);

                dataSet.stream(null, RDFNode.namedNode(id), RDFNode.namedNode(LDP.contains), null)
                    .map(Triple::getObject)
                    .filter(RDFNode::isNamedNode)
                    .map(RDFNode::getURI)
                    .map(SolidResource::of)
                    .forEach((builder)::containedResource);

            } catch (final IOException ex) {
                throw new SolidResourceException(ERROR_MESSAGE, ex);
            }

            return builder.build(id);
        };
    }

    private SolidResourceHandlers() {
        // Prevent instantiation
    }
}
