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

import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.ext.com.google.common.collect.Streams;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.shared.PropertyNotFoundException;

/**
 * A {@link Resource} that contains methods that aid authoring node wrapping classes.
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
public abstract class ResourceCon extends ResourceImpl {
    protected ResourceCon(final Node n, final EnhGraph m) {
        super(n, m);
    }

    /**
     * A converting singular getter helper for expected cardinality {@code 0..1} that ignores overflow.
     *
     * @param p the predicate to query
     * @param m the mapping applied to result nodes
     * @param <T> the type of values returned the type of values returned
     *
     * @return the converted object of an arbitrary statement with this subject and the given predicate or null if there
     * are no such statements
     */
    protected <T> T anyOrNull(final Property p, final ValueMapping<T> m) {
        Objects.requireNonNull(p);
        Objects.requireNonNull(m);

        final var statement = getProperty(p);

        if (statement == null) {
            return null;
        }

        return m.toValue(statement.getObject());
    }

    /**
     * A converting singular getter helper for expected cardinality {@code 1..1} that ignores overflow.
     *
     * @param p the predicate to query
     * @param m the mapping applied to result nodes
     * @param <T> the type of values returned
     *
     * @return the converted object of an arbitrary statement with this subject and the given predicate
     *
     * @throws PropertyNotFoundException if there are no such statements
     */
    protected <T> T anyOrThrow(final Property p, final ValueMapping<T> m) {
        Objects.requireNonNull(p);
        Objects.requireNonNull(m);

        return m.toValue(getRequiredProperty(p).getObject());
    }

    /**
     * A converting singular getter helper for expected cardinality {@code 0..1} that forbids overflow.
     *
     * @param p the predicate to query
     * @param m the mapping applied to result nodes
     * @param <T> the type of values returned
     *
     * @return the converted object of the only statement with this subject and the given predicate, or null if there is
     * no such statement
     *
     * @throws IllegalStateException if there are multiple such statements
     */
    protected <T> T singleOrNull(final Property p, final ValueMapping<T> m) {
        Objects.requireNonNull(p);
        Objects.requireNonNull(m);

        final var statements = iterator(p, m);

        if (!statements.hasNext()) {
            return null;
        }

        final var any = statements.next();

        throwIfMultiple(statements.hasNext(), p);

        return any;
    }

    /**
     * A converting singular getter helper for expected cardinality {@code 1..1} that forbids overflow.
     *
     * @param p the predicate to query
     * @param m the mapping applied to result nodes
     * @param <T> the type of values returned
     *
     * @return the converted object of the only statement with this subject and the given predicate
     *
     * @throws PropertyNotFoundException if there are no such statements
     * @throws IllegalStateException if there are multiple such statements
     */
    protected <T> T singleOrThrow(final Property p, final ValueMapping<T> m) {
        Objects.requireNonNull(p);
        Objects.requireNonNull(m);

        final var statements = iterator(p, m);

        if (!statements.hasNext()) {
            throw new PropertyNotFoundException(p);
        }

        final var any = statements.next();

        throwIfMultiple(statements.hasNext(), p);

        return any;
    }

    /**
     * A static converting plural getter helper for expected cardinality {@code 0..*}.
     *
     * @param p the predicate to query
     * @param m the mapping applied to result nodes
     * @param <T> the type of values returned
     *
     * @return the converted objects of statements with this subject and the given predicate
     */
    protected <T> Iterator<T> iterator(final Property p, final ValueMapping<T> m) {
        Objects.requireNonNull(p);
        Objects.requireNonNull(m);

        return listProperties(p).mapWith(Statement::getObject).mapWith(m::toValue);
    }

    /**
     * A static converting plural getter helper for expected cardinality {@code 0..*}.
     *
     * @param p the predicate to query
     * @param m the mapping applied to result nodes
     * @param <T> the type of values returned
     *
     * @return a static set view over converted objects of statements with this subject and the given predicate
     */
    protected <T> Set<T> snapshot(final Property p, final ValueMapping<T> m) {
        Objects.requireNonNull(p);
        Objects.requireNonNull(m);

        return Streams.stream(iterator(p, m)).collect(Collectors.toUnmodifiableSet());
    }

    /**
     * A dynamic converting plural getter helper for expected cardinality {@code 0..*}.
     *
     * @param p the predicate to query
     * @param nm the input mapping to apply to values
     * @param vm the output mapping to apply to nodes
     * @param <T> the type of values returned
     *
     * @return a dynamic set view over converted objects of statements with this subject and the given predicate
     */
    protected <T> Set<T> live(final Property p, final NodeMapping<T> nm, final ValueMapping<T> vm) {
        return new PredicateObjectSet<>(this, p, nm, vm);
    }

    /**
     * A destructive converting singular setter helper for expected cardinality {@code 1..1}.
     *
     * @param p the predicate to query
     * @param v the value to assert as object in the graph
     * @param m the mapping applied to result nodes
     * @param <T> the type of values returned
     *
     * @throws NullPointerException if the given value is {@code null}
     */
    protected <T> void overwrite(final Property p, final T v, final NodeMapping<T> m) {
        Objects.requireNonNull(p);
        Objects.requireNonNull(v);
        Objects.requireNonNull(m);

        removeAll(p);
        addProperty(p, m.toNode(v, getModel()));
    }

    /**
     * A destructive converting plural setter helper for expected cardinality {@code 1..*}.
     *
     * @param p the predicate to query
     * @param v the value to assert as object in the graph
     * @param m the mapping applied to result nodes
     * @param <T> the type of values returned
     *
     * @throws NullPointerException if the given value is {@code null}
     * @throws NullPointerException if the given value contains {@code null} elements
     */
    protected <T> void overwrite(final Property p, final Iterable<T> v, final NodeMapping<T> m) {
        Objects.requireNonNull(p);
        Objects.requireNonNull(v);
        Objects.requireNonNull(m);
        v.forEach(Objects::requireNonNull);

        removeAll(p);
        v.forEach(value -> addProperty(p, m.toNode(value, getModel())));
    }

    /**
     * A destructive converting singular setter helper for expected cardinality {@code 0..1}.
     *
     * @param p the predicate to query
     * @param v the value to assert as object in the graph
     * @param m the mapping applied to result nodes
     * @param <T> the type of values returned
     */
    protected <T> void overwriteNullable(final Property p, final T v, final NodeMapping<T> m) {
        Objects.requireNonNull(p);
        Objects.requireNonNull(m);

        removeAll(p);

        if (v == null) {
            return;
        }

        addProperty(p, m.toNode(v, getModel()));
    }

    /**
     * A destructive converting plural setter helper for expected cardinality {@code 0..*}.
     *
     * @param p the predicate to query
     * @param v the value to assert as object in the graph
     * @param m the mapping applied to result nodes
     * @param <T> the type of values returned
     *
     * @throws NullPointerException if the given value is not {@code null} and contains {@code null} elements
     */
    protected <T> void overwriteNullable(final Property p, final Iterable<T> v, final NodeMapping<T> m) {
        Objects.requireNonNull(p);
        Objects.requireNonNull(m);

        if (v != null) {
            v.forEach(Objects::requireNonNull);
        }

        removeAll(p);

        if (v == null) {
            return;
        }

        v.forEach(value -> add(p, value, m));
    }

    /**
     * An additive converting singular setter helper for expected cardinality {@code 0..*}.
     *
     * @param p the predicate to query
     * @param v the value to assert as object in the graph
     * @param m the mapping applied to result nodes
     * @param <T> the type of values returned
     *
     * @throws NullPointerException if the given value is {@code null}
     */
    protected <T> void add(final Property p, final T v, final NodeMapping<T> m) {
        Objects.requireNonNull(p);
        Objects.requireNonNull(v);
        Objects.requireNonNull(m);

        addProperty(p, m.toNode(v, getModel()));
    }

    /**
     * An additive converting singular setter helper for expected cardinality {@code 0..*}.
     *
     * @param p the predicate to query
     * @param v the value to assert as object in the graph
     * @param m the mapping applied to result nodes
     * @param <T> the type of values returned
     *
     * @throws NullPointerException if the given value is {@code null}
     * @throws NullPointerException if the given value contains {@code null} elements
     */
    protected <T> void add(final Property p, final Iterable<T> v, final NodeMapping<T> m) {
        Objects.requireNonNull(p);
        Objects.requireNonNull(v);
        Objects.requireNonNull(m);
        v.forEach(Objects::requireNonNull);

        v.forEach(value -> add(p, value, m));
    }

    private void throwIfMultiple(final boolean statements, final Property p) {
        if (statements) {
            final var message = String.format("Multiple statements with subject [%s] and predicate [%s]", this, p);
            throw new IllegalStateException(message);
        }
    }
}
