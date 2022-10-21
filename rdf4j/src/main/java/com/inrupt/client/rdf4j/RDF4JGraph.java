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

import com.inrupt.client.api.Graph;
import com.inrupt.client.api.RDFNode;
import com.inrupt.client.api.Triple;

import java.util.stream.Stream;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Values;

/**
 * The RDF4J implementation of a {@link Graph}.
 */
class RDF4JGraph implements Graph {

    private final Model model;
    private static final ValueFactory VF = SimpleValueFactory.getInstance();

    /**
     * Create a RDF4JGraph.
     *
     * @param model the RDF4J {@link Model}
     */
    public RDF4JGraph(final Model model) {
        this.model = model;
    }

    /**
     * Return the RDF4JGraph {@code model} value.
     *
     * @return the model as a RDF4J {@link Model}
     */
    public Model asRDF4JModel() {
        return model;
    }

    /**
     * Return the matching sequential stream of Triple with this RDF4JDataset as its source.
     *
     * @param subject the RDFNode subject, may be {@code null}
     * @param predicate the RDFNode predicate, may be {@code null}
     * @param object the RDFNode object, may be {@code null}
     * @return the matching quads as a sequential {@link Stream} of {@link Triple}s
     */
    @Override
    public Stream<Triple> stream(final RDFNode subject, final RDFNode predicate, final RDFNode object) {
        final Resource s = getSubject(subject);
        final IRI p = getPredicate(predicate);
        final Value o = getObject(object);
        return model.filter(s, p, o).stream().map(Values::triple).map(RDF4JTriple::new);
    }

    /**
     * Return a sequential stream of Triples with this RDF4JDataset as its source.
     *
     * @return a sequential {@link Stream} of {@link Triple}s
     */
    @Override
    public Stream<Triple> stream() {
        return model.filter(null, null, null).stream().map(Values::triple).map(RDF4JTriple::new);
    }

    /**
     * Retrieve the RDF4J subject from a RDFNode subject.
     *
     * @return the subject as a RDF4J {@link Resource}, may be {@code null}
     */
    static Resource getSubject(final RDFNode subject) {
        if (subject != null) {
            if (subject.isLiteral()) {
                throw new IllegalArgumentException("Subject cannot be an RDF literal");
            }
            if (subject.isNamedNode()) {
                return VF.createIRI(subject.getURI().toString());
            }
            return VF.createBNode(subject.getNodeId());
        }
        return null;
    }

    /**
     * Retrieve the RDF4J predicate from a RDFNode predicate.
     *
     * @return the predicate as a RDF4J {@link IRI}, may be {@code null}
     */
    static IRI getPredicate(final RDFNode predicate) {
        if (predicate != null) {
            if (predicate.isLiteral()) {
                throw new IllegalArgumentException("Predicate cannot be an RDF literal");
            }
            if (predicate.isBlankNode()) {
                throw new IllegalArgumentException("Predicate cannot be a blank node");
            }
            return VF.createIRI(predicate.getURI().toString());
        }
        return null;
    }

    /**
     * Retrieve the RDF4J object from a RDFNode object.
     *
     * @return the object as a RDF4J {@link Value}, may be {@code null}
     */
    static Value getObject(final RDFNode object) {
        if (object != null) {
            if (object.isNamedNode()) {
                return VF.createIRI(object.getURI().toString());
            } else if (object.isLiteral()) {
                if (object.getDatatype() != null) {
                    return VF.createLiteral(object.getLiteral(),
                            VF.createIRI(object.getDatatype().toString())
                    );
                } else if (object.getLanguage() != null) {
                    return VF.createLiteral(object.getLiteral(), object.getLanguage());
                } else {
                    return VF.createLiteral(object.getLiteral());
                }
            } else {
                return VF.createBNode(object.getNodeId());
            }
        }
        return null;
    }
}
