/*
 * Copyright 2023 Inrupt Inc.
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
 * This is the class used to access data processors for the Inrupt Java Client Libraries libraries.
 */
public final class ServiceProvider {

    private static JsonService jsonService;
    private static RdfService rdfService;
    private static HttpService httpService;
    private static DpopService dpopService;
    private static HeaderParser headerParser;

    /**
     * Get the {@link JsonService} for this application.
     *
     * @return an object for processing JSON
     */
    public static JsonService getJsonService() {
        if (jsonService == null) {
            synchronized (ServiceProvider.class) {
                if (jsonService != null) {
                    return jsonService;
                }
                jsonService = loadSpi(JsonService.class, ServiceProvider.class.getClassLoader());
            }

        }
        return jsonService;
    }

    /**
     * Get the {@link RdfService} for this application.
     *
     * @return an object for processing RDF
     */
    public static RdfService getRdfService() {
        if (rdfService == null) {
            synchronized (ServiceProvider.class) {
                if (rdfService != null) {
                    return rdfService;
                }
                rdfService = loadSpi(RdfService.class, ServiceProvider.class.getClassLoader());
            }

        }
        return rdfService;
    }

    /**
     * Get the {@link HttpService} for this application.
     *
     * @return an object for processing HTTP
     */
    public static HttpService getHttpService() {
        if (httpService == null) {
            synchronized (ServiceProvider.class) {
                if (httpService != null) {
                    return httpService;
                }
                httpService = loadSpi(HttpService.class, ServiceProvider.class.getClassLoader());
            }

        }
        return httpService;
    }

    public static DpopService getDpopService() {
        if (dpopService == null) {
            synchronized (ServiceProvider.class) {
                if (dpopService != null) {
                    return dpopService;
                }
                dpopService = loadSpi(DpopService.class, ServiceProvider.class.getClassLoader());
            }
        }
        return dpopService;
    }

    public static HeaderParser getHeaderParser() {
        if (headerParser == null) {
            synchronized (ServiceProvider.class) {
                if (headerParser != null) {
                    return headerParser;
                }
                headerParser = loadSpi(HeaderParser.class, ServiceProvider.class.getClassLoader());
            }
        }
        return headerParser;
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
