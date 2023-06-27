/*
 * Copyright Inrupt Inc.
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
package com.inrupt.client.integration.base;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.inrupt.client.Request;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class MockSolidServer {

    private final WireMockServer wireMockServer;
    private final String USER_AGENT_HEADER = "User-Agent";
    private static final String USER_AGENT = "InruptJavaClient/" + Request.class
        .getPackage().getImplementationVersion();
    private Map<String, ServerBody> storage = new ConcurrentHashMap<>();
    private String asUri;

    public MockSolidServer(final String asUri) {
        this.asUri = asUri;
        storage.put("/", new ServerBody(new byte[0], Utils.TEXT_TURTLE)); //add root to storage

        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort()
                .extensions(new SolidServerTransformer(storage, this.asUri)));
    }

    private void setupMocks() {
        wireMockServer.stubFor(get(anyUrl()).withHeader(USER_AGENT_HEADER, equalTo(USER_AGENT)));
        wireMockServer.stubFor(put(anyUrl()).withHeader(USER_AGENT_HEADER, equalTo(USER_AGENT)));
        wireMockServer.stubFor(post(anyUrl()).withHeader(USER_AGENT_HEADER, equalTo(USER_AGENT)));
        wireMockServer.stubFor(patch(anyUrl()).withHeader(USER_AGENT_HEADER, equalTo(USER_AGENT)));
        wireMockServer.stubFor(delete(anyUrl()).withHeader(USER_AGENT_HEADER, equalTo(USER_AGENT)));
    }

    public void start() {
        wireMockServer.start();
        setupMocks();
    }

    public String getMockServerUrl() {
        return wireMockServer.baseUrl();
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
