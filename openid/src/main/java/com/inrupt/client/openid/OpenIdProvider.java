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

import static java.nio.charset.StandardCharsets.UTF_8;

import com.inrupt.client.*;
import com.inrupt.client.spi.HttpService;
import com.inrupt.client.spi.JsonService;
import com.inrupt.client.spi.ServiceProvider;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * A class for interacting with an OpenID Provider.
 *
 * @see <a href="https://openid.net/specs/openid-connect-core-1_0.html">OpenID Connect 1.0</a>
 */
public class OpenIdProvider {

    private static final String CLIENT_ID = "client_id";
    private static final String REDIRECT_URI = "redirect_uri";
    private static final String EQUALS = "=";
    private static final String ETC = "&";

    private final URI issuer;
    private final HttpService httpClient;
    private final JsonService jsonService;

    /**
     * Create an OpenID Provider client.
     *
     * @param issuer the OpenID provider issuer
     */
    public OpenIdProvider(final URI issuer) {
        this(issuer, ServiceProvider.getHttpService());
    }

    /**
     * Create an OpenID Provider client.
     *
     * @param issuer the OpenID provider issuer
     * @param httpClient an HTTP client
     */
    public OpenIdProvider(final URI issuer, final HttpService httpClient) {
        this.issuer = Objects.requireNonNull(issuer);
        this.httpClient = Objects.requireNonNull(httpClient);
        this.jsonService = ServiceProvider.getJsonService();
    }

    /**
     * Fetch the OpenID metadata resource.
     *
     * @return the OpenID Provider's metadata resource
     */
    public Metadata metadata() {
        try {
            return metadataAsync().toCompletableFuture().join();
        } catch (final CompletionException ex) {
            throw new OpenIdException("Error fetching OpenID metadata resoruce", ex);
        }
    }

    /**
     * Fetch the OpenID metadata resource.
     *
     * @return the next stage of completion, containing the OpenID Provider's metadata resource
     */
    public CompletionStage<Metadata> metadataAsync() {
        // consider caching this response
        final var req = Request.newBuilder(getMetadataUrl()).header("Accept", "application/json").build();
        return httpClient.sendAsync(req, Response.BodyHandlers.ofInputStream())
            .thenApply(res -> {
                try {
                    final int httpStatus = res.statusCode();
                    if (httpStatus >= 200 && httpStatus < 300) {
                        return jsonService.fromJson(res.body(), Metadata.class);
                    }
                    throw new OpenIdException(
                        "Unexpected error while fetching the OpenID metadata resource.",
                        httpStatus);
                } catch (final IOException ex) {
                    throw new OpenIdException(
                        "Unexpected I/O exception while fetching the OpenID metadata resource.",
                        ex);
                }
            });
    }

    private URI getMetadataUrl() {
        return URIBuilder.newBuilder(issuer).path(".well-known/openid-configuration").build();
    }

    /**
     * Construct the OpenID authorization URI.
     *
     * @param request the authorization request
     * @return the URI for performing the authorization request
     */
    public URI authorize(final AuthorizationRequest request) {
        return authorizeAsync(request).toCompletableFuture().join();
    }

    /**
     * Construct the OpenID authorization URI asynchronously.
     *
     * @param request the authorization request
     * @return the next stage of completion, containing URI for performing the authorization request
     */
    public CompletionStage<URI> authorizeAsync(final AuthorizationRequest request) {
        return metadataAsync()
            .thenApply(metadata -> authorize(metadata.authorizationEndpoint, request));
    }

    private URI authorize(final URI authorizationEndpoint, final AuthorizationRequest request) {
        final var builder = URIBuilder.newBuilder(authorizationEndpoint)
            .queryParam(CLIENT_ID, request.getClientId())
            .queryParam(REDIRECT_URI, request.getRedirectUri().toString())
            .queryParam("response_type", request.getResponseType());

        if (request.getCodeChallenge() != null && request.getCodeChallengeMethod() != null) {
            builder.queryParam("code_challenge", request.getCodeChallenge());
            builder.queryParam("code_challenge_method", request.getCodeChallengeMethod());
        }

        return builder.build();
    }

    /**
     * Interact with the OpenID Provider's token endpoint.
     *
     * @param request the token request
     * @return the token response
     */
    public TokenResponse token(final TokenRequest request) {
        try {
            return tokenAsync(request).toCompletableFuture().join();
        } catch (final CompletionException ex) {
            throw new OpenIdException("Error fetching OpenID token", ex);
        }
    }

