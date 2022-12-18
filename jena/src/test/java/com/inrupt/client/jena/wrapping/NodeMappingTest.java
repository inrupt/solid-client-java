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
package com.inrupt.client.jena.wrapping;

import static com.inrupt.client.jena.wrapping.NodeMappings.*;
import static java.util.UUID.randomUUID;
import static org.apache.jena.rdf.model.ModelFactory.createDefaultModel;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.time.Instant;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.XSD;
import org.junit.jupiter.api.Test;

class NodeMappingTest {
    private static final Model MODEL = createDefaultModel();

    @Test
    void asStringLiteralTest() {
        final var string = randomUUID().toString();

        assertThrows(NullPointerException.class, () -> asStringLiteral(null, null));
        assertThrows(NullPointerException.class, () -> asStringLiteral(string, null));

        assertThat(asStringLiteral(string, MODEL), both(
                instanceOf(Literal.class)).and(
                hasProperty("lexicalForm", is(string))).and(
                hasProperty("datatypeURI", is(XSD.xstring.getURI()))));
    }

    @Test
    void asIriResourceStringTest() {
        final var uri = randomUUID().toString();

        assertThrows(NullPointerException.class, () -> asIriResource((String) null, null));
        assertThrows(NullPointerException.class, () -> asIriResource(uri, null));

        assertThat(asIriResource(uri, MODEL), both(
                instanceOf(Resource.class)).and(
                hasProperty("URI", is(uri))));
    }

    @Test
    void asIriResourceUriTest() {
        final var uri = URI.create(randomUUID().toString());

        assertThrows(NullPointerException.class, () -> asIriResource((URI) null, null));
        assertThrows(NullPointerException.class, () -> asIriResource(uri, null));

        assertThat(asIriResource(uri, MODEL), both(
                instanceOf(Resource.class)).and(
                hasProperty("URI", is(uri.toString()))));
    }

    @Test
    void asTypedLiteralTest() {
        final var instant = Instant.now();

        assertThrows(NullPointerException.class, () -> asTypedLiteral(null, null));
        assertThrows(NullPointerException.class, () -> asTypedLiteral(instant, null));

        assertThat(asTypedLiteral(instant, MODEL), both(
                instanceOf(Literal.class)).and(
                hasProperty("lexicalForm", is(instant.toString()))).and(
                hasProperty("datatypeURI", is(XSD.dateTime.getURI()))));
    }

    @Test
    void identityTest() {
        final var resource = ResourceFactory.createResource();

        assertThrows(NullPointerException.class, () -> identity(null, null));
        assertThrows(NullPointerException.class, () -> identity(resource, null));

        assertThat(identity(resource, MODEL), sameInstance(resource));
    }
}
