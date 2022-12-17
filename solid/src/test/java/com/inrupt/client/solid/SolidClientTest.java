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
package com.inrupt.client.solid;

import static org.junit.jupiter.api.Assertions.*;

import com.inrupt.client.Session;
import com.inrupt.client.rdf.RDFNode;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SolidClientTest {

    private static final SolidMockHttpService mockHttpServer = new SolidMockHttpService();
    private static Map<String, String> config = new HashMap<>();
    private static final SolidClient client = SolidClient.getClient().session(Session.anonymous());

    @BeforeAll
    static void setup() {
        config.putAll(mockHttpServer.start());
    }

    @AfterAll
    static void teardown() {
        mockHttpServer.stop();
    }

    @Test
    void testGetPlaylist() throws IOException, InterruptedException {
        final URI uri = URI.create(config.get("solid_resource_uri") + "/playlist");
        final URI song1 = URI.create("https://library.test/12345/song1.mp3");
        final URI song2 = URI.create("https://library.test/12345/song2.mp3");

        final Playlist playlist = client.read(uri, Playlist.class).toCompletableFuture().join();

        assertEquals(uri, playlist.getIdentifier());
        assertEquals("My playlist", playlist.getTitle());
        assertEquals(2, playlist.getSongs().size());
        assertTrue(playlist.getSongs().contains(song1));
        assertTrue(playlist.getSongs().contains(song2));

        assertDoesNotThrow(client.create(URI.create(uri + "Create"), playlist).toCompletableFuture()::join);
        assertDoesNotThrow(client.update(URI.create(uri + "Update"), playlist).toCompletableFuture()::join);
        assertDoesNotThrow(client.delete(uri).toCompletableFuture()::join);
    }

    @Test
    void testGetResource() throws IOException, InterruptedException {
        final URI uri = URI.create(config.get("solid_resource_uri") + "/playlist");
        final URI song1 = URI.create("https://library.test/12345/song1.mp3");
        final URI song2 = URI.create("https://library.test/12345/song2.mp3");

        final SolidResource resource = client.read(uri, SolidResource.class).toCompletableFuture().join();

        assertEquals(uri, resource.getIdentifier());
        assertEquals(4, resource.getDataset().stream().count());
        assertEquals(2, resource.getDataset().stream(Optional.empty(), RDFNode.namedNode(uri),
                    RDFNode.namedNode(URI.create("https://example.com/song")), null).count());

        assertDoesNotThrow(client.create(URI.create(uri + "Create"), resource).toCompletableFuture()::join);
        assertDoesNotThrow(client.update(URI.create(uri + "Update"), resource).toCompletableFuture()::join);
        assertDoesNotThrow(client.delete(uri).toCompletableFuture()::join);
    }

    @Test
    void testGetContainer() throws IOException, InterruptedException {
        final URI uri = URI.create(config.get("solid_resource_uri") + "/playlist");
        final URI song1 = URI.create("https://library.test/12345/song1.mp3");
        final URI song2 = URI.create("https://library.test/12345/song2.mp3");

        final SolidContainer container = client.read(uri, SolidContainer.class).toCompletableFuture().join();

        assertEquals(uri, container.getIdentifier());
        assertEquals(0, container.getContainedResources().count());
        assertEquals(4, container.getDataset().stream().count());
        assertEquals(2, container.getDataset().stream(Optional.empty(), RDFNode.namedNode(uri),
                    RDFNode.namedNode(URI.create("https://example.com/song")), null).count());

        assertDoesNotThrow(client.create(URI.create(uri + "Create"), container).toCompletableFuture()::join);
        assertDoesNotThrow(client.update(URI.create(uri + "Update"), container).toCompletableFuture()::join);
        assertDoesNotThrow(client.delete(uri).toCompletableFuture()::join);
    }

    @Test
    void testGetInvalidType() {
        final URI uri = URI.create(config.get("solid_resource_uri") + "/playlist");
        final CompletionException err1 = assertThrows(CompletionException.class,
                client.read(uri, InvalidType.class).toCompletableFuture()::join);
        assertTrue(err1.getCause() instanceof SolidResourceException);

        final InvalidType type = new InvalidType(uri);
        assertThrows(SolidResourceException.class, () -> client.update(uri, type));
    }
}
