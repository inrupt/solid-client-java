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

import com.inrupt.client.Syntax;
import com.inrupt.client.spi.RdfService;
import com.inrupt.client.spi.ServiceProvider;
import com.inrupt.client.test.rdf.RdfServices;
import com.inrupt.client.test.rdf.RdfTestModel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

import org.apache.jena.atlas.RuntimeIOException;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class JenaServiceTest extends RdfServices {

    private final RdfService service = ServiceProvider.getRdfService();
    private static JenaDataset jenaDataset;
    private static JenaGraph jenaGraph;

    private static final Model model = ModelFactory.createDefaultModel();

    private static final Resource S_JENA = model.createResource(RdfTestModel.S_VALUE);
    private static final Literal O_JENA = model.createLiteral(RdfTestModel.O_VALUE);
    private static final Property P_JENA = model.createProperty(RdfTestModel.P_VALUE);

    private static final Resource S1_JENA = model.createResource(RdfTestModel.S1_VALUE);
    private static final Literal O1_JENA = model.createLiteral(RdfTestModel.O1_VALUE);

    public static final Node S_NODE = NodeFactory.createURI(RdfTestModel.S_VALUE);
    public static final Node P_NODE = NodeFactory.createURI(RdfTestModel.P_VALUE);
    public static final Node O_NODE = NodeFactory.createLiteral(RdfTestModel.O_VALUE);
    public static final Node G_NODE = NodeFactory.createURI(RdfTestModel.G_VALUE);

    public static final Node S1_NODE = NodeFactory.createURI(RdfTestModel.S1_VALUE);
    public static final Node P1_NODE = NodeFactory.createURI(RdfTestModel.P1_VALUE);
    public static final Node O1_NODE = NodeFactory.createLiteral(RdfTestModel.O1_VALUE);

    @BeforeAll
    static void setup() {
        // create a JenaDataset
        final DatasetGraph dsg = DatasetGraphFactory.create();
        dsg.add(G_NODE,
            S_NODE,
            P_NODE,
            O_NODE);

        dsg.getDefaultGraph()
            .add(S1_NODE,
                P1_NODE,
                O1_NODE
            );

        jenaDataset = new JenaDataset(dsg);

        // create a JenaGraph
        model.add(S_JENA, P_JENA, O_JENA);
        model.add(S1_JENA, P_JENA, O1_JENA);
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
