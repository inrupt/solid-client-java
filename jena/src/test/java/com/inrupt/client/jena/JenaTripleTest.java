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
package com.inrupt.client.jena;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.jupiter.api.Test;

class JenaTripleTest {

    private JenaTriple jenaTriple;

    @Test
    void testValidTriple() {
        jenaTriple = new JenaTriple(Triple.create(JenaTestModel.S_NODE, JenaTestModel.P_NODE, JenaTestModel.O_NODE));

        assertAll("triple creation validation",
                () -> assertTrue(jenaTriple.getSubject().isNamedNode()),
                () -> assertEquals(JenaTestModel.S_VALUE, jenaTriple.getSubject().getURI().toString()),
                () -> assertTrue(jenaTriple.getPredicate().isNamedNode()),
                () -> assertEquals(JenaTestModel.P_VALUE, jenaTriple.getPredicate().getURI().toString()),
                () -> assertEquals(JenaTestModel.O_VALUE, jenaTriple.getObject().getLiteral()));
    }

    @Test
    void testInvalidTriple() {
        assertThrows(UnsupportedOperationException.class,
            () -> new JenaTriple(Triple.create(null, null, null)));
    }

    @Test
    void testObjectAsIRI() {
        final var object = NodeFactory.createURI("http://example.com/object");
        jenaTriple = new JenaTriple(Triple.create(JenaTestModel.S_NODE, JenaTestModel.P_NODE, object));

        assertAll("object is a NamedNode",
                () -> assertTrue(jenaTriple.getObject().isNamedNode()),
                () -> assertEquals("http://example.com/object", jenaTriple.getObject().getURI().toString()));
    }

    @Test
    void testObjectAsLiteral() {
        final var object = ModelFactory.createDefaultModel().createTypedLiteral(42).asNode();
        jenaTriple = new JenaTriple(Triple.create(JenaTestModel.S_NODE, JenaTestModel.P_NODE, object));

        assertAll("object is literal",
                () -> assertTrue(jenaTriple.getObject().isLiteral()),
                () -> assertEquals("42", jenaTriple.getObject().getLiteral()));
    }

    @Test
    void testObjectWithDatatype() {
        final var object = ModelFactory.createDefaultModel().createTypedLiteral("object", XSDDatatype.XSD).asNode();
        jenaTriple = new JenaTriple(Triple.create(JenaTestModel.S_NODE, JenaTestModel.P_NODE, object));

        assertAll("object is literal with datatype",
                () -> assertEquals("object", jenaTriple.getObject().getLiteral()),
                () -> assertEquals(XSDDatatype.XSD, jenaTriple.getObject().getDatatype().toString()));
    }

    @Test
    void testObjectWithLanguage() {
        final var object = NodeFactory.createLiteral("object", "en");
        jenaTriple = new JenaTriple(Triple.create(JenaTestModel.S_NODE, JenaTestModel.P_NODE, object));

        assertAll("object is literal with language",
            () -> assertEquals("object", jenaTriple.getObject().getLiteral()),
            () -> assertEquals("en", jenaTriple.getObject().getLanguage())
        );
    }
}