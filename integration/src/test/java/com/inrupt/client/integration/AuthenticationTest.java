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
package com.inrupt.client.integration;

import static org.apache.jena.rdf.model.ResourceFactory.createProperty;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.inrupt.client.Client;
import com.inrupt.client.ClientProvider;
import com.inrupt.client.Request;
import com.inrupt.client.jena.JenaBodyHandlers;
import com.inrupt.client.openid.OpenIdSession;
import com.inrupt.client.vocabulary.PIM;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AuthenticationTest {

    private static Config config = ConfigProvider.getConfig();
    private static Client session = ClientProvider.getClient();

    private static String testEnv = config.getValue("E2E_TEST_ENVIRONMENT", String.class);
    private static String podUrl = config.getValue("E2E_TEST_ID", String.class);
    private static String testResource = "";

    @BeforeAll
    static void setup() {
        final var username = config.getValue("E2E_TEST_USERNAME", String.class);
        final var iss = config.getValue("E2E_TEST_IDP", String.class);
        final var azp = config.getValue("E2E_TEST_AZP", String.class);
        if (testEnv.contains("MockSolidServer")) {
            Utils.initMockServer();
            podUrl = Utils.getMockServerUrl();
        }
        final var webid = URI.create(podUrl + "/" + username);
        //create a test claim
        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", webid.toString());
        claims.put("sub", username);
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

    @Test
    @DisplayName(":unauthenticatedPrivateNode Unauth fetch of private resource")
    void fetchPrivateResourceUnauthenticatedTest() {
        final Client client = ClientProvider.getClient();
        final Request request = Request.newBuilder(URI.create(testResource)).GET().build();
        final var response = client.send(request, JenaBodyHandlers.ofModel()).toCompletableFuture().join();

        assertEquals(404, response.statusCode());
    }
}
