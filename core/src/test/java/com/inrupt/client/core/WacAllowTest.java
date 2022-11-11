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
package com.inrupt.client.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;


class WacAllowTest {

    @Test
    void parseSingleAccessParam() {
        final String header = "WAC-Allow: user=\"read\"";
        final Map<String, Set<String>> accessParams = WacAllow.parse(header).getAccessParams();
        final Map<String, Set<String>> expected = Map.of("user", Set.of("read"));

        assertEquals(expected, accessParams);
    }

    @Test
    void parseEmptyAccessParam() {
        final String header = "WAC-Allow:";
        final Map<String, Set<String>> accessParams = WacAllow.parse(header).getAccessParams();
        final Map<String, Set<String>> expected = new HashMap<>();

        assertEquals(expected, accessParams);
    }

    @Test
    void parseEmptyAccessMode() {
        final String header = "WAC-Allow: user=\"\"";
        final Map<String, Set<String>> accessParams = WacAllow.parse(header).getAccessParams();

        assertTrue(!accessParams.containsKey("user"));
    }

    @Test
    void parseWhiteSpaceAccessMode() {
        final String header = "WAC-Allow: user=\" \"";
        final Map<String, Set<String>> accessParams = WacAllow.parse(header).getAccessParams();

        assertTrue(!accessParams.containsKey("user"));
    }

    @Test
    void trimAccessMode() {
        final String header = "WAC-Allow: user=\"    read   \"";
        final Map<String, Set<String>> accessParams = WacAllow.parse(header).getAccessParams();
        final Map<String, Set<String>> expected = Map.of("user", Set.of("read"));

        assertEquals(expected, accessParams);
    }


    @ParameterizedTest
    @MethodSource
    void parseListedAccessModes(final String header, final Map<String, Set<String>> expected) {
        final Map<String, Set<String>> accessParams = WacAllow.parse(header).getAccessParams();
        assertEquals(expected, accessParams);
    }

    private static Stream<Arguments> parseListedAccessModes() {
        return Stream.of(
                Arguments.of("WAC-Allow: user=\"read write\"",
                    Map.of("user", Set.of("read", "write"))),
                Arguments.of("WAC-Allow: user=\"read write append\"",
                    Map.of("user", Set.of("read", "write", "append"))));
    }

    @ParameterizedTest
    @MethodSource
    void parseWhiteSpaceEdgeCases(final String header, final Map<String, Set<String>> expected) {
        final Map<String, Set<String>> accessParams = WacAllow.parse(header).getAccessParams();
        assertEquals(expected, accessParams);
    }

    private static Stream<Arguments> parseWhiteSpaceEdgeCases() {
        return Stream.of(
                Arguments.of("WAC-Allow: user=\"read    write\"",
                    Map.of("user", Set.of("read", "write"))),
                Arguments.of("WAC-Allow: user=\"    read write    \"",
                    Map.of("user", Set.of("read", "write"))),
                Arguments.of("WAC-Allow: user=\"read    write      append\"",
                    Map.of("user", Set.of("read", "write", "append"))),
                Arguments.of("WAC-Allow: user=\"    read write append   \"",
                    Map.of("user", Set.of("read", "write", "append"))));
    }

    @ParameterizedTest
    @MethodSource
    void parseListedAccessParams(final String header, final Map<String, Set<String>> expected) {
        final Map<String, Set<String>> accessParams = WacAllow.parse(header).getAccessParams();
        assertEquals(expected, accessParams);
    }

    private static Stream<Arguments> parseListedAccessParams() {
        return Stream.of(
                Arguments.of("WAC-Allow: public=\"read\"",
                    Map.of("public", Set.of("read"))),
                Arguments.of("WAC-Allow: user=\"read\", public=\"read\"",
                    Map.of("user", Set.of("read"),
                            "public", Set.of("read"))),

                Arguments.of("WAC-Allow: user=\"read\", public=\"read\", other=\"read\"",
                    Map.of("user", Set.of("read"),
                            "public", Set.of("read"),
                            "other", Set.of("read"))));
    }

    @ParameterizedTest
    @MethodSource
    void parseMultipleParamsAndPermisions(final String header, final Map<String, Set<String>> expected) {
        final Map<String, Set<String>> accessParams = WacAllow.parse(header).getAccessParams();
        assertEquals(expected, accessParams);
    }

    private static Stream<Arguments> parseMultipleParamsAndPermisions() {
        return Stream.of(
                Arguments.of("WAC-Allow: user=\"read\", public=\"read write\"",
                    Map.of("user", Set.of("read"),
                            "public", Set.of("read", "write"))),
                Arguments.of("WAC-Allow: user=\"read write\", public=\"read write\"",
                    Map.of("user", Set.of("read", "write"),
                            "public", Set.of("read", "write"))),

                Arguments.of("WAC-Allow: user=\"read\", public=\"read write\", other=\"read write append\"",
                    Map.of("user", Set.of("read"),
                            "public", Set.of("read", "write"),
                            "other", Set.of("read", "write", "append"))));
    }

    @ParameterizedTest
    @MethodSource
    void parseAbnfListExtension(final String header, final Map<String, Set<String>> expected) {
        final Map<String, Set<String>> accessParams = WacAllow.parse(header).getAccessParams();
        assertEquals(expected, accessParams);
    }

    private static Stream<Arguments> parseAbnfListExtension() {
        return Stream.of(
                Arguments.of("WAC-Allow: user=\"read\",public=\"read\"",
                    Map.of("user", Set.of("read"),
                            "public", Set.of("read"))),
                Arguments.of("WAC-Allow: user=\"read\" ,public=\"read\"",
                    Map.of("user", Set.of("read"),
                            "public", Set.of("read"))),
                Arguments.of("WAC-Allow: user=\"read\" , public=\"read\"",
                    Map.of("user", Set.of("read"),
                            "public", Set.of("read"))),
                Arguments.of("WAC-Allow: user=\"read\",, public=\"read\"",
                    Map.of("user", Set.of("read"),
                            "public", Set.of("read"))),
                Arguments.of("WAC-Allow: ,,user=\"read\", public=\"read\"",
                    Map.of("user", Set.of("read"),
                            "public", Set.of("read"))));
    }
}