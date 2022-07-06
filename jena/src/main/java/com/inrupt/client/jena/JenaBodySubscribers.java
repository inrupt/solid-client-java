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

import org.apache.jena.graph.Graph;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;

public final class JenaBodySubscribers {

    public static HttpResponse.BodySubscriber<Model> ofModel(final Model model) {
        return ofModel(model, Lang.TURTLE);
    }

    public static HttpResponse.BodySubscriber<Model> ofModel(final Model model, final Lang lang) {
        // TODO implement
        return null;
    }

    public static HttpResponse.BodySubscriber<Graph> ofGraph(final Graph graph) {
        return ofGraph(graph, Lang.TURTLE);
    }

    public static HttpResponse.BodySubscriber<Graph> ofGraph(final Graph graph, final Lang lang) {
        // TODO implement
        return null;
    }

    public static HttpResponse.BodySubscriber<Dataset> ofDataset(final Dataset dataset) {
        return ofDataset(dataset, Lang.TURTLE);
    }

    public static HttpResponse.BodySubscriber<Dataset> ofDataset(final Dataset dataset, final Lang lang) {
        // TODO implement
        return null;
    }

    private JenaBodySubscribers() {
        // Prevent instantiation
    }
}
