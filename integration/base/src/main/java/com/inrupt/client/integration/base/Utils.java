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
package com.inrupt.client.integration.base;

import static com.inrupt.client.vocabulary.RDF.type;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.inrupt.client.Client;
import com.inrupt.client.Headers;
import com.inrupt.client.Request;
import com.inrupt.client.Response;
import com.inrupt.client.auth.Challenge;
import com.inrupt.client.auth.Credential;
import com.inrupt.client.auth.ReactiveAuthorization;
import com.inrupt.client.auth.Session;
import com.inrupt.client.okhttp.OkHttpService;
import com.inrupt.client.solid.*;
import com.inrupt.client.spi.HttpService;
import com.inrupt.client.spi.RDFFactory;
import com.inrupt.client.spi.ServiceProvider;
import com.inrupt.client.util.URIBuilder;
import com.inrupt.client.vocabulary.ACL;
import com.inrupt.client.vocabulary.ACP;
import com.inrupt.client.vocabulary.LDP;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.api.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.lang.JoseException;
import org.jose4j.lang.UncheckedJoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;

import okhttp3.OkHttpClient;

public final class Utils {

    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);
    public static final String ACCEPT = "Accept";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String IF_NONE_MATCH = "If-None-Match";
    public static final String TEXT_TURTLE = "text/turtle";
    public static final String SPARQL_UPDATE = "application/sparql-update";
    public static final String APPLICATION_JSON = "application/json";
    public static final String APPLICATION_FORM = "application/x-www-form-urlencoded";
    public static final String PLAIN_TEXT = "text/plain";
    public static final String APPLICATION_OCTET = "application/octet-stream";
    public static final String PATCH = "PATCH";
    public static final String WILDCARD = "*";
    public static final String BEARER = "Bearer";
    public static final String DPOP = "DPoP";
    public static final String UMA = "UMA";
    public static final String FOLDER_SEPARATOR = "/";

    public static final int SUCCESS = 200;
    public static final int CREATED = 201;
    public static final int NO_CONTENT = 204;
    public static final int UNAUTHORIZED = 401;
    public static final int NOT_FOUND = 404;
    public static final int NOT_ALLOWED = 405;
    public static final int CONFLICT = 409;
    public static final int PRECONDITION_FAILED = 412;
    public static final int ERROR = 500;

    public static final String UMA_DISCOVERY_ENDPOINT = "/.well-known/uma2-configuration";
    public static final String OPENID_DISCOVERY_ENDPOINT = "/.well-known/openid-configuration";
    public static final String VC_DISCOVERY_ENDPOINT = "/.well-known/vc-configuration";
    public static final String UMA_TOKEN_ENDPOINT = "uma/token";
    public static final String OAUTH_TOKEN_ENDPOINT = "oauth/oauth20/token";
    public static final String UMA_JWKS_ENDPOINT = "uma/jwks";
    public static final String OAUTH_JWKS_ENDPOINT = "oauth/jwks";

    private static final RDF rdf = RDFFactory.getInstance();
    private static final IRI PUBLIC_AGENT = rdf.createIRI("http://www.w3.org/ns/solid/acp#PublicAgent");

    public static boolean isSuccessful(final int status) {
        return Arrays.asList(SUCCESS, NO_CONTENT, CREATED).contains(status);
    }

    public static byte[] modifyBody(final byte[] originalBody, final String requestBody)
            throws IOException {
        try (final InputStream input = new ByteArrayInputStream(originalBody)) {
            final Model model = ModelFactory.createDefaultModel();
            RDFDataMgr.read(model, input, Lang.TURTLE);
            final UpdateRequest request = UpdateFactory.create(requestBody);
            UpdateAction.execute(request, model);

            try (final ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                RDFDataMgr.write(output, model, Lang.TURTLE);
                return output.toByteArray();
            }
        }
    }

    public static PublicJsonWebKey getDpopKey(final String resource) {
        try (final InputStream stream = Utils.class.getResourceAsStream(resource)) {
            final String jwks = IOUtils.toString(stream, UTF_8);
            return PublicJsonWebKey.Factory.newPublicJwk(jwks);
        } catch (final IOException ex) {
            throw new UncheckedIOException("Unable to read JWK", ex);
        } catch (final JoseException ex) {
            throw new UncheckedJoseException("Unable to generate DPoP token", ex);
        }
    }

    public static boolean isPrivateResource(final String uri) {
        return uri.contains(State.PRIVATE_RESOURCE_PATH);
    }

    public static boolean isPodRoot(final String url) {
        return "/".equals(url);
    }


    private static IRI asIRI(final URI uri) {
        return rdf.createIRI(uri.toString());
    }

    public static Set<Triple> publicAgentPolicyTriples(final URI acl) {
        final Set<Triple> triples = new HashSet<>();
        final IRI a = asIRI(type);

        // Matcher
        final IRI matcher = asIRI(URIBuilder.newBuilder(acl).fragment(UUID.randomUUID().toString()).build());
        triples.add(rdf.createTriple(matcher, a, asIRI(ACP.Matcher)));
        triples.add(rdf.createTriple(matcher, asIRI(URI.create("http://www.w3.org/ns/solid/acp#agent")), PUBLIC_AGENT));

        // Policy
        final IRI policy = asIRI(URIBuilder.newBuilder(acl).fragment(UUID.randomUUID().toString()).build());
        triples.add(rdf.createTriple(policy, a, asIRI(ACP.Policy)));
        triples.add(rdf.createTriple(policy, asIRI(ACP.allOf), matcher));
        triples.add(rdf.createTriple(policy, asIRI(ACP.allow), asIRI(ACL.Read)));
        triples.add(rdf.createTriple(policy, asIRI(ACP.allow), asIRI(ACL.Write)));
        triples.add(rdf.createTriple(policy, asIRI(ACP.allow), asIRI(ACL.Append)));

        // Access Control
        final IRI accessControl = asIRI(URIBuilder.newBuilder(acl).fragment(UUID.randomUUID().toString()).build());
        triples.add(rdf.createTriple(accessControl, a, asIRI(ACP.AccessControl)));
        triples.add(rdf.createTriple(accessControl, asIRI(ACP.apply), policy));

        // Access Control Resource
        final IRI subject = asIRI(acl);
        triples.add(rdf.createTriple(subject, a, asIRI(ACP.AccessControlResource)));
        triples.add(rdf.createTriple(subject, asIRI(ACP.accessControl), accessControl));
        triples.add(rdf.createTriple(subject, asIRI(ACP.memberAccessControl), accessControl));
        return triples;
    }

    public static void createPublicContainer(final SolidSyncClient authClient, final URI publicContainerURI) {
        if (!exists(authClient, publicContainerURI)) {
            try (SolidContainer newContainer = new SolidContainer(publicContainerURI)) {
                try (final SolidContainer container = authClient.create(newContainer)) {
                    container.getMetadata().getAcl().ifPresent(acl -> {
                        try (final SolidRDFSource acr = authClient.read(acl, SolidRDFSource.class)) {
                            Utils.publicAgentPolicyTriples(acl)
                                    .forEach(acr.getGraph()::add);
                            authClient.update(acr);
                        } catch (SolidClientException ignored) {
                            LOGGER.error("Integration tests - Problem reading and modifying the acl");
                        }
                    });
                } catch (SolidClientException ex) {
                    LOGGER.error(ex.getStatusCode() + " " + ex.getCause() + " " + ex.getMessage());
                }
            }
        }
    }

    public static boolean exists(final SolidSyncClient authClient, final URI containerURI) {
        final var headReq = Request.newBuilder(containerURI)
                .HEAD()
                .build();
        final var resCheckIfExists =
                authClient.send(headReq, Response.BodyHandlers.discarding());
        return Utils.isSuccessful(resCheckIfExists.statusCode());
    }

    public static void createContainer(final SolidSyncClient authClient, final URI containerURI) {
        if (!exists(authClient, containerURI)) {
            final var requestCreate = Request.newBuilder(containerURI)
                    .header(Utils.CONTENT_TYPE, Utils.TEXT_TURTLE)
                    .header("Link", Headers.Link.of(LDP.RDFSource, "type").toString())
                    .PUT(Request.BodyPublishers.noBody())
                    .build();
            final var resCreate =
                    authClient.send(requestCreate, Response.BodyHandlers.discarding());
            assertTrue(Utils.isSuccessful(resCreate.statusCode()));
        }
    }

    static void deleteContentsRecursively(final SolidSyncClient client, final URI url) {
        if (exists(client, url)) {
            deleteRecursive(client, url, new AtomicInteger(0));
        }
    }

    static void deleteRecursive(final SolidSyncClient client, final URI url, final AtomicInteger depth) {
        if (url != null) {
            if (url.toString().endsWith("/")) {
                if (depth != null) {
                    depth.incrementAndGet();
                }
                // get all members
                final List<URI> members = new ArrayList<>();
                try (final SolidContainer container = client.read(url, SolidContainer.class)) {
                    container.getResources().forEach(value -> members.add(value.getIdentifier()));
                } catch (Exception e) {
                    LOGGER.error("Failed to get container members: {}", e.toString());
                    // server may have overwritten a container as a resource so attempt to delete it
                    client.delete(url);
                }

                // delete members via this method
                LOGGER.debug("DELETING MEMBERS {}", members);
                try {
                    members.forEach(u -> deleteRecursive(client, u, depth));
                } catch (SolidClientException ex) {
                    LOGGER.error("Failed to delete resources", ex);
                }

                if (depth != null) {
                    depth.decrementAndGet();
                }
            }
            deleteResource(client, url, depth);
        }
    }

    static void deleteResource(final SolidSyncClient client, final URI url, final AtomicInteger depth) {
        // delete the resource unless depth counting to avoid this
        if (depth == null || depth.get() > 0) {
            client.delete(url);
            LOGGER.debug("DELETE RESOURCE {}", url);
        }
    }

    static SolidSyncClient customSolidClient() throws NoSuchAlgorithmException, KeyManagementException {
        // setup trust all certificates for when using live servers
        final TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public void checkClientTrusted(final X509Certificate[] chain,
                                               final String authType) {
                }

                @Override
                public void checkServerTrusted(final X509Certificate[] chain,
                                               final String authType) {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[]{};
                }
            }
        };

        final SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new SecureRandom());
        LOGGER.info("Identified client type is: " + ServiceProvider.getHttpService());

        if (ServiceProvider.getHttpService().toString().contains("okhttp")) {

            final OkHttpClient.Builder newBuilder = new OkHttpClient.Builder();
            newBuilder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0]);
            //newBuilder.hostnameVerifier((hostname, session) -> true);
            HostnameVerifier hostnameVerifier = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    LOGGER.info("Trust Host :" + hostname);
                    return true;
                }
            };
            newBuilder.hostnameVerifier(hostnameVerifier);

            LOGGER.info("Set a OKHttp client which trusts all certificates.");
            return SolidSyncClient.getClientBuilder()
                    .client(new CustomOkHttpClient(OkHttpService.ofOkHttpClient(newBuilder.build())))
                    .build();
        }

        return SolidSyncClient.getClient();
    }

    private Utils() {
        // Prevent instantiation
    }

    static class CustomOkHttpClient implements Client {

        private static final int UNAUTHORIZED = 401;
        private static final String AUTHORIZATION = "Authorization";
        private static final Logger LOGGER = LoggerFactory.getLogger(CustomOkHttpClient.class);
        private final ReactiveAuthorization authHandler = new ReactiveAuthorization();
        private final HttpService httpClient;
        private final Session clientSession;

        CustomOkHttpClient(final HttpService httpClient) {
            this(httpClient, Session.anonymous());
        }

        CustomOkHttpClient(final HttpService httpClient, final Session session) {
            Objects.requireNonNull(httpClient, "Http client may not be null!");
            Objects.requireNonNull(session, "Session may not be null!");
            this.httpClient = httpClient;
            this.clientSession = session;
        }
        @Override
        public <T> CompletionStage<Response<T>> send(final Request request,
                                                     final Response.BodyHandler<T> responseBodyHandler) {
            // if there is already an auth header, just pass the request directly through
            if (request.headers().firstValue(AUTHORIZATION).isPresent()) {
                LOGGER.debug("Sending user-supplied authorization, skipping Solid authorization handling");
                return httpClient.send(request, responseBodyHandler);
            }

            // Check session cache for a relevant access token
            return clientSession.fromCache(request)
                    // Use that token, if present
                    .map(token -> httpClient.send(upgradeRequest(request, token), responseBodyHandler))
                    // Otherwise perform the regular HTTP authorization dance
                    .orElseGet(() -> httpClient.send(request, responseBodyHandler)
                        .thenCompose(res -> {
                            if (res.statusCode() == UNAUTHORIZED) {
                                final List<Challenge> challenges = Headers.WwwAuthenticate
                                        .parse(res.headers().allValues("WWW-Authenticate").toArray(new String[0]))
                                        .getChallenges();

                                return authHandler.negotiate(clientSession, request, challenges)
                                        .thenCompose(token -> token.map(t ->
                                                        httpClient.send(upgradeRequest(request, t),responseBodyHandler))
                                                .orElseGet(() -> CompletableFuture.completedFuture(res)))
                                        .exceptionally(err -> {
                                            LOGGER.debug("Unable to negotiate an authentication token: {}",
                                                    err.getMessage());
                                            return res;
                                        });
                            }
                            return CompletableFuture.completedFuture(res);
                        }));
        }

        Request upgradeRequest(final Request request, final Credential token) {
            final Request.Builder builder = Request.newBuilder()
                    .uri(request.uri())
                    .method(request.method(), request.bodyPublisher().orElseGet(Request.BodyPublishers::noBody));

            LOGGER.debug("Sending upgraded request: {}", request.uri());
            request.timeout().ifPresent(builder::timeout);
            request.headers().asMap().forEach((name, values) -> {
                for (final String value : values) {
                    builder.header(name, value);
                }
            });

            // Use setHeader to overwrite any possible existing authorization header
            builder.setHeader(AUTHORIZATION, String.join(" ", token.getScheme(), token.getToken()));
            if (DPOP.equalsIgnoreCase(token.getScheme())) {
                token.getProofThumbprint().flatMap(jkt -> clientSession.generateProof(jkt, request))
                        .ifPresent(proof -> builder.setHeader(DPOP, proof));
            }

            return builder.build();
        }

        @Override
        public Client session(final Session session) {
            return new CustomOkHttpClient(this.httpClient, session);
        }
    }
}
