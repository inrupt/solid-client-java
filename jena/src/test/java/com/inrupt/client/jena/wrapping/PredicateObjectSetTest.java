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
import static org.apache.jena.rdf.model.ModelFactory.createDefaultModel;
import static org.apache.jena.rdf.model.ResourceFactory.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import org.apache.jena.ext.com.google.common.collect.Iterators;
import org.apache.jena.ext.com.google.common.primitives.Ints;
import org.apache.jena.rdf.model.HasNoModelException;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@SuppressWarnings({
    // Set methods frequently called to test implementation, ignoring results.
    "ResultOfMethodCallIgnored",

    // Class under test is a dynamic collection. Parity achieved by manipulating underlying graph.
    "MismatchedQueryAndUpdateOfCollection",

    // Set methods intentionally called with bad params to test invariants.
    "SuspiciousMethodCalls",
    "SuspiciousToArrayCall",
    "RedundantCollectionOperation"
})
class PredicateObjectSetTest {
    // region constants
    private static final Resource SX = createResource();
    private static final Property P = createProperty(randomUUID().toString());
    private static final Property PX = createProperty(randomUUID().toString());
    private static final String O = "o";
    private static final String OX = randomUUID().toString();
    private static final NodeMapping<String> N2V = NodeMappings::asStringLiteral;
    private static final ValueMapping<String> V2N = ValueMappings::literalAsString;

    // endregion

    // region constructor

    @DisplayName("constructor throws")
    @ParameterizedTest(name = "{0} when {1} is null")
    @MethodSource
    <T> void constructorRequiresArguments(
            final Class<Throwable> expectedException,
            final String ignoredName,
            final Resource subject,
            final Property predicate,
            final NodeMapping<T> nodeMapping,
            final ValueMapping<T> valueMapping) {

        assertThrows(expectedException, () ->
                new PredicateObjectSet<>(subject, predicate, nodeMapping, valueMapping));
    }

    static Stream<Arguments> constructorRequiresArguments() {
        final var npe = NullPointerException.class;
        final var noModel = HasNoModelException.class;

        return Stream.of(
                Arguments.of(npe, "subject", null, null, null, null),
                Arguments.of(npe, "predicate", SX, null, null, null),
                Arguments.of(npe, "node factory", SX, P, null, null),
                Arguments.of(npe, "value factory", SX, P, N2V, null),
                Arguments.of(noModel, "model of subject", SX, P, N2V, V2N)
        );
    }

    // endregion
    // region size

