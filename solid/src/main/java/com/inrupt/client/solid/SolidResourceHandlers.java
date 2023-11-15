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

import com.inrupt.client.Response;
import com.inrupt.client.spi.RdfService;
import com.inrupt.client.spi.ServiceProvider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.apache.commons.rdf.api.Dataset;
import org.apache.commons.rdf.api.RDFSyntax;

/**
 * Body handlers for Solid Resources.
 */
public final class SolidResourceHandlers {

    private static final RdfService service = ServiceProvider.getRdfService();
    private static final String CONTENT_TYPE = "Content-Type";

    /**
     * Transform an HTTP response into a Solid Resource.
     *
     * @return an HTTP body handler
     */
    public static Response.BodyHandler<SolidRDFSource> ofSolidRDFSource() {
        return responseInfo -> {
            final Metadata metadata = Metadata.of(responseInfo.uri(), responseInfo.headers());

            return responseInfo.headers().firstValue(CONTENT_TYPE)
                .flatMap(contentType ->
                        buildDataset(contentType, responseInfo.body().array(), responseInfo.uri().toString()))
                .map(dataset -> new SolidRDFSource(responseInfo.uri(), dataset, metadata))
                .orElseGet(() -> new SolidRDFSource(responseInfo.uri(), null, metadata));
        };
    }

    /**
     * Transform an HTTP response into a Solid Container.
     *
     * @return an HTTP body handler
     */
    public static Response.BodyHandler<SolidContainer> ofSolidContainer() {
        return responseInfo -> {
            final Metadata metadata = Metadata.of(responseInfo.uri(), responseInfo.headers());

            return responseInfo.headers().firstValue(CONTENT_TYPE)
                .flatMap(contentType ->
                        buildDataset(contentType, responseInfo.body().array(), responseInfo.uri().toString()))
                .map(dataset -> new SolidContainer(responseInfo.uri(), dataset, metadata))
                .orElseGet(() -> new SolidContainer(responseInfo.uri(), null, metadata));
        };
    }

    static Optional<Dataset> buildDataset(final String contentType, final byte[] data, final String baseUri) {
        return RDFSyntax.byMediaType(contentType)
            .map(syntax -> {
                try (final InputStream input = new ByteArrayInputStream(data)) {
                    return service.toDataset(syntax, input, baseUri);
                } catch (final IOException ex) {
                    throw new SolidResourceException("Error parsing Solid Container as RDF", ex);
                }
            });
    }

    private SolidResourceHandlers() {
        // Prevent instantiation
    }
}
