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

import com.inrupt.client.rdf.Dataset;
import com.inrupt.client.rdf.Quad;
import com.inrupt.client.rdf.RDFNode;

import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF4J;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.Repository;

class RDF4JDataset implements Dataset {

    private final Repository repository;

    public RDF4JDataset(final Repository repository) {
        this.repository = repository;
    }

    public Repository asRDF4JRepository() {
        return repository;
    }

    @Override
    public Stream<Quad> stream(final Optional<RDFNode> graph, final RDFNode subject, final RDFNode predicate,
            final RDFNode object) {
        final var g = getGraph(graph);
        final var s = RDF4JGraph.getSubject(subject);
        final var p = RDF4JGraph.getPredicate(predicate);
        final var o = RDF4JGraph.getObject(object);

        try (final var conn = repository.getConnection()) {
            final var statements = conn.getStatements(s, p, o, g);
            final var model = QueryResults.asModel(statements);
            return model.stream().map(RDF4JQuad::new);
        }
    }

    @Override
    public Stream<Quad> stream() {
        try (final var conn = repository.getConnection()) {
            final var statements = conn.getStatements(null, null, null, RDF4J.NIL);
            final var model = QueryResults.asModel(statements);
            return model.stream().map(RDF4JQuad::new);
        }
    }

    private Resource getGraph(final Optional<RDFNode> graph) {
        final var factory = SimpleValueFactory.getInstance();
        if (graph.isPresent()) {
            if (graph.get().isLiteral()) {
                throw new IllegalArgumentException("Graph cannot be an RDF literal");
            }
            if (graph.get().isNamedNode()) {
                return factory.createIRI(graph.get().getURI().toString());
            }
            return RDF4J.NIL;
        }
        return null;
    }
}
