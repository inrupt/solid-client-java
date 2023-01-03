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
package com.inrupt.client;

import com.inrupt.client.spi.RDFFactory;
import com.inrupt.client.spi.ServiceProvider;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.rdf.api.BlankNode;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.Dataset;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Quad;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.api.RDFSyntax;
import org.apache.commons.rdf.api.RDFTerm;

/**
 * A base class for resource mapping.
 *
 * <p>This class can be used as a basis for object mapping with higher-level client applications.
 */
public class Resource {

    private static final int SINGLETON = 1;

    /**
     * The RDF Factory instance.
     */
    protected static final RDF rdf = RDFFactory.getInstance();

    private final Dataset dataset;
    private final URI identifier;

    /**
     * Create a new resource.
     *
     * <p>Subclasses should have the same constructor signature to work with the provided object mapping mechanism.
     *
     * @param identifier the resource identifier
     * @param dataset the dataset corresponding to this resource, may be {@code null}
     */
    protected Resource(final URI identifier, final Dataset dataset) {
        this.identifier = identifier;
        this.dataset = dataset != null ? dataset : rdf.createDataset();
    }

    /**
     * Get the identifier for this resource.
     *
     * @return the resource identifier
     */
    public URI getIdentifier() {
        return identifier;
    }

    /**
     * Get the dataset for this resource.
     *
     * @return the resource dataset
     */
    public Dataset getDataset() {
        return dataset;
    }

    /**
     * Serialize this object with a defined RDF syntax.
     *
     * @param syntax the RDF syntax
     * @param out the output stream
     * @throws IOException in the case of an I/O error
     */
    public void serialize(final RDFSyntax syntax, final OutputStream out) throws IOException {
        ServiceProvider.getRdfService().fromDataset(getDataset(), syntax, out);
    }

    /**
     * Retrieve a path-based stream of quads, starting from the resource identifier.
     *
     * @param predicates the predicate path to follow
     * @return all matching quads
     */
    public Stream<Quad> path(final IRI... predicates) {
        final IRI root = rdf.createIRI(getIdentifier().toString());
        return pathRecursive(Collections.singleton(root), predicates);
    }

    /**
     * Validate the dataset for this object.
     *
     * <p>Subclasses may override this method to perform validation on the provided dataset during object creation.
     * By default, this method is a no-op.
     */
    public void validate() {
        // no-op
    }

    private Stream<Quad> pathRecursive(final Set<BlankNodeOrIRI> subjects, final IRI... predicates) {
        // Trivial case: no predicates
        if (predicates.length == 0) {
            return Stream.empty();
        }

        // Cases with a single, defined subject
        if (subjects.size() == SINGLETON) {
            return pathWithSubject(subjects.iterator().next(), predicates);
        }

        // Everything else
        return pathWithSubject(subjects, predicates);
    }

    private Stream<Quad> pathWithSubject(final BlankNodeOrIRI subject, final IRI... predicates) {
        if (predicates.length == SINGLETON) {
            return dataset.stream(null, subject, predicates[0], null).map(Quad.class::cast);
        }

        try (final Stream<? extends Quad> stream = dataset.stream(null, subject, predicates[0], null)) {
            final Set<BlankNodeOrIRI> objects = stream.map(Quad::getObject)
                .flatMap(Resource::asCandidateSubject)
                .collect(Collectors.toSet());
            return pathRecursive(objects, Arrays.copyOfRange(predicates, 1, predicates.length));
        }
    }

    private Stream<Quad> pathWithSubject(final Set<BlankNodeOrIRI> subjects, final IRI... predicates) {
        // Short circuit
        if (subjects.isEmpty()) {
            return Stream.empty();
        }

        if (predicates.length == SINGLETON) {
            return dataset.stream(null, null, predicates[0], null)
                .filter(quad -> subjects.contains(quad.getSubject()))
                .map(Quad.class::cast);
        }

        try (final Stream<? extends Quad> stream = dataset.stream(null, null, predicates[0], null)) {
            final Set<BlankNodeOrIRI> objects = stream.filter(quad -> subjects.contains(quad.getSubject()))
                .map(Quad::getObject).flatMap(Resource::asCandidateSubject)
                .collect(Collectors.toSet());
            return pathRecursive(objects, Arrays.copyOfRange(predicates, 1, predicates.length));
        }
    }

    static Stream<BlankNodeOrIRI> asCandidateSubject(final RDFTerm term) {
        if (term instanceof IRI) {
            return Stream.of((IRI) term);
        } else if (term instanceof BlankNode) {
            return Stream.of((BlankNode) term);
        }
        return Stream.empty();
    }
}
