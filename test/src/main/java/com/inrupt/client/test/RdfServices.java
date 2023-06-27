/*
 * Copyright Inrupt Inc.
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
package com.inrupt.client.test;

import static org.junit.jupiter.api.Assertions.*;

import com.inrupt.client.spi.RDFFactory;
import com.inrupt.client.spi.RdfService;
import com.inrupt.client.spi.ServiceProvider;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.rdf.api.BlankNode;
import org.apache.commons.rdf.api.Dataset;
import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Literal;
import org.apache.commons.rdf.api.Quad;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.api.RDFSyntax;
import org.apache.commons.rdf.api.Triple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;


/**
 * A {@code RdfService} class tester.
 */
public class RdfServices {

    private static final RdfService rdfService = ServiceProvider.getRdfService();
    private static final RDF rdf = RDFFactory.getInstance();

    @Test
    void parseToDatasetTurtle() throws Exception {
        try (final InputStream input = RdfServices.class
                .getResourceAsStream("/com/inrupt/client/test/rdf/profileExample.ttl");
                final Dataset dataset = rdfService.toDataset(RDFSyntax.TURTLE, input, null)) {
            try (final Stream<Quad> stream = dataset.stream().map(Quad.class::cast)) {
                assertTrue(stream.findFirst().isPresent());
            }

            try (final Stream<Quad> stream = dataset.stream().map(Quad.class::cast)) {
                assertTrue(stream.noneMatch(quad -> quad.getGraphName().isPresent()));
            }

            assertEquals(10, dataset.size());
        }
    }

    @Test
    void parseToDatasetTrig() throws Exception {
        try (final InputStream input = RdfServices.class
                .getResourceAsStream("/com/inrupt/client/test/rdf/oneTriple.trig");
                final Dataset dataset = rdfService.toDataset(RDFSyntax.TRIG, input, null)) {
            try (final Stream<Quad> stream = dataset.stream().map(Quad.class::cast)) {
                assertTrue(stream.anyMatch(quad -> quad.getGraphName().isPresent()));
            }

            try (final Stream<Quad> stream = dataset.stream().map(Quad.class::cast)) {
                assertTrue(stream.map(Quad::getGraphName)
                        .filter(Optional::isPresent).map(Optional::get)
                        .filter(IRI.class::isInstance).map(IRI.class::cast).map(IRI::getIRIString)
                        .anyMatch(RdfTestModel.G_VALUE::equals));
            }

            assertEquals(1, dataset.size());
        }
    }

    @Test
    void parseToDataserRelativeURIs() throws Exception {
        try (final InputStream input = RdfServices.class
                .getResourceAsStream("/com/inrupt/client/test/rdf/relativeURIs.ttl");
                final Dataset dataset = rdfService.toDataset(RDFSyntax.TURTLE, input, RdfTestModel.TEST_NAMESPACE)) {
            assertEquals(2, dataset.size());

            try (final Stream<Quad> stream = dataset.stream().map(Quad.class::cast)) {
                assertTrue(stream.map(Quad::getSubject)
                        .filter(IRI.class::isInstance).map(IRI.class::cast).map(IRI::getIRIString)
                        .anyMatch(iri -> iri.contains(RdfTestModel.TEST_NAMESPACE)));
            }
        }
    }

    @Test
    void parseToDatasetException() throws IOException {
        try (final InputStream input = RdfServices.class
                .getResourceAsStream("/com/inrupt/client/test/rdf/oneTriple.trig")) {
            assertThrows(IOException.class, () -> rdfService.toDataset(RDFSyntax.TURTLE, input, null));
        }
    }

    @Test
    void parseToGraph() throws Exception {
        try (final InputStream input = RdfServices.class
                .getResourceAsStream("/com/inrupt/client/test/rdf/profileExample.ttl");
                final Graph graph = rdfService.toGraph(RDFSyntax.TURTLE, input, null)) {
            assertEquals(10, graph.size());
        }
    }

