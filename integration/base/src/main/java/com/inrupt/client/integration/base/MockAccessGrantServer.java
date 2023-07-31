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
package com.inrupt.client.integration.base;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.inrupt.client.Request;
import com.inrupt.client.accessgrant.AccessCredentialVerification;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.io.IOUtils;

class MockAccessGrantServer {

    private final String USER_AGENT_HEADER = "User-Agent";
    private static final String USER_AGENT = "InruptJavaClient/" + Request.class
            .getPackage().getImplementationVersion();
    private static final String DERIVE = "/derive";
    private static final String ISSUE = "/issue";

    private static final String VERIFY = "/verify";

    private static final String HEADER_AUTHORIZATION = "Authorization";

    private static final String SCHEME_BEARER = "Bearer";

    // An identifier to enable statefulness in Wiremock, useful to manage revocation and verification.
    private static final String SCENARIO_ACCESS_GRANT = "AccessGrant";
    private static final String SCENARIO_STATE_ISSUED = "Issued";
    private static final String SCENARIO_STATE_REVOKED = "Revoked";

    private final WireMockServer wireMockServer;
    private final String ownerWebId;
    private final String requesterWebId;
    private final String sharedResource;

    private final String authorisationServerUrl;

    public MockAccessGrantServer(
            final URI ownerWebId,
            final URI requesterWebId,
            final URI sharedResource,
            final String authorisationServerUrl
    ) {
        this.ownerWebId = ownerWebId.toString();
        this.requesterWebId = requesterWebId.toString();
        this.sharedResource = sharedResource.toString();
        this.authorisationServerUrl = authorisationServerUrl;
        wireMockServer = new WireMockServer(WireMockConfiguration.options()
                .httpDisabled(true)
                .dynamicHttpsPort());
    }

    private void setupMocks() {
        wireMockServer.stubFor(get(urlEqualTo(Utils.VC_DISCOVERY_ENDPOINT))
                .withHeader(USER_AGENT_HEADER, equalTo(USER_AGENT))
                .willReturn(aResponse()
                        .withStatus(Utils.SUCCESS)
                        .withHeader(Utils.CONTENT_TYPE, Utils.APPLICATION_JSON)
                        .withBody(getResource("/vc-configuration.json", wireMockServer.baseUrl()))));

        wireMockServer.stubFor(get(urlPathEqualTo("/vc-grant"))
                .withHeader(USER_AGENT_HEADER, equalTo(USER_AGENT))
                .willReturn(aResponse()
                        .withStatus(Utils.SUCCESS)
                        .withHeader(Utils.CONTENT_TYPE, Utils.APPLICATION_JSON)
                        .withBody(getResource("/vc-grant.json", wireMockServer.baseUrl(),
                                this.requesterWebId, this.ownerWebId, this.sharedResource))));

        wireMockServer.stubFor(get(urlPathEqualTo("/vc-request"))
                .withHeader(USER_AGENT_HEADER, equalTo(USER_AGENT))
                .willReturn(aResponse()
                        .withStatus(Utils.SUCCESS)
                        .withHeader(Utils.CONTENT_TYPE, Utils.APPLICATION_JSON)
                        .withBody(getResource("/vc-request.json", wireMockServer.baseUrl(),
                                this.requesterWebId, this.ownerWebId, this.sharedResource))));

        wireMockServer.stubFor(delete(urlPathMatching("/vc-grant"))
                .withHeader(USER_AGENT_HEADER, equalTo(USER_AGENT))
                .willReturn(aResponse()
                        .withStatus(204)));

        wireMockServer.stubFor(post(urlEqualTo(ISSUE))
                .atPriority(2)
                .withHeader(USER_AGENT_HEADER, equalTo(USER_AGENT))
                .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("WWW-Authenticate", "Bearer, DPoP algs=\"ES256\", " +
                                "UMA ticket=\"ticket-67890\", as_uri=\"" + authorisationServerUrl + "\"")));

        wireMockServer.stubFor(post(urlEqualTo(ISSUE))
                .atPriority(3)
                .withHeader(USER_AGENT_HEADER, equalTo(USER_AGENT))
                .withHeader(HEADER_AUTHORIZATION, containing(SCHEME_BEARER))
                .withRequestBody(containing("hasConsent"))
                .withRequestBody(containing(this.sharedResource))
                .willReturn(aResponse()
                        .withStatus(Utils.SUCCESS)
                        .withHeader(Utils.CONTENT_TYPE, Utils.APPLICATION_JSON)
                        .withBody(getResource("/vc-request.json", wireMockServer.baseUrl(),
                                this.requesterWebId, this.ownerWebId, this.sharedResource))));

