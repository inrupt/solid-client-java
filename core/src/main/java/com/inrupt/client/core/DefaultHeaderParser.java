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
import com.inrupt.client.parser.LinkBaseListener;
import com.inrupt.client.parser.LinkLexer;
import com.inrupt.client.parser.LinkParser;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultHeaderParser implements HeaderParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(HeaderParser.class);

    private static final int PAIR = 2;
    private static final String DQUOTE = "\"";
    private static final String EQUALS = "=";

    private final ANTLRErrorListener errorListener;

    /**
     * Create a header parser with a default error handler.
     */
    public DefaultHeaderParser() {
        this(new ConsumerErrorListener(msg -> LOGGER.debug("Header parse error: {}", msg)));
    }

    /**
     * Create a header parser with a custom error handler.
     *
     * @param errorListener the error handler
     */
    public DefaultHeaderParser(final ANTLRErrorListener errorListener) {
        this.errorListener = errorListener;
    }

    @Override
    public List<Link> parseLinkHeaders(final String... headers) {
        final List<Link> links = new ArrayList<>();
        for (final String header : headers) {
            links.addAll(parseLinkHeader(header));
        }
        return links;
    }

    private List<Link> parseLinkHeader(final String header) {
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
            if (ctx.UriReference() != null && !ctx.UriReference().getText().isBlank()) {
                final Map<String, String> params = new HashMap<>();
                for (final TerminalNode p : ctx.LinkParam()) {
                    final String[] parts = p.getText().split(EQUALS, PAIR);
                    if (parts.length == PAIR) {
                        params.put(parts[0], unwrap(parts[1], DQUOTE));
                    }
                }
                links.add(Link.of(toURI(ctx.UriReference().getText()), params));
            }
        }


        static String unwrap(final String value, final String character) {
            if (value.startsWith(character) && value.endsWith(character)) {
                return value.substring(1, value.length() - 1);
            }
            return value;
        }

        static URI toURI(final String uri) {
            if (uri.startsWith("<") && uri.endsWith(">")) {
                return URI.create(uri.substring(1, uri.length() - 1));
            }
            return URI.create(uri);
        }
    }
}
