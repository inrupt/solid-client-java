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
package com.inrupt.client;

import com.inrupt.client.spi.HttpService;

import java.util.concurrent.CompletionStage;

public interface Client {

    /**
     * Perform an HTTP request.
     *
     * @param request the request
     * @param responseBodyHandler the response body handler
     * @param <T> the response handler type
     * @return the next stage of completion, containing the response
     */
    <T> CompletionStage<Response<T>> send(Request request, Response.BodyHandler<T> responseBodyHandler);

    /**
     * Create a session-scoped client.
     *
     * @param session the session manager
     * @return the session-scoped client
     */
    Client session(Session session);

    interface Builder {

        /**
         * Add a specific {@link HttpService} instance to the builder.
         *
         * @param instance the http service
         * @return this builder
         */
        Builder withInstance(HttpService instance);

        /**
         * Build the client.
         *
         * @return the client
         */
        Client build();
    }
}
