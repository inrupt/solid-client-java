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

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toSet;

import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.rdf.api.*;

/**
 * A wrapper for IRI and blank node terms which contains methods that aid authoring wrapping classes.
 *
 * <p>All methods require a predicate and a value mapping function.
 *
 * <p>This table details the behavior of singular getter helper methods depending on the number of matching statements
 * in the underlying graph:
 * <pre>
 * ┌───────────────┬───────┬────────┬────────┐
 * │               │   0   │   1    │   &gt;1   │
 * ├───────────────┼───────┼────────┼────────┤
 * │ anyOrNull     │ null  │ single │ random │
 * │ anyOrThrow    │ throw │ single │ random │
 * │ singleOrNull  │ null  │ single │ throw  │
 * │ singleOrThrow │ throw │ single │ throw  │
 * └───────────────┴───────┴────────┴────────┘
 * </pre>
 *
 * <p>This table details the behavior of plural getter helper methods in terms of reflecting changes to the underlying
 * graph after calling them:
 * <pre>
 * ┌──────────┬─────────┐
 * │ iterator │ static  │
 * │ snapshot │ static  │
 * │ live     │ dynamic │
 * └──────────┴─────────┘
 * </pre>
 *
 * <p>This table details the behavior of setter helper methods in terms of effect on existing statements in the
 * underlying graph and given values:
 * <pre>
 * ┌───────────────────┬──────────┬────────────┐
 * │                   │ existing │ null value │
 * ├───────────────────┼──────────┼────────────┤
 * │ overwrite         │ remove   │ throw      │
 * │ overwriteNullable │ remove   │ ignore     │
 * │ add               │ leave    │ throw      │
 * └───────────────────┴──────────┴────────────┘
 * </pre>
 *
 * @author Samu Lang
 */
public abstract class WrapperBlankNodeOrIRI implements BlankNodeOrIRI {
    // TODO: Document
    protected final Graph graph;
    // TODO: Document
    protected final RDF rdf = RDFFactory.getInstance();

    // TODO: Document
    protected WrapperBlankNodeOrIRI(final Graph graph) {
        Objects.requireNonNull(graph, "Graph is required");

        this.graph = graph;
    }

    /**
     * A converting singular getter helper for expected cardinality {@code 0..1} that ignores overflow.
     *
     * @param p the predicate to query
     * @param m the mapping applied to result terms
     * @param <T> the type of values returned the type of values returned
     *
     * @return the converted object of an arbitrary statement with this subject and the given predicate or null if there
     * are no such statements
     */
    protected <T> T anyOrNull(final IRI p, final ValueMapping<T> m) {
        Objects.requireNonNull(p);
        Objects.requireNonNull(m);

        return stream(p, m).findAny().orElse(null);
    }

    /**
     * A converting singular getter helper for expected cardinality {@code 1..1} that ignores overflow.
     *
     * @param p the predicate to query
     * @param m the mapping applied to result terms
     * @param <T> the type of values returned
     *
     * @return the converted object of an arbitrary statement with this subject and the given predicate
     *
     * @throws IllegalStateException if there are no such statements
     */
    protected <T> T anyOrThrow(final IRI p, final ValueMapping<T> m) {
        Objects.requireNonNull(p);
        Objects.requireNonNull(m);

        return atLeastOne(p, m).next();
    }

    /**
     * A converting singular getter helper for expected cardinality {@code 0..1} that forbids overflow.
     *
     * @param p the predicate to query
     * @param m the mapping applied to result terms
     * @param <T> the type of values returned
     *
     * @return the converted object of the only statement with this subject and the given predicate, or null if there is
     * no such statement
     *
     * @throws IllegalStateException if there are multiple such statements
     */
    protected <T> T singleOrNull(final IRI p, final ValueMapping<T> m) {
        Objects.requireNonNull(p);
        Objects.requireNonNull(m);

        final Iterator<T> statements = iterator(p, m);

        if (!statements.hasNext()) {
            return null;
        }

        final T any = statements.next();
        atMostOne(statements, p);

        return any;
    }

    /**
     * A converting singular getter helper for expected cardinality {@code 1..1} that forbids overflow.
     *
     * @param p the predicate to query
     * @param m the mapping applied to result terms
     * @param <T> the type of values returned
     *
     * @return the converted object of the only statement with this subject and the given predicate
     *
     * @throws IllegalStateException if there are no such statements
     * @throws IllegalStateException if there are multiple such statements
     */
    protected <T> T singleOrThrow(final IRI p, final ValueMapping<T> m) {
        Objects.requireNonNull(p);
        Objects.requireNonNull(m);

        final Iterator<T> statements = atLeastOne(p, m);
        final T any = statements.next();
        atMostOne(statements, p);

        return any;
    }

    /**
     * A static converting plural getter helper for expected cardinality {@code 0..*}.
     *
     * @param p the predicate to query
     * @param m the mapping applied to result terms
     * @param <T> the type of values returned
     *
     * @return the converted objects of statements with this subject and the given predicate
     */
    protected <T> Iterator<T> iterator(final IRI p, final ValueMapping<T> m) {
        Objects.requireNonNull(p);
        Objects.requireNonNull(m);

        return stream(p, m).iterator();
    }

    /**
     * A static converting plural getter helper for expected cardinality {@code 0..*}.
     *
     * @param p the predicate to query
     * @param m the mapping applied to result terms
     * @param <T> the type of values returned
     *
     * @return a static set view over converted objects of statements with this subject and the given predicate
     */
    protected <T> Set<T> snapshot(final IRI p, final ValueMapping<T> m) {
        Objects.requireNonNull(p);
        Objects.requireNonNull(m);

        return stream(p, m).collect(collectingAndThen(toSet(), Collections::unmodifiableSet));
    }

