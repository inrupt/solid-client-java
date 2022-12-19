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

import com.inrupt.client.rdf.Syntax;
import com.inrupt.client.spi.RdfService;
import com.inrupt.client.spi.ServiceProvider;
import com.inrupt.client.test.RdfServices;
import com.inrupt.client.test.RdfTestModel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

import org.apache.jena.atlas.RuntimeIOException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class JenaServiceTest extends RdfServices {

    private final RdfService service = ServiceProvider.getRdfService();
    private static JenaDataset jenaDataset;
    private static JenaGraph jenaGraph;

    private static final Model model = ModelFactory.createDefaultModel();

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
        model.add(JenaTestModel.S_JENA, JenaTestModel.P_JENA, JenaTestModel.O_JENA);
        model.add(JenaTestModel.S1_JENA, JenaTestModel.P_JENA, JenaTestModel.O1_JENA);
        jenaGraph = new JenaGraph(model);
    }

    @Test
    void checkInstance() {
        assertTrue(service instanceof JenaService);
    }

    @Test
    void parseToDatasetRelativeURIsButNoBaseURI() throws IOException {
        try (final var input = JenaServiceTest.class.getResourceAsStream("/relativeURIs.ttl")) {
            final var dataset = service.toDataset(Syntax.TURTLE, input);
            assertEquals(2, dataset.stream().count());
            assertTrue(dataset.stream().findFirst().get().getSubject().getURI().toString()
                .contains("file://") //treats relative URIs like local files
            );
        }
    }

    @Test
    void serializeFromDatasetTRIGRoundtrip() throws IOException {
        try (final var output = new ByteArrayOutputStream()) {
            service.fromDataset(jenaDataset, Syntax.TRIG, output);
            final var input = new ByteArrayInputStream(output.toByteArray());
            final var roundtrip = service.toDataset(Syntax.TRIG, input);
            assertEquals(2, roundtrip.stream().count());
            assertEquals(jenaDataset.stream().count(), roundtrip.stream().count());
            final var st = jenaDataset.stream(
                            Optional.of(RdfTestModel.G_RDFNode),
                            null,
                            null,
                            null
                            ).findFirst().get().getSubject().getURI().toString();
            final var st1 = roundtrip.stream(
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
        try (final var output = new ByteArrayOutputStream()) {
            service.fromDataset(jenaDataset, Syntax.TURTLE, output);
            final var input = new ByteArrayInputStream(output.toByteArray());
            final var roundtrip = service.toDataset(Syntax.TURTLE, input);

            assertEquals(2, roundtrip.stream().count());
            assertEquals(jenaDataset.stream().count(), roundtrip.stream().count());
            final var st = jenaDataset.stream(
                            Optional.of(RdfTestModel.G_RDFNode),
                            null,
                            null,
                            null
                            ).findFirst().get().getSubject().getURI().toString();
            final var st1 = roundtrip.stream(
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
        try (final var output = new ByteArrayOutputStream()) {
            service.fromGraph(jenaGraph, Syntax.TURTLE, output);
            final var input = new ByteArrayInputStream(output.toByteArray());
            final var roundtrip = service.toGraph(Syntax.TURTLE, input);
            assertEquals(2, roundtrip.stream().count());
            assertEquals(jenaGraph.stream().count(), roundtrip.stream().count());
            final var st = jenaGraph.stream(RdfTestModel.S_RDFNode, null, null).findFirst().get()
                    .getSubject().getURI().toString();
            final var st1 = roundtrip
                    .stream(RdfTestModel.S_RDFNode, RdfTestModel.P_RDFNode, RdfTestModel.O_RDFNode)
                    .findFirst().get().getSubject().getURI().toString();
            assertEquals(st, st1);
        }
    }

    @Test
    void serializeFromDatasetException() throws IOException {
        final var tmp = Files.createTempFile(null, null).toFile();
        try (final var output = new FileOutputStream(tmp)) {
            output.close();
            assertThrows(RuntimeIOException.class, () -> service.fromDataset(jenaDataset, Syntax.TRIG, output));
        }
    }

    @Test
    void serializeFromGraphException() throws IOException {
        final var tmp = Files.createTempFile(null, null).toFile();
        try (final var output = new FileOutputStream(tmp)) {
            output.close();
            assertThrows(RuntimeIOException.class, () -> service.fromGraph(jenaGraph, Syntax.TURTLE, output));
        }
    }
}
