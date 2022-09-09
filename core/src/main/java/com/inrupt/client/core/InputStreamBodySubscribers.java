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

/**
 * This class provides a convenience method for mapping an HTTP response from an
 * {@link InputStream} to a custom type.
 *
 * <p>For example, writing a custom {@link HttpResponse.BodySubscriber} to parse a JSON response
 * payload into a custom type can be performed in the following way:
 *
 * <pre>{@code
 *     public static HttpResponse.BodySubscriber<VerifiablePresentation> ofVerifiablePresentation() {
 *         return InputStreamBodySubscribers.mapping(input -> {
 *             try (var stream = input) {
 *                 return processor.fromJson(stream, VerifiablePresentation.class);
 *             } catch (IOException ex) {
 *                 throw new UncheckedIOException("Error parsing presentation", ex);
 *             }
 *         });
 *     }
 * }</pre>
 *
 * <p>After consuming an {@link InputStream}, please be sure to close the resource.
 *
 * @param <T> the type into which the {@link InputStream} is mapped.
 */
public final class InputStreamBodySubscribers<T> implements HttpResponse.BodySubscriber<T> {

    private final HttpResponse.BodySubscriber<InputStream> upstream;
    private final Function<InputStream, T> mapper;

    private InputStreamBodySubscribers(final HttpResponse.BodySubscriber<InputStream> upstream,
            final Function<InputStream, T> mapper) {
        this.upstream = upstream;
        this.mapper = mapper;
    }

    /**
     * Map an {@link InputStream}-based HTTP response body into a custom type.
     *
     * @param <T> the type into which the input stream will be converted
     * @param mapper the mapping function
     * @return the mapped body subscriber
     */
    public static <T> HttpResponse.BodySubscriber<T> mapping(final Function<InputStream, T> mapper) {
        return mapping(HttpResponse.BodySubscribers.ofInputStream(), mapper);
    }

    /**
     * Map an {@link InputStream}-based HTTP response body into a custom type.
     *
     * @param <T> the type into which the input stream will be converted
     * @param upstream the upstream input stream
     * @param mapper the mapping function
     * @return the mapped body subscriber
     */
    public static <T> HttpResponse.BodySubscriber<T> mapping(final HttpResponse.BodySubscriber<InputStream> upstream,
            final Function<InputStream, T> mapper) {
        return new InputStreamBodySubscribers<T>(upstream, mapper);
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
