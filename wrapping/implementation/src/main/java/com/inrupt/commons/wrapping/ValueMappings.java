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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Objects;

import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Literal;
import org.apache.commons.rdf.api.RDFTerm;

/**
 * Common mappings from RDF terms to other values. For use in wrapping classes.
 *
 * @author Samu Lang
 */
public final class ValueMappings {
    private static final String TERM_REQUIRED = "Term must not be null";
    private static final String GRAPH_REQUIRED = "Graph must not be null";
    private static final String TERM_IS_NOT_LITERAL = "Term is not literal";

    /**
     * Maps a literal term to its lexical form.
     *
     * @param term the term to map
     * @param graph ignored
     *
     * @return the lexical form of the term as a string
     *
     * @throws NullPointerException if the term is null
     * @throws IllegalStateException if the term is not a literal
     */
    public static String literalAsString(final RDFTerm term, final Graph graph) {
        Objects.requireNonNull(term, TERM_REQUIRED);
        Objects.requireNonNull(graph, GRAPH_REQUIRED);

        if (!(term instanceof Literal)) {
            // TODO: Throw specific exception
            throw new IllegalStateException(TERM_IS_NOT_LITERAL);
        }

        return ((Literal) term).getLexicalForm();
    }

    /**
     * Maps an IRI term to a {@code URI} created from its IRI string.
     *
     * @param term the term to convert
     * @param graph ignored
     *
     * @return the IRI of the term as a URI
     *
     * @throws NullPointerException if the term is null
     * @throws IllegalStateException if the term is not an IRI
     */
    public static URI iriAsUri(final RDFTerm term, final Graph graph) {
        Objects.requireNonNull(term, TERM_REQUIRED);
        Objects.requireNonNull(graph, GRAPH_REQUIRED);

        if (!(term instanceof IRI)) {
            // TODO: Throw specific exception
            throw new IllegalStateException("Term is not IRI");
        }

        return URI.create(((IRI) term).getIRIString());
    }

    /**
     * Maps an IRI term to its IRI string.
     *
     * @param term the term to convert
     * @param graph ignored
     *
     * @return the IRI of the term as a string
     *
     * @throws NullPointerException if the term is null
     * @throws IllegalStateException if the term is not an IRI
     */
    public static String iriAsString(final RDFTerm term, final Graph graph) {
        Objects.requireNonNull(term, TERM_REQUIRED);
        Objects.requireNonNull(graph, GRAPH_REQUIRED);

        if (!(term instanceof IRI)) {
            // TODO: Throw specific exception
            throw new IllegalStateException("Term is not IRI");
        }

        return ((IRI) term).getIRIString();
    }

    /**
     * Maps a literal term to a point in time.
     *
     * @param term the term to map
     * @param graph ignored
     *
     * @return the point in time represented by the term's lexical form as an {@link Instant}
     *
     * @throws NullPointerException if the term is null
     * @throws IllegalStateException if the term is not a literal
     * @throws DateTimeParseException if the term's lexical form cannot be parsed as an {@link Instant}
     */
    public static Instant literalAsInstant(final RDFTerm term, final Graph graph) {
        Objects.requireNonNull(term, TERM_REQUIRED);
        Objects.requireNonNull(graph, GRAPH_REQUIRED);

        if (!(term instanceof Literal)) {
            // TODO: Throw specific exception
            throw new IllegalStateException(TERM_IS_NOT_LITERAL);
        }

        return Instant.parse(((Literal) term).getLexicalForm());
    }

    /**
     * Maps a literal term to a boolean.
     *
     * @param term the term to map
     * @param graph ignored
     *
     * @return the boolean represented by the term's lexical form as a {@link boolean}
     *
     * @throws NullPointerException if the term is null
     * @throws IllegalStateException if the term is not a literal
     */
    public static boolean literalAsBoolean(final RDFTerm term, final Graph graph) {
        Objects.requireNonNull(term, TERM_REQUIRED);
        Objects.requireNonNull(graph, GRAPH_REQUIRED);

        if (!(term instanceof Literal)) {
            // TODO: Throw specific exception
            throw new IllegalStateException(TERM_IS_NOT_LITERAL);
        }

        return Boolean.parseBoolean(((Literal) term).getLexicalForm());
    }

    /**
     * Maps a literal term to an integer or null.
     *
     * @param term the term to map
     * @param graph ignored
     *
     * @return the integer represented by the term's lexical form or null if the form can not be parsed
     *
     * @throws NullPointerException if the term is null
     * @throws IllegalStateException if the term is not a literal
     */
    public static Integer literalAsIntegerOrNull(final RDFTerm term, final Graph graph) {
        Objects.requireNonNull(term, TERM_REQUIRED);
        Objects.requireNonNull(graph, GRAPH_REQUIRED);

        if (!(term instanceof Literal)) {
            // TODO: Throw specific exception
            throw new IllegalStateException("Node is not literal");
        }

        try {
            return Integer.parseInt(((Literal) term).getLexicalForm());

        } catch (NumberFormatException ignored) {
            // This helper method intentionally returns null when lexical form is not parsable as an integer.
        }

        return null;
    }

    /**
     * A factory for mappings from a term to a view.
     *
     * @param view the target class to map to
     * @param <T> the type of the target class to map to
     *
     * @return a mapping that converts the term to the supplied implementation type
     *
     * @throws NullPointerException if the view is null
     * @see #as(RDFTerm, Graph, Class) the returned mapping function (including exceptions it throws)
     */
    // TODO: Document constructor requirements
    public static <T extends RDFTerm> ValueMapping<T> as(final Class<T> view) {
        Objects.requireNonNull(view, "View must not be null");

        return (term, graph) -> as(term, graph, view);
    }

    /**
     * Maps a term to an instance of a view type.
     *
     * @param term the term to map
     * @param graph the graph that will contain the converted term
     * @param view the type to convert to
     * @param <T> the type of the target class to map to
     *
     * @return an instance of the view type representing the term
     *
     * @throws NullPointerException if the term is null
     * @throws IllegalStateException if the view type does not contain an appropriate public constructor
     * @throws IllegalStateException if the view type cannot be instantiated
     */
    private static <T extends RDFTerm> T as(final RDFTerm term, final Graph graph, final Class<T> view) {
        Objects.requireNonNull(term, TERM_REQUIRED);

        final Constructor<T> constructor = find(view, RDFTerm.class, Graph.class);
        if (constructor != null) {
            return instantiate(constructor, term, graph);
        }

        final Constructor<T> constructor2 = find(view, RDFTerm.class);
        if (constructor2 != null) {
            return instantiate(constructor2, term);
        }

        throw new IllegalStateException("No constructor found with parameter types (RDFTerm, Graph) or (RDFTerm)");
    }

    private static <T> Constructor<T> find(final Class<T> view, final Class<?>... parameterTypes) {
        try {
            return view.getConstructor(parameterTypes);
        } catch (NoSuchMethodException | SecurityException e) {
            return null;
        }
    }

    private static <T> T instantiate(final Constructor<T> constructor, final Object... initargs) {
        try {
            return constructor.newInstance(initargs);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Could not instantiate wrapping class", e);
        }
    }

    private ValueMappings() {
    }
}
