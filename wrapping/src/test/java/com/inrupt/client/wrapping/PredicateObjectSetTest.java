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

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.inrupt.client.spi.RDFFactory;

import java.util.*;
import java.util.stream.Stream;

import org.apache.commons.rdf.api.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@SuppressWarnings({
    "ResultOfMethodCallIgnored",
    "MismatchedQueryAndUpdateOfCollection",
    "SuspiciousMethodCalls",
    "SuspiciousToArrayCall",
    "RedundantCollectionOperation",
    "DataFlowIssue"
})
class PredicateObjectSetTest {
    // region constants
    private static final RDF FACTORY = RDFFactory.getInstance();
    private static final BlankNode S = FACTORY.createBlankNode();
    private static final BlankNode SX = FACTORY.createBlankNode();
    private static final IRI P = FACTORY.createIRI("urn:" + randomUUID());
    private static final IRI PX = FACTORY.createIRI("urn:" + randomUUID());
    private static final String V = randomUUID().toString();
    private static final Literal O = FACTORY.createLiteral(V);
    private static final Literal OX = FACTORY.createLiteral(randomUUID().toString());
    private static final NodeMapping<String> N2V = NodeMappings::asStringLiteral;
    private static final ValueMapping<String> V2N = ValueMappings::literalAsString;
    private Graph g;
    private Set<String> set;

    // endregion

    @BeforeEach
    void setUp() {
        g = FACTORY.createGraph();
        set = new PredicateObjectSet<>(S, P, g, N2V, V2N);
    }

    // region constructor

    @DisplayName("constructor throws")
    @ParameterizedTest(name = "{0} when {1} is null")
    @MethodSource
    <T> void constructorRequiresArguments(
            final Class<Throwable> expectedException,
            final String ignoredName,
            final BlankNodeOrIRI subject,
            final IRI predicate,
            final Graph graph,
            final NodeMapping<T> nodeMapping,
            final ValueMapping<T> valueMapping) {

        assertThrows(expectedException, () ->
                new PredicateObjectSet<>(subject, predicate, graph, nodeMapping, valueMapping));
    }

    static Stream<Arguments> constructorRequiresArguments() {
        final Class<NullPointerException> npe = NullPointerException.class;
        final Graph g = FACTORY.createGraph();

        return Stream.of(
                Arguments.of(npe, "subject", null, null, null, null, null),
                Arguments.of(npe, "predicate", SX, null, null, null, null),
                Arguments.of(npe, "graph", SX, P, null, null, null),
                Arguments.of(npe, "node factory", SX, P, g, null, null),
                Arguments.of(npe, "value factory", SX, P, g, N2V, null)
        );
    }

    // endregion
    // region size

    /**
     * This untestable invariant of {@link Set} happens to be guaranteed by implementation.
     */
    @DisplayName("Set invariant: size capped at greatest integer")
    @Test
    void sizeCappedAtIntegerMaxValue() {
        // Not feasible to exhaust possibility space as of writing.
        // Not feasible to mock either, because iterator is enumerated.
        for (long i = 0L; i < 1 /* 0x7fffffffL + 2 */; i++) {
            // Subject and predicate both match
            g.add(S, P, FACTORY.createLiteral(String.valueOf(i)));
        }

        assertThat(set, hasSize(lessThanOrEqualTo(Integer.MAX_VALUE)));
    }

    @DisplayName("size ignores other subjects")
    @Test
    void sizeIgnoresOtherSubjects() {
        g.add(SX, P, O);

        assertThat(set, hasSize(0));
    }

    @DisplayName("size ignores other predicates")
    @Test
    void sizeIgnoresOtherPredicates() {
        g.add(S, PX, O);

        assertThat(set, hasSize(0));
    }

    @DisplayName("size counts statements by subject and predicate")
    @Test
    void sizeCountsBySubjectAndPredicate() {
        g.add(S, P, O);

        assertThat(set, hasSize(1));
    }

    // endregion
    // region isEmpty

    @DisplayName("isEmpty ignores other subjects")
    @Test
    void isEmptyIgnoresOtherSubjects() {
        g.add(SX, P, O);

        assertThat(set, is(empty()));
    }

