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

import com.inrupt.client.webid.WebIdProfile;
import com.inrupt.rdf.wrapping.commons.ValueMappings;
import com.inrupt.rdf.wrapping.commons.WrapperIRI;

import java.net.URI;

import org.apache.commons.rdf.api.Dataset;
import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDFTerm;

public final class WebIdOwner extends WebIdProfile {

    private final IRI vcardName;
    private String token;

    public WebIdOwner(final URI identifier, final Dataset dataset) {
        super(identifier, dataset);
        this.vcardName = rdf.createIRI(Vocabulary.FN);
    }

    private String getVCARDName() {
        return new Node(rdf.createIRI(getIdentifier().toString()), getGraph()).getVCARDName();
    }

    public String geUserName() {
        return getVCARDName() != null ? getVCARDName() : getWebid();
    }

    private String getWebid() {
        return this.getIdentifier().toString();
    }

    public void setToken(final String token) {
        this.token = token;
    }

    public String getToken() {
        return this.token;
    }

    class Node extends WrapperIRI {

        Node(final RDFTerm original, final Graph graph) {
            super(original, graph);
        }

        String getVCARDName() {
            return anyOrNull(vcardName, ValueMappings::literalAsString);
        }
    }
}
