package com.inrupt.client.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import org.apache.commons.io.IOUtils;

public class MockUMAAuthorizationServer {

    private final WireMockServer wireMockServer;

    public MockUMAAuthorizationServer() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
    }

    private void setupMocks() {
        wireMockServer.stubFor(get(urlEqualTo(Utils.UMA_DISCOVERY_ENDPOINT))
                    .willReturn(aResponse()
                        .withStatus(Utils.SUCCESS)
                        .withHeader(Utils.CONTENT_TYPE, Utils.APPLICATION_JSON)
                        .withBody(getResource("/uma2-configuration.json", wireMockServer.baseUrl(), Utils.ISS))));
        wireMockServer.stubFor(post(urlPathMatching("/"+Utils.UMA_TOKEN_ENDPOINT))
                    .withRequestBody(WireMock.containing("claim_token"))
                    .willReturn(aResponse()
                        .withStatus(Utils.SUCCESS)
                        .withHeader(Utils.CONTENT_TYPE, Utils.APPLICATION_JSON)
                        .withBody("{\"access_token\":\"token-67890\",\"token_type\":\"Bearer\"}")));

    //checking for claim_token if the artifice we use to distinguish between auth and unauth requests
    //if (request.getUrl().contains(Utils.TOKEN_ENDPOINT) && request.getBodyAsString().contains("claim_token")) {
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

    private String getResource(final String path, final String baseUrl, String issuer) {
        return getResource(path)
                .replace("{{baseUrl}}", baseUrl)
                .replace("{{issuerUrl}}", issuer)
                .replace("{{tokenEndpoint}}", Utils.UMA_TOKEN_ENDPOINT)
                .replace("{{jwksEndpoint}}", Utils.JWKS_ENDPOINT);
    }

    private String getResource(final String path) {
        try (final InputStream res = MockUMAAuthorizationServer.class.getResourceAsStream(path)) {
            return new String(IOUtils.toByteArray(res), UTF_8);
        } catch (final IOException ex) {
            throw new UncheckedIOException("Could not read class resource", ex);
        }
    }
}
