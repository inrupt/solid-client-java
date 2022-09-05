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

import com.inrupt.client.core.IOUtils;

import java.net.http.HttpRequest;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.sparql.query.SPARQLUpdate;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

/**
 * {@link HttpRequest.BodyPublisher} implementations for use with RDF4J types.
 */
public final class RDF4JBodyPublishers {
    /**
     * Serialize a RDF4J Model as an HTTP request body.
     *
     * <p>This method uses the default (TURTLE) serialization.
     *
     * @param model the model
     * @return the body publisher
     */
    public static HttpRequest.BodyPublisher ofModel(final Model model) {
        return ofModel(model, RDFFormat.TURTLE);
    }

    /**
     * Serialize a RDF4J Model as an HTTP request body.
     *
     * @param model the model
     * @param format the serialization language
     * @return the body publisher
     */
    public static HttpRequest.BodyPublisher ofModel(final Model model, final RDFFormat format) {
        return HttpRequest.BodyPublishers.ofInputStream(() ->
                IOUtils.pipe(out -> Rio.write(model, out, format)));
    }

    /**
     * Serialize a RDF4J Repository as an HTTP request body.
     *
     * <p>This method uses the default (TRIG) serialization.
     *
     * @param repository the Repository
     * @return the body publisher
     */
    public static HttpRequest.BodyPublisher ofRepository(final Repository repository) {
        return ofRepository(repository, RDFFormat.TRIG);
    }

    /**
     * Serialize a RDF4J Repository as an HTTP request body.
     *
     * @param repository the Repository
     * @param format the serialization language
     * @return the body publisher
     */
    public static HttpRequest.BodyPublisher ofRepository(
        final Repository repository,
        final RDFFormat format) {
        return HttpRequest.BodyPublishers.ofInputStream(() ->
        IOUtils.pipe(out -> {
            try (final var conn = repository.getConnection()) {
                final var m = QueryResults.asModel(conn.getStatements(null, null, null));
                Rio.write(m, out, format);
            }
        }));
    }

    /**
     * Serialize a RDF4J SPARQLUpdate (SPARQL-Update) as an HTTP request body.
     *
     * @param sparql the SPARQL Update request
     * @return the body publisher
     */
    public static HttpRequest.BodyPublisher ofSparqlUpdate(final SPARQLUpdate sparql) {
        return HttpRequest.BodyPublishers.ofString(sparql.toString());
    }

    private RDF4JBodyPublishers() {
        // Prevent instantiation
    }
}