    /**
     * Interact asynchronously with the OpenID Provider's token endpoint.
     *
     * @param request the token request
     * @return the next stage of completion, containing the token response
     */
    public CompletionStage<TokenResponse> tokenAsync(final TokenRequest request) {
        return metadataAsync()
            .thenApply(metadata -> tokenRequest(metadata, request))
            .thenCompose(req -> httpClient.sendAsync(req, Response.BodyHandlers.ofInputStream()))
            .thenApply(res -> {
                try {
                    final int httpStatus = res.statusCode();
                    if (httpStatus >= 200 && httpStatus < 300) {
                        return jsonService.fromJson(res.body(), TokenResponse.class);
                    }
                    throw new OpenIdException(
                        "Unexpected error while interacting with the OpenID Provider's token endpoint.",
                        httpStatus);
                } catch (final IOException ex) {
                    throw new OpenIdException(
                        "Unexpected I/O exception while interacting with the OpenID Provider's token endpoint.",
                        ex);
                }
            });
    }

    private Request tokenRequest(final Metadata metadata, final TokenRequest request) {
        final var data = new HashMap<String, String>();
        data.put("grant_type", request.getGrantType());
        data.put("code", request.getCode());
        data.put("code_verifier", request.getCodeVerifier());
        data.put(REDIRECT_URI, request.getRedirectUri().toString());

        final Optional<String> authHeader;
        if (request.getClientSecret() != null) {
            if ("client_secret_basic".equals(request.getAuthMethod())) {
                authHeader = getBasicAuthHeader(request.getClientId(), request.getClientSecret());
            } else {
                if ("client_secret_post".equals(request.getAuthMethod())) {
                    data.put(CLIENT_ID, request.getClientId());
                    data.put("client_secret", request.getClientSecret());
                }
                authHeader = Optional.empty();
            }
        } else {
            data.put(CLIENT_ID, request.getClientId());
            authHeader = Optional.empty();
        }

        final var req = Request.newBuilder(metadata.tokenEndpoint)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(ofFormData(data));

        // Add auth header, if relevant
        authHeader.ifPresent(header -> req.header("Authorization", header));

        return req.build();
    }

    /**
     * End the session with the OpenID Provider.
     *
     * @param request the end session request
     * @return a URI to which the app should be redirected, may be {@code null} if RP-initiated logout is not supported
     */
    public URI endSession(final EndSessionRequest request) {
        return endSessionAsync(request).toCompletableFuture().join();
    }

    /**
     * End the session asynchronously with the OpenID Provider.
     *
     * @param request the end session request
     * @return a URI to which the app should be redirected, may be {@code null} if RP-initiated logout is not supported
     */
    public CompletionStage<URI> endSessionAsync(final EndSessionRequest request) {
        return metadataAsync()
            .thenApply(metadata -> {
                if (metadata.endSessionEndpoint != null) {
                    return endSession(metadata.endSessionEndpoint, request);
                }
                return null;
            });
    }

    static Request.BodyPublisher ofFormData(final Map<String, String> data) {
        final var form = data.entrySet().stream().map(entry -> {
            final var name = URLEncoder.encode(entry.getKey(), UTF_8);
            final var value = URLEncoder.encode(entry.getValue() != null ? entry.getValue() : "", UTF_8);
            return String.join(EQUALS, name, value);
        }).collect(Collectors.joining(ETC));

        return Request.BodyPublishers.ofString(form);
    }

    private URI endSession(final URI endSessionEndpoint, final EndSessionRequest request) {
        return URIBuilder.newBuilder(endSessionEndpoint)
            .queryParam(CLIENT_ID, request.getClientId())
            .queryParam("post_logout_redirect_uri", request.getPostLogoutRedirectUri().toString())
            .queryParam("id_token_hint", request.getIdTokenHint())
            .queryParam("state", request.getState())
            .build();
    }

    static Optional<String> getBasicAuthHeader(final String clientId, final String clientSecret) {
        if (clientSecret != null) {
            final var raw = String.join(":", clientId, clientSecret);
            return Optional.of("Basic " + Base64.getEncoder().encodeToString(raw.getBytes(UTF_8)));
        }
        return Optional.empty();
    }
}
