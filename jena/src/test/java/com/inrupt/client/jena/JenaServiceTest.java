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
package com.inrupt.client.jena;

import static org.junit.jupiter.api.Assertions.*;

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

import org.apache.commons.rdf.api.Dataset;
import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Quad;
import org.apache.commons.rdf.api.RDFSyntax;
import org.apache.commons.rdf.api.Triple;
import org.apache.jena.atlas.RuntimeIOException;
import org.apache.jena.commonsrdf.JenaCommonsRDF;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JenaServiceTest extends RdfServices {

    private final RdfService service = ServiceProvider.getRdfService();

    private static final Model model = ModelFactory.createDefaultModel();

    Dataset jenaDataset;
    Graph jenaGraph;

    @BeforeEach
    void setup() {
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

        jenaDataset = JenaCommonsRDF.fromJena(dsg);

        // create a JenaGraph
        model.add(JenaTestModel.S_JENA, JenaTestModel.P_JENA, JenaTestModel.O_JENA);
        model.add(JenaTestModel.S1_JENA, JenaTestModel.P_JENA, JenaTestModel.O1_JENA);
        jenaGraph = JenaCommonsRDF.fromJena(model.getGraph());
    }

    @Test
    void checkInstance() {
        assertTrue(service instanceof JenaService);
    }

    @Test
    void parseToDatasetRelativeURIsButNoBaseURI() throws IOException {
        try (final var input = JenaServiceTest.class.getResourceAsStream("/relativeURIs.ttl")) {
            final var dataset = service.toDataset(RDFSyntax.TURTLE, input, null);
            assertEquals(2, dataset.size());
            assertTrue(dataset.stream()
                    .map(Quad::getSubject)
                    .filter(IRI.class::isInstance).map(IRI.class::cast)
                    .map(IRI::getIRIString).anyMatch(iri -> iri.contains("file://")));
        }
    }

    @Test
    void serializeFromDatasetTRIGRoundtrip() throws IOException {
        try (final var output = new ByteArrayOutputStream()) {
            service.fromDataset(jenaDataset, RDFSyntax.TRIG, output);
            final var input = new ByteArrayInputStream(output.toByteArray());
            final var roundtrip = service.toDataset(RDFSyntax.TRIG, input, null);
            assertEquals(2, roundtrip.size());
            assertEquals(jenaDataset.size(), roundtrip.size());
            final var st = jenaDataset.stream(Optional.of(RdfTestModel.G_RDFNode), null, null, null)
                .map(Quad::getSubject).filter(IRI.class::isInstance).map(IRI.class::cast)
                .map(IRI::getIRIString).findFirst();
            final var st1 = roundtrip.stream(Optional.of(RdfTestModel.G_RDFNode),
                    RdfTestModel.S_RDFNode, RdfTestModel.P_RDFNode, RdfTestModel.O_RDFNode)
                .map(Quad::getSubject).filter(IRI.class::isInstance).map(IRI.class::cast)
                .map(IRI::getIRIString).findFirst();
            assertEquals(st, st1);
        }
    }

    @Test
    void serializeFromDatasetTURTLERoundtrip() throws IOException {
        try (final var output = new ByteArrayOutputStream()) {
            service.fromDataset(jenaDataset, RDFSyntax.TURTLE, output);
            final var input = new ByteArrayInputStream(output.toByteArray());
            final var roundtrip = service.toDataset(RDFSyntax.TURTLE, input, null);

            assertEquals(2, roundtrip.size());
            assertEquals(jenaDataset.size(), roundtrip.size());
            final var st = jenaDataset.stream(Optional.of(RdfTestModel.G_RDFNode), null, null, null)
                .map(Quad::getSubject).filter(IRI.class::isInstance).map(IRI.class::cast)
                .map(IRI::getIRIString).findFirst();
            final var st1 = roundtrip.stream(null, RdfTestModel.S_RDFNode, RdfTestModel.P_RDFNode,
                    RdfTestModel.O_RDFNode)
                .map(Quad::getSubject).filter(IRI.class::isInstance).map(IRI.class::cast)
                .map(IRI::getIRIString).findFirst();
            assertEquals(st, st1);
        }
    }

    @Test
    void serializeFromGraphRoundtrip() throws IOException {
        try (final var output = new ByteArrayOutputStream()) {
            service.fromGraph(jenaGraph, RDFSyntax.TURTLE, output);
            final var input = new ByteArrayInputStream(output.toByteArray());
            final var roundtrip = service.toGraph(RDFSyntax.TURTLE, input, null);
            assertEquals(2, roundtrip.size());
            assertEquals(jenaGraph.size(), roundtrip.size());
            final var st = jenaGraph.stream(RdfTestModel.S_RDFNode, null, null)
                .map(Triple::getSubject).filter(IRI.class::isInstance).map(IRI.class::cast)
                .map(IRI::getIRIString).findFirst();
            final var st1 = roundtrip.stream(RdfTestModel.S_RDFNode, RdfTestModel.P_RDFNode,
                    RdfTestModel.O_RDFNode)
                .map(Triple::getSubject).filter(IRI.class::isInstance).map(IRI.class::cast)
                .map(IRI::getIRIString).findFirst();
            assertEquals(st, st1);
        }
    }

    @Test
    void serializeFromDatasetException() throws IOException {
        final var tmp = Files.createTempFile(null, null).toFile();
        try (final var output = new FileOutputStream(tmp)) {
            output.close();
            assertThrows(RuntimeIOException.class, () -> service.fromDataset(jenaDataset, RDFSyntax.TRIG, output));
        }
    }

    @Test
    void serializeFromGraphException() throws IOException {
        final var tmp = Files.createTempFile(null, null).toFile();
        try (final var output = new FileOutputStream(tmp)) {
            output.close();
            assertThrows(RuntimeIOException.class, () -> service.fromGraph(jenaGraph, RDFSyntax.TURTLE, output));
        }
    }
}
