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
package com.inrupt.client.wrapping;

import static com.inrupt.client.wrapping.ValueMappings.*;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.inrupt.client.spi.RDFFactory;

import java.net.URI;
import java.time.Instant;
import java.time.format.DateTimeParseException;

import org.apache.commons.rdf.api.BlankNode;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Literal;
import org.apache.commons.rdf.api.RDF;
import org.junit.jupiter.api.Test;

class ValueMappingTest {
    private static final RDF FACTORY = RDFFactory.getInstance();

    @Test
    void literalAsStringTest() {
        final BlankNode blank = FACTORY.createBlankNode();
        final Literal literal = FACTORY.createLiteral(randomUUID().toString());

        assertThrows(NullPointerException.class, () -> literalAsString(null));
        assertThrows(IllegalStateException.class, () -> literalAsString(blank));

        assertThat(literalAsString(literal), is(literal.getLexicalForm()));
    }

    @Test
    void iriAsStringTest() {
        final BlankNode blank = FACTORY.createBlankNode();
        final IRI iri = FACTORY.createIRI("urn:" + randomUUID());

        assertThrows(NullPointerException.class, () -> iriAsString(null));
        assertThrows(IllegalStateException.class, () -> iriAsString(blank));

        assertThat(iriAsString(iri), is(iri.getIRIString()));
    }

    @Test
    void iriAsUriTest() {
        final BlankNode blank = FACTORY.createBlankNode();
        final IRI iri = FACTORY.createIRI("urn:" + randomUUID());

        assertThrows(NullPointerException.class, () -> iriAsUri(null));
        assertThrows(IllegalStateException.class, () -> iriAsUri(blank));

        assertThat(iriAsUri(iri), is(URI.create(iri.getIRIString())));
    }

    @Test
    void literalAsInstantTest() {
        final BlankNode blank = FACTORY.createBlankNode();
        final Literal malformed = FACTORY.createLiteral(randomUUID().toString());
        final Literal literal = FACTORY.createLiteral(Instant.now().toString());

        assertThrows(NullPointerException.class, () -> literalAsInstant(null));
        assertThrows(IllegalStateException.class, () -> literalAsInstant(blank));
        assertThrows(DateTimeParseException.class, () -> literalAsInstant(malformed));

        assertThat(literalAsInstant(literal), is(Instant.parse(literal.getLexicalForm())));
    }

    @Test
    void literalAsIntegerOrNullTest() {
        final BlankNode blank = FACTORY.createBlankNode();
        final Literal malformed = FACTORY.createLiteral(randomUUID().toString());
        final Literal literal = FACTORY.createLiteral(String.valueOf(Integer.MAX_VALUE));

        assertThrows(NullPointerException.class, () -> literalAsIntegerOrNull(null));
        assertThrows(IllegalStateException.class, () -> literalAsIntegerOrNull(blank));

        assertThat(literalAsIntegerOrNull(malformed), is(nullValue()));
        assertThat(literalAsIntegerOrNull(literal), is(Integer.parseInt(literal.getLexicalForm())));
    }
}
