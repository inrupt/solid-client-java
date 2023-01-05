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
package com.inrupt.client.openid;

import static org.junit.jupiter.api.Assertions.*;

import com.inrupt.client.Authenticator;
import com.inrupt.client.openid.TokenRequest.Builder;

import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class OpenIdProviderTest {

    private static OpenIdProvider openIdProvider;
    private static final OpenIdMockHttpService mockHttpService = new OpenIdMockHttpService();
    private static final Map<String, String> config = new HashMap<>();
    private static final Authenticator.DPoP dpop = Authenticator.DPoP.of();


    @BeforeAll
    static void setup() throws NoSuchAlgorithmException {
        config.put("openid_uri", mockHttpService.start());
        openIdProvider = new OpenIdProvider(URI.create(config.get("openid_uri")), dpop);
    }

    @AfterAll
    static void teardown() {
        mockHttpService.stop();
    }

    @Test
    void metadataAsyncTest() {
        assertEquals("http://example.test",
                openIdProvider.metadata().toCompletableFuture().join().issuer.toString());
        assertEquals("http://example.test/oauth/jwks",
                openIdProvider.metadata().toCompletableFuture().join().jwksUri.toString());
    }

    @Test
    void unknownMetadata() {
        final OpenIdProvider provider = new OpenIdProvider(URI.create(config.get("openid_uri") + "/not-found"), dpop);
        final CompletionException err = assertThrows(CompletionException.class,
                provider.metadata().toCompletableFuture()::join);
        assertTrue(err.getCause() instanceof OpenIdException);
    }

    @Test
    void authorizeAsyncTest() {
        final AuthorizationRequest authReq = AuthorizationRequest.newBuilder()
            .codeChallenge("myCodeChallenge")
            .codeChallengeMethod("method")
            .build(
                "myClientId",
                URI.create("myRedirectUri")
            );
        assertEquals(
            "http://example.test/auth?client_id=myClientId&redirect_uri=myRedirectUri&" +
            "response_type=code&code_challenge=myCodeChallenge&code_challenge_method=method",
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
    void tokenNoClientSecretTest() {
        final TokenRequest tokenReq = TokenRequest.newBuilder()
            .code("someCode")
            .codeVerifier("myCodeverifier")
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
            .redirectUri(URI.create("none"))
            .build("authorization_code", "none");

        assertAll("Not found",
            () -> {
                final CompletionStage<TokenResponse> completionStage = openIdProvider.token(tokenReq);
                final CompletableFuture<TokenResponse> completableFuture = completionStage.toCompletableFuture();
                final CompletionException exception = assertThrows(CompletionException.class,
                    () -> completableFuture.join()
                );
                assertTrue(exception.getCause() instanceof OpenIdException);
                final OpenIdException cause = (OpenIdException) exception.getCause();

                assertEquals(
                    "Unexpected error while interacting with the OpenID Provider's token endpoint.",
                    cause.getMessage());
                assertEquals(OptionalInt.of(404), cause.getStatus());
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
            "http://example.test/endSession?" +
            "client_id=myClientId&post_logout_redirect_uri=https://example.test/redirectUri&id_token_hint=&state=solid",
            uri.toString()
        );
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
