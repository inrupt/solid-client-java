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
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF4J;
import org.junit.jupiter.api.Test;

public class RDF4JQuadTest {

    private static final ValueFactory VF = SimpleValueFactory.getInstance();
    private final IRI subject = VF.createIRI("http://example.com/test");
    private final IRI predicate = VF.createIRI("http://example.com/belongsTo");
    private final Value object = VF.createLiteral("object");
    private RDF4JQuad rdf4jQuad;

    @Test
    void testGetGraphName() {
        final var graph = VF.createIRI("http://example.com/graphContext");
        rdf4jQuad = new RDF4JQuad(VF.createStatement(subject, predicate, object, graph));

        assertAll("quad creation validation",
            () -> assertTrue(rdf4jQuad.getGraphName().isPresent()),
            () -> assertTrue(rdf4jQuad.getGraphName().get().isNamedNode()),
            () -> assertEquals("http://example.com/graphContext", rdf4jQuad.getGraphName().get().getURI().toString())
        );
    }

    @Test
    void testNullGraphName() {
        rdf4jQuad = new RDF4JQuad(VF.createStatement(subject, predicate, object, null));

        assertAll("graph exists",
            () -> assertFalse(rdf4jQuad.getGraphName().isPresent())
        );
    }

    @Test
    void testDefaultGraphName() {
        final RDF4JQuad rdf4jQuad = new RDF4JQuad(VF.createStatement(subject, predicate, object, RDF4J.NIL));

        assertAll("quad creation validation",
            () -> assertTrue(rdf4jQuad.getGraphName().isPresent()),
            () -> assertTrue(rdf4jQuad.getGraphName().get().isNamedNode()),
            () -> assertEquals(RDF4J.NIL.toString(), rdf4jQuad.getGraphName().get().getURI().toString())
        );
    }

    @Test
    void testInvalidQuad() {
        assertThrows(NullPointerException.class,
            () -> new RDF4JQuad(VF.createStatement(null, null, null, RDF4J.NIL))
        );
    }

}