    @Test
    void parseToGraphRelativeURIs() throws Exception {
        try (final InputStream input = RdfServices.class
                .getResourceAsStream("/com/inrupt/client/test/rdf/relativeURIs.ttl");
                final Graph graph = rdfService.toGraph(RDFSyntax.TURTLE, input, RdfTestModel.TEST_NAMESPACE)) {
            assertEquals(2, graph.size());
            try (final Stream<Triple> stream = graph.stream().map(Triple.class::cast)) {
                assertTrue(stream.map(Triple::getSubject)
                        .filter(IRI.class::isInstance).map(IRI.class::cast).map(IRI::getIRIString)
                        .anyMatch(iri -> iri.contains(RdfTestModel.TEST_NAMESPACE)));
            }
        }
    }

    @Test
    void parseToGraphException() throws IOException {
        try (final InputStream input = RdfServices.class
                .getResourceAsStream("/com/inrupt/client/test/rdf/invalid.ttl")) {
            assertThrows(IOException.class, () -> rdfService.toGraph(RDFSyntax.TURTLE, input, null));
        }
    }

    @Test
    void createGraph() throws Exception {
        final IRI subject1 = rdf.createIRI("https://resource.test/1");
        final IRI subject2 = rdf.createIRI("https://resource.test/2");
        final IRI predicate = rdf.createIRI("http://purl.org/dc/terms/subject");
        final Literal object1 = rdf.createLiteral("Topic 1");
        final Literal object2 = rdf.createLiteral("Topic 2");

        try (final Graph graph = rdf.createGraph()) {
            assertEquals(0, graph.size());

            graph.add(rdf.createTriple(subject1, predicate, object1));
            graph.add(rdf.createTriple(subject1, predicate, object2));
            graph.add(rdf.createTriple(subject2, predicate, object2));
            assertEquals(3, graph.size());

            graph.remove(subject1, subject2, subject1);
            assertEquals(3, graph.size());

            graph.remove(rdf.createTriple(subject2, predicate, object2));
            assertEquals(2, graph.size());

            graph.remove(subject1, null, null);
            assertEquals(0, graph.size());
        }
    }

