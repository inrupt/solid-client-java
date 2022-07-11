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

import static org.apache.jena.sparql.core.Quad.defaultGraphNodeGenerated;

import com.inrupt.client.rdf.Dataset;
import com.inrupt.client.rdf.Quad;
import com.inrupt.client.rdf.RDFNode;

import java.util.Optional;
import java.util.stream.Stream;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraph;

class JenaDataset implements Dataset {

    private final DatasetGraph dataset;

    public JenaDataset(final DatasetGraph dataset) {
        this.dataset = dataset;
    }

    public Stream<Quad> stream(final Optional<RDFNode> graphName, final RDFNode subject,
            final RDFNode predicate, final RDFNode object) {

        final var g = getGraphName(graphName);
        final var s = JenaGraph.getSubject(subject);
        final var p = JenaGraph.getPredicate(predicate);
        final var o = JenaGraph.getObject(object);

        final var iter = dataset.find(g, s, p, o);
        return Iter.asStream(iter).map(JenaQuad::new);
    }

    public Stream<Quad> stream() {
        final var iter = dataset.find();
        return Iter.asStream(iter).map(JenaQuad::new);
    }

    static Node getGraphName(final Optional<RDFNode> graphName) {
        if (graphName != null) {
            return graphName.map(JenaGraph::getSubject).orElse(defaultGraphNodeGenerated);
        }
        return Node.ANY;
    }

}
