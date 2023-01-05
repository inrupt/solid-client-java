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
package com.inrupt.client.spi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.rdf.api.Dataset;
import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.RDFSyntax;

/**
 * A generic abstraction for interacting with different underlying RDF libraries.
 */
public interface RdfService {

    /**
     * Serialize a dataset to an output stream.
     *
     * @param dataset the dataset
     * @param syntax the concrete RDF syntax
     * @param output the output stream
     * @throws IOException when there is an error serializing the dataset
     */
    void fromDataset(Dataset dataset, RDFSyntax syntax, OutputStream output) throws IOException;

    /**
     * Serialize a graph to an output stream.
     *
     * @param graph the graph
     * @param syntax the concrete RDF syntax
     * @param output the output stream
     * @throws IOException when there is an error serializing the graph
     */
    void fromGraph(Graph graph, RDFSyntax syntax, OutputStream output) throws IOException;

    /**
     * Parse an input stream into a Dataset.
     *
     * @param syntax the concrete RDF syntax
     * @param input the input stream
     * @param baseUri the base URI to use in case of relative URIs, may be {@code null}
     * @return a dataset
     * @throws IOException when there is an error parsing the dataset
     */
    Dataset toDataset(RDFSyntax syntax, InputStream input, String baseUri) throws IOException;

    /**
     * Parse an input stream into a Graph.
     *
     * @param syntax the concrete RDF syntax
     * @param input the input stream
     * @param baseUri the base URI to use in case of relative URIs, may be {@code null}
     * @return a graph
     * @throws IOException when there is an error parsing the graph
     */
    Graph toGraph(RDFSyntax syntax, InputStream input, String baseUri) throws IOException;
}
