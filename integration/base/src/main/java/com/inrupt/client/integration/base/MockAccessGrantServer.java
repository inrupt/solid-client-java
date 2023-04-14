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
package com.inrupt.client.integration.base;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import org.apache.commons.io.IOUtils;

class MockAccessGrantServer {

    private final WireMockServer wireMockServer;
    private String webId;
    private String sharedFile;

    public MockAccessGrantServer(final String webId, final String sharedFile) {
        this.webId = webId;
        this.sharedFile = sharedFile;
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort()
        .notifier(new ConsoleNotifier(true))
        );
    }

    private void setupMocks() {
        wireMockServer.stubFor(get(urlEqualTo(Utils.VC_DISCOVERY_ENDPOINT))
                .willReturn(aResponse()
                    .withStatus(Utils.SUCCESS)
                    .withHeader(Utils.CONTENT_TYPE, Utils.APPLICATION_JSON)
                    .withBody(getResource("/vc-configuration.json", wireMockServer.baseUrl()))));

        wireMockServer.stubFor(get(urlPathMatching(".+:(\\d*)/vc-grant"))
                    .willReturn(aResponse()
                        .withStatus(Utils.SUCCESS)
                        .withHeader(Utils.CONTENT_TYPE, Utils.APPLICATION_JSON)
                        .withBody(getResource("/vc-grant.json", wireMockServer.baseUrl(),
                            this.webId, this.sharedFile))));

        wireMockServer.stubFor(delete(urlPathMatching("/vc-grant"))
                    .willReturn(aResponse()
                        .withStatus(204)));

        wireMockServer.stubFor(post(urlEqualTo("/issue"))
                    .willReturn(aResponse()
                        .withStatus(Utils.SUCCESS)
                        .withHeader(Utils.CONTENT_TYPE, Utils.APPLICATION_JSON)
                        .withBody(getResource("/vc-grant.json", wireMockServer.baseUrl(),
                            this.webId, this.sharedFile))));

        wireMockServer.stubFor(post(urlEqualTo("/status"))
                    .withRequestBody(containing("\"https://accessgrant.example/status/CVAM#2832\""))
                    .willReturn(aResponse()
                        .withStatus(Utils.NO_CONTENT)));

        wireMockServer.stubFor(post(urlEqualTo("/derive"))
                    .withRequestBody(containing("\"Read\""))
                    .withRequestBody(containing("\"" + this.webId + "\""))
                    .willReturn(aResponse()
                        .withStatus(Utils.SUCCESS)
                        .withHeader(Utils.CONTENT_TYPE, Utils.APPLICATION_JSON)
                        .withBody(getResource("/query_response.json", wireMockServer.baseUrl(),
                            this.webId, this.sharedFile))));

        wireMockServer.stubFor(post(urlEqualTo("/derive"))
                    .withRequestBody(containing("\"https://someuser.test\""))
                    .withRequestBody(containing("\"Read\""))
                    .willReturn(aResponse()
                        .withStatus(Utils.SUCCESS)
                        .withHeader(Utils.CONTENT_TYPE, Utils.APPLICATION_JSON)
                        .withBody(getResource("/query_response_empty.json"))));

        wireMockServer.stubFor(post(urlEqualTo("/derive"))
                    .withRequestBody(containing("\"Write\""))
                    .withRequestBody(containing(this.webId))
                    .willReturn(aResponse()
                        .withStatus(Utils.SUCCESS)
                        .withHeader(Utils.CONTENT_TYPE, Utils.APPLICATION_JSON)
                        .withBody(getResource("/query_response_empty.json"))));

        wireMockServer.stubFor(post(urlEqualTo("/derive"))
                        .withRequestBody(containing("\"Read\""))
                        .withRequestBody(containing("\"https://somerandom.test\""))
                        .withRequestBody(containing(this.webId))
                        .willReturn(aResponse()
                            .withStatus(Utils.SUCCESS)
                            .withHeader(Utils.CONTENT_TYPE, Utils.APPLICATION_JSON)
                            .withBody(getResource("/query_response_empty.json"))));

    }

    private String getResource(final String path) {
        try (final InputStream res = MockAccessGrantServer.class.getResourceAsStream(path)) {
            return new String(IOUtils.toByteArray(res), UTF_8);
        } catch (final IOException ex) {
            throw new UncheckedIOException("Could not read class resource", ex);
        }
    }

    private String getResource(final String path, final String baseUrl) {
        return getResource(path).replace("{{baseUrl}}", baseUrl);
    }

    private String getResource(final String path, final String baseUrl, final String webId, final String sharedFile) {
        return getResource(path).replace("{{baseUrl}}", baseUrl)
            .replace("{{webId}}", webId)
            .replace("{{sharedFile}}", sharedFile);
    }

    public void start() {
        wireMockServer.start();
        setupMocks();
    }

    public String getMockServerUrl() {
        return wireMockServer.baseUrl();
    }

    public void stop() {
        wireMockServer.stop();
    }

}

