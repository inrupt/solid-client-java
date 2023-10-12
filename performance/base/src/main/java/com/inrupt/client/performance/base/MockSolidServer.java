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
package com.inrupt.client.performance.base;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.inrupt.client.Request;

class MockSolidServer {

    private final WireMockServer wireMockServer;
    private final String USER_AGENT_HEADER = "User-Agent";
    private static final String USER_AGENT = "InruptJavaClient/" + Request.class
        .getPackage().getImplementationVersion();

    public MockSolidServer() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
    }

    private void setupMocks() {
        wireMockServer.stubFor(head(anyUrl())
                .withHeader(USER_AGENT_HEADER, equalTo(USER_AGENT))
                .willReturn(aResponse()
                        .withStatus(Utils.SUCCESS)));
        wireMockServer.stubFor(get(anyUrl())
                .withHeader(USER_AGENT_HEADER, equalTo(USER_AGENT))
                .willReturn(aResponse()
                        .withStatus(Utils.SUCCESS)
                        .withHeader(Utils.CONTENT_TYPE, Utils.PLAIN_TEXT)
                        .withBody(new byte[0])));
        wireMockServer.stubFor(put(anyUrl())
                .withHeader(USER_AGENT_HEADER, equalTo(USER_AGENT))
                .willReturn(aResponse()
                    .withStatus(Utils.NO_CONTENT)));
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
