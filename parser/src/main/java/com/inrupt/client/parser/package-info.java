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
/**
 * <h2>Parsing support for the Inrupt client libraries</h2>
 *
 * <p>The Solid ecosystem relies on conformance with many different formal specifications.
 * Consequently, many of these specifications define formal grammars as part of a protocol
 * definition. For HTTP interactions, this principally consists of header definitions.
 *
 * <p>This module defines a formal grammar for different HTTP headers along with generated
 * lexer and parser classes for use with processing these headers in a specification-compliant
 * way.
 *
 * <p>The grammars in this module make use of the <a href="https://www.antlr.org/">ANTLR</a> tooling.
 * From these defined grammars, Java classes are automatically generated. Convenience methods are provided
 * at higher levels to make this parsing simple, but it is also possible to use these classes directly.
 *
 * <p>For example, parsing a {@code WWW-Authenticate} header might take this form in code:
 *
 * <pre>{@code
    WwwAuthenticateLexer lexer = new WwwAuthenticateLexer(CharStreams.fromString(header));
    WwwAuthenticateParser parser = new WwwAuthenticateParser(new CommonTokenStream(lexer));
    List<Challenge> tree = parser.wwwAuthenticate();

    System.out.println(tree.toStringTree(parser));
 * }</pre>
 *
 * <p>Alternatively, this is an example using a
 * <a href="https://github.com/antlr/antlr4/blob/master/doc/listeners.md">custom listener</a>:
 * <pre>{@code
    WwwAuthenticateLexer lexer = new WwwAuthenticateLexer(CharStreams.fromString(header));
    WwwAuthenticateParser parser = new WwwAuthenticateParser(new CommonTokenStream(lexer));
    List<Challenge> tree = parser.wwwAuthenticate();

    ParseTreeWalker walker = new ParseTreeWalker();
    MyCustomListener listener = new MyCustomListener();
    walker.walk(listener, tree);
 *  }</pre>
 *
 */
package com.inrupt.client.parser;
