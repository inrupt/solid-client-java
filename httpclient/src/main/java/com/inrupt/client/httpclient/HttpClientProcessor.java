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
/**
 * HttpClient bindings for the Inrupt client libraries.
 */
package com.inrupt.client.httpclient;

import com.inrupt.client.api.Request;
import com.inrupt.client.api.Response;
import com.inrupt.client.spi.HttpProcessor;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.SubmissionPublisher;

public class HttpClientProcessor implements HttpProcessor {

    private final HttpClient client;

    public HttpClientProcessor() {
        this(HttpClient.newHttpClient());
    }

    public HttpClientProcessor(final HttpClient client) {
        this.client = client;
    }

    @Override
    public <T> Response<T> send(final Request request, final Response.BodyHandler<T> handler)
            throws IOException {
        return sendAsync(request, handler).toCompletableFuture().join();
    }

    @Override
    public <T> CompletionStage<Response<T>> sendAsync(final Request request, final Response.BodyHandler<T> handler)
            throws IOException {
        final var builder = HttpRequest.newBuilder(request.uri());

        final var publisher = new SubmissionPublisher<ByteBuffer>();
        builder.method(request.method(), HttpRequest.BodyPublishers.fromPublisher(publisher));

        request.bodyPublisher().map(Request.BodyPublisher::getBytes).ifPresent(publisher::submit);

        for (final Map.Entry<String, List<String>> entry : request.headers().entrySet()) {
            for (final String value : entry.getValue()) {
                builder.header(entry.getKey(), value);
            }
        }

        return client.sendAsync(builder.build(), HttpResponse.BodyHandlers.ofByteArray())
            .thenApply(res -> {
                final var info = new HttpClientResponseInfo(res, ByteBuffer.wrap(res.body()));
                return new HttpClientResponse<>(res.uri(), info, handler.apply(info));
            });
    }
}
