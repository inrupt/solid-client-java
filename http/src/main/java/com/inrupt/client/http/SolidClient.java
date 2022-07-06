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
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;

public class SolidClient extends HttpClient {

    private final HttpClient client;
    private final SolidAuthenticator solidAuthenticator;

    @Override
    public Optional<Authenticator> authenticator() {
        return client.authenticator();
    }

    @Override
    public Optional<Duration> connectTimeout() {
        return client.connectTimeout();
    }

    @Override
    public Optional<CookieHandler> cookieHandler() {
        return client.cookieHandler();
    }

    @Override
    public Optional<Executor> executor() {
        return client.executor();
    }

    @Override
    public HttpClient.Redirect followRedirects() {
        return client.followRedirects();
    }

    @Override
    public Optional<ProxySelector> proxy() {
        return client.proxy();
    }

    @Override
    public HttpClient.Version version() {
        return client.version();
    }

    @Override
    public SSLContext sslContext() {
        return client.sslContext();
    }

    @Override
    public SSLParameters sslParameters() {
        return client.sslParameters();
    }

    @Override
    public <T> HttpResponse<T> send(final HttpRequest request, final HttpResponse.BodyHandler<T> responseBodyHandler)
            throws IOException, InterruptedException {
        return client.send(request, responseBodyHandler);
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(final HttpRequest request,
            final HttpResponse.BodyHandler<T> responseBodyHandler) {
        return client.sendAsync(request, responseBodyHandler);
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(final HttpRequest request,
            final HttpResponse.BodyHandler<T> responseBodyHandler,
            final HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {
        return client.sendAsync(request, responseBodyHandler, pushPromiseHandler);
    }

    protected SolidClient(final HttpClient client, final SolidAuthenticator authenticator) {
        this.client = client;
        this.solidAuthenticator = authenticator;
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

        public static Builder newBuilder() {
            return new Builder();
        }
    }
}
