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
package com.inrupt.client.vc;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import java.util.Map;

class VerifiableCredentialMockService {

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";

    private final WireMockServer vcMockService;

    public VerifiableCredentialMockService() {
        vcMockService = new WireMockServer(WireMockConfiguration.options().dynamicPort());
    }

    public int getPort() {
        return vcMockService.port();
    }

    private void setupMocks() {
        vcMockService.stubFor(get(urlEqualTo("/vc"))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withBodyFile("verifiableCredential.json")));

        vcMockService.stubFor(get(urlEqualTo("/vp"))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withBodyFile("verifiablePresentation.json")));

        vcMockService.stubFor(post(urlEqualTo("/postVc"))
                    .withHeader(CONTENT_TYPE, containing(APPLICATION_JSON))
                    .willReturn(aResponse()
                        .withStatus(201)));

        vcMockService.stubFor(post(urlEqualTo("/postVp"))
                    .withHeader(CONTENT_TYPE, containing(APPLICATION_JSON))
                    .willReturn(aResponse()
                        .withStatus(201)));

        vcMockService.stubFor(post(urlEqualTo( "/credentials/verify"))
                    .withHeader("Content-Type", containing("application/json"))
                    .withRequestBody(containing("http://example.test/credentials/1872"))
                    .atPriority(1)
                    .willReturn(aResponse()
                        .withBodyFile("verificationResponse.json")
                        .withStatus(200)));
        vcMockService.stubFor(post(urlEqualTo( "/credentials/verify"))
                    .withHeader("Content-Type", containing("application/json"))
                    .willReturn(aResponse()
                        .withStatus(400)));

        vcMockService.stubFor(post(urlEqualTo( "/presentations/verify"))
                    .withHeader("Content-Type", containing("application/json"))
                    .withRequestBody(containing("http://example.test/credentials/1872"))
                    .atPriority(1)
                    .willReturn(aResponse()
                            .withBodyFile("verificationResponse.json")
                            .withStatus(200)));
        vcMockService.stubFor(post(urlEqualTo( "/presentations/verify"))
                    .withHeader("Content-Type", containing("application/json"))
                    .willReturn(aResponse()
                        .withStatus(400)));

        vcMockService.stubFor(post(urlEqualTo("/credentials/issue"))
                    .withHeader("Content-Type", containing("application/json"))
                    .withRequestBody(containing("http://example.test/credentials/1872"))
                    .atPriority(1)
                    .willReturn(aResponse()
                        .withBodyFile("verifiableCredential.json")
                        .withStatus(200)));
        vcMockService.stubFor(post(urlEqualTo("/credentials/issue"))
                    .withHeader(CONTENT_TYPE, containing(APPLICATION_JSON))
                    .willReturn(aResponse()
                        .withBodyFile("verifiableCredential.json")
                        .withStatus(400)));

        vcMockService.stubFor(post(urlEqualTo( "/credentials/status"))
                    .withHeader(CONTENT_TYPE, containing(APPLICATION_JSON))
                    .withRequestBody(containing("http://example.test/credentials/1872"))
                    .atPriority(1)
                    .willReturn(aResponse()
                        .withBody("{ \"id\": \"https://example.edu/status/24/\"," +
                        "\"type\": \"CredentialStatusList2017\" }")
                        .withStatus(200)));
        vcMockService.stubFor(post(urlEqualTo( "/credentials/status"))
                    .withHeader(CONTENT_TYPE, containing(APPLICATION_JSON))
                    .withRequestBody(containing("http://example.test/credentials/0000"))
                    .willReturn(aResponse()
                        .withStatus(404)));

        vcMockService.stubFor(get(urlPathMatching( "/credentials"))
                    .withQueryParam("type", matching("([A-Za-z0-9.,-:]*)"))
                    .atPriority(1)
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withBodyFile("verifiableCredentialList.json")));

        vcMockService.stubFor(get(urlPathMatching("/credentials/(.*)"))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withBodyFile("verifiableCredential.json")));
        vcMockService.stubFor(get(urlPathMatching("/credentials/http://example.test/credentials/0000"))
                    .willReturn(aResponse()
                        .withStatus(400)));
        vcMockService.stubFor(delete(urlPathMatching( "/credentials/(.*)"))
                    .willReturn(aResponse()
                        .withStatus(200)));

        vcMockService.stubFor(post(urlEqualTo( "/credentials/derive"))
                    .withHeader(CONTENT_TYPE, containing(APPLICATION_JSON))
                    .withRequestBody(containing("http://example.test/credentials/1872"))
                    .atPriority(1)
                    .willReturn(aResponse()
                        .withBodyFile("verifiableCredential.json")
                        .withStatus(201)));
        vcMockService.stubFor(post(urlEqualTo( "/credentials/derive"))
                    .withHeader(CONTENT_TYPE, containing(APPLICATION_JSON))
                    .willReturn(aResponse()
                        .withStatus(400)));

        vcMockService.stubFor(get(urlPathMatching("/presentations"))
                    .withQueryParam("type", matching("([A-Za-z0-9.,-:]*)"))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withBodyFile("verifiablePresentationList.json")));
        vcMockService.stubFor(get(urlPathMatching("/presentations/(.*)"))
                    .atPriority(1)
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withBodyFile("verifiablePresentation.json")));
        vcMockService.stubFor(delete(urlPathMatching( "/presentations/(.*)"))
                    .willReturn(aResponse()
                        .withStatus(200)));

        vcMockService.stubFor(post(urlEqualTo( "/presentations/prove"))
                    .withHeader("Content-Type", containing("application/json"))
                    .withRequestBody(containing("http://example.test/credentialVP/18"))
                    .atPriority(1)
                    .willReturn(aResponse()
                        .withBodyFile("verifiablePresentation.json")
                        .withStatus(201)));
        vcMockService.stubFor(post(urlEqualTo( "/presentations/prove"))
                    .withHeader(CONTENT_TYPE, containing(APPLICATION_JSON))
                    .willReturn(aResponse()
                        .withStatus(400)));

        vcMockService.stubFor(post(urlPathMatching( "/exchanges/(.*)"))
                    .withHeader("Content-Type", containing("application/json"))
                    .withRequestBody(containing("UniversityDegreeCredential"))
                    .atPriority(1)
                    .willReturn(aResponse()
                        .withBodyFile("verifiablePresentationRequest.json")
                        .withStatus(200)));
        vcMockService.stubFor(post(urlPathMatching( "/exchanges/(.*)"))
                    .withHeader(CONTENT_TYPE, containing(APPLICATION_JSON))
                    .willReturn(aResponse()
                        .withStatus(400)));
    }

    public Map<String, String> start() {
        vcMockService.start();

        setupMocks();

        return Map.of("vc_uri", vcMockService.baseUrl());
    }

    public void stop() {
        vcMockService.stop();
    }
}
