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

import com.inrupt.client.rdf.Graph;
import com.inrupt.client.rdf.RDFNode;
import com.inrupt.client.rdf.Triple;

import java.util.stream.Stream;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.RDF4J;

class RDF4JGraph implements Graph {

    private final Model model;

    public RDF4JGraph(final Model model) {
        this.model = model;
    }

    public Model asRDF4JModel() {
        return model;
    }

    @Override
    public Stream<Triple> stream(final RDFNode subject, final RDFNode predicate, final RDFNode object) {
        final var s = getSubject(subject);
        final var p = getPredicate(predicate);
        final var o = getObject(object);
        //TODO to test the context -> should work with the default graph here
        return model.filter(s, p, o, RDF4J.NIL).stream().map(st -> (RDF4JTriple)Values.triple(st));
    }

    @Override
    public Stream<Triple> stream() {
        //TODO to test the context -> should work with the default graph here
        return model.filter(null, null, null, RDF4J.NIL).stream().map(st -> (RDF4JTriple)Values.triple(st));
    }

    static Resource getSubject(final RDFNode subject) {
        if (subject != null) {
            final var factory = SimpleValueFactory.getInstance();
            if (subject.isLiteral()) {
                throw new IllegalArgumentException("Subject cannot be an RDF literal");
            }
            if (subject.isNamedNode()) {
                return factory.createIRI(subject.getURI().toString());
            }
            return factory.createBNode();
        }
        return null;
    }

    static IRI getPredicate(final RDFNode predicate) {
        if (predicate != null) {
            final var factory = SimpleValueFactory.getInstance();
            if (predicate.isLiteral()) {
                throw new IllegalArgumentException("Predicate cannot be an RDF literal");
            }
            if (predicate.isBlankNode()) {
                throw new IllegalArgumentException("Predicate cannot be a blank node");
            }
            return factory.createIRI(predicate.getURI().toString());
        }
        return null;
    }

    static Value getObject(final RDFNode object) {
        if (object != null) {
            final var factory = SimpleValueFactory.getInstance();
            if (object.isNamedNode()) {
                return factory.createIRI(object.getURI().toString());
            } else if (object.isLiteral()) {
                if (object.getDatatype() != null) {
                    return factory.createLiteral(object.getLiteral(),
                            factory.createIRI(object.getDatatype().toString())
                    );
                } else if (object.getLanguage() != null) {
                    return factory.createLiteral(object.getLiteral(), object.getLanguage());
                } else {
                    return factory.createLiteral(object.getLiteral());
                }
            } else {
                return factory.createBNode();
            }
        }
        return null;
    }
}
