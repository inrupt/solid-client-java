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
package com.inrupt.client.examples.spring.web;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

class MockWebServer {

    private final WireMockServer wireMockServer;

    public MockWebServer() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options()
                .dynamicPort());
    }

    public void start() {

        wireMockServer.start();

        wireMockServer.stubFor(get(urlEqualTo("/user"))
                .willReturn(aResponse()
                    .withHeader("Content-Type", "text/turtle")
                    .withBody("""
                        <> <http://www.w3.org/ns/pim/space#storage> </storage/>;
                        <http://www.w3.org/ns/solid/terms#oidcIssuer> </oidc> .""")));

        wireMockServer.stubFor(get(urlEqualTo("/storage/"))
                .willReturn(aResponse()
                    .withHeader("Content-Type", "text/turtle")
                    .withBody("""
                        <> <http://www.w3.org/ns/ldp#contains> <resource1> , <resource2> , <resource3> .
                       """)));
    }

    public String baseUrl() {
        return wireMockServer.baseUrl();
    }

    public void stop() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }
}
