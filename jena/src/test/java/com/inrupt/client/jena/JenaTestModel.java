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
package com.inrupt.client.jena;

import com.inrupt.client.rdf.RDFNode;

import java.net.URI;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

final class JenaTestModel {
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

    //JenaNode properties
    public static final Node S_NODE = NodeFactory.createURI(S_VALUE);
    public static final Node P_NODE = NodeFactory.createURI(P_VALUE);
    public static final Node O_NODE = NodeFactory.createLiteral(O_VALUE);
    public static final Node G_NODE = NodeFactory.createURI(G_VALUE);

    public static final Node S1_NODE = NodeFactory.createURI(S1_VALUE);
    public static final Node P1_NODE = NodeFactory.createURI(P1_VALUE);
    public static final Node O1_NODE = NodeFactory.createLiteral(O1_VALUE);

    public static final Node S2_NODE = NodeFactory.createURI(S2_VALUE);
    public static final Node P2_NODE = NodeFactory.createURI(P2_VALUE);
    public static final Node O2_NODE = NodeFactory.createLiteral(O2_VALUE);

    private JenaTestModel() {
        // Prevent instantiation
    }
}
