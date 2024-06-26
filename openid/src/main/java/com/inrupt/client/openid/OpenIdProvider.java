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

import com.inrupt.client.*;
import com.inrupt.client.auth.DPoP;
import com.inrupt.client.spi.HttpService;
import com.inrupt.client.spi.JsonService;
import com.inrupt.client.spi.ServiceProvider;
import com.inrupt.client.util.URIBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A class for interacting with an OpenID Provider.
 *
 * @see <a href="https://openid.net/specs/openid-connect-core-1_0.html">OpenID Connect 1.0</a>
 */
public class OpenIdProvider {

    // OAuth 2 and OpenID request parameters
    private static final String CLIENT_ID = "client_id";
    private static final String CODE_CHALLENGE = "code_challenge";
    private static final String CODE_CHALLENGE_METHOD = "code_challenge_method";
    private static final String NONCE = "nonce";
    private static final String REDIRECT_URI = "redirect_uri";
    private static final String RESPONSE_TYPE = "response_type";
    private static final String SCOPE = "scope";
    private static final String STATE = "state";

    private static final String EQUALS = "=";
    private static final String ETC = "&";

    private final URI issuer;
    private final HttpService httpClient;
    private final JsonService jsonService;
    private final ClientCache<URI, Metadata> metadataCache;
    private final DPoP dpop;

    /**
     * Create an OpenID Provider client.
     *
     * @param issuer the OpenID provider issuer
     * @param dpop the DPoP manager
     */
    public OpenIdProvider(final URI issuer, final DPoP dpop) {
        this(issuer, dpop, ServiceProvider.getHttpService());
    }

    /**
     * Create an OpenID Provider client.
     *
     * @param issuer the OpenID provider issuer
     * @param dpop the DPoP manager
     * @param httpClient an HTTP client
     */
    public OpenIdProvider(final URI issuer, final DPoP dpop, final HttpService httpClient) {
        this(issuer, dpop, httpClient, ServiceProvider.getCacheBuilder().build(100, Duration.ofMinutes(60)));
    }

    /**
     * Create an OpenID Provider client.
     *
     * @param issuer the OpenID provider issuer
     * @param dpop the DPoP manager
     * @param httpClient an HTTP client
     * @param metadataCache an OpenID Metadata cache
     */
    public OpenIdProvider(final URI issuer, final DPoP dpop, final HttpService httpClient,
            final ClientCache<URI, Metadata> metadataCache) {
        this.issuer = Objects.requireNonNull(issuer, "issuer may not be null!");
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient may not be null!");
        this.metadataCache = Objects.requireNonNull(metadataCache, "metadataCache may not be null!");
        this.jsonService = ServiceProvider.getJsonService();
        this.dpop = dpop;
    }

