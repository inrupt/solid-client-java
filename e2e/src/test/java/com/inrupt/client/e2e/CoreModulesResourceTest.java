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
package com.inrupt.client.e2e;

import static org.apache.jena.rdf.model.ResourceFactory.createProperty;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;
import static org.apache.jena.rdf.model.ResourceFactory.createStatement;
import static org.apache.jena.rdf.model.ResourceFactory.createTypedLiteral;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.inrupt.client.Client;
import com.inrupt.client.ClientProvider;
import com.inrupt.client.Headers;
import com.inrupt.client.InruptClientException;
import com.inrupt.client.Request;
import com.inrupt.client.Response;
import com.inrupt.client.jena.JenaBodyHandlers;
import com.inrupt.client.jena.JenaBodyPublishers;
import com.inrupt.client.openid.OpenIdSession;
import com.inrupt.client.vocabulary.LDP;
import com.inrupt.client.vocabulary.PIM;

import io.smallrye.config.SmallRyeConfig;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

public class CoreModulesResourceTest {

    private static final Config config = ConfigProvider.getConfig();
    private static Client session = ClientProvider.getClient();
    private static final SmallRyeConfig smallRyeConfig = config.unwrap(SmallRyeConfig.class);

    private static String testEnv = smallRyeConfig.getValue("E2E_TEST_ENVIRONMENT", String.class);
    private static String podUrl = smallRyeConfig.getValue("E2E_TEST_ID", String.class);
    private static String testResource = "";

    @BeforeAll
    static void setup() {
        final var username = smallRyeConfig.getValue("E2E_TEST_USERNAME", String.class);
        final var sub = smallRyeConfig.getValue("E2E_TEST_USERNAME", String.class);
        final var iss = smallRyeConfig.getValue("E2E_TEST_IDP", String.class);
        final var azp = smallRyeConfig.getValue("E2E_TEST_AZP", String.class);
        if (testEnv.contains("MockSolidServer")) {
            Utils.initMockServer();
            podUrl = Utils.getMockServerUrl();
        }
        final var webid = URI.create(podUrl + "/" + username);

        //create a test claim
        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", webid.toString());
        claims.put("sub", sub);
        claims.put("iss", iss);
        claims.put("azp", azp);

        final String token = Utils.generateIdToken(claims);
        session = session.session(OpenIdSession.ofIdToken(token));

        final Request requestRdf = Request.newBuilder(webid).GET().build();
        final var responseRdf =
                session.send(requestRdf, JenaBodyHandlers.ofModel()).toCompletableFuture().join();
        final var storages = responseRdf.body()
                .listSubjectsWithProperty(createProperty(PIM.storage.toString()))
                .toList();

        if (!storages.isEmpty()) {
            podUrl = storages.get(0).toString();

        }
        if (!podUrl.endsWith("/")) {
                podUrl += "/";
            }
            testResource = podUrl + "resource/";
    }
    
    @AfterAll
    static void teardown() {
        if (testEnv.equals("MockSolidServer")) {
            Utils.stopMockServer();
        }
    }

