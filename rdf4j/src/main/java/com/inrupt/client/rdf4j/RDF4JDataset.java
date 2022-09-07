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
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryResult;

/**
 * The RDF4J implementation of a {@link Dataset}.
 */
class RDF4JDataset implements Dataset {

    private final Repository repository;

    /**
     * Create a RDF4JDataset.
     *
     * @param repository the RDF4J {@link Repository}
     */
    public RDF4JDataset(final Repository repository) {
        this.repository = repository;
    }

    /**
     * Return the RDF4JDataset {@code repository} value.
     *
     * @return the repository as a RDF4J {@link Repository}
     */
    public Repository asRDF4JRepository() {
        return repository;
    }

    /**
     * Return the matching sequential stream of Quads with this RDF4JDataset as its source.
     *
     * @param graph the RDFNode graph, may be {@code null}
     * @param subject the RDFNode subject, may be {@code null}
     * @param predicate the RDFNode predicate, may be {@code null}
     * @param object the RDFNode object, may be {@code null}, may be {@code null}
     * @return the matching quads as a sequential {@link Stream} of {@link Quad}s
     */
    @Override
    public Stream<Quad> stream(final Optional<RDFNode> graph, final RDFNode subject, final RDFNode predicate,
            final RDFNode object) {
        final var g = getGraph(graph);
        final var s = RDF4JGraph.getSubject(subject);
        final var p = RDF4JGraph.getPredicate(predicate);
        final var o = RDF4JGraph.getObject(object);

        try (final var conn = repository.getConnection()) {
            final RepositoryResult<Statement> statements;
            if (g == null) {
                //retrieves all statements in the repository regardless of context
                statements = conn.getStatements(s, p, o);
            } else {
                statements = conn.getStatements(s, p, o, g);
            }
            final var model = QueryResults.asModel(statements);
            statements.close();
            return model.stream().map(RDF4JQuad::new);
        }
    }

    /**
     * Return a sequential stream of Quads with this RDF4JDataset as its source.
     *
     * @return a sequential {@link Stream} of {@link Quad}
     */
    @Override
    public Stream<Quad> stream() {
        try (final var conn = repository.getConnection()) {
            try (final var statements = conn.getStatements(null, null, null)) {
                final var model = QueryResults.asModel(statements);
                return model.stream().map(RDF4JQuad::new);
            }
        }
    }

    /**
     * Retrieve the RDF4J graph from a RDFNode graph.
     *
     * @return the graph as a RDF4J {@link Resource}, may be {@code null}
     */
    static Resource getGraph(final Optional<RDFNode> graph) {
        if (graph != null) {
            if (graph.isEmpty()) return null;
            if (graph.isPresent()) {
                if (graph.get().isLiteral()) {
                    throw new IllegalArgumentException("Graph cannot be an RDF literal");
                }
                if (graph.get().isNamedNode()) {
                    return SimpleValueFactory.getInstance().createIRI(graph.get().getURI().toString());
                }
                if (graph.get().isBlankNode()) {
                    //TODO add blank node creation
                    return null;
                }
            }
        }
        return null;
    }
}
