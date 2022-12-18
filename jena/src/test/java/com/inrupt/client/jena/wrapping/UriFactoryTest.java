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

import static java.util.UUID.randomUUID;
import static org.apache.jena.graph.NodeFactory.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.enhanced.EnhNode;
import org.apache.jena.rdf.model.ResourceRequiredException;
import org.junit.jupiter.api.Test;

class UriFactoryTest {
    @Test
    void constructorTest() {
        assertThrows(NullPointerException.class, () -> new UriOrBlankFactory(null));
    }

    @Test
    void wrapTest() {
        final var implementation = new UriFactory(EnhNode::new);
        final var iri = createURI(randomUUID().toString());
        final var blank = createBlankNode();
        final var literal = createLiteral(randomUUID().toString());
        final var graph = new EnhGraph(null, null);

        assertThrows(NullPointerException.class, () -> implementation.wrap(null, null));
        assertThrows(NullPointerException.class, () -> implementation.wrap(blank, null));
        assertThrows(ResourceRequiredException.class, () -> implementation.wrap(literal, graph));
        assertThrows(ResourceRequiredException.class, () -> implementation.wrap(blank, graph));

        assertThat(implementation.wrap(iri, graph).asNode(), is(iri));
    }

    @Test
    void canWrapTest() {
        final var implementation = new UriFactory(EnhNode::new);
        final var blank = createBlankNode();
        final var iri = createURI(randomUUID().toString());
        final var literal = createLiteral(randomUUID().toString());
        final var graph = new EnhGraph(null, null);

        assertThrows(NullPointerException.class, () -> implementation.canWrap(null, null));
        assertThrows(NullPointerException.class, () -> implementation.canWrap(blank, null));

        assertThat(implementation.canWrap(literal, graph), is(false));
        assertThat(implementation.canWrap(blank, graph), is(false));
        assertThat(implementation.canWrap(iri, graph), is(true));
    }
}
