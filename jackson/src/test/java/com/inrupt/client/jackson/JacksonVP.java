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
package com.inrupt.client.jackson;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.inrupt.client.spi.JsonProcessor;
import com.inrupt.client.spi.ServiceProvider;
import com.inrupt.client.spi.VerifiablePresentation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.junit.jupiter.api.Test;

public class JacksonVP {

    private static final JsonProcessor processor = ServiceProvider.getJsonProcessor();
    private static VerifiablePresentation vp;
    private static VerifiablePresentation vpCopy;

    @Test
    void roundtripVP() throws IOException {
        try (final var res = JacksonVCs.class.getResourceAsStream("/verifiablePresentation.json")) {
            vp = processor.fromJson(res, VerifiablePresentation.class);
        }

        final Path target = new File("target").toPath();
        final String folderName = UUID.randomUUID().toString();
        final Path folderLocation = Files.createTempDirectory(target, folderName);
        final Path file = Files.createTempFile(folderLocation, UUID.randomUUID().toString(), ".json");

        try (OutputStream out = new FileOutputStream(file.toString())) {
            processor.toJson(vp, out);
        }

        try (final InputStream in = new FileInputStream(file.toString())) {
            vpCopy = processor.fromJson(in, VerifiablePresentation.class);
        }

        assertEquals(vp.context, vpCopy.context);
        assertEquals(vp.id, vpCopy.id);
        assertEquals(vp.type, vpCopy.type);
        assertEquals(vp.holder, vpCopy.holder);
        assertEquals(vp.verifiableCredential.size(), vpCopy.verifiableCredential.size());
        assertEquals(vp.proof, vpCopy.proof);

        Files.deleteIfExists(file);
        Files.deleteIfExists(folderLocation);

    }
}
