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

import com.inrupt.client.ValidationResult;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.rdf.api.Dataset;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Literal;
import org.apache.commons.rdf.api.Quad;

public class Playlist extends SolidResource {

    private final IRI dcTitle;
    private final IRI exSong;
    private final IRI subject;

    public Playlist(final URI identifier, final Dataset dataset, final Metadata metadata) {
        super(identifier, dataset, metadata);

        this.subject = rdf.createIRI(identifier.toString());
        this.dcTitle = rdf.createIRI("http://purl.org/dc/terms/title");
        this.exSong = rdf.createIRI("https://example.com/song");
    }

    public String getTitle() {
        return stream(Optional.empty(), subject, dcTitle, null)
            .map(Quad::getObject).filter(Literal.class::isInstance).map(Literal.class::cast)
            .findFirst().map(Literal::getLexicalForm).orElse("Untitled");
    }

    public List<URI> getSongs() {
        return stream(Optional.empty(), subject, exSong, null)
            .map(Quad::getObject).filter(IRI.class::isInstance).map(IRI.class::cast)
            .map(IRI::getIRIString).map(URI::create).collect(Collectors.toList());
    }

    @Override
    public ValidationResult validate() {
        //EX: a playlist must contain at least one song
        if (getSongs().isEmpty()) {
            return new ValidationResult(false, "A playlist must contain at least one song.");
        }
        return new ValidationResult(true);
    }
}

