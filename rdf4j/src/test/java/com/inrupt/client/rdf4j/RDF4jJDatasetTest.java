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
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF4J;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class RDF4jJDatasetTest {

    private static final ValueFactory VF = SimpleValueFactory.getInstance();
    private static RDFNode testS;
    private static RDFNode testP;
    private static RDFNode testO;
    private static RDFNode testG;
    private static RDF4JDataset rdf4jDataset;

    @BeforeAll
    static void setup() {
        final var s = VF.createIRI("http://example.com/subject");
        final var p = VF.createIRI("http://example.com/predicate");
        final var o = VF.createLiteral("object");
        final var g = VF.createIRI("http://example.com/graph");
        final Statement st = VF.createStatement(s, p, o, g);

        final var s1 = VF.createIRI("http://example.com/subject1");
        final var p1 = VF.createIRI("http://example.com/predicate1");
        final var o1 = VF.createLiteral("object1");
        final Statement st1 = VF.createStatement(s1, p1, o1);

        final var repository = new SailRepository(new MemoryStore());

        try (final var conn = repository.getConnection()) {
            conn.add(st);
            conn.add(st1);
        }
        rdf4jDataset = new RDF4JDataset(repository);

        testS = RDFNode.namedNode(URI.create("http://example.com/subject"));
        testP = RDFNode.namedNode(URI.create("http://example.com/predicate"));
        testO = RDFNode.literal("object");
        testG = RDFNode.namedNode(URI.create("http://example.com/graph"));
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
        assertTrue(rdf4jDataset.stream(Optional.of(testG), testS, testP, testO).findFirst().isPresent());
        assertEquals(1, rdf4jDataset.stream(Optional.of(testG), testS, testP, testO).count());
        assertEquals(
            testP.getURI(),
            rdf4jDataset.stream(Optional.of(testG), testS, testP, testO).findFirst().get().getPredicate().getURI()
        );
    }

    @Test
    void testWithoutContextStream() {
        testG = RDFNode.namedNode(URI.create("http://example.com/graph"));

        assertTrue(rdf4jDataset.stream(Optional.empty(), testS, testP, testO).findFirst().isPresent());
        assertEquals(1, rdf4jDataset.stream(Optional.empty(), testS, testP, testO).count());
        assertEquals(
            testP.getURI(),
            rdf4jDataset.stream(Optional.empty(), testS, testP, testO).findFirst().get().getPredicate().getURI()
        );
        assertTrue(rdf4jDataset.stream(Optional.empty(), testS, testP, testO)
                            .findFirst()
                            .get()
                            .getGraphName()
                            .isPresent()
        );
        assertEquals(
            testG.getURI(),
            rdf4jDataset.stream(Optional.empty(), testS, testP, testO).findFirst().get().getGraphName().get().getURI()
        );
    }

    @Test
    void testBNodeContextStream() {
        testG = RDFNode.blankNode();

        assertFalse(rdf4jDataset.stream(Optional.of(testG), testS, testP, testO).findFirst().isPresent());
    }

    @Test
    void testWithNullContextStream() {
        testG = RDFNode.namedNode(URI.create("http://example.com/graph"));

        assertTrue(rdf4jDataset.stream(null, testS, testP, testO).findFirst().isPresent());
        assertEquals(1, rdf4jDataset.stream(null, testS, testP, testO).count());
        assertEquals(
            testP.getURI(),
            rdf4jDataset.stream(null, testS, testP, testO).findFirst().get().getPredicate().getURI()
        );
        assertTrue(rdf4jDataset.stream(null, testS, testP, testO).findFirst().get().getGraphName().isPresent());
        assertEquals(
            testG.getURI(),
            rdf4jDataset.stream(null, testS, testP, testO).findFirst().get().getGraphName().get().getURI()
        );
    }

    @Test
    void testNoParamsStream() {
        testG = RDFNode.namedNode(URI.create("http://example.com/graph"));

        assertTrue(rdf4jDataset.stream().findFirst().isPresent());
        assertEquals(2, rdf4jDataset.stream().count());
    }

    @Test
    void testWithInvalidContextStream() {
        testG = RDFNode.literal("graph");

        final Throwable exception = assertThrows(
            IllegalArgumentException.class,
            () -> rdf4jDataset.stream(Optional.of(testG), testS, testP, testO)
        );
        assertEquals("Graph cannot be an RDF literal", exception.getMessage());
    }


}
