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
package com.inrupt.client.guava;

import com.google.common.cache.Cache;
import com.inrupt.client.ClientCache;

import java.util.Objects;

/**
 * A cache implementation using Guava.
 *
 * @param <T> the key type
 * @param <U> the value type
 */
public class GuavaCache<T, U> implements ClientCache<T, U> {

    private static final String KEY_NOT_NULL = "cache key may not be null!";
    private static final String VALUE_NOT_NULL = "cache value may not be null!";

    private final Cache<T, U> cache;

    /**
     * Wrap an existing guava {@link Cache}.
     *
     * @param cache the guava cache
     */
    public GuavaCache(final Cache<T, U> cache) {
        this.cache = Objects.requireNonNull(cache, KEY_NOT_NULL);
    }

    @Override
    public U get(final T key) {
        Objects.requireNonNull(key, KEY_NOT_NULL);
        return cache.getIfPresent(key);
    }

    @Override
    public void put(final T key, final U value) {
        Objects.requireNonNull(key, KEY_NOT_NULL);
        Objects.requireNonNull(value, VALUE_NOT_NULL);
        cache.put(key, value);
    }

    @Override
    public void invalidate(final T key) {
        Objects.requireNonNull(key, KEY_NOT_NULL);
        cache.invalidate(key);
    }

    @Override
    public void invalidateAll() {
        cache.invalidateAll();
    }
}
