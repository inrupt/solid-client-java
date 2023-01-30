package com.inrupt.client.integration;

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

public class MockOpenIDProvider {
    private final WireMockServer wireMockServer;

    public MockOpenIDProvider() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
    }

    private void setupMocks() {
        wireMockServer.stubFor(get(urlEqualTo(Utils.OPENID_DISCOVERY_ENDPOINT))
                    .willReturn(aResponse()
                        .withStatus(Utils.SUCCESS)
                        .withHeader(Utils.CONTENT_TYPE, Utils.APPLICATION_JSON)
                        .withBody(getResource("/metadata.json", wireMockServer.baseUrl()))));

        wireMockServer.stubFor(post(urlPathMatching("/"+Utils.OAUTH_TOKEN_ENDPOINT))
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
        claims.put("webid", Utils.WEBID.toString());
        claims.put("sub", Utils.USERNAME);
        claims.put("iss", wireMockServer.baseUrl());
        claims.put("azp", Utils.AZP);

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

    private String getResource(final String path, final String issuer) {
        return getResource(path)
                .replace("{{issuerUrl}}", issuer)
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
