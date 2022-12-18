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
package com.inrupt.client.test;

import static org.junit.jupiter.api.Assertions.*;

import com.inrupt.client.rdf.Dataset;
import com.inrupt.client.rdf.Graph;
import com.inrupt.client.rdf.Quad;
import com.inrupt.client.rdf.RDFFactory;
import com.inrupt.client.rdf.RDFNode;
import com.inrupt.client.rdf.Syntax;
import com.inrupt.client.rdf.Triple;
import com.inrupt.client.spi.RdfService;
import com.inrupt.client.spi.ServiceProvider;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

public class RdfServices {

    private static final RdfService rdfService = ServiceProvider.getRdfService();

    @Test
    void parseToDatasetTurtle() throws IOException {
        try (final InputStream input = RdfServices.class
                .getResourceAsStream("/com/inrupt/client/test/rdf/profileExample.ttl")) {
            final Dataset dataset = rdfService.toDataset(Syntax.TURTLE, input);
            assertTrue(dataset.stream().findFirst().isPresent());
            assertFalse(dataset.stream().findFirst().get().getGraphName().isPresent());
            assertEquals(10, dataset.stream().count());
        }
    }

    @Test
    void parseToDatasetTrig() throws IOException {
        try (final InputStream input = RdfServices.class
                .getResourceAsStream("/com/inrupt/client/test/rdf/oneTriple.trig")) {
            final Dataset dataset = rdfService.toDataset(Syntax.TRIG, input);
            assertTrue(dataset.stream().findFirst().get().getGraphName().isPresent());
            assertEquals(
                RdfTestModel.G_VALUE,
                dataset.stream().findFirst().get().getGraphName().get().getURI().toString()
            );
            assertEquals(1, dataset.stream().count());
        }
    }

    @Test
    void parseToDataserRelativeURIs() throws IOException {
        try (final InputStream input = RdfServices.class
                .getResourceAsStream("/com/inrupt/client/test/rdf/relativeURIs.ttl")) {
            final Dataset dataset = rdfService.toDataset(Syntax.TURTLE, input, RdfTestModel.TEST_NAMESPACE);
            assertEquals(2, dataset.stream().count());
            assertTrue(dataset.stream().findFirst().get().getSubject().getURI().toString()
                .contains(RdfTestModel.TEST_NAMESPACE)
            );
        }
    }

    @Test
    void parseToDatasetException() throws IOException {
        try (final InputStream input = RdfServices.class
                .getResourceAsStream("/com/inrupt/client/test/rdf/oneTriple.trig")) {
            assertThrows(IOException.class, () -> rdfService.toDataset(Syntax.TURTLE, input));
        }
    }

    @Test
    void parseToGraph() throws IOException {
        try (final InputStream input = RdfServices.class
                .getResourceAsStream("/com/inrupt/client/test/rdf/profileExample.ttl")) {
            final Graph graph = rdfService.toGraph(Syntax.TURTLE, input);
            assertEquals(10, graph.stream().count());
        }
    }

    @Test
    void parseToGraphRelativeURIs() throws IOException {
        try (final InputStream input = RdfServices.class
                .getResourceAsStream("/com/inrupt/client/test/rdf/relativeURIs.ttl")) {
            final Graph graph = rdfService.toGraph(Syntax.TURTLE, input, RdfTestModel.TEST_NAMESPACE);
            assertEquals(2, graph.stream().count());
            assertTrue(graph.stream().findFirst().get().getSubject().getURI().toString()
                .contains(RdfTestModel.TEST_NAMESPACE)
            );
        }
    }

    @Test
    void parseToGraphException() throws IOException {
        try (final InputStream input = RdfServices.class
                .getResourceAsStream("/com/inrupt/client/test/rdf/invalid.ttl")) {
            assertThrows(IOException.class, () -> rdfService.toGraph(Syntax.TURTLE, input));
        }
    }

