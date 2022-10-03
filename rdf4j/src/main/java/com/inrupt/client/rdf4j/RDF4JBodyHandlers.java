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
package com.inrupt.client.rdf4j;

import java.net.http.HttpResponse;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.DynamicModelFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

/**
 * {@link HttpResponse.BodyHandler} implementations for use with RDF4J types.
 */
public final class RDF4JBodyHandlers {

    /**
     * Populate a RDF4J {@link Model} with an HTTP response.
     *
     * @return an HTTP body handler
     */
    public static HttpResponse.BodyHandler<Model> ofModel() {
        return responseInfo -> responseInfo.headers().firstValue("Content-Type")
            .map(RDF4JBodyHandlers::toRDF4JFormat).map(RDF4JBodySubscribers::ofModel)
            .orElseGet(() -> HttpResponse.BodySubscribers.replacing(new DynamicModelFactory().createEmptyModel()));
    }

    /**
     * Populate a RDF4J {@link Repository} with an HTTP response.
     *
     * @return an HTTP body handler
     */
    public static HttpResponse.BodyHandler<Repository> ofRepository() {
        return responseInfo -> responseInfo.headers().firstValue("Content-Type")
            .map(RDF4JBodyHandlers::toRDF4JFormat).map(RDF4JBodySubscribers::ofRepository)
            .orElseGet(() -> HttpResponse.BodySubscribers.replacing(new SailRepository(new MemoryStore())));
    }

    static RDFFormat toRDF4JFormat(final String mediaType) {
        return Rio.getParserFormatForMIMEType(mediaType).orElse(RDFFormat.TURTLE);
    }

    private RDF4JBodyHandlers() {
        //Prevent instantiation
    }

}
