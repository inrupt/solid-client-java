package com.inrupt.client.http;

import static org.junit.jupiter.api.Assertions.*;

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
    void parseSingleLink() {
        final var parser = new DefaultHeaderParser();
        final var header = "<https://example.com/%E8%8B%97%E6%9D%A1>; rel=\"preconnect\"";
        final var linkValues = parser.link(header);

        assertEquals(linkValues, List.of(
                    new LinkValue("https://example.com/%E8%8B%97%E6%9D%A1", Map.of(
                            "rel", "preconnect"))));
    }

    @Test
    void parseListedLinks() {
        final var parser = new DefaultHeaderParser();
        final var header = "<https://one.example.com>; rel=\"preconnect\", <https://two.example.com>; rel=\"meta\", <https://three.example.com>; rel=\"stylesheet\"";
        final var linkValues = parser.link(header);

        assertEquals(linkValues, List.of(
                    new LinkValue("https://one.example.com", Map.of(
                            "rel", "preconnect")),
                    new LinkValue("https://two.example.com", Map.of(
                            "rel", "meta")),
                    new LinkValue("https://three.example.com", Map.of(
                        "rel", "stylesheet"))));
    }

    @Test
    void parseRelativeReferenceLink() {
        final var parser = new DefaultHeaderParser();
        final var header = "</static/css/bootstrap.min.css>; rel=\"stylesheet\"";
        final var linkValues = parser.link(header);

        assertEquals(linkValues, List.of(
                    new LinkValue("/static/css/bootstrap.min.css", Map.of(
                            "rel", "stylesheet"))));
    }

    @ParameterizedTest
    @MethodSource
    void parseLinkParams(final String header, final List<LinkValue> expected) {
        final var parser = new DefaultHeaderParser();
        final var linkValues = parser.link(header);
        assertEquals(linkValues, expected);
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
        assertEquals(linkValues, expected);
    }

    private static Stream<Arguments> parseIgnoreInvalidParams() {
        return Stream.of(
                Arguments.of("<http://example.com/TheBook/chapter2>; type==sdasd rel=\"previous\"; title=\"previous chapter\"",
                    List.of(
                        new LinkValue("http://example.com/TheBook/chapter2", Map.of("rel", "previous", "title", "previous chapter"))))
            );
    }

    @ParameterizedTest
    @MethodSource
    void parseInvalidLinkHeader(final String header, final List<LinkValue> expected) {
        final var parser = new DefaultHeaderParser();
        final var linkValues = parser.link(header);
        assertEquals(linkValues, expected);
    }


    private static Stream<Arguments> parseInvalidLinkHeader() {
        return Stream.of(
                Arguments.of("https://bad.example", //Problem Here
                    Collections.emptyList()),
                Arguments.of("<https//bad.example>", //Problem Here
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
