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
package com.inrupt.client.jsonb;

import com.inrupt.client.spi.JsonProcessor;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

/**
 * A {@link JsonProcessor} using the JakartaEE JSON Bind API.
 */
public class JsonbProcessor implements JsonProcessor {

    private final Jsonb jsonb;

    /**
     * Create a JSON-B processor with the default {@link Jsonb} builder.
     */
    public JsonbProcessor() {
        this(JsonbBuilder.create());
    }

    /**
     * Create a JSON-B processor with the provided {@link Jsonb} object.
     *
     * @param jsonb the JSON-B object
     */
    public JsonbProcessor(final Jsonb jsonb) {
        this.jsonb = Objects.requireNonNull(jsonb);
    }

    @Override
    public <T> void toJson(final T object, final OutputStream output) {
        jsonb.toJson(object, output);
    }

    @Override
    public <T> T fromJson(final InputStream input, final Class<T> clazz) {
        return jsonb.fromJson(input, clazz);
    }
}

