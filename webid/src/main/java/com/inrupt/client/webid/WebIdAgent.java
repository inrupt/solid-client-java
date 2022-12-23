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

import com.inrupt.client.spi.RDFFactory;
import com.inrupt.client.vocabulary.PIM;
import com.inrupt.client.vocabulary.RDF;
import com.inrupt.client.vocabulary.RDFS;
import com.inrupt.client.vocabulary.Solid;
import com.inrupt.client.wrapping.NodeMappings;
import com.inrupt.client.wrapping.PredicateObjectSet;
import com.inrupt.client.wrapping.ValueMappings;
import com.inrupt.client.wrapping.WrapperIRI;

import java.net.URI;
import java.util.Set;

import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.IRI;

public class WebIdAgent extends WrapperIRI {
    private static final org.apache.commons.rdf.api.RDF FACTORY = RDFFactory.getInstance();

    protected final Graph graph;

    protected WebIdAgent(final IRI original, final Graph graph) {
        super(original);

        this.graph = graph;
    }

    public static WebIdAgent wrap(final IRI original, final Graph graph) {
        return new WebIdAgent(original, graph);
    }

    public Set<URI> getType() {
        return new PredicateObjectSet<>(
                this,
                FACTORY.createIRI(RDF.type.toString()),
                graph,
                NodeMappings::asIriResource,
                ValueMappings::iriAsUri);
    }

    public Set<URI> getOidcIssuer() {
        return new PredicateObjectSet<>(
                this,
                FACTORY.createIRI(Solid.oidcIssuer.toString()),
                graph,
                NodeMappings::asIriResource,
                ValueMappings::iriAsUri);
    }

    public Set<URI> getSeeAlso() {
        return new PredicateObjectSet<>(
                this,
                FACTORY.createIRI(RDFS.seeAlso.toString()),
                graph,
                NodeMappings::asIriResource,
                ValueMappings::iriAsUri);
    }

    public Set<URI> getStorage() {
        return new PredicateObjectSet<>(
                this,
                FACTORY.createIRI(PIM.storage.toString()),
                graph,
                NodeMappings::asIriResource,
                ValueMappings::iriAsUri);
    }
}
