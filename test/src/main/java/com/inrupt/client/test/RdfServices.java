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

import com.inrupt.client.Dataset;
import com.inrupt.client.Graph;
import com.inrupt.client.Syntax;
import com.inrupt.client.spi.RdfService;
import com.inrupt.client.spi.ServiceProvider;

import java.io.IOException;
import java.io.InputStream;

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

}