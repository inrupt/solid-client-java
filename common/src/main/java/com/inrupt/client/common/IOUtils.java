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
package com.inrupt.client.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UncheckedIOException;
import java.util.function.Consumer;

/**
 * Input/Output utilities for use with the Inrupt client libraries.
 */
public final class IOUtils {

    /**
     * Pipe an output stream to a consumable input stream.
     *
     * @param function the output stream
     * @return a consumable input stream
     */
    public static InputStream pipe(final Consumer<OutputStream> function) {

        final var in = new PipedInputStream();
        final Runnable task = () -> {
            try (final var out = new PipedOutputStream(in)) {
                function.accept(out);
            } catch (final IOException ex) {
                throw new UncheckedIOException("Error piping data across threads", ex);
            }
        };

        new Thread(task).start();
        return in;
    }

    private IOUtils() {
        // Prevent instantiation
    }
}