    @DisplayName("isEmpty ignores other predicates")
    @Test
    void isEmptyIgnoresOtherPredicates() {
        g.add(S, PX, O);

        assertThat(set, is(empty()));
    }

    @DisplayName("isEmpty is false if statements exist by subject and predicate")
    @Test
    void isEmptyFalseWithObject() {
        g.add(S, P, O);

        assertThat(set, is(not(empty())));
    }

    @DisplayName("isEmpty is true if no statements exist by subject and predicate")
    @Test
    void isEmptyTrueWithoutObject() {
        assertThat(set, is(empty()));
    }

    // endregion
    // region contains

    @DisplayName("Set invariant: contains throws if object is null")
    @Test
    void containsRequiresObject() {
        assertThrows(NullPointerException.class, () ->
                set.contains(null));
    }

    @DisplayName("Set invariant: contains throws if object cannot be cast")
    @Test
    void containsRequiresCastableObject() {
        assertThrows(ClassCastException.class, () ->
                set.contains(0));
    }

    @DisplayName("contains ignores other subject")
    @Test
    void containsIgnoresOtherSubject() {
        g.add(SX, P, O);

        assertThat(set.contains(V), is(false));
    }

    @DisplayName("contains ignores other predicate")
    @Test
    void containsIgnoresOtherPredicate() {
        g.add(S, PX, O);

        assertThat(set.contains(V), is(false));
    }

    @DisplayName("contains is false if no statement exists by subject, predicate and converted object")
    @Test
    void containsFalseWithOtherObject() {
        g.add(S, P, OX);

        assertThat(set.contains(V), is(false));
    }

    @DisplayName("contains is true if statement exists by subject, predicate and converted object")
    @Test
    void containsConsidersObjectsOfPredicateForSubject() {
        g.add(S, P, O);

        assertThat(set.contains(V), is(true));
    }

    // endregion
    // region iterator

    @DisplayName("iterator remove is not supported")
    @Test
    void iteratorRemoveIsUnsupported() {
        final Iterator<String> iterator = set.iterator();

        assertThrows(UnsupportedOperationException.class, iterator::remove);
    }

    @DisplayName("iterator ignores other subjects")
    @Test
    void iteratorIgnoresOtherSubjects() {
        g.add(SX, P, O);

        assertThat(set, is(emptyIterableOf(String.class)));
    }

    @DisplayName("iterator ignores other predicates")
    @Test
    void iteratorIgnoresOtherPredicates() {
        g.add(S, PX, O);

        assertThat(set, is(emptyIterableOf(String.class)));
    }

    @DisplayName("iterator enumerates converted objects by subject and predicate")
    @Test
    void iteratorConsidersConvertedObjectsOfPredicateForObject() {
        g.add(S, P, O);

        assertThat(set, containsInAnyOrder(V));
    }

    // endregion
    // region toArray

    @DisplayName("toArray ignores other subjects")
    @Test
    void toArrayIgnoresOtherSubjects() {
        g.add(SX, P, O);

        assertThat(set.toArray(), is(emptyArray()));
    }

    @DisplayName("toArray ignores other predicates")
    @Test
    void toArrayIgnoresOtherPredicates() {
        g.add(S, PX, O);

        assertThat(set.toArray(), is(emptyArray()));
    }

    @DisplayName("toArray enumerates converted objects by subject and predicate")
    @Test
    void toArrayConsidersConvertedObjectsOfPredicateForObject() {
        g.add(S, P, O);

        assertThat(set.toArray(), is(arrayContainingInAnyOrder(V)));
    }

    // endregion
    // region toArrayOther

    @DisplayName("Set invariant: toArray (other) throws if array is null")
    @Test
    void toArrayOtherRequiresArray() {
        assertThrows(NullPointerException.class, () ->
                set.toArray((String[]) null));
    }

    @DisplayName("Set invariant: toArray (other) throws if array is other type")
    @Test
    void toArrayOtherRequiresMatchingArrayType() {
        g.add(S, P, O);

        assertThrows(ArrayStoreException.class, () ->
                set.toArray(new UUID[0]));
    }

