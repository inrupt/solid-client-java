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
package com.inrupt.client.accessgrant;

import static com.inrupt.client.vocabulary.RDF.type;

import com.inrupt.client.spi.RDFFactory;
import com.inrupt.client.util.URIBuilder;
import com.inrupt.client.vocabulary.ACP;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.api.Triple;

/**
 * Utility methods for use with the Access Grant module.
 **/
public final class AccessGrantUtils {

    private static final RDF rdf = RDFFactory.getInstance();
    private static final IRI SOLID_ACCESS_GRANT = rdf.createIRI("http://www.w3.org/ns/solid/vc#SolidAccessGrant");

    private static IRI asIRI(final URI uri) {
        return rdf.createIRI(uri.toString());
    }

    public static Set<Triple> accessControlPolicyTriples(final URI acl, final URI... modes) {
        final Set<Triple> triples = new HashSet<>();
        final IRI a = asIRI(type);

        // Matcher
        final IRI matcher = asIRI(URIBuilder.newBuilder(acl).fragment(UUID.randomUUID().toString()).build());
        triples.add(rdf.createTriple(matcher, a, asIRI(ACP.Matcher)));
        triples.add(rdf.createTriple(matcher, asIRI(ACP.vc), SOLID_ACCESS_GRANT));

        // Policy
        final IRI policy = asIRI(URIBuilder.newBuilder(acl).fragment(UUID.randomUUID().toString()).build());
        triples.add(rdf.createTriple(policy, a, asIRI(ACP.Policy)));
        triples.add(rdf.createTriple(policy, asIRI(ACP.allOf), matcher));
        for (final URI mode : modes ) {
            triples.add(rdf.createTriple(policy, asIRI(ACP.allow), asIRI(mode)));
        }

        // Access Control
        final IRI accessControl = asIRI(URIBuilder.newBuilder(acl).fragment(UUID.randomUUID().toString()).build());
        triples.add(rdf.createTriple(accessControl, a, asIRI(ACP.AccessControl)));
        triples.add(rdf.createTriple(accessControl, asIRI(ACP.apply), policy));

        // Access Control Resource
        final IRI subject = asIRI(acl);
        triples.add(rdf.createTriple(subject, a, asIRI(ACP.AccessControlResource)));
        triples.add(rdf.createTriple(subject, asIRI(ACP.accessControl), accessControl));
        triples.add(rdf.createTriple(subject, asIRI(ACP.memberAccessControl), accessControl));
        return triples;
    }

    private AccessGrantUtils() {
        // Prevent instantiation
    }
}
