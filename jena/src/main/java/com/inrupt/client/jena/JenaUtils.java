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

import java.net.URI;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

final class JenaUtils {

    static Node toNode(final RDFNode node) {
        if (node.isNamedNode()) {
            return NodeFactory.createURI(node.getURI().toString());
        } else if (node.isLiteral()) {
            final String lang = node.getLanguage();
            if (lang != null) {
                return NodeFactory.createLiteral(node.getLiteral(), lang);
            }

            final URI datatype = node.getDatatype();
            if (datatype != null) {
                return NodeFactory.createLiteral(node.getLiteral(),
                        NodeFactory.getType(datatype.toString()));
            }

            return NodeFactory.createLiteral(node.getLiteral());
        }

        final String label = node.getNodeId();
        if (label != null) {
            return NodeFactory.createBlankNode(label);
        }
        return NodeFactory.createBlankNode();
    }

    private JenaUtils() {
    }
}
