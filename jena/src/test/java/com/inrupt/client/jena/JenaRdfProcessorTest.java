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

import com.inrupt.client.api.Syntax;
import com.inrupt.client.spi.RdfProcessor;
import com.inrupt.client.spi.ServiceProvider;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

import org.apache.jena.atlas.RuntimeIOException;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class JenaRdfProcessorTest {

    private final RdfProcessor processor = ServiceProvider.getRdfProcessor();
    private static JenaDataset jenaDataset;
    private static JenaGraph jenaGraph;

    private static final Model model = ModelFactory.createDefaultModel();

    private static final Resource S_JENA = model.createResource(JenaTestModel.S_VALUE);
    private static final Literal O_JENA = model.createLiteral(JenaTestModel.O_VALUE);
    private static final Property P_JENA = model.createProperty(JenaTestModel.P_VALUE);

    private static final Resource S1_JENA = model.createResource(JenaTestModel.S1_VALUE);
    private static final Literal O1_JENA = model.createLiteral(JenaTestModel.O1_VALUE);

    @BeforeAll
    static void setup() {
        // create a JenaDataset
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

        // create a JenaGraph
        model.add(S_JENA, P_JENA, O_JENA);
        model.add(S1_JENA, P_JENA, O1_JENA);
        jenaGraph = new JenaGraph(model);
    }

    @Test
    void checkInstance() {
        assertTrue(processor instanceof JenaRdfProcessor);
    }

    @Test
    void parseToDatasetTurtle() throws IOException {
        try (final var input = JenaRdfProcessorTest.class.getResourceAsStream("/profileExample.ttl")) {
            final var dataset = processor.toDataset(Syntax.TURTLE, input);
            assertFalse(dataset.stream().findFirst().get().getGraphName().isPresent());
            assertEquals(10, dataset.stream().count());
        }
    }

    @Test
    void parseToDatasetTrig() throws IOException {
        try (final var input = JenaRdfProcessorTest.class.getResourceAsStream("/oneTriple.trig")) {
            final var dataset = processor.toDataset(Syntax.TRIG, input);
            assertTrue(dataset.stream().findFirst().get().getGraphName().isPresent());
            assertEquals(
                "http://example.test/graph",
                dataset.stream().findFirst().get().getGraphName().get().getURI().toString()
            );
            assertEquals(1, dataset.stream().count());
        }
    }

    @Test
    void parseToDataserRelativeURIs() throws IOException {
        try (final var input = JenaRdfProcessorTest.class.getResourceAsStream("/relativeURIs.ttl")) {
            final var dataset = processor.toDataset(Syntax.TURTLE, input, "http://example.test/");
            assertEquals(2, dataset.stream().count());
            assertTrue(dataset.stream().findFirst().get().getSubject().getURI().toString()
                .contains("http://example.test/")
            );
        }
    }

    @Test
    void parseToDatasetRelativeURIsButNoBaseURI() throws IOException {
        try (final var input = JenaRdfProcessorTest.class.getResourceAsStream("/relativeURIs.ttl")) {
            final var dataset = processor.toDataset(Syntax.TURTLE, input);
            assertEquals(2, dataset.stream().count());
            assertTrue(dataset.stream().findFirst().get().getSubject().getURI().toString()
                .contains("file://") //treats relative URIs like local files
            );
        }
    }

    @Test
    void parsetoDatasetException() throws IOException {
        try (final var input = JenaRdfProcessorTest.class.getResourceAsStream("/oneTriple.trig")) {
            assertThrows(IOException.class, () -> processor.toDataset(Syntax.TURTLE, input));
        }
    }

    @Test
    void parseToGraph() throws IOException {
        try (final var input = JenaRdfProcessorTest.class.getResourceAsStream("/profileExample.ttl")) {
            final var graph = processor.toGraph(Syntax.TURTLE, input);
            assertEquals(10, graph.stream().count());
        }
    }

    @Test
    void parseToGraphRelativeURIs() throws IOException {
        try (final var input = JenaRdfProcessorTest.class.getResourceAsStream("/relativeURIs.ttl")) {
            final var graph = processor.toGraph(Syntax.TURTLE, input, "http://example.test/");
            assertEquals(2, graph.stream().count());
            assertTrue(graph.stream().findFirst().get().getSubject().getURI().toString()
                .contains("http://example.test/")
            );
        }
    }

    @Test
    void parseToGraphException() throws IOException {
        try (final var input = JenaRdfProcessorTest.class.getResourceAsStream("/invalid.ttl")) {
            assertThrows(IOException.class, () -> processor.toGraph(Syntax.TURTLE, input));
        }
    }

    @Test
    void serializeFromDatasetTRIGRoundtrip() throws IOException {
        try (final var output = new ByteArrayOutputStream()) {
            processor.fromDataset(jenaDataset, Syntax.TRIG, output);
            final var input = new ByteArrayInputStream(output.toByteArray());
            final var roundtrip = processor.toDataset(Syntax.TRIG, input);
            assertEquals(2, roundtrip.stream().count());
            assertEquals(jenaDataset.stream().count(), roundtrip.stream().count());
            final var st = jenaDataset.stream(
                            Optional.of(JenaTestModel.G_RDFNode),
                            null,
                            null,
                            null
                            ).findFirst().get().getSubject().getURI().toString();
            final var st1 = roundtrip.stream(
                            Optional.of(JenaTestModel.G_RDFNode),
                            JenaTestModel.S_RDFNode,
                            JenaTestModel.P_RDFNode,
                            JenaTestModel.O_RDFNode
                            ).findFirst().get().getSubject().getURI().toString();
            assertEquals(st, st1);
        }
    }

    @Test
    void serializeFromDatasetTURTLERoundtrip() throws IOException {
        try (final var output = new ByteArrayOutputStream()) {
            processor.fromDataset(jenaDataset, Syntax.TURTLE, output);
            final var input = new ByteArrayInputStream(output.toByteArray());
            final var roundtrip = processor.toDataset(Syntax.TURTLE, input);

            assertEquals(2, roundtrip.stream().count());
            assertEquals(jenaDataset.stream().count(), roundtrip.stream().count());
            final var st = jenaDataset.stream(
                            Optional.of(JenaTestModel.G_RDFNode),
                            null,
                            null,
                            null
                            ).findFirst().get().getSubject().getURI().toString();
            final var st1 = roundtrip.stream(
                            null,
                            JenaTestModel.S_RDFNode,
                            JenaTestModel.P_RDFNode,
                            JenaTestModel.O_RDFNode
                            ).findFirst().get().getSubject().getURI().toString();
            assertEquals(st, st1);
        }
    }

    @Test
    void serializeFromGraphRoundtrip() throws IOException {
        try (final var output = new ByteArrayOutputStream()) {
            processor.fromGraph(jenaGraph, Syntax.TURTLE, output);
            final var input = new ByteArrayInputStream(output.toByteArray());
            final var roundtrip = processor.toGraph(Syntax.TURTLE, input);
            assertEquals(2, roundtrip.stream().count());
            assertEquals(jenaGraph.stream().count(), roundtrip.stream().count());
            final var st = jenaGraph.stream(
                            JenaTestModel.S_RDFNode,
                            null,
                            null
                            ).findFirst().get().getSubject().getURI().toString();
            final var st1 = roundtrip.stream(
                            JenaTestModel.S_RDFNode,
                            JenaTestModel.P_RDFNode,
                            JenaTestModel.O_RDFNode
                            ).findFirst().get().getSubject().getURI().toString();
            assertEquals(st, st1);
        }
    }

    @Test
    void serializeFromDatasetException() throws IOException {
        final var tmp = Files.createTempFile(null, null).toFile();
        try (final var output = new FileOutputStream(tmp)) {
            output.close();
            assertThrows(RuntimeIOException.class, () -> processor.fromDataset(jenaDataset, Syntax.TRIG, output));
        }
    }

    @Test
    void serializeFromGraphException() throws IOException {
        final var tmp = Files.createTempFile(null, null).toFile();
        try (final var output = new FileOutputStream(tmp)) {
            output.close();
            assertThrows(RuntimeIOException.class, () -> processor.fromGraph(jenaGraph, Syntax.TURTLE, output));
        }
    }

}
