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
import java.io.UncheckedIOException;
import java.net.http.HttpResponse;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
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
        final HttpResponse.BodySubscriber<Model> downstream = HttpResponse.BodySubscribers.mapping(
            upstream,
            (InputStream is) -> {
                try (InputStream stream = is) {
                    final var model = Rio.parse(stream, format);
                    return model;
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        );
        return downstream;
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
        final HttpResponse.BodySubscriber<Repository> downstream = HttpResponse.BodySubscribers.mapping(
            upstream,
            (InputStream input) -> {
                try (InputStream stream = input) {
                    final var repository = new SailRepository(new MemoryStore());
                    try (final var conn = repository.getConnection()) {
                        conn.add(stream, format);
                    }
                    return repository;
                } catch (final IOException ex) {
                    throw new UncheckedIOException(
                        "An I/O error occurred while data was read from the InputStream into a Repository",
                        ex
                    );
                }
            });
        return downstream;
    }

    private RDF4JBodySubscribers() {
        // Prevent instantiation
    }
}
