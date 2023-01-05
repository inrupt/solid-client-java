/*
 * Copyright 2023 Inrupt Inc.
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

import com.inrupt.client.test.RdfTestModel;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

final class JenaTestModel {

    //JenaNode properties
    public static final Node S_NODE = NodeFactory.createURI(RdfTestModel.S_VALUE);
    public static final Node P_NODE = NodeFactory.createURI(RdfTestModel.P_VALUE);
    public static final Node O_NODE = NodeFactory.createLiteral(RdfTestModel.O_VALUE);
    public static final Node G_NODE = NodeFactory.createURI(RdfTestModel.G_VALUE);

    public static final Node S1_NODE = NodeFactory.createURI(RdfTestModel.S1_VALUE);
    public static final Node P1_NODE = NodeFactory.createURI(RdfTestModel.P1_VALUE);
    public static final Node O1_NODE = NodeFactory.createLiteral(RdfTestModel.O1_VALUE);

    //Jena Resources
    public static Model model = ModelFactory.createDefaultModel();
    public static final Resource S_JENA = model.createResource(RdfTestModel.S_VALUE);
    public static final Literal O_JENA = model.createLiteral(RdfTestModel.O_VALUE);
    public static final Property P_JENA = model.createProperty(RdfTestModel.P_VALUE);

    public static final Resource S1_JENA = model.createResource(RdfTestModel.S1_VALUE);
    public static final Literal O1_JENA = model.createLiteral(RdfTestModel.O1_VALUE);


    private JenaTestModel() {
        // Prevent instantiation
    }
}
