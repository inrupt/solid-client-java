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
package com.inrupt.client.httpclient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import java.util.Map;

class MockHttpServer {

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String TEXT_TURTLE = "text/turtle";

    private final WireMockServer wireMockServer;

    public MockHttpServer() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options()
                .dynamicPort());
    }

    public int getPort() {
        return wireMockServer.port();
    }

    private void setupMocks() {

        wireMockServer.stubFor(get(urlEqualTo("/file"))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, "text/plain")
                        .withBodyFile("clarissa-sample.txt")));

        wireMockServer.stubFor(get(urlEqualTo("/example"))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, TEXT_TURTLE)
                        .withBodyFile("profileExample.ttl")));

        wireMockServer.stubFor(post(urlEqualTo("/rdf"))
                    .withRequestBody(equalTo(
                            "<http://example.test/s> " +
                            "<http://example.test/p> \"object\" ."))
                    .withHeader(CONTENT_TYPE, containing(TEXT_TURTLE))
                    .willReturn(aResponse()
                        .withStatus(201)));

        wireMockServer.stubFor(get(urlEqualTo("/solid.png"))
                    .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, "image/png")
                        .withBodyFile("SolidOS.png")
                        .withStatus(200)));

        wireMockServer.stubFor(patch(urlEqualTo("/rdf"))
                    .withRequestBody(equalTo(
                            "INSERT DATA { " +
                            "<http://example.test/s> " +
                            "<http://example.test/p> \"data\" . }"))
                    .withHeader(CONTENT_TYPE, containing("application/sparql-update"))
                    .willReturn(aResponse()
                        .withStatus(204)));

        wireMockServer.stubFor(delete(urlEqualTo("/rdf"))
                    .willReturn(aResponse()
                        .withStatus(204)));
    }

    public Map<String, String> start() {
        wireMockServer.start();

        setupMocks();

        return Map.of("http_uri", wireMockServer.baseUrl());
    }

    public void stop() {
        wireMockServer.stop();
    }

}
