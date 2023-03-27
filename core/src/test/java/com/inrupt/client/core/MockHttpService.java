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
package com.inrupt.client.core;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.inrupt.client.Request;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import org.apache.commons.io.IOUtils;

class MockHttpService {

    private static final String USER_AGENT = "InruptJavaClient/" + Request.class
        .getPackage().getImplementationVersion();
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
    private static final String TEXT_TURTLE = "text/turtle";
    private static final String TEXT_PLAIN = "text/plain";

    private final WireMockServer wireMockServer;

    public MockHttpService() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
    }

    public String start() {
        wireMockServer.start();

        setupMocks();

        return wireMockServer.baseUrl();
    }

    public void stop() {
        wireMockServer.stop();
    }

    private void setupMocks() {
        wireMockServer.stubFor(get(urlEqualTo("/file"))
                    .withHeader("User-Agent", equalTo(USER_AGENT))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, TEXT_TURTLE)
                        .withBodyFile("clarissa-sample.txt")));

        wireMockServer.stubFor(get(urlEqualTo("/example"))
                    .withHeader("User-Agent", equalTo(USER_AGENT))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, TEXT_TURTLE)
                        .withBodyFile("profileExample.ttl")));

        wireMockServer.stubFor(put(urlEqualTo("/putRDF"))
                    .atPriority(1)
                    .withRequestBody(matching(
                            ".*<http://example.test/s>\\s+" +
                            "<http://example.test/p>\\s+\"object\"\\s+\\..*"))
                    .withHeader("User-Agent", equalTo(USER_AGENT))
                    .withHeader(CONTENT_TYPE, containing(TEXT_TURTLE))
                    .withHeader("Authorization", containing("DPoP "))
                    .withHeader("DPoP", containing("eyJhbGciOiJFUzI1NiIsInR5cCI6ImRwb3Arand0Iiw" +
                            "iandrIjp7Imt0eSI6IkVDIiwieCI6IlNvS1BhS1ZfNXpPaDFMLVhMMmVLbWhXcXJEe" +
                            "U5uY01ZMkEtLVl6MFo2ZDAiLCJ5IjoicEJGRWxIYnpDY0V5ODdGUnBzajJ3VHZnT1l" +
                            "1S1BGS1o0VTFJbHpRZ1dQUSIsImNydiI6IlAtMjU2In19"))
                    .willReturn(aResponse()
                        .withStatus(201)));

        wireMockServer.stubFor(put(urlEqualTo("/putRDF"))
                    .atPriority(2)
                    .withHeader("User-Agent", equalTo(USER_AGENT))
                    .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("WWW-Authenticate", "Bearer, DPoP algs=\"ES256 PS256\"")));


        wireMockServer.stubFor(post(urlEqualTo("/postOneTriple"))
                    .withHeader("User-Agent", equalTo(USER_AGENT))
                    .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("WWW-Authenticate", "Unknown, Bearer, " +
                            "DPoP algs=\"ES256\", " +
                            "UMA ticket=\"ticket-12345\", as_uri=\"" + wireMockServer.baseUrl() + "\"")));

        wireMockServer.stubFor(post(urlEqualTo("/postBearerToken"))
                    .atPriority(1)
                    .withHeader("User-Agent", equalTo(USER_AGENT))
                    .withRequestBody(matching(
                            ".*<http://example.test/s>\\s+" +
                            "<http://example.test/p>\\s+\"object\"\\s+\\..*"))
                    .withHeader(CONTENT_TYPE, containing(TEXT_TURTLE))
                    .withHeader("Authorization", containing("DPoP "))
                    .withHeader("DPoP", containing("eyJhbGciOiJFUzI1NiIsInR5cCI6ImRwb3Arand0Iiw" +
                            "iandrIjp7Imt0eSI6IkVDIiwieCI6IlNvS1BhS1ZfNXpPaDFMLVhMMmVLbWhXcXJEe" +
                            "U5uY01ZMkEtLVl6MFo2ZDAiLCJ5IjoicEJGRWxIYnpDY0V5ODdGUnBzajJ3VHZnT1l" +
                            "1S1BGS1o0VTFJbHpRZ1dQUSIsImNydiI6IlAtMjU2In19"))
                    .willReturn(aResponse()
                        .withStatus(201)));

        wireMockServer.stubFor(post(urlEqualTo("/postBearerToken"))
                    .atPriority(2)
                    .withHeader("User-Agent", equalTo(USER_AGENT))
                    .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("WWW-Authenticate", "Bearer,DPoP algs=\"ES256\"")));

        wireMockServer.stubFor(post(urlEqualTo("/postStringContainer/"))
                    .atPriority(1)
                    .withHeader("User-Agent", equalTo(USER_AGENT))
                    .withRequestBody(matching("Test String 1"))
                    .withHeader(CONTENT_TYPE, containing(TEXT_PLAIN))
                    .withHeader("Authorization", containing("Bearer token-67890"))
                    .withHeader("DPoP", absent())
                    .willReturn(aResponse()
                        .withStatus(201)));

        wireMockServer.stubFor(post(urlEqualTo("/postStringContainer/"))
                    .atPriority(2)
                    .withHeader("User-Agent", equalTo(USER_AGENT))
                    .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("WWW-Authenticate", "Bearer, DPoP algs=\"ES256\", " +
                            "UMA ticket=\"ticket-67890\", as_uri=\"" + wireMockServer.baseUrl() + "\"")));

        wireMockServer.stubFor(get(urlEqualTo("/solid.png"))
                    .withHeader("User-Agent", equalTo(USER_AGENT))
                    .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, "image/png")
                        .withBodyFile("SolidOS.png")
                        .withStatus(200)));

        wireMockServer.stubFor(get(urlEqualTo("/.well-known/uma2-configuration"))
                    .withHeader("User-Agent", equalTo(USER_AGENT))
                    .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(getResource("/uma2-configuration.json", wireMockServer.baseUrl()))));

        wireMockServer.stubFor(get(urlEqualTo("/uma/jwks"))
                    .withHeader("User-Agent", equalTo(USER_AGENT))
                    .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBodyFile("uma-jwks.json")));

        wireMockServer.stubFor(post(urlEqualTo("/uma/token"))
                    .atPriority(1)
                    .withHeader("User-Agent", equalTo(USER_AGENT))
                    .withRequestBody(containing("claim_token_format=" +
                            "http%3A%2F%2Fopenid.net%2Fspecs%2Fopenid-connect-core-1_0.html%23IDToken"))
                    .withRequestBody(containing("ticket=ticket-67890"))
                    .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody("{\"access_token\":\"token-67890\",\"token_type\":\"Bearer\"," +
                            "\"scope\":\"Read Write\"}")));

        wireMockServer.stubFor(post(urlEqualTo("/uma/token"))
                    .atPriority(2)
                    .withHeader("User-Agent", equalTo(USER_AGENT))
                    .withRequestBody(containing("ticket=ticket-67890"))
                    .willReturn(aResponse()
                        .withStatus(403)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody("{}")));

        wireMockServer.stubFor(post(urlEqualTo("/uma/token"))
                    .atPriority(2)
                    .withHeader("User-Agent", equalTo(USER_AGENT))
                    .withRequestBody(containing("ticket=ticket-12345"))
                    .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody("{\"access_token\":\"token-12345\",\"token_type\":\"Bearer\"," +
                            "\"scope\":\"Read Write\"}")));

        wireMockServer.stubFor(get(urlEqualTo("/protected/resource"))
                    .withHeader("Accept", containing(TEXT_TURTLE))
                    .withHeader("User-Agent", equalTo(USER_AGENT))
                    .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("WWW-Authenticate",
                            "UMA ticket=\"ticket-12345\", as_uri=\"" + wireMockServer.baseUrl() + "\"")));

        wireMockServer.stubFor(get(urlEqualTo("/protected/resource"))
                    .withHeader("User-Agent", equalTo(USER_AGENT))
                    .withHeader("Authorization", equalTo("Bearer token-12345"))
                    .withHeader("Accept", containing(TEXT_TURTLE))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, TEXT_TURTLE)
                        .withBodyFile("profileExample.ttl")));
    }

    private static String getResource(final String path) {
        try (final InputStream res = MockHttpService.class.getResourceAsStream(path)) {
            return new String(IOUtils.toByteArray(res), UTF_8);
        } catch (final IOException ex) {
            throw new UncheckedIOException("Could not read class resource", ex);
        }
    }

    private static String getResource(final String path, final String baseUrl) {
        return getResource(path).replace("{{baseUrl}}", baseUrl);
    }
}