    /**
     * A dynamic converting plural getter helper for expected cardinality {@code 0..*}.
     *
     * @param p the predicate to query
     * @param tm the input mapping to apply to values
     * @param vm the output mapping to apply to terms
     * @param <T> the type of values returned
     *
     * @return a dynamic set view over converted objects of statements with this subject and the given predicate
     */
    protected <T> Set<T> live(final IRI p, final TermMapping<T> tm, final ValueMapping<T> vm) {
        return new ObjectSet<>(this, p, graph, tm, vm);
    }

    /**
     * A static converting plural getter helper for expected cardinality {@code 0..*}.
     *
     * @param p the predicate to query
     * @param m the mapping applied to result terms
     * @param <T> the type of values returned
     *
     * @return a static stream of converted objects of statements with this subject and the given predicate
     */
    protected <T> Stream<T> stream(final IRI p, final ValueMapping<T> m) {
        Objects.requireNonNull(p);
        Objects.requireNonNull(m);

        return graph.stream(this, p, null).map(Triple::getObject).map(term -> m.apply(term, graph));
    }

    /**
     * A destructive converting singular setter helper for expected cardinality {@code 1..1}.
     *
     * @param p the predicate to query
     * @param v the value to assert as object in the graph
     * @param m the mapping applied to the value
     * @param <T> the type of values returned
     *
     * @throws NullPointerException if the given value is {@code null}
     */
    protected <T> void overwrite(final IRI p, final T v, final TermMapping<T> m) {
        Objects.requireNonNull(p);
        Objects.requireNonNull(v);
        Objects.requireNonNull(m);

        remove(p);
        add(p, v, m);
    }

    /**
     * A destructive converting plural setter helper for expected cardinality {@code 1..*}.
     *
     * @param p the predicate to query
     * @param v the values to assert as objects in the graph
     * @param m the mapping applied to the value
     * @param <T> the type of values returned
     *
     * @throws NullPointerException if the given value is {@code null}
     * @throws NullPointerException if the given value contains {@code null} elements
     */
    protected <T> void overwrite(final IRI p, final Iterable<T> v, final TermMapping<T> m) {
        Objects.requireNonNull(p);
        Objects.requireNonNull(v);
        Objects.requireNonNull(m);
        v.forEach(Objects::requireNonNull);

        remove(p);
        v.forEach(value -> add(p, v, m));
    }

    /**
     * A destructive converting plural setter helper for expected cardinality {@code 0..*}.
     *
     * @param p the predicate to query
     * @param v the values to assert as objects in the graph
     * @param m the mapping applied to the value
     * @param <T> the type of values returned
     *
     * @throws NullPointerException if the given value is not {@code null} and contains {@code null} elements
     */

    protected <T> void overwriteNullable(final IRI p, final Iterable<T> v, final TermMapping<T> m) {
        Objects.requireNonNull(p);
        Objects.requireNonNull(m);

        if (v != null) {
            v.forEach(Objects::requireNonNull);
        }

        remove(p);

        if (v == null) {
            return;
        }

        v.forEach(value -> add(p, value, m));
    }

    /**
     * A destructive converting singular setter helper for expected cardinality {@code 0..1}.
     *
     * @param p the predicate to query
     * @param v the value to assert as object in the graph
     * @param m the mapping applied to the value
     * @param <T> the type of values returned
     */
    protected <T> void overwriteNullable(final IRI p, final T v, final TermMapping<T> m) {
        Objects.requireNonNull(p);
        Objects.requireNonNull(m);

        remove(p);

        if (v == null) {
            return;
        }

        add(p, v, m);
    }

    /**
     * An additive converting singular setter helper for expected cardinality {@code 0..*}.
     *
     * @param p the predicate to query
     * @param v the value to assert as object in the graph
     * @param m the mapping applied to the value
     * @param <T> the type of values returned
     *
     * @throws NullPointerException if the given value is {@code null}
     */
    protected <T> void add(final IRI p, final T v, final TermMapping<T> m) {
        Objects.requireNonNull(p);
        Objects.requireNonNull(v);
        Objects.requireNonNull(m);

        graph.add(this, p, m.apply(v, graph));
    }

    /**
     * An additive converting singular setter helper for expected cardinality {@code 0..*}.
     *
     * @param p the predicate to query
     * @param v the values to assert as objects in the graph
     * @param m the mapping applied to the value
     * @param <T> the type of values returned
     *
     * @throws NullPointerException if the given value is {@code null}
     * @throws NullPointerException if the given value contains {@code null} elements
     */
    protected <T> void add(final IRI p, final Iterable<T> v, final TermMapping<T> m) {
        Objects.requireNonNull(p);
        Objects.requireNonNull(v);
        Objects.requireNonNull(m);
        v.forEach(Objects::requireNonNull);

        v.forEach(value -> add(p, value, m));
    }

    private void remove(final IRI p) {
        graph.remove(this, p, null);
    }

    private <T> void atMostOne(final Iterator<T> statements, final IRI p) {
        if (statements.hasNext()) {
            final String message = String.format("Multiple statements with subject [%s] and predicate [%s]", this, p);
            // TODO: Throw specific exception
            throw new IllegalStateException(message);
        }
    }

    private <T> Iterator<T> atLeastOne(final IRI p, final ValueMapping<T> m) {
        final Iterator<T> statements = iterator(p, m);

        if (!statements.hasNext()) {
            final String message = String.format("No statements with subject [%s] and predicate [%s]", this, p);
            // TODO: Throw specific exception
            throw new IllegalStateException(message);
        }

        return statements;
    }
}
