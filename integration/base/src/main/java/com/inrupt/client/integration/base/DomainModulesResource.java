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
package com.inrupt.client.integration.parent;

import static org.junit.jupiter.api.Assertions.*;

import com.inrupt.client.Request;
import com.inrupt.client.auth.Session;
import com.inrupt.client.solid.SolidClientException;
import com.inrupt.client.solid.SolidContainer;
import com.inrupt.client.solid.SolidResource;
import com.inrupt.client.solid.SolidSyncClient;
import com.inrupt.client.spi.RDFFactory;
import com.inrupt.client.util.URIBuilder;
import com.inrupt.client.webid.WebIdBodyHandlers;
import com.inrupt.client.webid.WebIdProfile;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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

public class DomainModulesResource {

    private static MockSolidServer mockHttpServer;
    private static MockOpenIDProvider identityProviderServer;
    private static MockUMAAuthorizationServer authServer;
    private static MockWebIdSevice webIdService;
    private static String podUrl;
    private static String webidUrl;
    private static final String MOCK_USERNAME = "someuser";

    private static final Config config = ConfigProvider.getConfig();
    private static final RDF rdf = RDFFactory.getInstance();
    private static final SolidSyncClient client = SolidSyncClient.getClient().session(Session.anonymous());

    private static String testContainer = "resource/";

    private static final String PRIVATE_RESOURCE_PATH = config
        .getOptionalValue("inrupt.test.privateResourcePath", String.class)
        .orElse("private");

    @BeforeAll
    static void setup() {
        authServer = new MockUMAAuthorizationServer();
        authServer.start();

        mockHttpServer = new MockSolidServer(authServer.getMockServerUrl());
        mockHttpServer.start();

        identityProviderServer = new MockOpenIDProvider(MOCK_USERNAME);
        identityProviderServer.start();

        webIdService = new MockWebIdSevice(
            mockHttpServer.getMockServerUrl(),
            identityProviderServer.getMockServerUrl(),
            MOCK_USERNAME);
        webIdService.start();

        State.PRIVATE_RESOURCE_PATH = PRIVATE_RESOURCE_PATH;

        webidUrl = config
            .getOptionalValue("inrupt.test.webid", String.class)
            .orElse(webIdService.getMockServerUrl() + Utils.FOLDER_SEPARATOR + MOCK_USERNAME);

        State.WEBID = URI.create(webidUrl);
        //find issuer & storage from WebID using domain-specific webID solid concept
        final Request requestRdf = Request.newBuilder(URI.create(webidUrl))
            .header(Utils.ACCEPT, Utils.TEXT_TURTLE)
            .GET().build();
        final var responseRdf = client.send(requestRdf, WebIdBodyHandlers.ofWebIdProfile());
        if (responseRdf.statusCode() == 200) {
            try (final var body = responseRdf.body()) {
                if (!body.getStorage().isEmpty()) {
                    podUrl = body.getStorage().iterator().next().toString();
                }
            }
        }
        testContainer = podUrl + Utils.FOLDER_SEPARATOR + testContainer;
    }

    @AfterAll
    static void teardown() {
        mockHttpServer.stop();
        identityProviderServer.stop();
        authServer.stop();
        webIdService.stop();
    }

    @Test
    @DisplayName("./solid-client-java:baseRdfSourceCrud CRUD on RDF resource")
    void crudRdfTest() {
        final String newResourceName = testContainer + "e2e-test-subject";
        final String newPredicateName = "https://example.example/predicate";

        final IRI newResourceNode = rdf.createIRI(newResourceName);
        final IRI newPredicateNode = rdf.createIRI(newPredicateName);
        final Literal object = rdf.createLiteral("true", rdf.createIRI("http://www.w3.org/2001/XMLSchema#boolean"));

        final Dataset dataset = rdf.createDataset();
        dataset.add(rdf.createQuad(newResourceNode, newResourceNode, newPredicateNode, object));

        final SolidResource newResource = new SolidResource(URI.create(newResourceName), dataset, null);

        assertDoesNotThrow(() -> client.create(newResource));

        try (final SolidResource resource = client.read(URI.create(newResourceName), SolidResource.class)) {
            assertEquals(newResource.getIdentifier(), resource.getIdentifier());
            assertEquals(1, resource.getDataset().stream().count());

            final Literal newObject = rdf.createLiteral("false",
                    rdf.createIRI("http://www.w3.org/2001/XMLSchema#boolean"));
            final Dataset newDataset = rdf.createDataset();
            resource.getDataset().stream(null, newResourceNode, null, null)
                    .map(quad ->
                        rdf.createQuad(quad.getSubject(), quad.getSubject(), quad.getPredicate(), newObject))
                    .collect(Collectors.toList()).forEach(newDataset::add);
            final SolidResource updatedResource = new SolidResource(URI.create(newResourceName), newDataset, null);
            assertDoesNotThrow(() -> client.update(updatedResource));
            assertDoesNotThrow(() -> client.delete(updatedResource));
        }
    }

