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

import static java.nio.charset.StandardCharsets.UTF_8;
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

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

class AccessRequestTest {

    private static final JsonService jsonService = ServiceProvider.getJsonService();

    @Test
    void testBuilderWithNulls() {
        final URI uri1 = URI.create("https://example.com/resource1");
        final URI uri2 = URI.create("https://example.com/resource2");
        final URI uri3 = URI.create("https://example.com/resource3");
        final URI uri4 = URI.create("https://example.com/resource4");
        final AccessRequest.RequestParameters params = AccessRequest.RequestParameters.newBuilder()
            .resource(uri1).resource(uri2)
            .mode("Read").mode("Append")
            .purpose(uri3).purpose(uri4)
            .recipient(null)
            .modes(null)
            .resources(null)
            .purposes(null).build();

        assertTrue(params.getPurposes().isEmpty());
        assertTrue(params.getResources().isEmpty());
        assertTrue(params.getModes().isEmpty());
        assertNull(params.getRecipient());
        assertNull(params.getExpiration());
        assertNull(params.getIssuedAt());
    }

    @Test
    void testBuilderWithCollections() {
        final URI uri1 = URI.create("https://example.com/resource1");
        final URI uri2 = URI.create("https://example.com/resource2");
        final URI uri3 = URI.create("https://example.com/resource3");
        final URI uri4 = URI.create("https://example.com/resource4");
        final AccessRequest.RequestParameters params = AccessRequest.RequestParameters.newBuilder()
            .resource(uri1)
            .mode("Read")
            .purpose(uri3)
            .recipient(uri2)
            .modes(Collections.singleton("Append"))
            .resources(Collections.singleton(uri2))
            .purposes(Collections.singleton(uri4)).build();

        final Set<URI> expectedPurposes = new HashSet<>();
        expectedPurposes.add(uri3);
        expectedPurposes.add(uri4);
        assertEquals(expectedPurposes, params.getPurposes());

        final Set<URI> expectedResources = new HashSet<>();
        expectedResources.add(uri1);
        expectedResources.add(uri2);
        assertEquals(expectedResources, params.getResources());

        final Set<String> expectedModes = new HashSet<>();
        expectedModes.add("Read");
        expectedModes.add("Append");
        assertEquals(expectedModes, params.getModes());

        assertEquals(uri2, params.getRecipient());
        assertNull(params.getExpiration());
        assertNull(params.getIssuedAt());
    }


