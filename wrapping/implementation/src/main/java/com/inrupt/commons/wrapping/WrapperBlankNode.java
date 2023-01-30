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
package com.inrupt.commons.wrapping;

import java.util.Objects;

import org.apache.commons.rdf.api.BlankNode;
import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.RDFTerm;

// TODO: Document
// TODO: Cover
public class WrapperBlankNode extends WrapperBlankNodeOrIRI implements BlankNode {
    private final BlankNode original;

    // TODO: Document
    protected WrapperBlankNode(final RDFTerm original, final Graph graph) {
        super(graph);

        Objects.requireNonNull(original, "Blank node is required");

        if (!(original instanceof BlankNode)) {
            // TODO: Throw specific exception
            throw new IllegalStateException("Original is not a blank node");
        }

        this.original = (BlankNode) original;
    }

    @Override
    public String uniqueReference() {
        return original.uniqueReference();
    }

    @Override
    public String ntriplesString() {
        return original.ntriplesString();
    }

    @Override
    public int hashCode() {
        return original.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return original.equals(obj);
    }
}
