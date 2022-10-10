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
 * OkHttp bindings for the Inrupt client libraries.
 */
package com.inrupt.client.okhttp;

import com.inrupt.client.api.Request;
import com.inrupt.client.api.Response;
import com.inrupt.client.spi.HttpProcessor;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

public class OkHttpProcessor implements HttpProcessor {

    private final OkHttpClient client;

    public OkHttpProcessor() {
        this.client = new OkHttpClient();
    }

    @Override
    public <T> Response<T> send(final Request request, final Response.BodyHandler<T> handler)
            throws IOException {
        try (final okhttp3.Response res = client.newCall(prepareRequest(request)).execute()) {
            final Response.ResponseInfo info = new OkHttpResponseInfo(res);
            return new OkHttpResponse<>(res.request().url().uri(), info, handler.apply(info));
        }
    }

    @Override
    public <T> CompletionStage<Response<T>> sendAsync(final Request request, final Response.BodyHandler<T> handler)
            throws IOException {
        final CompletableFuture<Response<T>> future = new CompletableFuture<>();
        client.newCall(prepareRequest(request)).enqueue(new Callback() {
            @Override
            public void onResponse(final Call call, final okhttp3.Response res) throws IOException {
                try (final okhttp3.Response r = res) {
                    final Response.ResponseInfo info = new OkHttpResponseInfo(r);
                    future.complete(new OkHttpResponse<>(res.request().url().uri(), info, handler.apply(info)));
                }
            }

            @Override
            public void onFailure(final Call call, final IOException ex) {
                future.completeExceptionally(ex);
            }
        });

        return future;
    }

    static okhttp3.Request prepareRequest(final Request request) {
        final Headers headers = buildHeaders(request.headers());

        final RequestBody body = RequestBody.Companion.create(request.bodyPublisher()
                .orElseGet(Request.BodyPublishers::noBody).getBytes().array(),
                MediaType.parse(headers.get("Content-Type")));

        return new okhttp3.Request.Builder()
            .url(request.uri().toString())
            .headers(headers)
            .method(request.method(), body)
            .build();
    }

    static Headers buildHeaders(final Map<String, List<String>> headers) {
        final Headers.Builder builder = Headers.of().newBuilder();
        for (final Map.Entry<String, List<String>> entry : headers.entrySet()) {
            for (final String value : entry.getValue()) {
                builder.add(entry.getKey(), value);
            }
        }
        return builder.build();
    }

}
