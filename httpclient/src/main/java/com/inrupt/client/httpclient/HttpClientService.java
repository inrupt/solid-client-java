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
/**
 * HttpClient bindings for the Inrupt client libraries.
 */
package com.inrupt.client.httpclient;

import com.inrupt.client.Request;
import com.inrupt.client.Response;
import com.inrupt.client.spi.HttpService;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link HttpService} using the {@code java.net.http.HttpClient}.
 */
public class HttpClientService implements HttpService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientService.class);

    private final HttpClient client;

    /**
     * Create an HTTP client service with a default {@link HttpClient}.
     */
    public HttpClientService() {
        this(HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build());
    }

    private HttpClientService(final HttpClient client) {
        LOGGER.debug("Initializing HttpClient service for HTTP client support");
        this.client = client;
    }

    @Override
    public <T> CompletionStage<Response<T>> send(final Request request, final Response.BodyHandler<T> handler) {
        final var builder = HttpRequest.newBuilder(request.uri());

        final var publisher = request.bodyPublisher().map(Request.BodyPublisher::getBytes)
            .map(buf -> HttpRequest.BodyPublishers.ofByteArray(buf.array()))
            .orElseGet(HttpRequest.BodyPublishers::noBody);

        builder.method(request.method(), publisher);
        request.timeout().ifPresent(builder::timeout);

        for (final Map.Entry<String, List<String>> entry : request.headers().asMap().entrySet()) {
            for (final String value : entry.getValue()) {
                builder.header(entry.getKey(), value);
            }
        }

        final HttpRequest req = builder.build();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Sending Request. Method: {}, URI: {}", req.method(), req.uri());
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Request Headers: {}", req.headers());
            }
        }

        return client.sendAsync(req, HttpResponse.BodyHandlers.ofByteArray())
            .thenApply(res -> {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Response Status Code: {}", res.statusCode());
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("Response Headers: {}", res.headers());
                    }
                }
                final var info = new HttpClientResponseInfo(res, ByteBuffer.wrap(res.body()));
                return new HttpClientResponse<>(res.uri(), info, handler.apply(info));
            });
    }

    /**
     * Create an HTTP client service with a pre-configured {@link HttpClient}.
     *
     * @param client the HttpClient
     * @return an HTTP client service
     */
    public static HttpClientService ofHttpClient(final HttpClient client) {
        return new HttpClientService(client);
    }
}
