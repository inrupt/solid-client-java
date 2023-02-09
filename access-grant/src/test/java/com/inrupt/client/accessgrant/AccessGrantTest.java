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
package com.inrupt.client.accessgrant;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

class AccessGrantTest {

    @Test
    void testReadAccessGrant() throws IOException {
        try (final InputStream resource = AccessGrantTest.class.getResourceAsStream("/access_grant1.json")) {
            final AccessGrant grant = AccessGrant.ofAccessGrant(resource);
            assertEquals(Collections.singleton("Read"), grant.getModes());
            assertEquals(URI.create("https://accessgrant.example"), grant.getIssuer());
            final Set<String> expectedTypes = new HashSet<>();
            expectedTypes.add("VerifiableCredential");
            expectedTypes.add("SolidAccessGrant");
            assertEquals(expectedTypes, grant.getTypes());
            assertEquals(Instant.parse("2022-08-27T12:00:00Z"), grant.getExpiration());
            assertEquals(URI.create("https://accessgrant.example/credential/5c6060ad-2f16-4bc1-b022-dffb46bff626"),
                    grant.getIdentifier());
            assertEquals(Collections.singleton("https://purpose.example/Purpose1"), grant.getPurpose());
            assertEquals(Collections.singleton(
                        URI.create("https://storage.example/e973cc3d-5c28-4a10-98c5-e40079289358/")),
                    grant.getResources());
            assertEquals(URI.create("https://id.example/grantor"), grant.getGrantor());
            assertEquals(URI.create("https://id.example/grantee"), grant.getGrantee());
        }
    }

    @Test
    void testReadAccessGrantSingletons() throws IOException {
        try (final InputStream resource = AccessGrantTest.class.getResourceAsStream("/access_grant2.json")) {
            final AccessGrant grant = AccessGrant.ofAccessGrant(resource);
            assertEquals(Collections.singleton("Read"), grant.getModes());
            assertEquals(URI.create("https://accessgrant.example"), grant.getIssuer());
            final Set<String> expectedTypes = new HashSet<>();
            expectedTypes.add("VerifiableCredential");
            expectedTypes.add("SolidAccessGrant");
            assertEquals(expectedTypes, grant.getTypes());
            assertEquals(Instant.parse("2022-08-27T12:00:00Z"), grant.getExpiration());
            assertEquals(URI.create("https://accessgrant.example/credential/5c6060ad-2f16-4bc1-b022-dffb46bff626"),
                    grant.getIdentifier());
            assertEquals(Collections.singleton("https://purpose.example/Purpose1"), grant.getPurpose());
            assertEquals(Collections.singleton(
                        URI.create("https://storage.example/e973cc3d-5c28-4a10-98c5-e40079289358/")),
                    grant.getResources());
            assertEquals(URI.create("https://id.example/grantor"), grant.getGrantor());
            assertEquals(URI.create("https://id.example/grantee"), grant.getGrantee());
        }
    }

    @Test
    void testRawAccessGrant() throws IOException {
        try (final InputStream resource = AccessGrantTest.class.getResourceAsStream("/access_grant1.json")) {
            final String raw = IOUtils.toString(resource);
            final AccessGrant grant = AccessGrant.ofAccessGrant(raw);

            assertEquals(raw, grant.getRawGrant());
        }
    }

    @Test
    void testBareAccessGrant() throws IOException {
        try (final InputStream resource = AccessGrantTest.class.getResourceAsStream("/invalid_access_grant1.json")) {
            assertThrows(IllegalArgumentException.class, () -> AccessGrant.ofAccessGrant(resource));
        }
    }

    @Test
    void testAccessGrantMissingIssuer() throws IOException {
        try (final InputStream resource = AccessGrantTest.class.getResourceAsStream("/invalid_access_grant2.json")) {
            assertThrows(IllegalArgumentException.class, () -> AccessGrant.ofAccessGrant(resource));
        }
    }

    @Test
    void testAccessGrantMissingCredentialSubject() throws IOException {
        try (final InputStream resource = AccessGrantTest.class.getResourceAsStream("/invalid_access_grant3.json")) {
            assertThrows(IllegalArgumentException.class, () -> AccessGrant.ofAccessGrant(resource));
        }
    }

    @Test
    void testAccessGrantMissingPresentationType() throws IOException {
        try (final InputStream resource = AccessGrantTest.class.getResourceAsStream("/invalid_access_grant4.json")) {
            assertThrows(IllegalArgumentException.class, () -> AccessGrant.ofAccessGrant(resource));
        }
    }

    @Test
    void testIrrelevantCredential() throws IOException {
        try (final InputStream resource = AccessGrantTest.class.getResourceAsStream("/invalid_access_grant5.json")) {
            assertThrows(IllegalArgumentException.class, () -> AccessGrant.ofAccessGrant(resource));
        }
    }

    @Test
    void testAccessGrantMissingId() throws IOException {
        try (final InputStream resource = AccessGrantTest.class.getResourceAsStream("/invalid_access_grant6.json")) {
            assertThrows(IllegalArgumentException.class, () -> AccessGrant.ofAccessGrant(resource));
        }
    }

    @Test
    void testAccessGrantMissingCredentialSubjectId() throws IOException {
        try (final InputStream resource = AccessGrantTest.class.getResourceAsStream("/invalid_access_grant7.json")) {
            assertThrows(IllegalArgumentException.class, () -> AccessGrant.ofAccessGrant(resource));
        }
    }

    @Test
    void testAccessGrantMissingConsent() throws IOException {
        try (final InputStream resource = AccessGrantTest.class.getResourceAsStream("/invalid_access_grant8.json")) {
            assertThrows(IllegalArgumentException.class, () -> AccessGrant.ofAccessGrant(resource));
        }
    }

    @Test
    void testAccessGrantTypeComplexListStructure() throws IOException {
        try (final InputStream resource = AccessGrantTest.class.getResourceAsStream("/invalid_access_grant9.json")) {
            assertThrows(IllegalArgumentException.class, () -> AccessGrant.ofAccessGrant(resource));
        }
    }

    @Test
    void testAccessGrantTypeObjectStructure() throws IOException {
        try (final InputStream resource = AccessGrantTest.class.getResourceAsStream("/invalid_access_grant10.json")) {
            assertThrows(IllegalArgumentException.class, () -> AccessGrant.ofAccessGrant(resource));
        }
    }

    @Test
    void testAccessGrantMissingGrantee() throws IOException {
        try (final InputStream resource = AccessGrantTest.class.getResourceAsStream("/invalid_access_grant11.json")) {
            assertThrows(IllegalArgumentException.class, () -> AccessGrant.ofAccessGrant(resource));
        }
    }

    @Test
    void testInvalidStream() throws IOException {
        final InputStream resource = AccessGrantTest.class.getResourceAsStream("/access_grant2.json");
        resource.close();
        assertThrows(IllegalArgumentException.class, () -> AccessGrant.ofAccessGrant(resource));
    }

    @Test
    void testInvalidString() throws IOException {
        assertThrows(IllegalArgumentException.class, () -> AccessGrant.ofAccessGrant("not json"));
    }
}
