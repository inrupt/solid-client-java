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
package com.inrupt.client.http;

import com.inrupt.client.authentication.SolidAuthenticator;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletionStage;

public class SolidClient {

    private final HttpClient client;
    private final SolidAuthenticator authenticator;

    public HttpClient httpClient() {
        return client;
    }

    public <T> HttpResponse<T> send(final HttpRequest request, final HttpResponse.BodyHandler<T> handler)
            throws IOException, InterruptedException {
        return client.send(request, handler);
    }

    public <T> CompletionStage<HttpResponse<T>> sendAsync(final HttpRequest request,
            final HttpResponse.BodyHandler<T> handler) {
        return client.sendAsync(request, handler);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    protected SolidClient(final HttpClient client, final SolidAuthenticator authenticator) {
        this.client = client;
        this.authenticator = authenticator;
    }

    public static class Builder {

        private HttpClient client;
        private SolidAuthenticator authenticator;

        public Builder authenticator(final SolidAuthenticator authenticator) {
            this.authenticator = authenticator;
            return this;
        }

        public Builder client(final HttpClient client) {
            this.client = client;
            return this;
        }

        public SolidClient build() {
            if (client == null) {
                client = HttpClient.newBuilder().build();
            }
            return new SolidClient(client, authenticator);
        }

        protected Builder() {
        }
    }
}
