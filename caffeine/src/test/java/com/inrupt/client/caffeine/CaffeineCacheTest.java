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
package com.inrupt.client.caffeine;

import static org.junit.jupiter.api.Assertions.*;

import com.inrupt.client.ClientCache;
import com.inrupt.client.spi.CacheBuilderService;
import com.inrupt.client.spi.ServiceProvider;

import java.time.Duration;

import org.junit.jupiter.api.Test;

class CaffeineCacheTest {

    @Test
    void testServiceLoader() {

        final CacheBuilderService svc = ServiceProvider.getCacheBuilder();
        assertTrue(svc instanceof CaffeineCacheBuilder);
    }

    @Test
    void testCacheBuilder() {
        final CacheBuilderService svc = ServiceProvider.getCacheBuilder();
        final ClientCache<String, Integer> cache = svc.build(10, Duration.ofMinutes(5));

        cache.put("one", 1);
        cache.put("two", 2);
        cache.put("three", 3);

        assertNull(cache.get("zero"));
        assertEquals(1, cache.get("one"));
        assertEquals(2, cache.get("two"));
        assertEquals(3, cache.get("three"));

        cache.invalidate("one");
        assertNull(cache.get("one"));
        assertEquals(2, cache.get("two"));
        assertEquals(3, cache.get("three"));

        cache.invalidateAll();
        assertNull(cache.get("one"));
        assertNull(cache.get("two"));
        assertNull(cache.get("three"));
    }
}
