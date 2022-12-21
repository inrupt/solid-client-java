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
package com.inrupt.client.rdf4j;

import com.inrupt.client.rdf.RDFNode;
import com.inrupt.client.rdf.Triple;

import java.net.URI;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;

/**
 * The RDF4J implementation of a {@link Triple}.
 */
class RDF4JTriple implements Triple {

    private final org.eclipse.rdf4j.model.Triple triple;

    /**
     * Create a RDF4JTriple.
     *
     * @param triple the RDF4J {@link org.eclipse.rdf4j.model.Triple}
     */
    public RDF4JTriple(final org.eclipse.rdf4j.model.Triple triple) {
        this.triple = triple;
    }

    /**
     * Retrieve the {@link RDFNode} subject.
     *
     * @return the {@link RDFNode} subject from {@code triple}
     */
    @Override
    public RDFNode getSubject() {
        final Resource s = triple.getSubject();
        if (s.isIRI()) {
            return RDFNode.namedNode(URI.create(s.stringValue()));
        }
        return RDFNode.blankNode(s.stringValue());
    }

    /**
     * Retrieve the {@link RDFNode} predicate.
     *
     * @return the {@link RDFNode} predicate from {@code triple}
     */
    @Override
    public RDFNode getPredicate() {
        final IRI p = triple.getPredicate();
        return RDFNode.namedNode(URI.create(p.stringValue()));
    }

    /**
     * Retrieve the {@link RDFNode} object.
     *
     * @return the {@link RDFNode} object from {@code triple}
     */
    @Override
    public RDFNode getObject() {
        final Value o = triple.getObject();
        if (o.isIRI()) {
            return RDFNode.namedNode(URI.create(o.stringValue()));
        } else if (o.isLiteral()) {
            final Literal lo = (Literal) o;
            if (lo.getLanguage().isPresent()) {
                return RDFNode.literal(lo.getLabel(), lo.getLanguage().get());
            } else if (lo.getDatatype() != null) {
                return RDFNode.literal(lo.getLabel(), URI.create((lo.getDatatype()).stringValue()));
            }
            return RDFNode.literal(lo.getLabel());
        } else {
            return RDFNode.blankNode(o.stringValue());
        }
    }
}
