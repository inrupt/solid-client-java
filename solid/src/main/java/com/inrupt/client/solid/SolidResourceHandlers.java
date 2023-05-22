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

import com.inrupt.client.Headers;
import com.inrupt.client.Response;
import com.inrupt.client.spi.RdfService;
import com.inrupt.client.spi.ServiceProvider;
import com.inrupt.client.vocabulary.PIM;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Optional;

import org.apache.commons.rdf.api.Dataset;
import org.apache.commons.rdf.api.RDFSyntax;

/**
 * Body handlers for Solid Resources.
 */
public final class SolidResourceHandlers {

    private static final URI STORAGE = URI.create(PIM.getNamespace() + "Storage");
    private static final RdfService service = ServiceProvider.getRdfService();
    private static final String CONTENT_TYPE = "Content-Type";

    /**
     * Transform an HTTP response into a Solid Resource.
     *
     * @return an HTTP body handler
     */
    public static Response.BodyHandler<SolidRDFSource> ofSolidRDFSource() {
        return responseInfo -> {
            final Metadata metadata = buildMetadata(responseInfo.uri(), responseInfo.headers());

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
            final Metadata metadata = buildMetadata(responseInfo.uri(), responseInfo.headers());

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

    static Metadata buildMetadata(final URI uri, final Headers headers) {
        // Gather metadata from HTTP headers
        final Metadata.Builder metadata = Metadata.newBuilder();
        headers.allValues("Link").stream()
            .flatMap(l -> Headers.Link.parse(l).stream())
            .forEach(link -> {
                if (link.getParameter("rel").contains("type")) {
                    if ((link.getUri().equals(STORAGE))) {
                        metadata.storage(uri);
                    }
                    metadata.type(link.getUri());
                } else if (link.getParameter("rel").contains("acl")) {
                    metadata.acl(link.getUri());
                } else if (link.getParameter("rel").contains(PIM.storage.toString())) {
                    metadata.storage(link.getUri());
                }
            });

        headers.allValues("WAC-Allow").stream()
            .map(Headers.WacAllow::parse)
            .map(Headers.WacAllow::getAccessParams)
            .flatMap(p -> p.entrySet().stream())
            .forEach(metadata::wacAllow);

        headers.allValues("Allow").stream()
            .flatMap(s -> Arrays.stream(s.split(",")))
            .map(String::trim)
            .forEach(metadata::allowedMethod);

        headers.allValues("Accept-Post").stream()
            .flatMap(s -> Arrays.stream(s.split(",")))
            .map(String::trim)
            .forEach(metadata::allowedPostSyntax);

        headers.allValues("Accept-Patch").stream()
            .flatMap(s -> Arrays.stream(s.split(",")))
            .map(String::trim)
            .forEach(metadata::allowedPatchSyntax);

        headers.allValues("Accept-Put").stream()
            .flatMap(s -> Arrays.stream(s.split(",")))
            .map(String::trim)
            .forEach(metadata::allowedPutSyntax);

        metadata.contentType(headers.firstValue(CONTENT_TYPE).orElse("application/octet-stream"));

        return metadata.build();
    }

    private SolidResourceHandlers() {
        // Prevent instantiation
    }
}
