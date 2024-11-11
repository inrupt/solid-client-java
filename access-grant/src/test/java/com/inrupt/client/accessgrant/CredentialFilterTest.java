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

import com.inrupt.client.util.URIBuilder;

import java.net.URI;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class CredentialFilterTest {

    private static final URI ALICE = URI.create("https://agent.test/alice");
    private static final URI BOB = URI.create("https://agent.test/bob");
    private static final URI PURPOSE = URI.create("https://purpose.test/Purpose");
    private static final URI RESOURCE = URI.create("https://storage.test/data/");
    private static final URI BASE_URL = URI.create("https://credential.test/query");

    @Test
    void testCredentialEmptyFilterBuilder() {
        final CredentialFilter<AccessGrant> filter = CredentialFilter.newBuilder().build(AccessGrant.class);
        assertFalse(filter.getPage().isPresent());
        assertFalse(filter.getFromAgent().isPresent());
        assertFalse(filter.getToAgent().isPresent());
        assertFalse(filter.getPurpose().isPresent());
        assertFalse(filter.getResource().isPresent());
        assertFalse(filter.getStatus().isPresent());
        assertFalse(filter.getIssuedWithin().isPresent());
        assertFalse(filter.getRevokedWithin().isPresent());
        assertEquals(20, filter.getPageSize());
        assertEquals(AccessGrant.class, filter.getCredentialType());

        final URI expectedUri = URIBuilder.newBuilder(BASE_URL)
            .queryParam("type", "SolidAccessGrant")
            .queryParam("pageSize", "20").build();
        assertEquals(expectedUri, filter.asURI(BASE_URL));
    }

    @Test
    void testCredentialInvalidPageSizeFilterBuilder() {
        final CredentialFilter<AccessDenial> filter = CredentialFilter.newBuilder()
            .pageSize(-10)
            .build(AccessDenial.class);
        assertEquals(20, filter.getPageSize());
        assertEquals(AccessDenial.class, filter.getCredentialType());

        final URI expectedUri = URIBuilder.newBuilder(BASE_URL)
            .queryParam("type", "SolidAccessDenial")
            .queryParam("pageSize", "20").build();
        assertEquals(expectedUri, filter.asURI(BASE_URL));
    }

    @Test
    void testCredentialExcessivePageSizeFilterBuilder() {
        final CredentialFilter<AccessDenial> filter = CredentialFilter.newBuilder()
            .pageSize(200)
            .build(AccessDenial.class);
        assertEquals(20, filter.getPageSize());
        assertEquals(AccessDenial.class, filter.getCredentialType());

        final URI expectedUri = URIBuilder.newBuilder(BASE_URL)
            .queryParam("type", "SolidAccessDenial")
            .queryParam("pageSize", "20").build();
        assertEquals(expectedUri, filter.asURI(BASE_URL));
    }

    @Test
    void testCredentialFilterBuilder() {
        final String page = UUID.randomUUID().toString();
        final CredentialFilter.CredentialStatus status = CredentialFilter.CredentialStatus.PENDING;
        final CredentialFilter.CredentialDuration issuedWithin = CredentialFilter.CredentialDuration.P1D;
        final CredentialFilter.CredentialDuration revokedWithin = CredentialFilter.CredentialDuration.P7D;

        final CredentialFilter<AccessRequest> filter = CredentialFilter.newBuilder()
            .status(status)
            .fromAgent(ALICE)
            .toAgent(BOB)
            .purpose(PURPOSE)
            .resource(RESOURCE)
            .issuedWithin(issuedWithin)
            .revokedWithin(revokedWithin)
            .page(page)
            .pageSize(40)
            .build(AccessRequest.class);

        assertEquals(Optional.of(ALICE), filter.getFromAgent());
        assertEquals(Optional.of(BOB), filter.getToAgent());
        assertEquals(Optional.of(PURPOSE), filter.getPurpose());
        assertEquals(Optional.of(RESOURCE), filter.getResource());
        assertEquals(Optional.of(status), filter.getStatus());
        assertEquals(Optional.of(issuedWithin), filter.getIssuedWithin());
        assertEquals(Optional.of(revokedWithin), filter.getRevokedWithin());
        assertEquals(Optional.of(page), filter.getPage());
        assertEquals(40, filter.getPageSize());
        assertEquals(AccessRequest.class, filter.getCredentialType());

        final URI expectedUri = URIBuilder.newBuilder(BASE_URL)
            .queryParam("type", "SolidAccessRequest")
            .queryParam("pageSize", "40")
            .queryParam("purpose", PURPOSE.toString())
            .queryParam("resource", RESOURCE.toString())
            .queryParam("fromAgent", ALICE.toString())
            .queryParam("toAgent", BOB.toString())
            .queryParam("status", status.getValue())
            .queryParam("issuedWithin", issuedWithin.name())
            .queryParam("revokedWithin", revokedWithin.name())
            .queryParam("page", page)
            .build();
        assertEquals(expectedUri, filter.asURI(BASE_URL));
    }

    @Test
    void testCredentialFilterAmendedBuilder() {
        final String page = UUID.randomUUID().toString();
        final CredentialFilter.CredentialStatus status = CredentialFilter.CredentialStatus.PENDING;
        final CredentialFilter.CredentialDuration issuedWithin = CredentialFilter.CredentialDuration.P1D;
        final CredentialFilter.CredentialDuration revokedWithin = CredentialFilter.CredentialDuration.P7D;

        final CredentialFilter<AccessRequest> filter1 = CredentialFilter.newBuilder()
            .status(status)
            .fromAgent(BOB)
            .toAgent(ALICE)
            .purpose(PURPOSE)
            .resource(RESOURCE)
            .issuedWithin(issuedWithin)
            .revokedWithin(revokedWithin)
            .page(page)
            .pageSize(10)
            .build(AccessRequest.class);

        final String page2 = UUID.randomUUID().toString();
        final CredentialFilter<AccessRequest> filter2 = CredentialFilter.newBuilder(filter1)
            .page(page2)
            .build(AccessRequest.class);

        assertEquals(Optional.of(BOB), filter2.getFromAgent());
        assertEquals(Optional.of(ALICE), filter2.getToAgent());
        assertEquals(Optional.of(PURPOSE), filter2.getPurpose());
        assertEquals(Optional.of(RESOURCE), filter2.getResource());
        assertEquals(Optional.of(status), filter2.getStatus());
        assertEquals(Optional.of(issuedWithin), filter2.getIssuedWithin());
        assertEquals(Optional.of(revokedWithin), filter2.getRevokedWithin());
        assertEquals(Optional.of(page2), filter2.getPage());
        assertEquals(10, filter2.getPageSize());
        assertEquals(AccessRequest.class, filter2.getCredentialType());

        final URI expectedUri = URIBuilder.newBuilder(BASE_URL)
            .queryParam("type", "SolidAccessRequest")
            .queryParam("pageSize", "10")
            .queryParam("purpose", PURPOSE.toString())
            .queryParam("resource", RESOURCE.toString())
            .queryParam("fromAgent", BOB.toString())
            .queryParam("toAgent", ALICE.toString())
            .queryParam("status", status.getValue())
            .queryParam("issuedWithin", issuedWithin.name())
            .queryParam("revokedWithin", revokedWithin.name())
            .queryParam("page", page2)
            .build();
        assertEquals(expectedUri, filter2.asURI(BASE_URL));
    }

    @Test
    void testDurations() {
        assertEquals(Duration.ofDays(1), CredentialFilter.CredentialDuration.P1D.asDuration());
        assertEquals(Duration.ofDays(7), CredentialFilter.CredentialDuration.P7D.asDuration());
        assertEquals(Duration.ofDays(30), CredentialFilter.CredentialDuration.P1M.asDuration());
        assertEquals(Duration.ofDays(90), CredentialFilter.CredentialDuration.P3M.asDuration());
    }
}
