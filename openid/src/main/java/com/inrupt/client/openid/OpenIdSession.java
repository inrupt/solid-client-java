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

import com.inrupt.client.Authenticator;
import com.inrupt.client.Client.Session;
import com.inrupt.client.Request;

import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

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
 * <p>This session implementation is for use with ID Tokens serialized as signed
 * JWTs. This abstraction can be used to make use of identity-based authorization
 * in Solid.
 *
 * <pre>{@code
 *   Client client = ClientProvider.getClient();
 *   Session session = client.session(OpenIdSession.ofIdToken(jwt));
 *   Response res = session.send(req, bodyHandler);
 * }</pre>
 *
 * <p>A developer can configure aspects of the ID Token validation.
 * All tokens require the presence of subject ({@code sub}) and issuer ({@code iss}) claims as well as
 * issued at ({@code iat}) and expiration ({@code exp}) claims. By default, signature verification
 * is not enabled, but it can be turned on via configuration, as can audience verification.
 *
 * <pre>{@code
 *   Client client = ClientProvider.getClient();
 *   OpenIdVerificationConfig config = new OpenIdVerificationConfig();
 *   config.setExpectedAudience("https://app.example/id");
 *   config.setPublicKeyLocation("https://issuer.example/jwks");
 *   config.setExpGracePeriodSecs(60);
 *   Session session = client.session(OpenIdSession.ofIdToken(jwt, config));
 *   Response res = session.send(req, bodyHandler);
 * }</pre>
 *
 * <p>An invalid token will throw an {@link OpenIdException} during session creation.
 */
public final class OpenIdSession implements Session {

    private final String jwt;
    private final String id;
    private final Instant expiration;
    private final OpenIdVerificationConfig config;

    private OpenIdSession(final String idToken, final OpenIdVerificationConfig config) {
        this.config = Objects.requireNonNull(config, "OpenID verification configuration may not be null!");
        this.jwt = Objects.requireNonNull(idToken, "ID Token may not be null!");

        final JwtClaims claims = parseIdToken(idToken, config);
        this.id = getSessionIdentifier(claims);
        this.expiration = getExpiration(claims);
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
        return new OpenIdSession(idToken, config);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Optional<Authenticator.AccessToken> fromCache(final Request request) {
        return Optional.empty();
    }

    @Override
    public CompletionStage<Authenticator.AccessToken> negotiate(final Authenticator authenticator,
            final Request request) {
        if (expiration.plusSeconds(config.getExpGracePeriodSecs()).isBefore(Instant.now())) {
            return authenticator.authenticateAsync();
        }
        return CompletableFuture.completedFuture(new Authenticator.AccessToken(jwt, "Bearer", expiration,
                    Arrays.asList("webid", "openid"), null));
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
