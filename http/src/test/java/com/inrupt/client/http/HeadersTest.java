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
package com.inrupt.client.http;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class HeadersTest {

    @Test
    void parseSingleLink() {
        final var header = "<https://example.com/%E8%8B%97%E6%9D%A1>; rel=\"preconnect\"";
        final var linkValues = Headers.link(header);

        assertEquals(List.of(
                    Link.of(URI.create("https://example.com/%E8%8B%97%E6%9D%A1"), Map.of(
                            "rel", "preconnect"))), linkValues);
    }

    @ParameterizedTest
    @MethodSource
    void parseListedParams(final String header, final List<Link> expected) {
        final var linkValues = Headers.link(header);
        assertEquals(expected, linkValues);
    }

    private static Stream<Arguments> parseListedParams() {
        return Stream.of(
                Arguments.of("<https://example.com/%E8%8B%97%E6%9D%A1>; rel=\"param1\" ; type=\"param2\"",
                    List.of(
                        Link.of(URI.create("https://example.com/%E8%8B%97%E6%9D%A1"),
                            Map.of("rel", "param1", "type", "param2")))),

                Arguments.of("<https://example.com/%E8%8B%97%E6%9D%A1>; rel=\"param1\" ;type=\"param2\"",
                        List.of(
                            Link.of(URI.create("https://example.com/%E8%8B%97%E6%9D%A1"),
                                Map.of("rel", "param1", "type", "param2")))),

                Arguments.of("<https://example.com/%E8%8B%97%E6%9D%A1>; rel=\"param1\"; type=\"param2\"",
                        List.of(
                            Link.of(URI.create("https://example.com/%E8%8B%97%E6%9D%A1"),
                                Map.of("rel", "param1", "type", "param2")))),

                Arguments.of("<https://example.com/%E8%8B%97%E6%9D%A1>; rel=\"param1\";type=\"param2\"",
                        List.of(
                            Link.of(URI.create("https://example.com/%E8%8B%97%E6%9D%A1"),
                                Map.of("rel", "param1", "type", "param2")))));
    }


    @ParameterizedTest
    @MethodSource
    void parseListedLinks(final String header, final List<Link> expected) {
        final var linkValues = Headers.link(header);
        assertEquals(expected, linkValues);
    }

    private static Stream<Arguments> parseListedLinks() {
        return Stream.of(
                Arguments.of("<https://one.example.com>, <https://two.example.com>, <https://three.example.com>",
                    List.of(
                        Link.of(URI.create("https://one.example.com"), Collections.emptyMap()),
                        Link.of(URI.create("https://two.example.com"), Collections.emptyMap()),
                        Link.of(URI.create("https://three.example.com"), Collections.emptyMap()))),

                Arguments.of("<https://one.example.com>; rel=\"preconnect\" ," +
                    "<https://two.example.com>; rel=\"meta\"," +
                    "<https://three.example.com>; rel=\"stylesheet\"",
                    List.of(
                        Link.of(URI.create("https://one.example.com"), Map.of("rel", "preconnect")),
                        Link.of(URI.create("https://two.example.com"), Map.of("rel", "meta")),
                        Link.of(URI.create("https://three.example.com"), Map.of("rel", "stylesheet")))),

                Arguments.of("<https://one.example.com>; rel=\"rel1\" ; type=\"type1\" ," +
                    "<https://two.example.com>; rel=\"rel2\" ; type=\"type2\" ," +
                    "<https://three.example.com>; rel=\"rel3\" ; type=\"type3\"",
                    List.of(
                        Link.of(URI.create("https://one.example.com"), Map.of("rel", "rel1", "type", "type1")),
                        Link.of(URI.create("https://two.example.com"), Map.of("rel", "rel2", "type", "type2")),
                        Link.of(URI.create("https://three.example.com"), Map.of("rel", "rel3", "type", "type3")))));
    }

    @ParameterizedTest
    @MethodSource
    void parseRelativeReferenceLink(final String header, final List<Link> expected) {
        final var linkValues = Headers.link(header);
        assertEquals(expected, linkValues);
    }

    private static Stream<Arguments> parseRelativeReferenceLink() {
        return Stream.of(
                Arguments.of("<../../team/>",
                    List.of(
                        Link.of(URI.create("../../team/"), Collections.emptyMap()))),
                Arguments.of("<../about/./team/>",
                    List.of(
                        Link.of(URI.create("../about/./team/"), Collections.emptyMap()))),
                Arguments.of("</about/team/>",
                        List.of(
                            Link.of(URI.create("/about/team/"), Collections.emptyMap()))));
    }

    @ParameterizedTest
    @MethodSource
    void parseLinkInOrder(final String header, final List<Link> expected) {
        final var linkValues = Headers.link(header);
        assertEquals(expected, linkValues);
    }

    private static Stream<Arguments> parseLinkInOrder() {
        return Stream.of(
                Arguments.of("<https://one.example.com>; rel=\"preconnect\", " +
                    "<https://two.example.com>; rel=\"meta\", " +
                    "<https://three.example.com>; rel=\"stylesheet\"",
                List.of(
                    Link.of(URI.create("https://one.example.com"), Map.of("rel", "preconnect")),
                    Link.of(URI.create("https://two.example.com"), Map.of("rel", "meta")),
                    Link.of(URI.create("https://three.example.com"), Map.of("rel", "stylesheet")))),
                Arguments.of("<https://two.example.com>; rel=\"meta\", " +
                    "<https://one.example.com>; rel=\"preconnect\"," +
                    "<https://three.example.com>; rel=\"stylesheet\"",
                List.of(
                    Link.of(URI.create("https://two.example.com"), Map.of("rel", "meta")),
                    Link.of(URI.create("https://one.example.com"), Map.of("rel", "preconnect")),
                    Link.of(URI.create("https://three.example.com"), Map.of("rel", "stylesheet"))))
            );
    }

    @ParameterizedTest
    @MethodSource
    void parseIRIs(final String header, final List<Link> expected) {
        final var linkValues = Headers.link(header);
        assertEquals(expected, linkValues, "Unexpected handling of IRI values");
    }

    private static Stream<Arguments> parseIRIs() {
        return Stream.of(
                Arguments.of("<https://example.com/苗条>",
                    List.of(Link.of(URI.create("https://example.com/苗条"), Collections.emptyMap())))
                );
    }

    @ParameterizedTest
    @MethodSource
    void parseNonHttpUris(final String header, final List<Link> expected) {
        final var linkValues = Headers.link(header);
        assertEquals(expected, linkValues, "Unexpected handling of invalid link header");
    }

    private static Stream<Arguments> parseNonHttpUris() {
        return Stream.of(
                Arguments.of("<did:web:resource.example/path>",
                    List.of(Link.of(URI.create("did:web:resource.example/path"), Collections.emptyMap()))),
                Arguments.of("<file:/path/to/file>",
                    List.of(Link.of(URI.create("file:/path/to/file"), Collections.emptyMap()))),
                Arguments.of("<file:///path/to/file>",
                    List.of(Link.of(URI.create("file:///path/to/file"), Collections.emptyMap())))
                );
    }

    @ParameterizedTest
    @MethodSource
    void ignoreInvalidParams(final String header, final List<Link> expected) {
        final var linkValues = Headers.link(header);
        assertEquals(expected, linkValues);
    }

    private static Stream<Arguments> ignoreInvalidParams() {
        return Stream.of(
                Arguments.of("<https://example.com>; rel==\"previous\"",
                    List.of(Link.of(URI.create("https://example.com"),
                            Collections.emptyMap()))),
                Arguments.of("<http://example.com>; type==text; rel=\"previous\";" +
                    "title=\"previous chapter\"",
                    List.of(Link.of(URI.create("http://example.com"),
                            Map.of("rel", "previous", "title", "previous chapter")))),              
                Arguments.of("<http://example.com>; \"type\"=\"text\"; rel=\"previous\";" +
                    "title=\"previous chapter\"",
                    List.of(Link.of(URI.create("http://example.com"),
                            Map.of("rel", "previous", "title", "previous chapter"))))
                );
    }

    @ParameterizedTest
    @MethodSource
    void parseInvalidLinkHeader(final String header, final List<Link> expected) {
        final var linkValues = Headers.link(header);
        assertEquals(expected, linkValues, "Unexpected handling of invalid link header");
    }


    private static Stream<Arguments> parseInvalidLinkHeader() {
        return Stream.of(
                Arguments.of("https://bad.example",
                    Collections.emptyList()),
                Arguments.of("rel=\"missingUri\"; type=\"text\"",
                    Collections.emptyList()),
                Arguments.of("<https://example.com/{}>",
                    Collections.emptyList()),
                Arguments.of("<https://example.com/^>",
                    Collections.emptyList()),
                Arguments.of("<https://example.com/`>",
                    Collections.emptyList()),
                Arguments.of("<https://example.com/|>",
                    Collections.emptyList()),
                Arguments.of("<https://example.com/\t>",
                    Collections.emptyList()),
                Arguments.of("<https://example.com/     >",
                    Collections.emptyList()),
                Arguments.of("<https://example.com />",
                    Collections.emptyList()),
                Arguments.of("<https://example.com/<>/test>",
                    Collections.emptyList()),
                Arguments.of("<https://example.com/>/test>",
                    Collections.emptyList()),
                Arguments.of("<https://example.com/>/test/>/test>",
                    Collections.emptyList()),
                Arguments.of("<https://example.com/>/test/>/test",
                    Collections.emptyList()),

                //not working
                Arguments.of("<https://example.com/>/test",
                    Collections.emptyList()),
                Arguments.of("<https://example.com/>/test; rel=\"param\"",
                    Collections.emptyList()),
                Arguments.of("https://example.com/</test/>/test",
                    Collections.emptyList()),
                Arguments.of("<https:/badexample>",
                    Collections.emptyList()),
                Arguments.of("<notURIReference>",
                    Collections.emptyList())
                
            );
    }

    @ParameterizedTest
    @MethodSource
    void ignoreInvalidLinks(final String header, final List<Link> expected) {
        final var linkValues = Headers.link(header);
        assertEquals(expected, linkValues);
    }

    private static Stream<Arguments> ignoreInvalidLinks() {
        return Stream.of(
                Arguments.of("<https://one.example.com>; title=\"valid\", " +
                            "https://two.example.com; title=\"invalid\"",
                    List.of(
                        Link.of(URI.create("https://one.example.com"), Map.of("title", "valid")))),
                Arguments.of("<https://one.example.com>; title=\"valid\", " +
                            "https://two.example.com; title=\"invalid\", " +
                            "<https://three.example.com>; title=\"valid\"",
                    List.of( 
                        Link.of(URI.create("https://one.example.com"), Map.of("title", "valid")), 
                        Link.of(URI.create("https://three.example.com"), Map.of("title", "valid"))))
                );
    }

    
}
