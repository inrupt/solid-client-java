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
import com.inrupt.rdf.wrapping.commons.TermMappings;
import com.inrupt.rdf.wrapping.commons.ValueMappings;
import com.inrupt.rdf.wrapping.commons.WrapperIRI;

import java.net.URI;
import java.util.Set;

import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDFTerm;

/**
 * A Policy type for use with Access Control Policies.
 *
 * <p>A policy will reference various {@link Matcher} objects
 * and apply access rules such as {@code Read} or {@code Write}.
 */
public class Policy extends WrapperIRI {

    /**
     * Create a new Policy.
     *
     * @param identifier the policy identifier
     * @param graph the underlying graph for this resource
     */
    public Policy(final RDFTerm identifier, final Graph graph) {
        super(identifier, graph);
        graph.add((IRI) identifier, asIRI(RDF.type), asIRI(ACP.Policy));
    }

    /**
     * Retrieve the acp:allOf structures.
     *
     * @return a collection of {@link Matcher} objects
     */
    public Set<Matcher> allOf() {
        return objects(asIRI(ACP.allOf),
                Matcher::asResource, ValueMappings.as(Matcher.class));
    }

    /**
     * Retrieve the acp:anyOf structures.
     *
     * @return a collection of {@link Matcher} objects
     */
    public Set<Matcher> anyOf() {
        return objects(asIRI(ACP.anyOf),
                Matcher::asResource, ValueMappings.as(Matcher.class));
    }

    /**
     * Retrieve the acp:noneOf structures.
     *
     * @return a collection of {@link Matcher} objects
     */
    public Set<Matcher> noneOf() {
        return objects(asIRI(ACP.noneOf),
                Matcher::asResource, ValueMappings.as(Matcher.class));
    }

    /**
     * Retrieve the acp:allow values.
     *
     * @return a collection of access values, such as {@code ACL.Read}
     */
    public Set<URI> allow() {
        return objects(asIRI(ACP.allow),
                TermMappings::asIri, ValueMappings::iriAsUri);
    }

    static IRI asResource(final Policy policy, final Graph graph) {
        graph.add(policy, asIRI(RDF.type), asIRI(ACP.Policy));
        policy.allOf().forEach(matcher -> {
            graph.add(policy, asIRI(ACP.allOf), matcher);
            Matcher.asResource(matcher, graph);
        });
        policy.anyOf().forEach(matcher -> {
            graph.add(policy, asIRI(ACP.anyOf), matcher);
            Matcher.asResource(matcher, graph);
        });
        policy.noneOf().forEach(matcher -> {
            graph.add(policy, asIRI(ACP.noneOf), matcher);
            Matcher.asResource(matcher, graph);
        });
        policy.allow().forEach(allow ->
                graph.add(policy, asIRI(ACP.allow), asIRI(allow)));
        return policy;
    }
}

