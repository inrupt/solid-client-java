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
package com.inrupt.client.integration.customokhttp;

import com.inrupt.client.Request;
import com.inrupt.client.Response;
import com.inrupt.client.spi.HttpService;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

/**
 * A {@link HttpService} using the {@code okhttp3.OkHttpClient}.
 */
public class CustomOkHttpService implements HttpService {

    static final Logger LOGGER = LoggerFactory.getLogger(CustomOkHttpService.class);

    private static final Set<String> NO_BODY_METHODS = new HashSet<>(Arrays.asList("GET", "HEAD", "DELETE"));

    private final OkHttpClient client;

    /**
     * Create an HTTP client service with a default {@link OkHttpClient}.
     */
    public CustomOkHttpService() {
        LOGGER.debug("Initializing CustomOkHttpClient service which trusts all certificates.");
        this.client = trustAllCertsClient();
    }

    private CustomOkHttpService(final OkHttpClient client) {
        LOGGER.debug("Initializing OkHttpClient service for HTTP client support");
        this.client = client;
    }

    @Override
    public <T> CompletionStage<Response<T>> send(final Request request, final Response.BodyHandler<T> handler) {
        final CompletableFuture<Response<T>> future = new CompletableFuture<>();
        final okhttp3.Request req = prepareRequest(request);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Sending Request. Method: {}, URI: {}", req.method(), req.url());
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Request Headers: {}", req.headers());
            }
        }
        client.newCall(req).enqueue(new Callback() {
            @Override
            public void onResponse(final Call call, final okhttp3.Response res) throws IOException {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Response Status Code: {}", res.code());
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("Response Headers: {}", res.headers());
                    }
                }
                try (final okhttp3.Response r = res) {
                    final Response.ResponseInfo info = new CustomOkHttpResponseInfo(r);
                    future.complete(new CustomOkHttpResponse<>(res.request().url().uri(), info, handler.apply(info)));
                } catch (final RuntimeException ex) {
                    future.completeExceptionally(ex);
                }
            }

            @Override
            public void onFailure(final Call call, final IOException ex) {
                future.completeExceptionally(ex);
            }
        });

        return future;
    }

    /**
     * Create an HTTP client service with a pre-configured {@link OkHttpClient}.
     *
     * @param client the OkHttpClient
     * @return an HTTP client service
     */
    public static CustomOkHttpService ofOkHttpClient(final OkHttpClient client) {
        return new CustomOkHttpService(client);
    }

    static RequestBody prepareBody(final Request request, final MediaType mediaType) {
        if (NO_BODY_METHODS.contains(request.method())) {
            return null;
        }
        return RequestBody.Companion.create(request.bodyPublisher()
                .orElseGet(Request.BodyPublishers::noBody).getBytes().array(), mediaType);
    }

    static okhttp3.Request prepareRequest(final Request request) {
        final Headers headers = prepareHeaders(request.headers().asMap());
        final MediaType mediaType = getContentType(headers);

        return new okhttp3.Request.Builder()
            .url(request.uri().toString())
            .headers(headers)
            .method(request.method(), prepareBody(request, mediaType))
            .build();
    }

    static Headers prepareHeaders(final Map<String, List<String>> headers) {
        final Headers.Builder builder = Headers.of().newBuilder();
        for (final Map.Entry<String, List<String>> entry : headers.entrySet()) {
            for (final String value : entry.getValue()) {
                builder.add(entry.getKey(), value);
            }
        }
        return builder.build();
    }

    static MediaType getContentType(final Headers headers) {
        final String ct = headers.get("Content-Type");
        if (ct != null) {
            return MediaType.parse(ct);
        }
        return MediaType.parse("application/octet-stream");
    }

    OkHttpClient trustAllCertsClient() {
        // setup trust all certificates
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

        final SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new SecureRandom());
        } catch (KeyManagementException e) {
            throw new RuntimeException("There was a key management exception in the CustomOkHttp client.");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("There was a no such algorithm exception in the CustomOkHttp client.");
        }

        final OkHttpClient.Builder newBuilder = new OkHttpClient.Builder();
        newBuilder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0]);
        newBuilder.hostnameVerifier((hostname, session) -> true);
        newBuilder.callTimeout(Duration.ofSeconds(30));

        return newBuilder.build();
    }

}
