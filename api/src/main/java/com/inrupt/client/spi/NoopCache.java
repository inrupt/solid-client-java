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

import com.inrupt.client.ClientCache;

import java.time.Duration;
import java.util.Objects;

/**
 * A no-op cache implementation.
 *
 * @param <T> the key type
 * @param <U> the value type
 */
class NoopCache<T, U> implements ClientCache<T, U> {

    @Override
    public U get(final T key) {
        Objects.requireNonNull(key, "cache key may not be null!");
        return null;
    }

    @Override
    public void put(final T key, final U value) {
        /* no-op */
        Objects.requireNonNull(key, "cache key may not be null!");
        Objects.requireNonNull(value, "cache value may not be null!");
    }

    @Override
    public void invalidate(final T key) {
        /* no-op */
        Objects.requireNonNull(key, "cache key may not be null!");
    }

    @Override
    public void invalidateAll() {
        /* no-op */
    }

    public static class NoopCacheBuilder implements CacheBuilderService {
        @Override
        public <T, U> ClientCache<T, U> build(final int size, final Duration duration) {
            return new NoopCache<T, U>();
        }
    }
}
