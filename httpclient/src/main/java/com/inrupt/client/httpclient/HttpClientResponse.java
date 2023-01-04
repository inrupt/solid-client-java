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
package com.inrupt.client.httpclient;

import com.inrupt.client.Headers;
import com.inrupt.client.Response;

import java.net.URI;

class HttpClientResponse<T> implements Response<T> {

    private final Response.ResponseInfo info;
    private final URI uri;
    private final T body;

    public HttpClientResponse(final URI uri, final Response.ResponseInfo info, final T body) {
        this.uri = uri;
        this.info = info;
        this.body = body;
    }

    @Override
    public T body() {
        return body;
    }

    @Override
    public URI uri() {
        return uri;
    }

    @Override
    public Headers headers() {
        return info.headers();
    }

    @Override
    public int statusCode() {
        return info.statusCode();
    }
}
