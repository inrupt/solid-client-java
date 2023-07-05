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
package com.inrupt.client.core;

import static com.inrupt.client.core.DefaultClientNonRdfTest.generateIdToken;
import static com.inrupt.client.core.DefaultClientNonRdfTest.getDpopKey;
import static org.jose4j.lang.HashUtil.SHA_256;
import static org.junit.jupiter.api.Assertions.*;

import com.inrupt.client.Client;
import com.inrupt.client.ClientProvider;
import com.inrupt.client.Request;
import com.inrupt.client.Response;
import com.inrupt.client.auth.Session;
import com.inrupt.client.openid.OpenIdConfig;
import com.inrupt.client.openid.OpenIdSession;
import com.inrupt.client.rdf.legacy.RDFLegacyBodyHandlers;
import com.inrupt.client.rdf.legacy.RDFLegacyBodyPublishers;

import java.io.IOException;
import java.net.URI;
import java.security.KeyPair;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.util.Values;
import org.jose4j.jwk.PublicJsonWebKey;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class DefaultClientRdfRDFLegacyTest {

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
    void testSendOfModelProtectedResource() {

        final Request request = Request.newBuilder()
                .uri(URI.create(baseUri.get() + "/protected/resource"))
                .header("Accept", "text/turtle")
                .GET()
                .build();

        final Response<Model> response = client.send(request, RDFLegacyBodyHandlers.ofModel())
            .toCompletableFuture().join();
        assertEquals(200, response.statusCode());

        final Model responseBody = response.body();
        assertEquals(7, responseBody.size());
        assertTrue(responseBody.subjects().contains(
                Values.iri("http://example.test/me"))
        );
    }

    @Test
    void testSendOfModel() {

        final Request request = Request.newBuilder()
                .uri(URI.create(baseUri.get() + "/example"))
                .header("Accept", "text/turtle")
                .GET()
                .build();

        final Response<Model> response = client.send(request, RDFLegacyBodyHandlers.ofModel())
            .toCompletableFuture().join();

        assertEquals(200, response.statusCode());

        final Model responseBody = response.body();
        assertEquals(7, responseBody.size());
        assertTrue(responseBody.subjects().contains(
                Values.iri("http://example.test/me"))
        );
    }

    @Test
    void testSendOfModelAsync() {
        final Request request = Request.newBuilder()
                .uri(URI.create(baseUri.get() + "/example"))
                .header("Accept", "text/turtle")
                .GET()
                .build();

        final Response<Model> response = client.send(request, RDFLegacyBodyHandlers.ofModel())
            .toCompletableFuture().join();

        assertEquals(200, response.statusCode());

        final Model model = response.body();
        assertEquals(7, model.size());
        assertTrue(model.contains(
            null,
            null,
            Values.iri("http://example.test//settings/prefs.ttl"))
        );
    }

    @Test
    void testOfModelPublisherBearer() {
        final Model model = new ModelBuilder()
                .add(Values.iri("http://example.test/s"), Values.iri("http://example.test/p"), Values.literal("object"))
                .build();

        final Request request = Request.newBuilder()
                .uri(URI.create(baseUri.get() + "/postBearerToken"))
                .header("Content-Type", "text/turtle")
                .POST(RDFLegacyBodyPublishers.ofModel(model))
                .build();

        final Response<Void> response = client.send(request, Response.BodyHandlers.discarding())
            .toCompletableFuture().join();

        assertEquals(401, response.statusCode());
        assertEquals(Optional.of("Bearer,DPoP algs=\"ES256\""), response.headers().firstValue("WWW-Authenticate"));

        final PublicJsonWebKey ecJwk = getDpopKey("/ec-key.json");
        final PublicJsonWebKey rsaJwk = getDpopKey("/rsa-key.json");
        final OpenIdConfig config = new OpenIdConfig();
        final Map<String, KeyPair> keypairs = new HashMap<>();
        keypairs.put("ES256", new KeyPair(ecJwk.getPublicKey(), ecJwk.getPrivateKey()));
        keypairs.put("RS256", new KeyPair(rsaJwk.getPublicKey(), rsaJwk.getPrivateKey()));
        config.setProofKeyPairs(keypairs);

        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", WEBID);
        claims.put("sub", SUB);
        claims.put("iss", ISS);
        claims.put("azp", AZP);
        claims.put("cnf", Collections.singletonMap("jkt", ecJwk.calculateBase64urlEncodedThumbprint(SHA_256)));
        final String token = generateIdToken(claims);
        final Session session = OpenIdSession.ofIdToken(token, config);
        assertEquals(Optional.of(URI.create(WEBID)), session.getPrincipal());

        final Session session2 = Session.anonymous();
        assertFalse(session2.getPrincipal().isPresent());
        assertNotEquals(session2.getId(), session.getId());
        assertFalse(session2.generateProof(null, null).isPresent());
        assertFalse(session2.selectThumbprint(Collections.singleton("ES256")).isPresent());

        assertDoesNotThrow(() -> {
            final Response<Void> res = client.session(session).send(request, Response.BodyHandlers.discarding())
                .toCompletableFuture().join();
            assertEquals(201, res.statusCode());
        });
    }

    @Test
    void testOfModelPublisher() {
        final Model model = new ModelBuilder()
                .add(Values.iri("http://example.test/s"), Values.iri("http://example.test/p"), Values.literal("object"))
                .build();

        final Request request = Request.newBuilder()
                .uri(URI.create(baseUri.get() + "/postOneTriple"))
                .header("Content-Type", "text/turtle")
                .POST(RDFLegacyBodyPublishers.ofModel(model))
                .build();

        final Response<Void> response = client.send(request, Response.BodyHandlers.discarding())
            .toCompletableFuture().join();

        assertEquals(401, response.statusCode());
        assertEquals(Optional.of("Unknown, Bearer, DPoP algs=\"ES256\", UMA ticket=\"ticket-12345\", as_uri=\"" +
                    baseUri.get() + "\""),
                response.headers().firstValue("WWW-Authenticate"));
    }

    @Test
    void testOfModelPublisherSession() throws IOException, InterruptedException {
        final Model model = new ModelBuilder()
                .add(Values.iri("http://example.test/s"), Values.iri("http://example.test/p"), Values.literal("object"))
                .build();

        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", WEBID);
        claims.put("sub", SUB);
        claims.put("iss", ISS);
        claims.put("azp", AZP);
        final String token = generateIdToken(claims);

        final Request request = Request.newBuilder()
                .uri(URI.create(baseUri.get() + "/postOneTriple"))
                .header("Content-Type", "text/turtle")
                .POST(RDFLegacyBodyPublishers.ofModel(model))
                .build();

        client.session(OpenIdSession.ofIdToken(token)).send(request, Response.BodyHandlers.discarding())
            .thenAccept(response -> {
                assertEquals(401, response.statusCode());
                assertEquals(Optional.of("Unknown, Bearer, DPoP algs=\"ES256\", " +
                            "UMA ticket=\"ticket-12345\", as_uri=\"" + baseUri.get() + "\""),
                        response.headers().firstValue("WWW-Authenticate"));
            })
            .toCompletableFuture().join();
    }

    @Test
    void testPutRDF() throws Exception {
        final Model model = new ModelBuilder()
                .add(Values.iri("http://example.test/s"), Values.iri("http://example.test/p"), Values.literal("object"))
                .build();

        final Request request = Request.newBuilder()
                .uri(URI.create(baseUri.get() + "/putRDF"))
                .header("Content-Type", "text/turtle")
                .PUT(RDFLegacyBodyPublishers.ofModel(model))
                .build();

        client.send(request, Response.BodyHandlers.discarding()).thenAccept(response -> {
            assertEquals(401, response.statusCode());
            assertEquals(Optional.of("Bearer, DPoP algs=\"ES256 PS256\""),
                    response.headers().firstValue("WWW-Authenticate"));

        }).toCompletableFuture().join();

        final PublicJsonWebKey ecJwk = getDpopKey("/ec-key.json");
        final PublicJsonWebKey rsaJwk = getDpopKey("/rsa-key.json");
        final OpenIdConfig config = new OpenIdConfig();
        final Map<String, KeyPair> keypairs = new HashMap<>();
        keypairs.put("ES256", new KeyPair(ecJwk.getPublicKey(), ecJwk.getPrivateKey()));
        keypairs.put("RS256", new KeyPair(rsaJwk.getPublicKey(), rsaJwk.getPrivateKey()));
        config.setProofKeyPairs(keypairs);

        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", WEBID);
        claims.put("sub", SUB);
        claims.put("iss", ISS);
        claims.put("azp", AZP);
        claims.put("cnf", Collections.singletonMap("jkt", ecJwk.calculateBase64urlEncodedThumbprint(SHA_256)));
        final String token = generateIdToken(claims);
        final Session session = OpenIdSession.ofIdToken(token, config);
        assertEquals(Optional.of(URI.create(WEBID)), session.getPrincipal());

        client.session(session).send(request, Response.BodyHandlers.discarding()).thenAccept(response -> {
            assertEquals(201, response.statusCode());
        }).toCompletableFuture().join();
    }
}
