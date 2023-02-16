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
import com.inrupt.client.ClientProvider;
import com.inrupt.client.Request;
import com.inrupt.client.Response;
import com.inrupt.client.auth.Session;
import com.inrupt.client.spi.JsonService;
import com.inrupt.client.spi.ServiceProvider;
import com.inrupt.client.util.URIBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.time.Instant;
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

/**
 * A client for interacting with Access Grant Resources.
 */
public class AccessGrantClient {

    private static final String CONTEXT = "@context";
    private static final String VC_CONTEXT_URI = "https://www.w3.org/2018/credentials/v1";
    private static final String INRUPT_CONTEXT_URI = "https://schema.inrupt.com/credentials/v1.jsonld";
    private static final String VERIFIABLE_CREDENTIAL = "verifiableCredential";
    private static final String TYPE = "type";
    private static final String APPLICATION_JSON = "application/json";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final URI ACCESS_GRANT = URI.create("http://www.w3.org/ns/solid/vc#SolidAccessGrant");
    private static final URI ACCESS_REQUEST = URI.create("http://www.w3.org/ns/solid/vc#SolidAccessRequest");
    private static final Set<String> ACCESS_GRANT_TYPES = getAccessGrantTypes();

    private final Client client;
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
     * @param issuer the issuer
     * @param client the client
     */
    public AccessGrantClient(final Client client, final URI issuer) {
        this(client, new AccessGrantConfiguration(issuer));
    }

    /**
     * Create an access grant client.
     *
     * @param client the client
     * @param config the access grant configuration
     */
    // This ctor may be made public at a later point
    private AccessGrantClient(final Client client, final AccessGrantConfiguration config) {
        this.client = Objects.requireNonNull(client);
        this.config = Objects.requireNonNull(config);
        this.jsonService = ServiceProvider.getJsonService();
    }

