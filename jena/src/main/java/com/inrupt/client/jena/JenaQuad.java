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

import com.inrupt.client.Quad;
import com.inrupt.client.RDFNode;

import java.net.URI;
import java.util.Optional;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Node_Blank;

/**
 * The Jena implementation of a {@link Quad}.
 */
class JenaQuad extends JenaTriple implements Quad {

    private final Node graphName;

    /**
     * Create a JenaQuad.
     *
     * @param quad the Jena {@link org.apache.jena.sparql.core.Quad}
     */
    public JenaQuad(final org.apache.jena.sparql.core.Quad quad) {
        super(quad.asTriple());
        this.graphName = getGraphName(quad);
    }

    /**
     * Retrieve the {@link RDFNode} graph name.
     *
     * @return the {@link RDFNode} graph name from {@code graphName}
     */
    @Override
    public Optional<RDFNode> getGraphName() {
        if (graphName != null && graphName.isBlank()) {
            final var nodeId = ((Node_Blank)graphName).getBlankNodeId().getLabelString();
            return Optional.of(RDFNode.blankNode(nodeId));
        }
        return Optional.ofNullable(graphName).map(Node::getURI).map(URI::create).map(RDFNode::namedNode);
    }

    static Node getGraphName(final org.apache.jena.sparql.core.Quad quad) {
        if (quad.isDefaultGraph()) {
            return null;
        }
        return quad.getGraph();
    }
}
