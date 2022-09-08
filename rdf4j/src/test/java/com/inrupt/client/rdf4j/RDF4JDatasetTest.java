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
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class RDF4JDatasetTest {

    private static RDF4JDataset rdf4jDataset;
    private static RDFNode G_RDFNode;

    @BeforeAll
    static void setup() {
        final Statement st = RDF4JTestModel.VF.createStatement(
            RDF4JTestModel.S_RDF4J,
            RDF4JTestModel.P_RDF4J,
            RDF4JTestModel.O_RDF4J,
            RDF4JTestModel.G_RDF4J
        );
        final Statement st1 = RDF4JTestModel.VF.createStatement(
            RDF4JTestModel.S1_RDF4J,
            RDF4JTestModel.P1_RDF4J,
            RDF4JTestModel.O1_RDF4J
        );
        final Statement st2 = RDF4JTestModel.VF.createStatement(
            RDF4JTestModel.S2_RDF4J,
            RDF4JTestModel.P2_RDF4J,
            RDF4JTestModel.O2_RDF4J,
            null
        );

        final var repository = new SailRepository(new MemoryStore());

        try (final var conn = repository.getConnection()) {
            conn.add(st);
            conn.add(st1);
            conn.add(st2);
        }
        rdf4jDataset = new RDF4JDataset(repository);
    }

    @Test
    void testGetGraph() {
        assertAll("different versions of a graph",
            () -> {
                final Throwable exception = assertThrows(IllegalArgumentException.class,
                    () -> RDF4JDataset.getContexts(Optional.of(RDFNode.literal("graph")))
                );
                assertEquals("Graph cannot be an RDF literal", exception.getMessage());
            },
            () -> assertEquals(
                "http://example.com/graph",
                RDF4JDataset.getContexts(
                    Optional.of(RDFNode.namedNode(URI.create("http://example.com/graph"))))[0].toString()
                ),
            //TODO handle blank nodes
            /*() -> assertFalse(RDF4JDataset.getContexts(Optional.of(RDFNode.blankNode())).isBNode()),
            () -> assertEquals(
                    RDF4J.NIL.toString(),
                    RDF4JDataset.getContexts(Optional.of(RDFNode.blankNode())).toString()
                ),
                */
            () -> assertEquals(0, RDF4JDataset.getContexts(null).length)
        );
    }

    @Test
    void testWithContextStream() {
        assertTrue(rdf4jDataset.stream(
            Optional.of(RDF4JTestModel.G_RDFNode),
            RDF4JTestModel.S_RDFNode,
            RDF4JTestModel.P_RDFNode,
            RDF4JTestModel.O_RDFNode
            ).findFirst().isPresent()
        );
        assertEquals(
            1,
            rdf4jDataset.stream(
                Optional.of(RDF4JTestModel.G_RDFNode),
                RDF4JTestModel.S_RDFNode,
                RDF4JTestModel.P_RDFNode,
                RDF4JTestModel.O_RDFNode
            ).count()
        );
        assertEquals(
            RDF4JTestModel.P_RDFNode.getURI(),
            rdf4jDataset.stream(
                Optional.of(RDF4JTestModel.G_RDFNode),
                RDF4JTestModel.S_RDFNode,
                RDF4JTestModel.P_RDFNode,
                RDF4JTestModel.O_RDFNode
            ).findFirst().get().getPredicate().getURI()
        );
    }

    @Test
    void testWithEmptyContextStream() {
        assertTrue(rdf4jDataset.stream(
            Optional.empty(),
            RDF4JTestModel.S1_RDFNode,
            RDF4JTestModel.P1_RDFNode,
            RDF4JTestModel.O1_RDFNode
        ).findFirst().isPresent());
        assertEquals(
            1,
            rdf4jDataset.stream(
                Optional.empty(),
                RDF4JTestModel.S2_RDFNode,
                RDF4JTestModel.P2_RDFNode,
                RDF4JTestModel.O2_RDFNode).count()
        );
        assertEquals(
            RDF4JTestModel.P2_RDFNode.getURI(),
            rdf4jDataset.stream(
                Optional.empty(),
                RDF4JTestModel.S2_RDFNode,
                RDF4JTestModel.P2_RDFNode,
                RDF4JTestModel.O2_RDFNode
            ).findFirst().get().getPredicate().getURI()
        );
        assertFalse(rdf4jDataset.stream(
            Optional.empty(),
            RDF4JTestModel.S1_RDFNode,
            RDF4JTestModel.P1_RDFNode,
            RDF4JTestModel.O1_RDFNode
        ).findFirst().get().getGraphName().isPresent());
        assertFalse(
            rdf4jDataset.stream(
                Optional.empty(),
                RDF4JTestModel.S2_RDFNode,
                RDF4JTestModel.P2_RDFNode,
                RDF4JTestModel.O2_RDFNode
            ).findFirst().get().getGraphName().isPresent());
        assertFalse(
            rdf4jDataset.stream(
                Optional.empty(),
                RDF4JTestModel.S2_RDFNode,
                RDF4JTestModel.P2_RDFNode,
                RDF4JTestModel.O2_RDFNode
            ).findFirst().get().getGraphName().isPresent()
        );
    }

    @Disabled("until we can handle Blank nodes")
    @Test
    void testBNodeContextStream() {
        G_RDFNode = RDFNode.blankNode();

        assertFalse(
            rdf4jDataset.stream(
                Optional.of(G_RDFNode),
                RDF4JTestModel.S_RDFNode,
                RDF4JTestModel.P_RDFNode,
                RDF4JTestModel.O_RDFNode
            ).findFirst().isPresent()
        );
    }

    @Test
    void testWithNullContextStream() {
        assertTrue(
            rdf4jDataset.stream(null, RDF4JTestModel.S1_RDFNode, RDF4JTestModel.P1_RDFNode, RDF4JTestModel.O1_RDFNode)
                .findFirst().isPresent()
        );
        assertTrue(
            rdf4jDataset.stream(null, RDF4JTestModel.S_RDFNode, RDF4JTestModel.P_RDFNode, RDF4JTestModel.O_RDFNode)
                .findFirst().isPresent()
        );
        assertEquals(
            1,
            rdf4jDataset.stream(
                null, RDF4JTestModel.S_RDFNode, RDF4JTestModel.P_RDFNode, RDF4JTestModel.O_RDFNode
            ).count()
        );
        assertEquals(
            RDF4JTestModel.P_RDFNode.getURI(),
            rdf4jDataset.stream(null, RDF4JTestModel.S_RDFNode, RDF4JTestModel.P_RDFNode, RDF4JTestModel.O_RDFNode)
                .findFirst().get().getPredicate().getURI()
        );
        assertTrue(
            rdf4jDataset.stream(null, RDF4JTestModel.S_RDFNode, RDF4JTestModel.P_RDFNode, RDF4JTestModel.O_RDFNode)
                .findFirst().get().getGraphName().isPresent()
        );
        assertEquals(
            RDF4JTestModel.G_RDFNode.getURI(),
            rdf4jDataset.stream(null, RDF4JTestModel.S_RDFNode, RDF4JTestModel.P_RDFNode, RDF4JTestModel.O_RDFNode)
                .findFirst().get().getGraphName().get().getURI()
        );
    }

    @Test
    void testNoParamsStream() {
        assertTrue(rdf4jDataset.stream().findFirst().isPresent());
        assertEquals(3, rdf4jDataset.stream().count());
    }

    @Test
    void testWithInvalidContextStream() {
        G_RDFNode = RDFNode.literal("graph");

        final Throwable exception = assertThrows(
            IllegalArgumentException.class,
            () -> rdf4jDataset.stream(
                Optional.of(G_RDFNode),
                RDF4JTestModel.S_RDFNode,
                RDF4JTestModel.P_RDFNode,
                RDF4JTestModel.O_RDFNode
            )
        );
        assertEquals("Graph cannot be an RDF literal", exception.getMessage());
    }

    @Test
    void testStreamQuadWithGraph() {
        //----- stream with named graph
        assertEquals(RDF4JTestModel.G_VALUE,
            rdf4jDataset.stream(
                Optional.of(RDF4JTestModel.G_RDFNode),
                RDF4JTestModel.S_RDFNode,
                RDF4JTestModel.P_RDFNode,
                RDF4JTestModel.O_RDFNode
                ).findFirst().get().getGraphName().get().getURI().toString()
        );
        //----- stream with null
        assertEquals(RDF4JTestModel.G_VALUE,
            rdf4jDataset.stream(
                null,
                RDF4JTestModel.S_RDFNode,
                RDF4JTestModel.P_RDFNode,
                RDF4JTestModel.O_RDFNode
                ).findFirst().get().getGraphName().get().getURI().toString()
        );
        //---- stream with empty optional
        assertFalse(rdf4jDataset.stream(
                Optional.empty(),
                RDF4JTestModel.S_RDFNode,
                RDF4JTestModel.P_RDFNode,
                RDF4JTestModel.O_RDFNode
                ).findFirst().isPresent());
    }

    @Test
    void testStreamQuadWithNOGraph() {
        //----- stream with null
        assertFalse(rdf4jDataset.stream(
            null,
            RDF4JTestModel.S1_RDFNode,
            RDF4JTestModel.P1_RDFNode,
            RDF4JTestModel.O1_RDFNode
            ).findFirst().get().getGraphName().isPresent()
        );
        //---- stream with empty optional
        assertFalse(rdf4jDataset.stream(
            Optional.empty(),
            RDF4JTestModel.S1_RDFNode,
            RDF4JTestModel.P1_RDFNode,
            RDF4JTestModel.O1_RDFNode
            ).findFirst().get().getGraphName().isPresent()
        );
    }

    @Test
    void testStreamQuadWithNULLGraph() {
        //----- stream with null
        assertFalse(rdf4jDataset.stream(
            null,
            RDF4JTestModel.S2_RDFNode,
            RDF4JTestModel.P2_RDFNode,
            RDF4JTestModel.O2_RDFNode
            ).findFirst().get().getGraphName().isPresent()
        );
        //---- stream with empty optional
        assertFalse(rdf4jDataset.stream(
            Optional.empty(),
            RDF4JTestModel.S2_RDFNode,
            RDF4JTestModel.P2_RDFNode,
            RDF4JTestModel.O2_RDFNode
            ).findFirst().get().getGraphName().isPresent()
        );
    }

}
