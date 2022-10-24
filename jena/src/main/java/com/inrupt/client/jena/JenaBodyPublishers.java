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

import com.inrupt.client.Request;
import com.inrupt.client.core.IOUtils;

import org.apache.jena.graph.Graph;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.update.UpdateRequest;

/**
 * {@link Request.BodyPublisher} implementations for use with Jena types.
 */
public final class JenaBodyPublishers {

    /**
     * Serialize a Jena Model as an HTTP request body.
     *
     * <p>This method uses the default (TURTLE) serialization.
     *
     * @param model the model
     * @return the body publisher
     */
    public static Request.BodyPublisher ofModel(final Model model) {
        return ofModel(model, Lang.TURTLE);
    }

    /**
     * Serialize a Jena Model as an HTTP request body.
     *
     * @param model the model
     * @param lang the serialization language
     * @return the body publisher
     */
    public static Request.BodyPublisher ofModel(final Model model, final Lang lang) {
        return IOUtils.buffer(out -> RDFDataMgr.write(out, model, lang));
    }

    /**
     * Serialize a Jena Graph as an HTTP request body.
     *
     * <p>This method uses the default (TURTLE) serialization.
     *
     * @param graph the graph
     * @return the body publisher
     */
    public static Request.BodyPublisher ofGraph(final Graph graph) {
        return ofGraph(graph, Lang.TURTLE);
    }

    /**
     * Serialize a Jena Graph as an HTTP request body.
     *
     * @param graph the graph
     * @param lang the serialization language
     * @return the body publisher
     */
    public static Request.BodyPublisher ofGraph(final Graph graph, final Lang lang) {
        return IOUtils.buffer(out -> RDFDataMgr.write(out, graph, lang));
    }

    /**
     * Serialize a Jena Dataset as an HTTP request body.
     *
     * <p>This method uses the default (TRIG) serialization.
     *
     * @param dataset the dataset
     * @return the body publisher
     */
    public static Request.BodyPublisher ofDataset(final Dataset dataset) {
        return ofDataset(dataset, Lang.TRIG);
    }

    /**
     * Serialize a Jena Dataset as an HTTP request body.
     *
     * @param dataset the dataset
     * @param lang the serialization language
     * @return the body publisher
     */
    public static Request.BodyPublisher ofDataset(final Dataset dataset, final Lang lang) {
        return IOUtils.buffer(out -> RDFDataMgr.write(out, dataset, lang));
    }

    /**
     * Serialize a Jena UpdateRequest (SPARQL-Update) as an HTTP request body.
     *
     * @param sparql the SPARQL Update request
     * @return the body publisher
     */
    public static Request.BodyPublisher ofUpdateRequest(final UpdateRequest sparql) {
        return Request.BodyPublishers.ofString(sparql.toString());
    }

    private JenaBodyPublishers() {
        // Prevent instantiation
    }
}

