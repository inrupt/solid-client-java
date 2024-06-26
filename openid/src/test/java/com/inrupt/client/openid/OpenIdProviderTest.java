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
package com.inrupt.client.openid;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

import com.inrupt.client.auth.DPoP;
import com.inrupt.client.openid.TokenRequest.Builder;
import com.inrupt.client.util.URIBuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class OpenIdProviderTest {

    private static OpenIdProvider openIdProvider;
    private static final OpenIdMockHttpService mockHttpService = new OpenIdMockHttpService();
    private static final DPoP dpop = DPoP.of();

    private static URI issuer;

    @BeforeAll
    static void setup() throws NoSuchAlgorithmException {
        issuer = URI.create(mockHttpService.start());
        openIdProvider = new OpenIdProvider(issuer, dpop);
    }

    @AfterAll
    static void teardown() {
        mockHttpService.stop();
    }

    @Test
    void metadataAsyncTest() {
        assertEquals(issuer,
                openIdProvider.metadata().toCompletableFuture().join().issuer);
        assertEquals(URIBuilder.newBuilder(issuer).path("jwks").build(),
                openIdProvider.metadata().toCompletableFuture().join().jwksUri);
    }

    @Test
    void unknownMetadata() {
        final OpenIdProvider provider = new OpenIdProvider(URIBuilder.newBuilder(issuer).path("not-found").build(),
                dpop);
        final CompletionException err = assertThrows(CompletionException.class,
                provider.metadata().toCompletableFuture()::join);
        assertTrue(err.getCause() instanceof OpenIdException);
    }

    @Test
    void authorizeAsyncTest() {
        final AuthorizationRequest authReq = AuthorizationRequest.newBuilder()
            .scope("openid")
            .scope("webid")
            .codeChallenge("myCodeChallenge")
            .codeChallengeMethod("method")
            .build(
                "myClientId",
                URI.create("myRedirectUri")
            );
        assertEquals(
            issuer + "/auth?client_id=myClientId&redirect_uri=myRedirectUri&" +
            "response_type=code&scope=openid%20webid&code_challenge=myCodeChallenge&code_challenge_method=method",
            openIdProvider.authorize(authReq).toCompletableFuture().join().toString()
        );
    }

    @Test
    void authorizeAsyncStateNonceTest() {
        final String state = UUID.randomUUID().toString();
        final String nonce = UUID.randomUUID().toString();
        final AuthorizationRequest authReq = AuthorizationRequest.newBuilder()
            .scope("openid")
            .scope("webid")
            .state(state)
            .nonce(nonce)
            .build(
                "myClientId",
                URI.create("myRedirectUri")
            );
        assertEquals(
            issuer + "/auth?client_id=myClientId&redirect_uri=myRedirectUri&" +
            "response_type=code&scope=openid%20webid&state=" + state + "&nonce=" + nonce,
            openIdProvider.authorize(authReq).toCompletableFuture().join().toString()
        );
    }

    @Test
    void tokenRequestIllegalArgumentsTest() {
        final Builder builder = TokenRequest.newBuilder()
            .redirectUri(URI.create("https://app.example/callback"));
        assertThrows(
            IllegalArgumentException.class,
            () -> builder.build("authorization_code", "myClientId"));
        assertThrows(
            IllegalArgumentException.class,
            () -> builder.build("client_credentials", "myClientId"));
        assertThrows(
            NullPointerException.class,
            () -> builder.build(null,"myClientId"));
        assertThrows(
            NullPointerException.class,
            () -> builder.build("myGrantType", null));
    }

    @Test
    void tokenIssuerMismatch() {
        final TokenRequest tokenReq = TokenRequest.newBuilder()
            .code("someCode")
            .codeVerifier("myCodeverifier")
            .issuer(URI.create("https://not.an.issuer.test"))
            .redirectUri(URI.create("https://example.test/redirectUri"))
            .build(
                "authorization_code",
                "myClientId"
            );

        final CompletionException ex = assertThrows(CompletionException.class, openIdProvider.token(tokenReq)
            .toCompletableFuture()::join);
        assertTrue(ex.getCause() instanceof OpenIdException);
        final OpenIdException cause = (OpenIdException) ex.getCause();
        assertTrue(cause.getMessage().contains("Issuer mismatch"));
    }

    @Test
    void tokenIssuerMissing() {
        final TokenRequest tokenReq = TokenRequest.newBuilder()
            .code("someCode")
            .codeVerifier("myCodeverifier")
            .redirectUri(URI.create("https://example.test/redirectUri"))
            .build(
                "authorization_code",
                "myClientId"
            );

        final CompletionException ex = assertThrows(CompletionException.class, openIdProvider.token(tokenReq)
            .toCompletableFuture()::join);
        assertTrue(ex.getCause() instanceof OpenIdException);
        final OpenIdException cause = (OpenIdException) ex.getCause();
        assertTrue(cause.getMessage().contains("Issuer mismatch"));
    }

    @Test
    void tokenIssuerMatch() {
        final TokenRequest tokenReq = TokenRequest.newBuilder()
            .code("someCode")
            .codeVerifier("myCodeverifier")
            .issuer(issuer)
            .redirectUri(URI.create("https://example.test/redirectUri"))
            .build(
                "authorization_code",
                "myClientId"
            );
        final TokenResponse token = openIdProvider.token(tokenReq)
            .toCompletableFuture().join();
        assertEquals("123456", token.accessToken);
        assertNotNull(token.idToken);
        assertEquals("Bearer", token.tokenType);
    }

    @Test
    void tokenNoClientSecretTest() {
        final TokenRequest tokenReq = TokenRequest.newBuilder()
            .code("someCode")
            .codeVerifier("myCodeverifier")
            .issuer(issuer)
            .redirectUri(URI.create("https://example.test/redirectUri"))
            .build(
                "authorization_code",
                "myClientId"
            );
        final TokenResponse token = openIdProvider.token(tokenReq)
            .toCompletableFuture().join();
        assertEquals("123456", token.accessToken);
        assertNotNull(token.idToken);
        assertEquals("Bearer", token.tokenType);
    }

    @Test
    void tokenWithClientSecretBasicTest() {
        final TokenRequest tokenReq = TokenRequest.newBuilder()
            .code("someCode")
            .codeVerifier("myCodeverifier")
            .clientSecret("myClientSecret")
            .issuer(issuer)
            .authMethod("client_secret_basic")
            .redirectUri(URI.create("https://example.test/redirectUri"))
            .build(
                "authorization_code",
                "myClientId"
            );
        final TokenResponse token = openIdProvider.token(tokenReq)
            .toCompletableFuture().join();
        assertEquals("123456", token.accessToken);
        assertNotNull(token.idToken);
        assertEquals("Bearer", token.tokenType);
    }

    @Test
    void tokenWithClientSecretePostTest() {
        final TokenRequest tokenReq = TokenRequest.newBuilder()
            .code("someCode")
            .codeVerifier("myCodeverifier")
            .clientSecret("myClientSecret")
            .issuer(issuer)
            .authMethod("client_secret_post")
            .redirectUri(URI.create("https://example.test/redirectUri"))
            .build(
                "authorization_code",
                "myClientId"
            );
        final TokenResponse token = openIdProvider.token(tokenReq)
            .toCompletableFuture().join();
        assertEquals("123456", token.accessToken);
        assertNotNull(token.idToken);
        assertEquals("Bearer", token.tokenType);
    }

    @Test
    void tokenAsyncTest() {
        final TokenRequest tokenReq = TokenRequest.newBuilder()
            .code("someCode")
            .codeVerifier("myCodeverifier")
            .issuer(issuer)
            .redirectUri(URI.create("https://example.test/redirectUri"))
            .build("authorization_code", "myClientId");
        final TokenResponse token = openIdProvider.token(tokenReq).toCompletableFuture().join();
        assertEquals("123456", token.accessToken);
        assertNotNull(token.idToken);
        assertEquals("Bearer", token.tokenType);
    }

    @Test
    void tokenAsyncStatusCodesTest() {
        final TokenRequest tokenReq = TokenRequest.newBuilder()
            .code("none")
            .codeVerifier("none")
            .issuer(issuer)
            .redirectUri(URI.create("none"))
            .build("authorization_code", "none");

        assertAll("Bad Request",
            () -> {
                final CompletionStage<TokenResponse> completionStage = openIdProvider.token(tokenReq);
                final CompletableFuture<TokenResponse> completableFuture = completionStage.toCompletableFuture();
                final CompletionException exception = assertThrows(CompletionException.class,
                    () -> completableFuture.join()
                );
                assertTrue(exception.getCause() instanceof OpenIdException);
                final OpenIdException cause = (OpenIdException) exception.getCause();

                assertEquals(
                    "invalid_grant error while interacting with the OpenID Provider's token endpoint: " +
                    "'Invalid authorization code value'.",
                    cause.getMessage());
                assertEquals(OptionalInt.of(400), cause.getStatus());
            });
    }

    @Test
    void endSessionAsyncTest() {
        final EndSessionRequest endReq = EndSessionRequest.Builder.newBuilder()
            .postLogoutRedirectUri(URI.create("https://example.test/redirectUri"))
            .clientId("myClientId")
            .state("solid")
            .build();
        final URI uri = openIdProvider.endSession(endReq).toCompletableFuture().join();
        assertEquals(
            issuer + "/endSession?" +
            "client_id=myClientId&post_logout_redirect_uri=https://example.test/redirectUri&id_token_hint=&state=solid",
            uri.toString()
        );
    }

    @Test
    void tryParseErrorResponseUnexpectedJson() throws IOException {
        final String json = "{\"unexpected\":\"json\"}";
        try (final InputStream input = new ByteArrayInputStream(json.getBytes(UTF_8))) {
            final ErrorResponse response = openIdProvider.tryParseError(input);
            assertEquals("undefined", response.error);
            assertNull(response.errorDescription);
        }
    }

    @Test
    void tryParseErrorResponseNotJson() throws IOException {
        try (final InputStream input = new ByteArrayInputStream(UUID.randomUUID().toString().getBytes(UTF_8))) {
            final ErrorResponse response = openIdProvider.tryParseError(input);
            assertEquals("Unexpected", response.error);
            assertNotNull(response.errorDescription);
        }
    }

    @Test
    void tryParseErrorResponseJsonWithExtraProperties() throws IOException {
        final String json = "{\"error\":\"invalid_client\"," +
            "\"error_description\": \"Description value\", " +
            "\"other_field\":\"Field value\"}";
        try (final InputStream input = new ByteArrayInputStream(json.getBytes(UTF_8))) {
            final ErrorResponse response = openIdProvider.tryParseError(input);
            assertEquals("invalid_client", response.error);
            assertEquals("Description value", response.errorDescription);
        }
    }

    @Test
    void dpopAlgTest() {
        assertFalse(OpenIdProvider.getDpopAlg(null, Collections.singleton("ES256")).isPresent());
        assertFalse(OpenIdProvider.getDpopAlg(Arrays.asList("RS256"), Collections.singleton("ES256")).isPresent());
    }

    @Test
    void getBasicAuthHeader() {
        assertFalse(OpenIdProvider.getBasicAuthHeader("123456", null).isPresent());
    }
}
