/*
 * Copyright 2023 Inrupt Inc.
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
package com.inrupt.commons.wrapping.test;

import static com.inrupt.commons.wrapping.TermMappings.*;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.inrupt.commons.wrapping.RDFFactory;

import java.net.URI;
import java.time.Instant;

import org.apache.commons.rdf.api.*;
import org.junit.jupiter.api.Test;

public class TermMappingTestBase {
    private static final RDF FACTORY = RDFFactory.getInstance();
    private static final Graph GRAPH = FACTORY.createGraph();

    @Test
    void asStringLiteralTest() {
        final IRI xsdString = FACTORY.createIRI("http://www.w3.org/2001/XMLSchema#string");
        final String string = randomUUID().toString();

        assertThrows(NullPointerException.class, () -> asStringLiteral(null, null));
        assertThrows(NullPointerException.class, () -> asStringLiteral(string, null));

        assertThat(asStringLiteral(string, GRAPH), both(
                instanceOf(Literal.class)).and(
                hasProperty("lexicalForm", is(string))).and(
                hasProperty("datatype", is(xsdString))));
    }

    @Test
    void asIriResourceStringTest() {
        final String uri = "urn:" + randomUUID();

        assertThrows(NullPointerException.class, () -> asIri((String) null, null));
        assertThrows(NullPointerException.class, () -> asIri(uri, null));

        assertThat(asIri(uri, GRAPH), both(
                instanceOf(IRI.class)).and(
                hasProperty("IRIString", is(uri))));
    }

    @Test
    void asIriResourceUriTest() {
        final URI uri = URI.create("urn:" + randomUUID());

        assertThrows(NullPointerException.class, () -> asIri((URI) null, null));
        assertThrows(NullPointerException.class, () -> asIri(uri, null));

        assertThat(asIri(uri, GRAPH), both(
                instanceOf(IRI.class)).and(
                hasProperty("IRIString", is(uri.toString()))));
    }

    @Test
    void asTypedLiteralTest() {
        final IRI xsdDateTime = FACTORY.createIRI("http://www.w3.org/2001/XMLSchema#dateTime");
        final Instant instant = Instant.now();

        assertThrows(NullPointerException.class, () -> asTypedLiteral(null, null));
        assertThrows(NullPointerException.class, () -> asTypedLiteral(instant, null));

        assertThat(asTypedLiteral(instant, GRAPH), both(
                instanceOf(Literal.class)).and(
                hasProperty("lexicalForm", is(instant.toString()))).and(
                hasProperty("datatype", is(xsdDateTime))));
    }

    @Test
    void identityTest() {
        final BlankNode blank = FACTORY.createBlankNode();

        assertThrows(NullPointerException.class, () -> identity(null, null));
        assertThrows(NullPointerException.class, () -> identity(blank, null));

        assertThat(identity(blank, GRAPH), sameInstance(blank));
    }
}
