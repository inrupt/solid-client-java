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
package com.inrupt.client.webid;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import java.util.Map;

public class WebIdMockHttpService {

    private final WireMockServer wireMockServer;

    public WebIdMockHttpService() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
    }

    public int getPort() {
        return wireMockServer.port();
    }

    private void setupMocks() {
        wireMockServer.stubFor(get(urlEqualTo("/webId"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/turtle")
                .withBody(getWebIdExample())
            )
        );
    }

    private String getWebIdExample() {
        return "<https://id.inrupt.com/username> " + 
                    " a <http://xmlns.com/foaf/0.1/Agent> ; " +
                    " <http://www.w3.org/2000/01/rdf-schema#seeAlso> <https://storage.inrupt.com/storage-id/extendedProfile> ; " + 
                    " <http://www.w3.org/ns/pim/space#storage> <https://storage.inrupt.com/storage-id/> ; " +
                    " <http://www.w3.org/ns/solid/terms#oidcIssuer> <https://login.inrupt.com> ; " +
                    " <http://xmlns.com/foaf/0.1/isPrimaryTopicOf> <https://storage.inrupt.com/storage-id/extendedProfile> . ";
    }

    public Map<String, String> start() {
        wireMockServer.start();

        setupMocks();

        return Map.of("webId_uri", wireMockServer.baseUrl());
    }

    public void stop() {
        wireMockServer.stop();
    }

}
