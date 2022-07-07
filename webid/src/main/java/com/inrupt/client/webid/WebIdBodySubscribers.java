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

import static org.apache.jena.rdf.model.ResourceFactory.createProperty;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;

import com.inrupt.client.vocabulary.PIM;
import com.inrupt.client.vocabulary.RDFS;
import com.inrupt.client.vocabulary.Solid;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpResponse;

import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

/**
 * Classes for reading HTTP responses as WebID Profile objects.
 */
public final class WebIdBodySubscribers {

    /**
     * Process an HTTP response as a WebID Profile.
     *
     * <p>This method expects to read a TURTLE serialization of an HTTP response.
     *
     * @param webid the WebID URI
     * @return the body subscriber
     */
    public static HttpResponse.BodySubscriber<WebIdProfile> ofWebIdProfile(final URI webid) {
        final var upstream = HttpResponse.BodySubscribers.ofInputStream();
        return HttpResponse.BodySubscribers.mapping(upstream, (InputStream input) -> {
            final var model = ModelFactory.createDefaultModel();
            RDFDataMgr.read(model, input, Lang.TURTLE);

            final var builder = WebIdProfile.newBuilder();
            model.listObjectsOfProperty(createResource(webid.toString()), createProperty(Solid.oidcIssuer.toString()))
                .filterKeep(RDFNode::isURIResource)
                .mapWith(RDFNode::asResource)
                .mapWith(Resource::getURI)
                .mapWith(URI::create)
                .forEach(builder::oidcIssuer);

            model.listObjectsOfProperty(createResource(webid.toString()), createProperty(RDFS.seeAlso.toString()))
                .filterKeep(RDFNode::isURIResource)
                .mapWith(RDFNode::asResource)
                .mapWith(Resource::getURI)
                .mapWith(URI::create)
                .forEach(builder::seeAlso);

            model.listObjectsOfProperty(createResource(webid.toString()), createProperty(PIM.storage.toString()))
                .filterKeep(RDFNode::isURIResource)
                .mapWith(RDFNode::asResource)
                .mapWith(Resource::getURI)
                .mapWith(URI::create)
                .forEach(builder::storage);

            return builder.build(webid);
        });
    }

    private WebIdBodySubscribers() {
        // Prevent instantiation
    }
}
