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
package com.inrupt.client.webid;

import com.inrupt.client.*;
import com.inrupt.client.spi.RdfService;
import com.inrupt.client.spi.ServiceProvider;
import com.inrupt.client.vocabulary.PIM;
import com.inrupt.client.vocabulary.RDF;
import com.inrupt.client.vocabulary.RDFS;
import com.inrupt.client.vocabulary.Solid;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;

/**
 * Body handlers for WebID Profiles.
 */
public final class WebIdBodyHandlers {

    private static final RdfService service = ServiceProvider.getRdfService();

    /**
     * Transform an HTTP response into a WebID Profile object.
     *
     * @param webid the WebID URI
     * @return an HTTP body handler
     */
    public static Response.BodyHandler<WebIdProfile> ofWebIdProfile(final URI webid) {
        return responseInfo -> {
            final var builder = WebIdProfile.newBuilder();
            try (final var input = new ByteArrayInputStream(responseInfo.body().array())) {
                final var graph = service.toGraph(Syntax.TURTLE, input);

                graph.stream(RDFNode.namedNode(webid), RDFNode.namedNode(Solid.oidcIssuer), null)
                    .map(Triple::getObject)
                    .filter(RDFNode::isNamedNode)
                    .map(RDFNode::getURI)
                    .forEach(builder::oidcIssuer);

                graph.stream(RDFNode.namedNode(webid), RDFNode.namedNode(RDFS.seeAlso), null)
                    .map(Triple::getObject)
                    .filter(RDFNode::isNamedNode)
                    .map(RDFNode::getURI)
                    .forEach(builder::seeAlso);

                graph.stream(RDFNode.namedNode(webid), RDFNode.namedNode(PIM.storage), null)
                    .map(Triple::getObject)
                    .filter(RDFNode::isNamedNode)
                    .map(RDFNode::getURI)
                    .forEach(builder::storage);

                graph.stream(RDFNode.namedNode(webid), RDFNode.namedNode(RDF.type), null)
                    .map(Triple::getObject)
                    .filter(RDFNode::isNamedNode)
                    .map(RDFNode::getURI)
                    .forEach(builder::type);

            } catch (final IOException ex) {
                throw new WebIdException("Error parsing WebId profile resource", ex);
            }
            return builder.build(webid);
        };
    }

    private WebIdBodyHandlers() {
        // Prevent instantiation
    }
}
