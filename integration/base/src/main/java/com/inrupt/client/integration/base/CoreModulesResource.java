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

import static org.apache.jena.rdf.model.ResourceFactory.createProperty;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;
import static org.apache.jena.rdf.model.ResourceFactory.createStatement;
import static org.apache.jena.rdf.model.ResourceFactory.createTypedLiteral;
import static org.junit.jupiter.api.Assertions.*;

import com.inrupt.client.Headers;
import com.inrupt.client.Request;
import com.inrupt.client.Response;
import com.inrupt.client.auth.Session;
import com.inrupt.client.jena.JenaBodyHandlers;
import com.inrupt.client.jena.JenaBodyPublishers;
import com.inrupt.client.openid.OpenIdSession;
import com.inrupt.client.solid.*;
import com.inrupt.client.util.URIBuilder;
import com.inrupt.client.vocabulary.LDP;
import com.inrupt.client.vocabulary.PIM;
import com.inrupt.client.vocabulary.Solid;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.update.UpdateFactory;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Core modules based test class for resource integration scenarios.
 */
public class CoreModulesResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoreModulesResource.class);

    private static MockSolidServer mockHttpServer;
    private static MockOpenIDProvider identityProviderServer;
    private static MockUMAAuthorizationServer authServer;
    private static MockWebIdService webIdService;
    private static final Config config = ConfigProvider.getConfig();
    private static final SolidSyncClient client = SolidSyncClient.getClient().session(Session.anonymous());
    private static String podUrl;
    private static final String MOCK_USERNAME = "someuser";
    private static final String TYPE = "type";
    private static final String LINK = "Link";
    private static URI publicContainerURI;

    private static SolidSyncClient localAuthClient;
    private static final String AUTH_METHOD = config
            .getOptionalValue("inrupt.test.auth-method", String.class)
            .orElse("client_secret_basic");
    private static final String CLIENT_ID = config.getValue("inrupt.test.client-id", String.class);
    private static final String CLIENT_SECRET = config.getValue("inrupt.test.client-secret", String.class);


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

        final String webidUrl = config
            .getOptionalValue("inrupt.test.webid", String.class)
            .orElse(webIdService.getMockServerUrl() + Utils.FOLDER_SEPARATOR + MOCK_USERNAME);

        State.WEBID = URI.create(webidUrl);
        //find storage from WebID using only core module
        final Request requestRdf = Request.newBuilder(URI.create(webidUrl)).GET().build();
        final var responseRdf = client.send(requestRdf, JenaBodyHandlers.ofModel());
        final var issuers = responseRdf.body()
                .listObjectsOfProperty(createProperty(Solid.oidcIssuer.toString()))
                .toList();
        final String issuer = issuers.get(0).toString();
        final var storages = responseRdf.body()
                .listObjectsOfProperty(createProperty(PIM.storage.toString()))
                .toList();
        if (!storages.isEmpty()) {
            podUrl = storages.get(0).toString();
        }
        final Session session = OpenIdSession.ofClientCredentials(
                URI.create(issuer), //Client credentials
                CLIENT_ID,
                CLIENT_SECRET,
                AUTH_METHOD);
        localAuthClient = SolidSyncClient.getClient().session(session);

        publicContainerURI = URIBuilder.newBuilder(URI.create(podUrl))
                .path("public-core-test-" + UUID.randomUUID() + "/")
                .build();

        Utils.createPublicContainer(localAuthClient, publicContainerURI);

        LOGGER.info("Integration Test Pod Host: [{}]", URI.create(podUrl).getHost());
    }

    @AfterAll
    static void teardown() {
        //cleanup pod
        Utils.deleteContentsRecursively(localAuthClient, publicContainerURI);

        mockHttpServer.stop();
        identityProviderServer.stop();
        authServer.stop();
        webIdService.stop();
    }

    @Test
    @DisplayName("https://w3id.org/inrupt/qa/manifest/solid-client-java/coreModulesLayerRdfSourceCrud " +
            "CRUD on RDF resource")
    void crudRdfTest() {
        LOGGER.info("Integration Test - CRUD on RDF resource");

        final String newResourceName = publicContainerURI + "e2e-test-subject-core1";
        final String newPredicateName = "https://example.example/predicate";

        //create
        final var requestCreateIfNotExist = Request.newBuilder(URI.create(newResourceName))
                .header(Utils.CONTENT_TYPE, Utils.TEXT_TURTLE)
                .header(Utils.IF_NONE_MATCH, Utils.WILDCARD)
                .header(LINK, Headers.Link.of(LDP.RDFSource, TYPE).toString())
                .PUT(Request.BodyPublishers.noBody())
                .build();
        final var resCreateIfNotExist =
                client.send(requestCreateIfNotExist, Response.BodyHandlers.discarding());

        //if the resource already exists -> we get all its statements and filter out the ones we are interested in
        List<Statement> statementsToDelete = new ArrayList<>();
        if (resCreateIfNotExist.statusCode() == Utils.PRECONDITION_FAILED) {
            final var requestRdf =
                    Request.newBuilder(URI.create(newResourceName))
                    .GET().build();
            final var responseRdf = client.send(requestRdf, JenaBodyHandlers.ofModel());
            statementsToDelete = responseRdf.body()
                    .listStatements(createResource(newResourceName),
                            createProperty(newPredicateName),
                            (org.apache.jena.rdf.model.RDFNode) null)
                    .toList();
        }

        final var statementsToAdd =
                List.of(createStatement(createResource(newResourceName),
                        createProperty(newPredicateName), createTypedLiteral(true)));

        final var sparqlQuery = deleteInsertSparqlQuery(statementsToDelete, statementsToAdd);
        final var ur = UpdateFactory.create(sparqlQuery);

        final var requestPatch = Request.newBuilder(URI.create(newResourceName))
                .header(Utils.CONTENT_TYPE, Utils.SPARQL_UPDATE)
                .method(Utils.PATCH, JenaBodyPublishers.ofUpdateRequest(ur)).build();
        final var responsePatch = client.send(requestPatch, Response.BodyHandlers.discarding());

        assertTrue(Utils.isSuccessful(responsePatch.statusCode()));

        //read
        final var reqRead = Request.newBuilder(URI.create(newResourceName))
                .GET().build();
        final var resRead = client.send(reqRead, JenaBodyHandlers.ofModel());
        assertTrue(Utils.isSuccessful(resRead.statusCode()));
        final var insertedStatement =
                resRead.body()
                        .listSubjectsWithProperty(createProperty(newPredicateName))
                        .toList();
        assertEquals(1, insertedStatement.size());

        //update
        final var reqReadAgain =
                Request.newBuilder(URI.create(newResourceName))
                .GET().build();
        final var resReadAgain = client.send(reqReadAgain, JenaBodyHandlers.ofModel());

        assertTrue(Utils.isSuccessful(resReadAgain.statusCode()));
        final List<Statement> statementsToDeleteAgain =
                resReadAgain.body().listStatements(createResource(newResourceName),
                        createProperty(newPredicateName), (RDFNode) null).toList();

        final var statementsToAddAgain =
                List.of(createStatement(createResource(newResourceName),
                        createProperty(newPredicateName), createTypedLiteral(false)));

        final var urAgain = UpdateFactory
                .create(deleteInsertSparqlQuery(statementsToDeleteAgain, statementsToAddAgain));

        final var reqPatch = Request.newBuilder(URI.create(newResourceName))
                .header(Utils.CONTENT_TYPE, Utils.SPARQL_UPDATE)
                .method(Utils.PATCH, JenaBodyPublishers.ofUpdateRequest(urAgain)).build();

        final var resPatch = client.send(reqPatch, Response.BodyHandlers.discarding());

        assertTrue(Utils.isSuccessful(resPatch.statusCode()));

        //read
        final var reqReadAgain1 = Request.newBuilder(URI.create(newResourceName))
                .GET().build();
        final var resReadAgain1 = client.send(reqReadAgain1, JenaBodyHandlers.ofModel());
        assertTrue(Utils.isSuccessful(resReadAgain1.statusCode()));
        final var insertedNewStatement =
                resReadAgain1.body()
                        .listSubjectsWithProperty(createProperty(newPredicateName))
                        .toList();
        assertEquals(1, insertedNewStatement.size());

        //delete
        final var reqDelete =
                Request.newBuilder(URI.create(newResourceName)).DELETE().build();
        final var resDelete = client.send(reqDelete, Response.BodyHandlers.discarding());

        assertTrue(Utils.isSuccessful(resDelete.statusCode()));
    }

    @Test
    @DisplayName("https://w3id.org/inrupt/qa/manifest/solid-client-java/coreModulesLayerContainerCrud" +
            " create and remove Containers")
    void containerCreateDeleteTest() {
        LOGGER.info("Integration Test - create and remove Containers");

        final String containerName = publicContainerURI + "newContainer-" + UUID.randomUUID() + "/";
        final String container2Name = "newContainer2";

        //create a Container
        final Request req = Request.newBuilder(URI.create(containerName))
                .header(Utils.CONTENT_TYPE, Utils.TEXT_TURTLE)
                .header(Utils.IF_NONE_MATCH, Utils.WILDCARD)
                .header(LINK, Headers.Link.of(LDP.BasicContainer, TYPE).toString())
                .PUT(Request.BodyPublishers.noBody())
                .build();

        final var res = client.send(req, Response.BodyHandlers.discarding());

        assertTrue(Utils.isSuccessful(res.statusCode()));

        //create a Container in a Container
        final Request reqPost = Request.newBuilder(URI.create(containerName))
                .header(Utils.CONTENT_TYPE, Utils.TEXT_TURTLE)
                .header(Utils.IF_NONE_MATCH, Utils.WILDCARD)
                .header(LINK, Headers.Link.of(LDP.BasicContainer, TYPE).toString())
                .header("Slug", container2Name)
                .POST(Request.BodyPublishers.noBody())
                .build();

        final var resPost = client.send(reqPost, Response.BodyHandlers.discarding());

        assertTrue(Utils.isSuccessful(resPost.statusCode()));

        //delete a Container
        final Request reqDeleteAgain = Request.newBuilder(URI.create(containerName + container2Name))
            .DELETE().build();
        final Response<Void> responseDeleteAgain =
                client.send(reqDeleteAgain, Response.BodyHandlers.discarding());

        assertTrue(Utils.isSuccessful(responseDeleteAgain.statusCode()));

        final Request reqDelete = Request.newBuilder(URI.create(containerName)).DELETE().build();
        final Response<Void> responseDelete =
                client.send(reqDelete, Response.BodyHandlers.discarding());

        assertTrue(Utils.isSuccessful(responseDelete.statusCode()));

    }

    @Test
    @DisplayName("https://w3id.org/inrupt/qa/manifest/solid-client-java/coreModulesLayerNonRdfSourceCrud " +
        "can create, delete, and differentiate between RDF and non-RDF Resources")
    void nonRdfTest() {
        LOGGER.info("Integration Test - create, delete, and differentiate between RDF and non-RDF Resources");

        final String fileName = "myFile.txt";
        final String fileURL = publicContainerURI + fileName;

        //create non RDF resource
        final Request reqCreate =
                Request.newBuilder(URI.create(fileURL)).header(Utils.CONTENT_TYPE, Utils.PLAIN_TEXT)
                        .PUT(Request.BodyPublishers.noBody()).build();

        final Response<Void> responseCreate =
                client.send(reqCreate, Response.BodyHandlers.discarding());

        assertTrue(Utils.isSuccessful(responseCreate.statusCode()));

        final Request req = Request.newBuilder(URI.create(fileURL)).GET().build();
        final Response<SolidRDFSource> headerResponse =
                client.send(req, SolidResourceHandlers.ofSolidRDFSource());

        assertTrue(headerResponse.body().getMetadata().getContentType().contains(Utils.PLAIN_TEXT));

        //delete non RDF resource
        final Request reqDelete = Request.newBuilder(URI.create(fileURL)).DELETE().build();
        final Response<String> responseDelete =
                client.send(reqDelete, Response.BodyHandlers.ofString());

        assertTrue(Utils.isSuccessful(responseDelete.statusCode()));
    }

    @Test
    @DisplayName("https://w3id.org/inrupt/qa/manifest/solid-client-java/coreModulesLayerBlankNodeSupport " +
        "can update statements containing Blank Nodes in different instances of the same model")
    void blankNodesTest() {
        LOGGER.info("Integration Test - update statements containing Blank Nodes " +
            "in different instances of the same model");

        final String newResourceName = publicContainerURI + "e2e-test-subject-core2";
        final String predicate = "https://example.example/predicate";
        final String predicateForBlank = "https://example.example/predicateForBlank";

        //add blank node and boolean triple to solid resource
        //we create the Solid resource if it does not exist
        final Request requestCreateIfNotExist = Request.newBuilder(URI.create(newResourceName))
                .header(Utils.CONTENT_TYPE, Utils.TEXT_TURTLE)
                .header(Utils.IF_NONE_MATCH, Utils.WILDCARD)
                .header(LINK, Headers.Link.of(LDP.RDFSource, TYPE).toString())
                .PUT(Request.BodyPublishers.noBody())
                .build();
        final Response<Void> resp =
                client.send(requestCreateIfNotExist, Response.BodyHandlers.discarding());
        assertTrue(Utils.isSuccessful(resp.statusCode()));

        //if the resource already exists -> we get all its statements and filter out the ones we are interested in
        List<Statement> statementsToDelete = new ArrayList<>();
        if (resp.statusCode() == Utils.PRECONDITION_FAILED) {
            final Request requestRdf =
                    Request.newBuilder(URI.create(newResourceName))
                    .GET().build();
            final var responseRdf = client.send(requestRdf, JenaBodyHandlers.ofModel());
            statementsToDelete = responseRdf
                    .body().listStatements(createResource(newResourceName),
                            createProperty(predicate), (org.apache.jena.rdf.model.RDFNode) null)
                    .toList();
        }

        final List<Statement> statementsToAdd = List.of(
                createStatement(createResource(newResourceName), createProperty(predicate),
                        ResourceFactory.createTypedLiteral(true)),
                createStatement(createResource(newResourceName), createProperty(predicateForBlank),
                        ResourceFactory.createResource())); //blankNode

        final var ur = UpdateFactory
                .create(deleteInsertSparqlQuery(statementsToDelete, statementsToAdd));

        final Request requestCreate = Request.newBuilder(URI.create(newResourceName))
                .header(Utils.CONTENT_TYPE, Utils.SPARQL_UPDATE)
                .method(Utils.PATCH, JenaBodyPublishers.ofUpdateRequest(ur)).build();

        final var responseCreate =
                client.send(requestCreate, Response.BodyHandlers.discarding());

        assertTrue(Utils.isSuccessful(responseCreate.statusCode()));

        //change non blank node
        //get the newly created dataset and change the non blank node
        final Request req = Request.newBuilder(URI.create(newResourceName))
                .GET().build();
        final Response<Model> res = client.send(req, JenaBodyHandlers.ofModel());

        assertTrue(Utils.isSuccessful(res.statusCode()));
        final List<Statement> statementsToDeleteAgain = res.body()
                .listStatements(createResource(newResourceName), createProperty(predicate),
                        (org.apache.jena.rdf.model.RDFNode) null)
                .toList();

        final List<Statement> statementsToAddAgain =
                List.of(createStatement(createResource(newResourceName), createProperty(predicate),
                        ResourceFactory.createTypedLiteral(false)));

        final var ur2 = UpdateFactory.create(
                deleteInsertSparqlQuery(statementsToDeleteAgain, statementsToAddAgain));

        final Request requestCreate2 = Request.newBuilder(URI.create(newResourceName))
                .header(Utils.CONTENT_TYPE, Utils.SPARQL_UPDATE)
                .method(Utils.PATCH, JenaBodyPublishers.ofUpdateRequest(ur2)).build();

        final var responseCreate2 =
                client.send(requestCreate2, Response.BodyHandlers.discarding());

        assertTrue(Utils.isSuccessful(responseCreate2.statusCode()));

        //cleanup resources
        final Request reqDelete = Request.newBuilder(URI.create(newResourceName)).DELETE().build();
        final Response<Void> responseDelete =
                client.send(reqDelete, Response.BodyHandlers.discarding());

        assertTrue(Utils.isSuccessful(responseDelete.statusCode()));
    }

    //utility method
    private String deleteInsertSparqlQuery(final List<Statement> quadsToDelete,
            final List<Statement> quadsToAdd) {
        var sparql = "";
        if (!quadsToDelete.isEmpty()) {
            sparql += "DELETE DATA { ";
            sparql +=
                    quadsToDelete.stream()
                            .map(quad -> "<" + quad.getSubject() + "> <" + quad.getPredicate()
                                    + "> '" + quad.getObject() + "'. ")
                            .collect(Collectors.joining());
            sparql += " }; \n";
        }
        if (!quadsToAdd.isEmpty()) {
            sparql += "INSERT DATA { ";
            sparql +=
                    quadsToAdd.stream()
                            .map(quad -> "<" + quad.getSubject() + "> <" + quad.getPredicate()
                                    + "> '" + quad.getObject() + "'. ")
                            .collect(Collectors.joining());
            sparql += " }";
        }
        return sparql;
    }

}