    /**
     * Scope an access grant client to a particular session.
     *
     * @param session the session
     * @return the scoped access grant client
     */
    public AccessGrantClient session(final Session session) {
        return new AccessGrantClient(client.session(session), config);
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
     */
    public CompletionStage<AccessGrant> issue(final URI type, final URI agent, final Set<URI> resources,
            final Set<String> modes, final Set<String> purposes, final Instant expiration) {
        return v1Metadata().thenCompose(metadata -> {
            final Map<String, Object> data;
            if (ACCESS_GRANT.equals(type)) {
                data = buildAccessGrantv1(agent, resources, modes, expiration, purposes);
            } else if (ACCESS_REQUEST.equals(type)) {
                data = buildAccessRequestv1(agent, resources, modes, expiration, purposes);
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
                            return processVerifiableCredential(input, ACCESS_GRANT_TYPES);
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
     * Perform an Access Grant query.
     *
     * @param type the Access Grant type
     * @param agent the agent identifier, may be {@code null}
     * @param resource the resource identifier, may be {@code null}
     * @param mode the access mode, may be {@code null}
     * @return the next stage of completion, including the found Access Grants
     */
    public CompletionStage<List<AccessGrant>> query(final URI type, final URI agent, final URI resource,
            final String mode) {
        Objects.requireNonNull(type, "The type parameter must not be null!");
        return v1Metadata().thenCompose(metadata -> {
            final List<CompletableFuture<List<AccessGrant>>> futures = buildQuery(config.getIssuer(), type,
                    agent, resource, mode).stream()
                .map(data -> Request.newBuilder(metadata.queryEndpoint)
                        .header(CONTENT_TYPE, APPLICATION_JSON)
                        .POST(Request.BodyPublishers.ofByteArray(serialize(data))).build())
                .map(req -> client.send(req, Response.BodyHandlers.ofInputStream())
                        .thenApply(res -> {
                            try (final InputStream input = res.body()) {
                                final int status = res.statusCode();
                                if (isSuccess(status)) {
                                    return processQueryResponse(input, ACCESS_GRANT_TYPES);
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
     * @param accessGrant the access grant
     * @return the next stage of completion
     */
    public CompletionStage<Void> revoke(final AccessGrant accessGrant) {
        return v1Metadata().thenCompose(metadata -> {
            final Status status = accessGrant.getStatus().orElseThrow(() ->
                    new AccessGrantException("Unable to revoke Access Grant: no credentialStatus data"));

            final Map<String, Object> credentialStatus = new HashMap<>();
            credentialStatus.put(TYPE, status.getType());
            credentialStatus.put("status", Integer.toString(status.getIndex()));

            final Map<String, Object> data = new HashMap<>();
            data.put("credentialId", status.getIdentifier());
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
                                accessGrant.getIdentifier(), code);
                    }
                });
        });
    }

    /**
     * Delete an access credential.
     *
     * @param accessGrant the access credential
     * @return the next stage of completion
     */
    public CompletionStage<Void> delete(final AccessGrant accessGrant) {
        final Request req = Request.newBuilder(accessGrant.getIdentifier()).DELETE().build();
        return client.send(req, Response.BodyHandlers.discarding())
            .thenAccept(res -> {
                final int status = res.statusCode();
                if (!isSuccess(status)) {
                    throw new AccessGrantException("Unable to delete Access Grant: " + accessGrant.getIdentifier(),
                            status);
                }
            });
    }

    /**
     * Fetch an access credential.
     *
     * @param identifier the access credential identifier
     * @return the next stage of completion, containing the access credential
     */
    public CompletionStage<AccessGrant> fetch(final URI identifier) {
        final Request req = Request.newBuilder(identifier)
            .header("Accept", "application/ld+json,application/json").build();
        return client.send(req, Response.BodyHandlers.ofInputStream())
            .thenApply(res -> {
                try (final InputStream input = res.body()) {
                    final int httpStatus = res.statusCode();
                    if (isSuccess(httpStatus)) {
                        return processVerifiableCredential(input, ACCESS_GRANT_TYPES);
                    }
                    throw new AccessGrantException(
                            "Unable to fetch the Access Grant: HTTP Error " + httpStatus, httpStatus);
                } catch (final IOException ex) {
                    throw new AccessGrantException(
                            "Unexpected I/O exception while fetching the Access Grant metadata resource.", ex);
                }
            });
    }

    AccessGrant processVerifiableCredential(final InputStream input, final Set<String> validTypes) throws IOException {
        final Map<String, Object> data = jsonService.fromJson(input,
                new HashMap<String, Object>(){}.getClass().getGenericSuperclass());
        final Set<String> types = AccessGrant.asSet(data.get(TYPE)).orElseThrow(() ->
                new AccessGrantException("Invalid Access Grant: no 'type' field"));
        types.retainAll(validTypes);
        if (!types.isEmpty()) {
            final Map<String, Object> presentation = new HashMap<>();
            presentation.put(CONTEXT, Arrays.asList(VC_CONTEXT_URI));
            presentation.put(TYPE, Arrays.asList("VerifiablePresentation"));
            presentation.put(VERIFIABLE_CREDENTIAL, Arrays.asList(data));
            return AccessGrant.ofAccessGrant(new String(serialize(presentation), UTF_8));
        } else {
            throw new AccessGrantException("Invalid Access Grant: missing SolidAccessGrant type");
        }
    }

    List<AccessGrant> processQueryResponse(final InputStream input, final Set<String> validTypes) throws IOException {
        final Map<String, Object> data = jsonService.fromJson(input,
                new HashMap<String, Object>(){}.getClass().getGenericSuperclass());
        final List<AccessGrant> grants = new ArrayList<>();
        if (data.get(VERIFIABLE_CREDENTIAL) instanceof Collection) {
            for (final Object item : (Collection) data.get(VERIFIABLE_CREDENTIAL)) {
                AccessGrant.asMap(item).ifPresent(credential ->
                    AccessGrant.asSet(credential.get(TYPE)).ifPresent(types -> {
                        types.retainAll(validTypes);
                        if (!types.isEmpty()) {
                            final Map<String, Object> presentation = new HashMap<>();
                            presentation.put(CONTEXT, Arrays.asList(VC_CONTEXT_URI));
                            presentation.put(TYPE, Arrays.asList("VerifiablePresentation"));
                            presentation.put(VERIFIABLE_CREDENTIAL, Arrays.asList(credential));
                            grants.add(AccessGrant.ofAccessGrant(new String(serialize(presentation), UTF_8)));
                        }
                    }));
            }
        }

        return grants;
    }

    CompletionStage<Metadata> v1Metadata() {
        final URI uri = URIBuilder.newBuilder(config.getIssuer()).path(".well-known/vc-configuration").build();
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

    static List<Map<String, Object>> buildQuery(final URI issuer, final URI type, final URI agent, final URI resource,
            final String mode) {
        final List<Map<String, Object>> queries = new ArrayList<>();
        buildQuery(queries, issuer, type, agent, resource, mode);
        return queries;
    }

    static void buildQuery(final List<Map<String, Object>> queries, final URI issuer, final URI type, final URI agent,
            final URI resource, final String mode) {
        final Map<String, Object> credential = new HashMap<>();
        credential.put(CONTEXT, Arrays.asList(VC_CONTEXT_URI, INRUPT_CONTEXT_URI));
        credential.put("issuer", issuer);
        credential.put(TYPE, Arrays.asList(type));

        final Map<String, Object> consent = new HashMap<>();
        if (agent != null) {
            consent.put("isProvidedToPerson", agent);
        }
        if (resource != null) {
            consent.put("forPersonalData", resource);
        }
        if (mode != null) {
            consent.put("mode", mode);
        }

        final Map<String, Object> subject = new HashMap<>();
        if (!consent.isEmpty()) {
            if (isAccessGrant(type)) {
                subject.put("providedConsent", consent);
            } else if (isAccessRequest(type)) {
                subject.put("hasConsent", consent);
            }
            credential.put("credentialSubject", subject);
        }

        final Map<String, Object> data = new HashMap<>();
        data.put(VERIFIABLE_CREDENTIAL, credential);

        queries.add(data);

        // Recurse
        final URI parent = getParent(resource);
        if (parent != null) {
            buildQuery(queries, issuer, type, agent, parent, mode);
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

    static Map<String, Object> buildAccessGrantv1(final URI agent, final Set<URI> resources, final Set<String> modes,
            final Instant expiration, final Set<String> purposes) {
        final Map<String, Object> consent = new HashMap<>();
        consent.put("mode", modes);
        consent.put("hasStatus", "https://w3id.org/GConsent#ConsentStatusExplicitlyGiven");
        consent.put("forPersonalData", resources);
        consent.put("isProvidedToPerson", agent);
        if (!purposes.isEmpty()) {
            consent.put("forPurpose", purposes);
        }

        final Map<String, Object> subject = new HashMap<>();
        subject.put("providedConsent", consent);

        final Map<String, Object> credential = new HashMap<>();
        credential.put(CONTEXT, Arrays.asList(VC_CONTEXT_URI, INRUPT_CONTEXT_URI));
        credential.put("expirationDate", expiration);
        credential.put("credentialSubjct", subject);

        final Map<String, Object> data = new HashMap<>();
        data.put("credential", credential);
        return data;
    }

    static Map<String, Object> buildAccessRequestv1(final URI agent, final Set<URI> resources, final Set<String> modes,
            final Instant expiration, final Set<String> purposes) {
        final Map<String, Object> consent = new HashMap<>();
        consent.put("mode", modes);
        consent.put("hasStatus", "https://w3id.org/GConsent#ConsentStatusRequested");
        consent.put("forPersonalData", resources);
        consent.put("isProvidedToPerson", agent);
        if (!purposes.isEmpty()) {
            consent.put("forPurpose", purposes);
        }

        final Map<String, Object> subject = new HashMap<>();
        subject.put("hasConsent", consent);

        final Map<String, Object> credential = new HashMap<>();
        credential.put(CONTEXT, Arrays.asList(VC_CONTEXT_URI, INRUPT_CONTEXT_URI));
        credential.put("expirationDate", expiration);
        credential.put("credentialSubjct", subject);

        final Map<String, Object> data = new HashMap<>();
        data.put("credential", credential);
        return data;
    }

    static boolean isSuccess(final int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }

    static Set<String> getAccessGrantTypes() {
        final Set<String> types = new HashSet<>();
        types.add("SolidAccessGrant");
        types.add("http://www.w3.org/ns/solid/vc#SolidAccessGrant");
        types.add("SolidAccessRequest");
        types.add("http://www.w3.org/ns/solid/vc#SolidAccessRequest");
        return Collections.unmodifiableSet(types);
    }

    static boolean isAccessGrant(final URI type) {
        return "SolidAccessGrant".equals(type.toString()) ||
            "http://www.w3.org/ns/solid/vc#SolidAccessGrant".equals(type.toString());
    }

    static boolean isAccessRequest(final URI type) {
        return "SolidAccessRequest".equals(type.toString()) ||
            "http://www.w3.org/ns/solid/vc#SolidAccessRequest".equals(type.toString());

    }
}
