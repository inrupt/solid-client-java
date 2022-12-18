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
package com.inrupt.client.webid.jena;

import static org.apache.jena.rdf.model.ModelFactory.createDefaultModel;

import com.inrupt.client.jena.wrapping.NodeMappings;
import com.inrupt.client.jena.wrapping.ResourceCon;
import com.inrupt.client.jena.wrapping.UriFactory;
import com.inrupt.client.jena.wrapping.ValueMappings;
import com.inrupt.client.model.WebIdAgent;
import com.inrupt.client.vocabulary.PIM;
import com.inrupt.client.vocabulary.RDF;
import com.inrupt.client.vocabulary.RDFS;
import com.inrupt.client.vocabulary.Solid;

import java.net.URI;
import java.util.Set;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.enhanced.Implementation;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.ResourceFactory;

public class JenaWebIdAgent extends ResourceCon implements WebIdAgent {
    static final Implementation factory = new UriFactory(JenaWebIdAgent::new);

    protected JenaWebIdAgent(final Node n, final EnhGraph m) {
        super(n, m);
    }

    public static JenaWebIdAgent create(final JenaWebIdProfile profile, final URI value) {
        return profile.createResource(value.toString()).as(JenaWebIdAgent.class);
    }

    public static JenaWebIdAgent clone(final WebIdAgent value) {
        if (value instanceof JenaWebIdAgent) {
            return (JenaWebIdAgent) value;
        }

        final var model = createDefaultModel();
        final var profile = JenaWebIdProfile.wrap(model);

        final var agent = create(profile, value.getUri());
        value.getOidcIssuer().forEach(agent.getOidcIssuer()::add);
        value.getSeeAlso().forEach(agent.getSeeAlso()::add);
        value.getStorage().forEach(agent.getStorage()::add);
        value.getType().forEach(agent.getType()::add);

        return agent;
    }

    @Override
    public URI getUri() {
        return URI.create(getURI());
    }

    @Override
    public Set<URI> getType() {
        return live(
                ResourceFactory.createProperty(RDF.type.toString()),
                NodeMappings::asIriResource,
                ValueMappings::iriAsUri);
    }

    @Override
    public Set<URI> getOidcIssuer() {
        return live(
                ResourceFactory.createProperty(Solid.oidcIssuer.toString()),
                NodeMappings::asIriResource,
                ValueMappings::iriAsUri);
    }

    @Override
    public Set<URI> getSeeAlso() {
        return live(
                ResourceFactory.createProperty(RDFS.seeAlso.toString()),
                NodeMappings::asIriResource,
                ValueMappings::iriAsUri);
    }

    @Override
    public Set<URI> getStorage() {
        return live(
                ResourceFactory.createProperty(PIM.storage.toString()),
                NodeMappings::asIriResource,
                ValueMappings::iriAsUri);
    }
}
