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
package com.inrupt.client.jena;

import com.inrupt.client.ClientHttpException;
import com.inrupt.client.Response;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphMemFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;

/**
 * {@link Response.BodyHandler} implementations for use with Jena types.
 */
public final class JenaBodyHandlers {

    private static final String CONTENT_TYPE = "Content-Type";

    private static void throwOnError(final Response.ResponseInfo responseInfo) {
        if (!Response.isSuccess(responseInfo.statusCode())) {
            throw new ClientHttpException(
                    "An HTTP error was encountered mapping to a Jena entity.",
                    responseInfo.uri(),
                    responseInfo.statusCode(),
                    responseInfo.headers(),
                    new String(responseInfo.body().array(), StandardCharsets.UTF_8)
            );
        }
    }

    private static Model responseToModel(final Response.ResponseInfo responseInfo) {
        return responseInfo.headers().firstValue(CONTENT_TYPE)
            .map(JenaBodyHandlers::toJenaLang).map(lang -> {
                try (final var input = new ByteArrayInputStream(responseInfo.body().array())) {
                    final var model = ModelFactory.createDefaultModel();
                    RDFDataMgr.read(model, input, responseInfo.uri().toString(), lang);
                    return model;
                } catch (final IOException ex) {
                    throw new UncheckedIOException(
                            "An I/O error occurred while data was read from the InputStream into a Model", ex);
                }
            })
            .orElseGet(ModelFactory::createDefaultModel);
    }

    /**
     * Populate a Jena {@link Model} with an HTTP response body.
     *
     * @return an HTTP body handler
     * @deprecated Use {@link JenaBodyHandlers#ofJenaModel()} instead for consistent HTTP error handling.
     */
    public static Response.BodyHandler<Model> ofModel() {
        return JenaBodyHandlers::responseToModel;
    }

    /**
     * Populate a Jena {@link Model} with an HTTP response body.
     *
     * @return an HTTP body handler
     */
    public static Response.BodyHandler<Model> ofJenaModel() {
        return responseInfo -> {
            JenaBodyHandlers.throwOnError(responseInfo);
            return JenaBodyHandlers.responseToModel(responseInfo);
        };
    }

    private static Graph responseToGraph(final Response.ResponseInfo responseInfo) {
        return responseInfo.headers().firstValue(CONTENT_TYPE)
            .map(JenaBodyHandlers::toJenaLang).map(lang -> {
                try (final var input = new ByteArrayInputStream(responseInfo.body().array())) {
                    final var graph = GraphMemFactory.createDefaultGraph();
                    RDFDataMgr.read(graph, input, responseInfo.uri().toString(), lang);
                    return graph;
                } catch (final IOException ex) {
                    throw new UncheckedIOException(
                            "An I/O error occurred while data was read from the InputStream into a Graph", ex);
                }
            })
            .orElseGet(GraphMemFactory::createDefaultGraph);
    }

    /**
     * Populate a Jena {@link Graph} with an HTTP response.
     *
     * @return an HTTP body handler
     * @deprecated Use {@link JenaBodyHandlers#ofJenaGraph} instead for consistent HTTP error handling.
     */
    public static Response.BodyHandler<Graph> ofGraph() {
        return JenaBodyHandlers::responseToGraph;
    }

    /**
     * Populate a Jena {@link Graph} with an HTTP response.
     *
     * @return an HTTP body handler
     */
    public static Response.BodyHandler<Graph> ofJenaGraph() {
        return responseInfo -> {
            JenaBodyHandlers.throwOnError(responseInfo);
            return JenaBodyHandlers.responseToGraph(responseInfo);
        };
    }

    private static Dataset responseToDataset(final Response.ResponseInfo responseInfo) {
        return responseInfo.headers().firstValue(CONTENT_TYPE)
                .map(JenaBodyHandlers::toJenaLang).map(lang -> {
                    try (final var input = new ByteArrayInputStream(responseInfo.body().array())) {
                        final var dataset = DatasetFactory.create();
                        RDFDataMgr.read(dataset, input, responseInfo.uri().toString(), lang);
                        return dataset;
                    } catch (final IOException ex) {
                        throw new UncheckedIOException(
                                "An I/O error occurred while data was read from the InputStream into a Dataset", ex);
                    }
                })
                .orElseGet(DatasetFactory::create);
    }

    /**
     * Populate a Jena {@link Dataset} with an HTTP response.
     *
     * @return an HTTP body handler
     * @deprecated Use {@link JenaBodyHandlers#ofJenaDataset} instead for consistent HTTP error handling.
     */
    public static Response.BodyHandler<Dataset> ofDataset() {
        return JenaBodyHandlers::responseToDataset;
    }

    /**
     * Populate a Jena {@link Dataset} with an HTTP response.
     *
     * @return an HTTP body handler
     */
    public static Response.BodyHandler<Dataset> ofJenaDataset() {
        return responseInfo -> {
            JenaBodyHandlers.throwOnError(responseInfo);
            return JenaBodyHandlers.responseToDataset(responseInfo);
        };
    }

    static Lang toJenaLang(final String mediaType) {
        return RDFLanguages.contentTypeToLang(ContentType.create(mediaType));
    }

    private JenaBodyHandlers() {
        // Prevent instantiation
    }
}
