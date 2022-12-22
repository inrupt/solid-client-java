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

import com.inrupt.client.Request;
import com.inrupt.client.Session;

import java.net.URI;
import java.time.Instant;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.apache.commons.codec.digest.DigestUtils;
import org.jose4j.jwk.HttpsJwks;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.resolvers.HttpsJwksVerificationKeyResolver;
import org.jose4j.keys.resolvers.VerificationKeyResolver;

/**
 * A session implementation for use with OpenID Connect ID Tokens.
 *
 */
public final class OpenIdSession implements Session {

    public static final String ID_TOKEN = "http://openid.net/specs/openid-connect-core-1_0.html#IDToken";

    private final String id;
    private final OpenIdVerificationConfig config;
    private final Set<String> schemes;
    private final Supplier<CompletionStage<Session.Credential>> authenticator;
    private final AtomicReference<Session.Credential> credential = new AtomicReference();

    private OpenIdSession(final String id, final Supplier<CompletionStage<Session.Credential>> authenticator,
            final OpenIdVerificationConfig config) {
        this.id = Objects.requireNonNull(id, "Session id may not be null!");
        this.config = Objects.requireNonNull(config, "OpenID verification configuration may not be null!");
        this.authenticator = Objects.requireNonNull(authenticator, "OpenID authenticator may not be null!");

        // Support case-insensitive lookups
        final Set<String> schemeNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        schemeNames.add("Bearer");

        this.schemes = Collections.unmodifiableSet(schemeNames);
    }

    /**
     * Create a session from an ID token, using the default validation rules.
     *
     * @param idToken the ID Token
     * @return the session
     */
    public static Session ofIdToken(final String idToken) {
        return ofIdToken(idToken, new OpenIdVerificationConfig());
    }

    /**
     * Create a session from an ID token, using a specific validation configuration.
     *
     * @param idToken the ID Token
     * @param config the validation configuration
     * @return the session
     */
    public static Session ofIdToken(final String idToken, final OpenIdVerificationConfig config) {
        final JwtClaims claims = parseIdToken(idToken, config);
        final String id = getSessionIdentifier(claims);
        final Session.Credential credential = new Session.Credential("Bearer", getIssuer(claims), idToken,
                getExpiration(claims), getPrincipal(claims));

        return new OpenIdSession(id, () -> CompletableFuture.completedFuture(credential), config);
    }

    /**
     * Create a session using OAuth2 client credentials.
     *
     * @param issuer the OpenID Provider URL
     * @param clientId the client id value
     * @param clientSecret the client secret value
     * @param authMethod the authentication mechanism (e.g. {@code client_secret_post} or {@code client_secret_basic})
     * @return the session
     */
    public static Session ofClientCredentials(final URI issuer, final String clientId, final String clientSecret,
            final String authMethod) {
        return ofClientCredentials(new OpenIdProvider(issuer), clientId, clientSecret, authMethod,
                new OpenIdVerificationConfig());
    }

