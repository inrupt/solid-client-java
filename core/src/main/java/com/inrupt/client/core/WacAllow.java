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

import com.inrupt.client.parser.ConsumerErrorListener;
import com.inrupt.client.parser.WacAllowBaseListener;
import com.inrupt.client.parser.WacAllowLexer;
import com.inrupt.client.parser.WacAllowParser;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;


import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * a class for parsing WAC-allow headers.
 *
 * @see <a href="https://solidproject.org/TR/wac#wac-allow">Solid TR: Web Access Control</a>
 */
public final class WacAllow {

    private final Map<String, Set<String>> accessParams;

    private WacAllow(final Map<String, Set<String>> accessParams) {
        this.accessParams = Objects.requireNonNull(accessParams);
    }

    /**
     * Create a new WAC-Allow object with a collection of Access Parameters.
     *
     * @param accessParams the Access Parameters
     * @return the WAC-Allow object
     */
    public static WacAllow of(final Map<String, Set<String>> accessParams) {
        return new WacAllow(accessParams);
    }

    /**
     * Get the Access Parameters associated with this HTTP WAC-Allow interaction.
     *
     * @return the  Access Parameters
     */
    public Map<String, Set<String>> getAccessParams() {
        return Collections.unmodifiableMap(accessParams);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof WacAllow)) {
            return false;
        }

        final WacAllow c = (WacAllow) obj;

        return Objects.equals(new HashMap<>(accessParams), new HashMap<>(c.accessParams));
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getAccessParams());
    }

    /**
     * Parse header strings into a WacAllow object.
     *
     * @param headers the header strings
     * @return WAC-Allow object containig Access Parameters from headers
     */
    public static WacAllow parse(final String... headers) {
        final Parser parser = new Parser();
        final Map<String, Set<String>> accessParams = new HashMap<>();

        for (final String header : headers) {
            final Map<String, Set<String>> accessParamEntry = parser.parse(header);

            for (Entry<String, Set<String>> entry : accessParamEntry.entrySet()) {
                accessParams.computeIfAbsent(entry.getKey(), k -> new HashSet<>())
                            .addAll(entry.getValue());
            }
        }
        return WacAllow.of(accessParams);
    }

    static final class Parser {

        private static final Logger LOGGER = LoggerFactory.getLogger(WacAllow.class);

        private static final int PAIR = 2;
        private static final String DQUOTE = "\"";
        private static final String EQUALS = "=";
        private static final String WS = " ";

        private final ANTLRErrorListener errorListener;

        /**
         * Create an access parameter parser with an error listener that records syntax exceptions to a DEBUG log.
         */
        public Parser() {
            this(new ConsumerErrorListener(msg -> LOGGER.debug("WAC-Allow parse error: {}", msg)));
        }

        /**
         * Create an access parameter parser with a custom error listener.
         *
         * @param errorListener the error listener
         */
        public Parser(final ANTLRErrorListener errorListener) {
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
                    final String[] parts = p.getText().split(EQUALS, PAIR);
                    if (parts.length == PAIR) {
                        final String[] accessModes = Arrays.stream(unwrap(parts[1], DQUOTE).split(WS))
                                                            .filter(s -> !s.isEmpty()).toArray(String[]::new);
                        if (accessModes.length > 0) {
                            accessParams.computeIfAbsent(parts[0], k -> new HashSet<>())
                                        .addAll(Set.of(accessModes));
                        }
                    }
                }
            }

            static String unwrap(final String value, final String character) {

                if (value.startsWith(character) && value.endsWith(character)) {
                    return value.substring(1, value.length() - 1);
                }
                return value;
            }
        }
    }
}