    @Test
    void testReadAccessRequest() throws IOException {
        try (final InputStream resource = AccessRequestTest.class.getResourceAsStream("/access_request1.json")) {
            final AccessRequest request = AccessRequest.of(resource);
            assertEquals(Collections.singleton("Read"), request.getModes());
            assertEquals(URI.create("https://accessgrant.test"), request.getIssuer());
            final Set<String> expectedTypes = new HashSet<>();
            expectedTypes.add("VerifiableCredential");
            expectedTypes.add("SolidAccessRequest");
            assertEquals(expectedTypes, request.getTypes());
            assertEquals(Instant.parse("2022-08-27T12:00:00Z"), request.getExpiration());
            assertEquals(Instant.parse("2022-08-25T20:34:05.153Z"), request.getIssuedAt());
            assertEquals(URI.create("https://accessgrant.test/credential/d604c858-209a-4bb6-a7f8-2f52c9617cab"),
                    request.getIdentifier());
            assertEquals(Collections.singleton(URI.create("https://purpose.test/Purpose1")), request.getPurposes());
            assertEquals(Collections.singleton(
                        URI.create("https://storage.test/data/")),
                    request.getResources());
            assertEquals(URI.create("https://id.test/username"), request.getCreator());
            assertEquals(Optional.of(URI.create("https://id.test/agent")), request.getRecipient());
            final Optional<Status> status = request.getStatus();
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
    void testReadAccessRequestSingletons() throws IOException {
        try (final InputStream resource = AccessRequestTest.class.getResourceAsStream("/access_request2.json")) {
            final AccessRequest request = AccessRequest.of(resource);
            assertEquals(Collections.singleton("Read"), request.getModes());
            assertEquals(URI.create("https://accessgrant.test"), request.getIssuer());
            final Set<String> expectedTypes = new HashSet<>();
            expectedTypes.add("VerifiableCredential");
            expectedTypes.add("SolidAccessRequest");
            assertEquals(expectedTypes, request.getTypes());
            assertEquals(Instant.parse("2022-08-27T12:00:00Z"), request.getExpiration());
            assertEquals(Instant.parse("2022-08-25T20:34:05.153Z"), request.getIssuedAt());
            assertEquals(URI.create("https://accessgrant.test/credential/d604c858-209a-4bb6-a7f8-2f52c9617cab"),
                    request.getIdentifier());
            assertEquals(Collections.singleton(URI.create("https://purpose.test/Purpose1")), request.getPurposes());
            assertEquals(Collections.singleton(
                        URI.create("https://storage.test/e973cc3d-5c28-4a10-98c5-e40079289358/")),
                    request.getResources());
            assertEquals(URI.create("https://id.test/username"), request.getCreator());
            assertEquals(Optional.of(URI.create("https://id.test/agent")), request.getRecipient());
            final Optional<Status> status = request.getStatus();
            assertFalse(status.isPresent());
        }
    }

    @Test
    void testRawAccessRequest() throws IOException {
        try (final InputStream resource = AccessRequestTest.class.getResourceAsStream("/access_request1.json")) {
            final String raw = IOUtils.toString(resource, UTF_8);
            final AccessRequest request = AccessRequest.of(raw);

            assertEquals(raw, request.serialize());
        }
    }

    @Test
    void testMissingAccessRequest() throws IOException {
        try (final InputStream resource = AccessRequestTest.class
                .getResourceAsStream("/access_grant1.json")) {
            assertThrows(IllegalArgumentException.class, () -> AccessRequest.of(resource));
        }
    }

    @Test
    void testBareAccessRequest() throws IOException {
        try (final InputStream resource = AccessRequestTest.class
                .getResourceAsStream("/invalid_access_request1.json")) {
            assertThrows(IllegalArgumentException.class, () -> AccessRequest.of(resource));
        }
    }

    @Test
    void testAccessRequestMissingIssuer() throws IOException {
        try (final InputStream resource = AccessRequestTest.class
                .getResourceAsStream("/invalid_access_request2.json")) {
            assertThrows(IllegalArgumentException.class, () -> AccessRequest.of(resource));
        }
    }

    @Test
    void testAccessRequestMissingCredentialSubject() throws IOException {
        try (final InputStream resource = AccessRequestTest.class
                .getResourceAsStream("/invalid_access_request3.json")) {
            assertThrows(IllegalArgumentException.class, () -> AccessRequest.of(resource));
        }
    }

    @Test
    void testAccessRequestMissingPresentationType() throws IOException {
        try (final InputStream resource = AccessRequestTest.class
                .getResourceAsStream("/invalid_access_request4.json")) {
            assertThrows(IllegalArgumentException.class, () -> AccessRequest.of(resource));
        }
    }

    @Test
    void testAccessRequestMissingId() throws IOException {
        try (final InputStream resource = AccessRequestTest.class
                .getResourceAsStream("/invalid_access_request5.json")) {
            assertThrows(IllegalArgumentException.class, () -> AccessRequest.of(resource));
        }
    }

    @Test
    void testAccessRequestMissingCredentialSubjectId() throws IOException {
        try (final InputStream resource = AccessRequestTest.class
                .getResourceAsStream("/invalid_access_request6.json")) {
            assertThrows(IllegalArgumentException.class, () -> AccessRequest.of(resource));
        }
    }

    @Test
    void testAccessRequestMissingConsent() throws IOException {
        try (final InputStream resource = AccessRequestTest.class
                .getResourceAsStream("/invalid_access_request7.json")) {
            assertThrows(IllegalArgumentException.class, () -> AccessRequest.of(resource));
        }
    }

    @Test
    void testInvalidStream() throws IOException {
        final InputStream resource = AccessRequestTest.class.getResourceAsStream("/access_request2.json");
        resource.close();
        assertThrows(IllegalArgumentException.class, () -> AccessRequest.of(resource));
    }

    @Test
    void testInvalidString() throws IOException {
        assertThrows(IllegalArgumentException.class, () -> AccessRequest.of("not json"));
    }
}
