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
package com.inrupt.client.util;

public final class HttpConfig implements Options {

    private final long retryRedirects;

    /**
     * The HttpConfig number of retry redirects.
     *
     * @return the retryRedirects
     */
    public long retryRedirects() {
        return this.retryRedirects;
    }

    /**
     * Creates a {@link HttpConfig} builder.
     *
     * @return the builder
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    private HttpConfig(final long retryRedirects) {
        this.retryRedirects = retryRedirects;
    }

    /**
     * A {@link HttpConfig} builder.
     */
    public static final class Builder {
        private long retryRedirects = DEFAULT_RETRY_REDIRECTS;

        /**
         * Build the {@link HttpConfig}.
         *
         * @return the config
         */
        public HttpConfig build() {
            return new HttpConfig(retryRedirects);
        }

        /**
         * A convenience method for setting the number of redirect retries.
         *
         * @param retryRedirects the number of retry redirects
         * @return this builder
         */
        public Builder redirects(final long retryRedirects) {
            this.retryRedirects = retryRedirects;
            return this;
        }

        private Builder() {
            // Prevent direct instantiation.
        }
    }

}
