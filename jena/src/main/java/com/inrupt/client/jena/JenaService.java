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

import static org.apache.jena.sparql.core.Quad.create;
import static org.apache.jena.sparql.core.Quad.defaultGraphIRI;

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
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RiotException;
import org.apache.jena.sparql.core.DatasetGraph;

/**
 * An {@link RdfService} that uses the Jena library.
 */
public class JenaService implements RdfService {

    private static final Map<Syntax, Lang> SYNTAX_TO_LANG = Map.of(
            Syntax.TURTLE, Lang.TURTLE,
            Syntax.TRIG, Lang.TRIG,
            Syntax.JSONLD, Lang.JSONLD,
            Syntax.NTRIPLES, Lang.NTRIPLES,
            Syntax.NQUADS, Lang.NQUADS);

    private static final Set<Syntax> SUPPORTS_QUADS = Set.of(Syntax.JSONLD, Syntax.TRIG, Syntax.NQUADS);

    @Override
    public void fromDataset(final Dataset dataset, final Syntax syntax, final OutputStream output) throws IOException {
        final var lang = Objects.requireNonNull(SYNTAX_TO_LANG.get(syntax));
        try {
            if (SUPPORTS_QUADS.contains(syntax)) {
                RDFDataMgr.write(output, asDatasetGraph(dataset), lang);
            } else {
                // Collapse all statements into a single union model
                final var jds = asDatasetGraph(dataset);
                final var union = ModelFactory.createUnion(
                        ModelFactory.createModelForGraph(jds.getDefaultGraph()),
                        ModelFactory.createModelForGraph(jds.getUnionGraph()));
                RDFDataMgr.write(output, union, lang);
            }
        } catch (final RiotException ex) {
            throw new IOException("Error serializing dataset", ex);
        }
    }

    static DatasetGraph asDatasetGraph(final Dataset dataset) {
        // If this is already a Jena dataset, just return that
        if (dataset instanceof JenaDataset) {
            return ((JenaDataset) dataset).asJenaDatasetGraph();
        }
        // Otherwise, copy the data into a structure that Jena understands
        final var dsgraph = DatasetFactory.create().asDatasetGraph();

        dataset.stream().forEach(quad -> {
            if (quad.getGraphName().isPresent()) {
                dsgraph.add(JenaUtils.toNode(quad.getGraphName().get()),
                        JenaUtils.toNode(quad.getSubject()),
                        JenaUtils.toNode(quad.getPredicate()),
                        JenaUtils.toNode(quad.getObject()));
            } else {
                dsgraph.getDefaultGraph().add(JenaUtils.toNode(quad.getSubject()),
                            JenaUtils.toNode(quad.getPredicate()),
                            JenaUtils.toNode(quad.getObject()));
            }
        });
        return dsgraph;
    }

    @Override
    public void fromGraph(final Graph graph, final Syntax syntax, final OutputStream output) throws IOException {
        final var lang = Objects.requireNonNull(SYNTAX_TO_LANG.get(syntax));
        try {
            if (graph instanceof JenaGraph) {
                RDFDataMgr.write(output, ((JenaGraph) graph).asJenaModel(), lang);
            } else {
                final var model = ModelFactory.createDefaultModel();
                graph.stream().forEach(triple ->
                    model.getGraph().add(JenaUtils.toNode(triple.getSubject()),
                            JenaUtils.toNode(triple.getPredicate()),
                            JenaUtils.toNode(triple.getObject())));
                RDFDataMgr.write(output, model, lang);
            }
        } catch (final RiotException ex) {
            throw new IOException("Error serializing graph", ex);
        }
    }

    @Override
    public Dataset toDataset(final Syntax syntax, final InputStream input, final String baseUri) throws IOException {
        final var lang = Objects.requireNonNull(SYNTAX_TO_LANG.get(syntax));
        final var dataset = DatasetFactory.create();
        try {
            RDFDataMgr.read(dataset, input, baseUri, lang);
        } catch (final RiotException ex) {
            throw new IOException("Error parsing dataset", ex);
        }

        return new JenaDataset(dataset.asDatasetGraph());
    }

    @Override
    public Graph toGraph(final Syntax syntax, final InputStream input, final String baseUri) throws IOException {
        final var lang = Objects.requireNonNull(SYNTAX_TO_LANG.get(syntax));
        final var model = ModelFactory.createDefaultModel();
        try {
            RDFDataMgr.read(model, input, baseUri, lang);
        } catch (final RiotException ex) {
            throw new IOException("Error parsing graph", ex);
        }

        return new JenaGraph(model);
    }

    @Override
    public Dataset createDataset() {
        final var dataset = DatasetFactory.create();
        return new JenaDataset(dataset.asDatasetGraph());
    }

    @Override
    public Graph createGraph() {
        final var model = ModelFactory.createDefaultModel();
        return new JenaGraph(model);
    }

    @Override
    public Triple createTriple(final RDFNode subject, final RDFNode predicate, final RDFNode object) {
        final var triple = org.apache.jena.graph.Triple.create(JenaUtils.toNode(subject),
                JenaUtils.toNode(predicate), JenaUtils.toNode(object));
        return new JenaTriple(triple);
    }

    @Override
    public Quad createQuad(final RDFNode subject, final RDFNode predicate, final RDFNode object,
            final RDFNode graphName) {
        if (graphName != null) {
            return new JenaQuad(create(JenaUtils.toNode(graphName),
                        JenaUtils.toNode(subject), JenaUtils.toNode(predicate), JenaUtils.toNode(object)));
        }
        return new JenaQuad(create(defaultGraphIRI, JenaUtils.toNode(subject),
                    JenaUtils.toNode(predicate), JenaUtils.toNode(object)));
    }
}

