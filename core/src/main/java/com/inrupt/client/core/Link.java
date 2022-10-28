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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
 * A class for representing an HTTP Link header.
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc8288">RFC 8288</a>
 */
public final class Link {

    private final URI uri;
    private final Map<String, String> parameters;

    private Link(final URI uri, final Map<String, String> parameters) {
        this.uri = Objects.requireNonNull(uri);
        this.parameters = Objects.requireNonNull(parameters);
    }

    public URI getUri() {
        return uri;
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
     * Get all the parameters for this Link.
     *
     * @return the complete collection of parameters
     */
    public Map<String, String> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }

    @Override
    public String toString() {
        if (parameters.isEmpty()) {
            return "<" + getUri() + ">";
        }
        return "<" + getUri() + ">; " + parameters.entrySet().stream()
            .map(e -> e.getKey() + "=\"" + e.getValue() + "\"")
            .collect(Collectors.joining("; "));
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof Link)) {
            return false;
        }

        final Link c = (Link) obj;

        if (!this.getUri().equals(c.getUri())) {
            return false;
        }

        return Objects.equals(new HashMap<>(parameters), new HashMap<>(c.parameters));
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getUri(), new HashMap<>(parameters));
    }

    /**
     * Create a new Link object with a specific URI-Reference and parameters.
     *
     * @param uri the link URI
     * @param parameters the link parameters
     * @return the new {@link Link} object
     */
    public static Link of(final URI uri, final Map<String, String> parameters) {
        return new Link(uri, parameters);
    }

    /**
     * A parser to convert link header string representations into a collection of links.
     *
     * @param headers the link headers
     * @return a list of links
     */
    public static List<Link> parse(final String... headers) {
        final Parser parser = new Parser();
        final List<Link> links = new ArrayList<>();
        for (final String header : headers) {
            links.addAll(parser.parse(header));
        }
        return links;
    }

    static final class Parser {
        private static final Logger LOGGER = LoggerFactory.getLogger(Link.class);

        private static final int PAIR = 2;
        private static final String DQUOTE = "\"";
        private static final String EQUALS = "=";

        private final ANTLRErrorListener errorListener;

        /**
         * Create a parser with a default error handler.
         */
        public Parser() {
            this(new ConsumerErrorListener(msg -> LOGGER.debug("Header parse error: {}", msg)));
        }

        /**
         * Create a parser with a custom error handler.
         *
         * @param errorListener the error handler
         */
        public Parser(final ANTLRErrorListener errorListener) {
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

            private boolean isBlank(final String value) {
                return (value == null || value.isEmpty() || value.trim().isEmpty());
            }
        }
    }
}
