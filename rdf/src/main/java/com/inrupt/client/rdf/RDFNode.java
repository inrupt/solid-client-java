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
package com.inrupt.client.rdf;

import java.net.URI;

/**
 * An RDF node.
 */
public final class RDFNode {

    private final URI uri;
    private final String lexicalValue;
    private final URI datatype;
    private final String language;

    /**
     * Indicate whether this is a named node.
     *
     * @return true if this is a named RDF node; false otherwise
     */
    public boolean isNamedNode() {
        return uri != null;
    }

    /**
     * Indicate whether this is a blank node.
     *
     * @return true if this is a blank node; false otherwise
     */
    public boolean isBlankNode() {
        return uri == null && lexicalValue == null;
    }

    /**
     * Indicate whether this is an RDF literal.
     *
     * @return true if this is an RDF literal; false otherwise
     */
    public boolean isLiteral() {
        return lexicalValue != null;
    }

    /**
     * Return the URI of this node.
     *
     * @return the URI associated with the node or {@code null}
     */
    public URI getURI() {
        return uri;
    }

    /**
     * Return the lexical value of this node.
     *
     * @return the lexical value associated with the node or {@code null}
     */
    public String getLiteral() {
        return lexicalValue;
    }

    /**
     * Return the datatype of this node.
     *
     * @return the datatype associated with the node or {@code null}
     */
    public URI getDatatype() {
        return datatype;
    }

    /**
     * Return the language tag for this node.
     *
     * @return the language tag assiciated with the node or {@code null}
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Create a new blank node.
     *
     * @return an RDF blank node
     */
    public static RDFNode blankNode() {
        return new RDFNode(null, null, null, null);
    }

    /**
     * Create a new named node.
     *
     * @param uri the node URI
     * @return the RDF named node
     */
    public static RDFNode namedNode(final URI uri) {
        return new RDFNode(uri, null, null, null);
    }

    /**
     * Create a new literal.
     *
     * @param literal the literal value
     * @return the RDF literal
     */
    public static RDFNode literal(final String literal) {
        return new RDFNode(null, literal, null, null);
    }

    /**
     * Create a new typed literal.
     *
     * @param literal the literal value
     * @param datatype the literal datatype
     * @return the RDF literal
     */
    public static RDFNode literal(final String literal, final URI datatype) {
        return new RDFNode(null, literal, null, datatype);
    }

    /**
     * Create a new literal with a language value.
     *
     * @param literal the literal value
     * @param language the literal language
     * @return the RDF literal
     */
    public static RDFNode literal(final String literal, final String language) {
        return new RDFNode(null, literal, language, null);
    }

    private RDFNode(final URI uri, final String literal, final String language, final URI datatype) {
        this.uri = uri;
        this.lexicalValue = literal;
        this.language = language;
        this.datatype = datatype;
    }
}
