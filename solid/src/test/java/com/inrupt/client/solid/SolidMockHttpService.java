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
package com.inrupt.client.solid;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.inrupt.client.Headers.Link;
import com.inrupt.client.vocabulary.LDP;
import com.inrupt.client.vocabulary.PIM;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

public class SolidMockHttpService {

    private final WireMockServer wireMockServer;

    public SolidMockHttpService() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
    }

    public int getPort() {
        return wireMockServer.port();
    }

    private void setupMocks() {
        wireMockServer.stubFor(get(urlEqualTo("/solid/"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/turtle")
                .withHeader("Link", Link.of(LDP.BasicContainer, "type").toString())
                .withHeader("Link", Link.of(URI.create("http://storage.example/"),
                        PIM.storage).toString())
                .withHeader("Link", Link.of(URI.create("https://history.test/"), "timegate").toString())
                .withHeader("WAC-Allow", "user=\"read write\",public=\"read\"")
                .withHeader("Allow", "POST, PUT, PATCH")
                .withHeader("Accept-Post", "application/ld+json, text/turtle")
                .withHeader("Accept-Put", "application/ld+json, text/turtle")
                .withHeader("Accept-Patch", "application/sparql-update, text/n3")
                .withBodyFile("solidResourceExample.ttl")
            )
        );

        wireMockServer.stubFor(get(urlEqualTo("/recipe"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/turtle")
                .withHeader("Link", Link.of(LDP.RDFSource, "type").toString())
                .withHeader("Link", Link.of(URI.create("http://storage.example/"),
                        PIM.storage).toString())
                .withHeader("Link", Link.of(URI.create("https://history.test/"), "timegate").toString())
                .withHeader("WAC-Allow", "user=\"read write\",public=\"read\"")
                .withHeader("Allow", "POST, PUT, PATCH")
                .withHeader("Accept-Post", "application/ld+json, text/turtle")
                .withHeader("Accept-Put", "application/ld+json, text/turtle")
                .withHeader("Accept-Patch", "application/sparql-update, text/n3")
                .withBodyFile("recipe.ttl")
            )
        );

        wireMockServer.stubFor(get(urlEqualTo("/playlist"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/turtle")
                .withHeader("Link", Link.of(LDP.RDFSource, "type").toString())
                .withHeader("Link", Link.of(URI.create("http://storage.example/"),
                        PIM.storage).toString())
                .withHeader("Link", Link.of(URI.create("https://history.test/"), "timegate").toString())
                .withHeader("WAC-Allow", "user=\"read write\",public=\"read\"")
                .withHeader("Allow", "POST, PUT, PATCH")
                .withHeader("Accept-Post", "application/ld+json, text/turtle")
                .withHeader("Accept-Put", "application/ld+json, text/turtle")
                .withHeader("Accept-Patch", "application/sparql-update, text/n3")
                .withBodyFile("playlist.ttl")
            )
        );

        wireMockServer.stubFor(put(urlEqualTo("/playlist"))
            .withHeader("Content-Type", containing("text/turtle"))
            .withRequestBody(containing(
                    "<https://library.test/12345/song1.mp3>"))
            .willReturn(aResponse()
                .withStatus(204)));

        wireMockServer.stubFor(delete(urlEqualTo("/playlist"))
                .willReturn(aResponse()
                    .withStatus(204)));

        wireMockServer.stubFor(get(urlEqualTo("/nonRDF"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "text/turtle")
                    .withBody("This isn't valid turtle.")
            )
        );
    }

    public Map<String, String> start() {
        wireMockServer.start();

        setupMocks();

        return Collections.singletonMap("solid_resource_uri", wireMockServer.baseUrl());
    }

    public void stop() {
        wireMockServer.stop();
    }
}
