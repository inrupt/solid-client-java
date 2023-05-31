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
package com.inrupt.client.accessgrant;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.inrupt.client.Client;
import com.inrupt.client.ClientCache;
import com.inrupt.client.ClientProvider;
import com.inrupt.client.Request;
import com.inrupt.client.Response;
import com.inrupt.client.auth.Session;
import com.inrupt.client.spi.JsonService;
import com.inrupt.client.spi.ServiceProvider;
import com.inrupt.client.util.URIBuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A client for interacting with and managing Access Grant Resources.
 *
 * <p>This client will require a suitable
 * {@link Session} object, typically an OpenID-based session:
 *
 * <pre>{@code
   URI SOLID_ACCESS_GRANT = URI.create("http://www.w3.org/ns/solid/vc#SolidAccessGrant");
   URI issuer = URI.create("https://issuer.example");
   Session openid = OpenIdSession.ofIdToken(idToken);

   AccessGrantClient client = new AccessGrantClient(issuer).session(session);

   URI resource = URI.create("https://storage.example/data/resource");
   URI purpose = URI.create("https://purpose.example/1");
   client.query(null, resource, purpose, "Read", AccessGrant.class)
       .thenApply(grants -> AccessGrantSession.ofAccessGrant(openid, grants.toArray(new AccessGrant[0])))
       .thenApply(session -> SolidClient.getClient().session(session))
       .thenAccept(cl -> {
            // Do something with the Access Grant-scoped client
       });
 * }</pre>
 */
