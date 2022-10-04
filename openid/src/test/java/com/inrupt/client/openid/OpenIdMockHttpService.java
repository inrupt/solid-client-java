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
package com.inrupt.client.openid;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

class OpenIdMockHttpService {

    private final WireMockServer wireMockServer;

    public OpenIdMockHttpService() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options()
                .dynamicPort());
    }

    public int getPort() {
        return wireMockServer.port();
    }

    private void setupMocks() {

        wireMockServer.stubFor(get(urlEqualTo("/.well-known/openid-configuration"))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(getMetadataJSON()
                                    .replace(
                                        "/oauth/oauth20/token",
                                        wireMockServer.baseUrl() +  "/oauth/oauth20/token"))));

        wireMockServer.stubFor(post(urlPathMatching("/oauth/oauth20/token"))
                    .withHeader("Content-Type", containing("application/x-www-form-urlencoded"))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(getTokenResponseJSON())));
    }

    private String getMetadataJSON() {
        try (final var res = OpenIdMockHttpService.class.getResourceAsStream("/metadata.json")) {
            return new String(res.readAllBytes(), UTF_8);
        } catch (final IOException ex) {
            throw new UncheckedIOException("Could not read class resource", ex);
        }
    }

    private String getTokenResponseJSON() {
        try (final var res = OpenIdMockHttpService.class.getResourceAsStream("/tokenResponse.json")) {
            return new String(res.readAllBytes(), UTF_8);
        } catch (final IOException ex) {
            throw new UncheckedIOException("Could not read class resource", ex);
        }
    }

    public Map<String, String> start() {
        wireMockServer.start();

        setupMocks();

        return Map.of("openid_uri", wireMockServer.baseUrl());
    }

    public void stop() {
        wireMockServer.stop();
    }
}