    /**
     * Create a session using OAuth2 client credentials.
     *
     * @param provider an OpenID Provider instance
     * @param clientId the client id value
     * @param clientSecret the client secret value
     * @param authMethod the authentication mechanism (e.g. {@code client_secret_post} or {@code client_secret_basic})
     * @param config the ID token verifification config
     * @param scopes an array of scope values
     * @return the session
     */
    public static Session ofClientCredentials(final OpenIdProvider provider,
            final String clientId, final String clientSecret, final String authMethod,
            final OpenIdVerificationConfig config, final String... scopes) {
        final String id = UUID.randomUUID().toString();
        return new OpenIdSession(id, () -> provider.tokenAsync(TokenRequest.newBuilder()
                .clientSecret(clientSecret)
                .authMethod(authMethod)
                .scopes(scopes)
                .build("client_credentials", clientId))
            .thenApply(response -> {
                final JwtClaims claims = parseIdToken(response.idToken, config);
                return new Session.Credential(response.tokenType, getIssuer(claims), response.idToken,
                        toInstant(response.expiresIn), getPrincipal(claims));
            }), config);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Optional<URI> getPrincipal() {
        return getCredential(ID_TOKEN).flatMap(Session.Credential::getPrincipal);
    }

    @Override
    public Set<String> supportedSchemes() {
        return schemes;
    }

    @Override
    public Optional<Session.Credential> getCredential(final String name) {
        if (ID_TOKEN.equals(name)) {
            final Session.Credential c = credential.get();
            if (c != null && !hasExpired(c)) {
                return Optional.of(c);
            }
            final Session.Credential freshCredential = authenticator.get().toCompletableFuture().join();
            if (freshCredential != null && !hasExpired(freshCredential)) {
                credential.set(freshCredential);
                return Optional.of(freshCredential);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Session.Credential> fromCache(final Request request) {
        final Session.Credential c = credential.get();
        if (c != null && !hasExpired(c)) {
            return Optional.of(c);
        }
        return Optional.empty();
    }

    @Override
    public CompletionStage<Optional<Session.Credential>> authenticate(final Request request) {
        return authenticator.get().thenApply(Optional::ofNullable);
    }

    boolean hasExpired(final Session.Credential c) {
        return c.getExpiration().plusSeconds(config.getExpGracePeriodSecs()).isBefore(Instant.now());
    }

    static String getSessionIdentifier(final JwtClaims claims) {
        final String webid = claims.getClaimValueAsString("webid");
        if (webid != null) {
            return DigestUtils.sha256Hex(webid);
        }
        try {
            return DigestUtils.sha256Hex(String.join("|", claims.getIssuer(), claims.getSubject()));
        } catch (final MalformedClaimException ex) {
            // This exception will never occur because of the validation rules in parseIdToken
            throw new OpenIdException("Malformed ID Token: unable to extract issuer and subject", ex);
        }
    }

    static Instant getExpiration(final JwtClaims claims) {
        try {
            return Instant.ofEpochSecond(claims.getExpirationTime().getValue());
        } catch (final MalformedClaimException ex) {
            // This exception will never occur because of the validation rules in parseIdToken
            throw new OpenIdException("Malformed ID Token: unable to extract expiration time", ex);
        }
    }

    static URI getIssuer(final JwtClaims claims) {
        try {
            return URI.create(claims.getIssuer());
        } catch (final MalformedClaimException ex) {
            // This exception will never occur because of the validation rules in parseIdToken
            throw new OpenIdException("Malformed ID Token: unable to extract expiration time", ex);
        }
    }

    static URI getPrincipal(final JwtClaims claims) {
        final String webid = claims.getClaimValueAsString("webid");
        if (webid != null) {
            return URI.create(webid);
        }
        return null;
    }

    static Instant toInstant(final int expiresIn) {
        if (expiresIn == 0) {
            return Instant.MAX;
        }
        return Instant.now().plusSeconds(expiresIn);
    }

    static JwtClaims parseIdToken(final String idToken, final OpenIdVerificationConfig config) {
        try {
            final JwtConsumerBuilder builder = new JwtConsumerBuilder();

            // Required by OpenID Connect
            builder.setRequireExpirationTime();
            builder.setExpectedIssuers(true, null);
            builder.setRequireSubject();
            builder.setRequireIssuedAt();

            // If a grace period is set, allow for some clock skew
            if (config.getExpGracePeriodSecs() > 0) {
                builder.setAllowedClockSkewInSeconds(config.getExpGracePeriodSecs());
            } else {
                builder.setEvaluationTime(NumericDate.fromSeconds(Instant.now().getEpochSecond()));
            }

            // If an expected audience is set, verify that we have the correct value
            if (config.getExpectedAudience() != null) {
                builder.setExpectedAudience(true, config.getExpectedAudience());
            } else {
                builder.setSkipDefaultAudienceValidation();
            }

            // If a JWKS location is set, perform signature validation
            if (config.getPublicKeyLocation() != null) {
                final HttpsJwks jwks = new HttpsJwks(config.getPublicKeyLocation().toString());
                final VerificationKeyResolver resolver = new HttpsJwksVerificationKeyResolver(jwks);
                builder.setVerificationKeyResolver(resolver);
            } else {
                builder.setSkipSignatureVerification();
            }

            final JwtConsumer consumer = builder.build();
            return consumer.processToClaims(idToken);
        } catch (final InvalidJwtException ex) {
            throw new OpenIdException("Unable to parse ID token", ex);
        }
    }
}
