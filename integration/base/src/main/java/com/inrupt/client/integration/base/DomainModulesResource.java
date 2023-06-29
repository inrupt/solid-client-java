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
package com.inrupt.client.integration.base;

import static org.junit.jupiter.api.Assertions.*;

import com.inrupt.client.Headers;
import com.inrupt.client.Request;
import com.inrupt.client.Response;
import com.inrupt.client.auth.Session;
import com.inrupt.client.openid.OpenIdSession;
import com.inrupt.client.solid.*;
import com.inrupt.client.spi.RDFFactory;
import com.inrupt.client.util.URIBuilder;
import com.inrupt.client.vocabulary.LDP;
import com.inrupt.client.vocabulary.PIM;
import com.inrupt.client.webid.WebIdProfile;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.rdf.api.BlankNode;
import org.apache.commons.rdf.api.Dataset;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Literal;
import org.apache.commons.rdf.api.Quad;
import org.apache.commons.rdf.api.RDF;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Domain-specific modules based test class for resource integration scenarios.
 */
public class DomainModulesResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DomainModulesResource.class);

    private static MockSolidServer mockHttpServer;
    private static MockOpenIDProvider identityProviderServer;
    private static MockUMAAuthorizationServer authServer;
    private static MockWebIdService webIdService;
    private static String podUrl;
    private static String webidUrl;
    private static String issuer;
    private static final String MOCK_USERNAME = "someuser";

    private static final Config config = ConfigProvider.getConfig();
    private static final RDF rdf = RDFFactory.getInstance();
    private static final SolidSyncClient client = SolidSyncClient.getClient().session(Session.anonymous());

    private static IRI booleanType = rdf.createIRI("http://www.w3.org/2001/XMLSchema#boolean");

    private static final String PUBLIC_RESOURCE_PATH = config
        .getOptionalValue("inrupt.test.public-resource-path", String.class)
        .orElse("");
    private static final String AUTH_METHOD = config
            .getOptionalValue("inrupt.test.auth-method", String.class)
            .orElse("client_secret_basic");
    private static final String CLIENT_ID = config.getValue("inrupt.test.client-id", String.class);
    private static final String CLIENT_SECRET = config.getValue("inrupt.test.client-secret", String.class);

    private static String testContainer = "resource/";
    private static final String FOLDER_SEPARATOR = "/";
    private static URI testContainerURI;
    private static URI publicContainerURI;

    private static SolidSyncClient authClient;

    @BeforeAll
    static void setup() {
        authServer = new MockUMAAuthorizationServer();
        authServer.start();

        mockHttpServer = new MockSolidServer(authServer.getMockServerUrl());
        mockHttpServer.start();

        identityProviderServer = new MockOpenIDProvider(MOCK_USERNAME);
        identityProviderServer.start();

        webIdService = new MockWebIdService(
            mockHttpServer.getMockServerUrl(),
            identityProviderServer.getMockServerUrl(),
            MOCK_USERNAME);
        webIdService.start();

        webidUrl = config
            .getOptionalValue("inrupt.test.webid", String.class)
            .orElse(webIdService.getMockServerUrl() + Utils.FOLDER_SEPARATOR + MOCK_USERNAME);

        State.WEBID = URI.create(webidUrl);
        //find storage from WebID using domain-specific webID solid concept
        try (final WebIdProfile sameProfile = client.read(URI.create(webidUrl), WebIdProfile.class)) {
            final var storages = sameProfile.getStorages();
            issuer = sameProfile.getOidcIssuers().iterator().next().toString();
            if (!storages.isEmpty()) {
                podUrl = storages.iterator().next().toString();
            }
        }
        createAuthenticatedClient();
        if (PUBLIC_RESOURCE_PATH.isEmpty()) {
            testContainerURI = URIBuilder.newBuilder(URI.create(podUrl))
                .path("test-" + UUID.randomUUID())
                .path(testContainer).build();
        } else {
            testContainerURI = URIBuilder.newBuilder(URI.create(podUrl))
                .path(PUBLIC_RESOURCE_PATH)
                .path("test-" + UUID.randomUUID())
                .path(testContainer)
                .build();

            publicContainerURI = URIBuilder.newBuilder(URI.create(podUrl))
                    .path(PUBLIC_RESOURCE_PATH + FOLDER_SEPARATOR).build();
            createContainer(publicContainerURI);
            prepareACR(publicContainerURI);
        }

        LOGGER.info("Integration Test Pod Host: [{}]", URI.create(podUrl).getHost());
    }

    @AfterAll
    static void teardown() {
        //cleanup pod
        client.send(Request.newBuilder(testContainerURI).DELETE().build(), Response.BodyHandlers.discarding());
        client.send(Request.newBuilder(testContainerURI.resolve("..")).DELETE().build(),
                Response.BodyHandlers.discarding());
        if (publicContainerURI != null) {
            authClient.send(Request.newBuilder(publicContainerURI).DELETE().build(),
                    Response.BodyHandlers.discarding());
        }

        mockHttpServer.stop();
        identityProviderServer.stop();
        authServer.stop();
        webIdService.stop();
    }

    @Test
    @DisplayName("https://w3id.org/inrupt/qa/manifest/solid-client-java/baseRdfSourceCrud " +
            "CRUD on RDF resource")
    void crudRdfTest() {
        LOGGER.info("Integration Test - CRUD on RDF resource");

        final String newResourceName = testContainerURI + "e2e-test-subject";
        final String newPredicateName = "https://example.example/predicate";

        final IRI newResourceNode = rdf.createIRI(newResourceName);
        final IRI newPredicateNode = rdf.createIRI(newPredicateName);
        final Literal object = rdf.createLiteral("true", booleanType);

        final Dataset dataset = rdf.createDataset();
        dataset.add(rdf.createQuad(newResourceNode, newResourceNode, newPredicateNode, object));

        try (final SolidRDFSource newResource = new SolidRDFSource(URI.create(newResourceName), dataset, null)) {
            assertDoesNotThrow(() -> client.create(newResource));

            try (final SolidRDFSource resource = client.read(URI.create(newResourceName), SolidRDFSource.class)) {
                assertEquals(newResource.getIdentifier(), resource.getIdentifier());
                assertEquals(1, resource.size());

                final Literal newObject = rdf.createLiteral("false", booleanType);
                final Dataset newDataset = rdf.createDataset();
                try (final var stream = resource.stream(null, newResourceNode, null, null)) {
                    stream.map(quad ->
                            rdf.createQuad(quad.getSubject(), quad.getSubject(), quad.getPredicate(), newObject))
                        .forEach(newDataset::add);
                }
                try (final SolidRDFSource updatedResource = new SolidRDFSource(URI.create(newResourceName),
                    newDataset, null)) {
                    assertDoesNotThrow(() -> client.update(updatedResource));
                    assertDoesNotThrow(() -> client.delete(updatedResource));
                }
            }
        }
    }

    @Test
    @DisplayName("https://w3id.org/inrupt/qa/manifest/solid-client-java/baseContainerCrud " +
            "create and remove Containers")
    void containerCreateDeleteTest() {
        LOGGER.info("Integration Test - create and remove Containers");

        final String containerURL = testContainerURI + "newContainer/";

        final SolidContainer newContainer = new SolidContainer(URI.create(containerURL), null, null);
        assertDoesNotThrow(() -> client.create(newContainer));

        assertDoesNotThrow(() -> client.delete(newContainer));
    }

    @Test
    @DisplayName("https://w3id.org/inrupt/qa/manifest/solid-client-java/blankNodeSupport " +
        "can update statements containing Blank Nodes in different instances of the same model")
    void blankNodesTest() {
        LOGGER.info("Integration Test - update statements containing Blank Nodes in " +
            "different instances of the same model");

        final String newResourceName = testContainerURI + "e2e-test-subject";
        final String predicateName = "https://example.example/predicate";
        final String predicateForBlankName = "https://example.example/predicateForBlank";

        final Dataset dataset = rdf.createDataset();

        final IRI newResourceNode = rdf.createIRI(newResourceName);
        final IRI newPredicateNode = rdf.createIRI(predicateName);
        final Literal object = rdf.createLiteral("true", booleanType);
        dataset.add(rdf.createQuad(newResourceNode, newResourceNode, newPredicateNode, object));

        final IRI newBNPredicateNode = rdf.createIRI(predicateForBlankName);
        final BlankNode bn = rdf.createBlankNode("blank");
        dataset.add(rdf.createQuad(newResourceNode, newResourceNode, newBNPredicateNode, bn));

        try (final SolidRDFSource newResource = new SolidRDFSource(URI.create(newResourceName), dataset, null)) {
            assertDoesNotThrow(() -> client.create(newResource));

            try (final SolidRDFSource resource = client.read(URI.create(newResourceName), SolidRDFSource.class)) {
                assertEquals(URI.create(newResourceName), resource.getIdentifier());
                assertEquals(2, resource.size());

                final Literal newObject = rdf.createLiteral("false", booleanType);
                final Dataset newDataset = rdf.createDataset();

                final List<Quad> allQuads = new ArrayList<>();
                try (final var stream = resource.stream()) {
                    stream.forEach(allQuads::add);
                }
                try (final var stream = resource.stream(null, newResourceNode, newPredicateNode, null)) {
                    stream.forEach(allQuads::remove);
                }

                final Quad toAddQuad = rdf.createQuad(newResourceNode, newResourceNode, newPredicateNode, newObject);

                allQuads.add(toAddQuad);
                allQuads.forEach(newDataset::add);

                try (final SolidRDFSource updatedResource = new SolidRDFSource(URI.create(newResourceName),
                    newDataset, null)) {
                    assertDoesNotThrow(() -> client.update(updatedResource));
                    assertDoesNotThrow(() -> client.delete(updatedResource));
                }
            }
        }
    }

    @Test
    @DisplayName("https://w3id.org/inrupt/qa/manifest/solid-client-java/podStorageFinding " +
            "find pod storage from webID")
    void findStorageTest() {
        LOGGER.info("Integration Test - find pod storage from webID");

        try (final WebIdProfile sameProfile = client.read(URI.create(webidUrl), WebIdProfile.class)) {
            assertFalse(sameProfile.getStorages().isEmpty());
        }

        final var missingWebId = URIBuilder.newBuilder(URI.create(webidUrl)).path(UUID.randomUUID().toString()).build();
        final var err = assertThrows(NotFoundException.class, () -> client.read(missingWebId, WebIdProfile.class));
        assertEquals(404, err.getStatusCode());
        assertEquals(missingWebId, err.getUri());
    }

    @Test
    @DisplayName("https://w3id.org/inrupt/qa/manifest/solid-client-java/ldpNavigation " +
            "from a leaf container navigate until finding the root")
    void ldpNavigationTest() {
        LOGGER.info("Integration Test - from a leaf container navigate until finding the root");

        //returns: testContainer + "UUID1/UUID2/UUID3/"
        final var leafPath = getNestedContainer(testContainerURI.toString(), 1);

        final String podRoot = podRoot(leafPath);

        //cleanup
        recursiveDeleteLDPcontainers(leafPath);
        assertFalse(podRoot.isEmpty());
    }

    private String podRoot(final String leafPath) {
        String tempURL = leafPath;
        final URI storage = URI.create(PIM.getNamespace() + "Storage");
        while (tempURL.chars().filter(ch -> ch == '/').count() >= 3) {
            final Request req = Request.newBuilder(URI.create(tempURL)).GET().build();
            final Response<SolidRDFSource> headerResponse =
                    authClient.send(req, SolidResourceHandlers.ofSolidRDFSource());
            final var headers = headerResponse.headers();
            final var isRoot = headers.allValues("Link").stream()
                .flatMap(l -> Headers.Link.parse(l).stream())
                .anyMatch(link -> link.getUri().equals(storage));
            if (isRoot) {
                return tempURL;
            }
            tempURL = tempURL.substring(0, tempURL.length() - 1 ); //eliminate the last /
            tempURL = tempURL.substring(0, tempURL.lastIndexOf(FOLDER_SEPARATOR) + 1);
        }
        return "";
    }

    private void recursiveDeleteLDPcontainers(final String leafPath) {
        String tempURL = leafPath;
        final String notToDeletePath = URIBuilder.newBuilder(URI.create(podUrl))
                .path(PUBLIC_RESOURCE_PATH).build().toString();
        while (!(tempURL.equals(notToDeletePath)) && !(tempURL.equals(notToDeletePath + FOLDER_SEPARATOR))) {
            final var url = new SolidRDFSource(URI.create(tempURL),null, null);
            authClient.delete(url);
            tempURL = tempURL.substring(0, tempURL.lastIndexOf(FOLDER_SEPARATOR));
            tempURL = tempURL.substring(0, tempURL.lastIndexOf(FOLDER_SEPARATOR) + 1);
        }
    }

    private String getNestedContainer(final String path, final int depth) {
        final URIBuilder tempURL = URIBuilder.newBuilder(URI.create(path));
        for (int i = 0; i < depth; i++) {
            tempURL.path(UUID.randomUUID().toString());
        }
        final String newURL = tempURL.build() + Utils.FOLDER_SEPARATOR;
        final var resource = new SolidRDFSource(URI.create(newURL));
        client.create(resource);
        return newURL;
    }

    private static void createAuthenticatedClient() {
        final Session session = OpenIdSession.ofClientCredentials(
                URI.create(issuer), //Client credentials
                CLIENT_ID,
                CLIENT_SECRET,
                AUTH_METHOD);

        authClient = SolidSyncClient.getClient().session(session);
    }

    private static void createContainer(final URI publicContainerURI) {
        final var requestCreate = Request.newBuilder(publicContainerURI)
                .header(Utils.CONTENT_TYPE, Utils.TEXT_TURTLE)
                .header("Link", Headers.Link.of(LDP.RDFSource, "type").toString())
                .PUT(Request.BodyPublishers.noBody())
                .build();
        final var resCreate =
                authClient.send(requestCreate, Response.BodyHandlers.discarding());
        assertTrue(Utils.isSuccessful(resCreate.statusCode()));
    }

    private static void prepareACR(final URI publicContainerURI) {
        try (SolidResource resource = authClient.read(publicContainerURI, SolidRDFSource.class)) {
            if (resource != null) {
                // find the acl Link in the header of the resource
                resource.getMetadata().getAcl().ifPresent(acl -> {
                    try (final SolidRDFSource acr = authClient.read(acl, SolidRDFSource.class)) {
                        Utils.publicAgentPolicyTriples(acl)
                                .forEach(acr.getGraph()::add);
                        authClient.update(acr);
                    }
                });
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
