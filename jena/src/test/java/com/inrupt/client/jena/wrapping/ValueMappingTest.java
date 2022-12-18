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

import static com.inrupt.client.jena.wrapping.ValueMappings.*;
import static java.util.UUID.randomUUID;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;
import static org.apache.jena.rdf.model.ResourceFactory.createStringLiteral;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.time.Instant;
import java.time.format.DateTimeParseException;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.enhanced.Implementation;
import org.apache.jena.enhanced.UnsupportedPolymorphismException;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.LiteralRequiredException;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.ResourceRequiredException;
import org.apache.jena.rdf.model.impl.ModelCom;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.sparql.graph.GraphFactory;
import org.junit.jupiter.api.Test;

class ValueMappingTest {
    @Test
    void literalAsStringTest() {
        final var blank = createResource();
        final var literal = createStringLiteral(randomUUID().toString());

        assertThrows(NullPointerException.class, () -> literalAsString(null));
        assertThrows(LiteralRequiredException.class, () -> literalAsString(blank));

        assertThat(literalAsString(literal), is(literal.getLexicalForm()));
    }

    @Test
    void iriAsStringTest() {
        final var blank = createResource();
        final var iri = createResource(randomUUID().toString());

        assertThrows(NullPointerException.class, () -> iriAsString(null));
        assertThrows(ResourceRequiredException.class, () -> iriAsString(blank));

        assertThat(iriAsString(iri), is(iri.getURI()));
    }

    @Test
    void iriAsUriTest() {
        final var blank = createResource();
        final var iri = createResource(randomUUID().toString());

        assertThrows(NullPointerException.class, () -> iriAsUri(null));
        assertThrows(ResourceRequiredException.class, () -> iriAsUri(blank));

        assertThat(iriAsUri(iri), is(URI.create(iri.getURI())));
    }

    @Test
    void literalAsInstantTest() {
        final var blank = createResource();
        final var malformed = ResourceFactory.createStringLiteral(randomUUID().toString());
        final var literal = createStringLiteral(Instant.now().toString());

        assertThrows(NullPointerException.class, () -> literalAsInstant(null));
        assertThrows(LiteralRequiredException.class, () -> literalAsInstant(blank));
        assertThrows(DateTimeParseException.class, () -> literalAsInstant(malformed));

        assertThat(literalAsInstant(literal), is(Instant.parse(literal.getLexicalForm())));
    }

    @Test
    void asTest() {
        final var model = new WrapperModel();
        final var blank = model.createResource();
        final var literal = createStringLiteral(randomUUID().toString());
        final var asWrapperNode = as(WrapperModel.WrapperNode.class);

        assertThrows(NullPointerException.class, () -> as(null));

        assertThrows(NullPointerException.class, () -> asWrapperNode.toValue(null));
        assertThrows(UnsupportedPolymorphismException.class, () -> asWrapperNode.toValue(literal));
        assertDoesNotThrow(() -> asWrapperNode.toValue(blank));
    }

    @Test
    void literalAsIntegerOrNullTest() {
        final var blank = createResource();
        final var malformed = ResourceFactory.createStringLiteral(randomUUID().toString());
        final var literal = createStringLiteral(String.valueOf(Integer.MAX_VALUE));

        assertThrows(NullPointerException.class, () -> literalAsIntegerOrNull(null));
        assertThrows(LiteralRequiredException.class, () -> literalAsIntegerOrNull(blank));

        assertThat(literalAsIntegerOrNull(malformed), is(nullValue()));
        assertThat(literalAsIntegerOrNull(literal), is(Integer.parseInt(literal.getLexicalForm())));
    }

    static class WrapperModel extends ModelCom {
        WrapperModel() {
            super(GraphFactory.createDefaultGraph());

            getPersonality().add(WrapperNode.class, WrapperNode.factory);
        }

        static final class WrapperNode extends ResourceImpl {
            static final Implementation factory = new UriOrBlankFactory(WrapperNode::new);

            private WrapperNode(final Node node, final EnhGraph enhGraph) {
                super(node, enhGraph);
            }
        }
    }
}
