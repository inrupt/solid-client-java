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

import static com.inrupt.client.vocabulary.RDF.type;

import com.inrupt.client.*;
import com.inrupt.client.spi.RDFFactory;
import com.inrupt.client.spi.RdfService;
import com.inrupt.client.spi.ServiceProvider;
import com.inrupt.client.vocabulary.PIM;
import com.inrupt.client.vocabulary.RDFS;
import com.inrupt.client.vocabulary.Solid;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.api.RDFSyntax;
import org.apache.commons.rdf.api.Triple;

/**
 * Body handlers for WebID Profiles.
 */
public final class WebIdBodyHandlers {

    private static final RdfService service = ServiceProvider.getRdfService();
    private static final RDF rdf = RDFFactory.instance();

    /**
     * Transform an HTTP response into a WebID Profile object.
     *
     * @param webid the WebID URI
     * @return an HTTP body handler
     */
    public static Response.BodyHandler<WebIdProfile> ofWebIdProfile(final URI webid) {
        final IRI oidcIssuer = rdf.createIRI(Solid.oidcIssuer.toString());
        final IRI seeAlso = rdf.createIRI(RDFS.seeAlso.toString());
        final IRI storage = rdf.createIRI(PIM.storage.toString());
        final IRI rdfType = rdf.createIRI(type.toString());

        return responseInfo -> {
            final WebIdProfile.Builder builder = WebIdProfile.newBuilder();
            try (final InputStream input = new ByteArrayInputStream(responseInfo.body().array())) {
                final Graph graph = service.toGraph(RDFSyntax.TURTLE, input, responseInfo.uri().toString());
                final IRI subject = rdf.createIRI(webid.toString());

                graph.stream(subject, oidcIssuer, null)
                    .map(Triple::getObject).filter(IRI.class::isInstance).map(IRI.class::cast)
                    .map(IRI::getIRIString).map(URI::create).forEach(builder::oidcIssuer);

                graph.stream(subject, seeAlso, null)
                    .map(Triple::getObject).filter(IRI.class::isInstance).map(IRI.class::cast)
                    .map(IRI::getIRIString).map(URI::create).forEach(builder::seeAlso);

                graph.stream(subject, storage, null)
                    .map(Triple::getObject).filter(IRI.class::isInstance).map(IRI.class::cast)
                    .map(IRI::getIRIString).map(URI::create).forEach(builder::storage);

                graph.stream(subject, rdfType, null)
                    .map(Triple::getObject).filter(IRI.class::isInstance).map(IRI.class::cast)
                    .map(IRI::getIRIString).map(URI::create).forEach(builder::type);

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
