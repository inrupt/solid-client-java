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

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.rdf.api.*;


/**
 * This class implements the {@link Set} interface as a dynamic, mutable view over an RDF predicate-object list
 * (statements that share a subject and a predicate). It is intended for use in classes that wrap
 * {@link RDFTerm RDF nodes} for strongly typed convenience mapping.
 *
 * <p>This set does not permit {@code null} elements.
 *
 * <p>The order of elements returned by this implementation is not guaranteed as it depends on the ordering of query
 * results in the underlying {@link Graph}. This reflects the unordered nature of RDF graphs.
 *
 * <p>The synchronization characteristics and time complexity of this implementation are those of the underlying
 * {@link Graph} implementation. It could well be that read and write operations on instances of this class result in
 * expensive IO operations. Even simple iteration is most likely to be much less performant than what callers expect
 * from other Java collections.
 *
 * <p>The iterators returned by this implementation do not support the {@link Iterator#remove()} operation.
 *
 * <p>This implementation uses the {@link AbstractSet#equals(Object)} and {@link AbstractSet#hashCode()} operations.
 * Equality and hashing are dynamic: They depend on the state of the underlying {@link Graph} at the time of calling and
 * are not fixed when creating the instance.
 *
 * <p>Example: Given a node wrapper {@code N}, instances of this class can be used to make read/write strongly typed
 * set properties.
 * <pre>{@code public class N {
 *     public Set<String> getType {
 *         return new PredicateObjectSet<>(
 *             this,
 *             RDF.type,
 *             g,
 *             NodeMappings::asIriResource,
 *             ValueMappings::iriAsString
 *         );
 *     }
 * }}</pre>
 *
 * @param <T> the type of elements handled by this set
 *
 * @author Samu Lang
 */
public class PredicateObjectSet<T> extends AbstractSet<T> {
    private static final BinaryOperator<Boolean> EITHER = (Boolean a, Boolean b) -> a || b;
    private static final BinaryOperator<Boolean> BOTH = (Boolean a, Boolean b) -> a && b;

    protected final BlankNodeOrIRI subject;
    protected final IRI predicate;
    protected final Graph graph;
    protected final NodeMapping<T> nodeMapping;
    protected final ValueMapping<T> valueMapping;

    /**
     * Constructs a new dynamic set view over the objects of statements that share a predicate and an object.
     *
     * @param subject the subject node shared by all statements
     * @param predicate the predicate node shared by all statements
     * @param graph the graph containing the statements
     * @param nodeMapping a mapping from nodes to values used for read operations (use {@link NodeMappings} for common
     * mappings)
     * @param valueMapping a mapping from values to nodes used for write operations (use {@link ValueMappings} for
     * common mappings)
     *
     * @throws NullPointerException if any of the arguments are null
     */
    public PredicateObjectSet(
            final BlankNodeOrIRI subject,
            final IRI predicate,
            final Graph graph, final NodeMapping<T> nodeMapping,
            final ValueMapping<T> valueMapping) {

        Objects.requireNonNull(subject, "Subject is required");
        Objects.requireNonNull(predicate, "Predicate is required");
        Objects.requireNonNull(graph, "Graph is required");
        Objects.requireNonNull(nodeMapping, "Node factory is required");
        Objects.requireNonNull(valueMapping, "Value factory is required");

        this.subject = subject;
        this.predicate = predicate;
        this.graph = graph;
        this.nodeMapping = nodeMapping;
        this.valueMapping = valueMapping;
    }

    @Override
    public int size() {
        final long size = statements().count();
        return size > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) size;
    }

    @Override
    public boolean isEmpty() {
        return !graph.contains(subject, predicate, null);
    }

    @Override
    public boolean contains(final Object o) {
        Objects.requireNonNull(o);

        @SuppressWarnings("unchecked") // Happy to throw if it cannot be cast.
        final RDFTerm object = nodeMapping.toNode((T) o, graph);

        return graph.contains(subject, predicate, object);
    }

    @Override
    public Iterator<T> iterator() {
        return values().iterator();
    }

    @Override
    public Object[] toArray() {
        return values().toArray();
    }

    @Override
    public <U> U[] toArray(final U[] a) {
        return new ArrayList<>(this).toArray(a);
    }

    @Override
    public boolean add(final T e) {
        Objects.requireNonNull(e);

        if (contains(e)) {
            return false;
        }

        graph.add(subject, predicate, nodeMapping.toNode(e, graph));

        return true;
    }

    @Override
    public boolean remove(final Object o) {
        Objects.requireNonNull(o);

        if (!contains(o)) {
            return false;
        }

        @SuppressWarnings("unchecked") // Would have thrown in #contains.
        final RDFTerm object = nodeMapping.toNode((T) o, graph);
        graph.remove(subject, predicate, object);

        return true;
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        Objects.requireNonNull(c);

        return c.stream()
                .map(this::contains)
                .reduce(true, BOTH);
    }

    @Override
    public boolean addAll(final Collection<? extends T> c) {
        Objects.requireNonNull(c);

        return c.stream()
                .map(this::add)
                .reduce(false, EITHER);
    }

    // AbstractSet#retainAll relies on Iterator#remove, which is not supported here.
    @Override
    public boolean retainAll(final Collection<?> c) {
        Objects.requireNonNull(c);

        return values().collect(Collectors.toList()).stream()
                .map(value -> removeUnlessContains(c, value))
                .reduce(false, EITHER);
    }

    // AbstractSet#removeAll relies on Iterator#remove, which is not supported here.
    @Override
    public boolean removeAll(final Collection<?> c) {
        Objects.requireNonNull(c);

        return c.stream()
                .map(this::remove)
                .reduce(false, EITHER);
    }

    @Override
    public void clear() {
        graph.remove(subject, predicate, null);
    }

    // Delegates to AbstractSet. Overriden for clarity.
    @Override
    public boolean equals(final Object o) {
        return super.equals(o);
    }

    // Delegates to AbstractSet. Overriden for clarity.
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    private Stream<? extends Triple> statements() {
        return graph.stream(subject, predicate, null);
    }

    private Stream<RDFTerm> objects() {
        return statements().map(Triple::getObject);
    }

    private Stream<T> values() {
        return objects().map(valueMapping::toValue);
    }

    private boolean removeUnlessContains(final Collection<?> c, final T value) {
        if (c.contains(value)) {
            return false;
        }

        return remove(value);
    }
}
