package com.inrupt.client.http;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class LinkHeaderParserTest {

    @Test
    void parseSingleLink() throws URISyntaxException {
        final var parser = new DefaultHeaderParser();
        final var header = "<https://example.com/%E8%8B%97%E6%9D%A1>; rel=\"preconnect\"";
        final var linkValues = parser.link(header);
        
        assertEquals(List.of(
                    new LinkValue("https://example.com/%E8%8B%97%E6%9D%A1", Map.of(
                            "rel", "preconnect"))), linkValues);
    }

    @ParameterizedTest
    @MethodSource
    void parseListedParams(final String header, final List<LinkValue> expected) {
        final var parser = new DefaultHeaderParser();
        final var linkValues = parser.link(header);
        assertEquals(expected, linkValues);
    }
    private static Stream<Arguments> parseListedParams() {
        return Stream.of(
                Arguments.of("<https://example.com/%E8%8B%97%E6%9D%A1>; rel=\"param1\" ; type=\"param2\"",
                    List.of(
                        new LinkValue("https://example.com/%E8%8B%97%E6%9D%A1", Map.of("rel", "param1", "type", "param2")))),

                Arguments.of("<https://example.com/%E8%8B%97%E6%9D%A1>; rel=\"param1\" ;type=\"param2\"",
                        List.of(
                            new LinkValue("https://example.com/%E8%8B%97%E6%9D%A1", Map.of("rel", "param1", "type", "param2")))),

                Arguments.of("<https://example.com/%E8%8B%97%E6%9D%A1>; rel=\"param1\"; type=\"param2\"",
                        List.of(
                            new LinkValue("https://example.com/%E8%8B%97%E6%9D%A1", Map.of("rel", "param1", "type", "param2")))),

                Arguments.of("<https://example.com/%E8%8B%97%E6%9D%A1>; rel=\"param1\";type=\"param2\"", // no Input missmatch error
                        List.of(
                            new LinkValue("https://example.com/%E8%8B%97%E6%9D%A1", Map.of("rel", "param1", "type", "param2")))));
                                    
    }

    @Test
    void parseListedParamsTestCase() throws URISyntaxException {
        final var parser = new DefaultHeaderParser();
        final var header = "<https://example.com/%E8%8B%97%E6%9D%A1>; rel=\"param1\" ;type=\"param2\"";
        final var linkValues = parser.link(header);
        
        assertEquals(List.of(
                    new LinkValue("https://example.com/%E8%8B%97%E6%9D%A1", Map.of(
                            "rel", "param1", "type", "param2"))), linkValues);
    }

    @ParameterizedTest
    @MethodSource
    void parseListedLinks(final String header, final List<LinkValue> expected) {
        final var parser = new DefaultHeaderParser();
        final var linkValues = parser.link(header);
        assertEquals(expected, linkValues);
    }
    private static Stream<Arguments> parseListedLinks() {
        return Stream.of(
                Arguments.of("<https://one.example.com>, <https://two.example.com>, <https://three.example.com>",
                    List.of(
                        new LinkValue("https://one.example.com"),
                        new LinkValue("https://two.example.com"),
                        new LinkValue("https://three.example.com"))),

                Arguments.of("<https://one.example.com>; rel=\"preconnect\" , <https://two.example.com>; rel=\"meta\" , <https://three.example.com>; rel=\"stylesheet\"",
                    List.of(
                        new LinkValue("https://one.example.com", Map.of(
                                "rel", "preconnect")),
                        new LinkValue("https://two.example.com", Map.of(
                                "rel", "meta")),
                        new LinkValue("https://three.example.com", Map.of(
                            "rel", "stylesheet")))),

                Arguments.of("<https://one.example.com>; rel=\"rel1\" ; type=\"type1\" , <https://two.example.com>; rel=\"rel2\" ; type=\"type2\" , <https://three.example.com>; rel=\"rel3\" ; type=\"type3\"" ,
                    List.of(
                        new LinkValue("https://one.example.com", Map.of(
                                "rel", "rel1", "type", "type1")),
                        new LinkValue("https://two.example.com", Map.of(
                            "rel", "rel2", "type", "type2")),
                        new LinkValue("https://three.example.com", Map.of(
                            "rel", "rel3", "type", "type3")))));
                                  
    }


    @ParameterizedTest
    @MethodSource
    void parseRelativeReferenceLink(final String header, final List<LinkValue> expected) {
        final var parser = new DefaultHeaderParser();
        final var linkValues = parser.link(header);
        assertEquals(expected, linkValues);
    }

    private static Stream<Arguments> parseRelativeReferenceLink() {
        return Stream.of(
                Arguments.of("<https://www.example.com/about/team/>",
                    List.of(
                        new LinkValue("https://www.example.com/about/team/"))),
                Arguments.of("</about/team/>",
                        List.of(
                            new LinkValue("/about/team/"))));
    }

    @ParameterizedTest
    @MethodSource
    void parseLinkParams(final String header, final List<LinkValue> expected) {
        final var parser = new DefaultHeaderParser();
        final var linkValues = parser.link(header);
        assertEquals(expected, linkValues);
    }

    private static Stream<Arguments> parseLinkParams() {
        return Stream.of(
                Arguments.of("<https://one.example.com>; rel=\"preconnect\", <https://two.example.com>; rel=\"meta\", <https://three.example.com>; rel=\"stylesheet\"",
                List.of(
                    new LinkValue("https://one.example.com", Map.of(
                            "rel", "preconnect")),
                    new LinkValue("https://two.example.com", Map.of(
                            "rel", "meta")),
                    new LinkValue("https://three.example.com", Map.of(
                        "rel", "stylesheet")))),
                Arguments.of("<https://two.example.com>; rel=\"meta\", <https://one.example.com>; rel=\"preconnect\", <https://three.example.com>; rel=\"stylesheet\"",
                List.of(
                    new LinkValue("https://two.example.com", Map.of(
                            "rel", "meta")),
                    new LinkValue("https://one.example.com", Map.of(
                            "rel", "preconnect")),
                    new LinkValue("https://three.example.com", Map.of(
                        "rel", "stylesheet"))))
            );
    }

    @ParameterizedTest
    @MethodSource
    void parseIgnoreInvalidParams(final String header, final List<LinkValue> expected) {
        final var parser = new DefaultHeaderParser();
        final var linkValues = parser.link(header);
        assertEquals(expected, linkValues);
    }

    private static Stream<Arguments> parseIgnoreInvalidParams() throws URISyntaxException {
        return Stream.of(
                Arguments.of("<http://example.com/TheBook/chapter2>; type==sdasd; rel=\"previous\"; title=\"previous chapter\"",
                    List.of(
                        new LinkValue("http://example.com/TheBook/chapter2", Map.of("rel", "previous", "title", "previous chapter"))))
            );
    }

    @ParameterizedTest
    @MethodSource
    void parseInvalidLinkHeader(final String header, final List<LinkValue> expected) {
        final var parser = new DefaultHeaderParser();
        final var linkValues = parser.link(header);
        assertEquals(expected, linkValues);
    }


    private static Stream<Arguments> parseInvalidLinkHeader() {
        return Stream.of(
                Arguments.of("https://bad.example", //Problem Here
                    Collections.emptyList()),
                Arguments.of("<https/badexample>", //Problem Here
                    Collections.emptyList()),
                Arguments.of("<notURI-Reference>",
                    Collections.emptyList()),    
                Arguments.of("<https://example.com>; rel==\"preconnect\"",
                    List.of(
                        new LinkValue("https://example.com"))),
                Arguments.of("rel=\"preconnect\"; rel=\"meta\"",
                    Collections.emptyList())
            );
    }

    
}
