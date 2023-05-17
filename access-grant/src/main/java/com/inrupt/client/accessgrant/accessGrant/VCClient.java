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
package com.inrupt.client.accessgrant.accessGrant;

import static java.nio.charset.StandardCharsets.UTF_8;
import static com.inrupt.client.accessgrant.accessGrant.Utils.isSuccess;

import com.inrupt.client.Client;
import com.inrupt.client.ClientCache;
import com.inrupt.client.ClientProvider;
import com.inrupt.client.Request;
import com.inrupt.client.Response;
import com.inrupt.client.accessgrant.AccessGrantException;
import com.inrupt.client.accessgrant.Status;
import com.inrupt.client.auth.Session;
import com.inrupt.client.spi.JsonService;
import com.inrupt.client.spi.ServiceProvider;
import com.inrupt.client.util.URIBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.time.Duration;
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

class VCClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(VCClient.class);

    private static final String APPLICATION_JSON = "application/json";
    private static final String CONTENT_TYPE = "Content-Type";
    
    private JsonService jsonService;
    private final Client client;
    private final ClientCache<URI, Metadata> metadataCache;
    private final VCConfiguration config;

    /**
     * Create a VC client.
     *
     * @param client the client
     * @param metadataCache the metadata cache
     * @param config the VC configuration
     */
    VCClient(final Client client, final ClientCache<URI, Metadata> metadataCache,
            final VCConfiguration config) {
        this.client = Objects.requireNonNull(client, "client may not be null!");
        this.config = Objects.requireNonNull(config, "config may not be null!");
        this.metadataCache = Objects.requireNonNull(metadataCache, "metadataCache may not be null!");
        this.jsonService = ServiceProvider.getJsonService();
    }

    /**
     * Scope a VC client to a particular session.
     *
     * @param session the session
     * @return the scoped vc client
     */
    VCClient session(final Session session) {
        Objects.requireNonNull(session, "Session may not be null!");
        return new VCClient(client.session(session), metadataCache, config);
    }

    /**
     * Issue a Verifiable Credential
     *
     * @param unprovedVC the unsigned verifiable credential
     * @return the next stage of completion containing the resulting credential
     */
    CompletionStage<Response<VerifiableCredential>> issue(final VerifiableCredential unprovedVC) {
        return v1Discovery()
            .thenApply(metadata ->
                Request.newBuilder(metadata.issueEndpoint)
                    .header(CONTENT_TYPE, APPLICATION_JSON)
                    .POST(VCBodyPublishers.ofVerifiableCredential(unprovedVC)).build()
            )
            .thenCompose(request ->
                client.send(request, VCBodyHandlers.ofVerifiableCredential())
            );
    }

    CompletionStage<VerificationResponse> verify(final VerifiableCredential credential) {
        return v1Discovery()
            .thenApply(metadata ->
                Request.newBuilder(metadata.verifyEndpoint)
                    .header(CONTENT_TYPE, APPLICATION_JSON)
                    //switch to a Verifiablepresentation
                    .POST(VCBodyPublishers.ofVerifiableCredential(credential)).build()
            )
            .thenCompose(request -> {
                return client.send(request, Response.BodyHandlers.ofInputStream())
                    .thenApply(res -> {
                        try (final InputStream input = res.body()) {
                            final int status = res.statusCode();
                            if (isSuccess(status)) {
                                return jsonService.fromJson(input, VerificationResponse.class);
                            }
                            throw new VCException("Unable to perform VC verify: HTTP error " + status,
                                    status);
                        } catch (final IOException ex) {
                            throw new VCException(
                                    "Unexpected I/O exception while verifying VC", ex);
                        }
                    });
                }
            );
    }


    //TODO
    CompletionStage<List<AccessGrant>> query(final URI type, final URI agent, final URI resource,
            final String mode) {
        Objects.requireNonNull(type, "The type parameter must not be null!");
        return v1Discovery()
                .thenCompose(metadata -> {
                    final List<CompletableFuture<List<AccessGrant>>> futures =
                        buildQuery(config.getIssuer(), type, agent, resource, mode)
                        .stream()
                        .map(data -> Request.newBuilder(metadata.queryEndpoint)
                                .header(CONTENT_TYPE, APPLICATION_JSON)
                                .POST(Request.BodyPublishers.ofByteArray(serialize(data))).build())
                        .map(req -> client.send(req, Response.BodyHandlers.ofInputStream())
                                .thenApply(res -> {
                                    try (final InputStream input = res.body()) {
                                        final int status = res.statusCode();
                                        if (isSuccess(status)) {
                                            return processQueryResponse(input, Utils.getAccessGrantTypes());
                                        }
                                        throw new VCException("Unable to perform VC query: HTTP error " +
                                                status, status);
                                    } catch (final IOException ex) {
                                        throw new VCException(
                                                "Unexpected I/O exception while processing VC query", ex);
                                    }
                                }).toCompletableFuture())
                        .collect(Collectors.toList());

                    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                        .thenApply(x -> futures.stream().map(CompletableFuture::join).flatMap(List::stream)
                                .collect(Collectors.toList()));
                        });
    }

    private CompletionStage<Metadata> v1Discovery() {
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
                    throw new VCException(
                            "Unable to fetch the VC service metadata: HTTP Error " + httpStatus, httpStatus);
                } catch (final IOException ex) {
                    throw new VCException(
                            "Unexpected I/O exception while fetching the VC service metadata resource.", ex);
                }
            })
            .thenApply(metadata -> {
                final Metadata m = new Metadata();
                m.queryEndpoint = URI.create((String) metadata.get("derivationService"));
                m.issueEndpoint = URI.create((String) metadata.get("issuerService"));
                m.verifyEndpoint = URI.create((String) metadata.get("verifierService"));
                m.statusEndpoint = URI.create((String) metadata.get("statusService"));
                return m;
            });
    }

    //TODO
    static List<Map<String, Object>> buildQuery(final URI issuer, final URI type, final URI agent, final URI resource,
            final String mode) {
        final List<Map<String, Object>> queries = new ArrayList<>();
        buildQuery(queries, issuer, type, agent, resource, mode);
        return queries;
    }

    //TODO
    static void buildQuery(final List<Map<String, Object>> queries, final URI issuer, final URI type, final URI agent,
            final URI resource, final String mode) {
        final Map<String, Object> credential = new HashMap<>();
        credential.put("context", Arrays.asList(Utils.VC_CONTEXT_URI, Utils.INRUPT_CONTEXT_URI));
        credential.put("issuer", issuer);
        credential.put(Utils.TYPE, Arrays.asList(type));

        final Map<String, Object> consent = new HashMap<>();
        if (agent != null) {
            consent.put("isProvidedTo", agent);
        }
        if (resource != null) {
            consent.put("forPersonalData", resource);
        }
        if (mode != null) {
            consent.put("mode", mode);
        }

        final Map<String, Object> subject = new HashMap<>();
        if (!consent.isEmpty()) {
            if (Utils.isAccessGrant(type)) {
                subject.put("providedConsent", consent);
            } else if (Utils.isAccessRequest(type)) {
                subject.put("hasConsent", consent);
            }
            credential.put("credentialSubject", subject);
        }

        final Map<String, Object> data = new HashMap<>();
        data.put("verifiableCredential", credential);

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


    byte[] serialize(final Map<String, Object> data) {
        try (final ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            jsonService.toJson(data, output);
            return output.toByteArray();
        } catch (final IOException ex) {
            throw new UncheckedIOException("Unable to serialize data as JSON", ex);
        }
    }

    List<AccessGrant> processQueryResponse(final InputStream input, final Set<String> validTypes) throws IOException {
        final Map<String, Object> data = jsonService.fromJson(input,
                new HashMap<String, Object>(){}.getClass().getGenericSuperclass());
        final List<AccessGrant> grants = new ArrayList<>();
        if (data.get(Utils.VERIFIABLE_CREDENTIAL) instanceof Collection) {
            for (final Object item : (Collection) data.get(Utils.VERIFIABLE_CREDENTIAL)) {
                Utils.asMap(item).ifPresent(credential ->
                    Utils.asSet(credential.get(Utils.TYPE)).ifPresent(types -> {
                        types.retainAll(validTypes);
                        if (!types.isEmpty()) {
                            final Map<String, Object> presentation = new HashMap<>();
                            presentation.put("context", Arrays.asList(Utils.VC_CONTEXT_URI));
                            presentation.put(Utils.TYPE, Arrays.asList("VerifiablePresentation"));
                            presentation.put(Utils.VERIFIABLE_CREDENTIAL, Arrays.asList(credential));
                            grants.add(AccessGrant.ofAccessGrant(new String(serialize(presentation), UTF_8)));
                        }
                    }));
            }
        }

        return grants;
    }
}
