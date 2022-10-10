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

import com.inrupt.client.api.Response;

import java.net.URI;
import java.util.List;
import java.util.Map;

class OkHttpResponse<T> implements Response<T> {
    private final URI responseUri;
    private final T responseBody;
    private final Response.ResponseInfo info;

    public OkHttpResponse(final URI uri, final Response.ResponseInfo info, final T body) {
        this.responseUri = uri;
        this.responseBody = body;
        this.info = info;
    }

    @Override
    public T body() {
        return responseBody;
    }

    @Override
    public Map<String, List<String>> headers() {
        return info.headers();
    }

    @Override
    public URI uri() {
        return responseUri;
    }

    @Override
    public int statusCode() {
        return info.statusCode();
    }

}
