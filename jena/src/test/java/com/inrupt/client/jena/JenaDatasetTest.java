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

import com.inrupt.client.rdf.RDFNode;

import java.net.URI;
import java.util.Optional;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class JenaDatasetTest {

    private static JenaDataset jenaDataset;
    private static RDFNode g_RDFNode;

    @BeforeAll
    static void setup() {

        final DatasetGraph dsg = DatasetGraphFactory.create();
        dsg.add(JenaTestModel.G_NODE,
            JenaTestModel.S_NODE,
            JenaTestModel.P_NODE,
            JenaTestModel.O_NODE);

        dsg.getDefaultGraph()
            .add(JenaTestModel.S1_NODE,
                JenaTestModel.P1_NODE,
                JenaTestModel.O1_NODE
            );

        jenaDataset = new JenaDataset(dsg);
    }

    @Test
    void testGetGraph() {
        assertAll("different versions of a graph",
            () -> {
                final Throwable exception = assertThrows(IllegalArgumentException.class,
                    () -> JenaDataset.getGraphName(Optional.of(RDFNode.literal("graph")))
                );
                assertEquals("Graph cannot be an RDF literal", exception.getMessage());
            },
            () -> assertEquals(
                "http://example.com/graph",
                JenaDataset.getGraphName(Optional.of(
                        RDFNode.namedNode(URI.create("http://example.com/graph")))
                    ).toString()
                ),
            () -> assertFalse(JenaDataset.getGraphName(Optional.of(RDFNode.blankNode())).isBlank()),
            () -> assertEquals(
                    defaultGraphNodeGenerated.toString(),
                    JenaDataset.getGraphName(Optional.of(RDFNode.blankNode())).toString()
                ),
            () -> assertEquals(Node.ANY, JenaDataset.getGraphName(null))
        );
    }

    @Test
    void testWithContextStream() {
        assertTrue(jenaDataset.stream(
            Optional.of(JenaTestModel.G_RDFNode),
            JenaTestModel.S_RDFNode,
            JenaTestModel.P_RDFNode,
            JenaTestModel.O_RDFNode
            ).findFirst().isPresent()
        );
        assertEquals(
            1,
            jenaDataset.stream(
                Optional.of(JenaTestModel.G_RDFNode),
                JenaTestModel.S_RDFNode,
                JenaTestModel.P_RDFNode,
                JenaTestModel.O_RDFNode
            ).count()
        );
        assertEquals(
            JenaTestModel.P_RDFNode.getURI(),
            jenaDataset.stream(
                Optional.of(JenaTestModel.G_RDFNode),
                JenaTestModel.S_RDFNode,
                JenaTestModel.P_RDFNode,
                JenaTestModel.O_RDFNode
            ).findFirst().get().getPredicate().getURI()
        );
    }

    @Test
    void testWithoutContextStream() {
        assertTrue(jenaDataset.stream(
            Optional.empty(),
            JenaTestModel.S_RDFNode,
            JenaTestModel.P_RDFNode,
            JenaTestModel.O_RDFNode
        ).findFirst().isPresent());
        assertEquals(
            1,
            jenaDataset.stream(
                Optional.empty(),
                JenaTestModel.S_RDFNode,
                JenaTestModel.P_RDFNode,
                JenaTestModel.O_RDFNode).count()
        );
        assertEquals(
            JenaTestModel.P_RDFNode.getURI(),
            jenaDataset.stream(
                Optional.empty(),
                JenaTestModel.S_RDFNode,
                JenaTestModel.P_RDFNode,
                JenaTestModel.O_RDFNode
            ).findFirst().get().getPredicate().getURI()
        );
        assertTrue(jenaDataset.stream(
            Optional.empty(),
            JenaTestModel.S_RDFNode,
            JenaTestModel.P_RDFNode,
            JenaTestModel.O_RDFNode
        ).findFirst().get().getGraphName().isPresent()
        );
        assertEquals(
            JenaTestModel.G_RDFNode.getURI(),
            jenaDataset.stream(
                Optional.empty(),
                JenaTestModel.S_RDFNode,
                JenaTestModel.P_RDFNode,
                JenaTestModel.O_RDFNode
            ).findFirst().get().getGraphName().get().getURI()
        );
    }

    @Test
    void testBNodeContextStream() {
        g_RDFNode = RDFNode.blankNode();

        assertFalse(
            jenaDataset.stream(
                Optional.of(g_RDFNode),
                JenaTestModel.S_RDFNode,
                JenaTestModel.P_RDFNode,
                JenaTestModel.O_RDFNode
            ).findFirst().isPresent()
        );
    }

    @Test
    void testWithNullContextStream() {
        assertTrue(
            jenaDataset.stream(null, JenaTestModel.S_RDFNode, JenaTestModel.P_RDFNode, JenaTestModel.O_RDFNode)
                .findFirst().isPresent()
        );
        assertEquals(
            1,
            jenaDataset.stream(null, JenaTestModel.S_RDFNode, JenaTestModel.P_RDFNode, JenaTestModel.O_RDFNode).count()
        );
        assertEquals(
            JenaTestModel.P_RDFNode.getURI(),
            jenaDataset.stream(null, JenaTestModel.S_RDFNode, JenaTestModel.P_RDFNode, JenaTestModel.O_RDFNode)
                .findFirst().get().getPredicate().getURI()
        );
        assertTrue(
            jenaDataset.stream(null, JenaTestModel.S_RDFNode, JenaTestModel.P_RDFNode, JenaTestModel.O_RDFNode)
                .findFirst().get().getGraphName().isPresent()
        );
        assertEquals(
            JenaTestModel.G_RDFNode.getURI(),
            jenaDataset.stream(null, JenaTestModel.S_RDFNode, JenaTestModel.P_RDFNode, JenaTestModel.O_RDFNode)
                .findFirst().get().getGraphName().get().getURI()
        );
    }

    @Test
    void testNoParamsStream() {
        assertTrue(jenaDataset.stream().findFirst().isPresent());
        assertEquals(2, jenaDataset.stream().count());
    }

    @Test
    void testWithInvalidContextStream() {
        g_RDFNode = RDFNode.literal("graph");

        final Throwable exception = assertThrows(
            IllegalArgumentException.class,
            () -> jenaDataset.stream(
                Optional.of(g_RDFNode),
                JenaTestModel.S_RDFNode,
                JenaTestModel.P_RDFNode,
                JenaTestModel.O_RDFNode
            )
        );
        assertEquals("Graph cannot be an RDF literal", exception.getMessage());
    }

}
