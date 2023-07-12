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
package com.inrupt.client.webid;

import static org.junit.jupiter.api.Assertions.*;

import com.inrupt.client.solid.SolidSyncClient;

import java.net.URI;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class WebIdProfileTest {

    private static final URI AGENT = URI.create("http://xmlns.test/foaf/0.1/Agent");
    private static final URI SEE_ALSO = URI.create("https://storage.example.test/storage-id/extendedProfile");
    private static final URI ISSUER = URI.create("https://login.example.test");
    private static final URI STORAGE = URI.create("https://storage.example.test/storage-id/");

    private static final WebIdMockHttpService mockHttpServer = new WebIdMockHttpService();
    private static final SolidSyncClient client = SolidSyncClient.getClient();
    private static String baseUrl;

    @BeforeAll
    static void setup() {
        baseUrl = mockHttpServer.start();
    }

    @AfterAll
    static void teardown() {
        mockHttpServer.stop();
    }

    @Test
    void testGetProfileSlash() {
        final URI uri = URI.create(baseUrl + "/webId");
        try (final WebIdProfile profile = client.read(uri, WebIdProfile.class)) {
            assertEquals(uri, profile.getIdentifier());
            assertTrue(profile.getTypes().contains(AGENT));
            assertTrue(profile.getRelatedResources().contains(SEE_ALSO));
            assertTrue(profile.getOidcIssuers().contains(ISSUER));
            assertTrue(profile.getStorages().contains(STORAGE));
        }
    }

    @Test
    void testGetProfileHash() {
        final URI uri = URI.create(baseUrl + "/webIdHash#me");
        try (final WebIdProfile profile = client.read(uri, WebIdProfile.class)) {
            assertEquals(uri, profile.getIdentifier());
            assertTrue(profile.getTypes().contains(AGENT));
            assertTrue(profile.getRelatedResources().contains(SEE_ALSO));
            assertTrue(profile.getOidcIssuers().contains(ISSUER));
            assertTrue(profile.getStorages().contains(STORAGE));
        }
    }
}
