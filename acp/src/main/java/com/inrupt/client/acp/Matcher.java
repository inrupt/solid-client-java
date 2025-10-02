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
 * A Matcher type for use with Access Control Policies.
 *
 * <p>A matcher is associated with {@link Policy} objects, defining
 * agents, clients, issuers or verifiable credential (vc) types.
 */
public class Matcher extends WrapperIRI {

    /**
     * Create a new Matcher.
     *
     * @param identifier the matcher identifier
     * @param graph the underlying graph
     */
    public Matcher(final RDFTerm identifier, final Graph graph) {
        super(identifier, graph);
        graph.add((IRI) identifier, asIRI(RDF.type), asIRI(ACP.Matcher));
    }


    /**
     * Retrieve the acp:vc values.
     *
     * @return a collection of verifiable credential types
     */
    public Set<URI> vc() {
        return objects(asIRI(ACP.vc), TermMappings::asIri, ValueMappings::iriAsUri);
    }

    /**
     * Retrieve the acp:agent values.
     *
     * @return a collection of agent identifiers
     */
    public Set<URI> agent() {
        return objects(asIRI(ACP.agent), TermMappings::asIri, ValueMappings::iriAsUri);
    }

    /**
     * Retrieve the acp:client values.
     *
     * @return a collection of client identifiers
     */
    public Set<URI> client() {
        return objects(asIRI(ACP.client), TermMappings::asIri, ValueMappings::iriAsUri);
    }

    /**
     * Retrieve the acp:issuer values.
     *
     * @return a collection of issuer identifiers
     */
    public Set<URI> issuer() {
        return objects(asIRI(ACP.issuer), TermMappings::asIri, ValueMappings::iriAsUri);
    }

    static RDFTerm asResource(final Matcher matcher, final Graph graph) {
        graph.add(matcher, asIRI(RDF.type), asIRI(ACP.Matcher));
        matcher.vc().forEach(vc ->
                graph.add(matcher, asIRI(ACP.vc), asIRI(vc)));
        matcher.agent().forEach(agent ->
                graph.add(matcher, asIRI(ACP.agent), asIRI(agent)));
        matcher.client().forEach(client ->
                graph.add(matcher, asIRI(ACP.client), asIRI(client)));
        matcher.issuer().forEach(issuer ->
                graph.add(matcher, asIRI(ACP.issuer), asIRI(issuer)));
        return matcher;
    }
}

