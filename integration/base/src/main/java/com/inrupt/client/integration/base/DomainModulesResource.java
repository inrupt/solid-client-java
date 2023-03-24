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
package com.inrupt.client.integration.base;

import static org.junit.jupiter.api.Assertions.*;

import com.inrupt.client.Headers;
import com.inrupt.client.Request;
import com.inrupt.client.Response;
import com.inrupt.client.auth.Session;
import com.inrupt.client.solid.SolidClientException;
import com.inrupt.client.solid.SolidContainer;
import com.inrupt.client.solid.SolidRDFSource;
import com.inrupt.client.solid.SolidResourceHandlers;
import com.inrupt.client.solid.SolidSyncClient;
import com.inrupt.client.spi.RDFFactory;
import com.inrupt.client.util.URIBuilder;
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
    private static final String MOCK_USERNAME = "someuser";

    private static final Config config = ConfigProvider.getConfig();
    private static final RDF rdf = RDFFactory.getInstance();
    private static final SolidSyncClient client = SolidSyncClient.getClient().session(Session.anonymous());

    private static IRI booleanType = rdf.createIRI("http://www.w3.org/2001/XMLSchema#boolean");

    private static final String PRIVATE_RESOURCE_PATH = config
        .getOptionalValue("inrupt.test.private-resource-path", String.class)
        .orElse("private");
    private static final String PUBLIC_RESOURCE_PATH = config
        .getOptionalValue("inrupt.test.public-resource-path", String.class)
        .orElse("");

    private static String testContainer = "resource/";
    private static URI testContainerURI;

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

        State.PRIVATE_RESOURCE_PATH = PRIVATE_RESOURCE_PATH;

        webidUrl = config
            .getOptionalValue("inrupt.test.webid", String.class)
            .orElse(webIdService.getMockServerUrl() + Utils.FOLDER_SEPARATOR + MOCK_USERNAME);

        State.WEBID = URI.create(webidUrl);
        //find storage from WebID using domain-specific webID solid concept
        try (final WebIdProfile sameProfile = client.read(URI.create(webidUrl), WebIdProfile.class)) {
            final var storages = sameProfile.getStorage();
            if (!storages.isEmpty()) {
                podUrl = storages.iterator().next().toString();
            }
        }
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
        }

        LOGGER.info("Integration Test Pod Host: [{}]", URI.create(podUrl).getHost());
    }

    @AfterAll
    static void teardown() {
        //cleanup pod
        client.send(Request.newBuilder(testContainerURI).DELETE().build(), Response.BodyHandlers.discarding());
        client.send(Request.newBuilder(testContainerURI.resolve("..")).DELETE().build(),
                Response.BodyHandlers.discarding());

        mockHttpServer.stop();
        identityProviderServer.stop();
        authServer.stop();
        webIdService.stop();
    }

    @Test
    @DisplayName("./solid-client-java:baseRdfSourceCrud CRUD on RDF resource")
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
    @DisplayName("./solid-client-java:baseContainerCrud create and remove Containers")
    void containerCreateDeleteTest() {
        LOGGER.info("Integration Test - create and remove Containers");

        final String containerURL = testContainerURI + "newContainer/";

        final SolidContainer newContainer = new SolidContainer(URI.create(containerURL), null, null);
        assertDoesNotThrow(() -> client.create(newContainer));

        assertDoesNotThrow(() -> client.delete(newContainer));
    }

    @Test
    @DisplayName("./solid-client-java:blankNodeSupport " +
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
    @DisplayName("./solid-client-java:podStorageFinding find pod storage from webID")
    void findStorageTest() {
        LOGGER.info("Integration Test - find pod storage from webID");

        try (final WebIdProfile sameProfile = client.read(URI.create(webidUrl), WebIdProfile.class)) {
            assertFalse(sameProfile.getStorage().isEmpty());
        }

        final var missingWebId = URIBuilder.newBuilder(URI.create(webidUrl)).path(UUID.randomUUID().toString()).build();
        final var err = assertThrows(SolidClientException.class, () -> client.read(missingWebId, WebIdProfile.class));
        assertEquals(404, err.getStatusCode());
        assertEquals(missingWebId, err.getUri());
    }

    @Test
    @DisplayName("./solid-client-java:ldpNavigation from a leaf container navigate until finding the root")
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
                    client.send(req, SolidResourceHandlers.ofSolidRDFSource());
            final var headers = headerResponse.headers();
            final var isRoot = headers.allValues("Link").stream()
                .flatMap(l -> Headers.Link.parse(l).stream())
                .anyMatch(link -> link.getUri().equals(storage));
            if (isRoot) {
                return tempURL;
            }
            tempURL = tempURL.substring(0, tempURL.length() - 1 ); //eliminate the last /
            tempURL = tempURL.substring(0, tempURL.lastIndexOf("/") + 1);
        }
        return "";
    }

    private void recursiveDeleteLDPcontainers(final String leafPath) {
        String tempURL = leafPath;
        while (!tempURL.equals(podUrl) && !tempURL.equals(podUrl + "/")) {
            final var url = new SolidRDFSource(URI.create(tempURL),null, null);
            client.delete(url);
            tempURL = tempURL.substring(0, tempURL.lastIndexOf("/"));
            tempURL = tempURL.substring(0, tempURL.lastIndexOf("/") + 1);
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
}
