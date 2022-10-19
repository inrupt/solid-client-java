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
package com.inrupt.client.openid;

import static org.junit.jupiter.api.Assertions.*;

import com.inrupt.client.authentication.DPoP;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class OpenIdProviderTest {

    private static final HttpClient client = HttpClient.newBuilder().version(Version.HTTP_1_1).build();
    private static OpenIdProvider openIdProvider;
    private static DPoP dpop;
    private static final OpenIdMockHttpService mockHttpService = new OpenIdMockHttpService();
    private static final Map<String, String> config = new HashMap<>();


    @BeforeAll
    static void setup() throws NoSuchAlgorithmException {
        config.putAll(mockHttpService.start());
        dpop = new DPoP();
        openIdProvider = new OpenIdProvider(URI.create(config.get("openid_uri")), dpop, client);
    }

    @AfterAll
    static void teardown() {
        mockHttpService.stop();
    }

    @Test
    void metadataTest() {
        assertEquals("http://example.test", openIdProvider.metadata().issuer.toString());
        assertEquals("http://example.test/oauth/jwks", openIdProvider.metadata().jwksUri.toString());
    }

    @Test
    void metadataAsyncTest() {
        assertEquals(
            "http://example.test",
            openIdProvider.metadataAsync().toCompletableFuture().join().issuer.toString()
        );
        assertEquals(
            "http://example.test/oauth/jwks",
            openIdProvider.metadataAsync().toCompletableFuture().join().jwksUri.toString()
        );
    }

    @Test
    void authorizeTest() {
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
            openIdProvider.authorizeAsync(authReq).toCompletableFuture().join().toString()
        );
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
            openIdProvider.authorizeAsync(authReq).toCompletableFuture().join().toString()
        );
    }

    @Test
    void tokenCodeNullTest() {
        final TokenRequest tokenReq = TokenRequest.newBuilder()
            .build(
                "myGrantType",
                "myClientId",
                URI.create("myRedirectUri")
            );
        final var err =
                assertThrows(CompletionException.class, () -> openIdProvider.token(tokenReq));
        assertTrue(err.getCause() instanceof NullPointerException);
    }

    @Test
    void tokenRequestIllegalArgumentsTest() {
        assertThrows(
            NullPointerException.class,
            () -> TokenRequest.newBuilder()
                .build(
                    null,
                    "myClientId",
                    URI.create("myRedirectUri")
                )
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> TokenRequest.newBuilder()
                .build(
                    "authorization_code",
                    "myClientId",
                    URI.create("myRedirectUri")
                ));
        assertThrows(
            IllegalArgumentException.class,
            () -> TokenRequest.newBuilder()
                .build(
                    "client_credentials",
                    "myClientId",
                    URI.create("myRedirectUri")
                    ));
        assertThrows(
            NullPointerException.class,
            () -> TokenRequest.newBuilder()
                .build(
                    "myGrantType",
                    null,
                    URI.create("myRedirectUri")
                ));
        assertThrows(
            NullPointerException.class,
            () -> TokenRequest.newBuilder()
                .build(
                    "myGrantType",
                    "myClientId",
                    null
                ));
    }

    @Test
    void tokenNoClientSecretTest() {
        final var tokenReq = TokenRequest.newBuilder()
            .code("someCode")
            .codeVerifier("myCodeverifier")
            .build(
                "authorization_code",
                "myClientId",
                URI.create("https://example.test/redirectUri")
            );
        final var token = openIdProvider.token(tokenReq);
        assertEquals("token-from-id-token", token.accessToken);
        assertEquals("123456", token.idToken);
        assertEquals("Bearer", token.tokenType);
    }

    @Test
    void tokenWithClientSecretBasicTest() {
        final var tokenReq = TokenRequest.newBuilder()
            .code("someCode")
            .codeVerifier("myCodeverifier")
            .clientSecret("myClientSecret")
            .authMethod("client_secret_basic")
            .build(
                "authorization_code",
                "myClientId",
                URI.create("https://example.test/redirectUri")
            );
        final var token = openIdProvider.token(tokenReq);
        assertEquals("token-from-id-token", token.accessToken);
        assertEquals("123456", token.idToken);
        assertEquals("Bearer", token.tokenType);
    }

    @Test
    void tokenWithClientSecretePostTest() {
        final var tokenReq = TokenRequest.newBuilder()
            .code("someCode")
            .codeVerifier("myCodeverifier")
            .clientSecret("myClientSecret")
            .authMethod("client_secret_post")
            .build(
                "authorization_code",
                "myClientId",
                URI.create("https://example.test/redirectUri")
            );
        final var token = openIdProvider.token(tokenReq);
        assertEquals("token-from-id-token", token.accessToken);
        assertEquals("123456", token.idToken);
        assertEquals("Bearer", token.tokenType);
    }

    @Test
    void tokenAsyncTest() {
        final var tokenReq = TokenRequest.newBuilder()
            .code("someCode")
            .codeVerifier("myCodeverifier")
            .build(
                "authorization_code",
                "myClientId",
                URI.create("https://example.test/redirectUri")
            );
        final var token = openIdProvider.tokenAsync(tokenReq).toCompletableFuture().join();
        assertEquals("token-from-id-token", token.accessToken);
        assertEquals("123456", token.idToken);
        assertEquals("Bearer", token.tokenType);

    }

    @Test
    void endSessionTest() {
        final EndSessionRequest endReq = EndSessionRequest.Builder.newBuilder()
            .postLogoutRedirectUri(URI.create("https://example.test/redirectUri"))
            .clientId("myClientId")
            .state("solid")
            .build();
        final var uri = openIdProvider.endSession(endReq);
        assertEquals(
            "http://example.test/endSession?" +
            "client_id=myClientId&post_logout_redirect_uri=https://example.test/redirectUri&id_token_hint=&state=solid",
            uri.toString()
        );
    }

    @Test
    void endSessionAsyncTest() {
        final EndSessionRequest endReq = EndSessionRequest.Builder.newBuilder()
            .postLogoutRedirectUri(URI.create("https://example.test/redirectUri"))
            .clientId("myClientId")
            .state("solid")
            .build();
        final var uri = openIdProvider.endSessionAsync(endReq).toCompletableFuture().join();
        assertEquals(
            "http://example.test/endSession?" +
            "client_id=myClientId&post_logout_redirect_uri=https://example.test/redirectUri&id_token_hint=&state=solid",
            uri.toString()
        );
    }
}
