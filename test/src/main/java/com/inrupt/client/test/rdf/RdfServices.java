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
package com.inrupt.client.test.rdf;

import static org.junit.jupiter.api.Assertions.*;

import com.inrupt.client.Dataset;
import com.inrupt.client.Graph;
import com.inrupt.client.Syntax;
import com.inrupt.client.spi.RdfService;
import com.inrupt.client.spi.ServiceProvider;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Optional;

import org.junit.jupiter.api.Test;

public class RdfServices {

    private static Dataset rdf4jDataset;
    private static Graph rdf4jGraph;
    private static final RdfService rdfService = ServiceProvider.getRdfService();

    // @BeforeAll
    // static void setup() {
        // create a RDF4JDataset
    /*     final Statement st = RdfTestModel.VF.createStatement(
            RdfTestModel.S_RDF4J,
            RdfTestModel.P_RDF4J,
            RdfTestModel.O_RDF4J,
            RdfTestModel.G_RDF4J
        );
        final Statement st1 = RdfTestModel.VF.createStatement(
            RdfTestModel.S1_RDF4J,
            RdfTestModel.P1_RDF4J,
            RdfTestModel.O1_RDF4J
        );
        final Repository repository = new SailRepository(new MemoryStore());
        try (final RepositoryConnection conn = repository.getConnection()) {
            conn.add(st);
            conn.add(st1);
        }
        //rdf4jDataset = new Dataset(repository);

        // create a RDF4JGraph
        final ModelBuilder builder = new ModelBuilder();
        builder.namedGraph(RdfTestModel.G_RDF4J)
                .subject(RdfTestModel.S_VALUE)
                    .add(RdfTestModel.P_VALUE, RdfTestModel.O_VALUE);
        builder.defaultGraph().subject(RdfTestModel.S1_VALUE).add(RdfTestModel.P_VALUE, RdfTestModel.O1_VALUE);
        final Model m = builder.build();
        //rdf4jGraph = new RDF4JGraph(m); */
    //}

    @Test
    void parseToDatasetTurtle() throws IOException {
        try (final InputStream input = RdfServices.class.getResourceAsStream("/profileExample.ttl")) {
            final Dataset dataset = rdfService.toDataset(Syntax.TURTLE, input);
            assertFalse(dataset.stream().findFirst().get().getGraphName().isPresent());
            assertEquals(10, dataset.stream().count());
        }
    }

