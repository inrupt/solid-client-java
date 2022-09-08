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

class RDF4JMockHttpService {

    private final WireMockServer wireMockServer;

    public RDF4JMockHttpService() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options()
                .dynamicPort());
    }

    public int getPort() {
        return wireMockServer.port();
    }

    private void setupMocks() {

        wireMockServer.stubFor(get(urlEqualTo("/oneTriple"))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/turtle")
                        .withBody("<http://example.test/s> <http://example.test/p> <http://example.test/o> .")));

        wireMockServer.stubFor(post(urlEqualTo("/postOneTriple"))
                    .withRequestBody(matching(
                            ".*<http://example.test/subject>\\s+" +
                            "<http://example.test/predicate>\\s+\"object\"\\s+\\..*"))
                    .withHeader("Content-Type", containing("text/turtle"))
                    .willReturn(aResponse()
                        .withStatus(204)));

        wireMockServer.stubFor(get(urlEqualTo("/example"))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/turtle")
                        .withBody(getExampleTTL())));

        wireMockServer.stubFor(patch(urlEqualTo("/sparqlUpdate"))
                    .withHeader("Content-Type", containing("application/sparql-update"))
                    .withRequestBody(containing(
                        "INSERT DATA { <http://example.test/s1> <http://example.test/p1> <http://example.test/o1> .}"))
                    .willReturn(aResponse()
                        .withStatus(204)));
    }

    private String getExampleTTL() {
        return "" +
            "@prefix : <http://example.test/>." +
            "\n @prefix foaf: <http://xmlns.com/foaf/0.1/>." +
            "\n @prefix schema: <http://schema.org/>." +
            "\n @prefix solid: <http://www.w3.org/ns/solid/terms#>." +
            "\n @prefix space: <http://www.w3.org/ns/pim/space#>." +
            "\n" +
            "\n :me" +
            "\n    a schema:Person, foaf:Person;" +
            "\n    space:preferencesFile <http://example.test//settings/prefs.ttl>;" +
            "\n    solid:privateTypeIndex <http://example.test//settings/privateTypeIndex.ttl>;" +
            "\n    solid:publicTypeIndex <http://example.test//settings/publicTypeIndex.ttl>;" +
            "\n    foaf:name \"Jane Doe\";" +
            "\n    solid:oidcIssuer <https://solidcommunity.net>.";
    }

    public Map<String, String> start() {
        wireMockServer.start();

        setupMocks();

        return Map.of("rdf4j_uri", wireMockServer.baseUrl());
    }

    public void stop() {
        wireMockServer.stop();
    }

}
