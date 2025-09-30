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
import org.apache.commons.rdf.api.Quad;
import org.apache.commons.rdf.api.RDFTerm;

/**
 * An Access Control Resource type.
 */
public class AccessControlResource extends RDFSource {

    private static final URI SOLID_ACCESS_GRANT = URI.create("http://www.w3.org/ns/solid/vc#SolidAccessGrant");

    /**
     *  Create a new Access Control Resource.
     *
     *  @param identifier the resource identifier
     *  @param dataset the underlying dataset for the resource
     */
    public AccessControlResource(final URI identifier, final Dataset dataset) {
        super(identifier, dataset);
        dataset.add(null, rdf.createIRI(identifier.toString()), rdf.createIRI(RDF.type.toString()),
                rdf.createIRI(ACP.AccessControlResource.toString()));
    }

    /**
     * Retrieve the acp:accessControl structures.
     *
     * <p>accessControl resources are applied (non-recursively) to a container or resource.
     *
     * @return a collection of {@link AccessControl} objects
     */
    public Set<AccessControl> accessControl() {
        return new ACPNode(rdf.createIRI(getIdentifier().toString()), getGraph()).accessControl();
    }

    /**
     * Retrieve the acp:memberAccessControl structures.
     *
     * <p>memberAccessControl resources are applied recursively to a containment hierarchy.
     *
     * @return a collection of {@link AccessControl} objects
     */
    public Set<AccessControl> memberAccessControl() {
        return new ACPNode(rdf.createIRI(getIdentifier().toString()), getGraph()).memberAccessControl();
    }

    /**
     * Compact the internal data.
     */
    public void compact() {
        final var accessControls = stream(null, null, rdf.createIRI(RDF.type.toString()),
                rdf.createIRI(ACP.AccessControl.toString())).map(Quad::getSubject).toList();
        for (final var accessControl : accessControls) {
            if (!contains(null, null, null, accessControl)) {
                for (final var quad : stream(null, accessControl, null, null).toList()) {
                    remove(quad);
                }
            }
        }

        final var policies = stream(null, null, rdf.createIRI(RDF.type.toString()),
                rdf.createIRI(ACP.Policy.toString())).map(Quad::getSubject).toList();
        for (final var policy : policies) {
            if (!contains(null, null, null, policy)) {
                for (final var quad : stream(null, policy, null, null).toList()) {
                    remove(quad);
                }
            }
        }

        final var matchers = stream(null, null, rdf.createIRI(RDF.type.toString()),
                rdf.createIRI(ACP.Matcher.toString())).map(Quad::getSubject).toList();
        for (final var matcher : matchers) {
            if (!contains(null, null, null, matcher)) {
                for (final var quad : stream(null, matcher, null, null).toList()) {
                    remove(quad);
                }
            }
        }
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

    /**
     * Add policies to the access control resource.
     *
     * @param policies the policies to add
     * @return the access control structure
     */
    public AccessControl accessControl(final Policy... policies) {
        final var baseUri = getIdentifier().getScheme() + ":" + getIdentifier().getSchemeSpecificPart();
        final var ac = new AccessControl(rdf.createIRI(baseUri + "#" + UUID.randomUUID()), getGraph());
        for (final var policy : policies) {
            ac.apply().add(policy);
        }
        return ac;
    }

    /**
     * Create a policy that matches authenticated agents.
     *
     * @param access the access levels, such as Read or Write
     * @return the new policy
     */
    public Policy authenticatedAgentPolicy(final URI... access) {
        return agentPolicy(ACP.AuthenticatedAgent, access);
    }

    /**
     * Create a policy that matches all agents.
     *
     * @param access the access levels, such as Read or Write
     * @return the new policy
     */
    public Policy anyAgentPolicy(final URI... access) {
        return agentPolicy(ACP.PublicAgent, access);
    }

    /**
     * Create a policy that matches all clients.
     *
     * @param access the access levels, such as Read or Write
     * @return the new policy
     */
    public Policy anyClientPolicy(final URI... access) {
        return clientPolicy(ACP.PublicClient, access);
    }

    /**
     * Create a policy that matches all issuers.
     *
     * @param access the access levels, such as Read or Write
     * @return the new policy
     */
    public Policy anyIssuerPolicy(final URI... access) {
        return issuerPolicy(ACP.PublicIssuer, access);
    }

    /**
     * Create a policy that matches access grants.
     *
     * @param access the access levels, such as Read or Write
     * @return the new policy
     */
    public Policy accessGrantsPolicy(final URI... access) {
        return simplePolicy(matcher -> matcher.vc().add(SOLID_ACCESS_GRANT), access);
    }

    /**
     * Create a policy that matches a particular agent.
     *
     * @param agent the agent identifier
     * @param access the access levels, such as Read or Write
     * @return the new policy
     */
    public Policy agentPolicy(final URI agent, final URI... access) {
        return simplePolicy(matcher -> matcher.agent().add(agent), access);
    }

    /**
     * Create a policy that matches a particular client.
     *
     * @param client the client identifier
     * @param access the access levels, such as Read or Write
     * @return the new policy
     */
    public Policy clientPolicy(final URI client, final URI... access) {
        return simplePolicy(matcher -> matcher.client().add(client), access);
    }

    /**
     * Create a policy that matches a particular issuer.
     *
     * @param issuer the issuer identifier
     * @param access the access levels, such as Read or Write
     * @return the new policy
     */
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
