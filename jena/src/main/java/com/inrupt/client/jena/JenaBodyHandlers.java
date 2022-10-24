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
package com.inrupt.client.jena;

import com.inrupt.client.Response;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.graph.Factory;
import org.apache.jena.graph.Graph;
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

    /**
     * Populate a Jena {@link Model} with an HTTP response body.
     *
     * @return an HTTP body handler
     */
    public static Response.BodyHandler<Model> ofModel() {
        return responseInfo -> responseInfo.headers().firstValue("Content-Type")
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
     * Populate a Jena {@link Graph} with an HTTP response.
     *
     * @return an HTTP body handler
     */
    public static Response.BodyHandler<Graph> ofGraph() {
        return responseInfo -> responseInfo.headers().firstValue("Content-Type")
            .map(JenaBodyHandlers::toJenaLang).map(lang -> {
                try (final var input = new ByteArrayInputStream(responseInfo.body().array())) {
                    final var graph = Factory.createDefaultGraph();
                    RDFDataMgr.read(graph, input, responseInfo.uri().toString(), lang);
                    return graph;
                } catch (final IOException ex) {
                    throw new UncheckedIOException(
                            "An I/O error occurred while data was read from the InputStream into a Graph", ex);
                }
            })
            .orElseGet(Factory::createDefaultGraph);
    }

    /**
     * Populate a Jena {@link Dataset} with an HTTP response.
     *
     * @return an HTTP body handler
     */
    public static Response.BodyHandler<Dataset> ofDataset() {
        return responseInfo -> responseInfo.headers().firstValue("Content-Type")
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

    static Lang toJenaLang(final String mediaType) {
        return RDFLanguages.contentTypeToLang(ContentType.create(mediaType));
    }

    private JenaBodyHandlers() {
        // Prevent instantiation
    }
}
