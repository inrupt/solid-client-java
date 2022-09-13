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

import com.inrupt.client.rdf.RDFNode;

import java.net.URI;
import java.util.stream.Collectors;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class JenaGraphTest {

    private static JenaGraph jenaGraph;
    private static final Model model = ModelFactory.createDefaultModel();

    private static final Resource S_JENA = model.createResource(JenaTestModel.S_VALUE);
    private static final Property P_JENA = model.createProperty(JenaTestModel.P_VALUE);
    private static final Literal O_JENA = model.createLiteral(JenaTestModel.O_VALUE);

    private static final Resource S1_JENA = model.createResource(JenaTestModel.S1_VALUE);
    private static final Literal O1_JENA = model.createLiteral(JenaTestModel.O1_VALUE);

    @BeforeAll
    static void setup() {
        model.add(S_JENA, P_JENA, O_JENA);
        model.add(S1_JENA, P_JENA, O1_JENA);
        jenaGraph = new JenaGraph(model);
    }

    @Test
    void testGetSubject() {
        assertAll("different versions of a subject",
            () -> {
                final Throwable exception = assertThrows(IllegalArgumentException.class,
                    () -> JenaGraph.getSubject(RDFNode.literal("subject"))
                );
                assertEquals("Subject cannot be an RDF literal", exception.getMessage());
            },
            () -> assertEquals(JenaTestModel.S_VALUE, JenaGraph.getSubject(JenaTestModel.S_RDFNode).toString()),
            () -> assertTrue(JenaGraph.getSubject(RDFNode.blankNode()).isBlank()),
            () -> assertTrue(JenaGraph.getSubject(RDFNode.blankNode("nodeID")).isBlank()),
            () -> assertEquals(
                "nodeID",
                JenaGraph.getSubject(RDFNode.blankNode("nodeID")).toString()
                ),
            () -> assertEquals(Node.ANY, JenaGraph.getSubject(null))
        );
    }

    @Test
    void testGetPredicate() {
        assertAll("different versions of a predicate",
            () -> {
                final Throwable exception = assertThrows(IllegalArgumentException.class,
                    () -> JenaGraph.getPredicate(RDFNode.literal("predicate"))
                );
                assertEquals("Predicate cannot be an RDF literal", exception.getMessage());
            },
            () -> assertEquals(JenaTestModel.P_VALUE, JenaGraph.getPredicate(JenaTestModel.P_RDFNode).toString()),
            () -> {
                final Throwable exception = assertThrows(IllegalArgumentException.class,
                    () -> JenaGraph.getPredicate(RDFNode.blankNode())
                );
                assertEquals("Predicate cannot be a blank node", exception.getMessage());
            },
            () -> assertEquals(Node.ANY, JenaGraph.getPredicate(null))
        );
    }

    @Test
    void testGetObject() {
        assertAll("different versions of an object",
            () -> assertEquals(
                            "http://example.test/object",
                            JenaGraph.getObject(RDFNode.namedNode(URI.create("http://example.test/object"))).toString()
                ),
            () -> assertEquals("object", JenaGraph.getObject(RDFNode.literal("object")).getLiteralLexicalForm()),
            () -> assertTrue(JenaGraph.getObject(RDFNode.literal(
                                        "object",
                                        URI.create("http://www.w3.org/2004/02/skos/core#Concept")))
                                        .isLiteral()
                            ),
            () -> assertEquals(
                    "http://www.w3.org/2004/02/skos/core#Concept",
                    JenaGraph.getObject(
                                RDFNode.literal(
                                    "object",
                                    URI.create("http://www.w3.org/2004/02/skos/core#Concept"))
                                )
                                .getLiteralDatatypeURI().toString()),
            () -> assertEquals(
                    "en",
                    JenaGraph.getObject(RDFNode.literal("object", "en")).getLiteralLanguage()
                ),
            () -> assertTrue(JenaGraph.getObject(RDFNode.blankNode()).isBlank()),
            () -> assertTrue(JenaGraph.getObject(RDFNode.blankNode("nodeID")).isBlank()),
            () -> assertEquals(
                "nodeID",
                JenaGraph.getObject(RDFNode.blankNode("nodeID")).toString()
                ),
            () -> assertEquals(Node.ANY, JenaGraph.getObject(null))
        );
    }

    @Test
    void testFullParamsStream() {
        assertTrue(jenaGraph.stream(JenaTestModel.S_RDFNode, JenaTestModel.P_RDFNode, JenaTestModel.O_RDFNode)
            .findFirst().isPresent()
        );
        assertEquals(
            1,
            jenaGraph.stream(JenaTestModel.S_RDFNode, JenaTestModel.P_RDFNode, JenaTestModel.O_RDFNode).count()
        );
        assertEquals(
            JenaTestModel.P_RDFNode.getURI(),
            jenaGraph.stream(JenaTestModel.S_RDFNode, JenaTestModel.P_RDFNode, JenaTestModel.O_RDFNode)
                .findFirst().get().getPredicate().getURI()
        );
    }

    @Test
    void testNullInStream() {
        assertEquals(2, jenaGraph.stream(null, JenaTestModel.P_RDFNode, null).count());
        assertEquals(2, jenaGraph.stream(null, null, null).count());
        assertTrue(jenaGraph.stream(null, JenaTestModel.P_RDFNode, null)
                            .map(r -> r.getSubject().getURI().toString())
                            .collect(Collectors.toList())
                            .contains(JenaTestModel.S_VALUE)
        );
        assertTrue(jenaGraph.stream(null, JenaTestModel.P_RDFNode, null)
                            .map(r -> r.getSubject().getURI().toString())
                            .collect(Collectors.toList())
                            .contains(JenaTestModel.S1_VALUE)
        );
        assertEquals(
            JenaTestModel.P_RDFNode.getURI(),
            jenaGraph.stream(JenaTestModel.S_RDFNode, JenaTestModel.P_RDFNode, JenaTestModel.O_RDFNode)
                .findFirst().get().getPredicate().getURI()
        );
        assertEquals(
            JenaTestModel.S_RDFNode.getURI(),
            jenaGraph.stream(JenaTestModel.S_RDFNode, JenaTestModel.P_RDFNode, JenaTestModel.O_RDFNode)
                .findFirst().get().getSubject().getURI()
        );
        assertEquals(
            JenaTestModel.O_RDFNode.getLiteral(),
            jenaGraph.stream(JenaTestModel.S_RDFNode, JenaTestModel.P_RDFNode, JenaTestModel.O_RDFNode)
                .findFirst().get().getObject().getLiteral()
        );
    }

    @Test
    void testInvalidSubjectStream() {
        final var invalidSubject = RDFNode.literal("subject");

        assertAll("invalid subject in stream call",
            () -> {
                final Throwable exception = assertThrows(IllegalArgumentException.class,
                    () -> jenaGraph.stream(invalidSubject, JenaTestModel.P_RDFNode, JenaTestModel.O_RDFNode)
                );
                assertEquals("Subject cannot be an RDF literal", exception.getMessage());
            }
        );
    }

    @Test
    void testInvalidPredicateStream() {
        final var invalidPredicate = RDFNode.literal("predicate");

        assertAll("invalid predicate in stream call",
            () -> {
                final Throwable exception = assertThrows(IllegalArgumentException.class,
                    () -> jenaGraph.stream(JenaTestModel.S_RDFNode, invalidPredicate, JenaTestModel.O_RDFNode)
                );
                assertEquals("Predicate cannot be an RDF literal", exception.getMessage());
            }
        );
    }

    @Test
    void testNoParamsStream() {
        assertEquals(2, jenaGraph.stream().count());
        assertTrue(jenaGraph.stream()
                    .map(r -> r.getSubject().getURI().toString())
                    .collect(Collectors.toList())
                    .contains(JenaTestModel.S1_VALUE)
        );
    }

}
