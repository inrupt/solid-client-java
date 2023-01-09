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

import com.inrupt.client.Request;
import com.inrupt.client.jena.JenaBodyHandlers;
import com.inrupt.client.openid.OpenIdSession;
import com.inrupt.client.solid.SolidSyncClient;
import com.inrupt.client.vocabulary.PIM;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AuthenticationTest {

    private static final Config config = ConfigProvider.getConfig();
    private static SolidSyncClient session;

    private static String testEnv = config.getValue("inrupt.test.environment", String.class);
    private static String podUrl = config.getValue("inrupt.test.id", String.class);
    private static String testResource = "";

    @BeforeAll
    static void setup() {
        final var username = config.getValue("inrupt.test.username", String.class);
        final var iss = config.getValue("inrupt.test.idp", String.class);
        final var azp = config.getValue("inrupt.test.azp", String.class);
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
        session = SolidSyncClient.getClient().session(OpenIdSession.ofIdToken(token));

        final Request requestRdf = Request.newBuilder(webid).GET().build();
        final var responseRdf = session.send(requestRdf, JenaBodyHandlers.ofModel());
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
        final SolidSyncClient client = SolidSyncClient.getClient();
        final Request request = Request.newBuilder(URI.create(testResource)).GET().build();
        final var response = client.send(request, JenaBodyHandlers.ofModel());

        assertEquals(404, response.statusCode());
    }
}