    @Test
    void parseToDatasetTrig() throws IOException {
        try (final InputStream input = RdfServices.class.getResourceAsStream("/oneTriple.trig")) {
            final Dataset dataset = rdfService.toDataset(Syntax.TRIG, input);
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
        try (final InputStream input = RdfServices.class.getResourceAsStream("/relativeURIs.ttl")) {
            final Dataset dataset = rdfService.toDataset(Syntax.TURTLE, input, "http://example.test/");
            assertEquals(2, dataset.stream().count());
            assertTrue(dataset.stream().findFirst().get().getSubject().getURI().toString()
                .contains("http://example.test/")
            );
        }
    }

    @Test
    void parseToDatasetRelativeURIsButNoBaseURI() throws IOException {
        try (final InputStream input = RdfServices.class.getResourceAsStream("/relativeURIs.ttl")) {
            assertThrows(IOException.class, () -> rdfService.toDataset(Syntax.TURTLE, input));
        }
    }

    @Test
    void parseToDatasetException() throws IOException {
        try (final InputStream input = RdfServices.class.getResourceAsStream("/oneTriple.trig")) {
            assertThrows(IOException.class, () -> rdfService.toDataset(Syntax.TURTLE, input));
        }
    }

    @Test
    void parseToGraph() throws IOException {
        try (final InputStream input = RdfServices.class.getResourceAsStream("/profileExample.ttl")) {
            final Graph graph = rdfService.toGraph(Syntax.TURTLE, input);
            assertEquals(10, graph.stream().count());
        }
    }

    @Test
    void parseToGraphRelativeURIs() throws IOException {
        try (final InputStream input = RdfServices.class.getResourceAsStream("/relativeURIs.ttl")) {
            final Graph graph = rdfService.toGraph(Syntax.TURTLE, input, "http://example.test/");
            assertEquals(2, graph.stream().count());
            assertTrue(graph.stream().findFirst().get().getSubject().getURI().toString()
                .contains("http://example.test/")
            );
        }
    }

    @Test
    void parseToGraphRelativeURIsButNoBaseURI() throws IOException {
        try (final InputStream input = RdfServices.class.getResourceAsStream("/relativeURIs.ttl")) {
            assertThrows(IOException.class, () -> rdfService.toGraph(Syntax.TURTLE, input));
        }
    }

    @Test
    void parseToGraphException() throws IOException {
        try (final InputStream input = RdfServices.class.getResourceAsStream("/invalid.ttl")) {
            assertThrows(IOException.class, () -> rdfService.toGraph(Syntax.TURTLE, input));
        }
    }

    @Test
    void serializeFromDatasetTRIGRoundtrip() throws IOException {
        try (final ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            rdfService.fromDataset(rdf4jDataset, Syntax.TRIG, output);
            final InputStream input = new ByteArrayInputStream(output.toByteArray());
            final Dataset roundtrip = rdfService.toDataset(Syntax.TRIG, input);
            assertEquals(2, roundtrip.stream().count());
            assertEquals(rdf4jDataset.stream().count(), roundtrip.stream().count());
            final String st = rdf4jDataset.stream(
                            Optional.of(RdfTestModel.G_RDFNode),
                            null,
                            null,
                            null
                            ).findFirst().get().getSubject().getURI().toString();
            final String st1 = roundtrip.stream(
                            Optional.of(RdfTestModel.G_RDFNode),
                            RdfTestModel.S_RDFNode,
                            RdfTestModel.P_RDFNode,
                            RdfTestModel.O_RDFNode
                            ).findFirst().get().getSubject().getURI().toString();
            assertEquals(st, st1);
        }
    }

    @Test
    void serializeFromDatasetTURTLERoundtrip() throws IOException {
        try (final ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            rdfService.fromDataset(rdf4jDataset, Syntax.TURTLE, output);
            final InputStream input = new ByteArrayInputStream(output.toByteArray());
            final Dataset roundtrip = rdfService.toDataset(Syntax.TURTLE, input);

            assertEquals(2, roundtrip.stream().count());
            assertEquals(rdf4jDataset.stream().count(), roundtrip.stream().count());
            final String st = rdf4jDataset.stream(
                            Optional.of(RdfTestModel.G_RDFNode),
                            null,
                            null,
                            null
                            ).findFirst().get().getSubject().getURI().toString();
            final String st1 = roundtrip.stream(
                            null,
                            RdfTestModel.S_RDFNode,
                            RdfTestModel.P_RDFNode,
                            RdfTestModel.O_RDFNode
                            ).findFirst().get().getSubject().getURI().toString();
            assertEquals(st, st1);
        }
    }

    @Test
    void serializeFromGraphRoundtrip() throws IOException {
        try (final ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            rdfService.fromGraph(rdf4jGraph, Syntax.TURTLE, output);
            final InputStream input = new ByteArrayInputStream(output.toByteArray());
            final Graph roundtrip = rdfService.toGraph(Syntax.TURTLE, input);
            assertEquals(2, roundtrip.stream().count());
            assertEquals(rdf4jGraph.stream().count(), roundtrip.stream().count());
            final String st = rdf4jGraph.stream(
                            RdfTestModel.S_RDFNode,
                            null,
                            null
                            ).findFirst().get().getSubject().getURI().toString();
            final String st1 = roundtrip.stream(
                            RdfTestModel.S_RDFNode,
                            RdfTestModel.P_RDFNode,
                            RdfTestModel.O_RDFNode
                            ).findFirst().get().getSubject().getURI().toString();
            assertEquals(st, st1);
        }
    }

    @Test
    void serializeFromDatasetException() throws IOException {
        final File tmp = Files.createTempFile(null, null).toFile();
        try (final OutputStream output = new FileOutputStream(tmp)) {
            output.close();
            assertThrows(IOException.class, () -> rdfService.fromDataset(rdf4jDataset, Syntax.TRIG, output));
        }
    }

    @Test
    void serializeFromGraphException() throws IOException {
        final File tmp = Files.createTempFile(null, null).toFile();
        try (final OutputStream output = new FileOutputStream(tmp)) {
            output.close();
            assertThrows(IOException.class,
                    () -> rdfService.fromGraph(rdf4jGraph, Syntax.TURTLE, output));
        }
    }
}