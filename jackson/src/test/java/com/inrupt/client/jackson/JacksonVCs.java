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
import com.inrupt.client.spi.VerifiableCredential;

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

public class JacksonVCs {

    private static final JsonProcessor processor = ServiceProvider.getJsonProcessor();
    private static VerifiableCredential vc;
    private static VerifiableCredential vcCopy;

    @Test
    void roundtripVC() throws IOException {
        try (final var res = JacksonVCs.class.getResourceAsStream("/verifiableCredential.json")) {
            vc = processor.fromJson(res, VerifiableCredential.class);
        }

        final Path target = new File("target").toPath();
        final String folderName = UUID.randomUUID().toString();
        final Path folderLocation = Files.createTempDirectory(target, folderName);
        final Path file = Files.createTempFile(folderLocation, UUID.randomUUID().toString(), ".json");

        try (OutputStream out = new FileOutputStream(file.toString())) {
            processor.toJson(vc, out);
        }

        try (final InputStream in = new FileInputStream(file.toString())) {
            vcCopy = processor.fromJson(in, VerifiableCredential.class);
        }

        assertEquals(vc.context, vcCopy.context);
        assertEquals(vc.id, vcCopy.id);
        assertEquals(vc.type, vcCopy.type);
        assertEquals(vc.issuer, vcCopy.issuer);
        assertEquals(vc.issuanceDate, vcCopy.issuanceDate);
        assertEquals(vc.expirationDate, vcCopy.expirationDate);
        assertEquals(vc.credentialSubject, vcCopy.credentialSubject);
        assertEquals(vc.credentialStatus, vcCopy.credentialStatus);
        assertEquals(vc.proof, vcCopy.proof);

        Files.deleteIfExists(file);
        Files.deleteIfExists(folderLocation);

    }
}
