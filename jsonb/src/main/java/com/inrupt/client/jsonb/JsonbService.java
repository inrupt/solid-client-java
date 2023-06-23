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
package com.inrupt.client.jsonb;

import com.inrupt.client.spi.JsonService;

import jakarta.json.JsonException;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.json.bind.JsonbException;
import jakarta.json.bind.config.PropertyNamingStrategy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link JsonService} using the JakartaEE JSON Bind API.
 */
public class JsonbService implements JsonService {

    static final Logger LOGGER = LoggerFactory.getLogger(JsonbService.class);

    private final Jsonb jsonb;

    /**
     * Create a JSON-B service.
     */
    public JsonbService() {
        final JsonbConfig config = new JsonbConfig();
        config.withPropertyNamingStrategy(new LowerCamelCase());
        this.jsonb = JsonbBuilder.create(config);
    }

    @Override
    public <T> void toJson(final T object, final OutputStream output) throws IOException {
        try {
            jsonb.toJson(object, output);
        } catch (final JsonbException | JsonException ex) {
            throw new IOException("Error serializing JSON", ex);
        }
    }

    @Override
    public <T> T fromJson(final InputStream input, final Class<T> clazz) throws IOException {
        try {
            return jsonb.fromJson(input, clazz);
        } catch (final JsonbException | JsonException ex) {
            throw new IOException("Error parsing JSON", ex);
        }
    }

    @Override
    public <T> T fromJson(final InputStream input, final Type type) throws IOException {
        try {
            return jsonb.fromJson(input, type);
        } catch (final JsonbException | JsonException ex) {
            throw new IOException("Error parsing JSON", ex);
        }
    }

    static class LowerCamelCase implements PropertyNamingStrategy {
        @Override
        public String translateName(final String s) {
            final var wordBreaks = IntStream.range(0, s.length())
                    // Only keep indexes of upper case letters.
                    .filter(i -> Character.isUpperCase(s.charAt(i)))
                    .boxed()
                    .collect(Collectors.collectingAndThen(
                            Collectors.toList(),
                            // Insert _ in reverse order so that it's not necessary to keep track of the offset.
                            l -> {
                                Collections.reverse(l);
                                return l;
                            }
                    ));
            final StringBuilder result = new StringBuilder(s.toLowerCase());
            for (Integer breakIdx: wordBreaks) {
                result.insert(breakIdx, "_");
            }
            return result.toString();
        }
    }
}

