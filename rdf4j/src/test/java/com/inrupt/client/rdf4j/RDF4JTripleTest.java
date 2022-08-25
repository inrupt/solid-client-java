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

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.junit.jupiter.api.Test;

public class RDF4JTripleTest {

    private RDF4JTriple rdf4jTriple;
    private static final ValueFactory VF = SimpleValueFactory.getInstance();
    private final IRI subject = VF.createIRI("http://example.com/test");
    private final IRI predicate = VF.createIRI("http://example.com/belongsTo");

    @Test
    void testValidTriple() {
        final var object = VF.createLiteral("TestCase1");
        rdf4jTriple = new RDF4JTriple(VF.createTriple(subject, predicate, object));

        assertAll("triple creation validation",
                () -> assertTrue(rdf4jTriple.getSubject().isNamedNode()),
                () -> assertEquals("http://example.com/test", rdf4jTriple.getSubject().getURI().toString()),
                () -> assertTrue(rdf4jTriple.getPredicate().isNamedNode()),
                () -> assertEquals("http://example.com/belongsTo", rdf4jTriple.getPredicate().getURI().toString()),
                () -> assertEquals("TestCase1", rdf4jTriple.getObject().getLiteral()));
    }

    @Test
    void testInvalidTriple() {
        assertThrows(NullPointerException.class,
            () -> new RDF4JTriple(VF.createTriple(null, null, null)));
    }

    @Test
    void testLiteralObject() {
        final var object = VF.createLiteral("TestCase1");
        rdf4jTriple = new RDF4JTriple(VF.createTriple(subject, predicate, object));

        assertEquals("TestCase1", rdf4jTriple.getObject().getLiteral());
    }

    @Test
    void testObjectAsIRI() {
        final var object = VF.createIRI("http://example.com/object");
        rdf4jTriple = new RDF4JTriple(VF.createTriple(subject, predicate, object));

        assertAll("object is a NamedNode",
                () -> assertTrue(rdf4jTriple.getObject().isNamedNode()),
                () -> assertEquals("http://example.com/object", rdf4jTriple.getObject().getURI().toString()));
    }

    @Test
    void testObjectAsLiteral() {
        final var object = VF.createLiteral(42);
        rdf4jTriple = new RDF4JTriple(VF.createTriple(subject, predicate, object));

        assertAll("object is literal",
                () -> assertTrue(rdf4jTriple.getObject().isLiteral()),
                () -> assertEquals("42", rdf4jTriple.getObject().getLiteral()));
    }

    @Test
    void testObjectWithDatatype() {
        final var object = VF.createLiteral("object", SKOS.CONCEPT);
        rdf4jTriple = new RDF4JTriple(VF.createTriple(subject, predicate, object));

        assertAll("object is literal with datatype",
                () -> assertEquals("object", rdf4jTriple.getObject().getLiteral()),
                () -> assertEquals(SKOS.CONCEPT.stringValue(), rdf4jTriple.getObject().getDatatype().toString()));
    }

    @Test
    void testObjectWithLanguage() {
        final var object = VF.createLiteral("object", "en");
        rdf4jTriple = new RDF4JTriple(VF.createTriple(subject, predicate, object));

        assertAll("object is literal with language",
            () -> assertEquals("object", rdf4jTriple.getObject().getLiteral()),
            () -> assertEquals("en", rdf4jTriple.getObject().getLanguage())
        );
    }

}
