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

import java.util.stream.Stream;

/**
 * A simple RDF graph abstraction.
 */
public interface Graph {

    /**
     * Stream all matched triples.
     *
     * <p>Using {@code null} acts as a wildcard.
     *
     * @param subject the subject node. May be {@code null}
     * @param predicate the predicate node. May be {@code null}
     * @param object the object node. May be {@code null}
     * @return a stream of matched triples
     */
    Stream<Triple> stream(RDFNode subject, RDFNode predicate, RDFNode object);

    /**
     * Stream all triples from the graph.
     *
     * @return a stream of all triples
     */
    Stream<Triple> stream();

    /**
     * Add a triple to the graph.
     *
     * @param triple the triple
     */
    void add(Triple triple);
}
