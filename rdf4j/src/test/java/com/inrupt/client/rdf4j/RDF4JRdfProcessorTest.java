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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class RDF4JRdfProcessorTest {

    private final RdfProcessor processor = ServiceProvider.getRdfProcessor();
    private static RDF4JDataset rdf4jDataset;
    private static RDF4JGraph rdf4jGraph;

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

        final var builder = new ModelBuilder();

        // add a new named graph to the model
        builder.namedGraph(TestModel.G_RDF4J)
                .subject(TestModel.S_VALUE)
                    .add(TestModel.P_VALUE, TestModel.O_VALUE);

        // add a triple to the default graph (which is null and NOT RDF4J.NIL)
        builder.defaultGraph().subject(TestModel.S1_VALUE).add(TestModel.P_VALUE, TestModel.O1_VALUE);

        final var m = builder.build();
        rdf4jGraph = new RDF4JGraph(m);
    }

    @Test
    void checkInstance() {
        assertTrue(processor instanceof RDF4JRdfProcessor);
    }

    @Test
    void parseToDataset() throws IOException {
        try (final var input = RDF4JRdfProcessorTest.class.getResourceAsStream("/profileExample.ttl")) {
            final var dataset = processor.toDataset(Syntax.TRIG, input);
            assertEquals(10, dataset.stream().count());
        }
    }

    @Test
    void parseToGraph() throws IOException {
        try (final var input = RDF4JRdfProcessorTest.class.getResourceAsStream("/profileExample.ttl")) {
            final var graph = processor.toGraph(Syntax.TURTLE, input);
            assertEquals(10, graph.stream().count());
        }
    }

    @Disabled("Fails on roundtrip having data")
    @Test
    void serializeFromDatasetRoundtrip() throws IOException {
        try (final var output = new ByteArrayOutputStream()) {
            processor.fromDataset(rdf4jDataset, Syntax.TURTLE, output);
            final var input = new ByteArrayInputStream(output.toByteArray());
            final var roundtrip = processor.toDataset(Syntax.TURTLE, input);
            assertEquals(2, rdf4jDataset.stream().count());
            assertEquals(rdf4jDataset.stream().count(), roundtrip.stream().count());
            assertEquals(rdf4jDataset.stream(
                            Optional.of(TestModel.G_RDFNode),
                            TestModel.S_RDFNode,
                            TestModel.P_RDFNode,
                            TestModel.O_RDFNode
                            ).findFirst().get().getSubject().toString(),
                        roundtrip.stream(
                            Optional.of(TestModel.G_RDFNode),
                            TestModel.S_RDFNode,
                            TestModel.P_RDFNode,
                            TestModel.O_RDFNode
                            ).findFirst().get().getSubject().toString()
            );
        }
    }

    @Test
    void serializeFromDataset() throws IOException {

        final var tmp = Files.createTempFile(null, null).toFile();
        try (final var output = new FileOutputStream(tmp)) {
            output.close();
            assertThrows(IOException.class, () -> processor.fromDataset(rdf4jDataset,Syntax.TURTLE, output));
        }
    }

    @Test
    void serializeFromGraph() throws IOException {
        final var tmp = Files.createTempFile(null, null).toFile();
        try (final var output = new FileOutputStream(tmp)) {
            output.close();
            assertThrows(IOException.class, () -> processor.fromGraph(rdf4jGraph, Syntax.TRIG, output));
        }
    }

}
