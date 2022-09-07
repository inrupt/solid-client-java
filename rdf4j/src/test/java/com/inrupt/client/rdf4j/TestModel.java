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
package com.inrupt.client.rdf4j;

import com.inrupt.client.rdf.RDFNode;

import java.net.URI;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

final class TestModel {

    private static final String TEST_NAMESPACE = "http://example.com/";

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

    public static final String S3_VALUE = TEST_NAMESPACE + "subject3";
    public static final String P3_VALUE = TEST_NAMESPACE + "predicate3";
    public static final String O3_VALUE = "object3";

    //RDFNode properties
    public static final RDFNode S_RDFNode = RDFNode.namedNode(URI.create(S_VALUE));
    public static final RDFNode P_RDFNode = RDFNode.namedNode(URI.create(P_VALUE));
    public static final RDFNode O_RDFNode = RDFNode.literal(O_VALUE);
    public static final RDFNode G_RDFNode = RDFNode.namedNode(URI.create(G_VALUE));

    public static final RDFNode S1_RDFNode = RDFNode.namedNode(URI.create(S1_VALUE));
    public static final RDFNode P1_RDFNode = RDFNode.namedNode(URI.create(P1_VALUE));
    public static final RDFNode O1_RDFNode = RDFNode.literal(O1_VALUE);

    public static final RDFNode S2_RDFNode = RDFNode.namedNode(URI.create(S2_VALUE));
    public static final RDFNode P2_RDFNode = RDFNode.namedNode(URI.create(P2_VALUE));
    public static final RDFNode O2_RDFNode = RDFNode.literal(O2_VALUE);

    public static final RDFNode S3_RDFNode = RDFNode.namedNode(URI.create(S3_VALUE));
    public static final RDFNode P3_RDFNode = RDFNode.namedNode(URI.create(P3_VALUE));
    public static final RDFNode O3_RDFNode = RDFNode.literal(O3_VALUE);

    //RDF4J properties
    public static final ValueFactory VF = SimpleValueFactory.getInstance();

    public static final Resource S_RDF4J = VF.createIRI(S_VALUE);
    public static final IRI P_RDF4J = VF.createIRI(P_VALUE);
    public static final Literal O_RDF4J = VF.createLiteral(O_VALUE);
    public static final Resource G_RDF4J = VF.createIRI(G_VALUE);

    public static final Resource S1_RDF4J = VF.createIRI(S1_VALUE);
    public static final IRI P1_RDF4J = VF.createIRI(P1_VALUE);
    public static final Literal O1_RDF4J = VF.createLiteral(O1_VALUE);

    public static final Resource S2_RDF4J = VF.createIRI(S2_VALUE);
    public static final IRI P2_RDF4J = VF.createIRI(P2_VALUE);
    public static final Literal O2_RDF4J = VF.createLiteral(O2_VALUE);

    public static final Resource S3_RDF4J = VF.createIRI(S3_VALUE);
    public static final IRI P3_RDF4J = VF.createIRI(P3_VALUE);
    public static final Literal O3_RDF4J = VF.createLiteral(O3_VALUE);

    private TestModel() {
        // Prevent instantiation
    }
}
