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

import java.net.URI;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Objects;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Literal;
import org.apache.commons.rdf.api.RDFTerm;

/**
 * Common mappings from RDF nodes to other values. For use with {@link PredicateObjectSet} in wrapping classes.
 */
public final class ValueMappings {
    private static final String NODE_MUST_NOT_BE_NULL = "Node must not be null";

    /**
     * Maps a literal node to its lexical form.
     *
     * @param node the node to map
     *
     * @return the lexical form of the node as a string
     *
     * @throws NullPointerException if the node is null
     * @throws IllegalStateException if the node is not a literal
     */
    public static String literalAsString(final RDFTerm node) {
        Objects.requireNonNull(node, NODE_MUST_NOT_BE_NULL);

        if (!(node instanceof Literal)) {
            throw new IllegalStateException("Node is not literal");
        }

        return ((Literal) node).getLexicalForm();
    }

    /**
     * Maps a resource node to its IRI.
     *
     * @param node the node to convert
     *
     * @return the IRI of the node as a string
     *
     * @throws NullPointerException if the node is null
     * @throws IllegalStateException if the node is not an IRI
     */
    public static String iriAsString(final RDFTerm node) {
        Objects.requireNonNull(node, NODE_MUST_NOT_BE_NULL);

        if (!(node instanceof IRI)) {
            throw new IllegalStateException("Node is not IRI");
        }

        return ((IRI) node).getIRIString();
    }

    /**
     * Maps a resource node to its IRI.
     *
     * @param node the node to convert
     *
     * @return the IRI of the node as a string
     *
     * @throws NullPointerException if the node is null
     * @throws IllegalStateException if the node is not an IRI
     */
    public static URI iriAsUri(final RDFTerm node) {
        Objects.requireNonNull(node, NODE_MUST_NOT_BE_NULL);

        if (!(node instanceof IRI)) {
            throw new IllegalStateException("Node is not IRI");
        }

        return URI.create(((IRI) node).getIRIString());
    }

    /**
     * Maps a literal node to a point in time.
     *
     * @param node the node to map
     *
     * @return the point in time represented by the node's lexical form as an {@link Instant}
     *
     * @throws NullPointerException if the node is null
     * @throws IllegalStateException if the node is not a literal
     * @throws DateTimeParseException if the node's lexical form cannot be parsed as an {@link Instant}
     */
    public static Instant literalAsInstant(final RDFTerm node) {
        Objects.requireNonNull(node, NODE_MUST_NOT_BE_NULL);

        if (!(node instanceof Literal)) {
            throw new IllegalStateException("Node is not literal");
        }

        return Instant.parse(((Literal) node).getLexicalForm());
    }

    /**
     * Maps a literal node to an integer.
     *
     * @param node the node to map
     *
     * @return the integer represented by the node's lexical form or null if the form can not be parsed
     *
     * @throws NullPointerException if the node is null
     * @throws IllegalStateException if the node is not a literal
     */
    public static Integer literalAsIntegerOrNull(final RDFTerm node) {
        Objects.requireNonNull(node, NODE_MUST_NOT_BE_NULL);

        if (!(node instanceof Literal)) {
            throw new IllegalStateException("Node is not literal");
        }

        try {
            return Integer.parseInt(((Literal) node).getLexicalForm());

        } catch (NumberFormatException ignored) {
            // This helper method intentionally returns null when literal node value is not parsable as an integer.
        }

        return null;
    }

    private ValueMappings() {
    }
}
