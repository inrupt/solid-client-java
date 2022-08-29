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
package com.inrupt.client.rdf4j;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import java.util.Map;

class MockHttpClient {

    private final WireMockServer wireMockServer;

    public MockHttpClient() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options()
                .dynamicPort());
    }

    public int getPort() {
        return wireMockServer.port();
    }

    private void setupMocks() {
        wireMockServer.stubFor(get(urlEqualTo("/test"))
                    .willReturn(ok()
                        .withBody("testResponse")));

        wireMockServer.stubFor(get(urlEqualTo("/oneTriple"))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/turtle")
                        //.withBody("@prefix ex: <http://example.com/> ex:subject ex:predicate ex:object .")));
                        .withBody("<http://example.com/s> <http://example.com/p> <http://example.com/o> .")));

        wireMockServer.stubFor(get(urlEqualTo("/example"))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/turtle; charset=UTF-8")
                        .withBody(getExampleTTL())));
    }

    private String getExampleTTL() {
        return "" +
            "@prefix : <#>." +
            "\n @prefix foaf: <http://xmlns.com/foaf/0.1/>." +
            "\n @prefix schema: <http://schema.org/>." +
            "\n @prefix solid: <http://www.w3.org/ns/solid/terms#>." +
            "\n @prefix space: <http://www.w3.org/ns/pim/space#>." +
            "\n @prefix prof: <./>." +
            "\n @prefix tim: </>." +
            "\n" +
            "\n prof:card" +
            "\n    a foaf:PersonalProfileDocument; " +
            "\n    foaf:maker :me; " +
            "\n    foaf:primaryTopic :me." +
            "\n" +
            "\n :me" +
            "\n    a schema:Person, foaf:Person;" +
            "\n    space:preferencesFile </settings/prefs.ttl>;" +
            "\n    space:storage tim:;" +
            "\n    solid:account tim:;" +
            "\n    solid:privateTypeIndex </settings/privateTypeIndex.ttl>;" +
            "\n    solid:publicTypeIndex </settings/publicTypeIndex.ttl>;" +
            "\n    foaf:name \"Jane Doe\";" +
            "\n    solid:oidcIssuer <https://solidcommunity.net>.";
    }

    public Map<String, String> start() {
        wireMockServer.start();

        setupMocks();

        return Map.of("httpMock_uri", wireMockServer.baseUrl());
    }

    public void stop() {
        wireMockServer.stop();
    }

}
