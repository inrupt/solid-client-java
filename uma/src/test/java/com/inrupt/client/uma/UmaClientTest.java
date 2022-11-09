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

import com.inrupt.client.util.URIBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class UmaClientTest {

    private static final MockAuthorizationServer as = new MockAuthorizationServer();
    private static final Map<String, String> config = new HashMap<>();
    private static final String ID_TOKEN_CLAIM_TOKEN_FORMAT =
        "http://openid.net/specs/openid-connect-core-1_0.html#IDToken";

    @BeforeAll
    static void setup() throws Exception {
        config.putAll(as.start());
    }

    @AfterAll
    static void teardown() {
        as.stop();
    }

    @Test
    void testMetadata() {
        final URI asUri = URI.create(config.get("as_uri"));
        final UmaClient client = new UmaClient();
        final Metadata metadata = client.metadata(asUri);
        checkMetadata(metadata);
    }

    @Test
    void testMetadataNotFound() {
        final URI asUri = URI.create(config.get("as_uri") + "/not-found");
        final UmaClient client = new UmaClient();
        assertThrows(UmaException.class, () -> client.metadata(asUri));
    }

    @Test
    void testMetadataMalformed() {
        final URI asUri = URI.create(config.get("as_uri") + "/malformed");
        final UmaClient client = new UmaClient();
        assertThrows(UmaException.class, () -> client.metadata(asUri));
    }

    @ParameterizedTest
    @MethodSource
    void testSimpleTokenError(final String ticket) {
        final URI asUri = URI.create(config.get("as_uri"));
        final UmaClient client = new UmaClient();
        final Metadata metadata = client.metadata(asUri);
        final TokenRequest req = new TokenRequest(ticket, null, null, null, null);

        assertThrows(UmaException.class,
                () -> client.token(metadata.tokenEndpoint, req, needInfo -> {
                    throw new UmaException("Unable to negotiation a token");
                }));
    }

    private static Stream<Arguments> testSimpleTokenError() {
        return Stream.of(
            Arguments.of("ticket-unknown-error"),
            Arguments.of("ticket-malformed-response"),
            Arguments.of("ticket-invalid-response"));
    }

    @Test
    void testSimpleTokenNegotiationInvalidTicket() {
        final URI asUri = URI.create(config.get("as_uri"));
        final UmaClient client = new UmaClient();
        final Metadata metadata = client.metadata(asUri);
        final String ticket = "ticket-invalid-grant";
        final TokenRequest req = new TokenRequest(ticket, null, null, null, null);

        assertThrows(InvalidGrantException.class,
                () -> client.token(metadata.tokenEndpoint, req, needInfo -> {
                    throw new UmaException("Unable to negotiation a token");
                }));
    }

    @Test
    void testSimpleTokenNegotiationRequestDenied() {
        final URI asUri = URI.create(config.get("as_uri"));
        final UmaClient client = new UmaClient();
        final Metadata metadata = client.metadata(asUri);
        final String ticket = "ticket-request-denied";
        final TokenRequest req = new TokenRequest(ticket, null, null, null, null);

        assertThrows(RequestDeniedException.class, () -> client.token(metadata.tokenEndpoint, req, needInfo -> {
            throw new UmaException("Unable to negotiation a token");
        }));
    }

    @Test
    void testSimpleTokenInvalidScope() {
        final URI asUri = URI.create(config.get("as_uri"));
        final UmaClient client = new UmaClient();
        final Metadata metadata = client.metadata(asUri);
        final String ticket = "ticket-invalid-scope";
        final TokenRequest req = new TokenRequest(ticket, null, null, null, Arrays.asList("invalid-scope"));

        assertThrows(InvalidScopeException.class, () -> client.token(metadata.tokenEndpoint, req, needInfo -> {
            throw new UmaException("Unable to negotiation a token");
        }));
    }

    @Test
    void testSimpleTokenNegotiation() {
        final URI asUri = URI.create(config.get("as_uri"));
        final UmaClient client = new UmaClient();
        final Metadata metadata = client.metadata(asUri);
        final String ticket = "ticket-12345";
        final TokenRequest req = new TokenRequest(ticket, null, null, null, null);

        final TokenResponse res = client.token(metadata.tokenEndpoint, req, needInfo -> {
            throw new UmaException("Unable to negotiate a simple token");
        });

        assertEquals("token-12345", res.accessToken);
        assertEquals("Bearer", res.tokenType);
    }

    @Test
    void testTokenNegotiationMissingResponseTicket() {
        final URI asUri = URI.create(config.get("as_uri"));
        final UmaClient client = new UmaClient();
        final Metadata metadata = client.metadata(asUri);
        final String ticket = "ticket-need-info-no-response-ticket";
        final TokenRequest req = new TokenRequest(ticket, null, null, null, null);

        assertThrows(RequestDeniedException.class, () -> client.token(metadata.tokenEndpoint, req, needInfo -> {
            throw new UmaException("Unable to negotiate a simple token");
        }));
    }

    @Test
    void testTokenNegotiationNullResponse() {
        final URI asUri = URI.create(config.get("as_uri"));
        final UmaClient client = new UmaClient();
        final Metadata metadata = client.metadata(asUri);
        final String ticket = "ticket-need-info-with-ticket";
        final TokenRequest req = new TokenRequest(ticket, null, null, null, null);

        assertThrows(RequestDeniedException.class, () -> client.token(metadata.tokenEndpoint, req, needInfo -> null));
    }

    @Test
    void testTokenNegotiationOidcMapper() {
        final URI asUri = URI.create(config.get("as_uri"));
        final UmaClient client = new UmaClient();
        final Metadata metadata = client.metadata(asUri);
        final String idToken = "oidc-id-token";

        final String ticket = "ticket-need-info-oidc-requirement";
        final TokenRequest req = new TokenRequest(ticket, null, null, null, null);

        final TokenResponse token = client.token(metadata.tokenEndpoint, req, needInfo ->
                ClaimToken.of(idToken, ID_TOKEN_CLAIM_TOKEN_FORMAT));

        assertEquals("token-from-id-token", token.accessToken);
        assertEquals("Bearer", token.tokenType);
    }

    @Test
    void testTokenNegotiationRecursionLimit() {
        final URI asUri = URI.create(config.get("as_uri"));
        final UmaClient client = new UmaClient(0);
        final Metadata metadata = client.metadata(asUri);
        final String idToken = "oidc-id-token";

        final String ticket = "ticket-need-info-oidc-requirement";
        final TokenRequest req = new TokenRequest(ticket, null, null, null, null);

        assertThrows(UmaException.class, () -> client.token(metadata.tokenEndpoint, req, needInfo ->
                ClaimToken.of(idToken, ID_TOKEN_CLAIM_TOKEN_FORMAT)));
    }


    // ---------------
    //   Async tests
    // ---------------
    @Test
    void testMetadataAsync() {
        final URI asUri = URI.create(config.get("as_uri"));
        final UmaClient client = new UmaClient();
        final Metadata metadata = client.metadataAsync(asUri).toCompletableFuture().join();
        checkMetadata(metadata);
    }

    @Test
    void testMetadataNotFoundAsync() {
        final URI asUri = URI.create(config.get("as_uri") + "/not-found");
        final UmaClient client = new UmaClient();
        final CompletionException err = assertThrows(CompletionException.class,
                client.metadataAsync(asUri).toCompletableFuture()::join);
        assertTrue(err.getCause() instanceof UmaException);
    }

    @Test
    void testMetadataMalformedAsync() {
        final URI asUri = URI.create(config.get("as_uri") + "/malformed");
        final UmaClient client = new UmaClient();
        final CompletionException err = assertThrows(CompletionException.class,
                client.metadataAsync(asUri).toCompletableFuture()::join);
        assertTrue(err.getCause() instanceof UmaException);
    }

    @Test
    void testSimpleTokenNegotiationInvalidTicketAsync() {
        final URI asUri = URI.create(config.get("as_uri"));
        final UmaClient client = new UmaClient();
        final String ticket = "ticket-invalid-grant";
        final TokenRequest req = new TokenRequest(ticket, null, null, null, null);

        final CompletionException err = assertThrows(CompletionException.class,
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
        final URI asUri = URI.create(config.get("as_uri"));
        final UmaClient client = new UmaClient();
        final String ticket = "ticket-request-denied";
        final TokenRequest req = new TokenRequest(ticket, null, null, null, null);

        final CompletionException err = assertThrows(CompletionException.class,
                client.metadataAsync(asUri)
                    .thenCompose(metadata ->
                        client.tokenAsync(metadata.tokenEndpoint, req, needInfo -> {
                            throw new UmaException("Unable to negotiation a token");
                        }))
                    .toCompletableFuture()::join);

        assertTrue(err.getCause() instanceof RequestDeniedException);
    }

    @ParameterizedTest
    @MethodSource
    void testSimpleTokenErrorAsync(final String ticket) {
        final URI asUri = URI.create(config.get("as_uri"));
        final UmaClient client = new UmaClient();
        final TokenRequest req = new TokenRequest(ticket, null, null, null, null);

        final CompletionException err =
                assertThrows(CompletionException.class, client.metadataAsync(asUri).thenCompose(
                        metadata -> client.tokenAsync(metadata.tokenEndpoint, req, needInfo -> {
                            throw new UmaException("Unable to negotiation a token");
                        })).toCompletableFuture()::join);

        assertTrue(err.getCause() instanceof UmaException);
    }

    private static Stream<Arguments> testSimpleTokenErrorAsync() {
        return Stream.of(
            Arguments.of("ticket-unknown-error"),
            Arguments.of("ticket-malformed-response"),
            Arguments.of("ticket-invalid-response"));
    }

    @Test
    void testSimpleTokenInvalidScopeAsync() {
        final URI asUri = URI.create(config.get("as_uri"));
        final UmaClient client = new UmaClient();
        final String ticket = "ticket-invalid-scope";
        final TokenRequest req = new TokenRequest(ticket, null, null, null, Arrays.asList("invalid-scope"));

        final CompletionException err = assertThrows(CompletionException.class,
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
        final URI asUri = URI.create(config.get("as_uri"));
        final UmaClient client = new UmaClient();
        final String ticket = "ticket-12345";
        final TokenRequest req = new TokenRequest(ticket, null, null, null, null);

        final TokenResponse res = client.metadataAsync(asUri).thenCompose(metadata ->
                client.tokenAsync(metadata.tokenEndpoint, req, needInfo -> {
                    throw new UmaException("Unable to negotiate a simple token");
                })).toCompletableFuture().join();

        assertEquals("token-12345", res.accessToken);
        assertEquals("Bearer", res.tokenType);
    }

    @Test
    void testTokenNegotiationMissingResponseTicketAsync() {
        final URI asUri = URI.create(config.get("as_uri"));
        final UmaClient client = new UmaClient();
        final String ticket = "ticket-need-info-no-response-ticket";
        final TokenRequest req = new TokenRequest(ticket, null, null, null, null);

        final CompletionException err = assertThrows(CompletionException.class,
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
        final URI asUri = URI.create(config.get("as_uri"));
        final UmaClient client = new UmaClient();
        final String ticket = "ticket-need-info-with-ticket";
        final TokenRequest req = new TokenRequest(ticket, null, null, null, null);

        final CompletionException err = assertThrows(CompletionException.class,
                client.metadataAsync(asUri)
                    .thenCompose(metadata ->
                        client.tokenAsync(metadata.tokenEndpoint, req, needInfo ->
                            CompletableFuture.completedFuture(null)))
                    .toCompletableFuture()::join);

        assertTrue(err.getCause() instanceof RequestDeniedException);
    }

    @Test
    void testTokenNegotiationOidcMapperAsync() {
        final URI asUri = URI.create(config.get("as_uri"));
        final UmaClient client = new UmaClient();
        final String idToken = "oidc-id-token";
        final String ticket = "ticket-need-info-oidc-requirement";
        final TokenRequest req = new TokenRequest(ticket, null, null, null, null);

        final TokenResponse token = client.metadataAsync(asUri)
                .thenCompose(metadata ->
                    client.tokenAsync(metadata.tokenEndpoint, req, needInfo ->
                        CompletableFuture.completedFuture(ClaimToken.of(idToken, ID_TOKEN_CLAIM_TOKEN_FORMAT))))
                .toCompletableFuture().join();

        assertEquals("token-from-id-token", token.accessToken);
        assertEquals("Bearer", token.tokenType);
    }

    @Test
    void testTokenNegotiationRecursionLimitAsync() {
        final URI asUri = URI.create(config.get("as_uri"));
        final UmaClient client = new UmaClient(0);
        final String idToken = "oidc-id-token";
        final String ticket = "ticket-need-info-oidc-requirement";
        final TokenRequest req = new TokenRequest(ticket, null, null, null, null);

        final CompletionException err = assertThrows(CompletionException.class,
                client.metadataAsync(asUri)
                    .thenCompose(metadata ->
                        client.tokenAsync(metadata.tokenEndpoint, req, needInfo ->
                            CompletableFuture.completedFuture(ClaimToken.of(idToken, ID_TOKEN_CLAIM_TOKEN_FORMAT))))
                    .toCompletableFuture()::join);

        assertTrue(err.getCause() instanceof UmaException);
    }

    static void checkMetadata(final Metadata metadata) {
        final URI asUri = URI.create(config.get("as_uri"));
        final URI jwksEndpoint = URIBuilder.newBuilder(asUri).path("jwks").build();
        final URI tokenEndpoint = URIBuilder.newBuilder(asUri).path("token").build();

        assertEquals(Arrays.asList("ES256", "RS256"), metadata.dpopSigningAlgValuesSupported);
        assertEquals(Arrays.asList("urn:ietf:params:oauth:grant-type:uma-ticket"),
                metadata.grantTypesSupported);
        assertEquals(asUri, metadata.issuer);
        assertEquals(jwksEndpoint, metadata.jwksUri);
        assertEquals(tokenEndpoint, metadata.tokenEndpoint);
        assertEquals(Arrays.asList(
                    URI.create("https://www.w3.org/TR/vc-data-model/#json-ld"),
                    URI.create("http://openid.net/specs/openid-connect-core-1_0.html#IDToken")),
                metadata.umaProfilesSupported);
    }
}
