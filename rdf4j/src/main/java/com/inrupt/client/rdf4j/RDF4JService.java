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
import com.inrupt.client.rdf.Graph;
import com.inrupt.client.rdf.Quad;
import com.inrupt.client.rdf.RDFNode;
import com.inrupt.client.rdf.Syntax;
import com.inrupt.client.rdf.Triple;
import com.inrupt.client.spi.RdfService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.rdf4j.common.exception.RDF4JException;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.DynamicModelFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

/**
 * An {@link RdfService} that uses the RDF4J library.
 */
public class RDF4JService implements RdfService {

    private static final Map<Syntax, RDFFormat> SYNTAX_TO_FORMAT = buildSyntaxMapping();
    private static final ValueFactory VF = SimpleValueFactory.getInstance();

    @Override
    public void fromDataset(final Dataset dataset, final Syntax syntax, final OutputStream output) throws IOException {
        final RDFFormat format = Objects.requireNonNull(SYNTAX_TO_FORMAT.get(syntax));
        try {
            if (dataset instanceof RDF4JDataset) {
                try (final RepositoryConnection conn = ((RDF4JDataset) dataset)
                        .asRDF4JRepository()
                        .getConnection()) {
                    final Model m = QueryResults.asModel(conn.getStatements(null, null, null));
                    Rio.write(m, output, format);
                }
            } else {
                final Model m = new DynamicModelFactory().createEmptyModel();
                dataset.stream().forEach(quad -> {
                    if (quad.getGraphName().isPresent()) {
                        m.add(RDF4JUtils.fromSubject(quad.getSubject()), RDF4JUtils.fromPredicate(quad.getPredicate()),
                                RDF4JUtils.fromObject(quad.getObject()),
                                RDF4JUtils.fromSubject(quad.getGraphName().get()));
                    } else {
                        m.add(RDF4JUtils.fromSubject(quad.getSubject()), RDF4JUtils.fromPredicate(quad.getPredicate()),
                                RDF4JUtils.fromObject(quad.getObject()));
                    }
                });
                Rio.write(m, output, format);
            }
        } catch (final RDF4JException ex) {
            throw new IOException("Error serializing dataset", ex);
        }
    }



    @Override
    public void fromGraph(final Graph graph, final Syntax syntax, final OutputStream output) throws IOException {
        final RDFFormat format = Objects.requireNonNull(SYNTAX_TO_FORMAT.get(syntax));
        final RDFWriter writer = Rio.createWriter(format, output);
        try {
            writer.startRDF();
            if (graph instanceof RDF4JGraph) {
                for (final Statement st : ((RDF4JGraph) graph).asRDF4JModel()) {
                    writer.handleStatement(st);
                }
            } else {
                graph.stream().forEach(triple -> {
                    writer.handleStatement(VF.createStatement(RDF4JUtils.fromSubject(triple.getSubject()),
                                RDF4JUtils.fromPredicate(triple.getPredicate()),
                                RDF4JUtils.fromObject(triple.getObject())));
                });
            }
            writer.endRDF();
        } catch (final RDF4JException ex) {
            throw new IOException("Error serializing graph", ex);
        }
    }

    @Override
    public Dataset toDataset(final Syntax syntax, final InputStream input, final String baseUri) throws IOException {
        final RDFFormat format = Objects.requireNonNull(SYNTAX_TO_FORMAT.get(syntax));
        final Repository repository = new SailRepository(new MemoryStore());
        try {
            try (final RepositoryConnection conn = repository.getConnection()) {
                conn.add(input, baseUri, format);
            }
            return new RDF4JDataset(repository);
        } catch (final RDF4JException ex) {
            throw new IOException("Error parsing dataset", ex);
        }
    }

    @Override
    public Graph toGraph(final Syntax syntax, final InputStream input, final String baseUri) throws IOException {
        final RDFFormat format = Objects.requireNonNull(SYNTAX_TO_FORMAT.get(syntax));

        try {
            final Model model = Rio.parse(input, baseUri, format);
            return new RDF4JGraph(model);
        } catch (final RDF4JException ex) {
            throw new IOException("Error parsing graph", ex);
        }
    }

    @Override
    public Graph createGraph() {
        final Model m = new DynamicModelFactory().createEmptyModel();
        return new RDF4JGraph(m);
    }

    @Override
    public Dataset createDataset() {
        final Repository repository = new SailRepository(new MemoryStore());
        return new RDF4JDataset(repository);
    }

    @Override
    public Triple createTriple(final RDFNode subject, final RDFNode predicate, final RDFNode object) {
        final org.eclipse.rdf4j.model.Triple triple = VF.createTriple(RDF4JUtils.fromSubject(subject),
                RDF4JUtils.fromPredicate(predicate), RDF4JUtils.fromObject(object));
        return new RDF4JTriple(triple);
    }

    @Override
    public Quad createQuad(final RDFNode subject, final RDFNode predicate, final RDFNode object,
            final RDFNode graphName) {
        if (graphName != null) {
            return new RDF4JQuad(VF.createStatement(RDF4JUtils.fromSubject(subject),
                        RDF4JUtils.fromPredicate(predicate), RDF4JUtils.fromObject(object),
                        RDF4JUtils.fromSubject(graphName)));
        }
        return new RDF4JQuad(VF.createStatement(RDF4JUtils.fromSubject(subject), RDF4JUtils.fromPredicate(predicate),
                    RDF4JUtils.fromObject(object)));
    }

    static Map<Syntax, RDFFormat> buildSyntaxMapping() {
        final Map<Syntax, RDFFormat> mapping = new EnumMap<>(Syntax.class);
        mapping.put(Syntax.TURTLE, RDFFormat.TURTLE);
        mapping.put(Syntax.TRIG, RDFFormat.TRIG);
        mapping.put(Syntax.JSONLD, RDFFormat.JSONLD);
        mapping.put(Syntax.NTRIPLES, RDFFormat.NTRIPLES);
        mapping.put(Syntax.NQUADS, RDFFormat.NQUADS);
        return Collections.unmodifiableMap(mapping);
    }
}
