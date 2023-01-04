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
package com.inrupt.client.solid;

import static org.junit.jupiter.api.Assertions.*;

import com.inrupt.client.Request;
import com.inrupt.client.Response;
import com.inrupt.client.Session;
import com.inrupt.client.spi.RDFFactory;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.rdf.api.RDF;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SolidSyncClientTest {

    private static final SolidMockHttpService mockHttpServer = new SolidMockHttpService();
    private static final Map<String, String> config = new HashMap<>();
    private static final RDF rdf = RDFFactory.getInstance();
    private static final SolidSyncClient client = SolidSyncClient.getClient().session(Session.anonymous());

    @BeforeAll
    static void setup() {
        config.putAll(mockHttpServer.start());
    }

    @AfterAll
    static void teardown() {
        mockHttpServer.stop();
    }

    @Test
    void testGetPlaylist() {
        final URI uri = URI.create(config.get("solid_resource_uri") + "/playlist");
        final URI song1 = URI.create("https://library.test/12345/song1.mp3");
        final URI song2 = URI.create("https://library.test/12345/song2.mp3");

        try (final Playlist playlist = client.read(uri, Playlist.class)) {
            assertEquals(uri, playlist.getIdentifier());
            assertEquals("My playlist", playlist.getTitle());
            assertEquals(2, playlist.getSongs().size());
            assertTrue(playlist.getSongs().contains(song1));
            assertTrue(playlist.getSongs().contains(song2));

            assertDoesNotThrow(() -> client.create(playlist));
            assertDoesNotThrow(() -> client.update(playlist));
            assertDoesNotThrow(() -> client.delete(playlist));
        }
    }

    @Test
    void testGetTextResource() {
        final URI uri = URI.create(config.get("solid_resource_uri") + "/nonRDF");

        final Response<String> res = client.send(Request.newBuilder(uri).GET().build(),
                Response.BodyHandlers.ofString());
        assertEquals(200, res.statusCode());
        assertEquals("This isn't valid turtle.", res.body());
    }

    @Test
    void testGetResource() {
        final URI uri = URI.create(config.get("solid_resource_uri") + "/playlist");

        try (final SolidResource resource = client.read(uri, SolidResource.class)) {
            assertEquals(uri, resource.getIdentifier());
            assertEquals(4, resource.getDataset().stream().count());
            assertEquals(2, resource.getDataset().stream(Optional.empty(), rdf.createIRI(uri.toString()),
                        rdf.createIRI("https://example.com/song"), null).count());

            assertDoesNotThrow(() -> client.create(resource));
            assertDoesNotThrow(() -> client.update(resource));
            assertDoesNotThrow(() -> client.delete(resource));
        }
    }

    @Test
    void testGetContainer() {
        final URI uri = URI.create(config.get("solid_resource_uri") + "/playlist");

        try (final SolidContainer container = client.read(uri, SolidContainer.class)) {
            assertEquals(uri, container.getIdentifier());
            assertEquals(0, container.getContainedResources().count());
            assertEquals(4, container.getDataset().stream().count());
            assertEquals(2, container.getDataset().stream(Optional.empty(), rdf.createIRI(uri.toString()),
                        rdf.createIRI("https://example.com/song"), null).count());

            assertDoesNotThrow(() -> client.update(container));
            assertDoesNotThrow(() -> client.create(container));
            assertDoesNotThrow(() -> client.delete(container));
        }
    }

    @Test
    void testGetInvalidType() {
        final URI uri = URI.create(config.get("solid_resource_uri") + "/playlist");
        assertThrows(SolidResourceException.class, () -> client.read(uri, InvalidType.class));

        final InvalidType type = new InvalidType(uri);
        assertThrows(SolidResourceException.class, () -> client.update(type));
    }

    @Test
    void testGetRecipeType() {
        final URI uri = URI.create(config.get("solid_resource_uri") + "/recipe");

        try (final Recipe recipe = client.read(uri, Recipe.class)) {
            assertEquals(uri, recipe.getIdentifier());
            assertEquals("Molasses Cookies", recipe.getTitle());
            assertEquals(11, recipe.getIngredients().size());
            assertEquals(7, recipe.getSteps().size());
        }
    }
}
