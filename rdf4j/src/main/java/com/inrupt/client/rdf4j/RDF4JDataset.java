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

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;

/**
 * The RDF4J implementation of a {@link Dataset}.
 */
class RDF4JDataset implements Dataset {

    private static final ValueFactory VF = SimpleValueFactory.getInstance();

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
        final Resource[] c = getContexts(graph);
        final Resource s = RDF4JGraph.getSubject(subject);
        final IRI p = RDF4JGraph.getPredicate(predicate);
        final Value o = RDF4JGraph.getObject(object);

        try (final RepositoryConnection conn = repository.getConnection();
                final RepositoryResult<Statement> statements = conn.getStatements(s, p, o, c)) {
            return QueryResults.asModel(statements).stream().map(RDF4JQuad::new);
        }
    }

    @Override
    public void add(final Quad quad) {
        try (final RepositoryConnection conn = repository.getConnection()) {
            if (quad.getGraphName().isPresent()) {
                conn.add(VF.createStatement(RDF4JUtils.fromSubject(quad.getSubject()),
                            RDF4JUtils.fromPredicate(quad.getPredicate()),
                            RDF4JUtils.fromObject(quad.getObject()),
                            RDF4JUtils.fromSubject(quad.getGraphName().get())));
            } else {
                conn.add(VF.createStatement(RDF4JUtils.fromSubject(quad.getSubject()),
                            RDF4JUtils.fromPredicate(quad.getPredicate()),
                            RDF4JUtils.fromObject(quad.getObject())));
            }
        }
    }

    @Override
    public void remove(final Optional<RDFNode> graphName, final RDFNode subject, final RDFNode predicate,
            final RDFNode object) {
        try (final RepositoryConnection conn = repository.getConnection()) {
            conn.remove(RDF4JUtils.fromSubject(subject), RDF4JUtils.fromPredicate(predicate),
                    RDF4JUtils.fromObject(object),
                    getContexts(graphName));
        }
    }

    /**
     * Retrieve the RDF4J context from a RDFNode graph.
     *
     * @param graph the RDFNode graph, may be {@code null}
     * @return the context as an Array of RDF4J {@link Resource}s
     */
    static Resource[] getContexts(final Optional<RDFNode> graph) {
        if (graph != null) {
            if (graph.isPresent()) {
                if (graph.get().isLiteral()) {
                    throw new IllegalArgumentException("Graph cannot be an RDF literal");
                }
                if (graph.get().isNamedNode()) {
                    return new Resource[] {
                        VF.createIRI(graph.get().getURI().toString())
                    };
                }
                if (graph.get().isBlankNode()) {
                    return new Resource[] {
                        VF.createBNode(graph.get().getNodeId())
                    };
                }
            } else {
                // a non-null but empty graph means the RDF4J default graph
                return new Resource[] { null };
            }
        }
        // a null graph means any context
        return new Resource[0];
    }
}
