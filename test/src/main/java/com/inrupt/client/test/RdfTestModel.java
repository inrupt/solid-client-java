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
package com.inrupt.client.test;

import com.inrupt.client.spi.RDFFactory;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Literal;
import org.apache.commons.rdf.api.RDF;

public final class RdfTestModel {

    private static final RDF rdf = RDFFactory.instance();

    public static final String TEST_NAMESPACE = "http://example.test/";

    public static final String S_VALUE = TEST_NAMESPACE + "subject";
    public static final String P_VALUE = TEST_NAMESPACE + "predicate";
    public static final String O_VALUE = "object";
    public static final String G_VALUE = TEST_NAMESPACE + "graph";

    public static final String S1_VALUE = TEST_NAMESPACE + "subject1";
    public static final String P1_VALUE = TEST_NAMESPACE + "predicate1";
    public static final String O1_VALUE = "object1";

    public static final String S2_VALUE = TEST_NAMESPACE + "subject2";
    public static final String P2_VALUE = TEST_NAMESPACE + "predicate2";
    public static final String O2_VALUE = "object2";

    //RDFNode properties
    public static final IRI S_RDFNode = rdf.createIRI(S_VALUE);
    public static final IRI P_RDFNode = rdf.createIRI(P_VALUE);
    public static final Literal O_RDFNode = rdf.createLiteral(O_VALUE);
    public static final IRI G_RDFNode = rdf.createIRI(G_VALUE);

    public static final IRI S1_RDFNode = rdf.createIRI(S1_VALUE);
    public static final IRI P1_RDFNode = rdf.createIRI(P1_VALUE);
    public static final Literal O1_RDFNode = rdf.createLiteral(O1_VALUE);

    public static final IRI S2_RDFNode = rdf.createIRI(S2_VALUE);
    public static final IRI P2_RDFNode = rdf.createIRI(P2_VALUE);
    public static final Literal O2_RDFNode = rdf.createLiteral(O2_VALUE);

    private RdfTestModel() {
        // Prevent instantiation
    }
}