    /**
     * This untestable invariant of {@link Set} happens to be guaranteed by implementation:
     * {@link PredicateObjectSet#size} -> {@link Iterators#size} -> {@link Ints#saturatedCast(long)}.
     */
    @DisplayName("Set invariant: size capped at greatest integer")
    @Test
    void sizeCappedAtIntegerMaxValue() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);

        // Not feasible to exhaust possibility space as of writing.
        // Not feasible to mock either, because iterator is enumerated.
        for (var i = 0L; i < 1 /* 0x7fffffffL + 2 */; i++) {
            // Subject and predicate both match
            s.getModel().add(s, P, String.valueOf(i));
        }

        assertThat(set, hasSize(lessThanOrEqualTo(Integer.MAX_VALUE)));
    }

    @DisplayName("size ignores other subjects")
    @Test
    void sizeIgnoresOtherSubjects() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);

        s.getModel().add(SX, P, O);

        assertThat(set, hasSize(0));
    }

    @DisplayName("size ignores other predicates")
    @Test
    void sizeIgnoresOtherPredicates() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);

        s.getModel().add(s, PX, O);

        assertThat(set, hasSize(0));
    }

    @DisplayName("size counts statements by subject and predicate")
    @Test
    void sizeCountsBySubjectAndPredicate() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);

        // All match
        s.getModel().add(s, P, O);

        assertThat(set, hasSize(1));
    }

    // endregion
    // region isEmpty

    @DisplayName("isEmpty ignores other subjects")
    @Test
    void isEmptyIgnoresOtherSubjects() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);

        s.getModel().add(SX, P, O);

        assertThat(set, is(empty()));
    }

    @DisplayName("isEmpty ignores other predicates")
    @Test
    void isEmptyIgnoresOtherPredicates() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);

        s.getModel().add(s, PX, O);

        assertThat(set, is(empty()));
    }

    @DisplayName("isEmpty is false if statements exist by subject and predicate")
    @Test
    void isEmptyFalseWithObject() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);

        s.getModel().add(s, P, O);

        assertThat(set, is(not(empty())));
    }

    @DisplayName("isEmpty is true if no statements exist by subject and predicate")
    @Test
    void isEmptyTrueWithoutObject() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);

        assertThat(set, is(empty()));
    }

    // endregion
    // region contains

    @DisplayName("Set invariant: contains throws if object is null")
    @Test
    void containsRequiresObject() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);

        assertThrows(NullPointerException.class, () ->
                set.contains(null));
    }

    @DisplayName("Set invariant: contains throws if object cannot be cast")
    @Test
    void containsRequiresCastableObject() {
        final var s = createDefaultModel().createResource();
        final var strings = new PredicateObjectSet<>(s, P, N2V, V2N);
        final var notAString = 0;

        assertThrows(ClassCastException.class, () ->
                strings.contains(notAString));
    }

    @DisplayName("contains ignores other subject")
    @Test
    void containsIgnoresOtherSubject() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);

        s.getModel().add(SX, P, O);

        assertThat(set, not(containsInAnyOrder(O)));
    }

    @DisplayName("contains ignores other predicate")
    @Test
    void containsIgnoresOtherPredicate() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);

        s.getModel().add(s, PX, O);

        assertThat(set, not(containsInAnyOrder(O)));
    }

    @DisplayName("contains is false if no statement exists by subject, predicate and converted object")
    @Test
    void containsTrueWithConvertedObject() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);

        s.getModel().add(s, P, OX);

        assertThat(set, not(containsInAnyOrder(O)));
    }

    @DisplayName("contains is true if statement exists by subject, predicate and converted object")
    @Test
    void containsConsidersObjectsOfPredicateForSubject() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);

        s.getModel().add(s, P, O);

        assertThat(set, containsInAnyOrder(O));
    }

    // endregion
    // region iterator

    @DisplayName("iterator remove is not supported")
    @Test
    void iteratorRemoveIsUnsupported() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);
        final var iterator = set.iterator();

        assertThrows(UnsupportedOperationException.class, iterator::remove);
    }

    @DisplayName("iterator ignores other subjects")
    @Test
    void iteratorIgnoresOtherSubjects() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);

        s.getModel().add(SX, P, O);

        assertThat(set, emptyIterableOf(String.class));
    }

    @DisplayName("iterator ignores other predicates")
    @Test
    void iteratorIgnoresOtherPredicates() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);

        s.getModel().add(s, PX, O);

        assertThat(set, emptyIterableOf(String.class));
    }

    @DisplayName("iterator enumerates converted objects by subject and predicate")
    @Test
    void iteratorConsidersConvertedObjectsOfPredicateForObject() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);

        // All match
        s.getModel().add(s, P, O);

        assertThat(set, containsInAnyOrder(O));
    }

    // endregion
    // region toArray

    @DisplayName("toArray ignores other subjects")
    @Test
    void toArrayIgnoresOtherSubjects() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);

        s.getModel().add(SX, P, O);

        assertThat(set.toArray(), emptyArray());
    }

    @DisplayName("toArray ignores other predicates")
    @Test
    void toArrayIgnoresOtherPredicates() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);

        s.getModel().add(s, PX, O);

        assertThat(set.toArray(), emptyArray());
    }

    @DisplayName("toArray enumerates converted objects by subject and predicate")
    @Test
    void toArrayConsidersConvertedObjectsOfPredicateForObject() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);

        s.getModel().add(s, P, O);

        assertThat(set.toArray(), arrayContainingInAnyOrder(O));
    }

    // endregion
    // region toArrayOther

    @DisplayName("Set invariant: toArray (other) throws if array is null")
    @Test
    void toArrayOtherRequiresArray() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);

        assertThrows(NullPointerException.class, () ->
                set.toArray((String[]) null));
    }

    @DisplayName("Set invariant: toArray (other) throws if array is other type")
    @Test
    void toArrayOtherRequiresMatchingArrayType() {
        final var s = createDefaultModel().createResource();
        final var strings = new PredicateObjectSet<>(s, P, N2V, V2N);
        final var notStrings = new UUID[0];

        s.getModel().add(s, P, O);

        assertThrows(ArrayStoreException.class, () ->
                strings.toArray(notStrings));
    }

    @DisplayName("Set invariant: toArray (other) reuses array if it fits")
    @Test
    void toArrayOtherReusesArray() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);
        final var spacious = new String[1];

        s.getModel().add(s, P, O);

        assertThat(set.toArray(spacious), is(sameInstance(spacious)));
    }

    @DisplayName("Set invariant: toArray (other) allocates new array if it does not fit")
    @Test
    void toArrayOtherAllocatesNewArray() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);
        final var crowded = new String[0];

        s.getModel().add(s, P, O);

        assertThat(set.toArray(crowded), is(not(sameInstance(crowded))));
    }

    @DisplayName("toArray (other) ignores other subjects")
    @Test
    void toArrayOtherIgnoresOtherSubjects() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);

        s.getModel().add(SX, P, O);

        assertThat(set.toArray(new String[0]), emptyArray());
    }

    @DisplayName("toArray (other) ignores other predicates")
    @Test
    void toArrayOtherIgnoresOtherPredicates() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);

        s.getModel().add(s, PX, O);

        assertThat(set.toArray(new String[0]), emptyArray());
    }

    @DisplayName("toArray (other) populates converted objects by subject and predicate")
    @Test
    void toArrayOtherConsidersConvertedObjectsOfPredicateForObject() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);

        s.getModel().add(s, P, O);

        assertThat(set.toArray(new String[0]), arrayContainingInAnyOrder(O));
    }

    // endregion
    // region add

    @DisplayName("Set invariant: add throws if element is null")
    @Test
    void addRequiresElement() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);

        assertThrows(NullPointerException.class, () ->
                set.add(null));
    }

    @DisplayName("Set invariant: add is false if set was not modified")
    @Test
    void addFalseWhenPresent() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);

        s.getModel().add(s, P, O);

        assertThat(set.add(O), is(false));
    }

    @DisplayName("Set invariant: add is true if set was modified")
    @Test
    void addTrueWhenNew() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);

        assertThat(set.add(O), is(true));
    }

    @DisplayName("add asserts statement by subject, predicate and converted element object")
    @Test
    void addAssertsConverted() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);
        final var oNode = createPlainLiteral(O);

        set.add(O);

        assertThat(s.getModel().getProperty(s, P), hasProperty("object", is(oNode)));
    }


    // endregion
    // region remove

    @DisplayName("Set invariant: remove throws if element is null")
    @Test
    void removeRequiresElement() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);

        assertThrows(NullPointerException.class, () ->
                set.remove(null));
    }

    @DisplayName("Set invariant: remove is false if set was not modified")
    @Test
    void removeFalseWhenPresent() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);

        assertThat(set.remove(O), is(false));
    }

    @DisplayName("Set invariant: remove is true if set was modified")
    @Test
    void removeTrueWhenNew() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);

        s.getModel().add(s, P, O);

        assertThat(set.remove(O), is(true));
    }

    @DisplayName("remove retracts statement by subject, predicate and converted object")
    @Test
    void removeRetractsConverted() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);

        s.getModel().add(s, P, O);

        set.remove(O);

        assertThat(s.getModel().getProperty(s, P), nullValue());
    }


    // endregion
    // region containsAll

    @DisplayName("Set invariant: contains all throws if collection is null")
    @Test
    void containsAllRequiresCollection() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);

        assertThrows(NullPointerException.class, () ->
                set.remove(null));
    }

    @DisplayName("Set invariant: contains all throws if collection has null element")
    @Test
    void containsAllRequiresNonNullElements() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);
        final var c = new ArrayList<>();
        c.add(null);

        assertThrows(NullPointerException.class, () ->
                set.containsAll(c));
    }

    @DisplayName("contains all ignores other subject")
    @Test
    void containsAllIgnoresOtherSubject() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);

        s.getModel().add(SX, P, O);

        assertThat(set.containsAll(List.of(O)), is(false));
    }

    @DisplayName("contains all ignores other predicate")
    @Test
    void containsAllIgnoresOtherPredicate() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);

        s.getModel().add(s, PX, O);

        assertThat(set.containsAll(List.of(O)), is(false));
    }

    @DisplayName("contains all is false if no statements exist by subject, predicate and any converted object")
    @Test
    void containsAllFalseForMissing() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);

        s.getModel().add(s, PX, OX);

        assertThat(set.containsAll(List.of(O)), is(false));
    }

    @DisplayName("contains all is true if statements exist by subject, predicate and any converted object")
    @Test
    void containsAllTrueForExisting() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);

        // All match
        s.getModel().add(s, P, O);

        assertThat(set.containsAll(List.of(O)), is(true));
    }

    // endregion
    // region addAll

    @DisplayName("Set invariant: add all throws if collection is null")
    @Test
    void addAllRequiresCollection() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);

        assertThrows(NullPointerException.class, () ->
                set.addAll(null));
    }

    @DisplayName("Set invariant: add all is false if set was not modified")
    @Test
    void addAllFalseWhenPresent() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);

        s.getModel().add(s, P, O);

        assertThat(set.addAll(List.of(O)), is(false));
    }

    @DisplayName("Set invariant: add all is true if set was modified")
    @Test
    void addAllTrueWhenNew() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);

        assertThat(set.addAll(List.of(O)), is(true));
    }

    @DisplayName("add all asserts statements by subject, predicate and converted element objects")
    @Test
    void addAllAssertsConverted() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);
        final var oNode = createPlainLiteral(O);

        set.addAll(List.of(O));

        assertThat(s.getModel().getProperty(s, P), hasProperty("object", is(oNode)));
    }


    // endregion
    // region retainAll

    @DisplayName("Set invariant: retain all throws if collection is null")
    @Test
    void retainAllRequiresCollection() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);

        assertThrows(NullPointerException.class, () ->
                set.retainAll(null));
    }

    @DisplayName("Set invariant: retain all does not throw if collection element is not castable")
    @Test
    void retainAllAllowsNonCastableCollection() {
        final var s = createDefaultModel().createResource();
        final var strings = new PredicateObjectSet<>(s, P, N2V, V2N);
        final var notStrings = List.of(randomUUID());

        s.getModel().add(s, P, O);

        assertDoesNotThrow(() ->
                strings.retainAll(notStrings));
    }

    @DisplayName("Set invariant: retain all is false if set was not modified")
    @Test
    void retainAllLeavesElementsOfCollection1() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);

        assertThat(set.retainAll(List.of(O)), is(false));
    }

    @DisplayName("Set invariant: retain all is true if set was modified")
    @Test
    void retainAllRetractsWhenObjectMissing1() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);

        s.getModel().add(s, P, OX);

        assertThat(set.retainAll(List.of(O)), is(true));
    }

    @DisplayName("retain all does not retract if converted object is in collection")
    @Test
    void retainAllLeavesElementsOfCollection() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);
        final var oNode = createPlainLiteral(O);

        s.getModel().add(s, P, O);

        set.retainAll(List.of(O));

        assertThat(s.getModel().getProperty(s, P), hasProperty("object", is(oNode)));
    }

    @DisplayName("retain all retracts by subject and predicate if converted object is not in collection")
    @Test
    void retainAllRetractsWhenObjectMissing() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);

        s.getModel().add(s, P, OX);

        set.retainAll(List.of(O));

        assertThat(s.getModel().getProperty(s, P), nullValue());
    }

    // endregion
    // region removeAll

    @DisplayName("Set invariant: remove all throws if collection is null")
    @Test
    void removeAllRequiresCollection() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);

        assertThrows(NullPointerException.class, () ->
                set.removeAll(null));
    }

    @DisplayName("Set invariant: remove all throws if collection element is not castable")
    @Test
    void removeAllRequiresCastableCollection() {
        final var s = createDefaultModel().createResource();
        final var strings = new PredicateObjectSet<>(s, P, N2V, V2N);
        final var notStrings = List.of(randomUUID());

        s.getModel().add(s, P, O);

        assertThrows(ClassCastException.class, () ->
                strings.removeAll(notStrings));
    }

    @DisplayName("Set invariant: remove all is false if set was not modified")
    @Test
    void removeAllFalseIfUnchanged() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);

        assertThat(set.removeAll(List.of(O)), is(false));
    }

    @DisplayName("Set invariant: remove all is true if set was modified")
    @Test
    void removeAllTruIfChanged() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);

        s.getModel().add(s, P, O);

        assertThat(set.removeAll(List.of(O)), is(true));
    }

    @DisplayName("remove all ignores other subject")
    @Test
    void removeAllIgnoresOtherSubject() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);

        s.getModel().add(SX, P, O);

        set.removeAll(List.of(O));

        assertThat(set.removeAll(List.of(O)), is(false));
    }

    @DisplayName("remove all ignores other predicate")
    @Test
    void removeAllIgnoresOtherPredicate() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);

        s.getModel().add(s, PX, O);

        set.removeAll(List.of(O));

        assertThat(set.removeAll(List.of(O)), is(false));
    }

    @DisplayName("remove all ignores other object")
    @Test
    void removeAllIgnoresOtherObject() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);

        s.getModel().add(s, P, OX);

        set.removeAll(List.of(O));

        assertThat(set.removeAll(List.of(O)), is(false));
    }

    @DisplayName("remove all retracts by subject, predicate and any converted object")
    @Test
    void removeAllRetracts() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);

        s.getModel().add(s, P, O);

        set.removeAll(List.of(O));

        assertThat(s.getModel().getProperty(s, P), nullValue());
    }

    // endregion
    // region clear

    @DisplayName("clear retracts all statements by subject and predicate")
    @Test
    void clearRetractsConverted() {
        final var s = createDefaultModel().createResource();
        final var set = new PredicateObjectSet<>(s, P, N2V, V2N);

        s.getModel().add(s, P, O);

        set.clear();

        assertThat(s.getModel().getProperty(s, P), nullValue());
    }

    // endregion
    // region equals

    @DisplayName("equals is false for different subjects")
    @Test
    void equalsFalseForDifferentSubjects() {
        final var s1 = createDefaultModel().createResource();
        final var set1 = new PredicateObjectSet<>(s1, P, N2V, V2N);
        final var s2 = createDefaultModel().createResource();
        final var set2 = new PredicateObjectSet<>(s2, P, N2V, V2N);

        s1.getModel().add(s1, P, O);
        s2.getModel().add(SX, P, O);

        assertThat(set1, is(not(set2)));
    }

    @DisplayName("equals is false for different predicates")
    @Test
    void equalsFalseForDifferentPredicates() {
        final var s1 = createDefaultModel().createResource();
        final var set1 = new PredicateObjectSet<>(s1, P, N2V, V2N);
        final var s2 = createDefaultModel().createResource();
        final var set2 = new PredicateObjectSet<>(s2, P, N2V, V2N);

        s1.getModel().add(s1, P, O);
        s2.getModel().add(s2, PX, O);

        assertThat(set1, is(not(set2)));
    }

    @DisplayName("equals is false for different objects")
    @Test
    void equalsFalseForDifferentObjects() {
        final var s1 = createDefaultModel().createResource();
        final var set1 = new PredicateObjectSet<>(s1, P, N2V, V2N);
        final var s2 = createDefaultModel().createResource();
        final var set2 = new PredicateObjectSet<>(s2, P, N2V, V2N);

        s1.getModel().add(s1, P, O);
        s2.getModel().add(s2, P, OX);

        assertThat(set1, is(not(set2)));
    }

    @DisplayName("equals is true for empty sets")
    @Test
    void equalsTrueForEmpty() {
        final var s1 = createDefaultModel().createResource();
        final var set1 = new PredicateObjectSet<>(s1, P, N2V, V2N);
        final var s2 = createDefaultModel().createResource();
        final var set2 = new PredicateObjectSet<>(s2, P, N2V, V2N);

        assertThat(set1, is(set2));
    }

    @DisplayName("equals is true for sets with equal predicate and object")
    @Test
    void equalsTrueForEqualPredicateAndObject() {
        final var s1 = createDefaultModel().createResource();
        final var set1 = new PredicateObjectSet<>(s1, P, N2V, V2N);
        final var s2 = createDefaultModel().createResource();
        final var set2 = new PredicateObjectSet<>(s2, P, N2V, V2N);

        s1.getModel().add(s1, P, O);
        s2.getModel().add(s2, P, O);

        assertThat(set1, is(set2));
    }

    // endregion
    // region hashCode

    @DisplayName("hashCode differs for different subjects")
    @Test
    void hashCodeDiffersForDifferentSubjects() {
        final var s1 = createDefaultModel().createResource();
        final var set1 = new PredicateObjectSet<>(s1, P, N2V, V2N);
        final var s2 = createDefaultModel().createResource();
        final var set2 = new PredicateObjectSet<>(s2, P, N2V, V2N);

        s1.getModel().add(s1, P, O);
        s2.getModel().add(SX, P, O);

        assertThat(set1.hashCode(), is(not(set2.hashCode())));
    }

    @DisplayName("hashCode differs for different predicates")
    @Test
    void hashCodeDiffersForDifferentPredicates() {
        final var s1 = createDefaultModel().createResource();
        final var set1 = new PredicateObjectSet<>(s1, P, N2V, V2N);
        final var s2 = createDefaultModel().createResource();
        final var set2 = new PredicateObjectSet<>(s2, P, N2V, V2N);

        s1.getModel().add(s1, P, O);
        s2.getModel().add(s2, PX, O);

        assertThat(set1.hashCode(), is(not(set2.hashCode())));
    }

    @DisplayName("hashCode differs for different objects")
    @Test
    void hashCodeDiffersForDifferentObjects() {
        final var s1 = createDefaultModel().createResource();
        final var set1 = new PredicateObjectSet<>(s1, P, N2V, V2N);
        final var s2 = createDefaultModel().createResource();
        final var set2 = new PredicateObjectSet<>(s2, P, N2V, V2N);

        s1.getModel().add(s1, P, O);
        s2.getModel().add(s2, P, OX);

        assertThat(set1.hashCode(), is(not(set2.hashCode())));
    }

    @DisplayName("hashCode matches for empty sets")
    @Test
    void hashCodeSameForEmpty() {
        final var s1 = createDefaultModel().createResource();
        final var set1 = new PredicateObjectSet<>(s1, P, N2V, V2N);
        final var s2 = createDefaultModel().createResource();
        final var set2 = new PredicateObjectSet<>(s2, P, N2V, V2N);

        assertThat(set1.hashCode(), is(set2.hashCode()));
    }

    @DisplayName("hashCode matches for sets with equal predicate and object")
    @Test
    void hashCodeSameForEqualPredicateAndObject() {
        final var s1 = createDefaultModel().createResource();
        final var set1 = new PredicateObjectSet<>(s1, P, N2V, V2N);
        final var s2 = createDefaultModel().createResource();
        final var set2 = new PredicateObjectSet<>(s2, P, N2V, V2N);

        s1.getModel().add(s1, P, O);
        s2.getModel().add(s2, P, O);

        assertThat(set1.hashCode(), is(set2.hashCode()));
    }

    // endregion
}
