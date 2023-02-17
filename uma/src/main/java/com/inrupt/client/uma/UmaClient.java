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
package com.inrupt.client.uma;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.inrupt.client.*;
import com.inrupt.client.spi.HttpService;
import com.inrupt.client.spi.JsonService;
import com.inrupt.client.spi.ServiceProvider;
import com.inrupt.client.util.URIBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * An UMA client implmentation.
 */
public class UmaClient {

    /* HTTP */
    private static final String EQUALS = "=";
    private static final String ETC = "&";
    private static final String ACCEPT = "Accept";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String JSON = "application/json";
    private static final String X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";
    private static final int SUCCESS = 200;

    /* UMA parameter names */
    private static final String CLAIM_TOKEN = "claim_token";
    private static final String CLAIM_TOKEN_FORMAT = "claim_token_format";
    private static final String GRANT_TYPE = "grant_type";
    private static final String PCT = "pct";
    private static final String RPT = "rpt";
    private static final String SCOPE = "scope";
    private static final String TICKET = "ticket";

    /* UMA parameter values */
    private static final String UMA_TICKET = "urn:ietf:params:oauth:grant-type:uma-ticket";

    /* UMA Error Codes */
    private static final String INVALID_GRANT = "invalid_grant";
    private static final String INVALID_SCOPE = "invalid_scope";
    private static final String NEED_INFO = "need_info";
    private static final String REQUEST_DENIED = "request_denied";

    // TODO add metadata cache
    private final HttpService httpClient;
    private final JsonService jsonService;
    private final int maxIterations;

    /**
     * Create a new UMA client using a default-configured HTTP client.
     */
    public UmaClient() {
        this(5);
    }

    public UmaClient(final int maxIterations) {
        this(ServiceProvider.getHttpService(), maxIterations);
    }

    /**
     * Create an UMA client using an externally-configured HTTP client.
     *
     * @param httpClient the externally configured HTTP client
     * @param maxIterations the maximum number of claims gathering stages
     */
    public UmaClient(final HttpService httpClient, final int maxIterations) {
        this.httpClient = httpClient;
        this.maxIterations = maxIterations;
        this.jsonService = ServiceProvider.getJsonService();
    }

    /**
     * Fetch the UMA metadata resource.
     *
     * @param authorizationServer the authorization server URI
     * @return the next stage of completion, containing the authorization server discovery metadata
     */
    public CompletionStage<Metadata> metadata(final URI authorizationServer) {
        final Request req = Request.newBuilder(getMetadataUrl(authorizationServer)).header(ACCEPT, JSON).build();
        return httpClient.send(req, Response.BodyHandlers.ofInputStream())
            .thenApply(this::processMetadataResponse);
    }

    /**
     * Fetch the UMA token resource.
     *
     * @param tokenEndpoint the token endpoint
     * @param tokenRequest the token request data
     * @param claimMapper a mapping function for interactive claim gathering
     * @return the next stage of completion, containing the token response
     */
    public CompletionStage<TokenResponse> token(final URI tokenEndpoint, final TokenRequest tokenRequest,
            final Function<NeedInfo, CompletionStage<ClaimToken>> claimMapper) {
        return negotiateToken(Objects.requireNonNull(tokenEndpoint),
                Objects.requireNonNull(tokenRequest), Objects.requireNonNull(claimMapper), 1);
    }

    private CompletionStage<TokenResponse> negotiateToken(final URI tokenEndpoint, final TokenRequest tokenRequest,
            final Function<NeedInfo, CompletionStage<ClaimToken>> claimMapper, final int count) {

        if (count > maxIterations) {
            throw new UmaException("Claim gathering stages exceeded configured maximum of " + maxIterations);
        }

        final Request req = buildTokenRequest(tokenEndpoint, tokenRequest);
        return httpClient.send(req, Response.BodyHandlers.ofInputStream()).thenCompose(res -> {
            try {
                // Successful terminal state
                if (SUCCESS == res.statusCode()) {
                    return CompletableFuture
                            .completedFuture(jsonService.fromJson(res.body(), TokenResponse.class));
                }

                // Everything else is a 4xx response
                // Attempt to read the error response as JSON
                final ErrorResponse err = jsonService.fromJson(res.body(), ErrorResponse.class);

                if (err.error != null) {
                    return readErrorMessage(err, tokenEndpoint, tokenRequest.getScopes(), claimMapper, count);
                }

                throw new UmaException(
                        "Unexpected error response while performing token negotiation: "
                                + res.statusCode());

            } catch (final IOException ex) {
                throw new UmaException("Unexpected I/O Error while performing token negotiation",
                        ex);
            }
        });
    }

