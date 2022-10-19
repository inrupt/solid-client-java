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

import com.inrupt.client.api.URIBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class UmaClientTest {

    private static final MockAuthorizationServer as = new MockAuthorizationServer();
    private static final Map<String, String> config = new HashMap<>();
    private static final String ID_TOKEN_CLAIM_TOKEN_FORMAT =
        "http://openid.net/specs/openid-connect-core-1_0.html#IDToken";

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
        System.out.println("-----> AS URI: " + asUri + " <-------");
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
    void testSimpleTokenInvalidJsonResponse() {
        final var asUri = URI.create(config.get("as_uri"));
        final var client = new UmaClient();
        final var metadata = client.metadata(asUri);
        final var ticket = "ticket-invalid-response";
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

    @Test
    void testTokenNegotiationMissingResponseTicket() {
        final var asUri = URI.create(config.get("as_uri"));
        final var client = new UmaClient();
        final var metadata = client.metadata(asUri);
        final var ticket = "ticket-need-info-no-response-ticket";
        final var req = new TokenRequest(ticket, null, null, null, null);

        assertThrows(RequestDeniedException.class, () -> client.token(metadata.tokenEndpoint, req, needInfo -> {
            throw new UmaException("Unable to negotiate a simple token");
        }));
    }

    @Test
    void testTokenNegotiationNullResponse() {
        final var asUri = URI.create(config.get("as_uri"));
        final var client = new UmaClient();
        final var metadata = client.metadata(asUri);
        final var ticket = "ticket-need-info-with-ticket";
        final var req = new TokenRequest(ticket, null, null, null, null);

        assertThrows(RequestDeniedException.class, () -> client.token(metadata.tokenEndpoint, req, needInfo -> null));
    }

    @Test
    void testTokenNegotiationOidcMapper() {
        final var asUri = URI.create(config.get("as_uri"));
        final var client = new UmaClient();
        final var metadata = client.metadata(asUri);
        final var idToken = "oidc-id-token";

        final var ticket = "ticket-need-info-oidc-requirement";
        final var req = new TokenRequest(ticket, null, null, null, null);

        final var token = client.token(metadata.tokenEndpoint, req, needInfo ->
                ClaimToken.of(idToken, ID_TOKEN_CLAIM_TOKEN_FORMAT));

        assertEquals("token-from-id-token", token.accessToken);
        assertEquals("Bearer", token.tokenType);
    }

    @Test
    void testTokenNegotiationRecursionLimit() {
        final var asUri = URI.create(config.get("as_uri"));
        final var client = new UmaClient(0);
        final var metadata = client.metadata(asUri);
        final var idToken = "oidc-id-token";

        final var ticket = "ticket-need-info-oidc-requirement";
        final var req = new TokenRequest(ticket, null, null, null, null);

        assertThrows(UmaException.class, () -> client.token(metadata.tokenEndpoint, req, needInfo ->
                ClaimToken.of(idToken, ID_TOKEN_CLAIM_TOKEN_FORMAT)));
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
        final var err = assertThrows(CompletionException.class,
                client.metadataAsync(asUri).toCompletableFuture()::join);
        assertTrue(err.getCause() instanceof UmaException);
    }

    @Test
    void testMetadataMalformedAsync() {
        final var asUri = URI.create(config.get("as_uri") + "/malformed");
        final var client = new UmaClient();
        final var err = assertThrows(CompletionException.class,
                client.metadataAsync(asUri).toCompletableFuture()::join);
        assertTrue(err.getCause() instanceof UmaException);
    }

    @Test
    void testSimpleTokenNegotiationInvalidTicketAsync() {
        final var asUri = URI.create(config.get("as_uri"));
        final var client = new UmaClient();
        final var ticket = "ticket-invalid-grant";
        final var req = new TokenRequest(ticket, null, null, null, null);

        final var err = assertThrows(CompletionException.class,
                client.metadataAsync(asUri)
                    .thenCompose(metadata ->
                        client.tokenAsync(metadata.tokenEndpoint, req, needInfo -> {
                            throw new UmaException("Unable to negotiation a token");
                        }))
                    .toCompletableFuture()::join);

        assertTrue(err.getCause() instanceof InvalidGrantException);
    }

    @Test
    void testSimpleTokenNegotiationRequestDeniedAsync() {
        final var asUri = URI.create(config.get("as_uri"));
        final var client = new UmaClient();
        final var ticket = "ticket-request-denied";
        final var req = new TokenRequest(ticket, null, null, null, null);

        final var err = assertThrows(CompletionException.class,
                client.metadataAsync(asUri)
                    .thenCompose(metadata ->
                        client.tokenAsync(metadata.tokenEndpoint, req, needInfo -> {
                            throw new UmaException("Unable to negotiation a token");
                        }))
                    .toCompletableFuture()::join);

        assertTrue(err.getCause() instanceof RequestDeniedException);
    }

    @Test
    void testSimpleTokenUnknownErrorAsync() {
        final var asUri = URI.create(config.get("as_uri"));
        final var client = new UmaClient();
        final var ticket = "ticket-unknown-error";
        final var req = new TokenRequest(ticket, null, null, null, null);

        final var err = assertThrows(CompletionException.class,
                client.metadataAsync(asUri)
                    .thenCompose(metadata ->
                        client.tokenAsync(metadata.tokenEndpoint, req, needInfo -> {
                            throw new UmaException("Unable to negotiation a token");
                        }))
                    .toCompletableFuture()::join);

        assertTrue(err.getCause() instanceof UmaException);
    }

    @Test
    void testSimpleTokenMalformedResponseAsync() {
        final var asUri = URI.create(config.get("as_uri"));
        final var client = new UmaClient();
        final var ticket = "ticket-malformed-response";
        final var req = new TokenRequest(ticket, null, null, null, null);

        final var err = assertThrows(CompletionException.class,
                client.metadataAsync(asUri)
                    .thenCompose(metadata ->
                        client.tokenAsync(metadata.tokenEndpoint, req, needInfo -> {
                            throw new UmaException("Unable to negotiation a token");
                        }))
                    .toCompletableFuture()::join);

        assertTrue(err.getCause() instanceof UmaException);
    }

    @Test
    void testSimpleTokenInvalidJsonResponseAsync() {
        final var asUri = URI.create(config.get("as_uri"));
        final var client = new UmaClient();
        final var ticket = "ticket-invalid-response";
        final var req = new TokenRequest(ticket, null, null, null, null);

        final var err = assertThrows(CompletionException.class,
                client.metadataAsync(asUri)
                    .thenCompose(metadata ->
                        client.tokenAsync(metadata.tokenEndpoint, req, needInfo -> {
                            throw new UmaException("Unable to negotiate a token");
                        }))
                    .toCompletableFuture()::join);

        assertTrue(err.getCause() instanceof UmaException);
    }

    @Test
    void testSimpleTokenInvalidScopeAsync() {
        final var asUri = URI.create(config.get("as_uri"));
        final var client = new UmaClient();
        final var ticket = "ticket-invalid-scope";
        final var req = new TokenRequest(ticket, null, null, null, List.of("invalid-scope"));

        final var err = assertThrows(CompletionException.class,
                client.metadataAsync(asUri)
                    .thenCompose(metadata ->
                        client.tokenAsync(metadata.tokenEndpoint, req, needInfo -> {
                            throw new UmaException("Unable to negotiation a token");
                        }))
                    .toCompletableFuture()::join);

        assertTrue(err.getCause() instanceof InvalidScopeException);
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

    @Test
    void testTokenNegotiationMissingResponseTicketAsync() {
        final var asUri = URI.create(config.get("as_uri"));
        final var client = new UmaClient();
        final var ticket = "ticket-need-info-no-response-ticket";
        final var req = new TokenRequest(ticket, null, null, null, null);

        final var err = assertThrows(CompletionException.class,
                client.metadataAsync(asUri)
                    .thenCompose(metadata ->
                        client.tokenAsync(metadata.tokenEndpoint, req, needInfo -> {
                            throw new UmaException("Unable to negotiate a simple token");
                        }))
                    .toCompletableFuture()::join);

        assertTrue(err.getCause() instanceof RequestDeniedException);
    }

    @Test
    void testTokenNegotiationNullResponseAsync() {
        final var asUri = URI.create(config.get("as_uri"));
        final var client = new UmaClient();
        final var ticket = "ticket-need-info-with-ticket";
        final var req = new TokenRequest(ticket, null, null, null, null);

        final var err = assertThrows(CompletionException.class,
                client.metadataAsync(asUri)
                    .thenCompose(metadata ->
                        client.tokenAsync(metadata.tokenEndpoint, req, needInfo ->
                            CompletableFuture.completedFuture(null)))
                    .toCompletableFuture()::join);

        assertTrue(err.getCause() instanceof RequestDeniedException);
    }

    @Test
    void testTokenNegotiationOidcMapperAsync() {
        final var asUri = URI.create(config.get("as_uri"));
        final var client = new UmaClient();
        final var idToken = "oidc-id-token";
        final var ticket = "ticket-need-info-oidc-requirement";
        final var req = new TokenRequest(ticket, null, null, null, null);

        final var token = client.metadataAsync(asUri)
                .thenCompose(metadata ->
                    client.tokenAsync(metadata.tokenEndpoint, req, needInfo ->
                        CompletableFuture.completedFuture(ClaimToken.of(idToken, ID_TOKEN_CLAIM_TOKEN_FORMAT))))
                .toCompletableFuture().join();

        assertEquals("token-from-id-token", token.accessToken);
        assertEquals("Bearer", token.tokenType);
    }

    @Test
    void testTokenNegotiationRecursionLimitAsync() {
        final var asUri = URI.create(config.get("as_uri"));
        final var client = new UmaClient(0);
        final var idToken = "oidc-id-token";
        final var ticket = "ticket-need-info-oidc-requirement";
        final var req = new TokenRequest(ticket, null, null, null, null);

        final var err = assertThrows(CompletionException.class,
                client.metadataAsync(asUri)
                    .thenCompose(metadata ->
                        client.tokenAsync(metadata.tokenEndpoint, req, needInfo ->
                            CompletableFuture.completedFuture(ClaimToken.of(idToken, ID_TOKEN_CLAIM_TOKEN_FORMAT))))
                    .toCompletableFuture()::join);

        assertTrue(err.getCause() instanceof UmaException);
    }

    static void checkMetadata(final Metadata metadata) {
        final var asUri = URI.create(config.get("as_uri"));
        final var jwksEndpoint = URIBuilder.newBuilder(asUri).path("jwks").build();
        final var tokenEndpoint = URIBuilder.newBuilder(asUri).path("token").build();

        assertEquals(List.of("ES256", "RS256"), metadata.dpopSigningAlgValuesSupported);
        assertEquals(List.of("urn:ietf:params:oauth:grant-type:uma-ticket"),
                metadata.grantTypesSupported);
        assertEquals(asUri, metadata.issuer);
        assertEquals(jwksEndpoint, metadata.jwksUri);
        assertEquals(tokenEndpoint, metadata.tokenEndpoint);
        assertEquals(List.of(
                    URI.create("https://www.w3.org/TR/vc-data-model/#json-ld"),
                    URI.create("http://openid.net/specs/openid-connect-core-1_0.html#IDToken")),
                metadata.umaProfilesSupported);
    }
}
