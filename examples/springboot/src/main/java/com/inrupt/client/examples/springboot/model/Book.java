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

    public Book(URI identifier, Dataset dataset, Metadata metadata) {
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

        Node(RDFTerm original, Graph graph) {
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