    @Test
    @DisplayName("./solid-client-java:coreModulesLayerRdfSourceCrud CRUD on RDF resource")
    void CRUDrdfTest() {
        final String newResourceName = testResource + "e2e-test-subject";
        final String newPredicateName = "https://example.example/predicate";

        //create
        final var requestCreateIfNotExist = Request.newBuilder(URI.create(newResourceName))
                .header(Utils.CONTENT_TYPE, Utils.TEXT_TURTLE)
                .header(Utils.IF_NONE_MATCH, Utils.WILDCARD)
                .header("Link", Headers.Link.of(LDP.RDFSource, "type").toString())
                .PUT(Request.BodyPublishers.noBody())
                .build();
        final var resCreateIfNotExist =
                session.send(requestCreateIfNotExist, Response.BodyHandlers.discarding())
                        .toCompletableFuture().join();
        if (resCreateIfNotExist.statusCode() != Utils.NO_CONTENT) {
            throw new InruptClientException(
                    "Failed to create solid resource at " + newResourceName);
        }

        //if the resource already exists -> we get all its statements and filter out the ones we are interested in
        List<Statement> statementsToDelete = new ArrayList<>();
        if (resCreateIfNotExist.statusCode() == Utils.PRECONDITION_FAILED) {
            final var requestRdf =
                    Request.newBuilder(URI.create(newResourceName)).GET().build();
            final var responseRdf = session.send(requestRdf, JenaBodyHandlers.ofModel())
                    .toCompletableFuture().join();
            statementsToDelete = responseRdf.body()
                    .listStatements(createResource(newResourceName),
                            createProperty(newPredicateName),
                            (org.apache.jena.rdf.model.RDFNode) null)
                    .toList();
        }

        final var statementsToAdd =
                List.of(createStatement(createResource(newResourceName),
                        createProperty(newPredicateName), createTypedLiteral(true)));

        var sparqlQuery = deleteInsertSparqlQuery(statementsToDelete, statementsToAdd);
        final var ur = UpdateFactory.create(sparqlQuery);

        final var requestPatch = Request.newBuilder(URI.create(newResourceName))
                .header(Utils.CONTENT_TYPE, Utils.SPARQL_UPDATE)
                .method("PATCH", JenaBodyPublishers.ofUpdateRequest(ur)).build();
        final var responsePatch = session.send(requestPatch, Response.BodyHandlers.discarding())
                .toCompletableFuture().join();

        assertEquals(Utils.NO_CONTENT, responsePatch.statusCode());

        //read
        final var reqRead = Request.newBuilder(URI.create(newResourceName)).GET().build();
        final var resRead =
                session.send(reqRead, JenaBodyHandlers.ofModel()).toCompletableFuture().join();
        assertEquals(Utils.SUCCESS, resRead.statusCode());
        final var insertedStatement =
                resRead.body()
                        .listSubjectsWithProperty(createProperty(newPredicateName))
                        .toList();
        assertEquals(1, insertedStatement.size());

        //update
        final var reqReadAgain =
                Request.newBuilder(URI.create(newResourceName)).GET().build();
        final var resReadAgain = session
                .send(reqReadAgain, JenaBodyHandlers.ofModel()).toCompletableFuture().join();

        assertEquals(Utils.SUCCESS, resReadAgain.statusCode());
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
                .method("PATCH", JenaBodyPublishers.ofUpdateRequest(urAgain)).build();

        final var resPatch = session.send(reqPatch, Response.BodyHandlers.discarding())
                .toCompletableFuture().join();

        assertEquals(Utils.NO_CONTENT, resPatch.statusCode());

        //read
        final var reqReadAgain1 = Request.newBuilder(URI.create(newResourceName)).GET().build();
        final var resReadAgain1 =
                session.send(reqReadAgain1, JenaBodyHandlers.ofModel()).toCompletableFuture().join();
        assertEquals(Utils.SUCCESS, resReadAgain1.statusCode());
        final var insertedNewStatement =
                resReadAgain1.body()
                        .listSubjectsWithProperty(createProperty(newPredicateName))
                        .toList();
        assertEquals(1, insertedNewStatement.size());

        //delete
        final var reqDelete =
                Request.newBuilder(URI.create(newResourceName)).DELETE().build();
        final var resDelete =
                session.send(reqDelete, Response.BodyHandlers.discarding())
                        .toCompletableFuture().join();

        assertEquals(Utils.NO_CONTENT, resDelete.statusCode());
    }

    @Test
    @DisplayName("./solid-client-java:coreModulesLayerContainerCrud can create and remove Containers")
    void vontainerCreateDeleteTest() {

        final String containerName = testResource + "newContainer/";
        final String container2Name = testResource + "newContainer2/";

        //create a Container
        final Request req = Request.newBuilder(URI.create(containerName))
                .header(Utils.CONTENT_TYPE, Utils.TEXT_TURTLE)
                .header(Utils.IF_NONE_MATCH, Utils.WILDCARD)
                .header("Link", Headers.Link.of(LDP.BasicContainer, "type").toString())
                .PUT(Request.BodyPublishers.noBody())
                .build();

        final var res = session.send(req, Response.BodyHandlers.discarding()).toCompletableFuture().join();

        assertEquals(Utils.NO_CONTENT, res.statusCode());
    
        //create a Container in a Container
        final Request reqPost = Request.newBuilder(URI.create(container2Name))
                .header(Utils.CONTENT_TYPE, Utils.TEXT_TURTLE)
                .header(Utils.IF_NONE_MATCH, Utils.WILDCARD)
                .header("Link", Headers.Link.of(LDP.BasicContainer, "type").toString())
                .header("Slug", "newContainer")
                .POST(Request.BodyPublishers.noBody())
                .build();

        final var resPost = session.send(reqPost, Response.BodyHandlers.discarding()).toCompletableFuture().join();

        assertEquals(Utils.NO_CONTENT, resPost.statusCode());

        //delete a Containers
        final Request reqDelete = Request.newBuilder(URI.create(containerName)).DELETE().build();
        final Response<Void> responseDelete =
                session.send(reqDelete, Response.BodyHandlers.discarding()).toCompletableFuture().join();

        assertEquals(Utils.NO_CONTENT, responseDelete.statusCode());

        final Request reqDeleteAgain = Request.newBuilder(URI.create(container2Name)).DELETE().build();
        final Response<Void> responseDeleteAgain =
                session.send(reqDeleteAgain, Response.BodyHandlers.discarding()).toCompletableFuture().join();

        assertEquals(Utils.NO_CONTENT, responseDeleteAgain.statusCode());
    }

