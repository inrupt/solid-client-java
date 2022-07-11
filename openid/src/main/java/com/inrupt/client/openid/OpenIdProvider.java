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

import com.inrupt.client.authentication.DPoP;
import com.inrupt.client.spi.JsonProcessor;
import com.inrupt.client.spi.ServiceLoadingException;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * A class for interacting with an OpenID Provider.
 *
 * @see <a href="https://openid.net/specs/openid-connect-core-1_0.html">OpenID Connect 1.0</a>
 */
public class OpenIdProvider {

    private final URI issuer;
    private final DPoP dpop;
    private final HttpClient httpClient;
    private final JsonProcessor processor;

    /**
     * Create an OpenID Provider client.
     *
     * @param issuer the OpenID provider issuer
     */
    public OpenIdProvider(final URI issuer) {
        this(issuer, new DPoP());
    }

    /**
     * Create an OpenID Provider client.
     *
     * @param issuer the OpenID provider issuer
     * @param dpop a DPoP proof generator
     */
    public OpenIdProvider(final URI issuer, final DPoP dpop) {
        this(issuer, dpop, HttpClient.newHttpClient());
    }

    /**
     * Create an OpenID Provider client.
     *
     * @param issuer the OpenID provider issuer
     * @param dpop a DPoP proof generator
     * @param httpClient an HTTP client
     */
    public OpenIdProvider(final URI issuer, final DPoP dpop, final HttpClient httpClient) {
        this.issuer = Objects.requireNonNull(issuer);
        this.dpop = Objects.requireNonNull(dpop);
        this.httpClient = Objects.requireNonNull(httpClient);
        this.processor = ServiceLoader.load(JsonProcessor.class).findFirst()
            .orElseThrow(() -> new ServiceLoadingException(
                        "Unable to load JSON processor. " +
                        "Please ensure that a JSON processor is available on the classpath"));
    }

    /**
     * Fetch the OpenID metadata resource.
     *
     * @return the OpenID Provider's metadata resource
     */
    public Metadata metadata() {
        // consider caching this response
        final var req = HttpRequest.newBuilder(getMetadataUrl()).header("Accept", "application/json").build();
        try {
            final var res = httpClient.send(req, HttpResponse.BodyHandlers.ofInputStream());
            return processor.fromJson(res.body(), Metadata.class);
        } catch (final InterruptedException | IOException ex) {
            throw new OpenIdException("Error fetching OpenID metadata resource", ex);
        }
    }

    /**
     * Fetch the OpenID metadata resource.
     *
     * @return the next stage of completion, containing the OpenID Provider's metadata resource
     */
    public CompletionStage<Metadata> metadataAsync() {
        // consider caching this response
        final var req = HttpRequest.newBuilder(getMetadataUrl()).header("Accept", "application/json").build();
        return httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofInputStream())
            .thenApply(res -> processor.fromJson(res.body(), Metadata.class));
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
        final var metadata = metadata();
        return authorize(metadata.authorizationEndpoint, request);
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
            .queryParam("client_id", request.getClientId())
            .queryParam("redirect_uri", request.getRedirectUri().toString())
            .queryParam("response_type", request.getResponseType());

        if (request.getCodeChallenge() != null && request.getCodeChallengeMethod() != null) {
            builder.queryParam("code_challenge", request.getCodeChallenge());
            builder.queryParam("code_challenge_method", request.getCodeChallengeMethod());
        }

        return builder.build();
    }

    /**
     * Retrieve a token fromt the OpenID Provider.
     *
     * @param request the token request
     * @return the token response
     */
    public TokenResponse getToken(final TokenRequest request) {
        final var metadata = metadata();
        final var req = getTokenRequest(metadata.tokenEndpoint, request);

        try {
            final var res = httpClient.send(req, HttpResponse.BodyHandlers.ofInputStream());
            return processor.fromJson(res.body(), TokenResponse.class);
        } catch (final InterruptedException | IOException ex) {
            throw new OpenIdException("Error fetching OpenID token", ex);
        }
    }

    /**
     * Retrieve a token fromt the OpenID Provider asynchronously.
     *
     * @param request the token request
     * @return the next stage of completion, containing the token response
     */
    public CompletionStage<TokenResponse> getTokenAsync(final TokenRequest request) {
        return metadataAsync()
            .thenApply(metadata -> getTokenRequest(metadata.tokenEndpoint, request))
            .thenCompose(req -> httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofInputStream()))
            .thenApply(res -> processor.fromJson(res.body(), TokenResponse.class));
    }

    private HttpRequest getTokenRequest(final URI tokenEndpoint, final TokenRequest request) {
        final var data = new HashMap<String, String>();
        data.put("grant_type", request.getGrantType());
        data.put("code", request.getCode());
        data.put("code_verifier", request.getCodeVerifier());
        data.put("redirect_uri", request.getRedirectUri().toString());

        final Optional<String> authHeader;
        if (request.getClientSecret() != null) {
            if ("client_secret_basic".equals(request.getAuthMethod())) {
                authHeader = getBasicAuthHeader(request.getClientId(), request.getClientSecret());
            } else {
                if ("client_secret_post".equals(request.getAuthMethod())) {
                    data.put("client_id", request.getClientId());
                    data.put("client_secret", request.getClientSecret());
                }
                authHeader = Optional.empty();
            }
        } else {
            data.put("client_id", request.getClientId());
            authHeader = Optional.empty();
        }


        final var req = HttpRequest.newBuilder(tokenEndpoint)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(ofFormData(data));

        // Add auth header, if relevant
        authHeader.ifPresent(header -> req.header("Authorization", header));

        return req.build();
    }

    static HttpRequest.BodyPublisher ofFormData(final Map<String, String> data) {
        final var form = data.entrySet().stream().map(entry -> {
            final var name = URLEncoder.encode(entry.getKey(), UTF_8);
            final var value = URLEncoder.encode(entry.getValue(), UTF_8);
            return String.join("=", name, value);
        }).collect(Collectors.joining("&"));

        return HttpRequest.BodyPublishers.ofString(form);
    }

    static Optional<String> getBasicAuthHeader(final String clientId, final String clientSecret) {
        if (clientSecret != null) {
            final var encoder = Base64.getEncoder();
            final var raw = String.join(":", clientId, clientSecret);
            return Optional.of("Basic " + Base64.getEncoder().encodeToString(raw.getBytes(UTF_8)));
        }
        return Optional.empty();
    }
}
