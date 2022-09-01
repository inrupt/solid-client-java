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
package com.inrupt.client.jena;

import com.inrupt.client.rdf.RDFNode;
import com.inrupt.client.rdf.Triple;

import java.net.URI;

/**
 * The Jena implementation of a {@link Triple}.
 */
class JenaTriple implements Triple {

    private final org.apache.jena.graph.Triple triple;

    /**
     * Create a JenaTriple.
     *
     * @param triple the Jena {@link org.apache.jena.graph.Triple}
     */
    public JenaTriple(final org.apache.jena.graph.Triple triple) {
        this.triple = triple;
    }

    /**
     * Retrieve the {@link RDFNode} subject.
     *
     * @return the {@link RDFNode} subject from the {@code triple}
     */
    @Override
    public RDFNode getSubject() {
        final var s = triple.getSubject();
        if (s.isURI()) {
            return RDFNode.namedNode(URI.create(s.getURI()));
        }
        return RDFNode.blankNode();
    }

    /**
     * Retrieve the {@link RDFNode} predicate.
     *
     * @return the {@link RDFNode} predicate from the {@code triple}
     */
    @Override
    public RDFNode getPredicate() {
        final var p = triple.getPredicate();
        return RDFNode.namedNode(URI.create(p.getURI()));

    }

    /**
     * Retrieve the {@link RDFNode} object.
     *
     * @return the {@link RDFNode} object from the {@code triple}
     */
    @Override
    public RDFNode getObject() {
        final var o = triple.getObject();
        if (o.isURI()) {
            return RDFNode.namedNode(URI.create(o.getURI()));
        } else if (o.isLiteral()) {
            if (o.getLiteralLanguage() != null && !o.getLiteralLanguage().isBlank()) {
                return RDFNode.literal(o.getLiteralLexicalForm(), o.getLiteralLanguage());
            } else if (o.getLiteralDatatypeURI() != null) {
                return RDFNode.literal(o.getLiteralLexicalForm(), URI.create(o.getLiteralDatatypeURI()));
            }
            return RDFNode.literal(o.getLiteralLexicalForm());
        } else {
            return RDFNode.blankNode();
        }
    }
}
