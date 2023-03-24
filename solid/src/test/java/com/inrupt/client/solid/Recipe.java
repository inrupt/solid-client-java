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

import com.inrupt.client.RDFSource;
import com.inrupt.rdf.wrapping.commons.TermMappings;
import com.inrupt.rdf.wrapping.commons.ValueMappings;
import com.inrupt.rdf.wrapping.commons.WrapperIRI;

import java.net.URI;
import java.util.Set;

import org.apache.commons.rdf.api.Dataset;
import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDFTerm;

public class Recipe extends RDFSource {

    private final IRI dcTitle;
    private final IRI exIngredient;
    private final IRI exStep;
    private final Node subject;

    public Recipe(final URI identifier, final Dataset dataset) {
        super(identifier, dataset);

        this.subject = new Node(rdf.createIRI(identifier.toString()), getGraph());
        this.dcTitle = rdf.createIRI("http://purl.org/dc/terms/title");
        this.exStep = rdf.createIRI("https://example.com/step");
        this.exIngredient = rdf.createIRI("https://example.com/ingredient");
    }

    public String getTitle() {
        return subject.getTitle();
    }

    public void setTitle(final String value) {
        subject.setTitle(value);
    }

    public Set<String> getIngredients() {
        return subject.getIngredients();
    }

    public Set<String> getSteps() {
        return subject.getSteps();
    }

    class Node extends WrapperIRI {

        Node(final RDFTerm original, final Graph graph) {
            super(original, graph);
        }

        String getTitle() {
            return anyOrNull(dcTitle, ValueMappings::literalAsString);
        }

        void setTitle(final String value) {
            overwriteNullable(dcTitle, value, TermMappings::asStringLiteral);
        }

        Set<String> getIngredients() {
            return objects(exIngredient, TermMappings::asStringLiteral, ValueMappings::literalAsString);
        }

        Set<String> getSteps() {
            return objects(exStep, TermMappings::asStringLiteral, ValueMappings::literalAsString);
        }
    }
}

