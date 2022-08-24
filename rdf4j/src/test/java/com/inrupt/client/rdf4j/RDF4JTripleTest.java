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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Triple;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.junit.jupiter.api.Test;

public class RDF4JTripleTest {

    Triple exampleTriple;
    RDF4JTriple rdf4jTriple;
    static final ValueFactory VF = SimpleValueFactory.getInstance();
    final IRI subject = VF.createIRI("https://example.com/test");
    final IRI predicate = VF.createIRI("https://example.com/belongsTo");;

    @Test
    void testValidTriple() {
        final var object = VF.createLiteral("TestCase1");
        exampleTriple = VF.createTriple(subject, predicate, object);
        rdf4jTriple = new RDF4JTriple(exampleTriple);

        assertAll("valid triple",
                () -> assertEquals(rdf4jTriple.getSubject().isNamedNode(),
                                true),
                () -> assertEquals(rdf4jTriple.getSubject().getURI().toString(),
                                "https://example.com/test"),
                () -> assertEquals(rdf4jTriple.getPredicate().isNamedNode(),
                        true),
                () -> assertEquals(rdf4jTriple.getPredicate().getURI().toString(),
                        "https://example.com/belongsTo"),
                () -> assertEquals(rdf4jTriple.getObject().getLiteral(), "TestCase1"));
    }

    @Test
    void testLiteralObject() {
        final var object = VF.createLiteral("TestCase1");
        exampleTriple = VF.createTriple(subject, predicate, object);
        rdf4jTriple = new RDF4JTriple(exampleTriple);

        assertEquals(rdf4jTriple.getObject().getLiteral(), "TestCase1");
    }

    @Test
    void testObjectAsIRI() {
        final var object = VF.createIRI("https://example.com/object");
        exampleTriple = VF.createTriple(subject, predicate, object);
        rdf4jTriple = new RDF4JTriple(exampleTriple);

        assertAll("valid triple",
                () -> assertEquals(rdf4jTriple.getObject().isNamedNode(), true),
                () -> assertEquals(rdf4jTriple.getObject().getURI().toString(),
                        "https://example.com/object"));
    }

    @Test
    void testObjectAsLiteral() {
        final var object = VF.createLiteral(42);
        exampleTriple = VF.createTriple(subject, predicate, object);

        rdf4jTriple = new RDF4JTriple(exampleTriple);

        assertAll("valid triple",
                () -> assertEquals(rdf4jTriple.getObject().isLiteral(), true),
                () -> assertEquals(rdf4jTriple.getObject().getLiteral(), "42"));
    }

    @Test
    void testObjectWithDatatype() {
        final var object = VF.createLiteral("object", SKOS.CONCEPT);
        exampleTriple = VF.createTriple(subject, predicate, object);
        rdf4jTriple = new RDF4JTriple(exampleTriple);

        assertAll("valid triple",
                () -> assertEquals(rdf4jTriple.getObject().getLiteral(), "object"),
                () -> assertEquals(rdf4jTriple.getObject().getDatatype().toString(), SKOS.CONCEPT.stringValue()));
    }

    @Test
    void testObjectWithLanguage() {
        final var object = VF.createLiteral("object", "en");
        exampleTriple = VF.createTriple(subject, predicate, object);
        rdf4jTriple = new RDF4JTriple(exampleTriple);

        assertAll("valid triple",
            () -> assertEquals(rdf4jTriple.getObject().getLiteral(), "object"),
            () -> assertEquals(rdf4jTriple.getObject().getLanguage(), "en")
        );
    }

}
