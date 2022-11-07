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

import com.inrupt.client.Dataset;
import com.inrupt.client.Graph;
import com.inrupt.client.Syntax;
import com.inrupt.client.spi.RdfService;
import com.inrupt.client.spi.ServiceProvider;
import com.inrupt.client.test.rdf.RdfServices;
import com.inrupt.client.test.rdf.RdfTestModel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Optional;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class RDF4JServiceTest extends RdfServices {

    private final RdfService service = ServiceProvider.getRdfService();
    private static RDF4JDataset rdf4jDataset;
    private static RDF4JGraph rdf4jGraph;

    public static final ValueFactory VF = SimpleValueFactory.getInstance();

    @BeforeAll
    static void setup() {
        // create a RDF4JDataset
        final Statement st = VF.createStatement(
            RDF4JTestModel.S_RDF4J,
            RDF4JTestModel.P_RDF4J,
            RDF4JTestModel.O_RDF4J,
            RDF4JTestModel.G_RDF4J
        );
        final Statement st1 = VF.createStatement(
            RDF4JTestModel.S1_RDF4J,
            RDF4JTestModel.P1_RDF4J,
            RDF4JTestModel.O1_RDF4J
        );
        final Repository repository = new SailRepository(new MemoryStore());
        try (final RepositoryConnection conn = repository.getConnection()) {
            conn.add(st);
            conn.add(st1);
        }
        rdf4jDataset = new RDF4JDataset(repository);

        // create a RDF4JGraph
        final ModelBuilder builder = new ModelBuilder();
        builder.namedGraph(RDF4JTestModel.G_RDF4J)
                .subject(RdfTestModel.S_VALUE)
                    .add(RdfTestModel.P_VALUE, RdfTestModel.O_VALUE);
        builder.defaultGraph().subject(RdfTestModel.S1_VALUE).add(RdfTestModel.P_VALUE, RdfTestModel.O1_VALUE);
        final Model m = builder.build();
        rdf4jGraph = new RDF4JGraph(m);
    }

    @Test
    void checkInstance() {
        assertTrue(service instanceof RDF4JService);
    }

    @Test
    void parseToDatasetRelativeURIsButNoBaseURI() throws IOException {
        try (final InputStream input = RdfServices.class.getResourceAsStream("/relativeURIs.ttl")) {
            assertThrows(IOException.class, () -> service.toDataset(Syntax.TURTLE, input));
        }
    }

    @Test
    void serializeFromDatasetTRIGRoundtrip() throws IOException {
        try (final ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            service.fromDataset(rdf4jDataset, Syntax.TRIG, output);
            final InputStream input = new ByteArrayInputStream(output.toByteArray());
            final Dataset roundtrip = service.toDataset(Syntax.TRIG, input);
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
            service.fromDataset(rdf4jDataset, Syntax.TURTLE, output);
            final InputStream input = new ByteArrayInputStream(output.toByteArray());
            final Dataset roundtrip = service.toDataset(Syntax.TURTLE, input);

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
            service.fromGraph(rdf4jGraph, Syntax.TURTLE, output);
            final InputStream input = new ByteArrayInputStream(output.toByteArray());
            final Graph roundtrip = service.toGraph(Syntax.TURTLE, input);
            assertEquals(2, roundtrip.stream().count());
            assertEquals(rdf4jGraph.stream().count(), roundtrip.stream().count());
            final String st = rdf4jGraph.stream(RdfTestModel.S_RDFNode, null, null).findFirst()
                    .get().getSubject().getURI().toString();
            final String st1 = roundtrip
                    .stream(RdfTestModel.S_RDFNode, RdfTestModel.P_RDFNode, RdfTestModel.O_RDFNode)
                    .findFirst().get().getSubject().getURI().toString();
            assertEquals(st, st1);
        }
    }

    @Test
    void serializeFromDatasetException() throws IOException {
        final File tmp = Files.createTempFile(null, null).toFile();
        try (final OutputStream output = new FileOutputStream(tmp)) {
            output.close();
            assertThrows(IOException.class, () -> service.fromDataset(rdf4jDataset, Syntax.TRIG, output));
        }
    }

    @Test
    void serializeFromGraphException() throws IOException {
        final File tmp = Files.createTempFile(null, null).toFile();
        try (final OutputStream output = new FileOutputStream(tmp)) {
            output.close();
            assertThrows(IOException.class,
                    () -> service.fromGraph(rdf4jGraph, Syntax.TURTLE, output));
        }
    }
}
