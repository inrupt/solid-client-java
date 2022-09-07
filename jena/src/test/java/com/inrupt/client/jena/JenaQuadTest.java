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

import static org.apache.jena.sparql.core.Quad.defaultGraphNodeGenerated;
import static org.junit.jupiter.api.Assertions.*;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Quad;
import org.junit.jupiter.api.Test;

class JenaQuadTest {

    private static JenaQuad jenaQuad;

    @Test
    void testGetGraphName() {
        final var graph = NodeFactory.createURI("http://example.com/graphContext");
        jenaQuad = new JenaQuad(
            Quad.create(
                graph,
                JenaTestModel.S_NODE,
                JenaTestModel.P_NODE,
                JenaTestModel.O_NODE
            )
        );

        assertAll("quad creation validation",
            () -> assertTrue(jenaQuad.getGraphName().isPresent()),
            () -> assertTrue(jenaQuad.getGraphName().get().isNamedNode()),
            () -> assertEquals(
                "http://example.com/graphContext",
                jenaQuad.getGraphName().get().getURI().toString()
            )
        );
    }

    @Test
    void testGetBNodeGraphName() {
        final var graph = NodeFactory.createBlankNode("testID");
        jenaQuad = new JenaQuad(
            Quad.create(
                graph,
                JenaTestModel.S_NODE,
                JenaTestModel.P_NODE,
                JenaTestModel.O_NODE
            )
        );

        assertAll("quad creation validation",
            () -> assertTrue(jenaQuad.getGraphName().isPresent()),
            () -> assertTrue(jenaQuad.getGraphName().get().isBlankNode())
            //TODO missing creation of blank nodes with label so we can return it
            /*() -> assertEquals(
                "testID",
                JenaQuad.getGraphName().get().get..().toString()
            )*/
        );
    }

    @Test
    void testNullGraphName() {
        jenaQuad = new JenaQuad(
            Quad.create(null, JenaTestModel.S_NODE, JenaTestModel.P_NODE, JenaTestModel.O_NODE)
        );

        assertAll("graph exists",
            () -> assertFalse(jenaQuad.getGraphName().isPresent())
        );
    }

    @Test
    void testDefaultGraphName() {
        jenaQuad = new JenaQuad(
            Quad.create(defaultGraphNodeGenerated, JenaTestModel.S_NODE, JenaTestModel.P_NODE, JenaTestModel.O_NODE)
        );
        assertFalse(jenaQuad.getGraphName().isPresent());
    }

    @Test
    void testInvalidQuad() {
        assertThrows(UnsupportedOperationException.class,
            () -> new JenaQuad(Quad.create(defaultGraphNodeGenerated, null, null, null))
        );
    }

}