        wireMockServer.stubFor(post(urlEqualTo(ISSUE))
                .inScenario(SCENARIO_ACCESS_GRANT)
                .willSetStateTo(SCENARIO_STATE_ISSUED)
                .withHeader(USER_AGENT_HEADER, equalTo(USER_AGENT))
                .atPriority(3)
                .withHeader(HEADER_AUTHORIZATION, containing(SCHEME_BEARER))
                .withRequestBody(containing("providedConsent"))
                .withRequestBody(containing(this.sharedResource))
                .willReturn(aResponse()
                        .withStatus(Utils.SUCCESS)
                        .withHeader(Utils.CONTENT_TYPE, Utils.APPLICATION_JSON)
                        .withBody(getResource("/vc-grant.json", wireMockServer.baseUrl(),
                                this.requesterWebId, this.ownerWebId, this.sharedResource))));

        wireMockServer.stubFor(post(urlEqualTo(ISSUE))
                .atPriority(1)
                .withHeader(USER_AGENT_HEADER, equalTo(USER_AGENT))
                .withHeader(HEADER_AUTHORIZATION, containing(SCHEME_BEARER))
                .withRequestBody(containing("hasConsent"))
                .willReturn(aResponse()
                        .withStatus(Utils.SUCCESS)
                        .withHeader(Utils.CONTENT_TYPE, Utils.APPLICATION_JSON)
                        .withBody(getResource("/vc-request.json", wireMockServer.baseUrl(),
                                this.requesterWebId, this.ownerWebId, this.sharedResource))));

        wireMockServer.stubFor(post(urlEqualTo(ISSUE))
                .inScenario(SCENARIO_ACCESS_GRANT)
                .willSetStateTo(SCENARIO_STATE_ISSUED)
                .atPriority(1)
                .withHeader(USER_AGENT_HEADER, equalTo(USER_AGENT))
                .withHeader(HEADER_AUTHORIZATION, containing(SCHEME_BEARER))
                .withRequestBody(containing("providedConsent"))
                .willReturn(aResponse()
                        .withStatus(Utils.SUCCESS)
                        .withHeader(Utils.CONTENT_TYPE, Utils.APPLICATION_JSON)
                        .withBody(getResource("/vc-grant.json", wireMockServer.baseUrl(),
                                this.requesterWebId, this.ownerWebId, this.sharedResource))));

        wireMockServer.stubFor(post(urlEqualTo(ISSUE))
                .atPriority(1)
                .withHeader(USER_AGENT_HEADER, equalTo(USER_AGENT))
                .withHeader(HEADER_AUTHORIZATION, containing(SCHEME_BEARER))
                .withRequestBody(containing("hasConsent"))
                .willReturn(aResponse()
                        .withStatus(Utils.SUCCESS)
                        .withHeader(Utils.CONTENT_TYPE, Utils.APPLICATION_JSON)
                        .withBody(getResource("/vc-request.json", wireMockServer.baseUrl(),
                                this.requesterWebId, this.ownerWebId, this.sharedResource))));

