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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

import com.inrupt.client.spi.JsonService;
import com.inrupt.client.spi.ServiceProvider;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

class AccessGrantTest {

    private static final JsonService jsonService = ServiceProvider.getJsonService();

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
            assertEquals(Collections.singleton("https://purpose.example/Purpose1"), grant.getPurposes());
            assertEquals(Collections.singleton(
                        URI.create("https://storage.example/e973cc3d-5c28-4a10-98c5-e40079289358/")),
                    grant.getResources());
            assertEquals(URI.create("https://id.example/grantor"), grant.getGrantor());
            assertEquals(Optional.of(URI.create("https://id.example/grantee")), grant.getGrantee());
            final Optional<Status> status = grant.getStatus();
            assertTrue(status.isPresent());
            status.ifPresent(s -> {
                assertEquals(URI.create("https://accessgrant.example/status/CVAM#2832"), s.getIdentifier());
                assertEquals(URI.create("https://accessgrant.example/status/CVAM"), s.getCredential());
                assertEquals(2832, s.getIndex());
                assertEquals("RevocationList2020Status", s.getType());
            });
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
            assertEquals(Collections.singleton("https://purpose.example/Purpose1"), grant.getPurposes());
            assertEquals(Collections.singleton(
                        URI.create("https://storage.example/e973cc3d-5c28-4a10-98c5-e40079289358/")),
                    grant.getResources());
            assertEquals(URI.create("https://id.example/grantor"), grant.getGrantor());
            assertEquals(Optional.of(URI.create("https://id.example/grantee")), grant.getGrantee());
            final Optional<Status> status = grant.getStatus();
            assertFalse(status.isPresent());
        }
    }

    @Test
    void testRawAccessGrant() throws IOException {
        try (final InputStream resource = AccessGrantTest.class.getResourceAsStream("/access_grant1.json")) {
            final String raw = IOUtils.toString(resource, UTF_8);
            final AccessGrant grant = AccessGrant.ofAccessGrant(raw);

            assertEquals(raw, grant.getRawGrant());
        }
    }

    @Test
    void testRevocationList2020() throws IOException {
        try (final InputStream resource = AccessGrantTest.class.getResourceAsStream("/status_list1.json")) {
            final Map<String, Object> data = jsonService.fromJson(resource,
                new HashMap<String, Object>(){}.getClass().getGenericSuperclass());
            final Status status = AccessGrant.asRevocationList2020(data);
            assertEquals(URI.create("https://accessgrant.example/status/CVAM#2832"), status.getIdentifier());
            assertEquals(URI.create("https://accessgrant.example/status/CVAM"), status.getCredential());
            assertEquals(2832, status.getIndex());
        }
    }

    @Test
    void testRevocationList2020Integer() throws IOException {
        try (final InputStream resource = AccessGrantTest.class.getResourceAsStream("/status_list2.json")) {
            final Map<String, Object> data = jsonService.fromJson(resource,
                new HashMap<String, Object>(){}.getClass().getGenericSuperclass());
            final Status status = AccessGrant.asRevocationList2020(data);
            assertEquals(URI.create("https://accessgrant.example/status/CVAM#2832"), status.getIdentifier());
            assertEquals(URI.create("https://accessgrant.example/status/CVAM"), status.getCredential());
            assertEquals(2832, status.getIndex());
        }
    }

    @Test
    void testRevocationList2020List() throws IOException {
        try (final InputStream resource = AccessGrantTest.class.getResourceAsStream("/status_list3.json")) {
            final Map<String, Object> data = jsonService.fromJson(resource,
                new HashMap<String, Object>(){}.getClass().getGenericSuperclass());
            assertThrows(IllegalArgumentException.class, () -> AccessGrant.asRevocationList2020(data));
        }
    }

    @Test
    void testRevocationList2020IdNotString() throws IOException {
        try (final InputStream resource = AccessGrantTest.class.getResourceAsStream("/status_list4.json")) {
            final Map<String, Object> data = jsonService.fromJson(resource,
                new HashMap<String, Object>(){}.getClass().getGenericSuperclass());
            assertThrows(IllegalArgumentException.class, () -> AccessGrant.asRevocationList2020(data));
        }
    }

    @Test
    void testRevocationList2020CredentialNotString() throws IOException {
        try (final InputStream resource = AccessGrantTest.class.getResourceAsStream("/status_list5.json")) {
            final Map<String, Object> data = jsonService.fromJson(resource,
                new HashMap<String, Object>(){}.getClass().getGenericSuperclass());
            assertThrows(IllegalArgumentException.class, () -> AccessGrant.asRevocationList2020(data));
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
    void testAccessGrantInvalidStatusNoId() throws IOException {
        try (final InputStream resource = AccessGrantTest.class.getResourceAsStream("/invalid_access_grant12.json")) {
            assertThrows(IllegalArgumentException.class, () -> AccessGrant.ofAccessGrant(resource));
        }
    }

    @Test
    void testAccessGrantInvalidStatusBadCredential() throws IOException {
        try (final InputStream resource = AccessGrantTest.class.getResourceAsStream("/invalid_access_grant13.json")) {
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
