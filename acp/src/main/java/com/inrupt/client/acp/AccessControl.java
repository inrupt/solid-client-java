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
package com.inrupt.client.acp;

import static com.inrupt.client.acp.AccessControlResource.asIRI;

import com.inrupt.client.vocabulary.ACP;
import com.inrupt.client.vocabulary.RDF;
import com.inrupt.rdf.wrapping.commons.ValueMappings;
import com.inrupt.rdf.wrapping.commons.WrapperIRI;

import java.util.Set;

import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDFTerm;

/**
 * An AccessControl type for use with Access Control Policies.
 *
 * <p>An access control applies {@link Policy} objects directly to a resource
 * via {@code acp:accessControl} or to container members via {@code acp:memberAccessControl}
 */
public class AccessControl extends WrapperIRI {

    /**
     * Create a new AccessControl.
     *
     * @param identifier the access control identifier
     * @param graph the underlying graph
     */
    public AccessControl(final RDFTerm identifier, final Graph graph) {
        super(identifier, graph);
        graph.add((IRI) identifier, asIRI(RDF.type), asIRI(ACP.AccessControl));
    }

    public Set<Policy> apply() {
        return objects(asIRI(ACP.apply), Policy::asResource, ValueMappings.as(Policy.class));
    }

    static IRI asResource(final AccessControl accessControl, final Graph graph) {
        graph.add(accessControl, asIRI(RDF.type), asIRI(ACP.AccessControl));
        accessControl.apply().forEach(policy -> {
            graph.add(accessControl, asIRI(ACP.apply), policy);
            Policy.asResource(policy, graph);
        });

        return accessControl;
    }
}

