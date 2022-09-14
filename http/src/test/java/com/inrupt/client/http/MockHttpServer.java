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
                        .withBodyFile("clarissa-sample.txt")));

        wireMockServer.stubFor(get(urlEqualTo("/example"))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/turtle")
                        .withBody(getProfileExampleTTL())));

        wireMockServer.stubFor(post(urlEqualTo("/postOneTriple"))
                    .withRequestBody(matching(
                            ".*<http://example.test/s>\\s+" +
                            "<http://example.test/p>\\s+\"object\"\\s+\\..*"))
                    .withHeader("Content-Type", containing("text/turtle"))
                    .willReturn(aResponse()
                        .withStatus(204)));

        wireMockServer.stubFor(get(urlEqualTo("/solid.png"))
                    .willReturn(aResponse()
                        .withStatus(200)));

    }

    private static String getProfileExampleTTL() {
        try (final var res = MockHttpServer.class.getResourceAsStream("/profileExample.ttl")) {
            return new String(res.readAllBytes(), UTF_8);
        } catch (final IOException ex) {
            throw new UncheckedIOException("Could not read class resource", ex);
        }
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
