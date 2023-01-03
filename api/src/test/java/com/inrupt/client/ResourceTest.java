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
package com.inrupt.client;

import static org.junit.jupiter.api.Assertions.*;

import com.inrupt.client.spi.RDFFactory;

import java.net.URI;
import java.util.stream.Stream;

import org.apache.commons.rdf.api.BlankNode;
import org.apache.commons.rdf.api.Dataset;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Literal;
import org.apache.commons.rdf.api.Quad;
import org.apache.commons.rdf.api.RDF;
import org.junit.jupiter.api.Test;

class ResourceTest {

    static final RDF rdf = RDFFactory.getInstance();

    @Test
    void testPathSimple() {
        final Dataset dataset = rdf.createDataset();
        final URI id = URI.create("https://resource.test/path");
        final IRI exFoo = rdf.createIRI("https://example.test/foo");
        final IRI exBar = rdf.createIRI("https://example.test/bar");

        dataset.add(rdf.createQuad(null, rdf.createIRI(id.toString()), exFoo, exBar));

        final Resource resource = new Resource(id, dataset);
        try (final Stream<Quad> stream = resource.path(exFoo)) {
            assertEquals(1, stream.count());
        }

        try (final Stream<Quad> stream = resource.path(exFoo, exBar)) {
            assertEquals(0, stream.count());
        }

        try (final Stream<Quad> stream = resource.path(exBar)) {
            assertEquals(0, stream.count());
        }

        try (final Stream<Quad> stream = resource.path()) {
            assertEquals(0, stream.count());
        }
    }

    @Test
    void testPathComplex() {
        final Dataset dataset = rdf.createDataset();
        final URI id = URI.create("https://resource.test/path");
        final IRI exFoo = rdf.createIRI("https://example.test/foo");
        final IRI exBar = rdf.createIRI("https://example.test/bar");
        final IRI exBaz = rdf.createIRI("https://example.test/baz");
        final IRI obj1 = rdf.createIRI("https://example.test/1");
        final IRI obj2 = rdf.createIRI("https://example.test/2");
        final IRI obj3 = rdf.createIRI("https://example.test/3");
        final IRI obj4 = rdf.createIRI("https://example.test/4");
        final IRI obj5 = rdf.createIRI("https://example.test/5");
        final IRI obj6 = rdf.createIRI("https://example.test/6");
        final IRI obj7 = rdf.createIRI("https://example.test/7");
        final IRI obj8 = rdf.createIRI("https://example.test/8");

        dataset.add(rdf.createQuad(null, rdf.createIRI(id.toString()), exFoo, obj1));
        dataset.add(rdf.createQuad(null, rdf.createIRI(id.toString()), exFoo, obj2));
        dataset.add(rdf.createQuad(null, obj1, exBar, obj3));
        dataset.add(rdf.createQuad(null, obj1, exBar, obj4));
        dataset.add(rdf.createQuad(null, obj1, exBaz, obj5));
        dataset.add(rdf.createQuad(null, obj2, exBar, obj6));
        dataset.add(rdf.createQuad(null, obj3, exBaz, obj7));
        dataset.add(rdf.createQuad(null, obj4, exBaz, obj8));

        final Resource resource = new Resource(id, dataset);
        try (final Stream<Quad> stream = resource.path()) {
            assertEquals(0, stream.count());
        }

        try (final Stream<Quad> stream = resource.path(exFoo)) {
            assertEquals(2, stream.count());
        }

        try (final Stream<Quad> stream = resource.path(exFoo, exBar)) {
            assertEquals(3, stream.count());
        }

        try (final Stream<Quad> stream = resource.path(exFoo, exBaz)) {
            assertEquals(1, stream.count());
        }

        try (final Stream<Quad> stream = resource.path(exFoo, exBar, exBaz)) {
            assertEquals(2, stream.count());
        }
    }

