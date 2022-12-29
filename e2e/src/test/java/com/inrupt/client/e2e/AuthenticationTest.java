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

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.inrupt.client.Client;
import com.inrupt.client.ClientProvider;
import com.inrupt.client.Request;
import com.inrupt.client.jena.JenaBodyHandlers;
import com.inrupt.client.openid.OpenIdSession;
import com.inrupt.client.webid.WebIdBodyHandlers;

import io.smallrye.config.SmallRyeConfig;

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
    private static SmallRyeConfig smallRyeConfig = config.unwrap(SmallRyeConfig.class);
    private static Client session = ClientProvider.getClient();

    private static String podUrl = "";
    private static String testResource = "";

    @BeforeAll
    static void setup() {
        final var webid = URI.create(smallRyeConfig.getValue("E2E_TEST_WEBID", String.class));
        final var sub = smallRyeConfig.getValue("E2E_TEST_USERNAME", String.class);
        final var iss = smallRyeConfig.getValue("E2E_TEST_IDP", String.class);
        final var azp = smallRyeConfig.getValue("E2E_TEST_AZP", String.class);

        //create a test claim
        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", webid.toString());
        claims.put("sub", sub);
        claims.put("iss", iss);
        claims.put("azp", azp);

        final String token = Utils.generateIdToken(claims);
        session = session.session(OpenIdSession.ofIdToken(token));

        final var req = Request.newBuilder(webid).header("Accept", "text/turtle").GET().build();
        final var profile = session.send(req, WebIdBodyHandlers.ofWebIdProfile(webid))
                            .toCompletableFuture().join().body();

        if (!profile.getStorage().isEmpty()) {
            podUrl = profile.getStorage().iterator().next().toString();
            if (!podUrl.endsWith("/")) {
                podUrl += "/";
            }
            testResource = podUrl + "resource/";
        }
    }

    @Test
    @DisplayName(":unauthenticatedPrivateNode Unauth fetch of private resource")
    void fetchPrivateResourceUnauthenticatedTest() {
        final Client client = ClientProvider.getClient();
        final Request request = Request.newBuilder(URI.create(testResource)).GET().build();
        final var response = client.send(request, JenaBodyHandlers.ofModel()).toCompletableFuture().join();

        assertEquals(401, response.statusCode());
    }
}
