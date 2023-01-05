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
package com.inrupt.client.util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;

class IOUtilsTest {

    static final String INPUT_DOC = "/clarissa.txt";

    enum Speed { FAST, SLOW };

    @Test
    void testAsyncOutputPipe() throws IOException {
        try (final InputStream res = IOUtilsTest.class.getResourceAsStream(INPUT_DOC)) {
            final byte[] data = org.apache.commons.io.IOUtils.toByteArray(res);

            final InputStream sink = IOUtils.pipe(source -> produce(source, data, Speed.FAST));

            CompletableFuture.runAsync(() -> consume(sink, data, Speed.FAST)).join();
        }
    }

    @Test
    void testAsyncOutputPipeSlowReader() throws IOException {
        try (final InputStream res = IOUtilsTest.class.getResourceAsStream(INPUT_DOC)) {
            final byte[] data = org.apache.commons.io.IOUtils.toByteArray(res);

            final InputStream sink = IOUtils.pipe(source -> produce(source, data, Speed.FAST));

            CompletableFuture.runAsync(() -> consume(sink, data, Speed.SLOW)).join();
        }
    }

    @Test
    void testAsyncOutputPipeSlowWriter() throws IOException {
        try (final InputStream res = IOUtilsTest.class.getResourceAsStream(INPUT_DOC)) {
            final byte[] data = org.apache.commons.io.IOUtils.toByteArray(res);

            final InputStream sink = IOUtils.pipe(source -> produce(source, data, Speed.SLOW));

            CompletableFuture.runAsync(() -> consume(sink, data, Speed.FAST)).join();
        }
    }

    @Test
    void testSyncOutputPipe() throws IOException {
        try (final InputStream res = IOUtilsTest.class.getResourceAsStream(INPUT_DOC)) {
            final byte[] data = org.apache.commons.io.IOUtils.toByteArray(res);

            final InputStream sink = IOUtils.pipe(source -> produce(source, data, Speed.FAST));

            consume(sink, data, Speed.FAST);
        }
    }

    @Test
    void testSyncOutputPipeSlowReader() throws Exception {
        try (final InputStream res = IOUtilsTest.class.getResourceAsStream(INPUT_DOC)) {
            final byte[] data = org.apache.commons.io.IOUtils.toByteArray(res);

            final InputStream sink = IOUtils.pipe(source -> produce(source, data, Speed.FAST));

            consume(sink, data, Speed.SLOW);
        }
    }

    @Test
    void testSyncOutputPipeSlowWriter() throws Exception {
        try (final InputStream res = IOUtilsTest.class.getResourceAsStream(INPUT_DOC)) {
            final byte[] data = org.apache.commons.io.IOUtils.toByteArray(res);

            final InputStream sink = IOUtils.pipe(source -> produce(source, data, Speed.SLOW));

            consume(sink, data, Speed.FAST);
        }
    }

    void produce(final OutputStream out, final byte[] data, final Speed speed) {
        try {
            for (int i = 0; i < data.length; i++) {
                if (Speed.SLOW.equals(speed) && i % 100 == 0) {
                    // Simulate a slow producer
                    Thread.sleep(1);
                }
                out.write(data[i]);
            }
        } catch (final InterruptedException ex) {
            throw new RuntimeException(ex);
        } catch (final IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    void consume(final InputStream in, final byte[] data, final Speed speed) {
        try {
            for (int i = 0; i < data.length; i++) {
                if (Speed.SLOW.equals(speed) && i % 100 == 0) {
                    // Simulate a slow consumer
                    Thread.sleep(1);
                }
                assertEquals(data[i], in.read());
            }
        } catch (final InterruptedException ex) {
            throw new RuntimeException(ex);
        } catch (final IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