    @DisplayName("Set invariant: toArray (other) reuses array if it fits")
    @Test
    void toArrayOtherReusesArray() {
        final String[] spacious = new String[1];

        g.add(S, P, O);

        assertThat(set.toArray(spacious), is(theInstance(spacious)));
    }

    @DisplayName("Set invariant: toArray (other) allocates new array if it does not fit")
    @Test
    void toArrayOtherAllocatesNewArray() {
        final String[] crowded = new String[0];

        g.add(S, P, O);

        assertThat(set.toArray(crowded), is(not(theInstance(crowded))));
    }

    @DisplayName("toArray (other) ignores other subjects")
    @Test
    void toArrayOtherIgnoresOtherSubjects() {
        g.add(SX, P, O);

        assertThat(set.toArray(new String[0]), is(emptyArray()));
    }

    @DisplayName("toArray (other) ignores other predicates")
    @Test
    void toArrayOtherIgnoresOtherPredicates() {
        g.add(S, PX, O);

        assertThat(set.toArray(new String[0]), is(emptyArray()));
    }

    @DisplayName("toArray (other) populates converted objects by subject and predicate")
    @Test
    void toArrayOtherConsidersConvertedObjectsOfPredicateForObject() {
        g.add(S, P, O);

        assertThat(set.toArray(new String[0]), is(arrayContainingInAnyOrder(V)));
    }

    // endregion
    // region add

    @DisplayName("Set invariant: add throws if element is null")
    @Test
    void addRequiresElement() {
        assertThrows(NullPointerException.class, () ->
                set.add(null));
    }

    @DisplayName("Set invariant: add is false if set was not modified")
    @Test
    void addFalseWhenPresent() {
        g.add(S, P, O);

        assertThat(set.add(V), is(false));
    }

    @DisplayName("Set invariant: add is true if set was modified")
    @Test
    void addTrueWhenNew() {
        assertThat(set.add(V), is(true));
    }

    @DisplayName("add asserts statement by subject, predicate and converted element object")
    @Test
    void addAssertsConverted() {
        set.add(V);

        assertThat(g.contains(S, P, O), is(true));
    }


    // endregion
    // region remove

    @DisplayName("Set invariant: remove throws if element is null")
    @Test
    void removeRequiresElement() {
        assertThrows(NullPointerException.class, () ->
                set.remove(null));
    }

    @DisplayName("Set invariant: remove is false if set was not modified")
    @Test
    void removeFalseWhenPresent() {
        assertThat(set.remove(V), is(false));
    }

    @DisplayName("Set invariant: remove is true if set was modified")
    @Test
    void removeTrueWhenNew() {
        g.add(S, P, O);

        assertThat(set.remove(V), is(true));
    }

    @DisplayName("remove retracts statement by subject, predicate and converted object")
    @Test
    void removeRetractsConverted() {
        g.add(S, P, O);

        set.remove(V);

        assertThat(g.contains(S, P, O), is(false));
    }


    // endregion
    // region containsAll

    @DisplayName("Set invariant: contains all throws if collection is null")
    @Test
    void containsAllRequiresCollection() {
        assertThrows(NullPointerException.class, () ->
                set.remove(null));
    }

    @DisplayName("Set invariant: contains all throws if collection has null element")
    @Test
    void containsAllRequiresNonNullElements() {
        final Collection<?> c = new ArrayList<>();
        c.add(null);

        assertThrows(NullPointerException.class, () ->
                set.containsAll(c));
    }

    @DisplayName("contains all ignores other subject")
    @Test
    void containsAllIgnoresOtherSubject() {
        g.add(SX, P, O);

        assertThat(set.containsAll(singletonList(V)), is(false));
    }

    @DisplayName("contains all ignores other predicate")
    @Test
    void containsAllIgnoresOtherPredicate() {
        g.add(S, PX, O);

        assertThat(set.containsAll(singletonList(V)), is(false));
    }

