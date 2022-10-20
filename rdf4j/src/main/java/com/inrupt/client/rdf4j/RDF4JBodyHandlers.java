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

import com.inrupt.client.api.Response;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.DynamicModelFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

/**
 * {@link Response.BodyHandler} implementations for use with RDF4J types.
 */
public final class RDF4JBodyHandlers {

    /**
     * Populate a RDF4J {@link Model} with an HTTP response.
     *
     * @return an HTTP body handler
     */
    public static Response.BodyHandler<Model> ofModel() {
        return responseInfo -> responseInfo.headers().firstValue("Content-Type")
            .map(RDF4JBodyHandlers::toRDF4JFormat).map(format -> {
                try (final InputStream stream = new ByteArrayInputStream(responseInfo.body().array())) {
                    return Rio.parse(stream, responseInfo.uri().toString(), format);
                } catch (final IOException ex) {
                    throw new UncheckedIOException(
                        "An I/O error occurred while data was read from the InputStream", ex);
                }
            })
            .orElseGet(() -> new DynamicModelFactory().createEmptyModel());
    }

    /**
     * Populate a RDF4J {@link Repository} with an HTTP response.
     *
     * @return an HTTP body handler
     */
    public static Response.BodyHandler<Repository> ofRepository() {
        return responseInfo -> responseInfo.headers().firstValue("Content-Type")
            .map(RDF4JBodyHandlers::toRDF4JFormat).map(format -> {
                final Repository repository = new SailRepository(new MemoryStore());
                try (final InputStream stream = new ByteArrayInputStream(responseInfo.body().array());
                        final var conn = repository.getConnection()) {
                    conn.add(stream, responseInfo.uri().toString(), format);
                } catch (final IOException ex) {
                    throw new UncheckedIOException(
                        "An I/O error occurred while data was read from the InputStream", ex);
                }
                return repository;
            })
            .orElseGet(() -> new SailRepository(new MemoryStore()));
    }

    static RDFFormat toRDF4JFormat(final String mediaType) {
        return Rio.getParserFormatForMIMEType(mediaType).orElse(RDFFormat.TURTLE);
    }

    private RDF4JBodyHandlers() {
        //Prevent instantiation
    }
}
