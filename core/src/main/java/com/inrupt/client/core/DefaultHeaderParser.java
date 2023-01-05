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
package com.inrupt.client.core;

import com.inrupt.client.Headers.Link;
import com.inrupt.client.Headers.WacAllow;
import com.inrupt.client.Headers.WwwAuthenticate;
import com.inrupt.client.auth.Challenge;
import com.inrupt.client.parser.ConsumerErrorListener;
import com.inrupt.client.parser.LinkBaseListener;
import com.inrupt.client.parser.LinkLexer;
import com.inrupt.client.parser.LinkParser;
import com.inrupt.client.parser.WacAllowBaseListener;
import com.inrupt.client.parser.WacAllowLexer;
import com.inrupt.client.parser.WacAllowParser;
import com.inrupt.client.parser.WwwAuthenticateBaseListener;
import com.inrupt.client.parser.WwwAuthenticateLexer;
import com.inrupt.client.parser.WwwAuthenticateParser;
import com.inrupt.client.spi.HeaderParser;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DefaultHeaderParser implements HeaderParser {

    @Override
    public List<Link> parseLink(final List<String> headers) {
        Objects.requireNonNull(headers, "Header values may not be null!");
        final LinkHeaderParser parser = new LinkHeaderParser();
        final List<Link> links = new ArrayList<>();
        for (final String header : headers) {
            links.addAll(parser.parse(header));
        }
        return links;
    }

    @Override
    public WwwAuthenticate parseWwwAuthenticate(final List<String> headers) {
        final WwwAuthenticateHeaderParser parser = new WwwAuthenticateHeaderParser();
        final List<Challenge> challenges = new ArrayList<>();
        for (final String header : headers) {
            challenges.addAll(parser.parse(header));
        }
        return WwwAuthenticate.of(challenges);
    }

    @Override
    public WacAllow parseWacAllow(final List<String> headers) {
        final WacAllowHeaderParser parser = new WacAllowHeaderParser();
        final Map<String, Set<String>> accessParams = new HashMap<>();

        for (final String header : headers) {
            final Map<String, Set<String>> accessParamEntry = parser.parse(header);

            for (Map.Entry<String, Set<String>> entry : accessParamEntry.entrySet()) {
                accessParams.computeIfAbsent(entry.getKey(), k -> new HashSet<>())
                            .addAll(entry.getValue());
            }
        }
        return WacAllow.of(accessParams);
    }

    static final class Util {
        static final int PAIR = 2;
        static final String DQUOTE = "\"";
        static final String EQUALS = "=";
        static final String WS = " ";

        static String unwrap(final String value, final String character) {
            if (value.startsWith(character) && value.endsWith(character)) {
                return value.substring(1, value.length() - 1);
            }
            return value;
        }

        private Util() {
            // Prevent instantiation
        }
    }

    static final class LinkHeaderParser {
        private static final Logger LOGGER = LoggerFactory.getLogger(LinkHeaderParser.class);

        private final ANTLRErrorListener errorListener;

        /**
         * Create a parser with a default error handler.
         */
        public LinkHeaderParser() {
            this(new ConsumerErrorListener(msg -> LOGGER.debug("Header parse error: {}", msg)));
        }

        /**
         * Create a parser with a custom error handler.
         *
         * @param errorListener the error handler
         */
        public LinkHeaderParser(final ANTLRErrorListener errorListener) {
            this.errorListener = errorListener;
        }


        /**
         * Parse a link header.
         *
         * @param header the link header string
         * @return a list of links
         */
        public List<Link> parse(final String header) {
            final LinkLexer lexer = new LinkLexer(CharStreams.fromString(header));
            // Update lexer error listeners
            if (errorListener != null) {
                lexer.removeErrorListeners();
                lexer.addErrorListener(errorListener);
            }

            final CommonTokenStream tokens = new CommonTokenStream(lexer);
            final LinkParser parser = new LinkParser(tokens);

            // Update parser error listeners
            if (errorListener != null) {
                parser.removeErrorListeners();
                parser.addErrorListener(errorListener);
            }

            final LinkListener listener = new LinkListener();
            final ParseTreeWalker walker = new ParseTreeWalker();
            final LinkParser.LinkHeaderContext tree = parser.linkHeader();

            try {
                walker.walk(listener, tree);
            } catch (final IllegalArgumentException ex) {
                parser.notifyErrorListeners("Link parsing error: " + ex.getMessage());
            }

            return listener.getLinks();
        }

        /**
         * An Antlr listener for use with walking over the parsed syntax tree of a Link header.
         */
        static class LinkListener extends LinkBaseListener {

            private final List<Link> links = new ArrayList<>();

            /**
             * Get the list of Challenge objects from the parsed header.
             *
             * @return the challenges
             */
            public List<Link> getLinks() {
                return links;
            }

            @Override
            public void exitLink(final LinkParser.LinkContext ctx) {
                if (ctx.UriReference() != null && !isBlank(ctx.UriReference().getText())) {
                    final Map<String, String> params = new HashMap<>();
                    for (final TerminalNode p : ctx.LinkParam()) {
                        final String[] parts = p.getText().split(Util.EQUALS, Util.PAIR);
                        if (parts.length == Util.PAIR) {
                            params.put(parts[0], Util.unwrap(parts[1], Util.DQUOTE));
                        }
                    }
                    links.add(Link.of(toURI(ctx.UriReference().getText()), params));
                }
            }


            static URI toURI(final String uri) {
                if (uri.startsWith("<") && uri.endsWith(">")) {
                    return URI.create(uri.substring(1, uri.length() - 1));
                }
                return URI.create(uri);
            }

            private boolean isBlank(final String value) {
                return (value == null || value.isEmpty() || value.trim().isEmpty());
            }
        }
    }

    static final class WwwAuthenticateHeaderParser {

        private static final Logger LOGGER = LoggerFactory.getLogger(WwwAuthenticateHeaderParser.class);

        private final ANTLRErrorListener errorListener;

        /**
         * Create a challenge parser with an error listener that records syntax exceptions to a DEBUG log.
         */
        public WwwAuthenticateHeaderParser() {
            this(new ConsumerErrorListener(msg -> LOGGER.debug("WWW-Authenticate parse error: {}", msg)));
        }

        /**
         * Create a challenge parser with a custom error listener.
         *
         * @param errorListener the error listener
         */
        public WwwAuthenticateHeaderParser(final ANTLRErrorListener errorListener) {
            this.errorListener = errorListener;
        }

        public List<Challenge> parse(final String header) {
            final WwwAuthenticateLexer lexer = new WwwAuthenticateLexer(CharStreams.fromString(header));
            // Update lexer error listeners
            if (errorListener != null) {
                lexer.removeErrorListeners();
                lexer.addErrorListener(errorListener);
            }

            final CommonTokenStream tokens = new CommonTokenStream(lexer);
            final WwwAuthenticateParser parser = new WwwAuthenticateParser(tokens);

            // Update error listeners
            if (errorListener != null) {
                parser.removeErrorListeners();
                parser.addErrorListener(errorListener);
            }


            final ChallengeListener listener = new ChallengeListener();
            final ParseTreeWalker walker = new ParseTreeWalker();
            final WwwAuthenticateParser.WwwAuthenticateContext tree = parser.wwwAuthenticate();

            walker.walk(listener, tree);

            return listener.getChallenges();
        }

        /**
         * An Antlr listener for use with walking over the parsed syntax tree of a WWW-Authenticate header.
         */
        public static class ChallengeListener extends WwwAuthenticateBaseListener {

            private final List<Challenge> challenges = new ArrayList<>();

            /**
             * Get the list of Challenge objects from the parsed header.
             *
             * @return the challenges
             */
            public List<Challenge> getChallenges() {
                return challenges;
            }

            @Override
            public void exitChallenge(final WwwAuthenticateParser.ChallengeContext ctx) {
                if (ctx.AuthScheme() != null) {
                    final Map<String, String> params = new HashMap<>();
                    for (final TerminalNode p : ctx.AuthParam()) {
                        final String[] parts = p.getText().split(Util.EQUALS, Util.PAIR);
                        if (parts.length == Util.PAIR) {
                            params.put(parts[0], Util.unwrap(parts[1], Util.DQUOTE));
                        }
                    }
                    challenges.add(Challenge.of(ctx.AuthScheme().getText(), params));
                }
            }
        }
    }

    static final class WacAllowHeaderParser {

        private static final Logger LOGGER = LoggerFactory.getLogger(WacAllowHeaderParser.class);


        private final ANTLRErrorListener errorListener;

        /**
         * Create an access parameter parser with an error listener that records syntax exceptions to a DEBUG log.
         */
        public WacAllowHeaderParser() {
            this(new ConsumerErrorListener(msg -> LOGGER.debug("WAC-Allow parse error: {}", msg)));
        }

        /**
         * Create an access parameter parser with a custom error listener.
         *
         * @param errorListener the error listener
         */
        public WacAllowHeaderParser(final ANTLRErrorListener errorListener) {
            this.errorListener = errorListener;
        }

        /**
         * Parse header string into a list of AccessParam objects.
         *
         * @param header the header string
         * @return list of AccessParam objects
         */
        public Map<String, Set<String>> parse(final String header) {
            final WacAllowLexer lexer = new WacAllowLexer(CharStreams.fromString(header));
            // Update lexer error listeners
            if (errorListener != null) {
                lexer.removeErrorListeners();
                lexer.addErrorListener(errorListener);
            }

            final CommonTokenStream tokens = new CommonTokenStream(lexer);
            final WacAllowParser parser = new WacAllowParser(tokens);

            // Update error listeners
            if (errorListener != null) {
                parser.removeErrorListeners();
                parser.addErrorListener(errorListener);
            }

            final AccessParamListener listener = new AccessParamListener();
            final ParseTreeWalker walker = new ParseTreeWalker();
            final WacAllowParser.WacAllowContext tree = parser.wacAllow();

            walker.walk(listener, tree);

            return listener.getAccessParams();
        }

        /**
         * An Antlr listener for use with walking over the parsed syntax tree of a WWW-Authenticate header.
         */
        public static class AccessParamListener extends WacAllowBaseListener {

            private final Map<String, Set<String>> accessParams = new HashMap<>();

            /**
             * Get the list of AccesParam objects from the parsed header.
             *
             * @return list of AccessParams
             */
            public Map<String, Set<String>> getAccessParams() {
                return accessParams;
            }

            @Override
            public void exitWacAllow(final WacAllowParser.WacAllowContext ctx) {
                for (final TerminalNode p : ctx.AccessParam()) {
                    final String[] parts = p.getText().split(Util.EQUALS, Util.PAIR);
                    if (parts.length == Util.PAIR) {
                        final String[] accessModes = Arrays.stream(Util.unwrap(parts[1], Util.DQUOTE).split(Util.WS))
                                                            .filter(s -> !s.isEmpty())
                                                            .toArray(String[]::new);
                        if (accessModes.length > 0) {
                            accessParams.computeIfAbsent(parts[0], k -> new HashSet<>())
                                        .addAll(Arrays.asList(accessModes));
                        }
                    }
                }
            }
        }
    }
}