    @DisplayName("contains all is false if no statements exist by subject, predicate and any converted object")
    @Test
    void containsAllFalseForMissing() {
        g.add(S, PX, OX);

        assertThat(set.containsAll(singletonList(V)), is(false));
    }

    @DisplayName("contains all is true if statements exist by subject, predicate and any converted object")
    @Test
    void containsAllTrueForExisting() {
        g.add(S, P, O);

        assertThat(set.containsAll(singletonList(V)), is(true));
    }

    // endregion
    // region addAll

    @DisplayName("Set invariant: add all throws if collection is null")
    @Test
    void addAllRequiresCollection() {
        assertThrows(NullPointerException.class, () ->
                set.addAll(null));
    }

    @DisplayName("Set invariant: add all is false if set was not modified")
    @Test
    void addAllFalseWhenPresent() {
        g.add(S, P, O);

        assertThat(set.addAll(singletonList(V)), is(false));
    }

    @DisplayName("Set invariant: add all is true if set was modified")
    @Test
    void addAllTrueWhenNew() {
        assertThat(set.addAll(singletonList(V)), is(true));
    }

    @DisplayName("add all asserts statements by subject, predicate and converted element objects")
    @Test
    void addAllAssertsConverted() {
        set.addAll(singletonList(V));

        assertThat(g.contains(S, P, O), is(true));
    }


    // endregion
    // region retainAll

    @DisplayName("Set invariant: retain all throws if collection is null")
    @Test
    void retainAllRequiresCollection() {
        assertThrows(NullPointerException.class, () ->
                set.retainAll(null));
    }

    @DisplayName("Set invariant: retain all does not throw if collection element is not castable")
    @Test
    void retainAllAllowsNonCastableCollection() {
        g.add(S, P, O);

        assertDoesNotThrow(() ->
                set.retainAll(singletonList(randomUUID())));
    }

    @DisplayName("Set invariant: retain all is false if set was not modified")
    @Test
    void retainAllLeavesElementsOfCollection1() {
        assertThat(set.retainAll(singletonList(V)), is(false));
    }

    @DisplayName("Set invariant: retain all is true if set was modified")
    @Test
    void retainAllRetractsWhenObjectMissing1() {
        g.add(S, P, OX);

        assertThat(set.retainAll(singletonList(V)), is(true));
    }

    @DisplayName("retain all does not retract if converted object is in collection")
    @Test
    void retainAllLeavesElementsOfCollection() {
        g.add(S, P, O);

        set.retainAll(singletonList(V));

        assertThat(g.contains(S, P, O), is(true));
    }

    @DisplayName("retain all retracts by subject and predicate if converted object is not in collection")
    @Test
    void retainAllRetractsWhenObjectMissing() {
        g.add(S, P, OX);

        set.retainAll(singletonList(V));

        assertThat(g.contains(S, P, O), is(false));
    }

    // endregion
    // region removeAll

    @DisplayName("Set invariant: remove all throws if collection is null")
    @Test
    void removeAllRequiresCollection() {
        assertThrows(NullPointerException.class, () ->
                set.removeAll(null));
    }

    @DisplayName("Set invariant: remove all throws if collection element is not castable")
    @Test
    void removeAllRequiresCastableCollection() {
        final Collection<?> c = singletonList(randomUUID());

        g.add(S, P, O);

        assertThrows(ClassCastException.class, () ->
                set.removeAll(c));
    }

    @DisplayName("Set invariant: remove all is false if set was not modified")
    @Test
    void removeAllFalseIfUnchanged() {
        assertThat(set.removeAll(singletonList(V)), is(false));
    }

    @DisplayName("Set invariant: remove all is true if set was modified")
    @Test
    void removeAllTruIfChanged() {
        g.add(S, P, O);

        assertThat(set.removeAll(singletonList(V)), is(true));
    }

    @DisplayName("remove all ignores other subject")
    @Test
    void removeAllIgnoresOtherSubject() {
        g.add(SX, P, O);

        assertThat(set.removeAll(singletonList(V)), is(false));
    }

    @DisplayName("remove all ignores other predicate")
    @Test
    void removeAllIgnoresOtherPredicate() {
        g.add(S, PX, O);

        assertThat(set.removeAll(singletonList(V)), is(false));
    }

