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
import com.inrupt.client.rdf.Syntax;
import com.inrupt.client.spi.RdfProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Objects;

import org.eclipse.rdf4j.common.exception.RDF4JException;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

/**
 * An {@link RdfProcessor} that uses the RDF4J library.
 */
public class RDF4JRdfProcessor implements RdfProcessor {

    private static Map<Syntax, RDFFormat> SYNTAX_TO_FORMAT = Map.of(
            Syntax.TURTLE, RDFFormat.TURTLE,
            Syntax.TRIG, RDFFormat.TRIG,
            Syntax.JSONLD, RDFFormat.JSONLD,
            Syntax.NTRIPLES, RDFFormat.NTRIPLES,
            Syntax.NQUADS, RDFFormat.NQUADS);

    @Override
    public void fromDataset(final Dataset dataset, final Syntax syntax, final OutputStream output) throws IOException {
        final var format = Objects.requireNonNull(SYNTAX_TO_FORMAT.get(syntax));
        try {
            try (final RepositoryConnection conn = ((RDF4JDataset) dataset)
                    .asRDF4JRepository()
                    .getConnection()) {
                final var m = QueryResults.asModel(conn.getStatements(null, null, null));
                Rio.write(m, output, format);
            }
        } catch (final RDFHandlerException ex) {
            throw new IOException("Error serializing dataset", ex);
        }
    }

    @Override
    public void fromGraph(final Graph graph, final Syntax syntax, final OutputStream output) throws IOException {
        final var format = Objects.requireNonNull(SYNTAX_TO_FORMAT.get(syntax));
        final var writer = Rio.createWriter(format, output);
        try {
            writer.startRDF();
            for (final var st : ((RDF4JGraph) graph).asRDF4JModel()) {
                writer.handleStatement(st);
            }
            writer.endRDF();
        } catch (final RDFHandlerException ex) {
            throw new IOException("Error serializing graph", ex);
        }
    }

    @Override
    public Dataset toDataset(final Syntax syntax, final InputStream input, final String baseURI) throws IOException {
        final var format = Objects.requireNonNull(SYNTAX_TO_FORMAT.get(syntax));
        final var repository = new SailRepository(new MemoryStore());
        try {
            try (final var conn = repository.getConnection()) {
                conn.add(input, format); //if TTL, data is added to the 'null' graph
            }
            return new RDF4JDataset(repository);
        } catch (final RDF4JException ex) {
            throw new IOException("Error parsing dataset", ex);
        }
    }

    @Override
    public Graph toGraph(final Syntax syntax, final InputStream input, final String baseURI) throws IOException {
        final var format = Objects.requireNonNull(SYNTAX_TO_FORMAT.get(syntax));

        try {
            final var model = Rio.parse(input, format);
            return new RDF4JGraph(model);
        } catch (RDFParseException ex) {
            throw new IOException("Error parsing graph", ex);
        } catch (RDFHandlerException ex) {
            throw new IOException("Error parsing graph", ex);
        }
    }

}