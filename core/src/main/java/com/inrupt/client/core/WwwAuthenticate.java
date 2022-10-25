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

import com.inrupt.client.Authenticator.Challenge;
import com.inrupt.client.parser.ConsumerErrorListener;
import com.inrupt.client.parser.WwwAuthenticateBaseListener;
import com.inrupt.client.parser.WwwAuthenticateLexer;
import com.inrupt.client.parser.WwwAuthenticateParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Part of the HTTP Challenge and Response authentication framework, this class represents a
 * challenge object as represented in a WWW-Authenticate Response Header.
 *
 * @see <a href="https://httpwg.org/specs/rfc7235.html#challenge.and.response">RFC 7235 2.1</a>
 */
public final class WwwAuthenticate {

    private final List<Challenge> challenges;

    private WwwAuthenticate(final List<Challenge> challenges) {
        this.challenges = Objects.requireNonNull(challenges);
    }

    /**
     * Get the challenges associated with this HTTP authentication interaction.
     *
     * @return the challenges
     */
    public List<Challenge> getChallenges() {
        return Collections.unmodifiableList(challenges);
    }

    /**
     * Create a new WWW-Authenticate object with a collection of challenges.
     *
     * @param challenges the challenges
     * @return the www-authenticate object
     */
    public static WwwAuthenticate of(final Challenge... challenges) {
        return of(Arrays.asList(challenges));
    }

    /**
     * Create a new WWW-Authenticate object with a collection of challenges.
     *
     * @param challenges the challenges
     * @return the www-authenticate object
     */
    public static WwwAuthenticate of(final List<Challenge> challenges) {
        return new WwwAuthenticate(challenges);
    }

    /**
     * Parse header strings into a list of Challenge objects.
     *
     * @param headers the header strings
     * @return the challenge objects
     */
    public static WwwAuthenticate parse(final String... headers) {
        final Parser parser = new Parser();
        final List<Challenge> challenges = new ArrayList<>();
        for (final String header : headers) {
            challenges.addAll(parser.parse(header));
        }
        return WwwAuthenticate.of(challenges);
    }

    static final class Parser {

        private static final Logger LOGGER = LoggerFactory.getLogger(Challenge.class);
        private static final int PAIR = 2;
        private static final String DQUOTE = "\"";
        private static final String EQUALS = "=";

        private final ANTLRErrorListener errorListener;

        /**
         * Create a challenge parser with an error listener that records syntax exceptions to a DEBUG log.
         */
        public Parser() {
            this(new ConsumerErrorListener(msg -> LOGGER.debug("WWW-Authenticate parse error: {}", msg)));
        }

        /**
         * Create a challenge parser with a custom error listener.
         *
         * @param errorListener the error listener
         */
        public Parser(final ANTLRErrorListener errorListener) {
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
                        final String[] parts = p.getText().split(EQUALS, PAIR);
                        if (parts.length == PAIR) {
                            params.put(parts[0], unwrap(parts[1], DQUOTE));
                        }
                    }
                    challenges.add(Challenge.of(ctx.AuthScheme().getText(), params));
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
