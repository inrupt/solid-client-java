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
package com.inrupt.client.solid;

import com.inrupt.client.vocabulary.LDP;
import com.inrupt.client.vocabulary.RDF;
import com.inrupt.rdf.wrapping.commons.ValueMappings;
import com.inrupt.rdf.wrapping.commons.WrapperIRI;

import java.net.URI;
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
     * @param dataset the dataset for this container, may be {@code null}
     * @param metadata the container's metadata, may be {@code null}
     */
    public SolidContainer(final URI identifier, final Dataset dataset, final Metadata metadata) {
        super(identifier, dataset, metadata);
    }

    /**
     * Retrieve the resources contained in this SolidContainer.
     *
     * @return the contained resources
     */
    public Stream<SolidResource> getContainedResources() {
        return new Node(rdf.createIRI(getIdentifier().toString()), getGraph())
            .getContainedResources()
            .map(child -> {
                final Metadata.Builder builder = Metadata.newBuilder();
                getMetadata().getStorage().ifPresent(builder::storage);
                child.getTypes().forEach(builder::type);
                return new SolidResourceReference(URI.create(child.getIRIString()), builder.build());
            });
    }

    @SuppressWarnings("java:S2160") // Wrapper equality is correctly delegated to underlying node
    static final class Node extends WrapperIRI {
        private final IRI ldpContains = rdf.createIRI(LDP.contains.toString());

        Node(final RDFTerm original, final Graph graph) {
            super(original, graph);
        }

        Stream<TypedNode> getContainedResources() {
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
