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
import java.util.stream.Collectors;

import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.Dataset;
import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Quad;
import org.apache.commons.rdf.api.RDFTerm;

/**
 * An Access Control Resource type.
 */
public class AccessControlResource extends RDFSource {

    public static final URI SOLID_ACCESS_GRANT = URI.create("http://www.w3.org/ns/solid/vc#SolidAccessGrant");

    /**
     *  Create a new Access Control Resource.
     *
     *  @param identifier the resource identifier
     *  @param dataset the underlying dataset for the resource
     */
    public AccessControlResource(final URI identifier, final Dataset dataset) {
        super(identifier, dataset);
        dataset.add(null, asIRI(identifier), asIRI(RDF.type), asIRI(ACP.AccessControlResource));
    }

    /**
     * Retrieve the acp:accessControl structures.
     *
     * <p>accessControl resources are applied (non-recursively) to a container or resource.
     *
     * @return a collection of {@link AccessControl} objects
     */
    public Set<AccessControl> accessControl() {
        return new ACPNode(asIRI(getIdentifier()), getGraph()).accessControl();
    }

    /**
     * Retrieve the acp:memberAccessControl structures.
     *
     * <p>memberAccessControl resources are applied recursively to a containment hierarchy.
     *
     * @return a collection of {@link AccessControl} objects
     */
    public Set<AccessControl> memberAccessControl() {
        return new ACPNode(asIRI(getIdentifier()), getGraph()).memberAccessControl();
    }

    /**
     * Compact the internal data.
     */
    public void compact() {
        final var accessControls = stream(null, null, asIRI(RDF.type), asIRI(ACP.AccessControl))
            .map(Quad::getSubject).toList();
        for (final var accessControl : accessControls) {
            removeUnusedStatements(accessControl);
        }

        final var policies = stream(null, null, asIRI(RDF.type), asIRI(ACP.Policy)).map(Quad::getSubject).toList();
        for (final var policy : policies) {
            removeUnusedStatements(policy);
        }

        final var matchers = stream(null, null, asIRI(RDF.type), asIRI(ACP.Matcher)).map(Quad::getSubject).toList();
        for (final var matcher : matchers) {
            removeUnusedStatements(matcher);
        }
    }

    <T extends BlankNodeOrIRI> void removeUnusedStatements(final T resource) {
        if (!contains(null, null, null, resource)) {
            for (final var quad : stream(null, resource, null, null).toList()) {
                remove(quad);
            }
        }
    }

    /**
     * Merge two or more policies into a single policies with combined matchers.
     *
     * @param policies the policies to merge
     * @return the merged policy
     */
    public Policy merge(final Policy... policies) {
        final var baseUri = getIdentifier().getScheme() + ":" + getIdentifier().getSchemeSpecificPart();
        final var policy = new Policy(asIRI(baseUri + "#" + UUID.randomUUID()), getGraph());
        for (final var p : policies) {
            policy.allOf().addAll(p.allOf());
            policy.anyOf().addAll(p.anyOf());
            policy.noneOf().addAll(p.noneOf());
        }
        return policy;
    }

    public enum MatcherType {
        AGENT(ACP.agent), CLIENT(ACP.client), ISSUER(ACP.issuer), VC(ACP.vc);

        private final URI predicate;

        MatcherType(final URI predicate) {
            this.predicate = predicate;
        }

        public IRI asIRI() {
            return AccessControlResource.asIRI(predicate);
        }

        public URI asURI() {
            return predicate;
        }
    }

    /**
     * Find a policy, given a type, value and set of modes.
     *
     * @param type the matcher type
     * @param value the matcher value
     * @param modes the expected modes of the enclosing policy
     * @return the matched policies
     */
    public Set<Policy> find(final MatcherType type, final URI value, final Set<URI> modes) {
        return stream(null, null, type.asIRI(), asIRI(value))
            .map(Quad::getSubject)
            .flatMap(matcher -> stream(null, null, null, matcher))
            .map(Quad::getSubject)
            .filter(policy -> contains(null, policy, asIRI(RDF.type), asIRI(ACP.Policy)))
            .filter(policy -> stream(null, policy, asIRI(ACP.allow), null)
                    .map(Quad::getObject).filter(IRI.class::isInstance).map(IRI.class::cast)
                    .map(IRI::getIRIString).map(URI::create).toList().containsAll(modes))
            .map(policy -> new Policy(policy, getGraph()))
            .collect(Collectors.toSet());
    }

    static class ACPNode extends WrapperIRI {
        public ACPNode(final RDFTerm original, final Graph graph) {
            super(original, graph);
        }

        public Set<AccessControl> memberAccessControl() {
            return objects(asIRI(ACP.memberAccessControl),
                    AccessControl::asResource, ValueMappings.as(AccessControl.class));
        }

        public Set<AccessControl> accessControl() {
            return objects(asIRI(ACP.accessControl),
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
        final var ac = new AccessControl(asIRI(baseUri + "#" + UUID.randomUUID()), getGraph());
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
        final var matcher = new Matcher(asIRI(baseUri + "#" + UUID.randomUUID()), getGraph());
        handler.accept(matcher);

        final var policy = new Policy(asIRI(baseUri + "#" + UUID.randomUUID()), getGraph());
        for (final var item : access) {
            policy.allow().add(item);
        }
        policy.allOf().add(matcher);
        return policy;
    }

    private static IRI asIRI(final URI uri) {
        return asIRI(uri.toString());
    }

    private static IRI asIRI(final String uri) {
        return rdf.createIRI(uri);
    }
}