    @Test
    @DisplayName("./solid-client-java:baseContainerCrud can create and remove Containers")
    void containerCreateDeleteTest() {

        final String containerURL = testContainer + "newContainer/";

        final SolidContainer newContainer = new SolidContainer(URI.create(containerURL), null, null);
        assertDoesNotThrow(() -> client.create(newContainer));

        assertDoesNotThrow(() -> client.delete(newContainer));
    }

    @Test
    @DisplayName("./solid-client-java:blankNodeSupport " +
        "can update statements containing Blank Nodes in different instances of the same model")
    void blankNodesTest() {

        final String newResourceName = testContainer + "e2e-test-subject";
        final String predicateName = "https://example.example/predicate";
        final String predicateForBlankName = "https://example.example/predicateForBlank";

        final Dataset dataset = rdf.createDataset();

        final IRI newResourceNode = rdf.createIRI(newResourceName);
        final IRI newPredicateNode = rdf.createIRI(predicateName);
        final Literal object = rdf.createLiteral("true", rdf.createIRI("http://www.w3.org/2001/XMLSchema#boolean"));
        dataset.add(rdf.createQuad(newResourceNode, newResourceNode, newPredicateNode, object));

        final IRI newBNPredicateNode = rdf.createIRI(predicateForBlankName);
        final BlankNode bn = rdf.createBlankNode("blank");
        dataset.add(rdf.createQuad(newResourceNode, newResourceNode, newBNPredicateNode, bn));

        final SolidResource newResource = new SolidResource(URI.create(newResourceName), dataset, null);

        assertDoesNotThrow(() -> client.create(newResource));

        try (final SolidResource resource = client.read(URI.create(newResourceName), SolidResource.class)) {
            assertEquals(newResource.getIdentifier(), resource.getIdentifier());
            assertEquals(2, resource.getDataset().stream().count());

            final Literal newObject = rdf.createLiteral("false",
                    rdf.createIRI("http://www.w3.org/2001/XMLSchema#boolean"));
            final Dataset newDataset = rdf.createDataset();

            final List<Quad> allQuads = resource.getDataset().stream().collect(Collectors.toList());
            final var toDeleteQuads =
                    resource.getDataset()
                            .stream(null, newResourceNode, newPredicateNode, null)
                            .collect(Collectors.toList());
            final Quad toAddQuad = rdf.createQuad(newResourceNode, newResourceNode, newPredicateNode, newObject);

            toDeleteQuads.forEach(allQuads::remove);
            allQuads.add(toAddQuad);
            allQuads.forEach(newDataset::add);

            final SolidResource updatedResource = new SolidResource(URI.create(newResourceName), newDataset, null);
            assertDoesNotThrow(() -> client.update(updatedResource));
            assertDoesNotThrow(() -> client.delete(updatedResource));
        }
    }

    @Test
    @DisplayName("./solid-client-java:podStorageFinding find pod storage from webID")
    void findStorageTest() {

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

        final var lefePath = getNestedContainer(podUrl, 3); //example: "UUID1/UUID2/UUID3"

        Optional<URI> root = Optional.empty();
        var containers = lefePath.split(Utils.FOLDER_SEPARATOR);
        while (!root.isPresent() && containers.length > 0) {
            root = client.read(URI.create(podUrl + Utils.FOLDER_SEPARATOR +
                String.join(Utils.FOLDER_SEPARATOR, containers)), SolidResource.class)
                            .getMetadata()
                            .getStorage();
            containers = Arrays.copyOf(containers, containers.length - 1);
        }
        if (!root.isPresent() && containers.length == 0) {
            root = client.read(URI.create(podUrl), SolidResource.class)
                    .getMetadata()
                    .getStorage();
        }
        assertTrue(root.isPresent());
        assertEquals(podUrl, root.get().toString());
    }

    private String getNestedContainer(final String podUrl, final int depth) {
        var tempUrl = UUID.randomUUID().toString();
        for (int i = 1; i < depth; i++) {
            tempUrl += Utils.FOLDER_SEPARATOR + UUID.randomUUID().toString();
        }
        final var resource = new SolidResource(URI.create(podUrl + Utils.FOLDER_SEPARATOR + tempUrl));
        client.create(resource);
        return tempUrl;
    }
}
