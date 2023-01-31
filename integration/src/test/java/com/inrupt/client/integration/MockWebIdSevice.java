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

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import org.apache.commons.io.IOUtils;

public class MockWebIdSevice {

    private final WireMockServer wireMockServer;

    public MockWebIdSevice() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
    }

    private void setupMocks() {
        wireMockServer.stubFor(get(urlEqualTo("/" + Utils.USERNAME))
            .willReturn(aResponse()
                .withStatus(Utils.SUCCESS)
                .withHeader(Utils.CONTENT_TYPE, Utils.TEXT_TURTLE)
                .withBody(getResource("/webId.ttl",
                    wireMockServer.baseUrl() + "/" + Utils.USERNAME,
                    Utils.POD_URL,
                    Utils.ISS))));
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

    private String getResource(final String path, final String webidUrl, final String podUrl, final String issuer) {
        return getResource(path)
                .replace("{{webidUrl}}", webidUrl)
                .replace("{{podUrl}}", podUrl)
                .replace("{{issuer}}", issuer);
    }

    private String getResource(final String path) {
        try (final InputStream res = MockWebIdSevice.class.getResourceAsStream(path)) {
            return new String(IOUtils.toByteArray(res), UTF_8);
        } catch (final IOException ex) {
            throw new UncheckedIOException("Could not read class resource", ex);
        }
    }
}
