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

import com.inrupt.client.spi.RdfService;
import com.inrupt.commons.rdf4j.RDF4J;
import com.inrupt.commons.rdf4j.RDF4JDataset;
import com.inrupt.commons.rdf4j.RDF4JGraph;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.rdf.api.Dataset;
import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.RDFSyntax;
import org.eclipse.rdf4j.common.exception.RDF4JException;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
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

    private static final Map<RDFSyntax, RDFFormat> SYNTAX_TO_FORMAT = buildSyntaxMapping();
    private static final RDF4J rdf = new RDF4J();

    @Override
    public void fromDataset(final Dataset dataset, final RDFSyntax syntax, final OutputStream output)
            throws IOException {
        final RDFFormat format = Objects.requireNonNull(SYNTAX_TO_FORMAT.get(syntax));
        try {
            try (final RepositoryConnection conn = toRdf4j(dataset).getConnection()) {
                final Model m = QueryResults.asModel(conn.getStatements(null, null, null));
                Rio.write(m, output, format);
            }
        } catch (final RDF4JException ex) {
            throw new IOException("Error serializing dataset", ex);
        }
    }

    @Override
    public void fromGraph(final Graph graph, final RDFSyntax syntax, final OutputStream output) throws IOException {
        final RDFFormat format = Objects.requireNonNull(SYNTAX_TO_FORMAT.get(syntax));
        final RDFWriter writer = Rio.createWriter(format, output);
        try {
            writer.startRDF();
            for (final Statement st : toRdf4j(graph)) {
                writer.handleStatement(st);
            }
            writer.endRDF();
        } catch (final RDF4JException ex) {
            throw new IOException("Error serializing graph", ex);
        }
    }

    static Model toRdf4j(final Graph graph) {
        if (graph instanceof RDF4JGraph) {
            final Optional<Model> model = ((RDF4JGraph) graph).asModel();
            if (model.isPresent()) {
                return model.get();
            }
        }
        final RDF4JGraph g = rdf.createGraph();
        graph.stream().forEach(g::add);
        return g.asModel()
            .orElseThrow(() -> new IllegalStateException("Could not adapt RDF4J graph"));
    }

    static Repository toRdf4j(final Dataset dataset) {
        if (dataset instanceof RDF4JDataset) {
            final Optional<Repository> repo = ((RDF4JDataset) dataset).asRepository();
            if (repo.isPresent()) {
                return repo.get();
            }
        }
        final RDF4JDataset d = rdf.createDataset();
        dataset.stream().forEach(d::add);
        return d.asRepository()
            .orElseThrow(() -> new IllegalStateException("Could not adapt RDF4J dataset"));
    }

    @Override
    public Dataset toDataset(final RDFSyntax syntax, final InputStream input, final String baseUri) throws IOException {
        final RDFFormat format = Objects.requireNonNull(SYNTAX_TO_FORMAT.get(syntax));
        final Repository repository = new SailRepository(new MemoryStore());
        try {
            try (final RepositoryConnection conn = repository.getConnection()) {
                conn.add(input, baseUri, format);
            }
            return rdf.asDataset(repository);
        } catch (final RDF4JException ex) {
            throw new IOException("Error parsing dataset", ex);
        }
    }

    @Override
    public Graph toGraph(final RDFSyntax syntax, final InputStream input, final String baseUri) throws IOException {
        final RDFFormat format = Objects.requireNonNull(SYNTAX_TO_FORMAT.get(syntax));

        try {
            final Model model = Rio.parse(input, baseUri, format);
            return rdf.asGraph(model);
        } catch (final RDF4JException ex) {
            throw new IOException("Error parsing graph", ex);
        }
    }

    static Map<RDFSyntax, RDFFormat> buildSyntaxMapping() {
        final Map<RDFSyntax, RDFFormat> mapping = new HashMap<>();
        mapping.put(RDFSyntax.TURTLE, RDFFormat.TURTLE);
        mapping.put(RDFSyntax.TRIG, RDFFormat.TRIG);
        mapping.put(RDFSyntax.JSONLD, RDFFormat.JSONLD);
        mapping.put(RDFSyntax.NTRIPLES, RDFFormat.NTRIPLES);
        mapping.put(RDFSyntax.NQUADS, RDFFormat.NQUADS);
        return Collections.unmodifiableMap(mapping);
    }
}
