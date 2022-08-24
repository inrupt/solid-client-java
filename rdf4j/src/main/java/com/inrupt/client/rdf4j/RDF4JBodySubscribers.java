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

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

/**
 * {@link HttpResponse.BodySubscriber} implementations for use with RDF4J types.
 */
public final class RDF4JBodySubscribers {
    /**
     * Process an HTTP response as a RDF4J {@link Model}.
     *
     * <p>This method expects the default (TURTLE) serialization of an HTTP response.
     *
     * @return the body subscriber
     */
    public static HttpResponse.BodySubscriber<Model> ofModel() {
        return ofModel(RDFFormat.TURTLE);
    }

    /**
     * Process an HTTP response as a RDF4J {@link Model}.
     *
     * @param format the RDF serialization of the HTTP response
     * @return the body subscriber
     */
    public static HttpResponse.BodySubscriber<Model> ofModel(final RDFFormat format) {
        final var upstream = HttpResponse.BodySubscribers.ofInputStream();
        //RDFParser rdfParser = Rio.createParser(format); -> not sure if needed
        return HttpResponse.BodySubscribers.mapping(upstream, (InputStream input) -> {
            try {
                return Rio.parse(input, format);
            } catch (RDFParseException | UnsupportedRDFormatException | IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
            }
        });
    }

    /**
     * Process an HTTP response as a RDF4J {@link Repository}.
     *
     * <p>This method expects the default (TRIG) serialization of an HTTP response.
     *
     * @return the body subscriber
     */
    public static HttpResponse.BodySubscriber<Repository> ofRepository() {
        return ofRepository(RDFFormat.TRIG);
    }

    /**
     * Process an HTTP response as a RDF4J {@link Repository}.
     *
     * @param format the RDF serialization of the HTTP response
     * @return the body subscriber
     */
    public static HttpResponse.BodySubscriber<Repository> ofRepository(final RDFFormat format) {
        final var upstream = HttpResponse.BodySubscribers.ofInputStream();
        return HttpResponse.BodySubscribers.mapping(upstream, (InputStream input) -> {
            final var repository = new SailRepository(new MemoryStore());
            try (final var conn = repository.getConnection()) {
                conn.add(input, format);
            } catch (final RDFParseException | RepositoryException | IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return repository;
        });
    }

    private RDF4JBodySubscribers() {
        // Prevent instantiation
    }
}
