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
package com.inrupt.client.core;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;

class IOUtilsTest {

    @Test
    void testAsyncOutputPipe() throws IOException {
        try (final var res = IOUtilsTest.class.getResourceAsStream("/clarissa.txt")) {
            final var data = res.readAllBytes();

            final var sink = IOUtils.pipe(source -> {
                try {
                    for (var i = 0; i < data.length; i++) {
                        source.write(data[i]);
                    }
                } catch (final IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            });

            CompletableFuture.runAsync(() -> {
                try {
                    final var bytes = sink.readAllBytes();
                    assertEquals(data.length, bytes.length);
                    for (var i = 0; i < data.length; i++) {
                        assertEquals(data[i], bytes[i]);
                    }
                } catch (final IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }).join();
        }
    }

    @Test
    void testAsyncOutputPipeSlowerReader() throws IOException {
        try (final var res = IOUtilsTest.class.getResourceAsStream("/clarissa.txt")) {
            final var data = res.readAllBytes();

            final var sink = IOUtils.pipe(source -> {
                try {
                    for (var i = 0; i < data.length; i++) {
                        source.write(data[i]);
                    }
                } catch (final IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            });

            CompletableFuture.runAsync(() -> {
                try {
                    for (var i = 0; i < data.length; i++) {
                        assertEquals(data[i], sink.read());
                    }
                } catch (final IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }).join();
        }
    }

    @Test
    void testSyncOutputPipe() throws IOException {
        try (final var res = IOUtilsTest.class.getResourceAsStream("/clarissa.txt")) {
            final var data = res.readAllBytes();

            final var sink = IOUtils.pipe(source -> {
                try {
                    for (var i = 0; i < data.length; i++) {
                        source.write(data[i]);
                    }
                } catch (final IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            });

            final var bytes = sink.readAllBytes();
            assertEquals(data.length, bytes.length);
            for (var i = 0; i < data.length; i++) {
                assertEquals(data[i], bytes[i]);
            }
        }
    }

    @Test
    void testSyncOutputPipeSlowerReader() throws IOException {
        try (final var res = IOUtilsTest.class.getResourceAsStream("/clarissa.txt")) {
            final var data = res.readAllBytes();

            final var sink = IOUtils.pipe(source -> {
                try {
                    for (var i = 0; i < data.length; i++) {
                        source.write(data[i]);
                    }
                } catch (final IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            });

            for (var i = 0; i < data.length; i++) {
                assertEquals(data[i], sink.read());
            }
        }
    }
}
