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
package com.inrupt.client;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

/**
 * An HTTP Response.
 *
 * <p>This interface provides a generic API for interacting with HTTP responses.
 *
 * @param <T> the type of the response body
 */
public interface Response<T> {

    /**
     * The body of the HTTP response.
     *
     * @return the response body
     */
    T body();

    /**
     * The headers from this HTTP response.
     *
     * @return the response headers
     */
    Headers headers();

    /**
     * The URI from which the response was received.
     *
     * @return the response URI
     */
    URI uri();

    /**
     * The status code of an HTTP response.
     *
     * @return the response code
     */
    int statusCode();

    /**
     * Initial response info supplied to a {@link BodyHandler} before the body is processed.
     */
    interface ResponseInfo {
        /**
         * Retrieve the response headers.
         *
         * @return the response headers
         */
        Headers headers();

        /**
         * Retrieve the URI of the response.
         *
         * @return the response URI
         */
        URI uri();

        /**
         * Retrieve the status code from the response.
         * @return the response status
         */
        int statusCode();

        /**
         * Retrieve the response body as a byte buffer.
         * @return the body
         */
        ByteBuffer body();
    }

    /**
     * An interface for mapping an HTTP response into a specific Java type.
     * @param <T> the body type
     */
    @FunctionalInterface
    interface BodyHandler<T> {

        /**
         * Transform the response into the desired Java type.
         *
         * @param response the initial response information
         * @return the response body
         */
        T apply(ResponseInfo response);
    }

    /**
     * Convenience methods for creating common {@link BodyHandler} objects.
     */
    final class BodyHandlers {

        /**
         * Creates a {@code byte[]} response handler.
         *
         * @return the body handler
         */
        public static BodyHandler<byte[]> ofByteArray() {
            return responseInfo -> responseInfo.body().array();
        }

        /**
         * Creates a {@link String}-based response handler.
         *
         * @return the body handler
         */
        public static BodyHandler<String> ofString() {
            return responseInfo -> new String(responseInfo.body().array(), UTF_8);
        }

        /**
         * Creates an {@link InputStream}-based response handler.
         *
         * @return the body handler
         */
        public static BodyHandler<InputStream> ofInputStream() {
            return responseInfo -> new ByteArrayInputStream(responseInfo.body().array());
        }

        /**
         * Creates a response handler that discards the body.
         *
         * @return the body handler
         */
        public static BodyHandler<Void> discarding() {
            return responseInfo -> null;
        }

        /**
         * Throws on HTTP error, or apply the provided body handler.
         * @param handler the body handler to apply on non-error HTTP responses
         * @param isSuccess a callback determining error cases
         * @return the body handler
         * @param <T> the type of the body handler
         */
        public static <T> Response.BodyHandler<T> throwOnError(
                final Response.BodyHandler<T> handler,
                final Function<Response.ResponseInfo, Boolean> isSuccess
        ) {
            return responseinfo -> {
                if (!isSuccess.apply(responseinfo)) {
                    throw new ClientHttpException(
                        "An HTTP error has been returned from "
                            + responseinfo.uri()
                            + " with status code "
                            + responseinfo.statusCode(),
                        responseinfo.uri(),
                        responseinfo.statusCode(),
                        responseinfo.headers(),
                        new String(responseinfo.body().array(), StandardCharsets.UTF_8)
                    );
                }
                return handler.apply(responseinfo);
            };
        }

        private BodyHandlers() {
            // Prevent instantiation
        }
    }
}

