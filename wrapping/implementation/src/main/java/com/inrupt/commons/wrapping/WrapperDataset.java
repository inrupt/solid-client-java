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

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.rdf.api.*;

// TODO: Document
// TODO: Cover
public class WrapperDataset implements Dataset, AutoCloseable {
    private final Dataset original;

    // TODO: Document
    protected WrapperDataset(final Dataset original) {
        Objects.requireNonNull(original, "Dataset is required");

        this.original = original;
    }

    @Override
    public void add(final Quad quad) {
        original.add(quad);
    }

    @Override
    public void add(
            final BlankNodeOrIRI graphName,
            final BlankNodeOrIRI subject,
            final IRI predicate,
            final RDFTerm object) {
        original.add(graphName, subject, predicate, object);
    }

    @Override
    public boolean contains(final Quad quad) {
        return original.contains(quad);
    }

    @Override
    public boolean contains(
            final Optional<BlankNodeOrIRI> graphName,
            final BlankNodeOrIRI subject,
            final IRI predicate,
            final RDFTerm object) {
        return original.contains(graphName, subject, predicate, object);
    }

    @Override
    public Graph getGraph() {
        return original.getGraph();
    }

    @Override
    public Optional<Graph> getGraph(final BlankNodeOrIRI graphName) {
        return original.getGraph(graphName);
    }

    @Override
    public Stream<BlankNodeOrIRI> getGraphNames() {
        return original.getGraphNames();
    }

    @Override
    public void remove(final Quad quad) {
        original.remove(quad);
    }

    @Override
    public void remove(
            final Optional<BlankNodeOrIRI> graphName,
            final BlankNodeOrIRI subject,
            final IRI predicate,
            final RDFTerm object) {
        original.remove(graphName, subject, predicate, object);
    }

    @Override
    public void clear() {
        original.clear();
    }

    @Override
    public long size() {
        return original.size();
    }

    @Override
    public Stream<? extends Quad> stream() {
        return original.stream();
    }

    @Override
    public Stream<? extends Quad> stream(
            final Optional<BlankNodeOrIRI> graphName,
            final BlankNodeOrIRI subject,
            final IRI predicate,
            final RDFTerm object) {
        return original.stream(graphName, subject, predicate, object);
    }

    @Override
    public void close() {
        try {
            original.close();
        } catch (Exception e) {
            // TODO: Throw specific exception
            throw new RuntimeException(e);
        }
    }
}
