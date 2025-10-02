/*
 * Copyright Inrupt Inc.
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
package com.inrupt.client.acp;

import static com.inrupt.client.vocabulary.RDF.type;

import com.inrupt.client.spi.RDFFactory;
import com.inrupt.client.vocabulary.ACP;
import com.inrupt.rdf.wrapping.commons.ValueMappings;
import com.inrupt.rdf.wrapping.commons.WrapperIRI;

import java.util.Set;

import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.api.RDFTerm;

public class AccessControl extends WrapperIRI {

    static final RDF rdf = RDFFactory.getInstance();

    public AccessControl(final RDFTerm original, final Graph graph) {
        super(original, graph);
        graph.add((IRI) original, rdf.createIRI(type.toString()), rdf.createIRI(ACP.AccessControl.toString()));
    }

    public static IRI asResource(final AccessControl accessControl, final Graph graph) {
        graph.add(accessControl, rdf.createIRI(type.toString()), rdf.createIRI(ACP.AccessControl.toString()));
        accessControl.apply().forEach(policy -> {
            graph.add(accessControl, rdf.createIRI(ACP.apply.toString()), policy);
            Policy.asResource(policy, graph);
        });

        return accessControl;
    }

    public Set<Policy> apply() {
        return objects(rdf.createIRI(ACP.apply.toString()),
                Policy::asResource, ValueMappings.as(Policy.class));
    }
}

