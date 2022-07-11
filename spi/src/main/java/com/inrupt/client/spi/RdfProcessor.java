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
package com.inrupt.client.spi;

import com.inrupt.client.rdf.Dataset;
import com.inrupt.client.rdf.Graph;
import com.inrupt.client.rdf.Syntax;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * A generic abstraction for interacting with different underlying RDF libraries.
 */
public interface RdfProcessor {

    /**
     * Serialize a dataset to an output stream.
     *
     * @param dataset the dataset
     * @param syntax the concrete RDF syntax
     * @param output the output stream
     */
    void fromDataset(Dataset dataset, Syntax syntax, OutputStream output);

    /**
     * Serialize a graph to an output stream.
     *
     * @param graph the graph
     * @param syntax the concrete RDF syntax
     * @param output the output stream
     */
    void fromGraph(Graph graph, Syntax syntax, OutputStream output);

    /**
     * Parse an input stream into a Dataset.
     *
     * @param syntax the concrete RDF syntax
     * @param input the input stream
     * @return a dataset
     */
    Dataset toDataset(Syntax syntax, InputStream input);

    /**
     * Parse an input stream into a Graph.
     *
     * @param syntax the concrete RDF syntax
     * @param input the input stream
     * @return a graph
     */
    Graph toGraph(Syntax syntax, InputStream input);
}
