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
package com.inrupt.client.uma;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class MockAuthorizationServer {

    public static final String NOT_FOUND_PATH = "/not-found";
    public static final String MALFORMED_PATH = "/malformed";
    public static final String DISCOVERY_ENDPOINT = "/.well-known/uma2-configuration";
    public static final String TOKEN_ENDPOINT = "/token";
    public static final String JWKS_ENDPOINT = "/jwks";

    public static final String CONTENT_TYPE = "Content-Type";
    public static final String APPLICATION_JSON = "application/json";

    private final WireMockServer wireMockServer;

    public MockAuthorizationServer() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options()
                .dynamicPort());
    }

    private void setupMocks() throws UnsupportedEncodingException {
        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(DISCOVERY_ENDPOINT))
                .willReturn(WireMock.aResponse()
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .withBody(getDiscoveryDocument(wireMockServer.baseUrl()))));

        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo(TOKEN_ENDPOINT))
                .withRequestBody(WireMock.containing("ticket=ticket-12345"))
                .willReturn(WireMock.aResponse()
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .withBody("{\"access_token\":\"token-12345\",\"token_type\":\"Bearer\"}")));

        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(JWKS_ENDPOINT))
                .willReturn(WireMock.aResponse()
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .withBodyFile("/jwks.json")));

        // Stubs for normal error responses (per spec)
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo(TOKEN_ENDPOINT))
                .withRequestBody(WireMock.containing("ticket=ticket-invalid-grant"))
                .willReturn(WireMock.aResponse()
                    .withStatus(400)
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .withBody("{\"error\":\"invalid_grant\"}")));

        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo(TOKEN_ENDPOINT))
                .withRequestBody(WireMock.containing("ticket=ticket-request-denied"))
                .willReturn(WireMock.aResponse()
                    .withStatus(403)
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .withBody("{\"error\":\"request_denied\"}")));

        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo(TOKEN_ENDPOINT))
                .withRequestBody(WireMock.containing("ticket=ticket-invalid-scope"))
                .withRequestBody(WireMock.containing("scope=invalid-scope"))
                .willReturn(WireMock.aResponse()
                    .withStatus(400)
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .withBody("{\"error\":\"invalid_scope\"}")));

        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo(TOKEN_ENDPOINT))
                .withRequestBody(WireMock.containing("ticket=ticket-need-info-no-response-ticket"))
                .willReturn(WireMock.aResponse()
                    .withStatus(400)
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .withBody("{\"error\":\"need_info\"}")));

        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo(TOKEN_ENDPOINT))
                .withRequestBody(WireMock.containing("ticket=ticket-need-info-with-ticket"))
                .willReturn(WireMock.aResponse()
                    .withStatus(403)
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .withBody("{\"error\":\"need_info\",\"ticket\":\"ticket-need-info-lvl-02\"}")));

        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo(TOKEN_ENDPOINT))
                .withRequestBody(WireMock.containing("ticket=ticket-need-info-lvl-02"))
                .willReturn(WireMock.aResponse()
                    .withStatus(403)
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .withBody("{\"error\":\"need_info\",\"ticket\":\"ticket-need-info-lvl-3\"}")));

        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo(TOKEN_ENDPOINT))
                .withRequestBody(WireMock.containing("ticket=ticket-need-info-lvl-03"))
                .willReturn(WireMock.aResponse()
                    .withStatus(403)
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .withBody("{\"error\":\"need_info\",\"ticket\":\"ticket-need-info-lvl-4\"}")));

        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo(TOKEN_ENDPOINT))
                .withRequestBody(WireMock.containing("ticket=ticket-need-info-lvl-04"))
                .willReturn(WireMock.aResponse()
                    .withStatus(403)
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .withBody("{\"error\":\"need_info\",\"ticket\":\"ticket-request-denied\"}")));

        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo(TOKEN_ENDPOINT))
                .withRequestBody(WireMock.containing("ticket=ticket-need-info-oidc-requirement"))
                .willReturn(WireMock.aResponse()
                    .withStatus(403)
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .withBody("{" +
                        "\"error\":\"need_info\"," +
                        "\"ticket\":\"ticket-request-id-token\"," +
                        "\"required_claims\":[{" +
                            "\"claim_token_format\":[" +
                                "\"http://openid.net/specs/openid-connect-core-1_0.html#IDToken\"]," +
                            "\"claim_type\":\"webid\"," +
                            "\"friendly_name\":\"webid\"" +
                        "}]}")));

        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo(TOKEN_ENDPOINT))
                .withRequestBody(WireMock.containing("ticket=ticket-request-id-token"))
                .withRequestBody(WireMock.containing("claim_token=oidc-id-token"))
                .withRequestBody(WireMock.containing("claim_token_format=" +
                        URLEncoder.encode(
                                "http://openid.net/specs/openid-connect-core-1_0.html#IDToken",
                            UTF_8.toString())))
                .willReturn(WireMock.aResponse()
                    .withStatus(200)
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .withBody("{\"access_token\":\"token-from-id-token\",\"token_type\":\"Bearer\"}")));


        // Stubs for unexpected error responses (per spec)
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo(TOKEN_ENDPOINT))
                .withRequestBody(WireMock.containing("ticket=ticket-unknown-error"))
                .willReturn(WireMock.aResponse()
                    .withStatus(400)
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .withBody("{\"error\":\"unknown-error\"}")));

        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo(TOKEN_ENDPOINT))
                .withRequestBody(WireMock.containing("ticket=ticket-invalid-response"))
                .willReturn(WireMock.aResponse()
                    .withStatus(400)
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .withBody("{\"foo\":\"bar\"}")));

        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo(TOKEN_ENDPOINT))
                .withRequestBody(WireMock.containing("ticket=ticket-malformed-response"))
                .willReturn(WireMock.aResponse()
                    .withStatus(400)
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .withBody("It's all wrong.")));

        // Stubs for returning 404s
        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(NOT_FOUND_PATH + DISCOVERY_ENDPOINT))
                .willReturn(WireMock.aResponse()
                    .withStatus(404)));

        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(NOT_FOUND_PATH + TOKEN_ENDPOINT))
                .willReturn(WireMock.aResponse()
                    .withStatus(404)));

        // Stubs for returning malformed JSON
        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(MALFORMED_PATH + DISCOVERY_ENDPOINT))
                .willReturn(WireMock.aResponse()
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .withBody("NOT JSON")));

        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(MALFORMED_PATH + TOKEN_ENDPOINT))
                .willReturn(WireMock.aResponse()
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .withBody("NOT JSON")));
    }

    public Map<String, String> start() throws UnsupportedEncodingException {
        wireMockServer.start();

        setupMocks();

        return new HashMap<String, String>() {
            {
                put("as_uri", wireMockServer.baseUrl());
            }};
    }

    public void stop() {
        wireMockServer.stop();
    }

    public String getDiscoveryDocument(final String baseUrl) {
        return "{" +
            "\"dpop_signing_alg_values_supported\": [\"ES256\",\"RS256\"]," +
            "\"grant_types_supported\": [\"urn:ietf:params:oauth:grant-type:uma-ticket\"]," +
            "\"issuer\": \"" + baseUrl + "\"," +
            "\"jwks_uri\": \"" + baseUrl + JWKS_ENDPOINT + "\"," +
            "\"token_endpoint\": \"" + baseUrl + TOKEN_ENDPOINT + "\"," +
            "\"uma_profiles_supported\": [" +
                "\"https://www.w3.org/TR/vc-data-model/#json-ld\"," +
                "\"http://openid.net/specs/openid-connect-core-1_0.html#IDToken\"]" +
            "}";
    }
}
