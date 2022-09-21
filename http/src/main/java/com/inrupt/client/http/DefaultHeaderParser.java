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

import com.inrupt.client.parser.ConsumerErrorListener;
import com.inrupt.client.parser.LinkBaseListener;
import com.inrupt.client.parser.LinkLexer;
import com.inrupt.client.parser.LinkParser;
import com.inrupt.client.parser.WwwAuthenticateBaseListener;
import com.inrupt.client.parser.WwwAuthenticateLexer;
import com.inrupt.client.parser.WwwAuthenticateParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultHeaderParser implements HeaderParser{

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultHeaderParser.class);

    private static int PAIR = 2;
    private static String DQUOTE = "\"";
    private static String EQUALS = "=";

    private final ANTLRErrorListener errorListener;

    /**
     * Create a header parser with an error listener that records syntax exceptions to a DEBUG log.
     */

    public DefaultHeaderParser() {
       this(new ConsumerErrorListener(msg -> LOGGER.debug("Header parse error: {}", msg)));
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
    public List<LinkValue> link(String header) {
        final var lexer = new LinkLexer(CharStreams.fromString(header));
        final var tokens = new CommonTokenStream(lexer);
        final var parser = new LinkParser(tokens);

        // Update error listeners
        if (errorListener != null) {
            parser.removeErrorListeners();
            parser.addErrorListener(errorListener);
        }

        final var tree = parser.link();
        final var walker = new ParseTreeWalker();
        final var listener = new LinkValueListener();
        walker.walk(listener, tree);

        return listener.getLinkValues();
    }

    /**
     * An Antlr listener for use with walking over the parsed syntax tree of a Link header.
     */
    public static class LinkValueListener extends LinkBaseListener {

        private final List<LinkValue> linkValues = new ArrayList<>();

        /**
         * Get the list of Challenge objects from the parsed header.
         *
         * @return the challenges
         */
        public List<LinkValue> getLinkValues() {
            return linkValues;
        }

        @Override
        public void exitLinkValue(final LinkParser.LinkValueContext ctx) {
            if (ctx.UriReference() != null) {
                final var params = new HashMap<String, String>();
                for (var p : ctx.LinkParam()) {
                    final var parts = p.getText().split(EQUALS, PAIR);
                    if (parts.length == PAIR) {
                        params.put(parts[0], unwrap(parts[1], DQUOTE));
                    }
                }
                linkValues.add(new LinkValue(ctx.UriReference().getText(), params));
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


    

