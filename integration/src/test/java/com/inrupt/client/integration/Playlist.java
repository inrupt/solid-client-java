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
package com.inrupt.client.integration;

import com.inrupt.client.solid.Metadata;
import com.inrupt.client.solid.SolidResource;
import com.inrupt.commons.wrapping.TermMappings;
import com.inrupt.commons.wrapping.ValueMappings;
import com.inrupt.commons.wrapping.WrapperIRI;

import java.net.URI;
import java.util.Set;

import org.apache.commons.rdf.api.Dataset;
import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDFTerm;

public class Playlist extends SolidResource {

    private final IRI dcTitle;
    private final IRI exSong;
    private final Node subject;

    public Playlist(final URI identifier, final Dataset dataset, final Metadata metadata) {
        super(identifier, dataset, metadata);

        this.subject = new Node(rdf.createIRI(identifier.toString()), getGraph());
        this.dcTitle = rdf.createIRI("http://purl.org/dc/terms/title");
        this.exSong = rdf.createIRI("https://example.com/song");
    }

    public String getTitle() {
        return subject.getTitle();
    }

    public void setTitle(final String value) {
        subject.setTitle(value);
    }

    public Set<URI> getSongs() {
        return subject.getSongs();
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

        Set<URI> getSongs() {
            return live(exSong, TermMappings::asIri, ValueMappings::iriAsUri);
        }
    }
}