    @DisplayName("remove all ignores other object")
    @Test
    void removeAllIgnoresOtherObject() {
        g.add(S, P, OX);

        assertThat(set.removeAll(singletonList(V)), is(false));
    }

    @DisplayName("remove all retracts by subject, predicate and any converted object")
    @Test
    void removeAllRetracts() {
        g.add(S, P, O);

        set.removeAll(singletonList(V));

        assertThat(g.contains(S, P, O), is(false));
    }

    // endregion
    // region clear

    @DisplayName("clear retracts all statements by subject and predicate")
    @Test
    void clearRetractsConverted() {
        g.add(S, P, O);

        set.clear();

        assertThat(g.contains(S, P, O), is(false));
    }

    // endregion
    // region equals

    @DisplayName("equals is false for different subjects")
    @Test
    void equalsFalseForDifferentSubjects() {
        final Set<String> other = new PredicateObjectSet<>(SX, P, g, N2V, V2N);

        g.add(S, P, O);

        assertThat(set, is(not(equalTo(other))));
    }

    @DisplayName("equals is false for different predicates")
    @Test
    void equalsFalseForDifferentPredicates() {
        final Set<String> other = new PredicateObjectSet<>(S, PX, g, N2V, V2N);

        g.add(S, P, O);

        assertThat(set, is(not(equalTo(other))));
    }

    @DisplayName("equals is false for different objects")
    @Test
    void equalsFalseForDifferentObjects() {
        final Set<String> other = new PredicateObjectSet<>(SX, PX, g, N2V, V2N);

        g.add(S, P, O);
        g.add(SX, PX, OX);

        assertThat(set, is(not(equalTo(other))));
    }

    @DisplayName("equals is true for empty sets")
    @Test
    void equalsTrueForEmpty() {
        final Set<String> other = new PredicateObjectSet<>(SX, PX, g, N2V, V2N);

        assertThat(set, is(equalTo(other)));
    }

    @DisplayName("equals is true for sets with equal predicate and object")
    @Test
    void equalsTrueForEqualPredicateAndObject() {
        final Set<String> other = new PredicateObjectSet<>(S, P, g, N2V, V2N);

        g.add(S, P, O);

        assertThat(set, is(equalTo(other)));
    }

    // endregion
    // region hashCode

    @DisplayName("hashCode differs for different subjects")
    @Test
    void hashCodeDiffersForDifferentSubjects() {
        final Set<String> other = new PredicateObjectSet<>(SX, P, g, N2V, V2N);

        g.add(S, P, O);

        assertThat(set.hashCode(), is(not(equalTo(other.hashCode()))));
    }

    @DisplayName("hashCode differs for different predicates")
    @Test
    void hashCodeDiffersForDifferentPredicates() {
        final Set<String> other = new PredicateObjectSet<>(S, PX, g, N2V, V2N);

        g.add(S, P, O);

        assertThat(set.hashCode(), is(not(equalTo(other.hashCode()))));
    }

    @DisplayName("hashCode differs for different objects")
    @Test
    void hashCodeDiffersForDifferentObjects() {
        final Set<String> other = new PredicateObjectSet<>(SX, PX, g, N2V, V2N);

        g.add(S, P, O);
        g.add(SX, PX, OX);

        assertThat(set.hashCode(), is(not(equalTo(other.hashCode()))));
    }

    @DisplayName("hashCode matches for empty sets")
    @Test
    void hashCodeSameForEmpty() {
        final Set<String> other = new PredicateObjectSet<>(SX, PX, g, N2V, V2N);

        assertThat(set.hashCode(), is(equalTo(other.hashCode())));
    }

    @DisplayName("hashCode matches for sets with equal predicate and object")
    @Test
    void hashCodeSameForEqualPredicateAndObject() {
        final Set<String> other = new PredicateObjectSet<>(S, P, g, N2V, V2N);

        g.add(S, P, O);

        assertThat(set.hashCode(), is(equalTo(other.hashCode())));
    }

    // endregion
}