    /**
     * Fetch the OpenID metadata resource.
     *
     * @return the next stage of completion, containing the OpenID Provider's metadata resource
     */
    public CompletionStage<Metadata> metadata() {
        final URI uri = getMetadataUrl();
        final Metadata m = metadataCache.get(uri);
        if (m != null) {
            return CompletableFuture.completedFuture(m);
        }

        final Request req = Request.newBuilder(getMetadataUrl()).header("Accept", "application/json").build();
        return httpClient.send(req, Response.BodyHandlers.ofInputStream())
            .thenApply(res -> {
                try {
                    final int httpStatus = res.statusCode();
                    if (httpStatus >= 200 && httpStatus < 300) {
                        final Metadata discovery = jsonService.fromJson(res.body(), Metadata.class);
                        metadataCache.put(uri, discovery);
                        return discovery;
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
     * Construct the OpenID authorization URI asynchronously.
     *
     * @param request the authorization request
     * @return the next stage of completion, containing URI for performing the authorization request
     */
    public CompletionStage<URI> authorize(final AuthorizationRequest request) {
        return metadata()
            .thenApply(metadata -> authorize(metadata.authorizationEndpoint, request));
    }

    private URI authorize(final URI authorizationEndpoint, final AuthorizationRequest request) {
        final URIBuilder builder = URIBuilder.newBuilder(authorizationEndpoint)
            .queryParam(CLIENT_ID, request.getClientId())
            .queryParam(REDIRECT_URI, request.getRedirectUri().toString())
            .queryParam(RESPONSE_TYPE, request.getResponseType())
            .queryParam(SCOPE, request.getScope());

        if (request.getState() != null) {
            builder.queryParam(STATE, request.getState());
        }

        if (request.getNonce() != null) {
            builder.queryParam(NONCE, request.getNonce());
        }

        if (request.getCodeChallenge() != null && request.getCodeChallengeMethod() != null) {
            builder.queryParam(CODE_CHALLENGE, request.getCodeChallenge());
            builder.queryParam(CODE_CHALLENGE_METHOD, request.getCodeChallengeMethod());
        }

        return builder.build();
    }

    /**
     * Interact asynchronously with the OpenID Provider's token endpoint.
     *
     * @param request the token request
     * @return the next stage of completion, containing the token response
     */
    public CompletionStage<TokenResponse> token(final TokenRequest request) {
        return metadata()
            .thenApply(metadata -> tokenRequest(metadata, request))
            .thenCompose(req -> httpClient.send(req, Response.BodyHandlers.ofInputStream()))
            .thenApply(res -> {
                try (final InputStream input = res.body()) {
                    final int httpStatus = res.statusCode();
                    if (httpStatus >= 200 && httpStatus < 300) {
                        return jsonService.fromJson(input, TokenResponse.class);
                    }
                    final ErrorResponse error = tryParseError(input);
                    throw new OpenIdException(
                        error.error + " error while interacting with the OpenID Provider's token endpoint" +
                        (error.errorDescription != null ? ": '" + error.errorDescription + "'." : "."),
                        httpStatus);
                } catch (final IOException ex) {
                    throw new OpenIdException(
                        "Unexpected I/O exception while interacting with the OpenID Provider's token endpoint.",
                        ex);
                }
            });
    }

    ErrorResponse tryParseError(final InputStream input) {
        // try to parse the input as JSON. This may be empty or not even JSON
        try {
            final ErrorResponse error = jsonService.fromJson(input, ErrorResponse.class);
            if (error.error == null) {
                error.error = "undefined";
            }
            return error;
        } catch (final IOException ex) {
            final ErrorResponse error = new ErrorResponse();
            error.error = "Unexpected";
            error.errorDescription = ex.getMessage();
            return error;
        }
    }

    private Request tokenRequest(final Metadata metadata, final TokenRequest request) {
        if (!metadata.grantTypesSupported.contains(request.getGrantType())) {
            throw new OpenIdException("Grant type [" + request.getGrantType() + "] is not supported by this provider.");
        }

        final Map<String, String> data = new HashMap<>();
        data.put("grant_type", request.getGrantType());
        if (request.getCode() != null) {
            data.put("code", request.getCode());
        }
        if (request.getCodeVerifier() != null) {
            data.put("code_verifier", request.getCodeVerifier());
        }

        if (request.getRedirectUri() != null) {
            data.put(REDIRECT_URI, request.getRedirectUri().toString());
        }

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

        final Request.Builder req = Request.newBuilder(metadata.tokenEndpoint)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(ofFormData(data));

        // Add auth header, if relevant
        authHeader.ifPresent(header -> req.header("Authorization", header));

        // Add dpop header, if relevant
        getDpopAlg(metadata.dpopSigningAlgValuesSupported, dpop.algorithms()).ifPresent(alg ->
                req.header("DPoP", dpop.generateProof(alg, metadata.tokenEndpoint, "POST")));

        return req.build();
    }

    /**
     * End the session asynchronously with the OpenID Provider.
     *
     * @param request the end session request
     * @return a URI to which the app should be redirected, may be {@code null} if RP-initiated logout is not supported
     */
    public CompletionStage<URI> endSession(final EndSessionRequest request) {
        return metadata()
            .thenApply(metadata -> {
                if (metadata.endSessionEndpoint != null) {
                    return endSession(metadata.endSessionEndpoint, request);
                }
                return null;
            });
    }

    static Request.BodyPublisher ofFormData(final Map<String, String> data) {
        final String form = data.entrySet().stream().flatMap(entry -> {
            try {
                if (entry.getKey() != null && entry.getValue() != null) {
                    final String name = URLEncoder.encode(entry.getKey(), UTF_8.toString());
                    final String value = URLEncoder.encode(entry.getValue(), UTF_8.toString());
                    return Stream.of(String.join(EQUALS, name, value));
                }
                return Stream.empty();
            } catch (UnsupportedEncodingException e) {
                throw new OpenIdException("Error encoding form data", e);
            }
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
            final String raw = String.join(":", clientId, clientSecret);
            return Optional.of("Basic " + Base64.getEncoder().encodeToString(raw.getBytes(UTF_8)));
        }
        return Optional.empty();
    }

    static Optional<String> getDpopAlg(final List<String> serverSupport, final Set<String> clientSupport) {
        if (serverSupport != null) {
            for (final String alg : serverSupport) {
                if (clientSupport.contains(alg)) {
                    return Optional.of(alg);
                }
            }
        }
        return Optional.empty();
    }
}
