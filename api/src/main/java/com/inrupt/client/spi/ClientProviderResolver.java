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

import com.inrupt.client.Client;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * An abstraction that loads the available {@link Client} from, the classpath.
 */
public abstract class ClientProviderResolver {

    private static ClientProviderResolver instance;

    /**
     * Get the {@link Client} for this application.
     *
     * @return the client
     */
    public abstract Client getClient();

    /**
     * Get a {@link Client.Builder} for this application.
     *
     * @return a client builder
     */
    public abstract Client.Builder getClientBuilder();

    /**
     * Find and return the provider resolver instance.
     *
     * @return the provider resolver instance
     */
    public static ClientProviderResolver getInstance() {
        if (instance == null) {
            synchronized (ClientProviderResolver.class) {
                if (instance != null) {
                    return instance;
                }
                instance = loadSpi(ClientProviderResolver.class.getClassLoader());
            }
        }

        return instance;
    }

    private static ClientProviderResolver loadSpi(final ClassLoader cl) {
        final ServiceLoader<ClientProviderResolver> loader = ServiceLoader.load(ClientProviderResolver.class, cl);
        final Iterator<ClientProviderResolver> iterator = loader.iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        }

        throw new IllegalStateException("No Client implementation found. " +
                "Please add the inrupt-core module or an alternative implementation");
    }
}

