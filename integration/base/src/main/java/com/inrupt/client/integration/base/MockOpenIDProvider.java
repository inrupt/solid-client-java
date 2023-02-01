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
package com.inrupt.client.integration.base;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.jose4j.jwx.HeaderParameterNames.TYPE;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;
import org.jose4j.lang.UncheckedJoseException;

class MockOpenIDProvider {
    private final WireMockServer wireMockServer;
    private String username;

    public MockOpenIDProvider(final String username) {
        this.username = username;
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
    }

    private void setupMocks() {
        wireMockServer.stubFor(get(urlEqualTo(Utils.OPENID_DISCOVERY_ENDPOINT))
                    .willReturn(aResponse()
                        .withStatus(Utils.SUCCESS)
                        .withHeader(Utils.CONTENT_TYPE, Utils.APPLICATION_JSON)
                        .withBody(getResource("/metadata.json", wireMockServer.baseUrl()))));

        wireMockServer.stubFor(post(urlPathMatching("/" + Utils.OAUTH_TOKEN_ENDPOINT))
                    .withHeader(Utils.CONTENT_TYPE, containing(Utils.APPLICATION_FORM))
                    .withRequestBody(containing("client_credentials"))
                    .willReturn(aResponse()
                        .withStatus(Utils.SUCCESS)
                        .withHeader(Utils.CONTENT_TYPE, Utils.APPLICATION_JSON)
                        .withBody(getTokenResponseJSON())));
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

    private String getTokenResponseJSON() {
        return "{" +
            "\"access_token\": \"123456\"," +
            "\"id_token\": \"" + setupIdToken() + "\"," +
            "\"token_type\": \"Bearer\"," +
            "\"expires_in\": 300" +
            "}";
    }

    private String setupIdToken() {
        final Map<String, Object> claims = new HashMap<>();
        if (State.WEBID != null) {
            claims.put("webid", State.WEBID.toString());
        } else {
            claims.put("webid", "");
        }
        claims.put("sub", this.username);
        claims.put("iss", wireMockServer.baseUrl());
        claims.put("azp", State.AZP);

        final String token = generateIdToken(claims);
        return token;
    }

    private String generateIdToken(final Map<String, Object> claims) {
        try (final InputStream resource = MockOpenIDProvider.class.getResourceAsStream("/signing-key.json")) {
            final String jwks = IOUtils.toString(resource, UTF_8);
            final PublicJsonWebKey jwk = PublicJsonWebKey.Factory
                .newPublicJwk(jwks);

            final JsonWebSignature jws = new JsonWebSignature();
            jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256);
            jws.setHeader(TYPE, "JWT");
            jws.setKey(jwk.getPrivateKey());
            final JwtClaims jwtClaims = new JwtClaims();
            jwtClaims.setJwtId(UUID.randomUUID().toString());
            jwtClaims.setExpirationTimeMinutesInTheFuture(5);
            jwtClaims.setIssuedAtToNow();
            // override/set claims
            claims.entrySet().forEach(entry -> jwtClaims.setClaim(entry.getKey(), entry.getValue()));
            jws.setPayload(jwtClaims.toJson());

            return jws.getCompactSerialization();
        } catch (final IOException ex) {
            throw new UncheckedIOException("Unable to read JWK", ex);
        } catch (final JoseException ex) {
            throw new UncheckedJoseException("Unable to generate DPoP token", ex);
        }
    }

    private String getResource(final String path, final String baseUrl) {
        return getResource(path)
                .replace("{{baseUrl}}", baseUrl)
                .replace("{{tokenEndpoint}}", Utils.OAUTH_TOKEN_ENDPOINT)
                .replace("{{jwksEndpoint}}", Utils.JWKS_ENDPOINT);
    }

    private String getResource(final String path) {
        try (final InputStream res = MockOpenIDProvider.class.getResourceAsStream(path)) {
            return new String(IOUtils.toByteArray(res), UTF_8);
        } catch (final IOException ex) {
            throw new UncheckedIOException("Could not read class resource", ex);
        }
    }
}
