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
package com.inrupt.client.rdf4j;

import com.inrupt.client.InruptClientException;
import com.inrupt.client.rdf.RDFNode;

import java.net.URI;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

final class RDF4JUtils {

    private static final ValueFactory VF = SimpleValueFactory.getInstance();

    public static IRI fromPredicate(final RDFNode predicate) {
        if (predicate.isNamedNode()) {
            return VF.createIRI(predicate.getURI().toString());
        }
        throw new InruptClientException("Invalid predicate");
    }

    public static Resource fromSubject(final RDFNode subject) {
        if (subject.isNamedNode()) {
            return VF.createIRI(subject.getURI().toString());
        } else if (subject.isBlankNode()) {
            final String nodeId = subject.getNodeId();
            if (nodeId == null) {
                return VF.createBNode();
            }
            return VF.createBNode(nodeId);
        }
        throw new InruptClientException("Invalid subject");
    }

    public static Value fromObject(final RDFNode object) {
        if (object.isNamedNode()) {
            return VF.createIRI(object.getURI().toString());
        } else if (object.isLiteral()) {
            final URI datatype = object.getDatatype();
            if (datatype != null) {
                return VF.createLiteral(object.getLiteral(), VF.createIRI(datatype.toString()));
            }

            final String lang = object.getLanguage();
            if (lang != null) {
                return VF.createLiteral(object.getLiteral(), lang);
            }

            return VF.createLiteral(object.getLiteral());
        }

        final String nodeId = object.getNodeId();
        if (nodeId == null) {
            return VF.createBNode();
        }
        return VF.createBNode(nodeId);
    }

    private RDF4JUtils() {
    }
}
