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

import java.net.http.HttpResponse;

import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.graph.Factory;
import org.apache.jena.graph.Graph;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;

/**
 * {@link HttpResponse.BodyHandler} implementations for use with Jena types.
 */
public final class JenaBodyHandlers {

    /**
     * Populate a Jena {@link Model} with an HTTP response.
     *
     * @return an HTTP body handler
     */
    public static HttpResponse.BodyHandler<Model> ofModel() {
        return responseInfo -> responseInfo.headers().firstValue("Content-Type")
            .map(JenaBodyHandlers::toJenaLang).map(JenaBodySubscribers::ofModel)
            .orElseGet(() -> HttpResponse.BodySubscribers.replacing(ModelFactory.createDefaultModel()));
    }

    /**
     * Populate a Jena {@link Graph} with an HTTP response.
     *
     * @return an HTTP body handler
     */
    public static HttpResponse.BodyHandler<Graph> ofGraph() {
        return responseInfo -> responseInfo.headers().firstValue("Content-Type")
            .map(JenaBodyHandlers::toJenaLang).map(JenaBodySubscribers::ofGraph)
            .orElseGet(() -> HttpResponse.BodySubscribers.replacing(Factory.createDefaultGraph()));
    }

    /**
     * Populate a Jena {@link Dataset} with an HTTP response.
     *
     * @return an HTTP body handler
     */
    public static HttpResponse.BodyHandler<Dataset> ofDataset() {
        return responseInfo -> responseInfo.headers().firstValue("Content-Type")
            .map(JenaBodyHandlers::toJenaLang).map(JenaBodySubscribers::ofDataset)
            .orElseGet(() -> HttpResponse.BodySubscribers.replacing(DatasetFactory.create()));
    }

    static Lang toJenaLang(final String mediaType) {
        return RDFLanguages.contentTypeToLang(ContentType.create(mediaType));
    }

    private JenaBodyHandlers() {
        // Prevent instantiation
    }
}
