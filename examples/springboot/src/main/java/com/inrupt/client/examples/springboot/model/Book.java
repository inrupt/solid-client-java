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
package com.inrupt.client.examples.springboot.model;

import com.inrupt.client.solid.Metadata;
import com.inrupt.client.solid.SolidResource;
import com.inrupt.rdf.wrapping.commons.TermMappings;
import com.inrupt.rdf.wrapping.commons.ValueMappings;
import com.inrupt.rdf.wrapping.commons.WrapperIRI;

import java.net.URI;

import org.apache.commons.rdf.api.Dataset;
import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDFTerm;

public class Book extends SolidResource {

    private final Node bookId;
    private final IRI title = rdf.createIRI(Vocabulary.DC_TITLE);
    private final IRI author = rdf.createIRI(Vocabulary.BOOK_AUTHOR);
    private final IRI description = rdf.createIRI(Vocabulary.BOOK_DESCRIPTION);

    public Book(final URI identifier, final Dataset dataset, final Metadata metadata) {
        super(identifier, dataset, metadata);

        this.bookId = new Node(rdf.createIRI(identifier.toString()), getGraph());
    }

    public void setTitle(final String title) {
        bookId.setTitle(title);
    }

    public String getTitle() {
        return bookId.getTitle();
    }

    public void setAuthor(final String author) {
        bookId.setAuthor(author);
    }

    public String getAuthor() {
        return bookId.getAuthor();
    }

    public void setDescription(final String description) {
        bookId.setDescription(description);
    }

    public String getDescription() {
        return bookId.getDescription();
    }

    class Node extends WrapperIRI {

        Node(final RDFTerm original, final Graph graph) {
            super(original, graph);
        }

        String getTitle() {
            return anyOrNull(title, ValueMappings::literalAsString);
        }

        void setTitle(final String value) {
            overwriteNullable(title, value, TermMappings::asStringLiteral);
        }

        String getDescription() {
            return anyOrNull(description, ValueMappings::literalAsString);
        }

        void setDescription(final String value) {
            overwriteNullable(description, value, TermMappings::asStringLiteral);
        }

        String getAuthor() {
            return anyOrNull(author, ValueMappings::literalAsString);
        }

        void setAuthor(final String value) {
            overwriteNullable(author, value, TermMappings::asStringLiteral);
        }

    }
}