    @Test
    @DisplayName("./solid-client-java:coreModulesLayerNonRdfSourceCrud " +
        "can create, delete, and differentiate between RDF and non-RDF Resources")
    void nonRdfTest() {
        final String fileName = "myFile.txt";
        final String fileURL = testResource + fileName;

        //create non RDF resource
        final Request reqCreate =
                Request.newBuilder(URI.create(fileURL)).header(Utils.CONTENT_TYPE, Utils.PLAIN_TEXT)
                        .PUT(Request.BodyPublishers.noBody()).build();

        final Response<Void> responseCreate =
                session.send(reqCreate, Response.BodyHandlers.discarding()).toCompletableFuture().join();

        assertEquals(Utils.NO_CONTENT, responseCreate.statusCode());

        //TODO - does not throw
        //test if file is a SolidResource or raw file and read and update
        //final Request req = Request.newBuilder(URI.create(fileURL)).GET().build();
        //assertThrows(IOException.class, () -> session.send(req, JenaBodyHandlers.ofModel()));

        //delete non RDF resource
        final Request reqDelete = Request.newBuilder(URI.create(fileURL)).DELETE().build();
        final Response<String> responseDelete =
                session.send(reqDelete, Response.BodyHandlers.ofString()).toCompletableFuture().join();

        assertEquals(Utils.NO_CONTENT, responseDelete.statusCode());
    }

    @Test
    @DisplayName("./solid-client-java:coreModulesLayerBlankNodeSupport " +
        "can update statements containing Blank Nodes in different instances of the same model")
    void blankNodesTest() {

        final String newResourceName = testResource + "e2e-test-subject";
        final String predicate = "https://example.example/predicate";
        final String predicateForBlank = "https://example.example/predicateForBlank";

        //add blank node and boolean triple to solid resource
        //we create the Solid resource if it does not exist
        final Request requestCreateIfNotExist = Request.newBuilder(URI.create(newResourceName))
                .header(Utils.CONTENT_TYPE, Utils.TEXT_TURTLE)
                .header(Utils.IF_NONE_MATCH, Utils.WILDCARD)
                .header("Link", Headers.Link.of(LDP.RDFSource, "type").toString())
                .PUT(Request.BodyPublishers.noBody())
                .build();
        final Response<Void> resp =
                session.send(requestCreateIfNotExist, Response.BodyHandlers.discarding())
                    .toCompletableFuture().join();
        assertEquals(Utils.NO_CONTENT, resp.statusCode());

        //if the resource already exists -> we get all its statements and filter out the ones we are interested in
        List<Statement> statementsToDelete = new ArrayList<>();
        if (resp.statusCode() == Utils.PRECONDITION_FAILED) {
            final Request requestRdf = Request.newBuilder(URI.create(newResourceName)).GET().build();
            final var responseRdf = session.send(requestRdf, JenaBodyHandlers.ofModel())
                                    .toCompletableFuture().join();
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
                .method("PATCH", JenaBodyPublishers.ofUpdateRequest(ur)).build();

        final var responseCreate =
                session.send(requestCreate, Response.BodyHandlers.discarding()).toCompletableFuture().join();

        assertEquals(Utils.NO_CONTENT, responseCreate.statusCode());

        //change non blank node
        //get the newly created dataset and change the non blank node
        final Request req = Request.newBuilder(URI.create(newResourceName)).GET().build();
        final Response<Model> res = session.send(req, JenaBodyHandlers.ofModel()).toCompletableFuture().join();

        assertEquals(Utils.SUCCESS, res.statusCode());
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
                .method("PATCH", JenaBodyPublishers.ofUpdateRequest(ur2)).build();

        final var responseCreate2 =
                session.send(requestCreate2, Response.BodyHandlers.discarding()).toCompletableFuture().join();

        assertEquals(Utils.NO_CONTENT, responseCreate2.statusCode());

        //cleanup resources
        final Request reqDelete = Request.newBuilder(URI.create(newResourceName)).DELETE().build();
        final Response<Void> responseDelete =
                session.send(reqDelete, Response.BodyHandlers.discarding()).toCompletableFuture().join();

        assertEquals(Utils.NO_CONTENT, responseDelete.statusCode());
    }

    //utility method
    private String deleteInsertSparqlQuery(final List<Statement> quadsToDelete,
            final List<Statement> quadsToAdd) {
        //TODO use a SparqlBuilder
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

