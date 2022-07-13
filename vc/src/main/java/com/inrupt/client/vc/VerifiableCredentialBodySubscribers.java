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
package com.inrupt.client.vc;

import com.inrupt.client.spi.JsonProcessor;
import com.inrupt.client.spi.ServiceProvider;

import java.net.http.HttpResponse;


/**
 * {@link HttpResponse.BodySubscriber} implementations for use with Verifiable Credential types.
 */
public final class VerifiableCredentialBodySubscribers {

    private static final JsonProcessor processor = ServiceProvider.getJsonProcessor();

    /**
     * Process an HTTP response as a Verifiable Credential.
     *
     * @return the body subscriber
     */
    public static HttpResponse.BodySubscriber<VerifiableCredential> ofVerifiableCredential() {
        final var upstream = HttpResponse.BodySubscribers.ofInputStream();

        return HttpResponse.BodySubscribers.mapping(upstream, input ->
                processor.fromJson(input, VerifiableCredential.class));
    }

    /**
     * Process an HTTP response as a Verifiable Presentation.
     *
     * @return the body subscriber
     */
    public static HttpResponse.BodySubscriber<VerifiablePresentation> ofVerifiablePresentation() {
        final var upstream = HttpResponse.BodySubscribers.ofInputStream();

        return HttpResponse.BodySubscribers.mapping(upstream, input ->
                processor.fromJson(input, VerifiablePresentation.class));
    }

    private VerifiableCredentialBodySubscribers() {
        // Prevent instantiation
    }
}
