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

import com.inrupt.client.spi.RdfService;
import com.inrupt.client.spi.ServiceProvider;
import com.inrupt.client.test.RdfServices;
import com.inrupt.client.test.RdfTestModel;
import com.inrupt.commons.rdf4j.RDF4J;
import com.inrupt.commons.rdf4j.RDF4JDataset;
import com.inrupt.commons.rdf4j.RDF4JGraph;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.rdf.api.Dataset;
import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Quad;
import org.apache.commons.rdf.api.RDFSyntax;
import org.apache.commons.rdf.api.Triple;
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
    private static RDF4J rdf = new RDF4J();

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
        rdf4jDataset = rdf.asDataset(repository);

        // create a RDF4JGraph
        final ModelBuilder builder = new ModelBuilder();
        builder.namedGraph(RDF4JTestModel.G_RDF4J)
                .subject(RdfTestModel.S_VALUE)
                    .add(RdfTestModel.P_VALUE, RdfTestModel.O_VALUE);
        builder.defaultGraph().subject(RdfTestModel.S1_VALUE).add(RdfTestModel.P_VALUE, RdfTestModel.O1_VALUE);
        final Model m = builder.build();
        rdf4jGraph = rdf.asGraph(m);
    }

    @Test
    void checkInstance() {
        assertTrue(service instanceof RDF4JService);
    }

    @Test
    void parseToDatasetRelativeURIsButNoBaseURI() throws IOException {
        try (final InputStream input = RdfServices.class.getResourceAsStream("/relativeURIs.ttl")) {
            assertThrows(IOException.class, () -> service.toDataset(RDFSyntax.TURTLE, input, null));
        }
    }

    @Test
    void serializeFromDatasetTRIGRoundtrip() throws Exception {
        try (final ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            service.fromDataset(rdf4jDataset, RDFSyntax.TRIG, output);
            try (final InputStream input = new ByteArrayInputStream(output.toByteArray());
                    final Dataset roundtrip = service.toDataset(RDFSyntax.TRIG, input, null)) {
                assertEquals(2, roundtrip.size());
                assertEquals(rdf4jDataset.size(), roundtrip.size());
                String st1 = null;
                try (final Stream<Quad> stream = rdf4jDataset
                        .stream(Optional.of(RdfTestModel.G_RDFNode), null, null, null).map(Quad.class::cast)) {
                    st1 = stream.map(Quad::getSubject).filter(IRI.class::isInstance).map(IRI.class::cast)
                        .map(IRI::getIRIString).findFirst().orElse(null);
                }

                String st2 = null;
                try (final Stream<Quad> stream = roundtrip.stream(Optional.of(RdfTestModel.G_RDFNode),
                        RdfTestModel.S_RDFNode, RdfTestModel.P_RDFNode, RdfTestModel.O_RDFNode).map(Quad.class::cast)) {
                    st2 = stream.map(Quad::getSubject).filter(IRI.class::isInstance).map(IRI.class::cast)
                        .map(IRI::getIRIString).findFirst().orElse(null);
                }
                assertEquals(st1, st2);
            }
        }
    }

    @Test
    void serializeFromDatasetTURTLERoundtrip() throws Exception {
        try (final ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            service.fromDataset(rdf4jDataset, RDFSyntax.TURTLE, output);
            try (final InputStream input = new ByteArrayInputStream(output.toByteArray());
                    final Dataset roundtrip = service.toDataset(RDFSyntax.TURTLE, input, null)) {

                assertEquals(2, roundtrip.size());
                assertEquals(rdf4jDataset.size(), roundtrip.size());

                String st1 = null;
                try (final Stream<Quad> stream = rdf4jDataset
                        .stream(Optional.of(RdfTestModel.G_RDFNode), null, null, null).map(Quad.class::cast)) {
                    st1 = stream.map(Quad::getSubject).filter(IRI.class::isInstance).map(IRI.class::cast)
                        .map(IRI::getIRIString).findFirst().orElse(null);
                }
                String st2 = null;
                try (final Stream<Quad> stream = roundtrip.stream(null, RdfTestModel.S_RDFNode, RdfTestModel.P_RDFNode,
                        RdfTestModel.O_RDFNode).map(Quad.class::cast)) {
                    st2 = stream.map(Quad::getSubject).filter(IRI.class::isInstance).map(IRI.class::cast)
                        .map(IRI::getIRIString).findFirst().orElse(null);
                }
                assertEquals(st1, st2);
            }
        }
    }

    @Test
    void serializeFromGraphRoundtrip() throws Exception {
        try (final ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            service.fromGraph(rdf4jGraph, RDFSyntax.TURTLE, output);
            try (final InputStream input = new ByteArrayInputStream(output.toByteArray());
                    final Graph roundtrip = service.toGraph(RDFSyntax.TURTLE, input, null)) {
                assertEquals(2, roundtrip.size());
                assertEquals(rdf4jGraph.size(), roundtrip.size());

                String st1 = null;
                try (final Stream<Triple> stream = rdf4jGraph.stream(RdfTestModel.S_RDFNode, null, null)
                        .map(Triple.class::cast)) {
                    st1 = stream.map(Triple::getSubject).filter(IRI.class::isInstance).map(IRI.class::cast)
                        .map(IRI::getIRIString).findFirst().orElse(null);
                }

                String st2 = null;
                try (final Stream<Triple> stream = roundtrip
                        .stream(RdfTestModel.S_RDFNode, RdfTestModel.P_RDFNode, RdfTestModel.O_RDFNode)
                        .map(Triple.class::cast)) {
                    st2 = stream.map(Triple::getSubject).filter(IRI.class::isInstance).map(IRI.class::cast)
                        .map(IRI::getIRIString).findFirst().orElse(null);
                }
                assertEquals(st1, st2);
            }
        }
    }

    @Test
    void serializeFromDatasetException() throws IOException {
        final File tmp = Files.createTempFile(null, null).toFile();
        try (final OutputStream output = new FileOutputStream(tmp)) {
            output.close();
            assertThrows(IOException.class, () -> service.fromDataset(rdf4jDataset, RDFSyntax.TRIG, output));
        }
    }

    @Test
    void serializeFromGraphException() throws IOException {
        final File tmp = Files.createTempFile(null, null).toFile();
        try (final OutputStream output = new FileOutputStream(tmp)) {
            output.close();
            assertThrows(IOException.class,
                    () -> service.fromGraph(rdf4jGraph, RDFSyntax.TURTLE, output));
        }
    }
}
