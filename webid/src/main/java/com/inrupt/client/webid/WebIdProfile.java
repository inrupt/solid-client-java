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

import com.inrupt.client.Resource;
import com.inrupt.client.vocabulary.PIM;
import com.inrupt.client.vocabulary.RDF;
import com.inrupt.client.vocabulary.RDFS;
import com.inrupt.client.vocabulary.Solid;

import java.net.URI;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.rdf.api.Dataset;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Quad;

/**
 * A WebID Profile for use with Solid.
 */
public class WebIdProfile extends Resource {

    private final IRI rdfType;
    private final IRI subject;
    private final IRI oidcIssuer;
    private final IRI seeAlso;
    private final IRI storage;

    /**
     * Create a new WebID profile resource.
     *
     * @param identifier the webid URI
     * @param dataset the webid profile dataset
     */
    public WebIdProfile(final URI identifier, final Dataset dataset) {
        super(identifier, dataset);

        this.subject = rdf.createIRI(identifier.toString());
        this.rdfType = rdf.createIRI(RDF.type.toString());
        this.oidcIssuer = rdf.createIRI(Solid.oidcIssuer.toString());
        this.seeAlso = rdf.createIRI(RDFS.seeAlso.toString());
        this.storage = rdf.createIRI(PIM.storage.toString());
    }

    /**
     * Retrieve the RDF type values.
     *
     * @return the {@code rdf:type} values
     */
    public Set<URI> getType() {
        try (final Stream<Quad> stream = getDataset().stream(null, subject, rdfType, null)
                .map(Quad.class::cast)) {
            return stream.map(Quad::getObject).filter(IRI.class::isInstance)
                .map(IRI.class::cast).map(IRI::getIRIString).map(URI::create)
                .collect(Collectors.toSet());
        }
    }

    /**
     * Retrieve the list of OIDC issuers.
     *
     * @return the {@code solid:oidcIssuer} values
     */
    public Set<URI> getOidcIssuer() {
        try (final Stream<Quad> stream = getDataset().stream(null, subject, oidcIssuer, null)
                .map(Quad.class::cast)) {
            return stream.map(Quad::getObject).filter(IRI.class::isInstance)
                .map(IRI.class::cast).map(IRI::getIRIString).map(URI::create)
                .collect(Collectors.toSet());
        }
    }

    /**
     * Retrieve the list of related profile resources.
     *
     * @return the {@code rdfs:seeAlso} values
     */
    public Set<URI> getSeeAlso() {
        try (final Stream<Quad> stream = getDataset().stream(null, subject, seeAlso, null)
                .map(Quad.class::cast)) {
            return stream.map(Quad::getObject).filter(IRI.class::isInstance)
                .map(IRI.class::cast).map(IRI::getIRIString).map(URI::create)
                .collect(Collectors.toSet());
        }
    }

    /**
     * Retrieve the list of storage locations.
     *
     * @return the {@code pim:storage} values
     */
    public Set<URI> getStorage() {
        try (final Stream<Quad> stream = getDataset().stream(null, subject, storage, null)
                .map(Quad.class::cast)) {
            return stream.map(Quad::getObject).filter(IRI.class::isInstance)
                .map(IRI.class::cast).map(IRI::getIRIString).map(URI::create)
                .collect(Collectors.toSet());
        }
    }
}
