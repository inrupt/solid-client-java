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
import com.inrupt.client.rdf.Dataset;
import com.inrupt.client.solid.SolidClient;
import com.inrupt.client.solid.SolidResource;
import com.inrupt.client.vocabulary.LDP;
import com.inrupt.client.webid.WebIdBodyHandlers;

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

public class DomainModulesResourceTest {

    private static Config config = ConfigProvider.getConfig();
    private static Client http = ClientProvider.getClient();
    private static SolidClient client;
    private static SmallRyeConfig smallRyeConfig = config.unwrap(SmallRyeConfig.class);

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
        http = http.session(OpenIdSession.ofIdToken(token));
        client = SolidClient.of(http);

        final var req = Request.newBuilder(webid).header("Accept", "text/turtle").GET().build();
        final var profile = http.send(req, WebIdBodyHandlers.ofWebIdProfile(webid)).body();

        if (!profile.getStorage().isEmpty()) {
            podUrl = profile.getStorage().iterator().next().toString();
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

            final List<Statement> statementsToAdd = List.of(createStatement(createResource(newResourceName),
                    createProperty(newPredicateName), createTypedLiteral(true)));
            final Dataset dataset;
            SolidResource newResource =
                    SolidResource.newResourceBuilder().dataset(null).build(URI.create(newResourceName));
            final var newResourceCreated = client.create(newResource)
                    .toCompletableFuture().join();
            
        }

        @Test
        @DisplayName("read RDF resource")
        void readResourceTest() {

        }

        @Test
        @DisplayName("update RDF resource")
        void updateResourceTest() {
        }

        @Test
        @DisplayName("delete RDF resource")
        void deleteResourceTest() {
        }

    }

    @Nested
    @Disabled()
    @DisplayName("./solid-client-java:baseContainerCrud can create and remove Containers")
    class ContainerCreateDeleteTest {

        final String containerURL = testResource + "newContainer/";
        final String containerURL2 = testResource + "newContainer2/";

        @Test
        @DisplayName("create a Container")
        void createContainer() {
        }
        
        @Test
        @DisplayName("create a Container in a Container")
        void createSlugInContainer() {
        }

        @Test
        @DisplayName("delete a Container")
        @ParameterizedTest
        @MethodSource
        void deleteContainer(String containerURL) {
        }

        private Stream<Arguments> deleteContainer() {
            return Stream.of(
            Arguments.of(containerURL),
            Arguments.of(containerURL2)
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

        }
        //TODO
        //test if file is a SolidResource or raw file and read and update - for now we have:
        @Test
        void throwOnNonRDFTest() {
        }

        @Test
        @DisplayName("delete non RDF resource")
        void deleteNonRDFTest() {
        }
    }

    @Disabled()
    @Nested
    @DisplayName("./solid-client-java:coreModulesLayerBlankNodeSupport " +
        "can update statements containing Blank Nodes in different instances of the same model")
    class BlankNodesTest {

        final String newResource = testResource + "e2e-test-subject";
        final String predicate = "https://example.example/predicate";
        final String predicateForBlank = "https://example.example/predicateForBlank";

        @Test
        @DisplayName("add blank node and boolean triple to solid resource")
        void createSolidResource() {
        }

        @Test
        @DisplayName("change non blank node")
        void changeNonBlankNode() {
        }

        @Test
        @DisplayName("cleanup resources")
        void deleteResources() {
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

