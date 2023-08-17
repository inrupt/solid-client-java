/*
 * Copyright Inrupt Inc.
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
package com.inrupt.client.solid;

import com.inrupt.client.ValidationResult;
import com.inrupt.client.vocabulary.LDP;
import com.inrupt.client.vocabulary.RDF;
import com.inrupt.rdf.wrapping.commons.ValueMappings;
import com.inrupt.rdf.wrapping.commons.WrapperIRI;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.rdf.api.Dataset;
import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDFTerm;
import org.apache.commons.rdf.api.Triple;

/**
 * A Solid Container Object.
 */
public class SolidContainer extends SolidRDFSource {

    /**
     * Create a new SolidContainer.
     *
     * @param identifier the container's unique identifier
     */
    public SolidContainer(final URI identifier) {
        this(identifier, null);
    }

    /**
     * Create a new SolidContainer.
     *
     * @param identifier the container's unique identifier
     * @param dataset the dataset for this container, may be {@code null}
     */
    public SolidContainer(final URI identifier, final Dataset dataset) {
        this(identifier, dataset, null);
    }

    /**
     * Create a new SolidContainer.
     *
     * @param identifier the container's unique identifier
     * @param dataset the dataset for this container, may be {@code null}
     * @param metadata the container's metadata, may be {@code null}
     */
    public SolidContainer(final URI identifier, final Dataset dataset, final Metadata metadata) {
        super(identifier, dataset, metadata);
    }

    /**
     * Get an immutable collection of resources contained in this SolidContainer.
     *
     * @return the contained resources
     */
    public Set<SolidResource> getResources() {
        // As defined by the Solid Protocol, containers always end with a slash.
        final URI base = getIdentifier().normalize();
        if (isContainer(base)) {
            final String container = normalize(base);
            final Node node = new Node(rdf.createIRI(base.toString()), getGraph());
            try (final Stream<Node.TypedNode> stream = node.getResources()) {
                return stream.filter(child -> verifyContainmentIri(container, child)).map(child -> {
                    final Metadata.Builder builder = Metadata.newBuilder();
                    getMetadata().getStorage().ifPresent(builder::storage);
                    child.getTypes().forEach(builder::type);
                    return new SolidResourceReference(URI.create(child.getIRIString()), builder.build());
                }).collect(Collectors.collectingAndThen(Collectors.toSet(), Collections::unmodifiableSet));
            }
        }
        return Collections.emptySet();
    }

    @Override
    public ValidationResult validate() {
        final List<String> messages = new ArrayList<>();
        // Verify that the container URI path ends with a slash
        final URI base = getIdentifier().normalize();
        if (!isContainer(base)) {
            messages.add("Container URI does not end in a slash");
        }

        // Get the normalized container URI
        final String container = normalize(base);
        // Verify that all ldp:contains triples align with Solid expectations
        getGraph().stream(null, rdf.createIRI(LDP.contains.toString()), null)
            .collect(Collectors.partitioningBy(verifyContainmentTriple(container)))
            .get(false) // we are only concerned with the invalid triples
            .forEach(triple -> messages.add("Invalid containment triple: " + triple.getSubject().ntriplesString() +
                        " ldp:contains " + triple.getObject().ntriplesString() + " ."));

        if (messages.isEmpty()) {
            return new ValidationResult(true);
        }
        return new ValidationResult(false, messages);
    }

    static boolean isContainer(final URI uri) {
        return uri.normalize().getPath().endsWith("/");
    }

    static String normalize(final URI uri) {
        return uri.normalize().toString().split("#")[0].split("\\?")[0];
    }

    static Predicate<Triple> verifyContainmentTriple(final String container) {
        final IRI subject = rdf.createIRI(container);
        return triple -> {
            if (!triple.getSubject().equals(subject)) {
                // Out-of-domain containment triple subject
                return false;
            }
            if (triple.getObject() instanceof IRI) {
                return verifyContainmentIri(container, (IRI) triple.getObject());
            }
            // Non-URI containment triple object
            return false;
        };
    }

    static boolean verifyContainmentIri(final String container, final IRI object) {

        // URI Structure Tests
        final URI base = URI.create(container).normalize();
        final URI normalized = URI.create(object.getIRIString()).normalize();

        // Query strings are not allowed in subject or object URI
        if (base.getQuery() != null || normalized.getQuery() != null) {
            return false;
        }

        // URI fragments are not allowed in subject or object URI
        if (base.getFragment() != null || normalized.getFragment() != null) {
            return false;
        }

        // Base URI cannot equal the object URI
        if (base.getScheme().equals(normalized.getScheme()) &&
                base.getSchemeSpecificPart().equals(normalized.getSchemeSpecificPart())) {
            return false;
        }

        // Relative path tests
        final URI relative = base.relativize(normalized);

        // Object URI must be relative to (contained in) the base URI
        if (relative.isAbsolute()) {
            return false;
        }

        final String relativePath = relative.getPath();
        final String normalizedPath = relativePath.endsWith("/") ?
            relativePath.substring(0, relativePath.length() - 1) : relativePath;

        // Containment cannot skip intermediate nodes
        if (normalizedPath.contains("/")) {
            return false;
        }

        return true;
    }

    @SuppressWarnings("java:S2160") // Wrapper equality is correctly delegated to underlying node
    static final class Node extends WrapperIRI {
        private final IRI ldpContains = rdf.createIRI(LDP.contains.toString());

        Node(final RDFTerm original, final Graph graph) {
            super(original, graph);
        }

        Stream<TypedNode> getResources() {
            return objectStream(ldpContains, ValueMappings.as(TypedNode.class));
        }

        public static final class TypedNode extends WrapperIRI {
            final IRI rdfType = rdf.createIRI(RDF.type.toString());

            public TypedNode(final RDFTerm original, final Graph graph) {
                super(original, graph);
            }

            Stream<URI> getTypes() {
                return objectStream(rdfType, ValueMappings::iriAsUri);
            }
        }
    }
}
