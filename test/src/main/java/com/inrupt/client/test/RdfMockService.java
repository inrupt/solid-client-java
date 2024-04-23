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
package com.inrupt.client.test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.inrupt.client.ProblemDetails;

import java.util.Collections;
import java.util.Map;

/**
 * A {@link WireMockServer} based HTTP service used for testing RDF services.
 */
public class RdfMockService {

    private static final String CONTENT_TYPE = "Content-Type";

    private final WireMockServer wireMockServer;

    public RdfMockService() {
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
                        .withHeader(CONTENT_TYPE, "text/turtle")
                        .withBody("<http://example.test/s> <http://example.test/p> <http://example.test/o> .")));

        wireMockServer.stubFor(post(urlEqualTo("/postOneTriple"))
                    .withRequestBody(matching(
                            ".*<http://example.test/subject>\\s+" +
                            "<http://example.test/predicate>\\s+\"object\"\\s+\\..*"))
                    .withHeader(CONTENT_TYPE, containing("text/turtle"))
                    .willReturn(aResponse()
                        .withStatus(204)));

        wireMockServer.stubFor(get(urlEqualTo("/example"))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, "text/turtle")
                        .withBody(getExampleTTL())));

        wireMockServer.stubFor(patch(urlEqualTo("/sparqlUpdate"))
                    .withHeader(CONTENT_TYPE, containing("application/sparql-update"))
                    .withRequestBody(matching(
                            "INSERT DATA\\s+\\{\\s*<http://example.test/s1>\\s+" +
                            "<http://example.test/p1>\\s+<http://example.test/o1>\\s*\\.\\s*\\}\\s*"))
                    .willReturn(aResponse()
                        .withStatus(204)));

        wireMockServer.stubFor(get(urlEqualTo("/error"))
                .willReturn(aResponse()
                        .withStatus(429)
                        .withHeader(CONTENT_TYPE, ProblemDetails.MIME_TYPE)
                        .withBody("{" +
                                "\"title\":\"Too Many Requests\"," +
                                "\"status\":429," +
                                "\"details\":\"Some details\"," +
                                "\"instance\":\"https://example.org/instance\"," +
                                "\"type\":\"https://example.org/type\"}")));
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

        return Collections.singletonMap("rdf_uri", wireMockServer.baseUrl());
    }

    public void stop() {
        wireMockServer.stop();
    }

}
