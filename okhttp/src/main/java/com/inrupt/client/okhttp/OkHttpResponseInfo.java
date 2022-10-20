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
package com.inrupt.client.okhttp;

import com.inrupt.client.api.Headers;
import com.inrupt.client.api.Response.ResponseInfo;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.ByteBuffer;

import okhttp3.Response;

class OkHttpResponseInfo implements ResponseInfo {

    private final Response response;

    public OkHttpResponseInfo(final Response response) {
        this.response = response;
    }

    @Override
    public Headers headers() {
        return Headers.of(response.headers().toMultimap());
    }

    @Override
    public int statusCode() {
        return response.code();
    }

    @Override
    public URI uri() {
        return response.request().url().uri();
    }

    @Override
    public ByteBuffer body() {
        try {
            return ByteBuffer.wrap(response.body().bytes());
        } catch (final IOException ex) {
            throw new UncheckedIOException("Unable to handle response data", ex);
        }
    }
}
