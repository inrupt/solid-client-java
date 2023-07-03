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

import com.inrupt.client.vocabulary.LDP;
import com.inrupt.client.vocabulary.RDF;
import com.inrupt.rdf.wrapping.commons.ValueMappings;
import com.inrupt.rdf.wrapping.commons.WrapperIRI;

import java.net.URI;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.rdf.api.Dataset;
import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDFTerm;

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
        final String container = getIdentifier().toString();
        // As defined by the Solid Protocol, containers always end with a slash.
        if (container.endsWith("/")) {
            final Node node = new Node(rdf.createIRI(getIdentifier().toString()), getGraph());
            try (final Stream<Node.TypedNode> stream = node.getResources()) {
                return stream.flatMap(child -> {
                    final URI childLocation = URI.create(child.getIRIString()).normalize();
                    // Solid containment is based on URI path hierarchy,
                    // so all child resources must start with the URL of the parent
                    if (childLocation.toString().startsWith(container)) {
                        final String relativePath = childLocation.toString().substring(container.length());
                        final String normalizedPath = relativePath.endsWith("/") ?
                            relativePath.substring(0, relativePath.length() - 1) : relativePath;
                        // Solid containment occurs via direct decent,
                        // so any recursively contained resources must not be included
                        if (!normalizedPath.isEmpty() && !normalizedPath.contains("/")) {
                            final Metadata.Builder builder = Metadata.newBuilder();
                            getMetadata().getStorage().ifPresent(builder::storage);
                            child.getTypes().forEach(builder::type);
                            return Stream.of(new SolidResourceReference(childLocation, builder.build()));
                        }
                    }
                    return Stream.empty();
                }).collect(Collectors.collectingAndThen(Collectors.toSet(), Collections::unmodifiableSet));
            }
        }
        return Collections.emptySet();
    }

    /**
     * Retrieve the resources contained in this SolidContainer.
     *
     * @deprecated As of Beta2, replaced by the {@link #getResources()} method.
     * @return the contained resources
     */
    @Deprecated
    public Stream<SolidResource> getContainedResources() {
        return getResources().stream();
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
