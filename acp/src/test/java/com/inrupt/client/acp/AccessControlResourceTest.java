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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

import com.inrupt.client.solid.SolidClient;
import com.inrupt.client.solid.SolidSyncClient;
import com.inrupt.client.spi.RDFFactory;
import com.inrupt.client.vocabulary.ACL;

import java.io.IOException;
import java.net.URI;
import java.util.Set;

import org.apache.commons.rdf.api.RDF;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class AccessControlResourceTest {

    static final AcpMockHttpService mockHttpServer = new AcpMockHttpService();
    static final SolidSyncClient client = SolidSyncClient.getClient();
    static final RDF rdf = RDFFactory.getInstance();

    @BeforeAll
    static void setup() {
        mockHttpServer.start();
    }

    @AfterAll
    static void teardown() {
        mockHttpServer.stop();
    }

    @Test
    void testAcr1CheckValues() {
        final var uri = mockHttpServer.acr1();
        try (final AccessControlResource acr = client.read(uri, AccessControlResource.class)) {
            assertEquals(2, acr.accessControl().size());
            assertEquals(2, acr.memberAccessControl().size());

            final var matchers1 = acr.accessControl().stream()
                    .flatMap(ac -> ac.apply().stream())
                    .filter(policy ->
                        policy.allow().contains(ACL.Write) && policy.allow().size() == 2)
                    .flatMap(policy -> policy.allOf().stream())
                    .toList();
            assertEquals(1, matchers1.size());
            assertTrue(matchers1.get(0).agent().contains(URI.create("https://id.example/user")));

            final var matchers2 = acr.accessControl().stream()
                    .flatMap(ac -> ac.apply().stream())
                    .filter(policy ->
                        !policy.allow().contains(ACL.Write) && policy.allow().size() == 1)
                    .flatMap(policy -> policy.allOf().stream())
                    .toList();
            assertEquals(1, matchers2.size());
            assertTrue(matchers2.get(0).vc().contains(URI.create("http://www.w3.org/ns/solid/vc#SolidAccessGrant")));

            final var matchers3 = acr.memberAccessControl().stream()
                    .flatMap(ac -> ac.apply().stream())
                    .filter(policy ->
                        policy.allow().contains(ACL.Write) && policy.allow().size() == 2)
                    .flatMap(policy -> policy.allOf().stream())
                    .toList();
            assertEquals(1, matchers3.size());
            assertTrue(matchers3.get(0).agent().contains(URI.create("https://id.example/user")));

            final var matchers4 = acr.memberAccessControl().stream()
                    .flatMap(ac -> ac.apply().stream())
                    .filter(policy ->
                        !policy.allow().contains(ACL.Write) && policy.allow().size() == 1)
                    .flatMap(policy -> policy.allOf().stream())
                    .toList();
            assertEquals(1, matchers4.size());
            assertTrue(matchers4.get(0).client().contains(URI.create("https://app.example/id")));
            assertTrue(matchers4.get(0).vc().contains(URI.create("http://www.w3.org/ns/solid/vc#SolidAccessGrant")));
        }
    }

    @Test
    void testAcr1Mutation() throws IOException {
        final var uri = mockHttpServer.acr1();
        try (final AccessControlResource acr = client.read(uri, AccessControlResource.class)) {
            final var quads = acr.stream().toList();

            acr.accessControl().forEach(ac ->
                ac.apply().forEach(policy -> policy.allow().add(ACL.Append)));
            assertEquals(quads.size(), acr.size() - 2);

            try (var entity = acr.getEntity()) {
                assertTrue(new String(entity.readAllBytes(), UTF_8).contains(ACL.Append.toString()));
            }

            acr.accessControl().forEach(ac ->
                ac.apply().forEach(policy -> policy.allow().remove(ACL.Write)));
            assertEquals(quads.size(), acr.size() - 1);
        }
    }

    @Test
    void testAcr2CheckValues() {
        final var uri = mockHttpServer.acr2();
        try (final AccessControlResource acr = client.read(uri, AccessControlResource.class)) {
            assertEquals(2, acr.accessControl().size());
            assertEquals(3, acr.memberAccessControl().size());

            final var matchers1 = acr.accessControl().stream()
                    .flatMap(ac -> ac.apply().stream())
                    .filter(policy ->
                        policy.allow().contains(ACL.Write) && policy.allow().size() == 2)
                    .flatMap(policy -> policy.allOf().stream())
                    .toList();
            assertEquals(1, matchers1.size());
            assertTrue(matchers1.get(0).agent().contains(URI.create("https://id.example/user2")));

            final var matchers2 = acr.accessControl().stream()
                    .flatMap(ac -> ac.apply().stream())
                    .filter(policy ->
                        !policy.allow().contains(ACL.Write) && policy.allow().size() == 1)
                    .flatMap(policy -> policy.allOf().stream())
                    .toList();
            assertEquals(1, matchers2.size());
            assertTrue(matchers2.get(0).agent().contains(URI.create("https://bot.example/id")));

            final var matchers3 = acr.memberAccessControl().stream()
                    .flatMap(ac -> ac.apply().stream())
                    .filter(policy ->
                        !policy.allow().contains(ACL.Write) && policy.allow().size() == 1)
                    .flatMap(policy -> policy.allOf().stream())
                    .toList();
            assertEquals(2, matchers3.size());
            assertTrue(matchers3.stream().anyMatch(matcher ->
                        matcher.vc().contains(URI.create("http://www.w3.org/ns/solid/vc#SolidAccessGrant"))));
            assertTrue(matchers3.stream().anyMatch(matcher ->
                        matcher.agent().contains(URI.create("https://bot.example/id"))));
        }
    }

    @Test
    void buildAcr() {
        final var identifier = "https://data.example/resource";
        final var dataset = rdf.createDataset();
        final var uri = URI.create(identifier);

        final var matcher = new Matcher(rdf.createIRI(identifier + "#matcher"), rdf.createGraph());
        matcher.agent().add(URI.create("https://id.example/agent"));

        final var policy = new Policy(rdf.createIRI(identifier + "#policy"), rdf.createGraph());
        policy.allOf().add(matcher);
        policy.allow().add(ACL.Read);
        policy.allow().add(ACL.Write);

        final var accessControl = new AccessControl(rdf.createIRI(identifier + "#access-control"), rdf.createGraph());
        accessControl.apply().add(policy);

        final var acr = new AccessControlResource(uri, dataset);
        acr.accessControl().add(accessControl);

        assertEquals(10, acr.size());
    }

    @Test
    void testAcrFindPolicies() {
        final var uri = mockHttpServer.acr2();
        try (final AccessControlResource acr = client.read(uri, AccessControlResource.class)) {
            assertEquals(1, acr.find(AccessControlResource.MatcherType.VC,
                    AccessControlResource.SOLID_ACCESS_GRANT, Set.of(ACL.Read)).size());

            assertEquals(0, acr.find(AccessControlResource.MatcherType.VC,
                    AccessControlResource.SOLID_ACCESS_GRANT, Set.of(ACL.Read, ACL.Write)).size());

            assertEquals(0, acr.find(AccessControlResource.MatcherType.AGENT,
                    URI.create("https://bot.example/id"), Set.of(ACL.Read, ACL.Write)).size());

            assertEquals(1, acr.find(AccessControlResource.MatcherType.AGENT,
                    URI.create("https://bot.example/id"), Set.of(ACL.Read)).size());
        }
    }

    @Test
    void testAcr1RemoveAccessControl() {
        final var uri = mockHttpServer.acr2();
        try (final AccessControlResource acr = client.read(uri, AccessControlResource.class)) {
            assertEquals(2, acr.accessControl().size());
            assertEquals(3, acr.memberAccessControl().size());

            // Check dataset size
            assertEquals(29, acr.size());

            acr.accessControl().stream().findFirst().ifPresent(acr.accessControl()::remove);

            // Check dataset size
            assertEquals(28, acr.size());
            acr.compact();
            assertEquals(28, acr.size());

            assertEquals(1, acr.accessControl().size());
            assertEquals(3, acr.memberAccessControl().size());

        }
    }

    @Test
    void testAcr1RemoveValues() {
        final var uri = mockHttpServer.acr2();
        try (final AccessControlResource acr = client.read(uri, AccessControlResource.class)) {
            assertEquals(2, acr.accessControl().size());
            assertEquals(3, acr.memberAccessControl().size());

            // Check dataset size
            assertEquals(29, acr.size());

            // Remove single matcher and compact
            for (final var accessControl : acr.accessControl()) {
                for (final var policy : accessControl.apply()) {
                    if (policy.allow().contains(ACL.Write) && policy.allow().size() == 2) {
                        for (final var matcher : policy.allOf()) {
                            policy.allOf().remove(matcher);
                        }
                    }
                }
            }
            assertEquals(28, acr.size());
            acr.compact();
            assertEquals(26, acr.size());

            // Remove single policy and compact
            for (final var accessControl : acr.accessControl()) {
                for (final var policy : accessControl.apply()) {
                    if (policy.allow().contains(ACL.Write) && policy.allow().size() == 2) {
                        accessControl.apply().remove(policy);
                    }
                }
            }
            assertEquals(25, acr.size());
            acr.compact();
            assertEquals(22, acr.size());

            // Remove single access control and compact - no compaction involved
            final var ac = acr.accessControl().stream().findFirst().get();
            acr.accessControl().remove(ac);
            assertEquals(21, acr.size());
            acr.compact();
            assertEquals(21, acr.size());
        }
    }

    @Test
    void buildAcrWithExistingPolicies() {
        final var identifier = "https://data.example/resource";
        final var dataset = rdf.createDataset();
        final var uri = URI.create(identifier);

        final var acr = new AccessControlResource(uri, dataset);
        acr.accessControl().add(acr.accessControl(
                    acr.authenticatedAgentPolicy(ACL.Read, ACL.Write),
                    acr.anyAgentPolicy(ACL.Read),
                    acr.anyClientPolicy(ACL.Read, ACL.Write)));
        acr.memberAccessControl().add(acr.accessControl(
                    acr.anyIssuerPolicy(ACL.Read),
                    acr.accessGrantsPolicy(ACL.Read, ACL.Write)));

        assertEquals(38, acr.size());
    }

    @Test
    void expandAcr3Sync() {
        final var uri = mockHttpServer.acr3();
        try (final AccessControlResource acr = client.read(uri, AccessControlResource.class)) {
            assertEquals(2, acr.accessControl().size());
            assertEquals(3, acr.memberAccessControl().size());

            // Check dataset size
            assertEquals(12, acr.size());

            final var expanded = acr.expand(client);
            assertEquals(29, expanded.size());
            assertEquals(12, acr.size());
        }
    }

    @Test
    void expandAcr3Async() {
        final var uri = mockHttpServer.acr3();
        final var asyncClient = SolidClient.getClient();
        asyncClient.read(uri, AccessControlResource.class).thenAccept(res -> {
            try (final var acr = res) {
                assertEquals(2, acr.accessControl().size());
                assertEquals(3, acr.memberAccessControl().size());

                // Check dataset size
                assertEquals(12, acr.size());

                final var expanded = acr.expand(asyncClient);
                assertEquals(29, expanded.size());
                assertEquals(12, acr.size());
            }
        }).toCompletableFuture().join();
    }

    @Test
    void expandAcr4Sync() {
        final var uri = mockHttpServer.acr4();
        try (final AccessControlResource acr = client.read(uri, AccessControlResource.class)) {
            assertEquals(2, acr.accessControl().size());
            assertEquals(3, acr.memberAccessControl().size());

            // Check dataset size
            assertEquals(20, acr.size());

            final var expanded = acr.expand(client);
            assertEquals(22, expanded.size());
            assertEquals(20, acr.size());
        }
    }
}
