/*
 * Copyright 2023 Inrupt Inc.
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

import com.inrupt.client.spi.RdfService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.rdf.api.Dataset;
import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.RDFSyntax;
import org.apache.jena.commonsrdf.JenaCommonsRDF;
import org.apache.jena.graph.Factory;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RiotException;
import org.apache.jena.sparql.core.DatasetGraphFactory;

/**
 * An {@link RdfService} that uses the Jena library.
 */
public class JenaService implements RdfService {

    @Override
    public void fromDataset(final Dataset dataset, final RDFSyntax syntax, final OutputStream output)
            throws IOException {
        final var lang = JenaCommonsRDF.toJena(syntax).orElseThrow(() ->
                new IllegalArgumentException("Unsupported syntax: " + syntax.title()));
        try {
            if (syntax.supportsDataset()) {
                RDFDataMgr.write(output, JenaCommonsRDF.toJena(dataset), lang);
            } else {
                final var dsg = JenaCommonsRDF.toJena(dataset);
                final var union = ModelFactory.createUnion(
                        ModelFactory.createModelForGraph(dsg.getDefaultGraph()),
                        ModelFactory.createModelForGraph(dsg.getUnionGraph()));
                RDFDataMgr.write(output, union, lang);
            }
        } catch (final RiotException ex) {
            throw new IOException("Error serializing dataset", ex);
        }
    }

    @Override
    public void fromGraph(final Graph graph, final RDFSyntax syntax, final OutputStream output) throws IOException {
        final var lang = JenaCommonsRDF.toJena(syntax).orElseThrow(() ->
                new IllegalArgumentException("Unsupported syntax: " + syntax.title()));
        try {
            RDFDataMgr.write(output, JenaCommonsRDF.toJena(graph), lang);
        } catch (final RiotException ex) {
            throw new IOException("Error serializing graph", ex);
        }
    }

    @Override
    public Dataset toDataset(final RDFSyntax syntax, final InputStream input, final String baseUri) throws IOException {
        final var lang = JenaCommonsRDF.toJena(syntax).orElseThrow(() ->
                new IllegalArgumentException("Unsupported syntax: " + syntax.title()));
        final var dataset = DatasetGraphFactory.createTxnMem();
        try {
            RDFDataMgr.read(dataset, input, baseUri, lang);
        } catch (final RiotException ex) {
            throw new IOException("Error parsing dataset", ex);
        }

        return JenaCommonsRDF.fromJena(dataset);
    }

    @Override
    public Graph toGraph(final RDFSyntax syntax, final InputStream input, final String baseUri) throws IOException {
        final var lang = JenaCommonsRDF.toJena(syntax).orElseThrow(() ->
                new IllegalArgumentException("Unsupported syntax: " + syntax.title()));
        final var graph = Factory.createDefaultGraph();
        try {
            RDFDataMgr.read(graph, input, baseUri, lang);
        } catch (final RiotException ex) {
            throw new IOException("Error parsing graph", ex);
        }
        return JenaCommonsRDF.fromJena(graph);
    }
}

