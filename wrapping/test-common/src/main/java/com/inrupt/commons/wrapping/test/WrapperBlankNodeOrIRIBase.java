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

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.inrupt.commons.wrapping.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.rdf.api.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@SuppressWarnings("checkstyle:MultipleStringLiterals")
public class WrapperBlankNodeOrIRIBase {
    private static final RDF FACTORY = RDFFactory.getInstance();
    private static final IRI P = FACTORY.createIRI("http://example.com/p");
    private static final String V1 = randomUUID().toString();
    private static final Literal O1 = FACTORY.createLiteral(V1);
    private static final String V2 = randomUUID().toString();
    private static final Literal O2 = FACTORY.createLiteral(V2);
    private static final String OBJECT = "object";
    private static final String LEXICAL_FORM = "lexicalForm";

    private Graph g;
    private MockNode s;

    @BeforeEach
    void setUp() {
        g = FACTORY.createGraph();
        s = new MockNode(FACTORY.createIRI("urn:uuid:" + randomUUID()), g);
    }

    @DisplayName("anyOrNull is null if no statement matches subject & predicate")
    @Test
    void anyOrNullIsNullIfZero() {
        assertThat(s.anyOrNull(), is(nullValue()));
    }

    @DisplayName("anyOrNull is converted object of random statement matching subject & predicate")
    @Test
    void anyOrNullIsAnyIfMany() {
        g.add(s, P, O1);
        g.add(s, P, O2);

        assertThat(s.anyOrNull(), is(oneOf(V1, V2)));
    }

    @DisplayName("anyOrThrow throws if no statement matches subject & predicate")
    @Test
    void anyOrThrowThrowsIfZero() {
        assertThrows(IllegalStateException.class, s::anyOrThrow);
    }

    @DisplayName("anyOrThrow is converted object of random statement matching subject & predicate")
    @Test
    void anyOrThrowIsAnyIfMany() {
        g.add(s, P, O1);
        g.add(s, P, O2);

        assertThat(s.anyOrThrow(), is(oneOf(V1, V2)));
    }

    @DisplayName("singleOrNull is null if no statement matches subject & predicate")
    @Test
    void singleOrNullIsNullIfZero() {
        assertThat(s.singleOrNull(), is(nullValue()));
    }

    @DisplayName("singleOrNull is converted object of only statement matching subject & predicate")
    @Test
    void singleOrNullIsOneIfOne() {
        g.add(s, P, O1);

        assertThat(s.singleOrNull(), is(V1));
    }

    @DisplayName("anyOrThrow throws if multiple statements match subject & predicate")
    @Test
    void singleOrNullThrowsIfMany() {
        g.add(s, P, O1);
        g.add(s, P, O2);

        assertThrows(IllegalStateException.class, s::singleOrNull);
    }

    @DisplayName("singleOrThrow throws if no statements match subject & predicate")
    @Test
    void singleOrThrowThrowsIfZero() {
        assertThrows(IllegalStateException.class, s::singleOrThrow);
    }

    @DisplayName("singleOrThrow is converted object of only statement matching subject & predicate")
    @Test
    void singleOrThrowIsOneIfOne() {
        g.add(s, P, O1);

        assertThat(s.singleOrThrow(), is(V1));
    }

    @DisplayName("singleOrThrow throws if multiple statements match subject & predicate")
    @Test
    void singleOrThrowThrowsIfMany() {
        g.add(s, P, O1);
        g.add(s, P, O2);

        assertThrows(IllegalStateException.class, s::singleOrThrow);
    }

    @DisplayName("iterator is empty if no statements match subject & predicate")
    @Test
    void iteratorEmptyIfZero() {
        assertThat(s.iterator().hasNext(), is(false));
    }

    @DisplayName("iterator contains converted objects of statements matching subject & predicate")
    @Test
    void iteratorContainsIfNonZero() {
        g.add(s, P, O1);
        g.add(s, P, O2);

        assertThat(() -> s.iterator(), containsInAnyOrder(V1, V2));
    }

