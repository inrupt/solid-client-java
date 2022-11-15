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

import java.util.UUID;

import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.vocabulary.RDF4J;
import org.junit.jupiter.api.Test;

class RDF4JQuadTest {

    private static RDF4JQuad rdf4jQuad;

    @Test
    void testGetGraphName() {
        final Resource graph = RDF4JTestModel.VF.createIRI("http://example.test/graphContext");
        rdf4jQuad = new RDF4JQuad(
            RDF4JTestModel.VF.createStatement(
                RDF4JTestModel.S_RDF4J,
                RDF4JTestModel.P_RDF4J,
                RDF4JTestModel.O_RDF4J,
                graph
            )
        );

        assertAll("quad creation validation",
            () -> assertTrue(rdf4jQuad.getGraphName().isPresent()),
            () -> assertTrue(rdf4jQuad.getGraphName().get().isNamedNode()),
            () -> assertEquals(
                "http://example.test/graphContext",
                rdf4jQuad.getGraphName().get().getURI().toString()
            )
        );
    }

    @Test
    void testGetBNodeWithIDGraphName() {
        final String nodeId = UUID.randomUUID().toString();
        final Resource graph = RDF4JTestModel.VF.createBNode(nodeId);
        rdf4jQuad = new RDF4JQuad(
            RDF4JTestModel.VF.createStatement(
                RDF4JTestModel.S_RDF4J,
                RDF4JTestModel.P_RDF4J,
                RDF4JTestModel.O_RDF4J,
                graph
            )
        );

        assertAll("quad creation validation",
            () -> assertTrue(rdf4jQuad.getGraphName().isPresent()),
            () -> assertTrue(rdf4jQuad.getGraphName().get().isBlankNode()),
            () -> assertEquals(
                nodeId,
                rdf4jQuad.getGraphName().get().getNodeId()
            )
        );
    }

    @Test
    void testGetBNodeGraphName() {
        final Resource graph = RDF4JTestModel.VF.createBNode();
        rdf4jQuad = new RDF4JQuad(
            RDF4JTestModel.VF.createStatement(
                RDF4JTestModel.S_RDF4J,
                RDF4JTestModel.P_RDF4J,
                RDF4JTestModel.O_RDF4J,
                graph
            )
        );

        assertAll("quad creation validation",
            () -> assertTrue(rdf4jQuad.getGraphName().isPresent()),
            () -> assertTrue(rdf4jQuad.getGraphName().get().isBlankNode())
        );
    }

    @Test
    void testNullGraphName() {
        rdf4jQuad = new RDF4JQuad(
            RDF4JTestModel.VF.createStatement(
                RDF4JTestModel.S_RDF4J,
                RDF4JTestModel.P_RDF4J,
                RDF4JTestModel.O_RDF4J,
                null
            )
        );

        assertAll("graph exists",
            () -> assertFalse(rdf4jQuad.getGraphName().isPresent())
        );
    }

    @Test
    void testDefaultGraphName() {
        rdf4jQuad = new RDF4JQuad(
            RDF4JTestModel.VF.createStatement(
                RDF4JTestModel.S_RDF4J,
                RDF4JTestModel.P_RDF4J,
                RDF4JTestModel.O_RDF4J,
                RDF4J.NIL
            )
        );
        assertFalse(rdf4jQuad.getGraphName().isPresent());
    }

}
