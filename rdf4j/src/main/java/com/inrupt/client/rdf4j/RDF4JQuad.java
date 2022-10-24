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

import com.inrupt.client.Quad;
import com.inrupt.client.RDFNode;

import java.net.URI;
import java.util.Optional;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.RDF4J;

/**
 * The RDF4J implementation of a {@link Quad}.
 */
class RDF4JQuad extends RDF4JTriple implements Quad {

    private final Resource graphName;

    /**
     * Create a RDF4JQuad.
     *
     * @param statement the RDF4J {@link Statement}
     */
    public RDF4JQuad(final Statement statement) {
        super(Values.triple(statement));
        this.graphName = getContext(statement);
    }

    /**
     * Retrieve the {@link RDFNode} graph name.
     *
     * @return the {@link RDFNode} graph name from {@code graphName}
     */
    @Override
    public Optional<RDFNode> getGraphName() {
        if (graphName != null && graphName.isBNode()) {
            final String nodeId = ((BNode)graphName).getID();
            return Optional.of(RDFNode.blankNode(nodeId));
        }
        return Optional.ofNullable(graphName).map(Resource::stringValue)
                .map(URI::create).map(RDFNode::namedNode);
    }

    static Resource getContext(final Statement statement) {
        final Resource ctx = statement.getContext();
        if (ctx == null || RDF4J.NIL.stringValue().equals(ctx.stringValue())) {
            return null;
        }
        return ctx;
    }
}
