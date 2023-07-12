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
package com.inrupt.client.integration.openid;

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
import com.inrupt.client.spi.ServiceProvider;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import okhttp3.OkHttpClient;

public final class OpenIdUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenIdUtils.class);
    public static final String DPOP = "DPoP";

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
            newBuilder.hostnameVerifier((hostname, session) -> {
                LOGGER.info("Trust Host :" + hostname);
                return true;
            });

            LOGGER.info("Set a OKHttp client which trusts all certificates.");
            return SolidSyncClient.getClientBuilder()
                    .client(new CustomOkHttpClient(OkHttpService.ofOkHttpClient(newBuilder.build())))
                    .build();
        }

        return SolidSyncClient.getClient();
    }

    private OpenIdUtils() {
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
