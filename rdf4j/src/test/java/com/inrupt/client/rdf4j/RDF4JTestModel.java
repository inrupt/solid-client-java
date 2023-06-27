/*
 * Copyright Inrupt Inc.
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

import com.inrupt.client.test.RdfTestModel;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

final class RDF4JTestModel {

    //RDF4J properties
    public static final ValueFactory VF = SimpleValueFactory.getInstance();

    public static final Resource S_RDF4J = VF.createIRI(RdfTestModel.S_VALUE);
    public static final IRI P_RDF4J = VF.createIRI(RdfTestModel.P_VALUE);
    public static final Literal O_RDF4J = VF.createLiteral(RdfTestModel.O_VALUE);
    public static final Resource G_RDF4J = VF.createIRI(RdfTestModel.G_VALUE);

    public static final Resource S1_RDF4J = VF.createIRI(RdfTestModel.S1_VALUE);
    public static final IRI P1_RDF4J = VF.createIRI(RdfTestModel.P1_VALUE);
    public static final Literal O1_RDF4J = VF.createLiteral(RdfTestModel.O1_VALUE);

    public static final Resource S2_RDF4J = VF.createIRI(RdfTestModel.S2_VALUE);
    public static final IRI P2_RDF4J = VF.createIRI(RdfTestModel.P2_VALUE);
    public static final Literal O2_RDF4J = VF.createLiteral(RdfTestModel.O2_VALUE);

    private RDF4JTestModel() {
        // Prevent instantiation
    }
}
