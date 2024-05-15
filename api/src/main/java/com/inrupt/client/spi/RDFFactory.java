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
package com.inrupt.client.spi;

import java.util.Iterator;
import java.util.ServiceLoader;

import org.apache.commons.rdf.api.RDF;

/**
 * A RDF commons handling abstraction.
 */
public final class RDFFactory {

    private static RDF instance;

    /**
     * Find and return the RDF instance.
     *
     * @return the RDF instance
     */
    public static RDF getInstance() {
        if (instance == null) {
            synchronized (RDFFactory.class) {
                if (instance != null) {
                    return instance;
                }
                instance = loadSpi(RDFFactory.class.getClassLoader());
            }
        }
        return instance;
    }

    private static RDF loadSpi(final ClassLoader cl) {
        final ServiceLoader<RDF> loader = ServiceLoader.load(RDF.class, cl);
        final Iterator<RDF> iterator = loader.iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        }
        throw new IllegalStateException("No RDF Commons implementation available");
    }

    private RDFFactory() {
    }
}