    @DisplayName("iterator does not implement remove")
    @Test
    void iteratorRemoveThrows() {
        g.add(s, P, O1);

        assertThrows(UnsupportedOperationException.class, s.iterator()::remove);
    }

    @DisplayName("snapshot is empty if no statements match subject & predicate")
    @Test
    void snapshotEmptyIfZero() {
        assertThat(s.snapshot(), is(empty()));
    }

    @DisplayName("snapshot contains converted objects of statements matching subject & predicate")
    @Test
    void snapshotContainsIfNonZero() {
        g.add(s, P, O1);
        g.add(s, P, O2);

        assertThat(s.snapshot(), containsInAnyOrder(V1, V2));
    }

    @DisplayName("snapshot does not reflect subsequent changes to underlying graph")
    @Test
    void snapshotIsStatic() {
        g.add(s, P, O1);

        final Set<String> snapshot = s.snapshot();

        g.add(s, P, O2);

        assertThat(snapshot, not(containsInAnyOrder(V1, V2)));
    }

    @DisplayName("live is empty if no statements match subject & predicate")
    @Test
    void liveEmptyIfZero() {
        assertThat(s.live(), is(empty()));
    }

    @DisplayName("live contains converted objects of statements matching subject & predicate")
    @Test
    void liveContainsIfNonZero() {
        g.add(s, P, O1);
        g.add(s, P, O2);

        assertThat(s.live(), containsInAnyOrder(V1, V2));
    }

    @DisplayName("live reflects subsequent changes to underlying graph")
    @Test
    void liveIsDynamic() {
        g.add(s, P, O1);

        final Set<String> live = s.live();

        g.add(s, P, O2);

        assertThat(live, containsInAnyOrder(V1, V2));
    }

    @DisplayName("overwrite (1) throws if value is null")
    @Test
    void overwriteOneThrowsIfNull() {
        assertThrows(NullPointerException.class, () -> s.overwrite((String) null));
    }

    @DisplayName("overwrite (1) removes statements matching subject & predicate and adds new with converted object")
    @Test
    void overwriteOneRemovesAndAdds() {
        g.add(s, P, O1);

        s.overwrite(V2);

        assertThat(
                (Iterable<Triple>) () -> g.iterate(s, P, null).iterator(),
                contains(hasProperty(OBJECT, hasProperty(LEXICAL_FORM, is(V2)))));
    }

    @DisplayName("overwrite (*) throws if value is null")
    @Test
    void overwriteManyThrowsIfNull() {
        assertThrows(NullPointerException.class, () -> s.overwrite((Iterable<String>) null));
    }

    @DisplayName("overwrite (*) throws if value has null elements")
    @Test
    void overwriteManyThrowsIfNullElement() {
        final ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add(null);

        assertThrows(NullPointerException.class, () -> s.overwrite(arrayList));
    }

    @DisplayName("overwrite (*) removes statements matching subject & predicate and adds new with converted objects")
    @Test
    void overwriteManyRemovesAndAdds() {
        g.add(s, P, O1);

        final List<String> value = new ArrayList<>();
        value.add(V2);
        s.overwrite(value);

        assertThat(
                (Iterable<Triple>) () -> g.iterate(s, P, null).iterator(),
                contains(hasProperty(OBJECT, hasProperty(LEXICAL_FORM, is(V2)))));
    }

    @DisplayName("overwriteNullable (1) removes when value is null")
    @Test
    void overwriteOneNullableRemovesIfNull() {
        g.add(s, P, O1);

        s.overwriteNullable((String) null);

        assertThat(g.iterate(s, P, null), emptyIterable());
    }

    @DisplayName("overwrite (1) removes statements matching subject & predicate and adds new with converted object")
    @Test
    void overwriteOneNullableRemovesAndAdds() {
        g.add(s, P, O1);

        s.overwriteNullable(V2);

        assertThat(
                (Iterable<Triple>) () -> g.iterate(s, P, null).iterator(),
                contains(hasProperty(OBJECT, hasProperty(LEXICAL_FORM, is(V2)))));
    }

