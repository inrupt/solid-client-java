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

import com.inrupt.client.RDFSource;
import com.inrupt.client.vocabulary.ACP;
import com.inrupt.client.vocabulary.RDF;
import com.inrupt.rdf.wrapping.commons.ValueMappings;
import com.inrupt.rdf.wrapping.commons.WrapperIRI;

import java.net.URI;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import org.apache.commons.rdf.api.Dataset;
import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.RDFTerm;

/**
 * An Access Control Resource type.
 */
public class AccessControlResource extends RDFSource {

    private static final URI SOLID_ACCESS_GRANT = URI.create("http://www.w3.org/ns/solid/vc#SolidAccessGrant");

    public AccessControlResource(final URI identifier, final Dataset dataset) {
        super(identifier, dataset);
        dataset.add(null, rdf.createIRI(identifier.toString()), rdf.createIRI(RDF.type.toString()),
                rdf.createIRI(ACP.AccessControlResource.toString()));
    }

    public Set<AccessControl> accessControl() {
        return new ACPNode(rdf.createIRI(getIdentifier().toString()), getGraph()).accessControl();
    }

    public Set<AccessControl> memberAccessControl() {
        return new ACPNode(rdf.createIRI(getIdentifier().toString()), getGraph()).memberAccessControl();
    }

    static class ACPNode extends WrapperIRI {
        public ACPNode(final RDFTerm original, final Graph graph) {
            super(original, graph);
        }

        public Set<AccessControl> memberAccessControl() {
            return objects(rdf.createIRI(ACP.memberAccessControl.toString()),
                    AccessControl::asResource, ValueMappings.as(AccessControl.class));
        }

        public Set<AccessControl> accessControl() {
            return objects(rdf.createIRI(ACP.accessControl.toString()),
                    AccessControl::asResource, ValueMappings.as(AccessControl.class));
        }
    }

    public AccessControl accessControl(final Policy... policies) {
        final var baseUri = getIdentifier().getScheme() + ":" + getIdentifier().getSchemeSpecificPart();
        final var ac = new AccessControl(rdf.createIRI(baseUri + "#" + UUID.randomUUID()), getGraph());
        for (final var policy : policies) {
            ac.apply().add(policy);
        }
        return ac;
    }

    public Policy authenticatedAgentPolicy(final URI... access) {
        return agentPolicy(ACP.AuthenticatedAgent, access);
    }

    public Policy anyAgentPolicy(final URI... access) {
        return agentPolicy(ACP.PublicAgent, access);
    }

    public Policy anyClientPolicy(final URI... access) {
        return clientPolicy(ACP.PublicClient, access);
    }

    public Policy anyIssuerPolicy(final URI... access) {
        return issuerPolicy(ACP.PublicIssuer, access);
    }

    public Policy accessGrantsPolicy(final URI... access) {
        return simplePolicy(matcher -> matcher.vc().add(SOLID_ACCESS_GRANT), access);
    }

    public Policy agentPolicy(final URI agent, final URI... access) {
        return simplePolicy(matcher -> matcher.agent().add(agent), access);
    }

    public Policy clientPolicy(final URI client, final URI... access) {
        return simplePolicy(matcher -> matcher.client().add(client), access);
    }

    public Policy issuerPolicy(final URI issuer, final URI... access) {
        return simplePolicy(matcher -> matcher.issuer().add(issuer), access);
    }

    Policy simplePolicy(final Consumer<Matcher> handler, final URI... access) {
        final var baseUri = getIdentifier().getScheme() + ":" + getIdentifier().getSchemeSpecificPart();
        final var matcher = new Matcher(rdf.createIRI(baseUri + "#" + UUID.randomUUID()), getGraph());
        handler.accept(matcher);

        final var policy = new Policy(rdf.createIRI(baseUri + "#" + UUID.randomUUID()), getGraph());
        for (final var item : access) {
            policy.allow().add(item);
        }
        policy.allOf().add(matcher);
        return policy;
    }
}
