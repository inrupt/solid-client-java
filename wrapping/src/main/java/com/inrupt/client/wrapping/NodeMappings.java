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
package com.inrupt.client.wrapping;

import com.inrupt.client.spi.RDFFactory;

import java.net.URI;
import java.time.Instant;
import java.util.Objects;

import org.apache.commons.rdf.api.*;

/**
 * Common mappings from various values to RDF nodes. For use with {@link PredicateObjectSet} in wrapping classes.
 */
public final class NodeMappings {
    private static final String VALUE_REQUIRED = "Value must not be null";
    private static final String MODEL_REQUIRED = "Model must not be null";

    private static final RDF FACTORY = RDFFactory.getInstance();

    /**
     * Maps a string to a literal node.
     *
     * @param value the value to map
     * @param model the graph used to create the node
     *
     * @return an xsd:string typed literal node with the string as its lexical value
     *
     * @throws NullPointerException if the value is null
     * @throws NullPointerException if the model is null
     */
    public static Literal asStringLiteral(final String value, final Graph model) {
        Objects.requireNonNull(value, VALUE_REQUIRED);
        Objects.requireNonNull(model, MODEL_REQUIRED);

        return FACTORY.createLiteral(value);
    }

    /**
     * Maps an IRI string to an IRI node.
     *
     * @param value the value to map
     * @param model the graph used to create the node
     *
     * @return an IRI node with the string as its identifier
     *
     * @throws NullPointerException if the value is null
     * @throws NullPointerException if the model is null
     */
    public static IRI asIriResource(final String value, final Graph model) {
        Objects.requireNonNull(value, VALUE_REQUIRED);
        Objects.requireNonNull(model, MODEL_REQUIRED);

        return FACTORY.createIRI(value);
    }

    /**
     * Maps a URI to an IRI node.
     *
     * @param value the value to map
     * @param model the graph used to create the node
     *
     * @return an IRI node with the URI as its identifier
     *
     * @throws NullPointerException if the value is null
     * @throws NullPointerException if the model is null
     */
    public static IRI asIriResource(final URI value, final Graph model) {
        Objects.requireNonNull(value, VALUE_REQUIRED);
        Objects.requireNonNull(model, MODEL_REQUIRED);

        return FACTORY.createIRI(value.toString());
    }

    /**
     * Maps a point in time to a literal node.
     *
     * @param value the value to map
     * @param model the graph used to create the node
     *
     * @return an xsd:dateTime typed literal node with the point in time as its lexical value
     *
     * @throws NullPointerException if the value is null
     * @throws NullPointerException if the model is null
     */
    public static Literal asTypedLiteral(final Instant value, final Graph model) {
        Objects.requireNonNull(value, VALUE_REQUIRED);
        Objects.requireNonNull(model, MODEL_REQUIRED);

        return FACTORY.createLiteral(value.toString(), FACTORY.createIRI("http://www.w3.org/2001/XMLSchema#dateTime"));
    }

    /**
     * Maps a node to itself.
     *
     * @param value the value to map
     * @param model the graph used to create the node
     *
     * @return the same node
     *
     * @throws NullPointerException if the value is null
     * @throws NullPointerException if the model is null
     */
    public static RDFTerm identity(final RDFTerm value, final Graph model) {
        Objects.requireNonNull(value, VALUE_REQUIRED);
        Objects.requireNonNull(model, MODEL_REQUIRED);

        return value;
    }

    private NodeMappings() {
    }
}
