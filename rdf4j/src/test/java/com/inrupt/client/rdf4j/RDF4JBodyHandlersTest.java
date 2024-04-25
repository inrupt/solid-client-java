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
package com.inrupt.client.rdf4j;

import static org.junit.jupiter.api.Assertions.*;

import com.inrupt.client.ClientHttpException;
import com.inrupt.client.Request;
import com.inrupt.client.Response;
import com.inrupt.client.spi.HttpService;
import com.inrupt.client.spi.ServiceProvider;
import com.inrupt.client.test.RdfMockService;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class RDF4JBodyHandlersTest {

    private static final RdfMockService mockHttpService = new RdfMockService();
    private static final Map<String, String> config = new HashMap<>();
    private static final HttpService client = ServiceProvider.getHttpService();

    @BeforeAll
    static void setup() {
        config.putAll(mockHttpService.start());
    }

    @AfterAll
    static void teardown() {
        mockHttpService.stop();
    }

    @Test
    void testOfRDF4JModelHandlerAsync() throws IOException,
            InterruptedException, ExecutionException, TimeoutException {
        final Request request = Request.newBuilder()
                .uri(URI.create(config.get("rdf_uri") + "/oneTriple"))
                .GET()
                .build();

        final Response<Model> res = client.send(request, RDF4JBodyHandlers.ofRDF4JModel()).toCompletableFuture().join();

        final int statusCode = res.statusCode();
        final Model responseBody = res.body();

        assertEquals(200, statusCode);
        assertEquals(1, responseBody.size());
        assertTrue(responseBody.contains(
            (Resource)SimpleValueFactory.getInstance().createIRI("http://example.test/s"),
            null,
            null,
            (Resource)null)
        );
    }

    /**
     * @deprecated covers the deprecated RDF4JBodyHandlers::ofModel function. To be removed when removing the function
     *  from the API.
     */
    @Test
    void testOfModelHandlerAsync() throws IOException,
            InterruptedException, ExecutionException, TimeoutException {
        final Request request = Request.newBuilder()
                .uri(URI.create(config.get("rdf_uri") + "/oneTriple"))
                .GET()
                .build();

        final Response<Model> res = client.send(request, RDF4JBodyHandlers.ofModel()).toCompletableFuture().join();

        final int statusCode = res.statusCode();
        final Model responseBody = res.body();

        assertEquals(200, statusCode);
        assertEquals(1, responseBody.size());
        assertTrue(responseBody.contains(
                (Resource)SimpleValueFactory.getInstance().createIRI("http://example.test/s"),
                null,
                null,
                (Resource)null)
        );
    }

    @Test
    void testOfRDF4JModelHandler() throws IOException,
            InterruptedException, ExecutionException, TimeoutException {
        final Request request = Request.newBuilder()
                .uri(URI.create(config.get("rdf_uri") + "/oneTriple"))
                .GET()
                .build();

        final Response<Model> response = client.send(request, RDF4JBodyHandlers.ofRDF4JModel())
            .toCompletableFuture().join();

        assertEquals(200, response.statusCode());
        final Model responseBody = response.body();
        assertEquals(1, responseBody.size());
        assertTrue(responseBody.contains(
            (Resource)SimpleValueFactory.getInstance().createIRI("http://example.test/s"),
            null,
            null,
            (Resource)null)
        );
    }

    /**
     * @deprecated covers the deprecated RDF4JBodyHandlers::ofModel function. To be removed when removing the function
     *  from the API.
     */
    @Test
    void testOfModelHandler() throws IOException,
            InterruptedException, ExecutionException, TimeoutException {
        final Request request = Request.newBuilder()
                .uri(URI.create(config.get("rdf_uri") + "/oneTriple"))
                .GET()
                .build();

        final Response<Model> response = client.send(request, RDF4JBodyHandlers.ofModel())
                .toCompletableFuture().join();

        assertEquals(200, response.statusCode());
        final Model responseBody = response.body();
        assertEquals(1, responseBody.size());
        assertTrue(responseBody.contains(
                (Resource)SimpleValueFactory.getInstance().createIRI("http://example.test/s"),
                null,
                null,
                (Resource)null)
        );
    }

    @Test
    void testOfRDF4JModelHandlerWithURL() throws IOException, InterruptedException {
        final Request request = Request.newBuilder()
                .uri(URI.create(config.get("rdf_uri") + "/example"))
                .GET()
                .build();

        final Response<Model> response = client.send(request, RDF4JBodyHandlers.ofRDF4JModel())
            .toCompletableFuture().join();

        assertEquals(200, response.statusCode());
        final Model responseBody = response.body();
        assertEquals(7, responseBody.size());
        assertTrue(responseBody.contains(
                null,
                SimpleValueFactory.getInstance().createIRI("http://www.w3.org/ns/pim/space#preferencesFile"),
                null,
                (Resource)null
                )
        );
    }

    /**
     * @deprecated covers the deprecated RDF4JBodyHandlers::ofModel function. To be removed when removing the function
     *  from the API.
     */
    @Test
    void testOfModelHandlerWithURL() throws IOException, InterruptedException {
        final Request request = Request.newBuilder()
                .uri(URI.create(config.get("rdf_uri") + "/example"))
                .GET()
                .build();

        final Response<Model> response = client.send(request, RDF4JBodyHandlers.ofModel())
                .toCompletableFuture().join();

        assertEquals(200, response.statusCode());
        final Model responseBody = response.body();
        assertEquals(7, responseBody.size());
        assertTrue(responseBody.contains(
                        null,
                        SimpleValueFactory.getInstance().createIRI("http://www.w3.org/ns/pim/space#preferencesFile"),
                        null,
                        (Resource)null
                )
        );
    }

    @Test
    void testOfRDF4JModelHandlerError() throws IOException,
            InterruptedException, ExecutionException, TimeoutException {
        final Request request = Request.newBuilder()
                .uri(URI.create(config.get("rdf_uri") + "/error"))
                .GET()
                .build();

        final CompletionException completionException = assertThrows(
                CompletionException.class,
                () -> client.send(request, RDF4JBodyHandlers.ofRDF4JModel()).toCompletableFuture().join()
        );

        final ClientHttpException httpException = (ClientHttpException) completionException.getCause();

        assertEquals(429, httpException.getProblemDetails().getStatus());
        assertEquals("Too Many Requests", httpException.getProblemDetails().getTitle());
        assertEquals("Some details", httpException.getProblemDetails().getDetails());
        assertEquals("https://example.org/type", httpException.getProblemDetails().getType().toString());
        assertEquals("https://example.org/instance", httpException.getProblemDetails().getInstance().toString());
    }

    @Test
    void testOfRDF4JRepositoryHandlerAsync() throws IOException,
            InterruptedException, ExecutionException, TimeoutException {
        final Request request = Request.newBuilder()
                .uri(URI.create(config.get("rdf_uri") + "/oneTriple"))
                .GET()
                .build();

        final Response<Repository> res = client.send(request, RDF4JBodyHandlers.ofRDF4JRepository())
                .toCompletableFuture().join();

        final int statusCode = res.statusCode();
        assertEquals(200, statusCode);

        final Repository responseBody = res.body();
        try (final RepositoryConnection conn = responseBody.getConnection()) {
            assertTrue(conn.hasStatement(
                            (Resource)SimpleValueFactory.getInstance().createIRI("http://example.test/s"),
                            null,
                            null,
                            false,
                            (Resource)null
                    )
            );
        }
    }

    /**
     * @deprecated covers the deprecated RDF4JBodyHandlers::ofRepository function.
     *   To be removed when removing the function from the API.
     */
    @Test
    void testOfRepositoryHandlerAsync() throws IOException,
            InterruptedException, ExecutionException, TimeoutException {
        final Request request = Request.newBuilder()
                .uri(URI.create(config.get("rdf_uri") + "/oneTriple"))
                .GET()
                .build();

        final Response<Repository> res = client.send(request, RDF4JBodyHandlers.ofRepository())
                .toCompletableFuture().join();

        final int statusCode = res.statusCode();
        assertEquals(200, statusCode);

        final Repository responseBody = res.body();
        try (final RepositoryConnection conn = responseBody.getConnection()) {
            assertTrue(conn.hasStatement(
                            (Resource)SimpleValueFactory.getInstance().createIRI("http://example.test/s"),
                            null,
                            null,
                            false,
                            (Resource)null
                    )
            );
        }
    }

    @Test
    void testOfRDF4JRepositoryHandler() throws IOException,
            InterruptedException, ExecutionException, TimeoutException {
        final Request request = Request.newBuilder()
                .uri(URI.create(config.get("rdf_uri") + "/oneTriple"))
                .GET()
                .build();

        final Response<Repository> response = client.send(request, RDF4JBodyHandlers.ofRDF4JRepository())
            .toCompletableFuture().join();
        assertEquals(200, response.statusCode());

        final Repository responseBody = response.body();
        try (final RepositoryConnection conn = responseBody.getConnection()) {
            assertTrue(conn.hasStatement(
                (Resource)SimpleValueFactory.getInstance().createIRI("http://example.test/s"),
                null,
                null,
                false,
                (Resource)null
            )
            );
        }
    }

    /**
     * @deprecated covers the deprecated RDF4JBodyHandlers::ofRepository function.
     *   To be removed when removing the function from the API.
     */
    @Test
    void testOfRepositoryHandler() throws IOException,
            InterruptedException, ExecutionException, TimeoutException {
        final Request request = Request.newBuilder()
                .uri(URI.create(config.get("rdf_uri") + "/oneTriple"))
                .GET()
                .build();

        final Response<Repository> response = client.send(request, RDF4JBodyHandlers.ofRepository())
                .toCompletableFuture().join();
        assertEquals(200, response.statusCode());

        final Repository responseBody = response.body();
        try (final RepositoryConnection conn = responseBody.getConnection()) {
            assertTrue(conn.hasStatement(
                            (Resource)SimpleValueFactory.getInstance().createIRI("http://example.test/s"),
                            null,
                            null,
                            false,
                            (Resource)null
                    )
            );
        }
    }

    @Test
    void testOfRDF4JRepositoryHandlerWithURL() throws IOException, InterruptedException {
        final Request request = Request.newBuilder()
                .uri(URI.create(config.get("rdf_uri") + "/example"))
                .GET()
                .build();

        final Response<Repository> response = client.send(request, RDF4JBodyHandlers.ofRDF4JRepository())
            .toCompletableFuture().join();
        assertEquals(200, response.statusCode());

        final Repository responseBody = response.body();
        try (final RepositoryConnection conn = responseBody.getConnection()) {
            assertTrue(conn.hasStatement(
                null,
                SimpleValueFactory.getInstance().createIRI("http://www.w3.org/ns/pim/space#preferencesFile"),
                null,
                false,
                (Resource)null
            )
            );
        }
    }

    /**
     * @deprecated covers the deprecated RDF4JBodyHandlers::ofRepository function.
     *   To be removed when removing the function from the API.
     */
    @Test
    void testOfRepositoryHandlerWithURL() throws IOException, InterruptedException {
        final Request request = Request.newBuilder()
                .uri(URI.create(config.get("rdf_uri") + "/example"))
                .GET()
                .build();

        final Response<Repository> response = client.send(request, RDF4JBodyHandlers.ofRepository())
                .toCompletableFuture().join();
        assertEquals(200, response.statusCode());

        final Repository responseBody = response.body();
        try (final RepositoryConnection conn = responseBody.getConnection()) {
            assertTrue(conn.hasStatement(
                            null,
                            SimpleValueFactory.getInstance().createIRI("http://www.w3.org/ns/pim/space#preferencesFile"),
                            null,
                            false,
                            (Resource)null
                    )
            );
        }
    }

    @Test
    void testOfRDF4JRepositoryHandlerError() throws IOException,
            InterruptedException, ExecutionException, TimeoutException {
        final Request request = Request.newBuilder()
                .uri(URI.create(config.get("rdf_uri") + "/error"))
                .GET()
                .build();

        final CompletionException completionException = assertThrows(
                CompletionException.class,
                () -> client.send(request, RDF4JBodyHandlers.ofRDF4JRepository()).toCompletableFuture().join()
        );

        final ClientHttpException httpException = (ClientHttpException) completionException.getCause();

        assertEquals(429, httpException.getProblemDetails().getStatus());
        assertEquals("Too Many Requests", httpException.getProblemDetails().getTitle());
        assertEquals("Some details", httpException.getProblemDetails().getDetails());
        assertEquals("https://example.org/type", httpException.getProblemDetails().getType().toString());
        assertEquals("https://example.org/instance", httpException.getProblemDetails().getInstance().toString());
    }
}
