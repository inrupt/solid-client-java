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
package com.inrupt.client.solid;

import com.inrupt.client.rdf.Dataset;
import com.inrupt.client.rdf.Quad;
import com.inrupt.client.rdf.RDFNode;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Playlist extends SolidResource {

    public Playlist(final URI identifier, final Dataset dataset, final Metadata metadata) {
        super(identifier, dataset, metadata);
    }

    public String getTitle() {
        return getDataset().stream(Optional.empty(), RDFNode.namedNode(getIdentifier()),
                    RDFNode.namedNode(URI.create("http://purl.org/dc/terms/title")), null)
            .map(Quad::getObject)
            .filter(RDFNode::isLiteral)
            .map(RDFNode::getLiteral)
            .findFirst()
            .orElse("Untitled");
    }

    public List<URI> getSongs() {
        return getDataset().stream(Optional.empty(), RDFNode.namedNode(getIdentifier()),
                RDFNode.namedNode(URI.create("https://example.com/song")), null)
            .map(Quad::getObject)
            .filter(RDFNode::isNamedNode)
            .map(RDFNode::getURI)
            .collect(Collectors.toList());
    }
}

