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
package com.inrupt.client.solid;

import com.inrupt.client.vocabulary.LDP;
import com.inrupt.client.vocabulary.RDF;

import java.net.URI;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.rdf.api.Dataset;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Quad;

/**
 * A Solid Container Object.
 */
public final class SolidContainer extends SolidResource {

    private final IRI rdfType;
    private final IRI ldpContains;

    /**
     * Create a new SolidContainer.
     *
     * @param identifier the container's unique identifier
     * @param dataset the dataset for this container, may be {@code null}
     * @param metadata the container's metadata, may be {@code null}
     */
    public SolidContainer(final URI identifier, final Dataset dataset, final Metadata metadata) {
        super(identifier, dataset, metadata);

        this.rdfType = rdf.createIRI(RDF.type.toString());
        this.ldpContains = rdf.createIRI(LDP.contains.toString());
    }

    /**
     * Retrieve the resources contained in this SolidContainer.
     *
     * @return the contained resources
     */
    public Stream<SolidResource> getContainedResources() {
        return path(ldpContains)
            .map(Quad::getObject).filter(IRI.class::isInstance).map(IRI.class::cast)
            .map(child -> {
                final Metadata.Builder builder = Metadata.newBuilder();
                getMetadata().getStorage().ifPresent(builder::storage);
                try (final Stream<Quad> stream = getDataset().stream(Optional.empty(), child, rdfType, null)
                        .map(Quad.class::cast)) {
                    stream.map(Quad::getObject).filter(IRI.class::isInstance).map(IRI.class::cast)
                        .map(IRI::getIRIString).map(URI::create).forEach(builder::type);
                }
                return new SolidResource(URI.create(child.getIRIString()), null, builder.build());
            });
    }
}
