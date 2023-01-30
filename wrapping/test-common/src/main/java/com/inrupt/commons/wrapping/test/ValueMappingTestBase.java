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

import static com.inrupt.commons.wrapping.ValueMappings.*;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.inrupt.commons.wrapping.RDFFactory;
import com.inrupt.commons.wrapping.ValueMapping;
import com.inrupt.commons.wrapping.WrapperIRI;

import java.net.URI;
import java.time.Instant;
import java.time.format.DateTimeParseException;

import org.apache.commons.rdf.api.*;
import org.junit.jupiter.api.Test;

public class ValueMappingTestBase {
    private static final RDF FACTORY = RDFFactory.getInstance();
    private static final Graph GRAPH = FACTORY.createGraph();
    private static final String URN_UUID = "urn:uuid:";

    @Test
    void literalAsStringTest() {
        final BlankNode blank = FACTORY.createBlankNode();
        final String string = randomUUID().toString();
        final Literal literal = FACTORY.createLiteral(string);

        assertThrows(NullPointerException.class, () -> literalAsString(null, null));
        assertThrows(NullPointerException.class, () -> literalAsString(literal, null));
        assertThrows(IllegalStateException.class, () -> literalAsString(blank, GRAPH));

        assertThat(literalAsString(literal, GRAPH), is(string));
    }

    @Test
    void iriAsStringTest() {
        final BlankNode blank = FACTORY.createBlankNode();
        final String string = URN_UUID + randomUUID();
        final IRI iri = FACTORY.createIRI(string);

        assertThrows(NullPointerException.class, () -> iriAsString(null, null));
        assertThrows(NullPointerException.class, () -> iriAsString(iri, null));
        assertThrows(IllegalStateException.class, () -> iriAsString(blank, GRAPH));

        assertThat(iriAsString(iri, GRAPH), is(string));
    }

    @Test
    void literalAsInstantTest() {
        final BlankNode blank = FACTORY.createBlankNode();
        final Literal malformed = FACTORY.createLiteral(randomUUID().toString());
        final Instant instant = Instant.now();
        final Literal literal = FACTORY.createLiteral(instant.toString());

        assertThrows(NullPointerException.class, () -> literalAsInstant(null, null));
        assertThrows(NullPointerException.class, () -> literalAsInstant(literal, null));
        assertThrows(IllegalStateException.class, () -> literalAsInstant(blank, GRAPH));
        assertThrows(DateTimeParseException.class, () -> literalAsInstant(malformed, GRAPH));

        assertThat(literalAsInstant(literal, GRAPH), is(instant));
    }

    @Test
    void literalAsBooleanTest() {
        final BlankNode blank = FACTORY.createBlankNode();
        final boolean bool = true;
        final Literal literal = FACTORY.createLiteral(Boolean.toString(bool));

        assertThrows(NullPointerException.class, () -> literalAsBoolean(null, null));
        assertThrows(NullPointerException.class, () -> literalAsBoolean(literal, null));
        assertThrows(IllegalStateException.class, () -> literalAsBoolean(blank, GRAPH));

        assertThat(literalAsBoolean(literal, GRAPH), is(bool));
    }

    @Test
    void asTest() {
        final IRI iri = FACTORY.createIRI(URN_UUID + randomUUID());
        final ValueMapping<Mock2CtorNode> asMockNode2Ctor = as(Mock2CtorNode.class);
        final ValueMapping<Mock1CtorNode> asMockNode1Ctor = as(Mock1CtorNode.class);
        final ValueMapping<Mock0CtorNode> asMockNodeNoCtor = as(Mock0CtorNode.class);
        final ValueMapping<MockBadCtorNode> asMockNodeBadCtor = as(MockBadCtorNode.class);

        assertThrows(NullPointerException.class, () -> as(null));

        assertThrows(NullPointerException.class, () -> asMockNode2Ctor.apply(null, GRAPH));
        assertThrows(NullPointerException.class, () -> asMockNode1Ctor.apply(null, GRAPH));
        assertThrows(IllegalStateException.class, () -> asMockNodeBadCtor.apply(iri, GRAPH));
        assertThrows(IllegalStateException.class, () -> asMockNodeNoCtor.apply(iri, GRAPH));

        assertThat(asMockNode2Ctor.apply(iri, GRAPH), is(instanceOf(Mock2CtorNode.class)));
        assertThat(asMockNode1Ctor.apply(iri, GRAPH), is(instanceOf(Mock1CtorNode.class)));
    }

    @Test
    void iriAsUriTest() {
        final BlankNode blank = FACTORY.createBlankNode();
        final URI uri = URI.create(URN_UUID + randomUUID());
        final IRI iri = FACTORY.createIRI(uri.toString());

        assertThrows(NullPointerException.class, () -> iriAsUri(null, null));
        assertThrows(NullPointerException.class, () -> iriAsUri(iri, null));
        assertThrows(IllegalStateException.class, () -> iriAsUri(blank, GRAPH));

        assertThat(iriAsUri(iri, GRAPH), is(uri));
    }

    @Test
    void literalAsIntegerOrNullTest() {
        final BlankNode blank = FACTORY.createBlankNode();
        final Literal malformed = FACTORY.createLiteral(randomUUID().toString());
        final int integer = Integer.MAX_VALUE;
        final Literal literal = FACTORY.createLiteral(String.valueOf(integer));

        assertThrows(NullPointerException.class, () -> literalAsIntegerOrNull(null, null));
        assertThrows(NullPointerException.class, () -> literalAsIntegerOrNull(literal, null));
        assertThrows(IllegalStateException.class, () -> literalAsIntegerOrNull(blank, GRAPH));

        assertThat(literalAsIntegerOrNull(malformed, GRAPH), is(nullValue()));
        assertThat(literalAsIntegerOrNull(literal, GRAPH), is(integer));
    }

    public static final class Mock2CtorNode extends WrapperIRI {
        public Mock2CtorNode(final RDFTerm node, final Graph graph) {
            super(node, graph);
        }
    }

    public static final class Mock1CtorNode extends WrapperIRI {
        private static final RDF FACTORY = RDFFactory.getInstance();

        public Mock1CtorNode(final RDFTerm node) {
            super(node, FACTORY.createGraph());
        }
    }

    public static final class MockBadCtorNode extends WrapperIRI {
        private static final RDF FACTORY = RDFFactory.getInstance();

        public MockBadCtorNode(final RDFTerm node) {
            super(node, FACTORY.createGraph());
            throw new RuntimeException();
        }
    }

    static final class Mock0CtorNode extends WrapperIRI {
        private Mock0CtorNode() {
            super(null, null);
        }
    }
}
