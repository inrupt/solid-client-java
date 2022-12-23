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
package com.inrupt.commons.wrapping;

import java.net.URI;
import java.time.Instant;
import java.util.Objects;

import org.apache.commons.rdf.api.*;

/**
 * Common mappings from various values to RDF terms. For use in wrapping classes.
 *
 * @author Samu Lang
 */
public final class TermMappings {
    private static final String VALUE_REQUIRED = "Value must not be null";
    private static final String GRAPH_REQUIRED = "Graph must not be null";

    private static final RDF FACTORY = RDFFactory.getInstance();

    /**
     * Maps a string to a literal term.
     *
     * @param value the value to map
     * @param graph the graph that serves as the context for creating the term
     *
     * @return an xsd:string typed literal with the string as its lexical form
     *
     * @throws NullPointerException if the value is null
     * @throws NullPointerException if the model is null
     */
    public static Literal asStringLiteral(final String value, final Graph graph) {
        Objects.requireNonNull(value, VALUE_REQUIRED);
        Objects.requireNonNull(graph, GRAPH_REQUIRED);

        return FACTORY.createLiteral(value);
    }

    /**
     * Maps an IRI string to an IRI term.
     *
     * @param value the value to map
     * @param graph ignored
     *
     * @return an IRI term with the string as its identifier
     *
     * @throws NullPointerException if the value is null
     * @throws NullPointerException if the model is null
     */
    public static IRI asIri(final String value, final Graph graph) {
        Objects.requireNonNull(value, VALUE_REQUIRED);
        Objects.requireNonNull(graph, GRAPH_REQUIRED);

        return FACTORY.createIRI(value);
    }

    /**
     * Maps a URI to an IRI term.
     *
     * @param value the value to map
     * @param graph ignored
     *
     * @return an IRI term with the URI as its identifier
     *
     * @throws NullPointerException if the value is null
     * @throws NullPointerException if the model is null
     */
    public static IRI asIri(final URI value, final Graph graph) {
        Objects.requireNonNull(value, VALUE_REQUIRED);
        Objects.requireNonNull(graph, GRAPH_REQUIRED);

        return FACTORY.createIRI(value.toString());
    }

    /**
     * Maps a point in time to a literal term.
     *
     * @param value the value to map
     * @param graph ignored
     *
     * @return an xsd:dateTime typed literal term with the point in time as its lexical value
     *
     * @throws NullPointerException if the value is null
     * @throws NullPointerException if the model is null
     */
    public static Literal asTypedLiteral(final Instant value, final Graph graph) {
        Objects.requireNonNull(value, VALUE_REQUIRED);
        Objects.requireNonNull(graph, GRAPH_REQUIRED);

        return FACTORY.createLiteral(value.toString(), FACTORY.createIRI("http://www.w3.org/2001/XMLSchema#dateTime"));
    }

    /**
     * Maps a term to itself.
     *
     * @param value the value to map
     * @param graph ignored
     *
     * @return the same term
     *
     * @throws NullPointerException if the value is null
     * @throws NullPointerException if the model is null
     */
    public static RDFTerm identity(final RDFTerm value, final Graph graph) {
        Objects.requireNonNull(value, VALUE_REQUIRED);
        Objects.requireNonNull(graph, GRAPH_REQUIRED);

        return value;
    }

    private TermMappings() {
    }
}
