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
package com.inrupt.client.rdf;

import com.inrupt.client.spi.RdfService;
import com.inrupt.client.spi.ServiceProvider;

/**
 * A class for creating RDF objects.
 */
public final class RDFFactory {

    private static final RdfService service = ServiceProvider.getRdfService();

    /**
     * Create an empty dataset.
     *
     * @return the dataset
     */
    public static Dataset createDataset() {
        return service.createDataset();
    }

    /**
     * Create an empty graph.
     *
     * @return the graph
     */
    public static Graph createGraph() {
        return service.createGraph();
    }

    /**
     * Create a triple.
     *
     * @param subject the subject
     * @param predicate the predicate
     * @param object the object
     * @return the triple
     */
    public static Triple createTriple(final RDFNode subject, final RDFNode predicate,
            final RDFNode object) {
        return service.createTriple(subject, predicate, object);
    }

    /**
     * Create a quad in the default graph.
     *
     * @param subject the subject
     * @param predicate the predicate
     * @param object the object
     * @return the quad
     */
    public static Quad createQuad(final RDFNode subject, final RDFNode predicate,
            final RDFNode object) {
        return createQuad(subject, predicate, object, null);
    }

    /**
     * Create a quad.
     *
     * @param subject the subject
     * @param predicate the predicate
     * @param object the object
     * @param graph the graph name, may be {@code null}
     * @return the quad
     */
    public static Quad createQuad(final RDFNode subject, final RDFNode predicate,
            final RDFNode object, final RDFNode graph) {
        return service.createQuad(subject, predicate, object, graph);
    }

    private RDFFactory() {
    }
}
