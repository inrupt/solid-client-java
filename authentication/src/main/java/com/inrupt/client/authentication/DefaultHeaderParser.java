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

import com.inrupt.client.parser.DebugLoggingErrorListener;
import com.inrupt.client.parser.WwwAuthenticateBaseListener;
import com.inrupt.client.parser.WwwAuthenticateLexer;
import com.inrupt.client.parser.WwwAuthenticateParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

/**
 * A default header parser implementation.
 */
public class DefaultHeaderParser implements HeaderParser {

    private static int PAIR = 2;
    private static String DQUOTE = "\"";
    private static String EQUALS = "=";

    private final ANTLRErrorListener errorListener;

    /**
     * Create a header parser with an error listener that records syntax exceptions to a DEBUG log.
     */
    public DefaultHeaderParser() {
        this(DebugLoggingErrorListener.INSTANCE);
    }

    /**
     * Create a header parser with a custom error listener.
     *
     * @param errorListener the error listener
     */
    public DefaultHeaderParser(final ANTLRErrorListener errorListener) {
        this.errorListener = errorListener;
    }

    @Override
    public List<Challenge> wwwAuthenticate(final String header) {
        final var lexer = new WwwAuthenticateLexer(CharStreams.fromString(header));
        final var tokens = new CommonTokenStream(lexer);
        final var parser = new WwwAuthenticateParser(tokens);

        // Update error listeners
        if (errorListener != null) {
            parser.removeErrorListeners();
            parser.addErrorListener(errorListener);
        }

        final var tree = parser.wwwAuthenticate();

        final var walker = new ParseTreeWalker();
        final var listener = new ChallengeListener();
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
                final var params = new HashMap<String, String>();
                for (var p : ctx.AuthParam()) {
                    final var parts = p.getText().split(EQUALS, PAIR);
                    if (parts.length == PAIR) {
                        params.put(parts[0], unwrap(parts[1], DQUOTE));
                    }
                }
                challenges.add(new Challenge(ctx.AuthScheme().getText(), params));
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