    @Test
    void testPathComplexBnodes() {
        final Dataset dataset = rdf.createDataset();
        final URI id = URI.create("https://resource.test/path");
        final IRI exFoo = rdf.createIRI("https://example.test/foo");
        final IRI exBar = rdf.createIRI("https://example.test/bar");
        final IRI exBaz = rdf.createIRI("https://example.test/baz");
        final BlankNode obj1 = rdf.createBlankNode();
        final BlankNode obj2 = rdf.createBlankNode();
        final BlankNode obj3 = rdf.createBlankNode();
        final BlankNode obj4 = rdf.createBlankNode();
        final BlankNode obj5 = rdf.createBlankNode();
        final BlankNode obj6 = rdf.createBlankNode();
        final BlankNode obj7 = rdf.createBlankNode();
        final BlankNode obj8 = rdf.createBlankNode();

        dataset.add(rdf.createQuad(null, rdf.createIRI(id.toString()), exFoo, obj1));
        dataset.add(rdf.createQuad(null, rdf.createIRI(id.toString()), exFoo, obj2));
        dataset.add(rdf.createQuad(null, obj1, exBar, obj3));
        dataset.add(rdf.createQuad(null, obj1, exBar, obj4));
        dataset.add(rdf.createQuad(null, obj1, exBaz, obj5));
        dataset.add(rdf.createQuad(null, obj2, exBar, obj6));
        dataset.add(rdf.createQuad(null, obj3, exBaz, obj7));
        dataset.add(rdf.createQuad(null, obj4, exBaz, obj8));

        final Resource resource = new Resource(id, dataset);
        try (final Stream<Quad> stream = resource.path()) {
            assertEquals(0, stream.count());
        }

        try (final Stream<Quad> stream = resource.path(exFoo)) {
            assertEquals(2, stream.count());
        }

        try (final Stream<Quad> stream = resource.path(exFoo, exBar)) {
            assertEquals(3, stream.count());
        }

        try (final Stream<Quad> stream = resource.path(exFoo, exBaz)) {
            assertEquals(1, stream.count());
        }

        try (final Stream<Quad> stream = resource.path(exFoo, exBar, exBaz)) {
            assertEquals(2, stream.count());
        }
    }

    @Test
    void testPathComplexLiteral() {
        final Dataset dataset = rdf.createDataset();
        final URI id = URI.create("https://resource.test/path");
        final IRI exFoo = rdf.createIRI("https://example.test/foo");
        final IRI exBar = rdf.createIRI("https://example.test/bar");
        final IRI exBaz = rdf.createIRI("https://example.test/baz");
        final Literal obj1 = rdf.createLiteral("1");
        final Literal obj2 = rdf.createLiteral("2");
        final BlankNode obj3 = rdf.createBlankNode();
        final BlankNode obj4 = rdf.createBlankNode();

        dataset.add(rdf.createQuad(null, rdf.createIRI(id.toString()), exFoo, obj1));
        dataset.add(rdf.createQuad(null, rdf.createIRI(id.toString()), exFoo, obj2));
        dataset.add(rdf.createQuad(null, obj3, exBar, obj4));

        final Resource resource = new Resource(id, dataset);
        try (final Stream<Quad> stream = resource.path()) {
            assertEquals(0, stream.count());
        }

        try (final Stream<Quad> stream = resource.path(exFoo)) {
            assertEquals(2, stream.count());
        }

        try (final Stream<Quad> stream = resource.path(exFoo, exBar)) {
            assertEquals(0, stream.count());
        }

        try (final Stream<Quad> stream = resource.path(exFoo, exBaz)) {
            assertEquals(0, stream.count());
        }

        try (final Stream<Quad> stream = resource.path(exFoo, exBar, exBaz)) {
            assertEquals(0, stream.count());
        }
    }

    @Test
    void testValidate() {
        final URI id = URI.create("https://resource.test/path");
        final Resource resource = new Resource(id, null);
        assertDoesNotThrow(() -> resource.validate());
    }
}
