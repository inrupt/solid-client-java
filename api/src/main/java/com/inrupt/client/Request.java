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

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

import org.apache.commons.io.IOUtils;

/**
 * An HTTP Request.
 *
 * <p>This interface provides a generic API for building and interacting with HTTP requests.
 */
public final class Request {

    private final String requestMethod;
    private final URI requestUri;
    private final Headers requestHeaders;
    private final BodyPublisher publisher;
    private final Duration requestTimeout;

    /**
     * The HTTP method.
     *
     * @return the HTTP method
     */
    public String method() {
        return this.requestMethod;
    }

    /**
     * The HTTP URI.
     *
     * @return the HTTP URI
     */
    public URI uri() {
        return this.requestUri;
    }

    /**
     * The {@link BodyPublisher} set on this request.
     *
     * @return the body publisher, if present
     */
    public Optional<BodyPublisher> bodyPublisher() {
        return Optional.ofNullable(publisher);
    }

    /**
     * The HTTP headers for this request.
     *
     * @return the HTTP headers
     */
    public Headers headers() {
        return requestHeaders;
    }

    /**
     * The timeout for this request.
     *
     * @return the timeout for this request, if present
     */
    public Optional<Duration> timeout() {
        return Optional.ofNullable(requestTimeout);
    }

    /**
     * Creates a {@link Request} builder.
     *
     * @return the builder
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Creates a {@link Request} builder with the given URI.
     *
     * @param uri the request URI
     * @return the builder
     */
    public static Builder newBuilder(final URI uri) {
        return new Builder().uri(uri);
    }

    Request(final URI uri, final String method, final Map<String, List<String>> headers,
            final BodyPublisher publisher, final Duration timeout) {
        this.requestUri = Objects.requireNonNull(uri, "Request URI may not be null!");
        this.requestMethod = Objects.requireNonNull(method, "Request method may not be null!");
        this.requestHeaders = Headers.of(Objects.requireNonNull(headers, "Request headers may not be null!"));
        this.requestTimeout = timeout;
        this.publisher = publisher;
    }

    /**
     * An API for serializing an HTTP Request.
     */
    public interface BodyPublisher {

        /**
         * Get the serialized bytes for an HTTP Request.
         *
         * @return the serialized request body
         */
        ByteBuffer getBytes();

        /**
         * Get the length of the HTTP Request.
         *
         * @return the request length
         */
        long contentLength();
    }

    /**
     * Built-in {@link BodyPublisher} implementations.
     */
    public static final class BodyPublishers {

        /**
         * Create a String-based {@link BodyPublisher}.
         *
         * @param body the request body
         * @return the publisher
         */
        public static BodyPublisher ofString(final String body) {
            return ofByteArray(body.getBytes(UTF_8));
        }

        /**
         * Create a byte-array-based {@link BodyPublisher}.
         *
         * @param body the request body
         * @return the publisher
         */
        public static BodyPublisher ofByteArray(final byte[] body) {
            return new ByteBufferPublisher(body);
        }

        /**
         * Create an InputStream-based {@link BodyPublisher}.
         *
         * @param body the request body
         * @return the publisher
         */
        public static BodyPublisher ofInputStream(final InputStream body) {
            try {
                return new ByteBufferPublisher(IOUtils.toByteArray(body));
            } catch (final IOException ex) {
                throw new UncheckedIOException("Error reading request body", ex);
            }
        }

        /**
         * Create an empty {@link BodyPublisher}.
         *
         * @return the publisher
         */
        public static BodyPublisher noBody() {
            return new ByteBufferPublisher(new byte[0]);
        }

        private BodyPublishers() {
            // Prevent instantiation
        }
    }

    /**
     * A {@link Request} builder.
     */
    public static final class Builder {
        private URI requestUri;
        private String requestMethod = "GET";
        private Duration requestTimeout;
        private BodyPublisher publisher = BodyPublishers.noBody();
        private final Map<String, List<String>> requestHeaders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        /**
         * Set the URI for this request.
         *
         * @param uri the request URI
         * @return this builder
         */
        public Builder uri(final URI uri) {
            this.requestUri = uri;
            return this;
        }

        /**
         * Set an individual header for this request.
         *
         * @param name the header name
         * @param value the header value
         * @return this builder
         */
        public Builder header(final String name, final String value) {
            this.requestHeaders.computeIfAbsent(name, k -> new ArrayList<String>()).add(value);
            return this;
        }

        /**
         * Set an individual header for this request.
         *
         * <p>This method will remove all existing headers defined for the provided name
         *
         * @param name the header name
         * @param value the header value
         * @return this builder
         */
        public Builder setHeader(final String name, final String value) {
            final List<String> values = new ArrayList<>();
            values.add(value);
            this.requestHeaders.put(name, values);
            return this;
        }

        /**
         * Set the HTTP method for this request.
         *
         * @param method the HTTP request method
         * @param publisher the body publisher
         * @return this builder
         */
        public Builder method(final String method, final BodyPublisher publisher) {
            this.requestMethod = method;
            this.publisher = publisher;
            return this;
        }

        /**
         * Set a timeout value for this request.
         *
         * @param timeout the timeout value
         * @return this builder
         */
        public Builder timeout(final Duration timeout) {
            this.requestTimeout = timeout;
            return this;
        }

        /**
         * A convenience method for setting a Content-Type header.
         *
         * @param type the content-type
         * @return this builder
         */
        public Builder type(final String type) {
            return setHeader("Content-Type", type);
        }

        /**
         * A convenience method for building an HTTP GET request.
         *
         * @return this builder
         */
        public Builder GET() {
            return method("GET", BodyPublishers.noBody());
        }

        /**
         * A convenience method for building an HTTP POST request with a body.
         *
         * @param publisher the body publisher
         * @return this builder
         */
        public Builder POST(final BodyPublisher publisher) {
            return method("POST", publisher);
        }

        /**
         * A convenience method for building an HTTP PUT request with a body.
         *
         * @param publisher the body publisher
         * @return this builder
         */
        public Builder PUT(final BodyPublisher publisher) {
            return method("PUT", publisher);
        }

        /**
         * A convenience method for building an HTTP PATCH request with a body.
         *
         * @param publisher the body publisher
         * @return this builder
         */
        public Builder PATCH(final BodyPublisher publisher) {
            return method("PATCH", publisher);
        }

        /**
         * A convenience method for building an HTTP HEAD request.
         *
         * @return this builder
         */
        public Builder HEAD() {
            return method("HEAD", BodyPublishers.noBody());
        }

        /**
         * A convenience method for building an HTTP DELETE request.
         *
         * @return this builder
         */
        public Builder DELETE() {
            return method("DELETE", BodyPublishers.noBody());
        }

        /**
         * Build the {@link Request}.
         *
         * @return the request
         */
        public Request build() {
            // Set a default user agent
            requestHeaders.putIfAbsent("User-Agent",
                    Arrays.asList("InruptJavaClient/" + Request.class.getPackage().getImplementationVersion()));
            return new Request(requestUri, requestMethod, requestHeaders, publisher, requestTimeout);
        }

        Builder() {
            // Prevent direct instantiation.
        }
    }
}
