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
package com.inrupt.client.accessgrant;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import org.apache.commons.io.IOUtils;

class MockAccessGrantServer {

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";

    private final WireMockServer wireMockServer;

    public MockAccessGrantServer() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
    }

    public String start() {
        wireMockServer.start();

        setupMocks();

        return wireMockServer.baseUrl();
    }

    public void stop() {
        wireMockServer.stop();
    }

    private void setupMocks() {
        wireMockServer.stubFor(get(urlEqualTo("/.well-known/vc-configuration"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .withBody(getResource("/vc-configuration.json", wireMockServer.baseUrl()))));

        wireMockServer.stubFor(get(urlEqualTo("/access-grant-1"))
                    .atPriority(1)
                    .withHeader("Authorization", containing("Bearer eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9."))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(getResource("/vc-1.json", wireMockServer.baseUrl()))));

        wireMockServer.stubFor(get(urlEqualTo("/access-grant-1"))
                    .atPriority(2)
                    .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("WWW-Authenticate", "Bearer,DPoP algs=\"ES256\"")));

        wireMockServer.stubFor(delete(urlEqualTo("/access-grant-1"))
                    .atPriority(1)
                    .withHeader("Authorization", containing("Bearer eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9."))
                    .willReturn(aResponse()
                        .withStatus(204)));

        wireMockServer.stubFor(delete(urlEqualTo("/access-grant-1"))
                    .atPriority(2)
                    .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("WWW-Authenticate", "Bearer,DPoP algs=\"ES256\"")));

        wireMockServer.stubFor(get(urlEqualTo("/access-grant-2"))
                    .atPriority(1)
                    .withHeader("Authorization", containing("Bearer eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9."))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(getResource("/vc-2.json", wireMockServer.baseUrl()))));

        wireMockServer.stubFor(get(urlEqualTo("/access-grant-2"))
                    .atPriority(2)
                    .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("WWW-Authenticate", "Bearer,DPoP algs=\"ES256\"")));

        wireMockServer.stubFor(delete(urlEqualTo("/access-grant-2"))
                    .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("WWW-Authenticate", "Bearer,DPoP algs=\"ES256\"")));

        wireMockServer.stubFor(get(urlEqualTo("/access-request-5"))
                    .atPriority(1)
                    .withHeader("Authorization", containing("Bearer eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9."))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(getResource("/vc-5.json", wireMockServer.baseUrl()))));

        wireMockServer.stubFor(get(urlEqualTo("/access-request-5"))
                    .atPriority(2)
                    .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("WWW-Authenticate", "Bearer,DPoP algs=\"ES256\"")));

        wireMockServer.stubFor(delete(urlEqualTo("/access-request-5"))
                    .atPriority(1)
                    .withHeader("Authorization", containing("Bearer eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9."))
                    .willReturn(aResponse()
                        .withStatus(204)));

        wireMockServer.stubFor(delete(urlEqualTo("/access-request-5"))
                    .atPriority(2)
                    .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("WWW-Authenticate", "Bearer,DPoP algs=\"ES256\"")));

        wireMockServer.stubFor(get(urlEqualTo("/access-grant-6"))
                    .atPriority(1)
                    .withHeader("Authorization", containing("Bearer eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9."))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(getResource("/vc-6.json", wireMockServer.baseUrl()))));

        wireMockServer.stubFor(get(urlEqualTo("/access-grant-6"))
                    .atPriority(2)
                    .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("WWW-Authenticate", "Bearer,DPoP algs=\"ES256\"")));

        wireMockServer.stubFor(delete(urlEqualTo("/access-grant-6"))
                    .atPriority(1)
                    .withHeader("Authorization", containing("Bearer eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9."))
                    .willReturn(aResponse()
                        .withStatus(204)));

        wireMockServer.stubFor(delete(urlEqualTo("/access-grant-6"))
                    .atPriority(2)
                    .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("WWW-Authenticate", "Bearer,DPoP algs=\"ES256\"")));

        wireMockServer.stubFor(get(urlEqualTo("/vc-3"))
                    .atPriority(1)
                    .withHeader("Authorization", containing("Bearer eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9."))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(getResource("/vc-3.json", wireMockServer.baseUrl()))));

        wireMockServer.stubFor(get(urlEqualTo("/vc-3"))
                    .atPriority(2)
                    .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("WWW-Authenticate", "Bearer,DPoP algs=\"ES256\"")));

        wireMockServer.stubFor(get(urlMatching("/not-found.*"))
                    .willReturn(aResponse()
                        .withStatus(404)));

        // Access Denial
        wireMockServer.stubFor(post(urlEqualTo("/issue"))
                    .atPriority(1)
                    .withHeader("Authorization", containing("Bearer eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9."))
                    .withRequestBody(containing("\"providedConsent\""))
                    .withRequestBody(containing("\"2022-09-12T12:00:00Z\""))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(getResource("/vc-7.json", wireMockServer.baseUrl()))));

        // Access Request
        wireMockServer.stubFor(post(urlEqualTo("/issue"))
                    .atPriority(1)
                    .withHeader("Authorization", containing("Bearer eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9."))
                    .withRequestBody(containing("\"hasConsent\""))
                    .withRequestBody(containing("\"2022-09-12T12:00:00Z\""))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(getResource("/vc-8.json", wireMockServer.baseUrl()))));

        // Access Grant
        wireMockServer.stubFor(post(urlEqualTo("/issue"))
                    .atPriority(1)
                    .withHeader("Authorization", containing("Bearer eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9."))
                    .withRequestBody(containing("\"providedConsent\""))
                    .withRequestBody(containing("\"2022-08-27T12:00:00Z\""))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(getResource("/vc-4.json", wireMockServer.baseUrl()))));

        // Access Request
        wireMockServer.stubFor(post(urlEqualTo("/issue"))
                    .atPriority(1)
                    .withHeader("Authorization", containing("Bearer eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9."))
                    .withRequestBody(containing("\"hasConsent\""))
                    .withRequestBody(containing("\"2022-08-27T12:00:00Z\""))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(getResource("/vc-5.json", wireMockServer.baseUrl()))));

        wireMockServer.stubFor(post(urlEqualTo("/issue"))
                    .atPriority(2)
                    .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("WWW-Authenticate", "Bearer,DPoP algs=\"ES256\"")));

        wireMockServer.stubFor(post(urlEqualTo("/verify"))
                    .atPriority(1)
                    .withHeader("Authorization", containing("Bearer eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9."))
                    .withRequestBody(containing("\"" + wireMockServer.baseUrl() + "/access-grant-1\""))
                    .withRequestBody(containing("\"verifiableCredential\":{\""))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(getResource("/verify-results.json"))));

        wireMockServer.stubFor(post(urlEqualTo("/verify"))
                    .atPriority(1)
                    .withHeader("Authorization", containing("Bearer eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9."))
                    .withRequestBody(containing("\"" + wireMockServer.baseUrl() + "/access-request-5\""))
                    .withRequestBody(containing("\"verifiableCredential\":{\""))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(getResource("/verify-results.json"))));

        wireMockServer.stubFor(post(urlEqualTo("/verify"))
                    .atPriority(2)
                    .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("WWW-Authenticate", "Bearer,DPoP algs=\"ES256\"")));

        wireMockServer.stubFor(post(urlEqualTo("/status"))
                    .atPriority(1)
                    .withHeader("Authorization", containing("Bearer eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9."))
                    .withRequestBody(containing("\"status\":\"1\""))
                    .withRequestBody(containing("\"" + wireMockServer.baseUrl() + "/access-grant-1\""))
                    .willReturn(aResponse()
                        .withStatus(204)));

        wireMockServer.stubFor(post(urlEqualTo("/status"))
                    .atPriority(1)
                    .withHeader("Authorization", containing("Bearer eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9."))
                    .withRequestBody(containing("\"" + wireMockServer.baseUrl() + "/access-grant-2\""))
                    .willReturn(aResponse()
                        .withStatus(403)));

        wireMockServer.stubFor(post(urlEqualTo("/status"))
                    .atPriority(2)
                    .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("WWW-Authenticate", "Bearer,DPoP algs=\"ES256\"")));

        wireMockServer.stubFor(post(urlEqualTo("/derive"))
                    .atPriority(1)
                    .withHeader("Authorization", containing("Bearer eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9."))
                    .withRequestBody(containing("SolidAccessDenial"))
                    .withRequestBody(containing(
                            "\"https://storage.example/ef9c4b90-0459-408d-bfa9-1c61d46e1eaf/\""))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(getResource("/query_response6.json", wireMockServer.baseUrl()))));

        wireMockServer.stubFor(post(urlEqualTo("/derive"))
                    .atPriority(1)
                    .withHeader("Authorization", containing("Bearer eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9."))
                    .withRequestBody(containing("SolidAccessRequest"))
                    .withRequestBody(containing(
                            "\"https://storage.example/f1759e6d-4dda-4401-be61-d90d070a5474/\""))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(getResource("/query_response3.json", wireMockServer.baseUrl()))));

        wireMockServer.stubFor(post(urlEqualTo("/derive"))
                    .atPriority(1)
                    .withHeader("Authorization", containing("Bearer eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9."))
                    .withRequestBody(containing("SolidAccessGrant"))
                    .withRequestBody(containing(
                            "\"https://storage.example/e973cc3d-5c28-4a10-98c5-e40079289358/\""))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(getResource("/query_response1.json", wireMockServer.baseUrl()))));

        wireMockServer.stubFor(post(urlEqualTo("/derive"))
                    .atPriority(2)
                    .withHeader("Authorization", containing("Bearer eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9."))
                    .withRequestBody(containing("\"isProvidedTo\":\"https://id.test/user\""))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(getResource("/query_response4.json", wireMockServer.baseUrl()))));

        wireMockServer.stubFor(post(urlEqualTo("/derive"))
                    .atPriority(2)
                    .withHeader("Authorization", containing("Bearer eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9."))
                    .withRequestBody(containing("\"isConsentForDataSubject\":\"https://id.test/user\""))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(getResource("/query_response5.json", wireMockServer.baseUrl()))));

        wireMockServer.stubFor(post(urlEqualTo("/derive"))
                    .atPriority(2)
                    .withHeader("Authorization", containing("Bearer eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9."))
                    .withRequestBody(containing("\"https://storage.example/"))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(getResource("/query_response2.json", wireMockServer.baseUrl()))));

        wireMockServer.stubFor(post(urlEqualTo("/derive"))
                    .atPriority(3)
                    .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("WWW-Authenticate", "Bearer,DPoP algs=\"ES256\"")));
    }

    private static String getResource(final String path) {
        try (final InputStream res = MockAccessGrantServer.class.getResourceAsStream(path)) {
            return new String(IOUtils.toByteArray(res), UTF_8);
        } catch (final IOException ex) {
            throw new UncheckedIOException("Could not read class resource", ex);
        }
    }

    private static String getResource(final String path, final String baseUrl) {
        return getResource(path).replace("{{baseUrl}}", baseUrl);
    }


}

