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

import com.inrupt.client.rdf.RDFNode;

import java.net.URI;
import java.util.Optional;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.vocabulary.RDF4J;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class RDF4jJDatasetTest {

    private static RDF4JDataset rdf4jDataset;
    private static RDFNode g_RDFNode;

    @BeforeAll
    static void setup() {
        final Statement st = TestModel.VF.createStatement(
            TestModel.S_RDF4J,
            TestModel.P_RDF4J,
            TestModel.O_RDF4J,
            TestModel.G_RDF4J
        );
        final Statement st1 = TestModel.VF.createStatement(
            TestModel.S1_RDF4J,
            TestModel.P1_RDF4J,
            TestModel.O1_RDF4J
        );

        final var repository = new SailRepository(new MemoryStore());

        try (final var conn = repository.getConnection()) {
            conn.add(st);
            conn.add(st1);
        }
        rdf4jDataset = new RDF4JDataset(repository);
    }

    @Test
    void testGetGraph() {
        assertAll("different versions of a graph",
            () -> {
                final Throwable exception = assertThrows(IllegalArgumentException.class,
                    () -> RDF4JDataset.getGraph(Optional.of(RDFNode.literal("graph")))
                );
                assertEquals("Graph cannot be an RDF literal", exception.getMessage());
            },
            () -> assertEquals(
                "http://example.com/graph",
                RDF4JDataset.getGraph(Optional.of(RDFNode.namedNode(URI.create("http://example.com/graph")))).toString()
                ),
            () -> assertFalse(RDF4JDataset.getGraph(Optional.of(RDFNode.blankNode())).isBNode()),
            () -> assertEquals(
                    RDF4J.NIL.toString(),
                    RDF4JDataset.getGraph(Optional.of(RDFNode.blankNode())).toString()
                ),
            () -> assertNull(RDF4JDataset.getGraph(null))
        );
    }

    @Test
    void testWithContextStream() {
        assertTrue(rdf4jDataset.stream(
            Optional.of(TestModel.G_RDFNode),
            TestModel.S_RDFNode,
            TestModel.P_RDFNode,
            TestModel.O_RDFNode
            ).findFirst().isPresent()
        );
        assertEquals(
            1,
            rdf4jDataset.stream(
                Optional.of(TestModel.G_RDFNode),
                TestModel.S_RDFNode,
                TestModel.P_RDFNode,
                TestModel.O_RDFNode
            ).count()
        );
        assertEquals(
            TestModel.P_RDFNode.getURI(),
            rdf4jDataset.stream(
                Optional.of(TestModel.G_RDFNode),
                TestModel.S_RDFNode,
                TestModel.P_RDFNode,
                TestModel.O_RDFNode
            ).findFirst().get().getPredicate().getURI()
        );
    }

    @Test
    void testWithoutContextStream() {
        assertTrue(rdf4jDataset.stream(
            Optional.empty(),
            TestModel.S_RDFNode,
            TestModel.P_RDFNode,
            TestModel.O_RDFNode
        ).findFirst().isPresent());
        assertEquals(
            1,
            rdf4jDataset.stream(
                Optional.empty(),
                TestModel.S_RDFNode,
                TestModel.P_RDFNode,
                TestModel.O_RDFNode).count()
        );
        assertEquals(
            TestModel.P_RDFNode.getURI(),
            rdf4jDataset.stream(
                Optional.empty(),
                TestModel.S_RDFNode,
                TestModel.P_RDFNode,
                TestModel.O_RDFNode
            ).findFirst().get().getPredicate().getURI()
        );
        assertTrue(rdf4jDataset.stream(
            Optional.empty(),
            TestModel.S_RDFNode,
            TestModel.P_RDFNode,
            TestModel.O_RDFNode
        ).findFirst().get().getGraphName().isPresent()
        );
        assertEquals(
            TestModel.G_RDFNode.getURI(),
            rdf4jDataset.stream(
                Optional.empty(),
                TestModel.S_RDFNode,
                TestModel.P_RDFNode,
                TestModel.O_RDFNode
            ).findFirst().get().getGraphName().get().getURI()
        );
    }

    @Test
    void testBNodeContextStream() {
        g_RDFNode = RDFNode.blankNode();

        assertFalse(
            rdf4jDataset.stream(
                Optional.of(g_RDFNode),
                TestModel.S_RDFNode,
                TestModel.P_RDFNode,
                TestModel.O_RDFNode
            ).findFirst().isPresent()
        );
    }

    @Test
    void testWithNullContextStream() {
        assertTrue(
            rdf4jDataset.stream(null, TestModel.S_RDFNode, TestModel.P_RDFNode, TestModel.O_RDFNode)
                .findFirst().isPresent()
        );
        assertEquals(
            1,
            rdf4jDataset.stream(null, TestModel.S_RDFNode, TestModel.P_RDFNode, TestModel.O_RDFNode).count()
        );
        assertEquals(
            TestModel.P_RDFNode.getURI(),
            rdf4jDataset.stream(null, TestModel.S_RDFNode, TestModel.P_RDFNode, TestModel.O_RDFNode)
                .findFirst().get().getPredicate().getURI()
        );
        assertTrue(
            rdf4jDataset.stream(null, TestModel.S_RDFNode, TestModel.P_RDFNode, TestModel.O_RDFNode)
                .findFirst().get().getGraphName().isPresent()
        );
        assertEquals(
            TestModel.G_RDFNode.getURI(),
            rdf4jDataset.stream(null, TestModel.S_RDFNode, TestModel.P_RDFNode, TestModel.O_RDFNode)
                .findFirst().get().getGraphName().get().getURI()
        );
    }

    @Test
    void testNoParamsStream() {
        assertTrue(rdf4jDataset.stream().findFirst().isPresent());
        assertEquals(2, rdf4jDataset.stream().count());
    }

    @Test
    void testWithInvalidContextStream() {
        g_RDFNode = RDFNode.literal("graph");

        final Throwable exception = assertThrows(
            IllegalArgumentException.class,
            () -> rdf4jDataset.stream(
                Optional.of(g_RDFNode),
                TestModel.S_RDFNode,
                TestModel.P_RDFNode,
                TestModel.O_RDFNode
            )
        );
        assertEquals("Graph cannot be an RDF literal", exception.getMessage());
    }


}
