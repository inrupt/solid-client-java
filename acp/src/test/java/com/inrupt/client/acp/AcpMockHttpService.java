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
package com.inrupt.client.acp;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import java.net.URI;

public class AcpMockHttpService {

    private final WireMockServer wireMockServer;

    public AcpMockHttpService() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
    }

    public URI acr1() {
        return URI.create(wireMockServer.baseUrl() + "/acr-1");
    }

    public URI acr2() {
        return URI.create(wireMockServer.baseUrl() + "/acr-2");
    }

    private void setupMocks() {
        wireMockServer.stubFor(get(urlEqualTo("/acr-1"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/turtle")
                .withBodyFile("acr-1.ttl")
            )
        );
        wireMockServer.stubFor(get(urlEqualTo("/acr-2"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/turtle")
                .withBodyFile("acr-2.ttl")
            )
        );
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

