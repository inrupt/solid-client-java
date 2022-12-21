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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.inrupt.client.Client;
import com.inrupt.client.ClientProvider;
import com.inrupt.client.Request;
import com.inrupt.client.Response;
import com.inrupt.client.openid.OpenIdSession;
import com.inrupt.client.rdf.Dataset;
import com.inrupt.client.rdf.RDFFactory;
import com.inrupt.client.rdf.RDFNode;
import com.inrupt.client.solid.SolidClient;
import com.inrupt.client.solid.SolidContainer;
import com.inrupt.client.solid.SolidResource;
import com.inrupt.client.webid.WebIdBodyHandlers;
import com.inrupt.client.webid.WebIdProfile;

import io.smallrye.config.SmallRyeConfig;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class DomainModulesResourceTest {

    private static Config config = ConfigProvider.getConfig();
    private static Client http = ClientProvider.getClient();
    private static SolidClient client;
    private static SmallRyeConfig smallRyeConfig = config.unwrap(SmallRyeConfig.class);

    private static String podUrl = "";
    private static String testResource = "";
    private static final URI webid = URI.create(smallRyeConfig.getValue("webid", String.class));

    @BeforeAll
    static void setup() {

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

    @Test
    @DisplayName("./solid-client-java:baseRdfSourceCrud CRUD on RDF resource")
    @Disabled()
    void crudRdfTest() {
        final String newResourceName = testResource + "e2e-test-subject";
        final String newPredicateName = "https://example.example/predicate";

        final RDFNode newResourceNode = RDFNode.namedNode(URI.create(newResourceName));
        final RDFNode newPredicateNode = RDFNode.namedNode(URI.create(newPredicateName));
        final RDFNode object =
                RDFNode.literal("true", URI.create("http://www.w3.org/2001/XMLSchema#boolean"));

        final Dataset dataset = RDFFactory.createDataset();
        dataset.add(RDFFactory.createQuad(newResourceNode, newPredicateNode, object));

        SolidResource newResource =
                SolidResource.newResourceBuilder().dataset(dataset)
                        .build(URI.create(newResourceName));

        assertDoesNotThrow(client.create(newResource).toCompletableFuture()::join);

        newResource = client.read(URI.create(newResourceName), SolidResource.class)
                        .toCompletableFuture().join();
        assertEquals(URI.create(newResourceName), newResource.getIdentifier());
        assertEquals(1, newResource.getDataset().stream().count());

        final RDFNode newObject =
                RDFNode.literal("false", URI.create("http://www.w3.org/2001/XMLSchema#boolean"));
        final Dataset newDataset = RDFFactory.createDataset();
        newResource.getDataset().stream(null, newResourceNode, null, null)
                .map(quad ->
                    RDFFactory.createQuad(quad.getSubject(), quad.getPredicate(), newObject))
                .collect(Collectors.toList()).forEach(newDataset::add);
        final SolidResource updatedResource =
                SolidResource.newResourceBuilder().dataset(newDataset)
                        .build(URI.create(newResourceName));
        assertDoesNotThrow(client.update(updatedResource).toCompletableFuture()::join);

        assertDoesNotThrow(client.delete(updatedResource).toCompletableFuture()::join);
    }

    @Test
    @Disabled()
    @DisplayName("./solid-client-java:baseContainerCrud can create and remove Containers")
    void containerCreateDeleteTest() {

        final String containerURL = testResource + "newContainer/";

        final SolidContainer newContainer =
                SolidContainer.newContainerBuilder().build(URI.create(containerURL));
        assertDoesNotThrow(client.create(newContainer).toCompletableFuture()::join);

        assertDoesNotThrow(client.delete(newContainer).toCompletableFuture()::join);
    }

    @Disabled()
    @Test
    @DisplayName("./solid-client-java:blankNodeSupport " +
        "can update statements containing Blank Nodes in different instances of the same model")
    void blankNodesTest() {

        final String newResourceName = testResource + "e2e-test-subject";
        final String predicateName = "https://example.example/predicate";
        final String predicateForBlankName = "https://example.example/predicateForBlank";

        final Dataset dataset = RDFFactory.createDataset();

        final RDFNode newResourceNode = RDFNode.namedNode(URI.create(newResourceName));
        final RDFNode newPredicateNode = RDFNode.namedNode(URI.create(predicateName));
        final RDFNode object =
                RDFNode.literal("true", URI.create("http://www.w3.org/2001/XMLSchema#boolean"));

        dataset.add(RDFFactory.createQuad(newResourceNode, newPredicateNode, object));

        final RDFNode newBNPredicateNode = RDFNode.namedNode(URI.create(predicateForBlankName));
        final RDFNode bn =
                RDFNode.blankNode("blank");
        dataset.add(RDFFactory.createQuad(newResourceNode, newBNPredicateNode, bn));

        SolidResource newResource =
                SolidResource.newResourceBuilder().dataset(dataset)
                        .build(URI.create(newResourceName));

        assertDoesNotThrow(client.create(newResource).toCompletableFuture()::join);

        newResource = client.read(URI.create(newResourceName), SolidResource.class)
                        .toCompletableFuture().join();
        assertEquals(URI.create(newResourceName), newResource.getIdentifier());
        assertEquals(2, newResource.getDataset().stream().count());

        final RDFNode newObject =
                RDFNode.literal("false", URI.create("http://www.w3.org/2001/XMLSchema#boolean"));
        final Dataset newDataset = RDFFactory.createDataset();

        final var allQuads = newResource.getDataset().stream().collect(Collectors.toList());
        final var toDeleteQuads =
                newResource.getDataset()
                        .stream(null, newResourceNode, newPredicateNode, null)
                        .collect(Collectors.toList());
        final var toAddQuad = RDFFactory.createQuad(newResourceNode, newPredicateNode, newObject);

        toDeleteQuads.forEach(allQuads::remove);
        allQuads.add(toAddQuad);
        allQuads.forEach(newDataset::add);

        final SolidResource updatedResource =
                SolidResource.newResourceBuilder().dataset(newDataset)
                        .build(URI.create(newResourceName));
        assertDoesNotThrow(client.update(updatedResource).toCompletableFuture()::join);

        assertDoesNotThrow(client.delete(updatedResource).toCompletableFuture()::join);
    }

    @Test
    @DisplayName("./solid-client-java:podStorageFinding find pod storage from webID")
    void findStorageTest() {
        final var req = Request.newBuilder(webid).header("Accept", "text/turtle").GET().build();
        final Response<WebIdProfile> res = http.send(req, WebIdBodyHandlers.ofWebIdProfile(webid));

        assertEquals(200, res.statusCode());
        assertFalse(res.body().getStorage().isEmpty());
    }

    @Test
    @DisplayName("./solid-client-java:ldpNavigation navigate to a container's lefes")
    void ldpNavigationTest() {

        final String startingResourceName = podUrl + "/testContainer/anotherContainer/endResource";

        final SolidResource startingResource = client.read(URI.create(startingResourceName), SolidResource.class)
                .toCompletableFuture().join();

        if (startingResource.getMetadata().getStorage().isPresent()) {
            assertEquals(podUrl, startingResource.getMetadata().getStorage().get().toString());
        }

    }
}