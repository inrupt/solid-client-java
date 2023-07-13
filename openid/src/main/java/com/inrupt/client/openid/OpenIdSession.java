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

import com.inrupt.client.ClientCache;
import com.inrupt.client.Request;
import com.inrupt.client.auth.Authenticator;
import com.inrupt.client.auth.Credential;
import com.inrupt.client.auth.DPoP;
import com.inrupt.client.auth.Session;
import com.inrupt.client.spi.ServiceProvider;

import java.net.URI;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.apache.commons.codec.binary.Hex;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A session implementation for use with OpenID Connect ID Tokens.
 *
 */
public final class OpenIdSession implements Session {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenIdSession.class);

    public static final URI ID_TOKEN = URI.create("http://openid.net/specs/openid-connect-core-1_0.html#IDToken");

    private final String id;
    private final Set<String> schemes;
    private final Supplier<CompletionStage<Credential>> authenticator;
    private final AtomicReference<Credential> credential = new AtomicReference<>();
    private final ForkJoinPool executor = new ForkJoinPool(1);
    private final DPoP dpop;
    private final ClientCache<URI, Credential> requestCache;

    private OpenIdSession(final String id, final DPoP dpop,
            final Supplier<CompletionStage<Credential>> authenticator) {
        this.id = Objects.requireNonNull(id, "Session id may not be null!");
        this.authenticator = Objects.requireNonNull(authenticator, "OpenID authenticator may not be null!");
        this.dpop = Objects.requireNonNull(dpop);
        this.requestCache = ServiceProvider.getCacheBuilder().build(1000, Duration.ofMinutes(5));

        // Support case-insensitive lookups
        final Set<String> schemeNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        schemeNames.add("Bearer");
        schemeNames.add("DPoP");

        this.schemes = Collections.unmodifiableSet(schemeNames);
    }

    /**
     * Create a session from an ID token, using the default validation rules.
     *
     * @param idToken the ID Token
     * @return the session
     */
    public static Session ofIdToken(final String idToken) {
        return ofIdToken(idToken, new OpenIdConfig());
    }

    /**
     * Create a session from an ID token, using a specific validation configuration.
     *
     * @param idToken the ID Token
     * @param config the validation configuration
     * @return the session
     */
    public static Session ofIdToken(final String idToken, final OpenIdConfig config) {
        final DPoP dpop = DPoP.of(config.getProofKeyPairs());
        final JwtClaims claims = parseIdToken(idToken, config);
        final String id = getSessionIdentifier(claims);
        final String jkt = getProofThumbprint(claims);
        final Credential credential = new Credential(jkt == null ? "Bearer" : "DPoP",
                getIssuer(claims), idToken, getExpiration(claims), getPrincipal(claims), jkt);
        return new OpenIdSession(id, dpop, () -> CompletableFuture.completedFuture(credential));
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

        final String id = UUID.randomUUID().toString();
        final DPoP dpop = DPoP.of();
        final OpenIdProvider provider = new OpenIdProvider(issuer, dpop);
        final OpenIdConfig config = new OpenIdConfig();
        return new OpenIdSession(id, dpop, () -> provider.token(TokenRequest.newBuilder()
                .clientSecret(clientSecret)
                .authMethod(authMethod)
                .scopes(config.getScopes().toArray(new String[0]))
                .build("client_credentials", clientId))
            .thenApply(response -> {
                final JwtClaims claims = parseIdToken(response.idToken, config);
                return new Credential(response.tokenType, getIssuer(claims), response.idToken,
                        toInstant(response.expiresIn), getPrincipal(claims), getProofThumbprint(claims));
            }));
    }

    /**
     * Create a session using OAuth2 client credentials.
     *
     * @param provider an OpenID Provider instance
     * @param clientId the client id value
     * @param clientSecret the client secret value
     * @param authMethod the authentication mechanism (e.g. {@code client_secret_post} or {@code client_secret_basic})
     * @param config the ID token verification config
     * @return the session
     */
    public static Session ofClientCredentials(final OpenIdProvider provider,
            final String clientId, final String clientSecret, final String authMethod,
            final OpenIdConfig config) {
        final String id = UUID.randomUUID().toString();
        final DPoP dpop = DPoP.of(config.getProofKeyPairs());
        return new OpenIdSession(id, dpop, () -> provider.token(TokenRequest.newBuilder()
                .clientSecret(clientSecret)
                .authMethod(authMethod)
                .scopes(config.getScopes().toArray(new String[0]))
                .build("client_credentials", clientId))
            .thenApply(response -> {
                final JwtClaims claims = parseIdToken(response.idToken, config);
                return new Credential(response.tokenType, getIssuer(claims), response.idToken,
                        toInstant(response.expiresIn), getPrincipal(claims), getProofThumbprint(claims));
            }));
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Optional<URI> getPrincipal() {
        return getCredential(ID_TOKEN, null).flatMap(Credential::getPrincipal);
    }

    @Override
    public Set<String> supportedSchemes() {
        return schemes;
    }

    @Override
    public Optional<Credential> getCredential(final URI name, final URI uri) {
        if (ID_TOKEN.equals(name)) {
            final Credential cred = credential.get();
            if (!hasExpired(cred)) {
                return Optional.of(cred);
            }
            return Optional.ofNullable(executor.invoke(ForkJoinTask.adapt(this::synchronizedFetch)));
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> selectThumbprint(final Collection<String> algorithms) {
        for (final String alg : algorithms) {
            if (dpop.algorithms().contains(alg)) {
                return dpop.lookupThumbprint(alg);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> generateProof(final String jkt, final Request request) {
        return dpop.lookupAlgorithm(jkt).map(alg -> dpop.generateProof(alg, request.uri(), request.method()));
    }

    @Override
    public Optional<Credential> fromCache(final Request request) {
        if (request != null) {
            final Credential cachedToken = requestCache.get(cacheKey(request.uri()));
            if (!hasExpired(cachedToken)) {
                LOGGER.debug("Using cached token for request: {}", request.uri());
                return Optional.of(cachedToken);
            }
        }
        return Optional.empty();
    }

    @Override
    public void reset() {
        credential.set(null);
        requestCache.invalidateAll();
    }

    @Override
    public CompletionStage<Optional<Credential>> authenticate(final Authenticator auth,
            final Request request, final Set<String> algorithms) {
        return auth.authenticate(this, request, algorithms)
                .thenApply(cred-> {
                    if (cred!= null) {
                        LOGGER.debug("Setting cache entry for request: {}", request.uri());
                        requestCache.put(cacheKey(request.uri()), cred);
                    }
                    return Optional.ofNullable(cred);
                });
    }

    boolean hasExpired(final Credential credential) {
        if (credential != null) {
            return credential.getExpiration().isBefore(Instant.now());
        }
        return true;
    }

    private synchronized Credential synchronizedFetch() {
        // Check again inside the synchronized method
        final Credential cred = credential.get();
        if (!hasExpired(cred)) {
            return cred;
        }

        // Fetch the refreshed credentials
        final Credential refreshed = authenticator.get().toCompletableFuture().join();
        if (!hasExpired(refreshed)) {
            credential.set(refreshed);
            return refreshed;
        }
        return null;
    }

    static String getSessionIdentifier(final JwtClaims claims) {
        final String webid = claims.getClaimValueAsString("webid");
        if (webid != null) {
            return sha256(webid);
        }
        try {
            return sha256(String.join("|", claims.getIssuer(), claims.getSubject()));
        } catch (final MalformedClaimException ex) {
            // This exception will never occur because of the validation rules in parseIdToken
            throw new OpenIdException("Malformed ID Token: unable to extract issuer and subject", ex);
        }
    }

    static String sha256(final String value) {
        final MessageDigest md = DigestUtils.getDigest("SHA-256");
        return new String(Hex.encodeHex(md.digest(value.getBytes(UTF_8))));
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

    static String getProofThumbprint(final JwtClaims claims) {
        final Object cnf = claims.getClaimValue("cnf");
        if (cnf instanceof Map) {
            final Object jkt = ((Map) cnf).get("jkt");
            if (jkt instanceof String) {
                return (String) jkt;
            }
        }
        return null;
    }

    static Instant toInstant(final int expiresIn) {
        if (expiresIn == 0) {
            return Instant.MAX;
        }
        return Instant.now().plusSeconds(expiresIn);
    }

    static URI cacheKey(final URI uri) {
        if (uri.getFragment() != null) {
            return URI.create(uri.getScheme() + ":" + uri.getSchemeSpecificPart());
        }
        return uri;
    }

    static JwtClaims parseIdToken(final String idToken, final OpenIdConfig config) {
        try {
            final JwtConsumerBuilder builder = new JwtConsumerBuilder();

            // Required by OpenID Connect
            builder.setRequireExpirationTime();
            builder.setExpectedIssuers(true, (String[]) null);
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
