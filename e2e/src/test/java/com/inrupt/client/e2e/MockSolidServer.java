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
package com.inrupt.client.e2e;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class MockSolidServer {

    private final WireMockServer wireMockServer;
    private Map<String, ServerBody> storage = new ConcurrentHashMap<>();

    public MockSolidServer() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort()
                .extensions(new SolidServerTransformer(storage))
                .notifier(new ConsoleNotifier(true)));
    }
    
    public int getPort() {
        return wireMockServer.port();
    }

    private void setupMocks() {
        wireMockServer.stubFor(get(anyUrl()));
        wireMockServer.stubFor(put(anyUrl()));
        wireMockServer.stubFor(post(anyUrl()));
        wireMockServer.stubFor(patch(anyUrl()));
        wireMockServer.stubFor(delete(anyUrl()));
    }

    public Map<String, String> start() {
        wireMockServer.start();

        setupMocks();
        return Collections.singletonMap("solid_server", wireMockServer.baseUrl());
    }

    public void stop() {
        wireMockServer.stop();
    }

    static class ServerBody {
        final byte[] body;
        final String contentType;

        public ServerBody(final byte[] body, final String contentType) {
            this.body = body.clone();
            this.contentType = contentType;
        }
    }

}
