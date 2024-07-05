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

import com.inrupt.client.*;
import com.inrupt.client.auth.Session;
import com.inrupt.client.openid.OpenIdSession;
import com.inrupt.client.solid.*;
import com.inrupt.client.spi.RDFFactory;
import com.inrupt.client.util.URIBuilder;
import com.inrupt.client.vocabulary.PIM;
import com.inrupt.client.webid.WebIdProfile;

import java.net.URI;
import java.util.UUID;

import org.apache.commons.rdf.api.BlankNode;
import org.apache.commons.rdf.api.Dataset;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Literal;
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
    private static final String AUTH_METHOD = config
            .getOptionalValue("inrupt.test.auth-method", String.class)
            .orElse("client_secret_basic");
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CLIENT_ID = config.getValue("inrupt.test.client-id", String.class);
    private static final String CLIENT_SECRET = config.getValue("inrupt.test.client-secret", String.class);
    private static final String FOLDER_SEPARATOR = "/";
    private static URI publicContainerURI;
    private static final Boolean INRUPT_TEST_ERROR_DESCRIPTION_FEATURE =
            config.getValue("inrupt.test.error-description.feature", Boolean.class);

    private static SolidSyncClient localAuthClient;

    @BeforeAll
    static void setup() {
        LOGGER.info("Setup DomainModulesResource test");
        if (config.getOptionalValue("inrupt.test.webid", String.class).isPresent()) {
            LOGGER.info("Running DomainModulesResource on live server");
            webidUrl = config.getOptionalValue("inrupt.test.webid", String.class).get();
        } else {
            LOGGER.info("Running DomainModulesResource on Mock services");
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
            webidUrl = webIdService.getMockServerUrl() + Utils.FOLDER_SEPARATOR + MOCK_USERNAME;
        }

        State.WEBID = URI.create(webidUrl);
        //find storage from WebID using domain-specific webID solid concept
        try (final WebIdProfile sameProfile = client.read(URI.create(webidUrl), WebIdProfile.class)) {
            final var storages = sameProfile.getStorages();
            final String issuer = sameProfile.getOidcIssuers().iterator().next().toString();
            if (!storages.isEmpty()) {
                podUrl = storages.iterator().next().toString();
            }

            final Session session = OpenIdSession.ofClientCredentials(
                    URI.create(issuer), //Client credentials
                    CLIENT_ID,
                    CLIENT_SECRET,
                    AUTH_METHOD);

            localAuthClient = SolidSyncClient.getClient().session(session);
        } catch (SolidClientException ex) {
            LOGGER.error("problems reading the webId");
        }

        publicContainerURI = URIBuilder.newBuilder(URI.create(podUrl))
                .path("public-domain-test-" + UUID.randomUUID() + FOLDER_SEPARATOR)
                .build();

        Utils.createPublicContainer(localAuthClient, publicContainerURI);

        LOGGER.info("Integration Test Pod Host: [{}]", URI.create(podUrl).getHost());
    }

    @AfterAll
    static void teardown() {
        //cleanup pod
        Utils.deleteContentsRecursively(localAuthClient, publicContainerURI);

        if (config.getOptionalValue("inrupt.test.webid", String.class).isEmpty()) {
            mockHttpServer.stop();
            identityProviderServer.stop();
            authServer.stop();
            webIdService.stop();
        }
    }

    @Test
    @DisplayName("https://w3id.org/inrupt/qa/manifest/solid-client-java/baseRdfSourceCrud " +
            "CRUD on RDF resource")
    void crudRdfTest() {
        LOGGER.info("Integration Test - CRUD on RDF resource");

        final String resourceName = publicContainerURI + "e2e-test-subject-domain1";
        final String predicateName = "https://example.example/predicate";

        final IRI subject = rdf.createIRI(resourceName);
        final IRI predicate = rdf.createIRI(predicateName);
        final Literal objectTrue = rdf.createLiteral("true", booleanType);
        final Literal objectFalse = rdf.createLiteral("false", booleanType);

        // Populate data for this resource
        final Dataset dataset = rdf.createDataset();
        dataset.add(null, subject, predicate, objectTrue);

        // Create a new resource
        final URI resourceUri = URI.create(resourceName);
        try (final SolidRDFSource resource = client.create(new SolidRDFSource(resourceUri, dataset))) {

            // Assert that the returned resource contains the expected data
            assertEquals(resourceUri, resource.getIdentifier());
            assertEquals(1, resource.size());
            assertTrue(resource.contains(null, subject, predicate, objectTrue));

            // Adjust the contents of the underlying dataset
            resource.clear();
            resource.add(null, subject, predicate, objectFalse);

            // Update the resource
            assertDoesNotThrow(() -> client.update(resource));
        }

        // Re-fetch the resource, ensuring that it has been updated
        try (final SolidRDFSource resource = client.read(resourceUri, SolidRDFSource.class)) {
            // Assert that the returned resource contains the expected data
            assertEquals(resourceUri, resource.getIdentifier());
            assertEquals(1, resource.size());
            assertTrue(resource.contains(null, subject, predicate, objectFalse));
        }

        // Delete the resource
        assertDoesNotThrow(() -> client.delete(resourceUri));
    }

    @Test
    @DisplayName("https://w3id.org/inrupt/qa/manifest/solid-client-java/baseContainerCrud " +
            "create and remove Containers")
    void containerCreateDeleteTest() {
        LOGGER.info("Integration Test - create and remove Containers");

        final String containerURL = publicContainerURI + "newContainer-" + UUID.randomUUID() + "/";

        final SolidContainer newContainer = new SolidContainer(URI.create(containerURL));
        assertDoesNotThrow(() -> client.create(newContainer));

        assertDoesNotThrow(() -> client.delete(newContainer));
    }

    @Test
    @DisplayName("https://w3id.org/inrupt/qa/manifest/solid-client-java/blankNodeSupport " +
        "can update statements containing Blank Nodes in different instances of the same model")
    void blankNodesTest() {
        LOGGER.info("Integration Test - update statements containing Blank Nodes in " +
            "different instances of the same model");

        final String resourceName = publicContainerURI + "e2e-test-subject-domain2";
        final String predicate1Name = "https://example.example/predicate";
        final String predicate2Name = "https://example.example/predicateForBlank";


        final IRI subject = rdf.createIRI(resourceName);
        final IRI predicate1 = rdf.createIRI(predicate1Name);
        final IRI predicate2 = rdf.createIRI(predicate2Name);
        final BlankNode objectBlank = rdf.createBlankNode("blank");
        final Literal objectTrue = rdf.createLiteral("true", booleanType);
        final Literal objectFalse = rdf.createLiteral("false", booleanType);

        // Populate data for this resource
        final Dataset dataset = rdf.createDataset();
        dataset.getGraph().add(subject, predicate1, objectTrue);
        dataset.getGraph().add(subject, predicate2, objectBlank);

        // Create a new resource
        final URI resourceUri = URI.create(resourceName);
        try (final SolidRDFSource resource = client.create(new SolidRDFSource(resourceUri, dataset))) {
            // Assert that the returned resource contains the expected data
            assertEquals(resourceUri, resource.getIdentifier());
            assertEquals(2, resource.getGraph().size());
            assertTrue(resource.getGraph().contains(subject, predicate1, objectTrue));
            assertTrue(resource.getGraph().contains(subject, predicate2, null));

            // Adjust the contents of the underlying dataset
            resource.getGraph().remove(subject, predicate1, null);
            resource.getGraph().add(subject, predicate1, objectFalse);

            // Update the resource
            assertDoesNotThrow(() -> client.update(resource));
        }

        // Re-fetch the resource, ensuring that that it has been updated
        try (final SolidRDFSource resource = client.read(resourceUri, SolidRDFSource.class)) {
            // Assert that the returned resource contains the expected data
            assertEquals(resourceUri, resource.getIdentifier());
            assertEquals(2, resource.getGraph().size());
            assertTrue(resource.getGraph().contains(subject, predicate1, objectFalse));
            assertTrue(resource.getGraph().contains(subject, predicate2, null));
        }

        assertDoesNotThrow(() -> client.delete(resourceUri));
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

        assertEquals(INRUPT_TEST_ERROR_DESCRIPTION_FEATURE, Utils.checkProblemDetails(err).isPresent());
        Utils.checkProblemDetails(err).ifPresent(problemDetails -> {
            assertEquals("Not Found", problemDetails.getTitle());
            assertNotNull(problemDetails.getInstance());
            assertNotNull(problemDetails.getDetail());
        });
    }

    @Test
    @DisplayName("https://w3id.org/inrupt/qa/manifest/solid-client-java/ldpNavigation " +
            "from a leaf container navigate until finding the root")
    void ldpNavigationTest() {
        LOGGER.info("Integration Test - from a leaf container navigate until finding the root");

        //returns: testContainer + "UUID1/UUID2/UUID3/"
        final var leafPath = getNestedContainer(publicContainerURI.toString(), 1);

        final String podRoot = podRoot(leafPath);

        assertFalse(podRoot.isEmpty());
    }

    private String podRoot(final String leafPath) {
        String tempURL = leafPath;
        final URI storage = URI.create(PIM.getNamespace() + "Storage");
        while (tempURL.chars().filter(ch -> ch == '/').count() >= 3) {
            final Request req = Request.newBuilder(URI.create(tempURL)).GET().build();
            final Response<SolidRDFSource> headerResponse =
                    localAuthClient.send(req, SolidResourceHandlers.ofSolidRDFSource());
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
