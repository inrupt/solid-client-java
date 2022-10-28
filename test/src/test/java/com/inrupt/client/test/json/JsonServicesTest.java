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
package com.inrupt.client.test.json;

import static org.junit.jupiter.api.Assertions.*;

import com.inrupt.client.jackson.JacksonService;
import com.inrupt.client.jsonb.JsonbService;
import com.inrupt.client.spi.JsonService;
import com.inrupt.client.spi.ServiceProvider;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class JsonServicesTest {

    @ParameterizedTest
    @MethodSource("jsonServices")
    void testInstance(final JsonService processor) {
        assertTrue(processor instanceof JacksonService || processor instanceof JsonbService);
    }

    @ParameterizedTest
    @MethodSource("jsonServices")
    void parseJsonInput(final JsonService processor) throws IOException {
        try (final var input = JsonServicesTest.class.getResourceAsStream("/json/myobject.json")) {
            final var obj = processor.fromJson(input, MyObject.class);
            assertEquals("Quinn", obj.name);
            assertEquals(25, obj.age);
            assertEquals(List.of("Dog", "Goldfish"), obj.pets);
        }
    }

    @ParameterizedTest
    @MethodSource("jsonServices")
    void serializeJsonObject(final JsonService processor) throws IOException {
        final var obj = new MyObject();
        obj.name = "Quinn";
        obj.age = 25;
        obj.pets = List.of("Dog", "Goldfish");

        try (final var output = new ByteArrayOutputStream()) {
            processor.toJson(obj, output);
            final var input = new ByteArrayInputStream(output.toByteArray());
            final var roundtrip = processor.fromJson(input, MyObject.class);
            assertEquals(obj.name, roundtrip.name);
            assertEquals(obj.age, roundtrip.age);
            assertEquals(obj.pets, roundtrip.pets);
        }
    }

    @ParameterizedTest
    @MethodSource("jsonServices")
    void serializeToClosedOutputStream(final JsonService processor) throws IOException {
        final var obj = new MyObject();
        obj.name = "Quinn";
        obj.age = 25;
        obj.pets = List.of("Dog", "Goldfish");

        final var tmp = Files.createTempFile(null, null).toFile();
        try (final var output = new FileOutputStream(tmp)) {
            output.close();
            assertThrows(IOException.class, () -> processor.toJson(obj, output));
        }
    }

    @ParameterizedTest
    @MethodSource("jsonServices")
    void parseInvalidJson(final JsonService processor) throws IOException {
        try (final var input = JsonServicesTest.class.getResourceAsStream("/json/invalid.json")) {
            assertThrows(IOException.class, () -> processor.fromJson(input, MyObject.class));
        }
    }

    @ParameterizedTest
    @MethodSource("jsonServices")
    void parseMalformedJson(final JsonService processor) throws IOException {
        try (final var input = JsonServicesTest.class.getResourceAsStream("/json/malformed.json")) {
            assertThrows(IOException.class, () -> processor.fromJson(input, MyObject.class));
        }
    }

    public static class MyObject {
        public String name;
        public List<String> pets;
        public Integer age;
    }

    private static Stream<Arguments> jsonServices() throws IOException {
        return Stream.of(Arguments.of(ServiceProvider.getJsonService()));
    }

}

