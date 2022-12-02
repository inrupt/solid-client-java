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

import com.inrupt.client.Headers.Link;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class LinkTest {

    static final Map<String, String> REL_PRECONNECT = Collections.singletonMap("rel", "preconnect");
    static final Map<String, String> REL_META = Collections.singletonMap("rel", "meta");
    static final Map<String, String> REL_STYLESHEET = Collections.singletonMap("rel", "stylesheet");
    static final Map<String, String> REL_PARAM = new HashMap<String, String>(){
        {
            put("rel", "param1");
            put("type", "param2");
        }
    };
    static final Map<String, String> REL_PREVIOUS = new HashMap<String, String>(){
        {
            put("rel", "previous");
            put("title", "previous chapter");
        }
    };

    @Test
    void testLinkFactories() {
        final URI uri = URI.create("https://example.com/%E8%8B%97%E6%9D%A1");
        final String rel = "preconnect";

        assertEquals(Link.of(uri), Link.of(uri, new HashMap<>()));
        assertEquals(Link.of(uri, rel), Link.of(uri, Collections.singletonMap("rel", rel)));

    }

    @Test
    void parseSingleLink() {
        final String header = "<https://example.com/%E8%8B%97%E6%9D%A1>; rel=\"preconnect\"";
        final List<Link> linkValues = Link.parse(header);

        final List<Link> expected = Arrays.asList(
                    Link.of(URI.create("https://example.com/%E8%8B%97%E6%9D%A1"),
                    REL_PRECONNECT));

        assertEquals(expected, linkValues);
    }

    @Test
    void parseMultipleLinks() {
        final String header1 = "<https://example.com/%E8%8B%97%E6%9D%A1>; rel=\"preconnect\"";
        final String header2 = "<https://example.com/Type>; rel=\"type\"";
        final List<Link> linkValues = Link.parse(header1, header2);

        final List<Link> expected = Arrays.asList(
                    Link.of(URI.create("https://example.com/%E8%8B%97%E6%9D%A1"), REL_PRECONNECT),
                Link.of(URI.create("https://example.com/Type"), new HashMap<String, String>() {
                    {
                        put("rel", "type");
                    }
                }));

        assertEquals(expected, linkValues);
    }

    @ParameterizedTest
    @MethodSource
    void parseListedParams(final String header, final List<Link> expected) {
        final List<Link> linkValues = Link.parse(header);
        assertEquals(expected, linkValues);
    }

    private static Stream<Arguments> parseListedParams() {
        return Stream.of(
                Arguments.of("<https://example.com/%E8%8B%97%E6%9D%A1>; rel=\"param1\" ; type=\"param2\"",
                    Arrays.asList(
                        Link.of(URI.create("https://example.com/%E8%8B%97%E6%9D%A1"),
                            REL_PARAM))),

                Arguments.of("<https://example.com/%E8%8B%97%E6%9D%A1>; rel=\"param1\" ;type=\"param2\"",
                        Arrays.asList(
                            Link.of(URI.create("https://example.com/%E8%8B%97%E6%9D%A1"),
                                REL_PARAM))),

                Arguments.of("<https://example.com/%E8%8B%97%E6%9D%A1>; rel=\"param1\"; type=\"param2\"",
                        Arrays.asList(
                            Link.of(URI.create("https://example.com/%E8%8B%97%E6%9D%A1"),
                                REL_PARAM))),

                Arguments.of("<https://example.com/%E8%8B%97%E6%9D%A1>; rel=\"param1\";type=\"param2\"",
                        Arrays.asList(
                            Link.of(URI.create("https://example.com/%E8%8B%97%E6%9D%A1"),
                                REL_PARAM))));
    }


    @ParameterizedTest
    @MethodSource
    void parseListedLinks(final String header, final List<Link> expected) {
        final List<Link> linkValues = Link.parse(header);
        assertEquals(expected, linkValues);
    }

    private static Stream<Arguments> parseListedLinks() {
        return Stream.of(
                Arguments.of("<https://one.example.com>, <https://two.example.com>, <https://three.example.com>",
                    Arrays.asList(
                        Link.of(URI.create("https://one.example.com"), Collections.emptyMap()),
                        Link.of(URI.create("https://two.example.com"), Collections.emptyMap()),
                        Link.of(URI.create("https://three.example.com"), Collections.emptyMap()))),

                Arguments.of("<https://one.example.com>; rel=\"preconnect\" ," +
                    "<https://two.example.com>; rel=\"meta\"," +
                    "<https://three.example.com>; rel=\"stylesheet\"",
                    Arrays.asList(
                        Link.of(URI.create("https://one.example.com"), REL_PRECONNECT),
                        Link.of(URI.create("https://two.example.com"), REL_META),
                        Link.of(URI.create("https://three.example.com"), REL_STYLESHEET)),

                Arguments.of("<https://one.example.com>; rel=\"rel1\" ; type=\"type1\" ," +
                    "<https://two.example.com>; rel=\"rel2\" ; type=\"type2\" ," +
                    "<https://three.example.com>; rel=\"rel3\" ; type=\"type3\"",
                    Arrays.asList(
                        Link.of(URI.create("https://one.example.com"),
                            new HashMap<String, String>() {
                                {
                                    put("rel", "rel1");
                                    put("type", "type1");
                                }
                            }
                        ),
                        Link.of(URI.create("https://two.example.com"),
                            new HashMap<String, String>() {
                                {
                                    put("rel", "rel2");
                                    put("type", "type2");
                                }
                            }
                        ),
                        Link.of(URI.create("https://three.example.com"),
                            new HashMap<String, String>() {
                                {
                                    put("rel", "rel3");
                                    put("type", "type3");
                                }
                            }
                        )
                ))));
    }

    @ParameterizedTest
    @MethodSource
    void parseRelativeReferenceLink(final String header, final List<Link> expected) {
        final List<Link> linkValues = Link.parse(header);
        assertEquals(expected, linkValues);
    }

    private static Stream<Arguments> parseRelativeReferenceLink() {
        return Stream.of(
                Arguments.of("<../../team/>",
                    Arrays.asList(
                        Link.of(URI.create("../../team/"), Collections.emptyMap()))),
                Arguments.of("<../about/./team/>",
                    Arrays.asList(
                        Link.of(URI.create("../about/./team/"), Collections.emptyMap()))),
                Arguments.of("</about/team/>",
                    Arrays.asList(
                        Link.of(URI.create("/about/team/"), Collections.emptyMap()))));
    }

    @ParameterizedTest
    @MethodSource
    void parseLinkInOrder(final String header, final List<Link> expected) {
        final List<Link> linkValues = Link.parse(header);
        assertEquals(expected, linkValues);
    }

    private static Stream<Arguments> parseLinkInOrder() {
        return Stream.of(
                Arguments.of("<https://one.example.com>; rel=\"preconnect\", " +
                    "<https://two.example.com>; rel=\"meta\", " +
                    "<https://three.example.com>; rel=\"stylesheet\"",
                Arrays.asList(
                    Link.of(URI.create("https://one.example.com"), REL_PRECONNECT),
                    Link.of(URI.create("https://two.example.com"), REL_META),
                    Link.of(URI.create("https://three.example.com"), REL_STYLESHEET)),
                Arguments.of("<https://two.example.com>; rel=\"meta\", " +
                    "<https://one.example.com>; rel=\"preconnect\"," +
                    "<https://three.example.com>; rel=\"stylesheet\"",
                Arrays.asList(
                    Link.of(URI.create("https://two.example.com"), REL_META),
                    Link.of(URI.create("https://one.example.com"), REL_PRECONNECT),
                    Link.of(URI.create("https://three.example.com"), REL_STYLESHEET))
            )));
    }

    @ParameterizedTest
    @MethodSource
    void parseIRIs(final String header, final List<Link> expected) {
        final List<Link> linkValues = Link.parse(header);
        assertEquals(expected, linkValues, "Unexpected handling of IRI values");
    }

    private static Stream<Arguments> parseIRIs() {
        return Stream.of(
                Arguments.of("<https://example.com/苗条>",
                    Arrays.asList(Link.of(URI.create("https://example.com/苗条"), Collections.emptyMap())))
                );
    }

    @ParameterizedTest
    @MethodSource
    void parseNonHttpUris(final String header, final List<Link> expected) {
        final List<Link> linkValues = Link.parse(header);
        assertEquals(expected, linkValues, "Unexpected handling of invalid link header");
    }

    private static Stream<Arguments> parseNonHttpUris() {
        return Stream.of(
                Arguments.of("<urn:example:6e8bc430-9c3a-11d9>",
                    Arrays.asList(Link.of(URI.create("urn:example:6e8bc430-9c3a-11d9"), Collections.emptyMap()))),
                Arguments.of("<did:web:resource.example/path>",
                    Arrays.asList(Link.of(URI.create("did:web:resource.example/path"), Collections.emptyMap()))),
                Arguments.of("<file:/path/to/file>",
                    Arrays.asList(Link.of(URI.create("file:/path/to/file"), Collections.emptyMap()))),
                Arguments.of("<file:///path/to/file>",
                    Arrays.asList(Link.of(URI.create("file:///path/to/file"), Collections.emptyMap())))
                );
    }

    @ParameterizedTest
    @MethodSource
    void ignoreInvalidParams(final String header, final List<Link> expected) {
        final List<Link> linkValues = Link.parse(header);
        assertEquals(expected, linkValues);
    }

    private static Stream<Arguments> ignoreInvalidParams() {
        return Stream.of(
                Arguments.of("<https://example.com>; rel==\"previous\"",
                    Arrays.asList(Link.of(URI.create("https://example.com"),
                            Collections.emptyMap()))),
                Arguments.of("<http://example.com>; type==text; rel=\"previous\";" +
                    "title=\"previous chapter\"",
                    Arrays.asList(Link.of(URI.create("http://example.com"),
                            REL_PREVIOUS))),
                Arguments.of("<http://example.com>; \"type\"=\"text\"; rel=\"previous\";" +
                    "title=\"previous chapter\"",
                    Arrays.asList(Link.of(URI.create("http://example.com"),
                            REL_PREVIOUS)))
                );
    }

    @ParameterizedTest
    @MethodSource
    void parseInvalidLinkHeader(final String header) {
        final List<Link> linkValues = Link.parse(header);
        final List<Link> empty = Collections.emptyList();
        assertEquals(empty, linkValues, "Unexpected handling of invalid link header");
    }


    private static Stream<String> parseInvalidLinkHeader() {
        return Stream.of(
                "https://bad.example",
                "rel=\"missingUri\"; type=\"text\"",
                "<https://example.com/{}>",
                "<https://example.com/^>",
                "<https://example.com/`>",
                "<https://example.com/|>",
                "<https://example.com/\t>",
                "<https://example.com/     >",
                "<https://example.com />",
                "<https://example.com/<>/test>",
                "<https://example.com/>/test>",
                "<https://example.com/>/test/>/test>",
                "<https://example.com/>/test/>/test");
    }
}
