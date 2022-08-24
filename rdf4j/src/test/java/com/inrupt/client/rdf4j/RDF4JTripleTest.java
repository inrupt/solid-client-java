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
    private final IRI subject = VF.createIRI("https://example.com/test");
    private final IRI predicate = VF.createIRI("https://example.com/belongsTo");;

    @Test
    void testValidTriple() {
        final var object = VF.createLiteral("TestCase1");
        rdf4jTriple = new RDF4JTriple(VF.createTriple(subject, predicate, object));

        assertAll("triple creation validation",
                () -> assertTrue(rdf4jTriple.getSubject().isNamedNode()),
                () -> assertEquals(rdf4jTriple.getSubject().getURI().toString(),
                                "https://example.com/test"),
                () -> assertTrue(rdf4jTriple.getPredicate().isNamedNode()),
                () -> assertEquals(rdf4jTriple.getPredicate().getURI().toString(),
                        "https://example.com/belongsTo"),
                () -> assertEquals(rdf4jTriple.getObject().getLiteral(), "TestCase1"));
    }

    @Test
    void testLiteralObject() {
        final var object = VF.createLiteral("TestCase1");
        rdf4jTriple = new RDF4JTriple(VF.createTriple(subject, predicate, object));

        assertEquals(rdf4jTriple.getObject().getLiteral(), "TestCase1");
    }

    @Test
    void testObjectAsIRI() {
        final var object = VF.createIRI("https://example.com/object");
        rdf4jTriple = new RDF4JTriple(VF.createTriple(subject, predicate, object));

        assertAll("object is a NamedNode",
                () -> assertTrue(rdf4jTriple.getObject().isNamedNode()),
                () -> assertEquals(rdf4jTriple.getObject().getURI().toString(),
                        "https://example.com/object"));
    }

    @Test
    void testObjectAsLiteral() {
        final var object = VF.createLiteral(42);
        rdf4jTriple = new RDF4JTriple(VF.createTriple(subject, predicate, object));

        assertAll("object is literal",
                () -> assertTrue(rdf4jTriple.getObject().isLiteral()),
                () -> assertEquals(rdf4jTriple.getObject().getLiteral(), "42"));
    }

    @Test
    void testObjectWithDatatype() {
        final var object = VF.createLiteral("object", SKOS.CONCEPT);
        rdf4jTriple = new RDF4JTriple(VF.createTriple(subject, predicate, object));

        assertAll("object is literal with datatype",
                () -> assertEquals(rdf4jTriple.getObject().getLiteral(), "object"),
                () -> assertEquals(rdf4jTriple.getObject().getDatatype().toString(), SKOS.CONCEPT.stringValue()));
    }

    @Test
    void testObjectWithLanguage() {
        final var object = VF.createLiteral("object", "en");
        rdf4jTriple = new RDF4JTriple(VF.createTriple(subject, predicate, object));

        assertAll("object is literal with language",
            () -> assertEquals(rdf4jTriple.getObject().getLiteral(), "object"),
            () -> assertEquals(rdf4jTriple.getObject().getLanguage(), "en")
        );
    }

}
