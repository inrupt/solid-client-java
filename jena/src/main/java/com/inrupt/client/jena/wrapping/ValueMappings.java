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
package com.inrupt.client.jena.wrapping;

import java.net.URI;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Objects;

import org.apache.jena.enhanced.UnsupportedPolymorphismException;
import org.apache.jena.rdf.model.LiteralRequiredException;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceRequiredException;

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
     * @throws LiteralRequiredException if the node is not a literal
     */
    public static String literalAsString(final RDFNode node) {
        Objects.requireNonNull(node, NODE_MUST_NOT_BE_NULL);

        if (!node.isLiteral()) {
            throw new LiteralRequiredException(node);
        }

        return node.asLiteral().getLexicalForm();
    }

    /**
     * Maps a resource node to its IRI.
     *
     * @param node the node to convert
     *
     * @return the IRI of the node as a string
     *
     * @throws NullPointerException if the node is null
     * @throws ResourceRequiredException if the node is not a resource
     */
    public static String iriAsString(final RDFNode node) {
        Objects.requireNonNull(node, NODE_MUST_NOT_BE_NULL);

        if (!node.isURIResource()) {
            throw new ResourceRequiredException(node);
        }

        return node.asResource().getURI();
    }

    /**
     * Maps a resource node to its IRI.
     *
     * @param node the node to convert
     *
     * @return the IRI of the node as a string
     *
     * @throws NullPointerException if the node is null
     * @throws ResourceRequiredException if the node is not a resource
     */
    public static URI iriAsUri(final RDFNode node) {
        Objects.requireNonNull(node, NODE_MUST_NOT_BE_NULL);

        if (!node.isURIResource()) {
            throw new ResourceRequiredException(node);
        }

        return URI.create(node.asResource().getURI());
    }

    /**
     * Maps a literal node to a point in time.
     *
     * @param node the node to map
     *
     * @return the point in time represented by the node's lexical form as an {@link Instant}
     *
     * @throws NullPointerException if the node is null
     * @throws LiteralRequiredException if the node is not a literal
     * @throws DateTimeParseException if the node's lexical form cannot be parsed as an {@link Instant}
     */
    public static Instant literalAsInstant(final RDFNode node) {
        Objects.requireNonNull(node, NODE_MUST_NOT_BE_NULL);

        if (!node.isLiteral()) {
            throw new LiteralRequiredException(node);
        }

        return Instant.parse(node.asLiteral().getLexicalForm());
    }

    /**
     * A factory for mappings from a node to a view.
     *
     * @param view the target class to map to
     * @param <T> the type of the target class to map to
     *
     * @return a mapping that converts the node to the supplied implementation type
     *
     * @throws NullPointerException if the view is null
     * @see #as(RDFNode, Class) the returned mapping function (including exceptions it throws)
     */
    public static <T extends RDFNode> ValueMapping<T> as(final Class<T> view) {
        Objects.requireNonNull(view, "View must not be null");

        return node -> as(node, view);
    }

    /**
     * Maps a node to an instance of a view type.
     *
     * @param node the node to map
     * @param view the type to convert to
     * @param <T> the type of the target class to map to
     *
     * @return an instance of the view type representing the node
     *
     * @throws NullPointerException if the node is null
     * @throws UnsupportedPolymorphismException if the node cannot be converted to the view type
     */
    private static <T extends RDFNode> T as(final RDFNode node, final Class<T> view) {
        Objects.requireNonNull(node, NODE_MUST_NOT_BE_NULL);

        return node.as(view);
    }

    /**
     * Maps a literal node to an integer.
     *
     * @param node the node to map
     *
     * @return the integer represented by the node's lexical form or null if the form can not be parsed
     *
     * @throws NullPointerException if the node is null
     * @throws LiteralRequiredException if the node is not a literal
     */
    public static Integer literalAsIntegerOrNull(final RDFNode node) {
        Objects.requireNonNull(node, NODE_MUST_NOT_BE_NULL);

        if (!node.isLiteral()) {
            throw new LiteralRequiredException(node);
        }

        try {
            return Integer.parseInt(node.asLiteral().getLexicalForm());

        } catch (NumberFormatException ignored) {
            // This helper method intentionally returns null when literal node value is not parsable as an integer.
        }

        return null;
    }

    private ValueMappings() {
    }
}
