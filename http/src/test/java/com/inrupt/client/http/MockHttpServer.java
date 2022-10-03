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
package com.inrupt.client.http;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

class MockHttpServer {

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
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
                        .withHeader(CONTENT_TYPE, TEXT_TURTLE)
                        .withBodyFile("clarissa-sample.txt")));

        wireMockServer.stubFor(get(urlEqualTo("/example"))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, TEXT_TURTLE)
                        .withBodyFile("profileExample.ttl")));

        wireMockServer.stubFor(post(urlEqualTo("/postOneTriple"))
                    .withRequestBody(matching(
                            ".*<http://example.test/s>\\s+" +
                            "<http://example.test/p>\\s+\"object\"\\s+\\..*"))
                    .withHeader(CONTENT_TYPE, containing(TEXT_TURTLE))
                    .withHeader("Authorization", containing("Bearer "))
                    .willReturn(aResponse()
                        .withStatus(201)));

        wireMockServer.stubFor(post(urlEqualTo("/postOneTriple"))
                    .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("WWW-Authenticate",
                            "UMA ticket=\"ticket-12345\", as_uri=\"" + wireMockServer.baseUrl() + "\"")));

        wireMockServer.stubFor(get(urlEqualTo("/solid.png"))
                    .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, "image/png")
                        .withBodyFile("SolidOS.png")
                        .withStatus(200)));

        wireMockServer.stubFor(get(urlEqualTo("/.well-known/uma2-configuration"))
                    .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(getResource("/uma2-configuration.json", wireMockServer.baseUrl()))));

        wireMockServer.stubFor(get(urlEqualTo("/uma/jwks"))
                    .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(getResource("/uma-jwks.json"))));

        wireMockServer.stubFor(post(urlEqualTo("/uma/token"))
                    .withRequestBody(containing("ticket=ticket-12345"))
                    .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody("{\"access_token\":\"token-12345\",\"token_type\":\"Bearer\"}")));

        wireMockServer.stubFor(get(urlEqualTo("/protected/resource"))
                    .withHeader("Accept", containing(TEXT_TURTLE))
                    .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("WWW-Authenticate",
                            "UMA ticket=\"ticket-12345\", as_uri=\"" + wireMockServer.baseUrl() + "\"")));

        wireMockServer.stubFor(get(urlEqualTo("/protected/resource"))
                    .withHeader("Authorization", equalTo("Bearer token-12345"))
                    .withHeader("Accept", containing(TEXT_TURTLE))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, TEXT_TURTLE)
                        .withBodyFile("profileExample.ttl")));

    }

    private static String getResource(final String path) {
        try (final var res = MockHttpServer.class.getResourceAsStream(path)) {
            return new String(res.readAllBytes(), UTF_8);
        } catch (final IOException ex) {
            throw new UncheckedIOException("Could not read class resource", ex);
        }
    }

    private static String getResource(final String path, final String baseUrl) {
        return getResource(path).replace("{{baseUrl}}", baseUrl);
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
