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
package com.inrupt.client.spi;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * This is the class used to access data processors for the Inrupt Client libraries.
 */
public final class ServiceProvider {

    private static volatile JsonProcessor jsonProcessor = null;
    private static volatile RdfProcessor rdfProcessor = null;
    private static volatile HttpProcessor httpProcessor = null;

    /**
     * Get the {@link JsonProcessor} for this application.
     *
     * @return an object for processing JSON
     */
    public static JsonProcessor getJsonProcessor() {
        if (jsonProcessor == null) {
            synchronized (ServiceProvider.class) {
                if (jsonProcessor != null) {
                    return jsonProcessor;
                }
                jsonProcessor = loadSpi(JsonProcessor.class, ServiceProvider.class.getClassLoader());
            }

        }
        return jsonProcessor;
    }

    /**
     * Get the {@link RdfProcessor} for this application.
     *
     * @return an object for processing RDF
     */
    public static RdfProcessor getRdfProcessor() {
        if (rdfProcessor == null) {
            synchronized (ServiceProvider.class) {
                if (rdfProcessor != null) {
                    return rdfProcessor;
                }
                rdfProcessor = loadSpi(RdfProcessor.class, ServiceProvider.class.getClassLoader());
            }

        }
        return rdfProcessor;
    }

    /**
     * Get the {@link HttpProcessor} for this application.
     *
     * @return an object for processing HTTP
     */
    public static HttpProcessor getHttpProcessor() {
        if (httpProcessor == null) {
            synchronized (ServiceProvider.class) {
                if (httpProcessor != null) {
                    return httpProcessor;
                }
                httpProcessor = loadSpi(HttpProcessor.class, ServiceProvider.class.getClassLoader());
            }

        }
        return httpProcessor;
    }

    static <T> T loadSpi(final Class<T> clazz, final ClassLoader cl) {
        final ServiceLoader<T> loader = ServiceLoader.load(clazz, cl);
        final Iterator<T> iterator = loader.iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        }
        throw new IllegalStateException("No implementation found for " + clazz.getName() + "! " +
                "Please add an implementation to the classpath");
    }

    private ServiceProvider() {
        // Prevent instantiation
    }
}
