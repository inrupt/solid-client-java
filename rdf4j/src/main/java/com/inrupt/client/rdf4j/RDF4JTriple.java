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

import org.eclipse.rdf4j.model.Literal;

class RDF4JTriple implements Triple {

    private final org.eclipse.rdf4j.model.Triple triple;

    public RDF4JTriple(final org.eclipse.rdf4j.model.Triple triple) {
        this.triple = triple;
    }

    @Override
    public RDFNode getSubject() {
        final var s = triple.getSubject();
        if (s.isIRI()) {
            return RDFNode.namedNode(URI.create(s.stringValue()));
        }
        return RDFNode.blankNode();
    }

    @Override
    public RDFNode getPredicate() {
        final var p = triple.getPredicate();
        return RDFNode.namedNode(URI.create(p.stringValue()));
    }

    @Override
    public RDFNode getObject() {
        final var o = triple.getObject();
        if (o.isIRI()) {
            return RDFNode.namedNode(URI.create(o.stringValue()));
        } else if (o.isLiteral()) {
            final var lo = (Literal) o;
            if (lo.getLanguage().isPresent()) {
                return RDFNode.literal(lo.getLabel(), lo.getLanguage().get());
            } else if (lo.getDatatype() != null) {
                return RDFNode.literal(lo.getLabel(), URI.create((lo.getDatatype()).stringValue()));
            }
            return RDFNode.literal(lo.getLabel());
        } else {
            return RDFNode.blankNode();
        }
    }
}
