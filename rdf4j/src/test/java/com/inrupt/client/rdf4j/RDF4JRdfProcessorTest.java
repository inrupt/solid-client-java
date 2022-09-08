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

import com.inrupt.client.rdf.Syntax;
import com.inrupt.client.spi.RdfProcessor;
import com.inrupt.client.spi.ServiceProvider;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class RDF4JRdfProcessorTest {

    private final RdfProcessor processor = ServiceProvider.getRdfProcessor();
    private static RDF4JDataset rdf4jDataset;
    private static RDF4JGraph rdf4jGraph;

    @BeforeAll
    static void setup() {
        // create a RDF4JDataset
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
        final var repository = new SailRepository(new MemoryStore());
        try (final var conn = repository.getConnection()) {
            conn.add(st);
            conn.add(st1);
        }
        rdf4jDataset = new RDF4JDataset(repository);

        // create a RDF4JGraph
        final var builder = new ModelBuilder();
        builder.namedGraph(RDF4JTestModel.G_RDF4J)
                .subject(RDF4JTestModel.S_VALUE)
                    .add(RDF4JTestModel.P_VALUE, RDF4JTestModel.O_VALUE);
        builder.defaultGraph().subject(RDF4JTestModel.S1_VALUE).add(RDF4JTestModel.P_VALUE, RDF4JTestModel.O1_VALUE);
        final var m = builder.build();
        rdf4jGraph = new RDF4JGraph(m);
    }

    @Test
    void checkInstance() {
        assertTrue(processor instanceof RDF4JRdfProcessor);
    }

    @Test
    void parseToDatasetTurtle() throws IOException {
        try (final var input = RDF4JRdfProcessorTest.class.getResourceAsStream("/profileExample.ttl")) {
            final var dataset = processor.toDataset(Syntax.TURTLE, input);
            assertFalse(dataset.stream().findFirst().get().getGraphName().isPresent());
            assertEquals(10, dataset.stream().count());
        }
    }

    @Test
    void parseToDatasetTrig() throws IOException {
        try (final var input = RDF4JRdfProcessorTest.class.getResourceAsStream("/oneTriple.trig")) {
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
    void parsetoDatasetException() throws IOException {
        try (final var input = RDF4JRdfProcessorTest.class.getResourceAsStream("/oneTriple.trig")) {
            assertThrows(IOException.class, () -> processor.toDataset(Syntax.TURTLE, input));
        }
    }

    @Test
    void parseToGraph() throws IOException {
        try (final var input = RDF4JRdfProcessorTest.class.getResourceAsStream("/profileExample.ttl")) {
            final var graph = processor.toGraph(Syntax.TURTLE, input);
            assertEquals(10, graph.stream().count());
        }
    }

    @Test
    void parseToGraphException() throws IOException {
        try (final var input = RDF4JRdfProcessorTest.class.getResourceAsStream("/invalid.ttl")) {
            assertThrows(IOException.class, () -> processor.toGraph(Syntax.TURTLE, input));
        }
    }

    @Test
    void serializeFromDatasetTRIGRoundtrip() throws IOException {
        try (final var output = new ByteArrayOutputStream()) {
            processor.fromDataset(rdf4jDataset, Syntax.TRIG, output);
            final var input = new ByteArrayInputStream(output.toByteArray());
            final var roundtrip = processor.toDataset(Syntax.TRIG, input);
            assertEquals(2, roundtrip.stream().count());
            assertEquals(rdf4jDataset.stream().count(), roundtrip.stream().count());
            final var st = rdf4jDataset.stream(
                            Optional.of(RDF4JTestModel.G_RDFNode),
                            null,
                            null,
                            null
                            ).findFirst().get().getSubject().getURI().toString();
            final var st1 = roundtrip.stream(
                            Optional.of(RDF4JTestModel.G_RDFNode),
                            RDF4JTestModel.S_RDFNode,
                            RDF4JTestModel.P_RDFNode,
                            RDF4JTestModel.O_RDFNode
                            ).findFirst().get().getSubject().getURI().toString();
            assertEquals(st, st1);
        }
    }

    @Test
    void serializeFromDatasetTURTLERoundtrip() throws IOException {
        try (final var output = new ByteArrayOutputStream()) {
            processor.fromDataset(rdf4jDataset, Syntax.TURTLE, output);
            final var input = new ByteArrayInputStream(output.toByteArray());
            final var roundtrip = processor.toDataset(Syntax.TURTLE, input);

            assertEquals(2, roundtrip.stream().count());
            assertEquals(rdf4jDataset.stream().count(), roundtrip.stream().count());
            final var st = rdf4jDataset.stream(
                            Optional.of(RDF4JTestModel.G_RDFNode),
                            null,
                            null,
                            null
                            ).findFirst().get().getSubject().getURI().toString();
            final var st1 = roundtrip.stream(
                            null,
                            RDF4JTestModel.S_RDFNode,
                            RDF4JTestModel.P_RDFNode,
                            RDF4JTestModel.O_RDFNode
                            ).findFirst().get().getSubject().getURI().toString();
            assertEquals(st, st1);
        }
    }

    @Test
    void serializeFromGraphRoundtrip() throws IOException {
        try (final var output = new ByteArrayOutputStream()) {
            processor.fromGraph(rdf4jGraph, Syntax.TURTLE, output);
            final var input = new ByteArrayInputStream(output.toByteArray());
            final var roundtrip = processor.toGraph(Syntax.TURTLE, input);
            assertEquals(2, roundtrip.stream().count());
            assertEquals(rdf4jGraph.stream().count(), roundtrip.stream().count());
            final var st = rdf4jGraph.stream(
                            RDF4JTestModel.S_RDFNode,
                            null,
                            null
                            ).findFirst().get().getSubject().getURI().toString();
            final var st1 = roundtrip.stream(
                            RDF4JTestModel.S_RDFNode,
                            RDF4JTestModel.P_RDFNode,
                            RDF4JTestModel.O_RDFNode
                            ).findFirst().get().getSubject().getURI().toString();
            assertEquals(st, st1);
        }
    }

    @Test
    void serializeFromDatasetException() throws IOException {
        final var tmp = Files.createTempFile(null, null).toFile();
        try (final var output = new FileOutputStream(tmp)) {
            output.close();
            assertThrows(IOException.class, () -> processor.fromDataset(rdf4jDataset, Syntax.TRIG, output));
        }
    }

    @Test
    void serializeFromGraphException() throws IOException {
        final var tmp = Files.createTempFile(null, null).toFile();
        try (final var output = new FileOutputStream(tmp)) {
            output.close();
            assertThrows(IOException.class, () -> processor.fromGraph(rdf4jGraph, Syntax.TURTLE, output));
        }
    }

}