    @Test
    void createGraph() {
        final RDFNode subject1 = RDFNode.namedNode(URI.create("https://resource.test/1"));
        final RDFNode subject2 = RDFNode.namedNode(URI.create("https://resource.test/2"));
        final RDFNode predicate = RDFNode.namedNode(URI.create("http://purl.org/dc/terms/subject"));
        final RDFNode object1 = RDFNode.literal("Topic 1");
        final RDFNode object2 = RDFNode.literal("Topic 2");

        final Graph graph = RDFFactory.createGraph();
        assertEquals(0, graph.stream().count());
        graph.add(RDFFactory.createTriple(subject1, predicate, object1));
        graph.add(RDFFactory.createTriple(subject1, predicate, object2));
        graph.add(RDFFactory.createTriple(subject2, predicate, object2));
        assertEquals(3, graph.stream().count());
        graph.remove(subject1, subject2, subject1);
        assertEquals(3, graph.stream().count());
        graph.remove(RDFFactory.createTriple(subject2, predicate, object2));
        assertEquals(2, graph.stream().count());
        graph.remove(subject1, null, null);
        assertEquals(0, graph.stream().count());
    }

    @Test
    void createDataset() {
        final RDFNode graphName = RDFNode.namedNode(URI.create("https://graph.test/"));
        final RDFNode subject1 = RDFNode.namedNode(URI.create("https://resource.test/1"));
        final RDFNode subject2 = RDFNode.namedNode(URI.create("https://resource.test/2"));
        final RDFNode predicate = RDFNode.namedNode(URI.create("http://purl.org/dc/terms/subject"));
        final RDFNode object1 = RDFNode.literal("Topic 1");
        final RDFNode object2 = RDFNode.literal("Topic 2");

        final Dataset dataset = RDFFactory.createDataset();
        assertEquals(0, dataset.stream().count());
        dataset.add(RDFFactory.createQuad(subject1, predicate, object1, graphName));
        dataset.add(RDFFactory.createQuad(subject1, predicate, object1));
        dataset.add(RDFFactory.createQuad(subject1, predicate, object2));
        dataset.add(RDFFactory.createQuad(subject2, predicate, object2));
        assertEquals(4, dataset.stream().count());
        dataset.remove(Optional.of(graphName), subject1, subject2, subject1);
        assertEquals(4, dataset.stream().count());
        dataset.remove(RDFFactory.createQuad(subject2, predicate, object2));
        assertEquals(3, dataset.stream().count());
        dataset.remove(Optional.empty(), subject1, null, null);
        assertEquals(1, dataset.stream().count());
        dataset.remove(Optional.of(graphName), subject1, null, null);
        assertEquals(0, dataset.stream().count());
    }

    @Test
    void testRoundTripDataset() throws IOException {
        final RDFNode g1 = RDFNode.namedNode(URI.create("https://graph.example/1"));
        final RDFNode s1 = RDFNode.namedNode(URI.create("https://storage.example/1"));
        final RDFNode s2 = RDFNode.blankNode("testing");
        final RDFNode p1 = RDFNode.namedNode(URI.create("http://purl.org/dc/terms/title"));
        final RDFNode p2 = RDFNode.namedNode(URI.create("http://purl.org/dc/terms/subject"));
        final RDFNode o1 = RDFNode.literal("My title", "en");
        final RDFNode o2 = RDFNode.namedNode(URI.create("https://vocab.example/Sub2"));
        final Dataset dataset = new SimpleDataset();
        dataset.add(RDFFactory.createQuad(s1, p1, o1, g1));
        dataset.add(RDFFactory.createQuad(s2, p2, o2));

        assertEquals(2, dataset.stream().count());
        assertEquals(1, dataset.stream(Optional.of(g1), s1, p1, o1).count());
        assertEquals(1, dataset.stream(Optional.empty(), s2, p2, o2).count());

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        rdfService.fromDataset(dataset, Syntax.NQUADS, out);

        final Dataset dataset2 = rdfService.toDataset(Syntax.NQUADS, new ByteArrayInputStream(out.toByteArray()));
        assertEquals(2, dataset2.stream().count());
        assertEquals(1, dataset2.stream(Optional.of(g1), s1, p1, o1).count());
        assertEquals(1, dataset2.stream(Optional.empty(), null, p2, o2).count());
    }

