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

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UncheckedIOException;
import java.net.http.HttpRequest;

/**
 * {@link HttpRequest.BodyPublisher} implementations for use with Verifiable Credential types.
 */
public final class VerifiableCredentialBodyPublishers {

    private static final JsonProcessor processor = VcUtils.loadJsonProcessor();

    /**
     * Serialize a {@link VerifiableCredential} as an HTTP request body.
     *
     * @param vc the verifiable credential
     * @return the body publisher
     */
    public static HttpRequest.BodyPublisher ofVerifiableCredential(final VerifiableCredential vc) {
        final var in = new PipedInputStream();
        try (final var out = new PipedOutputStream(in)) {
            processor.toJson(vc, out);
        } catch (final IOException ex) {
            throw new UncheckedIOException("Error serializing verifiable credential", ex);
        }
        return HttpRequest.BodyPublishers.ofInputStream(() -> in);
    }

    /**
     * Serialize a {@link VerifiablePresentation} as an HTTP request body.
     *
     * @param vp the verifiable presentation
     * @return the body publisher
     */
    public static HttpRequest.BodyPublisher ofVerifiablePresentation(final VerifiablePresentation vp) {
        final var in = new PipedInputStream();
        try (final var out = new PipedOutputStream(in)) {
            processor.toJson(vp, out);
        } catch (final IOException ex) {
            throw new UncheckedIOException("Error serializing verifiable presentation", ex);
        }
        return HttpRequest.BodyPublishers.ofInputStream(() -> in);
    }

    private VerifiableCredentialBodyPublishers() {
        // Prevent instantiation
    }
}
