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
package com.inrupt.client.core;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.jose4j.jwx.HeaderParameterNames.TYPE;
import static org.junit.jupiter.api.Assertions.*;

import com.inrupt.client.Client;
import com.inrupt.client.ClientProvider;
import com.inrupt.client.Request;
import com.inrupt.client.Response;
import com.inrupt.client.Session;
import com.inrupt.client.jena.JenaBodyHandlers;
import com.inrupt.client.jena.JenaBodyPublishers;
import com.inrupt.client.openid.OpenIdSession;
import com.inrupt.client.openid.OpenIdVerificationConfig;
import com.inrupt.client.uma.UmaSession;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;
import org.jose4j.lang.UncheckedJoseException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class DefaultClientTest {

    private static final String WEBID = "https://id.example/username";
    private static final String SUB = "username";
    private static final String ISS = "https://iss.example";
    private static final String AZP = "https://app.example";
    private static final MockHttpService mockHttpServer = new MockHttpService();
    private static final Client client = ClientProvider.getClient();
    private static final AtomicReference<String> baseUri = new AtomicReference<>();

    @BeforeAll
    static void setup() {
        baseUri.set(mockHttpServer.start());
    }

    @AfterAll
    static void teardown() {
        mockHttpServer.stop();
    }

    @Test
    void clientLoader() {
        assertNotNull(client);
    }

    @Test
    void testSendOfString() throws IOException, InterruptedException {
        final Request request = Request.newBuilder()
                .uri(URI.create(baseUri.get() + "/file"))
                .GET()
                .build();

        final Response<String> response = client.send(request, Response.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Julie C. Sparks and David Widger"));
    }

    @Test
    void testSendOfStringAsync() throws IOException, InterruptedException {
        final Request request = Request.newBuilder()
                .uri(URI.create(baseUri.get() + "/file"))
                .GET()
                .build();

        final Response<String> response = client.sendAsync(request, Response.BodyHandlers.ofString())
            .toCompletableFuture().join();

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Julie C. Sparks and David Widger"));
    }

    @Test
    void testSendOfModelProtectedResource() throws IOException, InterruptedException {

        final Request request = Request.newBuilder()
                .uri(URI.create(baseUri.get() + "/protected/resource"))
                .header("Accept", "text/turtle")
                .GET()
                .build();

        final Response<Model> response = client.send(request, JenaBodyHandlers.ofModel());
        assertEquals(200, response.statusCode());

        final Model responseBody = response.body();
        assertEquals(7, responseBody.size());
        assertTrue(responseBody.contains(
            ResourceFactory.createResource("http://example.test/me"),
            null)
        );
    }

    @Test
    void testSendOfModel() throws IOException, InterruptedException {

        final Request request = Request.newBuilder()
                .uri(URI.create(baseUri.get() + "/example"))
                .header("Accept", "text/turtle")
                .GET()
                .build();

        final Response<Model> response = client.send(request, JenaBodyHandlers.ofModel());
        assertEquals(200, response.statusCode());

        final Model responseBody = response.body();
        assertEquals(7, responseBody.size());
        assertTrue(responseBody.contains(
            ResourceFactory.createResource("http://example.test/me"),
            null)
        );
    }

    @Test
    void testSendOfModelAsync() throws IOException {
        final Request request = Request.newBuilder()
                .uri(URI.create(baseUri.get() + "/example"))
                .header("Accept", "text/turtle")
                .GET()
                .build();

        final Response<Model> response = client.sendAsync(request, JenaBodyHandlers.ofModel())
            .toCompletableFuture().join();

        assertEquals(200, response.statusCode());

        final Model model = response.body();
        assertEquals(7, model.size());
        assertTrue(model.contains(
            null,
            null,
            ResourceFactory.createResource("http://example.test//settings/prefs.ttl"))
        );
    }

    @Test
    void testOfModelPublisherBearer() throws IOException, InterruptedException {
        final Model model = ModelFactory.createDefaultModel();

        model.add(
            model.createResource("http://example.test/s"),
            model.createProperty("http://example.test/p"),
            model.createLiteral("object")
        );

        final Request request = Request.newBuilder()
                .uri(URI.create(baseUri.get() + "/postBearerToken"))
                .header("Content-Type", "text/turtle")
                .POST(JenaBodyPublishers.ofModel(model))
                .build();

        final Response<Void> response = client.sendAsync(request, Response.BodyHandlers.discarding())
            .toCompletableFuture().join();

        assertEquals(401, response.statusCode());
        assertEquals(Optional.of("Bearer"), response.headers().firstValue("WWW-Authenticate"));

        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", WEBID);
        claims.put("sub", SUB);
        claims.put("iss", ISS);
        claims.put("azp", AZP);
        final String token = generateIdToken(claims);
        final Session session = OpenIdSession.ofIdToken(token);
        assertDoesNotThrow(() -> {
            final Response<Void> res = client.session(session).sendAsync(request, Response.BodyHandlers.discarding())
                .toCompletableFuture().join();
            assertEquals(201, res.statusCode());
        });
    }

    @Test
    void testOfModelPublisher() throws IOException, InterruptedException {
        final Model model = ModelFactory.createDefaultModel();

        model.add(
            model.createResource("http://example.test/s"),
            model.createProperty("http://example.test/p"),
            model.createLiteral("object")
        );

        final Request request = Request.newBuilder()
                .uri(URI.create(baseUri.get() + "/postOneTriple"))
                .header("Content-Type", "text/turtle")
                .POST(JenaBodyPublishers.ofModel(model))
                .build();

        final Response<Void> response = client.sendAsync(request, Response.BodyHandlers.discarding())
            .toCompletableFuture().join();

        assertEquals(401, response.statusCode());
        assertEquals(Optional.of("Unknown, Bearer, UMA ticket=\"ticket-12345\", as_uri=\"" + baseUri.get() + "\""),
                response.headers().firstValue("WWW-Authenticate"));
    }

    @Test
    void testOfModelPublisherSession() throws IOException, InterruptedException {
        final Model model = ModelFactory.createDefaultModel();

        model.add(
            model.createResource("http://example.test/s"),
            model.createProperty("http://example.test/p"),
            model.createLiteral("object")
        );

        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", WEBID);
        claims.put("sub", SUB);
        claims.put("iss", ISS);
        claims.put("azp", AZP);
        final String token = generateIdToken(claims);

        final Request request = Request.newBuilder()
                .uri(URI.create(baseUri.get() + "/postOneTriple"))
                .header("Content-Type", "text/turtle")
                .POST(JenaBodyPublishers.ofModel(model))
                .build();

        final Response<Void> response = client.session(OpenIdSession.ofIdToken(token))
            .sendAsync(request, Response.BodyHandlers.discarding())
            .toCompletableFuture().join();

        assertEquals(401, response.statusCode());
        assertEquals(Optional.of("Unknown, Bearer, UMA ticket=\"ticket-12345\", as_uri=\"" + baseUri.get() + "\""),
                response.headers().firstValue("WWW-Authenticate"));
    }

    @Test
    void testOfStringPublisherUmaSession() throws IOException, InterruptedException {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", WEBID);
        claims.put("sub", SUB);
        claims.put("iss", ISS);
        claims.put("azp", AZP);
        final String token = generateIdToken(claims);

        final Request request = Request.newBuilder()
                .uri(URI.create(baseUri.get() + "/postString"))
                .header("Content-Type", "text/plain")
                .POST(Request.BodyPublishers.ofString("Test String 1"))
                .build();

        final Response<Void> response = client.session(UmaSession.of(OpenIdSession.ofIdToken(token)))
            .sendAsync(request, Response.BodyHandlers.discarding())
            .toCompletableFuture().join();

        assertEquals(201, response.statusCode());
    }

    @Test
    void testOfStringPublisherUmaAnonSession() throws IOException, InterruptedException {
        final Request request = Request.newBuilder()
                .uri(URI.create(baseUri.get() + "/postString"))
                .header("Content-Type", "text/plain")
                .POST(Request.BodyPublishers.ofString("Test String 1"))
                .build();

        final Response<Void> response = client.session(UmaSession.of())
            .sendAsync(request, Response.BodyHandlers.discarding())
            .toCompletableFuture().join();

        assertEquals(401, response.statusCode());
    }

    @Test
    void testUmaSessionExpiredIdToken() throws Exception {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", WEBID);
        claims.put("sub", SUB);
        claims.put("iss", ISS);
        claims.put("azp", AZP);
        claims.put("exp", Instant.now().plusSeconds(2).getEpochSecond());
        claims.put("iat", Instant.now().minusSeconds(61).getEpochSecond());

        final String token = generateIdToken(claims);
        final OpenIdVerificationConfig config = new OpenIdVerificationConfig();
        config.setExpGracePeriodSecs(0);
        final Session s = OpenIdSession.ofIdToken(token, config);

        // Wait for the token to expire
        await().atMost(5, SECONDS).until(() -> !s.getCredential(OpenIdSession.ID_TOKEN).isPresent());

        final Request request = Request.newBuilder()
                .uri(URI.create(baseUri.get() + "/postString"))
                .header("Content-Type", "text/plain")
                .POST(Request.BodyPublishers.ofString("Test String 1"))
                .build();

        final Response<Void> response = client.session(UmaSession.of(s))
            .sendAsync(request, Response.BodyHandlers.discarding())
            .toCompletableFuture().join();

        assertEquals(401, response.statusCode());
    }

    @Test
    void testSendRequestImage() throws IOException, InterruptedException {
        final Request request = Request.newBuilder()
                .uri(URI.create(baseUri.get() + "/solid.png"))
                .GET()
                .build();

        final Response<byte[]> response = client.send(request, Response.BodyHandlers.ofByteArray());

        assertEquals(200, response.statusCode());
    }

    static String generateIdToken(final Map<String, Object> claims) {
        try (final InputStream resource = DefaultClientTest.class.getResourceAsStream("/signing-key.json")) {
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
}
