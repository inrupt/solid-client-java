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
import static org.junit.jupiter.api.Assertions.assertThrows;

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

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.update.UpdateFactory;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class CoreModulesResourceTest {

    private static final Config config = ConfigProvider.getConfig();
    private static Client session = ClientProvider.getClient();
    private static final SmallRyeConfig smallRyeConfig = config.unwrap(SmallRyeConfig.class);

    private static String podUrl = "";
    private static String testResource = "";

    @BeforeAll
    static void setup() {
        final var webid = URI.create(smallRyeConfig.getValue("webid", String.class));
        final var sub = smallRyeConfig.getValue("username", String.class);
        final var iss = smallRyeConfig.getValue("issuer", String.class);
        final var azp = smallRyeConfig.getValue("azp", String.class);

        //create a test claim
        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", webid.toString());
        claims.put("sub", sub);
        claims.put("iss", iss);
        claims.put("azp", azp);

        final String token = Utils.generateIdToken(claims);
        session = session.session(OpenIdSession.ofIdToken(token));

        final Request requestRdf = Request.newBuilder(webid).GET().build();
        final var responseRdf = session.send(requestRdf, JenaBodyHandlers.ofModel());
        final var storages = responseRdf.body().listStatements(null,
                createProperty(PIM.storage.toString()), (org.apache.jena.rdf.model.RDFNode) null)
                .toList();

        if (!storages.isEmpty()) {
            podUrl = storages.get(0).toString();
            if (!podUrl.endsWith("/")) {
                podUrl += "/";
            }
            testResource = podUrl + "resource/";
        }
    }

    @Nested
    @DisplayName("./solid-client-java:coreModulesLayerRdfSourceCrud CRUD on RDF resource")
    @Disabled()
    class CRUDrdfTest {
        final String newResourceName = testResource + "e2e-test-subject";
        final String newPredicateName = "https://example.example/predicate";

        @Test
        @DisplayName("create RDF resource")
        void createResourceTest() {
            //we create the Solid resource if it does not exist
            final Request requestCreateIfNotExist = Request.newBuilder(URI.create(newResourceName))
                    .header("Content-Type", "text/turtle").header("If-None-Match", "*")
                    .header("Link", Headers.Link.of(LDP.Resource, "type").toString())
                    .PUT(Request.BodyPublishers.noBody()).build();
            final var res =
                    session.send(requestCreateIfNotExist, Response.BodyHandlers.discarding());
            if (res.statusCode() != 204) {
                throw new InruptClientException("Failed to create solid resource at " + newResourceName);
            }

            //if the resource already exists -> we get all its statements and filter out the ones we are interested in
            List<Statement> statementsToDelete = new ArrayList<>();
            if (res.statusCode() == 412) {
                final Request requestRdf = Request.newBuilder(URI.create(newResourceName)).GET().build();
                final var responseRdf = session.send(requestRdf, JenaBodyHandlers.ofModel());
                statementsToDelete = responseRdf.body().listStatements(createResource(newResourceName),
                        createProperty(newPredicateName), (org.apache.jena.rdf.model.RDFNode) null)
                        .toList();
            }

            final List<Statement> statementsToAdd = List.of(createStatement(createResource(newResourceName),
                    createProperty(newPredicateName), createTypedLiteral(true)));

            final var ur = UpdateFactory
                    .create(deleteInsertSparqlQuery(statementsToDelete, statementsToAdd));

            final Request requestPatch = Request.newBuilder(URI.create(newResourceName))
                    .header("Content-Type", "application/sparql-update")
                    .method("PATCH", JenaBodyPublishers.ofUpdateRequest(ur)).build();
            final var responsePatch =
                    session.send(requestPatch, Response.BodyHandlers.discarding());

            assertEquals(204, responsePatch.statusCode());
        }

        @Test
        @DisplayName("read RDF resource")
        void readResourceTest() {
            final Request req = Request.newBuilder(URI.create(newResourceName)).GET().build();
            final var res = session.send(req, JenaBodyHandlers.ofModel());
            final var insertedStatement = res.body().listStatements(createResource(newResourceName),
                    createProperty(newPredicateName), createTypedLiteral(true)).toList();
            assertEquals(1, insertedStatement.size());
        }

        @Test
        @DisplayName("update RDF resource")
        void updateResourceTest() {
            //get the newly created dataset and change the non blank node
            final Request req = Request.newBuilder(URI.create(newResourceName)).GET().build();
            final Response<Model> res = session.send(req, JenaBodyHandlers.ofModel());

            assertEquals(200, res.statusCode());
            final List<Statement> statementsToDelete =
                    res.body().listStatements(createResource(newResourceName), createProperty(newPredicateName),
                            (org.apache.jena.rdf.model.RDFNode) null).toList();

            final List<Statement> statementsToAdd = List.of(createStatement(createResource(newResourceName),
                    createProperty(newPredicateName), createTypedLiteral(false)));

            final var ur = UpdateFactory
                    .create(deleteInsertSparqlQuery(statementsToDelete, statementsToAdd));

            final Request reqPatch = Request.newBuilder(URI.create(testResource))
                    .header("Content-Type", "application/sparql-update")
                    .method("PATCH", JenaBodyPublishers.ofUpdateRequest(ur)).build();

            final var resPatch = session.send(reqPatch, Response.BodyHandlers.discarding());

            assertEquals(204, resPatch.statusCode());
        }

        @Test
        @DisplayName("delete RDF resource")
        void deleteResourceTest() {
            final Request req = Request.newBuilder(URI.create(newResourceName)).DELETE().build();
            final Response<Void> res = session.send(req, Response.BodyHandlers.discarding());

            assertEquals(200, res.statusCode());
        }

    }

    @Nested
    @Disabled()
    @DisplayName("./solid-client-java:coreModulesLayerContainerCrud can create and remove Containers")
    class ContainerCreateDeleteTest {

        final String containerName = testResource + "newContainer/";
        final String container2Name = testResource + "newContainer2/";

        @Test
        @DisplayName("create a Container")
        void createContainer() {
            final Request req = Request.newBuilder(URI.create(containerName))
                    .header("Content-Type", "text/turtle")
                    .header("If-None-Match", "*")
                    .header("Link", "<" + LDP.BasicContainer + ">; rel=\"type\"")
                    .PUT(Request.BodyPublishers.noBody()).build();

            final var resp = session.send(req, Response.BodyHandlers.discarding());

            assertEquals(204, resp.statusCode());

        }

        @Test
        @DisplayName("create a Container in a Container")
        void createSlugInContainer() {
            final Request req = Request.newBuilder(URI.create(container2Name))
                    .header("Content-Type", "text/turtle")
                    .header("If-None-Match", "*")
                    .header("Link", "<" + LDP.BasicContainer + ">; rel=\"type\"")
                    .header("Slug", "newContainer")
                    .POST(Request.BodyPublishers.noBody()).build();

            final var resp = session.send(req, Response.BodyHandlers.discarding());

            assertEquals(204, resp.statusCode());

        }

        @Test
        @DisplayName("delete a Container")
        @ParameterizedTest
        @MethodSource
        void deleteContainer(final String containerURL) {
            final Request reqDelete = Request.newBuilder(URI.create(containerURL)).DELETE().build();
            final Response<Void> responseDelete =
                    session.send(reqDelete, Response.BodyHandlers.discarding());

            assertEquals(200, responseDelete.statusCode());
        }

        private Stream<Arguments> deleteContainer() {
            return Stream.of(
            Arguments.of(containerName),
            Arguments.of(container2Name)
            );
        }
    }

    @Disabled()
    @Nested
    @DisplayName("./solid-client-java:coreModulesLayerNonRdfSourceCrud " +
        "can create, delete, and differentiate between RDF and non-RDF Resources")
    class NonRdfTest {
        final String fileName = "myFile.txt";
        final String fileURL = testResource + fileName;

        @Test
        @DisplayName("create non RDF resource")
        void createNonRDFTest() {
            final Request reqCreate =
                    Request.newBuilder(URI.create(fileURL)).header("Content-Type", "text/plain")
                            .PUT(Request.BodyPublishers.noBody()).build();

            final Response<Void> responseCreate =
                    session.send(reqCreate, Response.BodyHandlers.discarding());

            assertEquals(204, responseCreate.statusCode());

        }
        //TODO
        //test if file is a SolidResource or raw file and read and update - for now we have:
        @Test
        void throwOnNonRDFTest() {
            final Request req = Request.newBuilder(URI.create(fileURL)).GET().build();
            assertThrows(IOException.class, () -> session.send(req, JenaBodyHandlers.ofModel()));
        }

        @Test
        @DisplayName("delete non RDF resource")
        void deleteNonRDFTest() {
            final Request reqDelete = Request.newBuilder(URI.create(fileURL)).DELETE().build();
            final Response<String> responseDelete =
                    session.send(reqDelete, Response.BodyHandlers.ofString());

            assertEquals(200, responseDelete.statusCode());
        }
    }

    @Disabled()
    @Nested
    @DisplayName("./solid-client-java:coreModulesLayerBlankNodeSupport " +
        "can update statements containing Blank Nodes in different instances of the same model")
    class BlankNodesTest {

        final String newResourceName = testResource + "e2e-test-subject";
        final String predicate = "https://example.example/predicate";
        final String predicateForBlank = "https://example.example/predicateForBlank";

        @Test
        @DisplayName("add blank node and boolean triple to solid resource")
        void createSolidResource() {
            //we create the Solid resource if it does not exist
            final Request requestCreateIfNotExist = Request.newBuilder(URI.create(newResourceName))
                    .header("Content-Type", "text/turtle").header("If-None-Match", "*")
                    .header("Link", "<" + LDP.Resource + ">; rel=\"type\"")
                    .PUT(Request.BodyPublishers.noBody()).build();
            final Response<Void> resp =
                    session.send(requestCreateIfNotExist, Response.BodyHandlers.discarding());
            assertEquals(204, resp.statusCode());

            //if the resource already exists -> we get all its statements and filter out the ones we are interested in
            List<Statement> statementsToDelete = new ArrayList<>();
            if (resp.statusCode() == 412) {
                final Request requestRdf = Request.newBuilder(URI.create(newResourceName)).GET().build();
                final var responseRdf = session.send(requestRdf, JenaBodyHandlers.ofModel());
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

            final Request requestCreate = Request.newBuilder(URI.create(testResource))
                    .header("Content-Type", "application/sparql-update")
                    .method("PATCH", JenaBodyPublishers.ofUpdateRequest(ur)).build();

            final var responseCreate =
                    session.send(requestCreate, Response.BodyHandlers.discarding());

            assertEquals(204, responseCreate.statusCode());
        }

        @Test
        @DisplayName("change non blank node")
        void changeNonBlankNode() {
            //get the newly created dataset and change the non blank node
            final Request req = Request.newBuilder(URI.create(testResource)).GET().build();
            final Response<Model> res = session.send(req, JenaBodyHandlers.ofModel());

            assertEquals(200, res.statusCode());
            final List<Statement> statementsToDeleteAgain = res.body()
                    .listStatements(createResource(newResourceName), createProperty(predicate),
                            (org.apache.jena.rdf.model.RDFNode) null)
                    .toList();

            final List<Statement> statementsToAddAgain =
                    List.of(createStatement(createResource(newResourceName), createProperty(predicate),
                            ResourceFactory.createTypedLiteral(false)));

            final var ur2 = UpdateFactory.create(
                    deleteInsertSparqlQuery(statementsToDeleteAgain, statementsToAddAgain));

            final Request requestCreate2 = Request.newBuilder(URI.create(testResource))
                    .header("Content-Type", "application/sparql-update")
                    .method("PATCH", JenaBodyPublishers.ofUpdateRequest(ur2)).build();

            final var responseCreate2 =
                    session.send(requestCreate2, Response.BodyHandlers.discarding());

            assertEquals(204, responseCreate2.statusCode());
        }

        @Test
        @DisplayName("cleanup resources")
        void deleteResources() {
            final Request reqDelete = Request.newBuilder(URI.create(newResourceName)).DELETE().build();
            final Response<Void> responseDelete =
                    session.send(reqDelete, Response.BodyHandlers.discarding());

            assertEquals(200, responseDelete.statusCode());
        }
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
                                    + "> " + quad.getObject() + "'. ")
                            .collect(Collectors.joining());
            sparql += " }; \n";
        }
        if (!quadsToAdd.isEmpty()) {
            sparql += "INSERT DATA { ";
            sparql +=
                    quadsToAdd.stream()
                            .map(quad -> "<" + quad.getSubject() + "> <" + quad.getPredicate()
                                    + "> " + quad.getObject() + "'. ")
                            .collect(Collectors.joining());
            sparql += " }";
        }
        return sparql;
    }

}