    @DisplayName("overwriteNullable (*) removes when value is null")
    @Test
    void overwriteManyNullableRemovesIfNull() {
        g.add(s, P, O1);

        s.overwriteNullable((Iterable<String>) null);

        assertThat(g.iterate(s, P, null), emptyIterable());
    }

    @DisplayName("overwriteNullable (*) throws if value has null elements")
    @Test
    void overwriteManyNullableThrowsIfNullElement() {
        final ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add(null);

        assertThrows(NullPointerException.class, () -> s.overwriteNullable(arrayList));
    }

    @DisplayName("overwrite (*) removes statements matching subject & predicate and adds new with converted objects")
    @Test
    void overwriteManyNullableRemovesAndAdds() {
        g.add(s, P, O1);

        final List<String> value = new ArrayList<>();
        value.add(V2);
        s.overwriteNullable(value);

        assertThat(
                (Iterable<Triple>) () -> g.iterate(s, P, null).iterator(),
                contains(hasProperty(OBJECT, hasProperty(LEXICAL_FORM, is(V2)))));
    }

    @DisplayName("add (1) throws if value is null")
    @Test
    void addOneThrowsIfNull() {
        assertThrows(NullPointerException.class, () -> s.add((String) null));
    }

    @DisplayName("add (1) adds statement with converted object, subject & predicate")
    @Test
    void addOneAdds() {
        g.add(s, P, O1);

        s.add(V2);

        assertThat(
                (Iterable<Triple>) () -> g.iterate(s, P, null).iterator(),
                containsInAnyOrder(
                        hasProperty(OBJECT, hasProperty(LEXICAL_FORM, is(V1))),
                        hasProperty(OBJECT, hasProperty(LEXICAL_FORM, is(V2)))));
    }

    @DisplayName("add (*) throws if value is null")
    @Test
    void addManyThrowsIfNull() {
        assertThrows(NullPointerException.class, () -> s.add((Iterable<String>) null));
    }

    @DisplayName("add (*) throws if value has null elements")
    @Test
    void addManyThrowsIfNullElement() {
        final ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add(null);

        assertThrows(NullPointerException.class, () -> s.add(arrayList));
    }

    @DisplayName("add (1) adds statements with converted objects, subject & predicate")
    @Test
    void addManyAdds() {
        g.add(s, P, O1);

        final List<String> value = new ArrayList<>();
        value.add(V2);
        s.add(value);

        assertThat(
                (Iterable<Triple>) () -> g.iterate(s, P, null).iterator(),
                containsInAnyOrder(
                        hasProperty(OBJECT, hasProperty(LEXICAL_FORM, is(V1))),
                        hasProperty(OBJECT, hasProperty(LEXICAL_FORM, is(V2)))));
    }

    public static class MockNode extends WrapperIRI {
        private static final ValueMapping<String> VM = ValueMappings::literalAsString;
        private static final TermMapping<String> NM = TermMappings::asStringLiteral;

        MockNode(final RDFTerm n, final Graph m) {
            super(n, m);
        }

        String anyOrNull() {
            return anyOrNull(P, VM);
        }

        String anyOrThrow() {
            return anyOrThrow(P, VM);
        }

        String singleOrNull() {
            return singleOrNull(P, VM);
        }

        String singleOrThrow() {
            return singleOrThrow(P, VM);
        }

        Iterator<String> iterator() {
            return iterator(P, VM);
        }

        Set<String> snapshot() {
            return snapshot(P, VM);
        }

        Set<String> live() {
            return live(P, NM, VM);
        }

        void overwrite(final String value) {
            overwrite(P, value, NM);
        }

        void overwrite(final Iterable<String> value) {
            overwrite(P, value, NM);
        }

        void overwriteNullable(final String value) {
            overwriteNullable(P, value, NM);
        }

        void overwriteNullable(final Iterable<String> value) {
            overwriteNullable(P, value, NM);
        }

        void add(final String value) {
            add(P, value, NM);
        }

        void add(final Iterable<String> value) {
            add(P, value, NM);
        }
    }
}