    @Test
    void testRoundTripGraph() throws IOException {
        final RDFNode s1 = RDFNode.namedNode(URI.create("https://storage.example/1"));
        final RDFNode s2 = RDFNode.blankNode();
        final RDFNode p1 = RDFNode.namedNode(URI.create("http://purl.org/dc/terms/title"));
        final RDFNode p2 = RDFNode.namedNode(URI.create("http://purl.org/dc/terms/subject"));
        final RDFNode o1 = RDFNode.literal("My title", "en");
        final RDFNode o2 = RDFNode.namedNode(URI.create("https://vocab.example/Sub2"));
        final Graph graph = new SimpleGraph();
        graph.add(RDFFactory.createTriple(s1, p1, o1));
        graph.add(RDFFactory.createTriple(s2, p2, o2));

        assertEquals(2, graph.stream().count());
        assertEquals(1, graph.stream(s1, p1, o1).count());
        assertEquals(1, graph.stream(s2, p2, o2).count());

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        rdfService.fromGraph(graph, Syntax.TURTLE, out);

        final Graph graph2 = rdfService.toGraph(Syntax.TURTLE, new ByteArrayInputStream(out.toByteArray()));
        assertEquals(2, graph2.stream().count());
        assertEquals(1, graph2.stream(s1, p1, o1).count());
        assertEquals(1, graph2.stream(null, p2, o2).count());
    }

    static class SimpleDataset implements Dataset {

        private final Set<Quad> quads = new HashSet<>();

        @Override
        public Stream<Quad> stream(final Optional<RDFNode> graph, final RDFNode subject, final RDFNode predicate,
                final RDFNode object) {
            return quads.stream().filter(quad -> matches(quad, graph, subject, predicate, object));
        }

        @Override
        public void add(final Quad quad) {
            quads.add(quad);
        }

        @Override
        public void remove(final Optional<RDFNode> graphName, final RDFNode subject,
                final RDFNode predicate, final RDFNode object) {
            final List<Quad> toDelete = stream(graphName, subject, predicate, object)
                .collect(Collectors.toList());
            quads.removeAll(toDelete);
        }

        static boolean matches(final Quad quad, final Optional<RDFNode> graphName, final RDFNode subject,
                final RDFNode predicate, final RDFNode object) {
            if (graphName != null && !graphName.equals(quad.getGraphName())) {
                return false;
            }
            if (subject != null && !subject.equals(quad.getSubject())) {
                return false;
            }
            if (predicate != null && !predicate.equals(quad.getPredicate())) {
                return false;
            }
            if (object != null && !object.equals(quad.getObject())) {
                return false;
            }
            return true;
        }
    }

    static class SimpleGraph implements Graph {

        private final Set<Triple> triples = new HashSet<>();

        @Override
        public Stream<Triple> stream(final RDFNode subject, final RDFNode predicate, final RDFNode object) {
            return triples.stream().filter(triple -> matches(triple, subject, predicate, object));
        }

        @Override
        public void add(final Triple triple) {
            triples.add(triple);
        }

        @Override
        public void remove(final RDFNode subject, final RDFNode predicate, final RDFNode object) {
            final List<Triple> toDelete = stream(subject, predicate, object).collect(Collectors.toList());
            triples.removeAll(toDelete);
        }

        static boolean matches(final Triple triple, final RDFNode subject, final RDFNode predicate,
                final RDFNode object) {
            if (subject != null && !subject.equals(triple.getSubject())) {
                return false;
            }
            if (predicate != null && !predicate.equals(triple.getPredicate())) {
                return false;
            }
            if (object != null && !object.equals(triple.getObject())) {
                return false;
            }
            return true;
        }
    }
}
