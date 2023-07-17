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
/**
 * <h2>Application interfaces for the Inrupt Java Client Libraries.</h2>
 *
 * <p>The Inrupt Java Client Libraries use a set of generic interfaces which help integrate with
 * some of the higher-level modules.
 *
 * <p>Working with an HTTP client is easier by implementing the {@link Client}. Then, to make use of the HTTP client,
 * which previously was loaded on the classpath, you can call the {@link ClientProvider}.
 *
 * <p>{@link Request} and {@link Response} classes help with interacting with an HTTP client. And {@link Headers}
 * helps parsing header values, including those often used with Solid, such as {@link Headers.Link}
 * or {@link Headers.WacAllow}.
 *
 * <p>Further, to work with HTTP resources as RDF-based resources, you can make use of the {@link Resource}
 * class.
 * {@link ValidationResult} can be of use when validation of the Solid resource is needed.
 *
 * <p>The {@link InruptClientException} provides a runtime exception used as a generic exception throughout the
 * Inrupt Java Client Libraries.
 *
 */
package com.inrupt.client;
