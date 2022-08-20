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

import static org.junit.jupiter.api.Assertions.*;

import com.inrupt.client.core.URIBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class UmaClientTest {

    private static MockAuthorizationServer as = new MockAuthorizationServer();
    private static Map<String, String> config = new HashMap<>();

    @BeforeAll
    static void setup() {
        config.putAll(as.start());
    }

    @AfterAll
    static void teardown() {
        as.stop();
    }

    @Test
    void testMetadata() {
        final var asUri = URI.create(config.get("as_uri"));
        final var client = new UmaClient();
        final var metadata = client.metadata(asUri);
        checkMetadata(metadata);
    }

    @Test
    void testMetadataNotFound() {
        final var asUri = URI.create(config.get("as_uri") + "/not-found");
        final var client = new UmaClient();
        assertThrows(UmaException.class, () -> client.metadata(asUri));
    }

    @Test
    void testMetadataMalformed() {
        final var asUri = URI.create(config.get("as_uri") + "/malformed");
        final var client = new UmaClient();
        assertThrows(UmaException.class, () -> client.metadata(asUri));
    }

    @Test
    void testSimpleTokenNegotiationInvalidTicket() {
        final var asUri = URI.create(config.get("as_uri"));
        final var client = new UmaClient();
        final var metadata = client.metadata(asUri);
        final var ticket = "ticket-invalid-grant";
        final var req = new TokenRequest(ticket, null, null, null, null);

        assertThrows(InvalidGrantException.class, () -> client.token(metadata.tokenEndpoint, req, needInfo -> {
            throw new UmaException("Unable to negotiation a token");
        }));
    }

    @Test
    void testSimpleTokenNegotiationRequestDenied() {
        final var asUri = URI.create(config.get("as_uri"));
        final var client = new UmaClient();
        final var metadata = client.metadata(asUri);
        final var ticket = "ticket-request-denied";
        final var req = new TokenRequest(ticket, null, null, null, null);

        assertThrows(RequestDeniedException.class, () -> client.token(metadata.tokenEndpoint, req, needInfo -> {
            throw new UmaException("Unable to negotiation a token");
        }));
    }

    @Test
    void testSimpleTokenUnknownError() {
        final var asUri = URI.create(config.get("as_uri"));
        final var client = new UmaClient();
        final var metadata = client.metadata(asUri);
        final var ticket = "ticket-unknown-error";
        final var req = new TokenRequest(ticket, null, null, null, null);

        assertThrows(UmaException.class, () -> client.token(metadata.tokenEndpoint, req, needInfo -> {
            throw new UmaException("Unable to negotiation a token");
        }));
    }

    @Test
    void testSimpleTokenMalformedResponse() {
        final var asUri = URI.create(config.get("as_uri"));
        final var client = new UmaClient();
        final var metadata = client.metadata(asUri);
        final var ticket = "ticket-malformed-response";
        final var req = new TokenRequest(ticket, null, null, null, null);

        assertThrows(UmaException.class, () -> client.token(metadata.tokenEndpoint, req, needInfo -> {
            throw new UmaException("Unable to negotiation a token");
        }));
    }

    @Test
    void testSimpleTokenInvalidScope() {
        final var asUri = URI.create(config.get("as_uri"));
        final var client = new UmaClient();
        final var metadata = client.metadata(asUri);
        final var ticket = "ticket-invalid-scope";
        final var req = new TokenRequest(ticket, null, null, null, List.of("invalid-scope"));

        assertThrows(InvalidScopeException.class, () -> client.token(metadata.tokenEndpoint, req, needInfo -> {
            throw new UmaException("Unable to negotiation a token");
        }));
    }

    @Test
    void testSimpleTokenNegotiation() {
        final var asUri = URI.create(config.get("as_uri"));
        final var client = new UmaClient();
        final var metadata = client.metadata(asUri);
        final var ticket = "ticket-12345";
        final var req = new TokenRequest(ticket, null, null, null, null);

        final var res = client.token(metadata.tokenEndpoint, req, needInfo -> {
            throw new UmaException("Unable to negotiate a simple token");
        });

        assertEquals("token-12345", res.accessToken);
        assertEquals("Bearer", res.tokenType);
    }


    // ---------------
    //   Async tests
    // ---------------
    @Test
    void testMetadataAsync() {
        final var asUri = URI.create(config.get("as_uri"));
        final var client = new UmaClient();
        final var metadata = client.metadataAsync(asUri).toCompletableFuture().join();
        checkMetadata(metadata);
    }

    @Test
    void testMetadataNotFoundAsync() {
        final var asUri = URI.create(config.get("as_uri") + "/not-found");
        final var client = new UmaClient();
        final var throwable = assertThrows(CompletionException.class,
                client.metadataAsync(asUri).toCompletableFuture()::join);
        assertTrue(throwable.getCause() instanceof UmaException);
    }

    @Test
    void testMetadataMalformedAsync() {
        final var asUri = URI.create(config.get("as_uri") + "/malformed");
        final var client = new UmaClient();
        final var throwable = assertThrows(CompletionException.class,
                client.metadataAsync(asUri).toCompletableFuture()::join);
        assertTrue(throwable.getCause() instanceof UmaException);
    }

    @Test
    void testSimpleTokenNegotiationInvalidTicketAsync() {
        final var asUri = URI.create(config.get("as_uri"));
        final var client = new UmaClient();
        final var ticket = "ticket-invalid-grant";
        final var req = new TokenRequest(ticket, null, null, null, null);

        final var throwable = assertThrows(CompletionException.class,
                client.metadataAsync(asUri)
                    .thenCompose(metadata ->
                        client.tokenAsync(metadata.tokenEndpoint, req, needInfo -> {
                            throw new UmaException("Unable to negotiation a token");
                        }))
                    .toCompletableFuture()::join);

        assertTrue(throwable.getCause() instanceof InvalidGrantException);
    }

    @Test
    void testSimpleTokenNegotiationRequestDeniedAsync() {
        final var asUri = URI.create(config.get("as_uri"));
        final var client = new UmaClient();
        final var ticket = "ticket-request-denied";
        final var req = new TokenRequest(ticket, null, null, null, null);

        final var throwable = assertThrows(CompletionException.class,
                client.metadataAsync(asUri)
                    .thenCompose(metadata ->
                        client.tokenAsync(metadata.tokenEndpoint, req, needInfo -> {
                            throw new UmaException("Unable to negotiation a token");
                        }))
                    .toCompletableFuture()::join);

        assertTrue(throwable.getCause() instanceof RequestDeniedException);
    }

    @Test
    void testSimpleTokenUnknownErrorAsync() {
        final var asUri = URI.create(config.get("as_uri"));
        final var client = new UmaClient();
        final var ticket = "ticket-unknown-error";
        final var req = new TokenRequest(ticket, null, null, null, null);

        final var throwable = assertThrows(CompletionException.class,
                client.metadataAsync(asUri)
                    .thenCompose(metadata ->
                        client.tokenAsync(metadata.tokenEndpoint, req, needInfo -> {
                            throw new UmaException("Unable to negotiation a token");
                        }))
                    .toCompletableFuture()::join);

        assertTrue(throwable.getCause() instanceof UmaException);
    }

    @Test
    void testSimpleTokenMalformedResponseAsync() {
        final var asUri = URI.create(config.get("as_uri"));
        final var client = new UmaClient();
        final var ticket = "ticket-malformed-response";
        final var req = new TokenRequest(ticket, null, null, null, null);

        final var throwable = assertThrows(CompletionException.class,
                client.metadataAsync(asUri)
                    .thenCompose(metadata ->
                        client.tokenAsync(metadata.tokenEndpoint, req, needInfo -> {
                            throw new UmaException("Unable to negotiation a token");
                        }))
                    .toCompletableFuture()::join);

        assertTrue(throwable.getCause() instanceof UmaException);
    }

    @Test
    void testSimpleTokenInvalidScopeAsync() {
        final var asUri = URI.create(config.get("as_uri"));
        final var client = new UmaClient();
        final var ticket = "ticket-invalid-scope";
        final var req = new TokenRequest(ticket, null, null, null, List.of("invalid-scope"));

        final var throwable = assertThrows(CompletionException.class,
                client.metadataAsync(asUri)
                    .thenCompose(metadata ->
                        client.tokenAsync(metadata.tokenEndpoint, req, needInfo -> {
                            throw new UmaException("Unable to negotiation a token");
                        }))
                    .toCompletableFuture()::join);

        assertTrue(throwable.getCause() instanceof InvalidScopeException);
    }

    @Test
    void testSimpleTokenNegotiationAsync() {
        final var asUri = URI.create(config.get("as_uri"));
        final var client = new UmaClient();
        final var ticket = "ticket-12345";
        final var req = new TokenRequest(ticket, null, null, null, null);

        final var res = client.metadataAsync(asUri).thenCompose(metadata ->
                client.tokenAsync(metadata.tokenEndpoint, req, needInfo -> {
                    throw new UmaException("Unable to negotiate a simple token");
                })).toCompletableFuture().join();

        assertEquals("token-12345", res.accessToken);
        assertEquals("Bearer", res.tokenType);
    }

    static void checkMetadata(final Metadata metadata) {
        final var asUri = URI.create(config.get("as_uri"));
        final var jwksEndpoint = URIBuilder.newBuilder(asUri).path("jwks").build();
        final var tokenEndpoint = URIBuilder.newBuilder(asUri).path("token").build();

        assertEquals(List.of("ES256", "RS256"), metadata.dpopSigningAlgValuesSupported);
        assertEquals(List.of("urn:ietf:params:oauth:grant-type:uma-ticket"),
                metadata.grantTypesSupported);
        assertEquals(asUri, metadata.issuer);
        // TODO - re-enable these checks once the URIBuilder class is fixed
        //assertEquals(jwksEndpoint, metadata.jwksUri);
        //assertEquals(tokenEndpoint, metadata.tokenEndpoint);
        assertEquals(List.of(
                    URI.create("https://www.w3.org/TR/vc-data-model/#json-ld"),
                    URI.create("http://openid.net/specs/openid-connect-core-1_0.html#IDToken")),
                metadata.umaProfilesSupported);
    }
}
