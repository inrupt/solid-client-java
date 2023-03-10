package com.inrupt.client.examples.springboot.model;

import com.inrupt.client.solid.Metadata;
import com.inrupt.client.solid.SolidResource;
import com.inrupt.rdf.wrapping.commons.TermMappings;
import com.inrupt.rdf.wrapping.commons.ValueMapping;
import com.inrupt.rdf.wrapping.commons.ValueMappings;
import com.inrupt.rdf.wrapping.commons.WrapperIRI;
import java.net.URI;
import java.util.Set;
import org.apache.commons.rdf.api.Dataset;
import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDFTerm;

public class BookLibrary extends SolidResource {

    private Node bookLibraryId;
    private final IRI contains = rdf.createIRI(Vocabulary.CONTAINS_BOOK);

    public BookLibrary(URI identifier, Dataset dataset, Metadata metadata) {
        super(identifier, dataset, metadata);

        this.bookLibraryId = new Node(rdf.createIRI(identifier.toString()), getGraph());
    }

    public Set<URI> getAllBooks() {
        return bookLibraryId.getAllBooks();
    }

    class Node extends WrapperIRI {

        Node(RDFTerm original, Graph graph) {
            super(original, graph);
        }

        Set<URI> getAllBooks() {
            return objects(contains, TermMappings::asIri, ValueMappings::iriAsUri);
        }
        
    }
    
    
}
