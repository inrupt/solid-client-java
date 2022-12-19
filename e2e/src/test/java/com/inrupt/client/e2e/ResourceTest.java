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
import com.inrupt.client.webid.WebIdBodyHandlers;

import io.smallrye.config.SmallRyeConfig;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.update.UpdateFactory;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ResourceTest {

    private static Config config = ConfigProvider.getConfig();
    private static SmallRyeConfig smallRyeConfig = config.unwrap(SmallRyeConfig.class);
    private static Client session = ClientProvider.getClient();

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

        final var req = Request.newBuilder(webid).header("Accept", "text/turtle").GET().build();
        final var profile = session.send(req, WebIdBodyHandlers.ofWebIdProfile(webid)).body();

        if (!profile.getStorage().isEmpty()) {
            podUrl = profile.getStorage().iterator().next().toString();
            if (!podUrl.endsWith("/")) {
                podUrl += "/";
            }
            testResource = podUrl + "resource/";
        }
    }

    @Disabled("until proper token available")
    @Test
    @DisplayName("CRUD on RDF resource")
    void createResourceTest() {
        final var newResource = testResource + "e2e-test-subject";
        final var newResourceURL = URI.create(testResource + "e2e-test-subject");
        final var predicate = "https://example.example/predicate";

        //we create the Solid resource if it does not exist
        final Request requestCreateIfNotExist = Request.newBuilder(newResourceURL)
                .header("Content-Type", "text/turtle")
                .header("If-None-Match", "*")
                .header("Link", Headers.Link.of(LDP.Resource, "type").toString())
                .PUT(Request.BodyPublishers.noBody())
                .build();
        final Response<Void> resp = session.send(requestCreateIfNotExist, Response.BodyHandlers.discarding());
        if (resp.statusCode() != 204) {
            throw new InruptClientException("Failed to create solid resource at " + newResource);
        }

        //if the resource already exists -> we get all its statements and filter out the ones we are interested in
        List<Statement> statementsToDelete = new ArrayList<>();
        if (resp.statusCode() == 412) {
            final Request requestRdf = Request.newBuilder(newResourceURL).GET().build();
            final var responseRdf = session.send(requestRdf, JenaBodyHandlers.ofModel());
            statementsToDelete = responseRdf.body().listStatements(
                    createResource(newResource),
                    createProperty(predicate),
                    (org.apache.jena.rdf.model.RDFNode)null).toList();
        }

        final List<Statement> statementsToAdd = List.of(
                createStatement(
                        createResource(newResource),
                        createProperty(predicate),
                        createTypedLiteral(true)));

        final var ur = UpdateFactory.create(createDeleteInsertSparqlQuery(statementsToDelete, statementsToAdd));
        final Request requestPatch = Request.newBuilder(newResourceURL)
            .header("Content-Type", "application/sparql-update")
            .method("PATCH", JenaBodyPublishers.ofUpdateRequest(ur)).build();
        final var responsePatch = session.send(requestPatch, Response.BodyHandlers.discarding());

        assertEquals(204, responsePatch.statusCode());

        final Request requestRdf = Request.newBuilder(newResourceURL).GET().build();
        final var responseRdf = session.send(requestRdf, JenaBodyHandlers.ofModel());
        final var insertedStatement = responseRdf
                        .body().listStatements(createResource(newResource),
                                createProperty(predicate), createTypedLiteral(true))
                        .toList();
        assertEquals(1, insertedStatement.size());

    }

    @Disabled("until proper token available")
    @Test
    @DisplayName("can create and remove Containers")
    void containerCreateDeleteTest() {
        //create
        var containerURL = testResource + "newContainer";
        if (!containerURL.endsWith("/")) {
            containerURL += "/";
        }

        final Request reqCreate = Request.newBuilder(URI.create(containerURL))
            .header("Content-Type", "text/turtle")
            .header("If-None-Match", "*")
            .header("Link", "<" + LDP.BasicContainer + ">; rel=\"type\"")
            .PUT(Request.BodyPublishers.noBody())
            .build();

        final var responseCreate =
                session.send(reqCreate, Response.BodyHandlers.discarding());

        assertEquals(204, responseCreate.statusCode());

        //TODO
        //consider creating contianer with slug

        //delete
        final Request reqDelete = Request.newBuilder(URI.create(containerURL)).DELETE().build();

        final Response<Void> responseDelete =
                session.send(reqDelete, Response.BodyHandlers.discarding());

        assertEquals(200, responseDelete.statusCode());
    }

    @Disabled("until proper token available")
    @Test
    @DisplayName("can create, delete, and differentiate between RDF and non-RDF Resources")
    void rdfNonRdfTest() {
        final var fileName = "myFile.txt";
        final var fileURL = testResource + fileName;

        //create
        final Request reqCreate = Request.newBuilder(URI.create(fileURL))
                .header("Content-Type", "text/plain")
                .PUT(Request.BodyPublishers.noBody())
                .build();

        final Response<Void> responseCreate =
                session.send(reqCreate, Response.BodyHandlers.discarding());

        assertEquals(204, responseCreate.statusCode());

        //TODO
        //test if file is a SolidResource or raw file

        //delete
        final Request reqDelete = Request.newBuilder(URI.create(fileURL)).DELETE().build();
        final Response<String> responseDelete =
                session.send(reqDelete, Response.BodyHandlers.ofString());

        assertEquals(200, responseDelete.statusCode());
    }

    @Disabled("until proper token available")
    @Test
    @DisplayName("can update statements containing Blank Nodes in different instances of the same model")
    void blankNodesTest() {
        //CREATE
        final var newResource = testResource + "e2e-test-subject";
        final var newResourceURL = URI.create(testResource + "e2e-test-subject");
        final var predicate = "https://example.example/predicate";
        final var predicateForBalnk = "https://example.example/predicateForBlank";

        //we create the Solid resource if it does not exist
        final Request requestCreateIfNotExist = Request.newBuilder(newResourceURL)
                .header("Content-Type", "text/turtle")
                .header("If-None-Match", "*")
                .header("Link", "<" + LDP.Resource + ">; rel=\"type\"")
                .PUT(Request.BodyPublishers.noBody())
                .build();
        final Response<Void> resp = session.send(requestCreateIfNotExist, Response.BodyHandlers.discarding());
        assertEquals(204, resp.statusCode());

        //if the resource already exists -> we get all its statements and filter out the ones we are interested in
        List<Statement> statementsToDelete = new ArrayList<>();
        if (resp.statusCode() == 412) {
            final Request requestRdf = Request.newBuilder(newResourceURL).GET().build();
            final var responseRdf = session.send(requestRdf, JenaBodyHandlers.ofModel());
            statementsToDelete = responseRdf.body().listStatements(
                    createResource(newResource),
                    createProperty(predicate),
                    (org.apache.jena.rdf.model.RDFNode)null).toList();
        }

        final List<Statement> statementsToAdd = List.of(
                createStatement(
                        createResource(newResource),
                        createProperty(predicate),
                        ResourceFactory.createTypedLiteral(true)),
                createStatement(
                        createResource(newResource),
                        createProperty(predicateForBalnk),
                        ResourceFactory.createResource())); //blankNode

        final var ur = UpdateFactory.create(createDeleteInsertSparqlQuery(statementsToDelete, statementsToAdd));

        final Request requestCreate = Request.newBuilder(URI.create(testResource))
            .header("Content-Type", "application/sparql-update")
            .method("PATCH", JenaBodyPublishers.ofUpdateRequest(ur)).build();

        final var responseCreate = session.send(requestCreate, Response.BodyHandlers.discarding());

        assertEquals(204, responseCreate.statusCode());

        //get the newly created dataset and change the non blank node
        final Request req = Request.newBuilder(URI.create(testResource)).GET().build();
        final Response<Model> res = session.send(req, JenaBodyHandlers.ofModel());

        assertEquals(200, res.statusCode());
        final List<Statement> statementsToDeleteAgain = res.body().listStatements(
                createResource(newResource),
                createProperty(predicate),
                (org.apache.jena.rdf.model.RDFNode)null).toList();

        final List<Statement> statementsToAddAgain = List.of(
                createStatement(
                        createResource(newResource),
                        createProperty(predicate),
                        ResourceFactory.createTypedLiteral(false)));

        final var ur2 = UpdateFactory.create(
            createDeleteInsertSparqlQuery(statementsToDeleteAgain, statementsToAddAgain));

        final Request requestCreate2 = Request.newBuilder(URI.create(testResource))
            .header("Content-Type", "application/sparql-update")
            .method("PATCH", JenaBodyPublishers.ofUpdateRequest(ur2)).build();

        final var responseCreate2 = session.send(requestCreate2, Response.BodyHandlers.discarding());

        assertEquals(204, responseCreate2.statusCode());

        //clean up -> delete all
    }

    //utility method
    private String createDeleteInsertSparqlQuery(final List<Statement> quadsToDelete,
            final List<Statement> quadsToAdd) {
        //use a SparqlBuilder....
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

