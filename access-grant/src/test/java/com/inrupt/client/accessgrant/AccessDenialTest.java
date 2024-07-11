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
package com.inrupt.client.accessgrant;

import static org.junit.jupiter.api.Assertions.*;

import com.inrupt.client.spi.JsonService;
import com.inrupt.client.spi.ServiceProvider;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

class AccessDenialTest {

    private static final JsonService jsonService = ServiceProvider.getJsonService();

    @Test
    void testReadAccessDenial() throws IOException {
        try (final InputStream resource = AccessDenialTest.class.getResourceAsStream("/access_denial1.json")) {
            final AccessDenial denial = AccessDenial.of(resource);
            assertEquals(Collections.singleton("Read"), denial.getModes());
            assertEquals(URI.create("https://accessgrant.test"), denial.getIssuer());
            final Set<String> expectedTypes = new HashSet<>();
            expectedTypes.add("VerifiableCredential");
            expectedTypes.add("SolidAccessDenial");
            assertEquals(expectedTypes, denial.getTypes());
            assertEquals(Instant.parse("2022-08-27T12:00:00Z"), denial.getExpiration());
            assertEquals(Instant.parse("2022-08-25T20:34:05.153Z"), denial.getIssuedAt());
            assertEquals(URI.create("https://accessgrant.test/credential/fc2dbcd9-81d4-4fa4-8fd4-239e16dd83ab"),
                    denial.getIdentifier());
            assertEquals(Collections.singleton(URI.create("https://purpose.test/Purpose1")), denial.getPurposes());
            assertEquals(Collections.singleton(
                        URI.create("https://storage.test/e973cc3d-5c28-4a10-98c5-e40079289358/")),
                    denial.getResources());
            assertEquals(URI.create("https://id.test/grantor"), denial.getCreator());
            assertEquals(Optional.of(URI.create("https://id.test/grantee")), denial.getRecipient());
            final Optional<Status> status = denial.getStatus();
            assertTrue(status.isPresent());
            status.ifPresent(s -> {
                assertEquals(URI.create("https://accessgrant.test/status/CVAM#2832"), s.getIdentifier());
                assertEquals(URI.create("https://accessgrant.test/status/CVAM"), s.getCredential());
                assertEquals(2832, s.getIndex());
                assertEquals("RevocationList2020Status", s.getType());
            });
        }
    }

    @Test
    void testReadAccessDenialQualifiedName() throws IOException {
        try (final InputStream resource = AccessDenialTest.class.getResourceAsStream("/access_denial2.json")) {
            final AccessDenial denial = AccessDenial.of(resource);
            assertEquals(Collections.singleton("Read"), denial.getModes());
            assertEquals(URI.create("https://accessgrant.test"), denial.getIssuer());
            final Set<String> expectedTypes = new HashSet<>();
            expectedTypes.add("VerifiableCredential");
            expectedTypes.add("vc:SolidAccessDenial");
            assertEquals(expectedTypes, denial.getTypes());
            assertEquals(Instant.parse("2022-08-27T12:00:00Z"), denial.getExpiration());
            assertEquals(Instant.parse("2022-08-25T20:34:05.153Z"), denial.getIssuedAt());
            assertEquals(URI.create("https://accessgrant.test/credential/39a4fdd4-44b0-48a5-a9b5-7a9b648e9a67"),
                    denial.getIdentifier());
            assertEquals(Collections.singleton(URI.create("https://purpose.test/Purpose1")), denial.getPurposes());
            assertEquals(Collections.singleton(
                        URI.create("https://storage.test/d5ef4173-4f12-40b2-9a0e-18fa7cc0dd38/")),
                    denial.getResources());
            assertEquals(URI.create("https://id.test/grantor"), denial.getCreator());
            assertEquals(Optional.of(URI.create("https://id.test/grantee")), denial.getRecipient());
            final Optional<Status> status = denial.getStatus();
            assertTrue(status.isPresent());
            status.ifPresent(s -> {
                assertEquals(URI.create("https://accessgrant.test/status/CVAM#2832"), s.getIdentifier());
                assertEquals(URI.create("https://accessgrant.test/status/CVAM"), s.getCredential());
                assertEquals(2832, s.getIndex());
                assertEquals("RevocationList2020Status", s.getType());
            });
        }
    }
}
