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
package com.inrupt.client.vc;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

class VerifiableCredentialMockService {

    private final WireMockServer vcMockService;

    public VerifiableCredentialMockService() {
        vcMockService = new WireMockServer(WireMockConfiguration.options()
            .dynamicPort());
    }

    public int getPort() {
        return vcMockService.port();
    }

    private void setupMocks() {
        vcMockService.stubFor(get(urlEqualTo("/vc"))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Accept", "application/json")
                        .withBody(getVerifiableCredentialJson())));

        vcMockService.stubFor(get(urlEqualTo("/vp"))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Accept", "application/json")
                        .withBody(getVerifiablePresentationJson())));

        vcMockService.stubFor(post(urlEqualTo("/postVc"))
                    .withHeader("Content-Type", containing("application/json"))
                    .willReturn(aResponse()
                        .withStatus(201)));

        vcMockService.stubFor(post(urlEqualTo("/postVp"))
                    .withHeader("Content-Type", containing("application/json"))
                    .willReturn(aResponse()
                        .withStatus(201)));

        vcMockService.stubFor(post(urlEqualTo("/credentials/issue"))
                    .withHeader("Content-Type", containing("application/json"))
                    .willReturn(aResponse()
                        .withBody(getVerifiableCredentialJson())
                        .withStatus(201)));

        vcMockService.stubFor(post(urlEqualTo( "/credentials/status"))
                    .withHeader("Content-Type", containing("application/json"))
                    .willReturn(aResponse()
                        .withBody("{ \"id\": \"https://example.edu/status/24/\"," +
                        "\"type\": \"CredentialStatusList2017\" }")
                        .withStatus(201)));

        vcMockService.stubFor(post(urlEqualTo( "/credentials/verify"))
                    .withHeader("Content-Type", containing("application/json"))
                    .willReturn(aResponse()
                        .withBody(getVerificationResponseJson())
                        .withStatus(200)));
        vcMockService.stubFor(post(urlEqualTo( "/presentations/verify"))
                    .withHeader("Content-Type", containing("application/json"))
                    .willReturn(aResponse()
                        .withBody(getVerificationResponseJson())
                        .withStatus(200)));

        vcMockService.stubFor(get(urlPathMatching( "/credentials"))
                    .withQueryParam("type", matching("([A-Za-z0-9.,-:]*)"))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(getVClistJson())
                        .withHeader("Accept", "application/json")));
        vcMockService.stubFor(get(urlPathMatching( "/credentials/(.*)"))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(getVerifiableCredentialJson())
                        .withHeader("Accept", "application/json")));
        vcMockService.stubFor(delete(urlPathMatching( "/credentials/(.*)"))
                    .willReturn(aResponse()
                        .withStatus(200)));
        vcMockService.stubFor(post(urlEqualTo( "/credentials/derive"))
                    .withHeader("Content-Type", containing("application/json"))
                    .willReturn(aResponse()
                        .withBody(getVerifiableCredentialJson())
                        .withStatus(200)));

        vcMockService.stubFor(get(urlPathMatching( "/presentations"))
                    .withQueryParam("type", matching("([A-Za-z0-9.,-:]*)"))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(getVPlistJson())
                        .withHeader("Accept", "application/json")));
        vcMockService.stubFor(get(urlPathMatching( "/presentations/(.*)"))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(getVerifiablePresentationJson())
                        .withHeader("Accept", "application/json")));
        vcMockService.stubFor(delete(urlPathMatching( "/presentations/(.*)"))
                    .willReturn(aResponse()
                        .withStatus(200)));
        vcMockService.stubFor(post(urlEqualTo( "/presentations/prove"))
                    .withHeader("Content-Type", containing("application/json"))
                    .willReturn(aResponse()
                        .withBody(getVerifiablePresentationJson())
                        .withStatus(200)));
        vcMockService.stubFor(post(urlPathMatching( "/exchanges/(.*)"))
                    .withHeader("Content-Type", containing("application/json"))
                    .willReturn(aResponse()
                        .withBody(getVerifiablePresentationRequestJson())
                        .withStatus(200)));
    }

    private static String getVerifiableCredentialJson() {
        try (final var res = VerifiableCredentialMockService.class.getResourceAsStream("/verifiableCredential.json")) {
            return new String(res.readAllBytes(), UTF_8);
        } catch (final IOException ex) {
            throw new UncheckedIOException("Could not read class resource", ex);
        }
    }

    private static String getVerifiablePresentationJson() {
        try (final var res = VerifiableCredentialMockService.class.getResourceAsStream(
                "/verifiablePresentation.json")) {
            return new String(res.readAllBytes(), UTF_8);
        } catch (final IOException ex) {
            throw new UncheckedIOException("Could not read class resource", ex);
        }
    }

    private String getVerificationResponseJson() {
        try (final var res = VerifiableCredentialMockService.class.getResourceAsStream(
                "/verificationResponse.json")) {
            return new String(res.readAllBytes(), UTF_8);
        } catch (final IOException ex) {
            throw new UncheckedIOException("Could not read class resource", ex);
        }
    }

    private String getVClistJson() {
        try (final var res = VerifiableCredentialMockService.class.getResourceAsStream(
            "/verifiableCredentialList.json"
            )) {
            return new String(res.readAllBytes(), UTF_8);
        } catch (final IOException ex) {
            throw new UncheckedIOException("Could not read class resource", ex);
        }
    }

    private String getVPlistJson() {
        try (final var res = VerifiableCredentialMockService.class.getResourceAsStream(
            "/verifiablePresentationList.json"
            )) {
            return new String(res.readAllBytes(), UTF_8);
        } catch (final IOException ex) {
            throw new UncheckedIOException("Could not read class resource", ex);
        }
    }

    private String getVerifiablePresentationRequestJson() {
        try (final var res = VerifiableCredentialMockService.class.getResourceAsStream(
            "/verifiablePresentationRequest.json"
            )) {
            return new String(res.readAllBytes(), UTF_8);
        } catch (final IOException ex) {
            throw new UncheckedIOException("Could not read class resource", ex);
        }
    }

    public Map<String, String> start() {
        vcMockService.start();

        setupMocks();

        return Map.of("vc_uri", vcMockService.baseUrl());
    }

    public void stop() {
        vcMockService.stop();
    }
}