    @Test
    void createDataset() throws Exception {
        final IRI graphName = rdf.createIRI("https://graph.test/");
        final IRI subject1 = rdf.createIRI("https://resource.test/1");
        final IRI subject2 = rdf.createIRI("https://resource.test/2");
        final IRI predicate = rdf.createIRI("http://purl.org/dc/terms/subject");
        final Literal object1 = rdf.createLiteral("Topic 1");
        final Literal object2 = rdf.createLiteral("Topic 2");

        try (final Dataset dataset = rdf.createDataset()) {
            assertEquals(0, dataset.size());

            dataset.add(rdf.createQuad(graphName, subject1, predicate, object1));
            dataset.add(rdf.createQuad(null, subject1, predicate, object1));
            dataset.add(rdf.createQuad(null, subject1, predicate, object2));
            dataset.add(rdf.createQuad(null, subject2, predicate, object2));
            assertEquals(4, dataset.size());

            dataset.remove(Optional.of(graphName), subject1, subject2, subject1);
            assertEquals(4, dataset.size());

            dataset.remove(rdf.createQuad(null, subject2, predicate, object2));
            assertEquals(3, dataset.size());

            dataset.remove(Optional.empty(), subject1, null, null);
            assertEquals(1, dataset.size());

            dataset.remove(Optional.of(graphName), subject1, null, null);
            assertEquals(0, dataset.size());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"TriG", "N-Quads"})
    void testRoundTripDataset(final String name) throws Exception {
        final Optional<RDFSyntax> syntax = RDFSyntax.byName(name);
        assertTrue(syntax.isPresent());

        final BlankNode bnode = rdf.createBlankNode("testing");
        final IRI g1 = rdf.createIRI("https://graph.example/1");
        final IRI s1 = rdf.createIRI("https://storage.example/1");
        final BlankNode s2 = bnode;
        final IRI s3 = rdf.createIRI("https://storage.example/3");
        final IRI p1 = rdf.createIRI("http://purl.org/dc/terms/title");
        final IRI p2 = rdf.createIRI("http://purl.org/dc/terms/subject");
        final IRI p3 = rdf.createIRI("http://purl.org/dc/terms/relation");
        final Literal o1 = rdf.createLiteral("My title", "en");
        final IRI o2 = rdf.createIRI("https://vocab.example/Sub2");
        final BlankNode o3 = bnode;

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (final Dataset dataset = rdf.createDataset()) {
            dataset.add(rdf.createQuad(g1, s1, p1, o1));
            dataset.add(rdf.createQuad(null, s2, p2, o2));
            dataset.add(rdf.createQuad(null, s3, p3, o3));

            assertTrue(dataset.contains(Optional.of(g1), s1, p1, o1));
            assertTrue(dataset.contains(Optional.empty(), s2, p2, o2));
            assertTrue(dataset.contains(Optional.empty(), s3, p3, o3));
            assertEquals(3, dataset.size());
            try (final Stream<Quad> stream = dataset.stream().map(Quad.class::cast)) {
                assertEquals(2, stream.filter(quad -> !quad.getGraphName().isPresent()).count());
            }
            rdfService.fromDataset(dataset, syntax.get(), out);
        }

        try (final Dataset dataset2 = rdfService.toDataset(syntax.get(),
                    new ByteArrayInputStream(out.toByteArray()), null)) {
            assertTrue(dataset2.contains(Optional.of(g1), s1, p1, o1));
            assertTrue(dataset2.contains(Optional.empty(), null, p2, o2));
            assertTrue(dataset2.contains(Optional.empty(), s3, p3, null));
            assertEquals(3, dataset2.size());
            try (final Stream<Quad> stream = dataset2.stream().map(Quad.class::cast)) {
                assertEquals(2, stream.filter(quad -> !quad.getGraphName().isPresent()).count());
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"Turtle", "TriG", "N-Triples", "N-Quads"})
    void testRoundTripGraph(final String name) throws Exception {
        final Optional<RDFSyntax> syntax = RDFSyntax.byName(name);
        assertTrue(syntax.isPresent());

        final BlankNode bnode = rdf.createBlankNode();
        final IRI s1 = rdf.createIRI("https://storage.example/1");
        final BlankNode s2 = bnode;
        final IRI s3 = rdf.createIRI("https://storage.example/3");
        final IRI p1 = rdf.createIRI("http://purl.org/dc/terms/title");
        final IRI p2 = rdf.createIRI("http://purl.org/dc/terms/subject");
        final IRI p3 = rdf.createIRI("http://purl.org/dc/terms/relation");
        final Literal o1 = rdf.createLiteral("My title", "en");
        final IRI o2 = rdf.createIRI("https://vocab.example/Sub2");
        final BlankNode o3 = bnode;

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (final Graph graph = rdf.createGraph()) {
            graph.add(rdf.createTriple(s1, p1, o1));
            graph.add(rdf.createTriple(s2, p2, o2));
            graph.add(rdf.createTriple(s3, p3, o3));

            assertTrue(graph.contains(s1, p1, o1));
            assertTrue(graph.contains(s2, p2, o2));
            assertTrue(graph.contains(s3, p3, o3));
            assertEquals(3, graph.size());

            rdfService.fromGraph(graph, syntax.get(), out);
        }

        try (final Graph graph2 = rdfService.toGraph(syntax.get(),
                    new ByteArrayInputStream(out.toByteArray()), null)) {
            assertTrue(graph2.contains(s1, p1, o1));
            assertTrue(graph2.contains(null, p2, o2));
            assertTrue(graph2.contains(s3, p3, null));
            assertEquals(3, graph2.size());
        }
    }
}