public class AccessGrantClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessGrantClient.class);

    private static final String CONTEXT = "@context";
    private static final String VC_CONTEXT_URI = "https://www.w3.org/2018/credentials/v1";
    private static final String INRUPT_CONTEXT_URI = "https://schema.inrupt.com/credentials/v1.jsonld";
    private static final String VERIFIABLE_CREDENTIAL = "verifiableCredential";
    private static final String TYPE = "type";
    private static final String APPLICATION_JSON = "application/json";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CREDENTIAL_SUBJECT = "credentialSubject";
    private static final String IS_PROVIDED_TO = "isProvidedTo";
    private static final String IS_CONSENT_FOR_DATA_SUBJECT = "isConsentForDataSubject";
    private static final String FOR_PERSONAL_DATA = "forPersonalData";
    private static final String HAS_STATUS = "hasStatus";
    private static final String MODE = "mode";
    private static final String PROVIDED_CONSENT = "providedConsent";
    private static final String FOR_PURPOSE = "forPurpose";
    private static final String EXPIRATION_DATE = "expirationDate";
    private static final String CREDENTIAL = "credential";
    private static final URI ACCESS_GRANT = URI.create("http://www.w3.org/ns/solid/vc#SolidAccessGrant");
    private static final URI ACCESS_REQUEST = URI.create("http://www.w3.org/ns/solid/vc#SolidAccessRequest");
    private static final URI ACCESS_DENIAL = URI.create("http://www.w3.org/ns/solid/vc#SolidAccessDenial");
    private static final Set<String> ACCESS_GRANT_TYPES = getAccessGrantTypes();
    private static final Set<String> ACCESS_REQUEST_TYPES = getAccessRequestTypes();
    private static final Set<String> ACCESS_DENIAL_TYPES = getAccessDenialTypes();

    private final Client client;
    private final ClientCache<URI, Metadata> metadataCache;
    private final JsonService jsonService;
    private final AccessGrantConfiguration config;

    /**
     * Create an access grant client.
     *
     * @param issuer the issuer
     */
    public AccessGrantClient(final URI issuer) {
        this(ClientProvider.getClient(), issuer);
    }

    /**
     * Create an access grant client.
     *
     * @param client the client
     * @param issuer the issuer
     */
    public AccessGrantClient(final Client client, final URI issuer) {
        this(client, issuer, ServiceProvider.getCacheBuilder().build(100, Duration.ofMinutes(60)));
    }

    /**
     * Create an access grant client.
     *
     * @param client the client
     * @param issuer the issuer
     * @param metadataCache the metadata cache
     */
    public AccessGrantClient(final Client client, final URI issuer, final ClientCache<URI, Metadata> metadataCache) {
        this(client, metadataCache, new AccessGrantConfiguration(issuer));
    }

    /**
     * Create an access grant client.
     *
     * @param client the client
     * @param metadataCache the metadata cache
     * @param config the access grant configuration
     */
    // This ctor may be made public at a later point
    private AccessGrantClient(final Client client, final ClientCache<URI, Metadata> metadataCache,
            final AccessGrantConfiguration config) {
        this.client = Objects.requireNonNull(client, "client may not be null!");
        this.config = Objects.requireNonNull(config, "config may not be null!");
        this.metadataCache = Objects.requireNonNull(metadataCache, "metadataCache may not be null!");
        this.jsonService = ServiceProvider.getJsonService();
    }

    /**
     * Scope an access grant client to a particular session.
     *
     * @param session the session
     * @return the scoped access grant client
     */
    public AccessGrantClient session(final Session session) {
        Objects.requireNonNull(session, "Session may not be null!");
        return new AccessGrantClient(client.session(session), metadataCache, config);
    }

    /**
     * Issue an access request.
     *
     * @param agent the agent to whom the access request is made; i.e., the agent controlling access to the resources
     * @param resources the resources to which this credential applies
     * @param modes the access modes for this credential
     * @param purposes the purposes of this credential
     * @param expiration the expiration time of this credential
     * @return the next stage of completion containing the resulting access request
     */
    public CompletionStage<AccessRequest> requestAccess(final URI agent, final Set<URI> resources,
            final Set<String> modes, final Set<URI> purposes, final Instant expiration) {
        Objects.requireNonNull(resources, "Resources may not be null!");
        Objects.requireNonNull(modes, "Access modes may not be null!");
        return v1Metadata().thenCompose(metadata -> {
            final Map<String, Object> data = buildAccessRequestv1(agent, resources, modes, expiration, purposes);

            final Request req = Request.newBuilder(metadata.issueEndpoint)
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .POST(Request.BodyPublishers.ofByteArray(serialize(data))).build();

            return client.send(req, Response.BodyHandlers.ofInputStream())
                .thenApply(res -> {
                    try (final InputStream input = res.body()) {
                        final int status = res.statusCode();
                        if (isSuccess(status)) {
                            return processVerifiableCredential(input, ACCESS_REQUEST_TYPES, AccessRequest.class);
                        }
                        throw new AccessGrantException("Unable to issue Access Request: HTTP error " + status,
                                status);
                    } catch (final IOException ex) {
                        throw new AccessGrantException(
                                "Unexpected I/O exception while processing Access Request", ex);
                    }
                });
        });
    }

    /**
     * Issue an access grant based on an access request.
     *
     * @param request the access request
     * @return the next stage of completion containing the issued access grant
     */
    public CompletionStage<AccessGrant> grantAccess(final AccessRequest request) {
        Objects.requireNonNull(request, "Request may not be null!");
        return v1Metadata().thenCompose(metadata -> {
            final Map<String, Object> data = buildAccessGrantv1(request.getCreator(), request.getResources(),
                    request.getModes(), request.getExpiration(), request.getPurposes());
            final Request req = Request.newBuilder(metadata.issueEndpoint)
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .POST(Request.BodyPublishers.ofByteArray(serialize(data))).build();

            return client.send(req, Response.BodyHandlers.ofInputStream())
                .thenApply(res -> {
                    try (final InputStream input = res.body()) {
                        final int status = res.statusCode();
                        if (isSuccess(status)) {
                            return processVerifiableCredential(input, ACCESS_GRANT_TYPES, AccessGrant.class);
                        }
                        throw new AccessGrantException("Unable to issue Access Grant: HTTP error " + status,
                                status);
                    } catch (final IOException ex) {
                        throw new AccessGrantException(
                                "Unexpected I/O exception while processing Access Grant", ex);
                    }
                });
        });
    }

    /**
     * Issue an access denial receipt based on an access request.
     *
     * @param request the access request
     * @return the next stage of completion containing the issued access denial
     */
    public CompletionStage<AccessDenial> denyAccess(final AccessRequest request) {
        Objects.requireNonNull(request, "Request may not be null!");
        return v1Metadata().thenCompose(metadata -> {
            final Map<String, Object> data = buildAccessDenialv1(request.getCreator(), request.getResources(),
                    request.getModes(), request.getExpiration(), request.getPurposes());
            final Request req = Request.newBuilder(metadata.issueEndpoint)
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .POST(Request.BodyPublishers.ofByteArray(serialize(data))).build();

            return client.send(req, Response.BodyHandlers.ofInputStream())
                .thenApply(res -> {
                    try (final InputStream input = res.body()) {
                        final int status = res.statusCode();
                        if (isSuccess(status)) {
                            return processVerifiableCredential(input, ACCESS_DENIAL_TYPES, AccessDenial.class);
                        }
                        throw new AccessGrantException("Unable to issue Access Denial: HTTP error " + status,
                                status);
                    } catch (final IOException ex) {
                        throw new AccessGrantException(
                                "Unexpected I/O exception while processing Access Denial", ex);
                    }
                });
        });
    }

    /**
     * Issue an access grant or request.
     *
     * @param type the credential type
     * @param agent the receiving agent for this credential
     * @param resources the resources to which this credential applies
     * @param modes the access modes for this credential
     * @param purposes the purposes of this credential
     * @param expiration the expiration time of this credential
     * @return the next stage of completion containing the resulting credential
     * @deprecated as of Beta3, please use the {@link #requestAccess} or {@link #grantAccess} methods
     */
    @Deprecated
    public CompletionStage<AccessGrant> issue(final URI type, final URI agent, final Set<URI> resources,
            final Set<String> modes, final Set<String> purposes, final Instant expiration) {
        Objects.requireNonNull(type, "Access Grant type may not be null!");
        Objects.requireNonNull(resources, "Resources may not be null!");
        Objects.requireNonNull(modes, "Access modes may not be null!");
        final Set<URI> uriPurposes = new HashSet<>();
        for (final String p : purposes) {
            try {
                uriPurposes.add(URI.create(p));
            } catch (final IllegalArgumentException ex) {
                LOGGER.debug("Ignoring non-URI purpose: {}", ex.getMessage());
            }
        }
        return v1Metadata().thenCompose(metadata -> {
            final Map<String, Object> data;
            if (ACCESS_GRANT.equals(type)) {
                data = buildAccessGrantv1(agent, resources, modes, expiration, uriPurposes);
            } else if (ACCESS_REQUEST.equals(type)) {
                data = buildAccessRequestv1(agent, resources, modes, expiration, uriPurposes);
            } else {
                throw new AccessGrantException("Unsupported grant type: " + type);
            }

            final Request req = Request.newBuilder(metadata.issueEndpoint)
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .POST(Request.BodyPublishers.ofByteArray(serialize(data))).build();

            return client.send(req, Response.BodyHandlers.ofInputStream())
                .thenApply(res -> {
                    try (final InputStream input = res.body()) {
                        final int status = res.statusCode();
                        if (isSuccess(status)) {
                            return processVerifiableCredential(input, ACCESS_GRANT_TYPES, AccessGrant.class);
                        }
                        throw new AccessGrantException("Unable to issue Access Grant: HTTP error " + status,
                                status);
                    } catch (final IOException ex) {
                        throw new AccessGrantException(
                                "Unexpected I/O exception while processing Access Grant", ex);
                    }
                });
        });
    }

    /**
     * Verify an access grant or request.
     *
     * @param credential the credential to verify
     * @return the next stage of completion containing the verification result
     */
    public CompletionStage<AccessCredentialVerification> verify(final AccessCredential credential) {
        return v1Metadata().thenCompose(metadata -> {

            final Map<String, Object> presentation = new HashMap<>();

            try (final InputStream is = new ByteArrayInputStream(credential.serialize().getBytes(UTF_8))) {
                final Map<String, Object> data = jsonService.fromJson(is,
                        new HashMap<String, Object>(){}.getClass().getGenericSuperclass());
                Utils.getCredentialsFromPresentation(data, credential.getTypes()).stream().findFirst()
                    .ifPresent(c -> presentation.put(VERIFIABLE_CREDENTIAL, c));
            } catch (final IOException ex) {
                throw new AccessGrantException("Unable to serialize credential", ex);
            }

            final Request req = Request.newBuilder(metadata.verifyEndpoint)
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .POST(Request.BodyPublishers.ofByteArray(serialize(presentation))).build();

            return client.send(req, Response.BodyHandlers.ofInputStream())
                .thenApply(res -> {
                    try (final InputStream input = res.body()) {
                        final int status = res.statusCode();
                        if (isSuccess(status)) {
                            final VerificationResponseData data = jsonService.fromJson(input,
                                    VerificationResponseData.class);
                            return new AccessCredentialVerification(data.checks, data.warnings, data.errors);
                        }
                        throw new AccessGrantException("Unable to perform Access Grant verify: HTTP error " + status,
                                status);
                    } catch (final IOException ex) {
                        throw new AccessGrantException(
                                "Unexpected I/O exception while verifying Access Grant", ex);
                    }
                });
        });
    }

    /**
     * Perform an Access Grant query.
     *
     * @param <T> the AccessCredential type
     * @param agent the agent identifier, may be {@code null}
     * @param resource the resource identifier, may be {@code null}
     * @param purpose the access purpose, may be {@code null}
     * @param mode the access mode, may be {@code null}
     * @param clazz the AccessCredential type, either {@link AccessGrant} or {@link AccessRequest}
     * @return the next stage of completion, including the matched Access Grants
     */
    public <T extends AccessCredential> CompletionStage<List<T>> query(final URI agent, final URI resource,
            final URI purpose, final String mode, final Class<T> clazz) {
        Objects.requireNonNull(clazz, "The clazz parameter must not be null!");

        final URI type;
        final Set<String> supportedTypes;
        if (AccessGrant.class.isAssignableFrom(clazz)) {
            type = URI.create("SolidAccessGrant");
            supportedTypes = ACCESS_GRANT_TYPES;
        } else if (AccessRequest.class.isAssignableFrom(clazz)) {
            type = URI.create("SolidAccessRequest");
            supportedTypes = ACCESS_REQUEST_TYPES;
        } else if (AccessDenial.class.isAssignableFrom(clazz)) {
            type = URI.create("SolidAccessDenial");
            supportedTypes = ACCESS_DENIAL_TYPES;
        } else {
            throw new AccessGrantException("Unsupported type " + clazz + " in query request");
        }

        return v1Metadata().thenCompose(metadata -> {
            final List<CompletableFuture<List<T>>> futures = buildQuery(config.getIssuer(), type,
                    agent, resource, purpose, mode).stream()
                .map(data -> Request.newBuilder(metadata.queryEndpoint)
                        .header(CONTENT_TYPE, APPLICATION_JSON)
                        .POST(Request.BodyPublishers.ofByteArray(serialize(data))).build())
                .map(req -> client.send(req, Response.BodyHandlers.ofInputStream())
                        .thenApply(res -> {
                            try (final InputStream input = res.body()) {
                                final int status = res.statusCode();
                                if (isSuccess(status)) {
                                    return processQueryResponse(input, supportedTypes, clazz);
                                }
                                throw new AccessGrantException("Unable to perform Access Grant query: HTTP error " +
                                        status, status);
                            } catch (final IOException ex) {
                                throw new AccessGrantException(
                                        "Unexpected I/O exception while processing Access Grant query", ex);
                            }
                        }).toCompletableFuture())
                .collect(Collectors.toList());

            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(x -> futures.stream().map(CompletableFuture::join).flatMap(List::stream)
                        .collect(Collectors.toList()));
        });
    }

    /**
     * Perform an Access Grant query.
     *
     * <p>The {@code type} parameter must be an absolute URI. For Access Requests,
     * the URI is {@code http://www.w3.org/ns/solid/vc#SolidAccessRequest}. For Access Grants, the URI
     * is {@code http://www.w3.org/ns/solid/vc#SolidAccessGrant}. Other URIs may be defined in the future.
     *
     * @param type the Access Grant type
     * @param agent the agent identifier, may be {@code null}
     * @param resource the resource identifier, may be {@code null}
     * @param mode the access mode, may be {@code null}
     * @return the next stage of completion, including the matched Access Grants
     * @deprecated as of Beta3, please use the alternative {@link #query} method
     */
    @Deprecated
    public CompletionStage<List<AccessGrant>> query(final URI type, final URI agent, final URI resource,
            final String mode) {
        Objects.requireNonNull(type, "The type parameter must not be null!");
        return v1Metadata().thenCompose(metadata -> {
            final List<CompletableFuture<List<AccessGrant>>> futures = buildQuery(config.getIssuer(), type,
                    agent, resource, null, mode).stream()
                .map(data -> Request.newBuilder(metadata.queryEndpoint)
                        .header(CONTENT_TYPE, APPLICATION_JSON)
                        .POST(Request.BodyPublishers.ofByteArray(serialize(data))).build())
                .map(req -> client.send(req, Response.BodyHandlers.ofInputStream())
                        .thenApply(res -> {
                            try (final InputStream input = res.body()) {
                                final int status = res.statusCode();
                                if (isSuccess(status)) {
                                    return processQueryResponse(input, ACCESS_GRANT_TYPES, AccessGrant.class);
                                }
                                throw new AccessGrantException("Unable to perform Access Grant query: HTTP error " +
                                        status, status);
                            } catch (final IOException ex) {
                                throw new AccessGrantException(
                                        "Unexpected I/O exception while processing Access Grant query", ex);
                            }
                        }).toCompletableFuture())
                .collect(Collectors.toList());

            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(x -> futures.stream().map(CompletableFuture::join).flatMap(List::stream)
                        .collect(Collectors.toList()));
        });
    }

    /**
     * Revoke an access credential.
     *
     * @param credential the access credential
     * @return the next stage of completion
     */
    public CompletionStage<Void> revoke(final AccessCredential credential) {
        return v1Metadata().thenCompose(metadata -> {
            final Status status = credential.getStatus().orElseThrow(() ->
                    new AccessGrantException("Unable to revoke Access Credential: no credentialStatus data"));

            final Map<String, Object> credentialStatus = new HashMap<>();
            credentialStatus.put(TYPE, status.getType());
            credentialStatus.put("status", Integer.toString(status.getIndex()));

            final Map<String, Object> data = new HashMap<>();
            data.put("credentialId", credential.getIdentifier());
            data.put("credentialStatus", Arrays.asList(credentialStatus));

            final Request req = Request.newBuilder(metadata.statusEndpoint)
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .POST(Request.BodyPublishers.ofByteArray(serialize(data)))
                .build();

            return client.send(req, Response.BodyHandlers.discarding())
                .thenAccept(res -> {
                    final int code = res.statusCode();
                    if (!isSuccess(code)) {
                        throw new AccessGrantException("Unable to revoke Access Grant: " +
                                credential.getIdentifier(), code);
                    }
                });
        });
    }

    /**
     * Delete an access credential.
     *
     * @param credential the access credential
     * @return the next stage of completion
     */
    public CompletionStage<Void> delete(final AccessCredential credential) {
        final Request req = Request.newBuilder(credential.getIdentifier()).DELETE().build();
        return client.send(req, Response.BodyHandlers.discarding())
            .thenAccept(res -> {
                final int status = res.statusCode();
                if (!isSuccess(status)) {
                    throw new AccessGrantException("Unable to delete Access Credential: " + credential.getIdentifier(),
                            status);
                }
            });
    }

    /**
     * Fetch an access credential.
     *
     * @param identifier the access credential identifier
     * @return the next stage of completion, containing the access credential
     * @deprecated as of Beta3, please use the {@link #fetch(URI, Class)} method
     */
    @Deprecated
    public CompletionStage<AccessGrant> fetch(final URI identifier) {
        return fetch(identifier, AccessGrant.class);
    }

    /**
     * Fetch an access credential.
     *
     * @param <T> the credential type
     * @param identifier the access credential identifier
     * @param clazz the credential type, such as {@link AccessGrant} or {@link AccessRequest}
     * @return the next stage of completion, containing the access credential
     */
    public <T extends AccessCredential> CompletionStage<T> fetch(final URI identifier, final Class<T> clazz) {
        final Request req = Request.newBuilder(identifier)
            .header("Accept", "application/ld+json,application/json").build();
        return client.send(req, Response.BodyHandlers.ofInputStream())
            .thenApply(res -> {
                try (final InputStream input = res.body()) {
                    final int httpStatus = res.statusCode();
                    if (isSuccess(httpStatus)) {
                        if (AccessGrant.class.equals(clazz)) {
                            return (T) processVerifiableCredential(input, ACCESS_GRANT_TYPES, clazz);
                        } else if (AccessRequest.class.equals(clazz)) {
                            return (T) processVerifiableCredential(input, ACCESS_REQUEST_TYPES, clazz);
                        } else if (AccessDenial.class.equals(clazz)) {
                            return (T) processVerifiableCredential(input, ACCESS_DENIAL_TYPES, clazz);
                        }
                        throw new AccessGrantException("Unable to fetch credential as " + clazz);
                    }
                    throw new AccessGrantException(
                            "Unable to fetch the Access Grant: HTTP Error " + httpStatus, httpStatus);
                } catch (final IOException ex) {
                    throw new AccessGrantException(
                            "Unexpected I/O exception while fetching the Access Grant metadata resource.", ex);
                }
            });
    }



    <T extends AccessCredential> T processVerifiableCredential(final InputStream input, final Set<String> validTypes,
            final Class<T> clazz) throws IOException {
        final Map<String, Object> data = jsonService.fromJson(input,
                new HashMap<String, Object>(){}.getClass().getGenericSuperclass());
        final Set<String> types = Utils.asSet(data.get(TYPE)).orElseThrow(() ->
                new AccessGrantException("Invalid Access Grant: no 'type' field"));
        types.retainAll(validTypes);
        if (!types.isEmpty()) {
            final Map<String, Object> presentation = new HashMap<>();
            presentation.put(CONTEXT, Arrays.asList(VC_CONTEXT_URI));
            presentation.put(TYPE, Arrays.asList("VerifiablePresentation"));
            presentation.put(VERIFIABLE_CREDENTIAL, Arrays.asList(data));
            if (AccessGrant.class.isAssignableFrom(clazz)) {
                return (T) AccessGrant.of(new String(serialize(presentation), UTF_8));
            } else if (AccessRequest.class.isAssignableFrom(clazz)) {
                return (T) AccessRequest.of(new String(serialize(presentation), UTF_8));
            } else if (AccessDenial.class.isAssignableFrom(clazz)) {
                return (T) AccessDenial.of(new String(serialize(presentation), UTF_8));
            }
        }
        throw new AccessGrantException("Invalid Access Grant: missing supported type");
    }

    <T extends AccessCredential> List<T> processQueryResponse(final InputStream input, final Set<String> validTypes,
            final Class<T> clazz) throws IOException {
        final Map<String, Object> data = jsonService.fromJson(input,
                new HashMap<String, Object>(){}.getClass().getGenericSuperclass());
        final List<T> grants = new ArrayList<>();
        for (final Object item : getCredentials(data)) {
            Utils.asMap(item).ifPresent(credential ->
                Utils.asSet(credential.get(TYPE)).ifPresent(types -> {
                    types.retainAll(validTypes);
                    if (!types.isEmpty()) {
                        final Map<String, Object> presentation = new HashMap<>();
                        presentation.put(CONTEXT, Arrays.asList(VC_CONTEXT_URI));
                        presentation.put(TYPE, Arrays.asList("VerifiablePresentation"));
                        presentation.put(VERIFIABLE_CREDENTIAL, Arrays.asList(credential));
                        if (AccessGrant.class.equals(clazz)) {
                            grants.add((T) AccessGrant.of(new String(serialize(presentation), UTF_8)));
                        } else if (AccessRequest.class.equals(clazz)) {
                            grants.add((T) AccessRequest.of(new String(serialize(presentation), UTF_8)));
                        } else if (AccessDenial.class.equals(clazz)) {
                            grants.add((T) AccessDenial.of(new String(serialize(presentation), UTF_8)));
                        }
                    }
                }));
        }

        return grants;
    }

    CompletionStage<Metadata> v1Metadata() {
        final URI uri = URIBuilder.newBuilder(config.getIssuer()).path(".well-known/vc-configuration").build();
        final Metadata cached = metadataCache.get(uri);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }

        final Request req = Request.newBuilder(uri).header("Accept", APPLICATION_JSON).build();
        return client.send(req, Response.BodyHandlers.ofInputStream())
            .thenApply(res -> {
                try (final InputStream input = res.body()) {
                    final int httpStatus = res.statusCode();
                    if (isSuccess(httpStatus)) {
                        final Map<String, Object> data = jsonService.fromJson(input,
                                new HashMap<String, Object>(){}.getClass().getGenericSuperclass());
                        return data;
                    }
                    throw new AccessGrantException(
                            "Unable to fetch the Access Grant metadata: HTTP Error " + httpStatus, httpStatus);
                } catch (final IOException ex) {
                    throw new AccessGrantException(
                            "Unexpected I/O exception while fetching the Access Grant metadata resource.", ex);
                }
            })
            .thenApply(metadata -> {
                final Metadata m = new Metadata();
                m.queryEndpoint = asUri(metadata.get("derivationService"));
                m.issueEndpoint = asUri(metadata.get("issuerService"));
                m.verifyEndpoint = asUri(metadata.get("verifierService"));
                m.statusEndpoint = asUri(metadata.get("statusService"));
                metadataCache.put(uri, m);
                return m;
            });
    }

    byte[] serialize(final Map<String, Object> data) {
        try (final ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            jsonService.toJson(data, output);
            return output.toByteArray();
        } catch (final IOException ex) {
            throw new UncheckedIOException("Unable to serialize data as JSON", ex);
        }
    }

    static Collection<Object> getCredentials(final Map<String, Object> data) {
        final Object credential = data.get(VERIFIABLE_CREDENTIAL);
        if (credential instanceof Collection) {
            return (Collection) credential;
        }
        return Collections.emptyList();
    }

    static List<Map<String, Object>> buildQuery(final URI issuer, final URI type, final URI agent, final URI resource,
            final URI purpose, final String mode) {
        final List<Map<String, Object>> queries = new ArrayList<>();
        buildQuery(queries, issuer, type, agent, resource, purpose, mode);
        return queries;
    }

    static void buildQuery(final List<Map<String, Object>> queries, final URI issuer, final URI type, final URI agent,
            final URI resource, final URI purpose, final String mode) {
        final Map<String, Object> credential = new HashMap<>();
        credential.put(CONTEXT, Arrays.asList(VC_CONTEXT_URI, INRUPT_CONTEXT_URI));
        credential.put("issuer", issuer);
        credential.put(TYPE, Arrays.asList(type));

        final Map<String, Object> consent = new HashMap<>();
        if (agent != null) {
            if (isAccessGrant(type) || isAccessDenial(type)) {
                consent.put(IS_PROVIDED_TO, agent);
            } else if (isAccessRequest(type)) {
                consent.put(IS_CONSENT_FOR_DATA_SUBJECT, agent);
            }
        }
        if (resource != null) {
            consent.put(FOR_PERSONAL_DATA, resource);
        }
        if (purpose != null) {
            consent.put(FOR_PURPOSE, purpose);
        }
        if (mode != null) {
            consent.put(MODE, mode);
        }

        final Map<String, Object> subject = new HashMap<>();
        if (!consent.isEmpty()) {
            if (isAccessGrant(type) || isAccessDenial(type)) {
                subject.put(PROVIDED_CONSENT, consent);
            } else if (isAccessRequest(type)) {
                subject.put("hasConsent", consent);
            }
            credential.put(CREDENTIAL_SUBJECT, subject);
        }

        final Map<String, Object> data = new HashMap<>();
        data.put(VERIFIABLE_CREDENTIAL, credential);

        queries.add(data);

        // Recurse
        final URI parent = getParent(resource);
        if (parent != null) {
            buildQuery(queries, issuer, type, agent, parent, purpose, mode);
        }
    }

    static URI getParent(final URI resource) {
        if (resource != null) {
            if (resource.getPath().isEmpty() || "/".equals(resource.getPath())) {
                // already at the root of the URL hierarchy
                return null;
            }
            final URI container = resource.resolve(".");
            if (!resource.equals(container)) {
                // a non-container resource has a parent container
                return container;
            } else {
                return container.resolve("..");
            }
        }
        return null;
    }

    static URI asUri(final Object value) {
        if (value instanceof String) {
            return URI.create((String) value);
        }
        return null;
    }

    static Map<String, Object> buildAccessDenialv1(final URI agent, final Set<URI> resources, final Set<String> modes,
            final Instant expiration, final Set<URI> purposes) {
        Objects.requireNonNull(agent, "Access denial agent may not be null!");
        final Map<String, Object> consent = new HashMap<>();
        consent.put(MODE, modes);
        consent.put(HAS_STATUS, "https://w3id.org/GConsent#ConsentStatusRefused");
        consent.put(FOR_PERSONAL_DATA, resources);
        consent.put(IS_PROVIDED_TO, agent);
        if (!purposes.isEmpty()) {
            consent.put(FOR_PURPOSE, purposes);
        }

        final Map<String, Object> subject = new HashMap<>();
        subject.put(PROVIDED_CONSENT, consent);

        final Map<String, Object> credential = new HashMap<>();
        credential.put(CONTEXT, Arrays.asList(VC_CONTEXT_URI, INRUPT_CONTEXT_URI));
        if (expiration != null) {
            credential.put(EXPIRATION_DATE, expiration.truncatedTo(ChronoUnit.SECONDS).toString());
        }
        credential.put(CREDENTIAL_SUBJECT, subject);

        final Map<String, Object> data = new HashMap<>();
        data.put(CREDENTIAL, credential);
        return data;
    }

    static Map<String, Object> buildAccessGrantv1(final URI agent, final Set<URI> resources, final Set<String> modes,
            final Instant expiration, final Set<URI> purposes) {
        Objects.requireNonNull(agent, "Access grant agent may not be null!");
        final Map<String, Object> consent = new HashMap<>();
        consent.put(MODE, modes);
        consent.put(HAS_STATUS, "https://w3id.org/GConsent#ConsentStatusExplicitlyGiven");
        consent.put(FOR_PERSONAL_DATA, resources);
        consent.put(IS_PROVIDED_TO, agent);
        if (!purposes.isEmpty()) {
            consent.put(FOR_PURPOSE, purposes);
        }

        final Map<String, Object> subject = new HashMap<>();
        subject.put(PROVIDED_CONSENT, consent);

        final Map<String, Object> credential = new HashMap<>();
        credential.put(CONTEXT, Arrays.asList(VC_CONTEXT_URI, INRUPT_CONTEXT_URI));
        if (expiration != null) {
            credential.put(EXPIRATION_DATE, expiration.truncatedTo(ChronoUnit.SECONDS).toString());
        }
        credential.put(CREDENTIAL_SUBJECT, subject);

        final Map<String, Object> data = new HashMap<>();
        data.put(CREDENTIAL, credential);
        return data;
    }

    static Map<String, Object> buildAccessRequestv1(final URI agent, final Set<URI> resources, final Set<String> modes,
            final Instant expiration, final Set<URI> purposes) {
        final Map<String, Object> consent = new HashMap<>();
        consent.put(HAS_STATUS, "https://w3id.org/GConsent#ConsentStatusRequested");
        consent.put(MODE, modes);
        consent.put(FOR_PERSONAL_DATA, resources);
        if (agent != null) {
            consent.put(IS_CONSENT_FOR_DATA_SUBJECT, agent);
        }
        if (!purposes.isEmpty()) {
            consent.put(FOR_PURPOSE, purposes);
        }

        final Map<String, Object> subject = new HashMap<>();
        subject.put("hasConsent", consent);

        final Map<String, Object> credential = new HashMap<>();
        credential.put(CONTEXT, Arrays.asList(VC_CONTEXT_URI, INRUPT_CONTEXT_URI));
        if (expiration != null) {
            credential.put(EXPIRATION_DATE, expiration.truncatedTo(ChronoUnit.SECONDS).toString());
        }
        credential.put(CREDENTIAL_SUBJECT, subject);

        final Map<String, Object> data = new HashMap<>();
        data.put(CREDENTIAL, credential);
        return data;
    }

    static boolean isSuccess(final int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }

    static Set<String> getAccessRequestTypes() {
        final Set<String> types = new HashSet<>();
        types.add("SolidAccessRequest");
        types.add(ACCESS_REQUEST.toString());
        return Collections.unmodifiableSet(types);
    }

    static Set<String> getAccessGrantTypes() {
        final Set<String> types = new HashSet<>();
        types.add("SolidAccessGrant");
        types.add(ACCESS_GRANT.toString());
        return Collections.unmodifiableSet(types);
    }

    static Set<String> getAccessDenialTypes() {
        final Set<String> types = new HashSet<>();
        types.add("SolidAccessDenial");
        types.add(ACCESS_DENIAL.toString());
        return Collections.unmodifiableSet(types);
    }

    static boolean isAccessGrant(final URI type) {
        return "SolidAccessGrant".equals(type.toString()) || ACCESS_GRANT.equals(type);
    }

    static boolean isAccessRequest(final URI type) {
        return "SolidAccessRequest".equals(type.toString()) || ACCESS_REQUEST.equals(type);
    }

    static boolean isAccessDenial(final URI type) {
        return "SolidAccessDenial".equals(type.toString()) || ACCESS_DENIAL.equals(type);
    }

    /**
     * A data object for verification responses.
     */
    static class VerificationResponseData {
        /**
         * The verification checks that were performed.
         */
        public List<String> checks;

        /**
         * The verification warnings that were discovered.
         */
        public List<String> warnings;

        /**
         * The verification errors that were discovered.
         */
        public List<String> errors;
    }
}
