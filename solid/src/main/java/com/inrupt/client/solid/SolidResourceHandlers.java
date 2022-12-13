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

import com.inrupt.client.Headers.Link;
import com.inrupt.client.Headers.WacAllow;
import com.inrupt.client.Response;
import com.inrupt.client.Syntax;
import com.inrupt.client.spi.RdfService;
import com.inrupt.client.spi.ServiceProvider;
import com.inrupt.client.vocabulary.PIM;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Body handlers for Solid Resources.
 */
public final class SolidResourceHandlers {

    private static final RdfService service = ServiceProvider.getRdfService();
    private static final Map<String, Syntax> SYNTAX_MAPPING = buildMapping();

    /**
     * Transform an HTTP response into a Solid Resource.
     *
     * @return an HTTP body handler
     */
    public static Response.BodyHandler<SolidResource> ofSolidResource() {
        return responseInfo -> {
            final SolidResource.Builder builder = SolidResource.newResourceBuilder();
            responseInfo.headers().firstValue("Content-Type")
                .flatMap(SolidResourceHandlers::toSyntax)
                .ifPresent(syntax -> {
                    try (final InputStream input = new ByteArrayInputStream(responseInfo.body().array())) {
                        builder.dataset(service.toDataset(syntax, input, responseInfo.uri().toString()));
                    } catch (final IOException ex) {
                        throw new SolidResourceException("Error parsing Solid Resource as RDF", ex);
                    }
                });

            responseInfo.headers().allValues("Link").stream()
                .flatMap(l -> Link.parse(l).stream())
                .forEach(link -> {
                    if (link.getParameter("rel").contains("type")) {
                        builder.type(link.getUri());
                    } else if (link.getParameter("rel").contains(PIM.storage.toString())) {
                        builder.storage(link.getUri());
                    }
                });

            responseInfo.headers().allValues("WAC-Allow").stream()
                .map(WacAllow::parse)
                .map(WacAllow::getAccessParams)
                .flatMap(p -> p.entrySet().stream())
                .forEach(builder::wacAllow);

            responseInfo.headers().allValues("Allow").stream()
                .flatMap(s -> Arrays.stream(s.split(",")))
                .map(String::trim)
                .forEach(builder::allowedMethod);

            responseInfo.headers().allValues("Accept-Post").stream()
                .flatMap(s -> Arrays.stream(s.split(",")))
                .map(String::trim)
                .forEach(builder::allowedPostSyntax);

            responseInfo.headers().allValues("Accept-Patch").stream()
                .flatMap(s -> Arrays.stream(s.split(",")))
                .map(String::trim)
                .forEach(builder::allowedPatchSyntax);

            responseInfo.headers().allValues("Accept-Put").stream()
                .flatMap(s -> Arrays.stream(s.split(",")))
                .map(String::trim)
                .forEach(builder::allowedPutSyntax);

            return builder.build(responseInfo.uri());
        };
    }

    /**
     * Transform an HTTP response into a Solid Container.
     *
     * @return an HTTP body handler
     */
    public static Response.BodyHandler<SolidContainer> ofSolidContainer() {
        return responseInfo -> {
            final SolidContainer.Builder builder = SolidContainer.newContainerBuilder();
            responseInfo.headers().firstValue("Content-Type")
                .flatMap(SolidResourceHandlers::toSyntax)
                .ifPresent(syntax -> {
                    try (final InputStream input = new ByteArrayInputStream(responseInfo.body().array())) {
                        builder.dataset(service.toDataset(syntax, input, responseInfo.uri().toString()));
                    } catch (final IOException ex) {
                        throw new SolidResourceException("Error parsing Solid Container as RDF", ex);
                    }
                });

            responseInfo.headers().allValues("Link").stream()
                .flatMap(l -> Link.parse(l).stream())
                .forEach(link -> {
                    if (link.getParameter("rel").contains("type")) {
                        builder.type(link.getUri());
                    } else if (link.getParameter("rel").contains(PIM.storage.toString())) {
                        builder.storage(link.getUri());
                    }
                });

            responseInfo.headers().allValues("WAC-Allow").stream()
                .map(WacAllow::parse)
                .map(WacAllow::getAccessParams)
                .flatMap(p -> p.entrySet().stream())
                .forEach(builder::wacAllow);

            responseInfo.headers().allValues("Allow").stream()
                .flatMap(s -> Arrays.stream(s.split(",")))
                .map(String::trim)
                .forEach(builder::allowedMethod);

            responseInfo.headers().allValues("Accept-Post").stream()
                .flatMap(s -> Arrays.stream(s.split(",")))
                .map(String::trim)
                .forEach(builder::allowedPostSyntax);

            responseInfo.headers().allValues("Accept-Patch").stream()
                .flatMap(s -> Arrays.stream(s.split(",")))
                .map(String::trim)
                .forEach(builder::allowedPatchSyntax);

            responseInfo.headers().allValues("Accept-Put").stream()
                .flatMap(s -> Arrays.stream(s.split(",")))
                .map(String::trim)
                .forEach(builder::allowedPutSyntax);

            return builder.build(responseInfo.uri());
        };
    }

    static Optional<Syntax> toSyntax(final String contentType) {
        final String type = contentType.split(";")[0].trim();
        return Optional.ofNullable(SYNTAX_MAPPING.get(type));
    }

    static Map<String, Syntax> buildMapping() {
        final Map<String, Syntax> mapping = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        mapping.put("text/turtle", Syntax.TURTLE);
        mapping.put("application/n-triples", Syntax.NTRIPLES);
        mapping.put("application/trig", Syntax.TRIG);
        mapping.put("application/n-quads", Syntax.NQUADS);
        mapping.put("application/ld+json", Syntax.JSONLD);
        return Collections.unmodifiableMap(mapping);
    }

    private SolidResourceHandlers() {
        // Prevent instantiation
    }
}
