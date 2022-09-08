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
package com.inrupt.client.core;

import java.io.InputStream;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
import java.util.function.Function;

public final class InputStreamSubscriber<T> implements HttpResponse.BodySubscriber<T> {

    private final HttpResponse.BodySubscriber<InputStream> upstream;
    private final Function<InputStream, T> mapper;

    private InputStreamSubscriber(final HttpResponse.BodySubscriber<InputStream> upstream,
            final Function<InputStream, T> mapper) {
        this.upstream = upstream;
        this.mapper = mapper;
    }

    public static <T> HttpResponse.BodySubscriber<T> mapping(final HttpResponse.BodySubscriber<InputStream> upstream,
            final Function<InputStream, T> mapper) {
        return new InputStreamSubscriber<T>(upstream, mapper);
    }

    @Override
    public CompletionStage<T> getBody() {
        return upstream.getBody().thenApplyAsync(mapper::apply);
    }

    @Override
    public void onComplete() {
        upstream.onComplete();
    }

    @Override
    public void onError(final Throwable throwable) {
        upstream.onError(throwable);
    }

    @Override
    public void onNext(final List<ByteBuffer> item) {
        upstream.onNext(item);
    }

    @Override
    public void onSubscribe(final Flow.Subscription subscription) {
        upstream.onSubscribe(subscription);
    }
}
