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
package com.inrupt.client;

/**
 * The names of concrete RDF Syntaxes.
 */
public enum Syntax {

    /**
     * Turtle.
     *
     * @see <a href="https://www.w3.org/TR/turtle/">RDF 1.1 Turtle</a>
     */
    TURTLE,

    /**
     * N-Triples.
     *
     * @see <a href="https://www.w3.org/TR/n-triples/">RDF 1.1 N-Triples</a>
     */
    NTRIPLES,

    /**
     * TriG.
     *
     * @see <a href="https://www.w3.org/TR/trig/">RDF 1.1 TriG</a>
     */
    TRIG,

    /**
     * N-Quads.
     *
     * @see <a href="https://www.w3.org/TR/n-quads/">RDF 1.1 N-Quads</a>
     */
    NQUADS,

    /**
     * JSON-LD.
     *
     * @see <a href="https://www.w3.org/TR/json-ld/">JSON-LD 1.1</a>
     */
    JSONLD;

}
