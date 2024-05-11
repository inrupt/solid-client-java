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
package com.inrupt.client.openid;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

class OpenIdMockHttpService {

    private static final String WEBID = "https://id.example/username";
    private static final String SUB = "username";
    private static final String ISS = "https://iss.example";
    private static final String AZP = "https://app.example";

    private final WireMockServer wireMockServer;

    public OpenIdMockHttpService() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options()
                .dynamicPort());
    }

    public int getPort() {
        return wireMockServer.port();
    }

    private void setupMocks() {

        wireMockServer.stubFor(get(urlEqualTo("/jwks"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBodyFile("jwks.json")));

        wireMockServer.stubFor(get(urlEqualTo("/jwks-other"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBodyFile("jwks-other.json")));

        wireMockServer.stubFor(get(urlPathMatching("/not-found.*"))
                .willReturn(aResponse()
                    .withStatus(404)));

        wireMockServer.stubFor(get(urlEqualTo("/.well-known/openid-configuration"))
                    .atPriority(1)
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(getMetadataJSON())));

        wireMockServer.stubFor(post(urlPathMatching("/token"))
                    .withHeader("Content-Type", containing("application/x-www-form-urlencoded"))
                    .withRequestBody(containing("myCodeverifier"))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(getTokenResponseJSON())));
        wireMockServer.stubFor(post(urlPathMatching("/token"))
                .withHeader("Content-Type", containing("application/x-www-form-urlencoded"))
                    .atPriority(1)
                    .withRequestBody(containing("code=none"))
                    .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("token-error.json")));

        wireMockServer.stubFor(post(urlPathMatching("/token"))
                .withHeader("Content-Type", containing("application/x-www-form-urlencoded"))
                    .atPriority(2)
                    .withRequestBody(containing("grant_type=client_credentials"))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(getTokenResponseJSON())));
    }

    private String getMetadataJSON() {
        try (final InputStream res = OpenIdMockHttpService.class.getResourceAsStream("/metadata.json")) {
            return new String(IOUtils.toByteArray(res), UTF_8)
                .replace("http://example.test", wireMockServer.baseUrl());
        } catch (final IOException ex) {
            throw new UncheckedIOException("Could not read class resource", ex);
        }
    }

    private String getTokenResponseJSON() {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", WEBID);
        claims.put("sub", SUB);
        claims.put("iss", ISS);
        claims.put("azp", AZP);

        return "{" +
            "\"access_token\": \"123456\"," +
            "\"id_token\": \"" + OpenIdTestUtils.generateIdToken(claims) + "\"," +
            "\"token_type\": \"Bearer\"," +
            "\"scope\": \"openid webid\"," +
            "\"expires_in\": 300" +
            "}";
    }

    public String start() {
        wireMockServer.start();

        setupMocks();

        return wireMockServer.baseUrl();
    }

    public void stop() {
        wireMockServer.stop();
    }
}
