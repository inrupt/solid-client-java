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
import com.inrupt.client.parser.WwwAuthenticateBaseListener;
import com.inrupt.client.parser.WwwAuthenticateLexer;
import com.inrupt.client.parser.WwwAuthenticateParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
public final class Challenge {

    private final String scheme;
    private final Map<String, String> parameters;

    private Challenge(final String scheme, final Map<String, String> parameters) {
        this.scheme = Objects.requireNonNull(scheme);
        this.parameters = Objects.requireNonNull(parameters);
    }

    /**
     * Get the authentication scheme for this challenge.
     *
     * @return the scheme name
     */
    public String getScheme() {
        return scheme;
    }

    /**
     * Get the value of the given parameter.
     *
     * @param parameter the parameter name
     * @return the parameter value, may be {@code null}
     */
    public String getParameter(final String parameter) {
        return parameters.get(parameter);
    }

    /**
     * Get all the parameters for this challenge.
     *
     * @return the complete collection of parameters
     */
    public Map<String, String> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }

    @Override
    public String toString() {
        return getScheme() + " " + parameters.entrySet().stream()
            .map(e -> e.getKey() + "=\"" + e.getValue() + "\"")
            .collect(Collectors.joining(", "));
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof Challenge)) {
            return false;
        }

        final Challenge c = (Challenge) obj;

        if (!scheme.toLowerCase(Locale.ENGLISH).equals(c.scheme.toLowerCase(Locale.ENGLISH))) {
            return false;
        }

        return Objects.equals(parameters, c.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scheme.toLowerCase(Locale.ENGLISH), parameters);
    }

    /**
     * Create a new Challenge object with a specific authentication scheme.
     *
     * @param scheme the authentication scheme
     * @return the challenge
     */
    public static Challenge of(final String scheme) {
        return of(scheme, Collections.emptyMap());
    }

    /**
     * Create a new Challenge object with a specific authentication scheme and parameters.
     *
     * @param scheme the authentication scheme
     * @param parameters the authentication parameters
     * @return the challenge
     */
    public static Challenge of(final String scheme, final Map<String, String> parameters) {
        return new Challenge(scheme, parameters);
    }

    /**
     * Parse header strings into a list of Challenge objects.
     *
     * @param headers the header strings
     * @return the challenge objects
     */
    public static List<Challenge> parse(final String... headers) {
        final Parser parser = new Parser();
        final List<Challenge> challenges = new ArrayList<>();
        for (final String header : headers) {
            challenges.addAll(parser.parse(header));
        }
        return challenges;
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
