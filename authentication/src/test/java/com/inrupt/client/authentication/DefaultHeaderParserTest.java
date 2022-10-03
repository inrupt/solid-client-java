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
package com.inrupt.client.authentication;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DefaultHeaderParserTest {

    @Test
    void parseWwwAuthenticateUmaBearerDpop() {
        final var parser = new DefaultHeaderParser();
        final var header = "UMA as_uri=\"https://example.test\", ticket=value, Bearer, DPoP algs=\"ES256 RS256\"";
        final var challenges = parser.wwwAuthenticate(header);

        final var expected = List.of(
                    new Challenge("UMA", Map.of( "as_uri", "https://example.test","ticket", "value")),
                    new Challenge("Bearer"),
                    new Challenge("DPoP", Map.of("algs", "ES256 RS256")));

        assertEquals(expected, challenges);

    }

    @Test
    void parseCaseInsensitive() {
        final var parser = new DefaultHeaderParser();
        final var header = "uma as_uri=\"https://example.test\", ticket=value, dpop algs=\"ES256 RS256\"";
        final var challenges = parser.wwwAuthenticate(header);

        final var expected = List.of(
                    new Challenge("UMA", Map.of("as_uri", "https://example.test", "ticket", "value")),
                    new Challenge("DPoP", Map.of("algs", "ES256 RS256")));


        assertEquals(expected, challenges);

    }

    @Test
    void parseBasic() {
        final var parser = new DefaultHeaderParser();
        final var header = "UMA as_uri=\"https://example.test\", ticket=value, basic realm=\"protected\"";
        final var challenges = parser.wwwAuthenticate(header);


        final var expected = List.of(
                    new Challenge("UMA", Map.of("as_uri", "https://example.test", "ticket", "value")),
                    new Challenge("Basic", Map.of("realm", "protected")));

        assertEquals(expected, challenges);

    }

    @Test
    void parseWwwAuthenticateBearerUmaGnap() {
        final var parser = new DefaultHeaderParser();
        final var header = "Bearer, UMA as_uri=\"https://example.test\", GNAP ticket=1234567890";
        final var challenges = parser.wwwAuthenticate(header);

        final var expected = List.of(
                    new Challenge("Bearer"),
                    new Challenge("UMA", Map.of("as_uri", "https://example.test")),
                    new Challenge("GNAP", Map.of("ticket", "1234567890")));

        assertEquals(expected, challenges);

    }

    @ParameterizedTest
    @MethodSource
    void parseWwwAuthenticateParams(final String header, final List<Challenge> expected) {
        final var parser = new DefaultHeaderParser();
        final var challenges = parser.wwwAuthenticate(header);
        assertEquals(challenges, expected);
    }

    private static Stream<Arguments> parseWwwAuthenticateParams() {
        return Stream.of(
                Arguments.of("UMA as_uri=\"https://example.test\", ticket=value, Bearer, DPoP algs=\"ES256 RS256\"",
                    List.of(
                        new Challenge("UMA", Map.of("as_uri", "https://example.test", "ticket", "value")),
                        new Challenge("Bearer"),
                        new Challenge("DPoP", Map.of("algs", "ES256 RS256")))),
                Arguments.of("Bearer, UMA as_uri=\"https://example.test\", GNAP ticket=1234567890",
                    List.of(
                        new Challenge("Bearer"),
                        new Challenge("UMA", Map.of("as_uri", "https://example.test")),
                        new Challenge("GNAP", Map.of("ticket", "1234567890"))))
            );
    }

    @ParameterizedTest
    @MethodSource
    void parseWwwAuthenticateToken68(final String header, final List<Challenge> expected) {
        final var parser = new DefaultHeaderParser();
        final var challenges = parser.wwwAuthenticate(header);
        assertEquals(challenges, expected);
    }

    private static Stream<Arguments> parseWwwAuthenticateToken68() {
        return Stream.of(
                Arguments.of("Basic abcdef== realm=basic key=\"a value\"",
                    List.of(
                        new Challenge("Basic", Map.of("realm", "basic", "key", "a value"))))
            );
    }

    @ParameterizedTest
    @MethodSource
    void parseInvalidWwwAuthenticationHeader(final String header, final List<Challenge> expected) {
        final var parser = new DefaultHeaderParser();
        final var challenges = parser.wwwAuthenticate(header);
        assertEquals(challenges, expected);
    }

    private static Stream<Arguments> parseInvalidWwwAuthenticationHeader() {
        return Stream.of(
                Arguments.of("Basic realm==basic, UMA =not =valid",
                    List.of(
                        new Challenge("Basic"))),
                Arguments.of("In=Valid realm=\"basic\"",
                    Collections.emptyList())
            );
    }
}

