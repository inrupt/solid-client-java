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
package com.inrupt.client.examples.spring.web;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import com.inrupt.client.examples.spring.web.controller.SolidController;
import com.inrupt.client.examples.spring.web.model.*;

import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.util.ResourceUtils;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.Map;

import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.lang.JoseException;
import org.jose4j.lang.UncheckedJoseException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = WebApplication.class)
class WebApplicationTests {

    static final MockWebServer mockServer = new MockWebServer();

    @Autowired
    SolidController controller;

    @Autowired
    TestRestTemplate restTemplate;

    @BeforeAll
    static void setup() {
        mockServer.start();
    }

    @AfterAll
    static void teardown() {
        mockServer.stop();
    }

    @Test
    void contextLoads() {
        assertThat(controller).isNotNull();
    }

    @Test
    void testIndexPage() {
        final var entity = restTemplate.getForEntity("/", String.class);
        assertThat(entity.getStatusCode().value()).isEqualTo(200);
        assertThat(entity.getHeaders().getFirst("Content-Type")).isEqualTo("text/html;charset=UTF-8");
        assertThat(entity.hasBody());
        assertThat(entity.getBody()).contains("Authentication Demo");
    }

    @Test
    void testScriptAsset() {
        final var entity = restTemplate.getForEntity("/assets/script.js", String.class);
        assertThat(entity.getStatusCode().value()).isEqualTo(200);
        assertThat(entity.getHeaders().getFirst("Content-Type")).isEqualTo("text/javascript");
        assertThat(entity.hasBody());
    }

    @Test
    void testUnauthenticatedProfileEndpoint() {
        final var entity = restTemplate.getForEntity("/api/profile", Map.class);
        assertThat(entity.getStatusCode().value()).isEqualTo(401);
    }

    @Test
    void testUnauthenticatedWebIdEndpoint() {
        final var entity = restTemplate.getForEntity("/api/webid", Map.class);
        assertThat(entity.getStatusCode().value()).isEqualTo(401);
    }

    @Test
    void testUnauthenticatedResourceEndpoint() {
        final var entity = restTemplate.getForEntity("/api/resource?uri=https://example.com", byte[].class);
        assertThat(entity.getStatusCode().value()).isEqualTo(401);
    }

    @Test
    void testControllerWebId() {
        final var idToken = generateIdToken("user", "https://issuer.example", "https://example.com");
        final var oauthUser = new DefaultOidcUser(AuthorityUtils.NO_AUTHORITIES,
            OidcIdToken.withTokenValue(idToken)
                       .expiresAt(Instant.now().plusSeconds(100)).subject("user").claim("webid", "https://example.com")
                       .build());
        final var webid = controller.getUser(oauthUser);
        assertThat(webid.id()).isEqualTo(URI.create("https://example.com"));
    }

    @Test
    void testControllerProfile() {
        final var webid = mockServer.baseUrl() + "/user";
        final var issuer = mockServer.baseUrl() + "/oidc";
        final var storage = mockServer.baseUrl() + "/storage/";
        final var idToken = generateIdToken("user", issuer, webid);
        final var oauthUser = new DefaultOidcUser(AuthorityUtils.NO_AUTHORITIES,
            OidcIdToken.withTokenValue(idToken)
                       .expiresAt(Instant.now().plusSeconds(100)).subject("user").claim("webid", webid)
                       .build());
        final var profile = controller.getProfile(oauthUser);
        assertThat(profile.id()).isEqualTo(URI.create(webid));
        assertThat(profile.storages()).contains(URI.create(storage));
        assertThat(profile.issuers()).contains(URI.create(issuer));
    }

    @Test
    void testControllerStorage() {
        final var webid = mockServer.baseUrl() + "/user";
        final var storage = mockServer.baseUrl() + "/storage/";
        final var idToken = generateIdToken("user", mockServer.baseUrl() + "/oidc", webid);
        final var oauthUser = new DefaultOidcUser(AuthorityUtils.NO_AUTHORITIES,
            OidcIdToken.withTokenValue(idToken)
                       .expiresAt(Instant.now().plusSeconds(100)).subject("user").claim("webid", webid)
                       .build());
        final var resource = controller.getResource(oauthUser, URI.create(storage));
        final var body = new String(resource.getBody(), UTF_8);
        assertThat(body).contains("<http://www.w3.org/ns/ldp#contains>");
        assertThat(body).contains("<" + storage + "resource1>");
    }

    String generateIdToken(final String sub, final String issuer, final String webid) {
        try {
            final var jwk = PublicJsonWebKey.Factory
                   .newPublicJwk(ResourceUtils.readResource("testKey.json"));
            return Jwt.claims()
                    .subject(sub)
                    .issuer(issuer)
                    .claim("webid", webid)
                    .audience("solid").jws()
                    .keyId("7ZG5H7LrFxphsVW1G_wTKA").sign(jwk.getPrivateKey());
        } catch (final IOException | JoseException ex) {
            throw new UncheckedJoseException("Could not build token", ex);
        }
    }
}
