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

import com.inrupt.client.spi.VerifiableCredential;
import com.inrupt.client.spi.VerifiablePresentation;

import java.net.http.HttpResponse;

/**
 * {@link HttpResponse.BodyHandler} implementations for use with Verifiable Credential types.
 */
public final class VerifiableCredentialBodyHandlers {

    private static final int SUCCESS = 200;
    private static final int CREATED = 201;
    private static final int NO_CONTENT = 204;

    /**
     * Create a {@link VerifiableCredential} from an HTTP response.
     *
     * @return the body handler
     */
    public static HttpResponse.BodyHandler<VerifiableCredential> ofVerifiableCredential() {
        return responseInfo -> {
            final HttpResponse.BodySubscriber<VerifiableCredential> bodySubscriber;
            final int httpStatus = responseInfo.statusCode();
            if (SUCCESS == httpStatus || CREATED == httpStatus || NO_CONTENT == httpStatus ) {
                return VerifiableCredentialBodySubscribers.ofVerifiableCredential();
            } else {
                bodySubscriber = HttpResponse.BodySubscribers.replacing(null);
                bodySubscriber.onError(new VerifiableCredentialException(
                    "Unexpected error response when handling a verifiable credential.",
                    httpStatus));
            }
            return bodySubscriber;
        };
    }

    /**
     * Create a {@link VerifiablePresentation} from an HTTP response.
     *
     * @return the body handler
     */
    public static HttpResponse.BodyHandler<VerifiablePresentation> ofVerifiablePresentation() {
        return responseInfo -> {
            final HttpResponse.BodySubscriber<VerifiablePresentation> bodySubscriber;
            final int httpStatus = responseInfo.statusCode();
            if (SUCCESS == httpStatus || CREATED == httpStatus || NO_CONTENT == httpStatus ) {
                return VerifiableCredentialBodySubscribers.ofVerifiablePresentation();
            } else {
                bodySubscriber = HttpResponse.BodySubscribers.replacing(null);
                bodySubscriber.onError(new VerifiableCredentialException(
                    "Unexpected error response when handling a verifiable presentation.",
                    httpStatus));
            }
            return bodySubscriber;
        };
    }

    private VerifiableCredentialBodyHandlers() {
        // Prevent instantiation
    }
}

