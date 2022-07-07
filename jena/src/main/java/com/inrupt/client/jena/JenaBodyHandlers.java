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
import org.apache.jena.graph.Graph;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RiotException;

/**
 * Body handlers for Jena types.
 */
public final class JenaBodyHandlers {

    /**
     * Populate a Jena {@link Model} with an HTTP response.
     *
     * @return an HTTP body handler
     */
    public static HttpResponse.BodyHandler<Model> ofModel() {
        return new HttpResponse.BodyHandler<Model>() {
            @Override
            public HttpResponse.BodySubscriber<Model> apply(final HttpResponse.ResponseInfo responseInfo) {
                final var lang = responseInfo.headers().firstValue("Content-Type")
                    .orElseThrow(() -> new RiotException("Missing content-type header from response"));
                return JenaBodySubscribers.ofModel(toJenaLang(lang));
            }
        };
    }

    /**
     * Populate a Jena {@link Graph} with an HTTP response.
     *
     * @return an HTTP body handler
     */
    public static HttpResponse.BodyHandler<Graph> ofGraph() {
        return new HttpResponse.BodyHandler<Graph>() {
            @Override
            public HttpResponse.BodySubscriber<Graph> apply(final HttpResponse.ResponseInfo responseInfo) {
                final var lang = responseInfo.headers().firstValue("Content-Type")
                    .orElseThrow(() -> new RiotException("Missing content-type header from response"));
                return JenaBodySubscribers.ofGraph(toJenaLang(lang));
            }
        };
    }

    /**
     * Populate a Jena {@link Dataset} with an HTTP response.
     *
     * @return an HTTP body handler
     */
    public static HttpResponse.BodyHandler<Dataset> ofDataset() {
        return new HttpResponse.BodyHandler<Dataset>() {
            @Override
            public HttpResponse.BodySubscriber<Dataset> apply(final HttpResponse.ResponseInfo responseInfo) {
                final var lang = responseInfo.headers().firstValue("Content-Type")
                    .orElseThrow(() -> new RiotException("Missing content-type header from response"));
                return JenaBodySubscribers.ofDataset(toJenaLang(lang));
            }
        };
    }

    static Lang toJenaLang(final String mediaType) {
        return RDFLanguages.contentTypeToLang(ContentType.create(mediaType));
    }

    private JenaBodyHandlers() {
        // Prevent instantiation
    }
}
