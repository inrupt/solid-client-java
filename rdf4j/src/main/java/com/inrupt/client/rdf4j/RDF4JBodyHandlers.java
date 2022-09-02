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
import java.util.function.Supplier;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.Rio;

/**
 * {@link HttpResponse.BodyHandler} implementations for use with RDF4J types.
 */
public final class RDF4JBodyHandlers {

    /**
     * Populate a RDF4J {@link Model} with an HTTP response.
     *
     * @return an HTTP body handler
     */
    public static HttpResponse.BodyHandler<Supplier<Model>> ofModel() {
        return responseInfo -> {
            final var format = responseInfo.headers().firstValue("Content-Type").orElseThrow(
                    () -> new RDFHandlerException("Missing content-type header from response"));
            return RDF4JBodySubscribers.ofModel(toRDF4JFormat(format));
        };
    }

    /**
     * Populate a RDF4J {@link Repository} with an HTTP response.
     *
     * @return an HTTP body handler
     */
    public static HttpResponse.BodyHandler<Repository> ofRepository() {
        return responseInfo -> {
            final var format = responseInfo.headers().firstValue("Content-Type").orElseThrow(
                    () -> new RDFHandlerException("Missing content-type header from response"));
            return RDF4JBodySubscribers.ofRepository(toRDF4JFormat(format));
        };
    }

    static RDFFormat toRDF4JFormat(final String mediaType) {
        return Rio.getParserFormatForMIMEType(mediaType).orElse(RDFFormat.TURTLE);
    }

    private RDF4JBodyHandlers() {
        //Prevent instantiation
    }

}