        // Require UMA authentication for the /verify endpoint
        wireMockServer.stubFor(post(urlEqualTo(VERIFY))
                .atPriority(2)
                .withHeader(USER_AGENT_HEADER, equalTo(USER_AGENT))
                .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("WWW-Authenticate", "Bearer, DPoP algs=\"ES256\", " +
                                "UMA ticket=\"ticket-67890\", as_uri=\"" + authorisationServerUrl + "\"")));

        wireMockServer.stubFor(post(urlEqualTo(VERIFY))
                .inScenario(SCENARIO_ACCESS_GRANT)
                .whenScenarioStateIs(SCENARIO_STATE_ISSUED)
                .atPriority(1)
                .withHeader(USER_AGENT_HEADER, equalTo(USER_AGENT))
                .withHeader(HEADER_AUTHORIZATION, containing(SCHEME_BEARER))
                .willReturn(jsonResponse(
                        new AccessCredentialVerification(
                                Arrays.asList("issuanceDate", "proof", "expirationDate", "credentialStatus"),
                                Collections.emptyList(),
                                Collections.emptyList()),
                        200)
                ));

        wireMockServer.stubFor(post(urlEqualTo(VERIFY))
                .inScenario(SCENARIO_ACCESS_GRANT)
                .whenScenarioStateIs(SCENARIO_STATE_REVOKED)
                .atPriority(1)
                .withHeader(USER_AGENT_HEADER, equalTo(USER_AGENT))
                .withHeader(HEADER_AUTHORIZATION, containing(SCHEME_BEARER))
                .withRequestBody(containing("providedConsent"))
                .willReturn(jsonResponse(
                        new AccessCredentialVerification(
                                Arrays.asList("issuanceDate", "proof", "expirationDate", "credentialStatus"),
                                Collections.emptyList(),
                                Arrays.asList("credentialStatus validation has failed: credential has been revoked")),
                        200)
                )
        );

        wireMockServer.stubFor(post(urlEqualTo("/status"))
                .inScenario(SCENARIO_ACCESS_GRANT)
                .willSetStateTo(SCENARIO_STATE_REVOKED)
                .withHeader(USER_AGENT_HEADER, equalTo(USER_AGENT))
                .withRequestBody(containing("\"RevocationList2020Status\""))
                .willReturn(aResponse()
                        .withStatus(Utils.NO_CONTENT)));

        wireMockServer.stubFor(post(urlEqualTo(DERIVE))
                .atPriority(1)
                .withHeader(USER_AGENT_HEADER, equalTo(USER_AGENT))
                .withRequestBody(containing("\"Append\""))
                .withRequestBody(containing("\"https://purpose.example/212efdf4-e1a4-4dcd-9d3b-d6eb92e0205f\""))
                .withRequestBody(containing("\"" + this.sharedResource + "\""))
                .withRequestBody(containing("SolidAccessGrant"))
                .willReturn(aResponse()
                        .withStatus(Utils.SUCCESS)
                        .withHeader(Utils.CONTENT_TYPE, Utils.APPLICATION_JSON)
                        .withBody(getResource("/query_response.json", wireMockServer.baseUrl(),
                                this.requesterWebId, this.ownerWebId, this.sharedResource))));

        wireMockServer.stubFor(post(urlEqualTo(DERIVE))
                .atPriority(1)
                .withHeader(USER_AGENT_HEADER, equalTo(USER_AGENT))
                .withRequestBody(containing("\"Read\""))
                .withRequestBody(containing("\"" + this.sharedResource + "\""))
                .withRequestBody(containing("SolidAccessGrant"))
                .willReturn(aResponse()
                        .withStatus(Utils.SUCCESS)
                        .withHeader(Utils.CONTENT_TYPE, Utils.APPLICATION_JSON)
                        .withBody(getResource("/query_response.json", wireMockServer.baseUrl(),
                                this.requesterWebId, this.ownerWebId, this.sharedResource))));

        wireMockServer.stubFor(post(urlEqualTo(DERIVE))
                .atPriority(2)
                .withHeader(USER_AGENT_HEADER, equalTo(USER_AGENT))
                .willReturn(aResponse()
                        .withStatus(Utils.SUCCESS)
                        .withHeader(Utils.CONTENT_TYPE, Utils.APPLICATION_JSON)
                        .withBody(getResource("/query_response_empty.json"))));

    }

    private static String getResource(final String path) {
        try (final InputStream res = MockAccessGrantServer.class.getResourceAsStream(path)) {
            return new String(IOUtils.toByteArray(res), UTF_8);
        } catch (final IOException ex) {
            throw new UncheckedIOException("Could not read class resource", ex);
        }
    }

    private static String getResource(final String path, final String baseUrl) {
        return getResource(path).replace("{{baseUrl}}", baseUrl);
    }

    private static String getResource(final String path, final String baseUrl, final String requesterWebId,
                                      final String ownerWebId, final String sharedFile) {
        return getResource(path).replace("{{baseUrl}}", baseUrl)
                .replace("{{requesterWebId}}", requesterWebId)
                .replace("{{ownerWebId}}", ownerWebId)
                .replace("{{sharedFile}}", sharedFile);
    }

    public void start() {
        wireMockServer.start();
        setupMocks();
    }

    public String getMockServerUrl() {
        return wireMockServer.baseUrl();
    }

    public void stop() {
        wireMockServer.stop();
    }

}