    private CompletionStage<TokenResponse> readErrorMessage(final ErrorResponse err,
            final URI tokenEndpoint, final List<String> scopes,
            final Function<NeedInfo, CompletionStage<ClaimToken>> claimMapper,
            final int count) {
        switch (err.error) {
            case REQUEST_DENIED:
                throw new RequestDeniedException(
                        "The client is not authorized for the requested permissions");

            case INVALID_GRANT:
                throw new InvalidGrantException("Invalid grant provided");

            case INVALID_SCOPE:
                throw new InvalidScopeException("Invalid scope provided");

            case NEED_INFO:
            default:
                // recursive claims gathering
                return NeedInfo
                    .ofErrorResponse(err)
                    .map(needInfo -> claimMapper
                            .apply(needInfo)
                            .thenApply(claimToken -> {
                                if (claimToken == null) {
                                    throw new RequestDeniedException(
                                            "The client is unable to negotiate an access token");
                                }
                                return new TokenRequest(needInfo.getTicket(), null, null, claimToken,
                                            scopes);
                            })
                    )
                    .orElseThrow(() -> new RequestDeniedException("Invalid need_info error response"))
                    .thenCompose(modifiedTokenRequest ->
                        negotiateToken(tokenEndpoint, modifiedTokenRequest, claimMapper, count + 1));
        }
    }


    private Request buildTokenRequest(final URI tokenEndpoint, final TokenRequest request) {
        final Map<String, String> data = new HashMap<>();
        data.put(GRANT_TYPE, UMA_TICKET);
        data.put(TICKET, request.getTicket());
        request.getPersistedClaimToken().ifPresent(pct -> data.put(PCT, pct));
        request.getRequestingPartyToken().ifPresent(rpt -> data.put(RPT, rpt));
        request.getClaimToken().ifPresent(claimToken -> {
            data.put(CLAIM_TOKEN, claimToken.getClaimToken());
            data.put(CLAIM_TOKEN_FORMAT, claimToken.getClaimTokenType().toString());
        });
        if (!request.getScopes().isEmpty()) {
            data.put(SCOPE, String.join(" ", request.getScopes()));
        }

        // TODO add dpop support, if available
        return Request.newBuilder(tokenEndpoint)
            .header(CONTENT_TYPE, X_WWW_FORM_URLENCODED)
            .POST(ofFormData(data))
            .build();
    }

    private static Request.BodyPublisher ofFormData(final Map<String, String> data) {
        final String form = data.entrySet().stream().map(entry -> {
            String name = "";
            String value = "";
            try {
                name = URLEncoder.encode(entry.getKey(), UTF_8.toString());
                value = URLEncoder.encode(entry.getValue(), UTF_8.toString());
            } catch (UnsupportedEncodingException e) {
                throw new UmaException(e.getMessage());
            }
            return String.join(EQUALS, name, value);
        }).collect(Collectors.joining(ETC));

        return Request.BodyPublishers.ofString(form);
    }


    private Metadata processMetadataResponse(final Response<InputStream> response) {
        if (response.statusCode() == SUCCESS) {
            try {
                return jsonService.fromJson(response.body(), Metadata.class);
            } catch (final IOException ex) {
                throw new UmaException("Error while processing UMA metadata response", ex);
            }
        }
        throw new UmaException("Unexpected response code during UMA discovery: " + response.statusCode());
    }

    private URI getMetadataUrl(final URI authorizationServer) {
        return URIBuilder.newBuilder(authorizationServer).path(".well-known/uma2-configuration").build();
    }
}
