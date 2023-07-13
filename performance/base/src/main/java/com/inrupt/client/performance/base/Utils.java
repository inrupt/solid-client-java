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
package com.inrupt.client.performance.base;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.inrupt.client.Headers;
import com.inrupt.client.Request;
import com.inrupt.client.Response;
import com.inrupt.client.solid.*;
import com.inrupt.client.vocabulary.LDP;

import java.net.URI;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Utils {

    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String TEXT_TURTLE = "text/turtle";
    public static final String APPLICATION_JSON = "application/json";
    public static final String APPLICATION_FORM = "application/x-www-form-urlencoded";
    public static final String PLAIN_TEXT = "text/plain";
    public static final String FOLDER_SEPARATOR = "/";

    public static final int SUCCESS = 200;
    public static final int CREATED = 201;
    public static final int NO_CONTENT = 204;

    public static final String UMA_DISCOVERY_ENDPOINT = "/.well-known/uma2-configuration";
    public static final String OPENID_DISCOVERY_ENDPOINT = "/.well-known/openid-configuration";
    public static final String VC_DISCOVERY_ENDPOINT = "/.well-known/vc-configuration";
    public static final String UMA_TOKEN_ENDPOINT = "uma/token";
    public static final String OAUTH_TOKEN_ENDPOINT = "oauth/oauth20/token";
    public static final String UMA_JWKS_ENDPOINT = "uma/jwks";
    public static final String OAUTH_JWKS_ENDPOINT = "oauth/jwks";

    public static boolean isSuccessful(final int status) {
        return Arrays.asList(SUCCESS, NO_CONTENT, CREATED).contains(status);
    }

    public static boolean exists(final SolidSyncClient authClient, final URI containerURI) {
        final var headReq = Request.newBuilder(containerURI)
                .HEAD()
                .build();
        final var resCheckIfExists =
                authClient.send(headReq, Response.BodyHandlers.discarding());
        return Utils.isSuccessful(resCheckIfExists.statusCode());
    }

    public static void createContainer(final SolidSyncClient authClient, final URI containerURI) {
        if (!exists(authClient, containerURI)) {
            final var requestCreate = Request.newBuilder(containerURI)
                    .header(Utils.CONTENT_TYPE, Utils.TEXT_TURTLE)
                    .header("Link", Headers.Link.of(LDP.RDFSource, "type").toString())
                    .PUT(Request.BodyPublishers.noBody())
                    .build();
            final var resCreate =
                    authClient.send(requestCreate, Response.BodyHandlers.discarding());
            assertTrue(Utils.isSuccessful(resCreate.statusCode()));
        }
    }

    static void deleteContentsRecursively(final SolidSyncClient client, final URI url) {
        if (exists(client, url)) {
            deleteRecursive(client, url, new AtomicInteger(0));
        }
    }

    static void deleteRecursive(final SolidSyncClient client, final URI url, final AtomicInteger depth) {
        if (url != null) {
            if (url.toString().endsWith("/")) {
                if (depth != null) {
                    depth.incrementAndGet();
                }
                // get all members
                final List<URI> members = new ArrayList<>();
                try (final SolidContainer container = client.read(url, SolidContainer.class)) {
                    container.getResources().forEach(value -> members.add(value.getIdentifier()));
                } catch (Exception e) {
                    LOGGER.error("Failed to get container members: {}", e.toString());
                    // server may have overwritten a container as a resource so attempt to delete it
                    client.delete(url);
                }

                // delete members via this method
                LOGGER.debug("DELETING MEMBERS {}", members);
                try {
                    members.forEach(u -> deleteRecursive(client, u, depth));
                } catch (SolidClientException ex) {
                    LOGGER.error("Failed to delete resources", ex);
                }

                if (depth != null) {
                    depth.decrementAndGet();
                }
            }
            deleteResource(client, url, depth);
        }
    }

    static void deleteResource(final SolidSyncClient client, final URI url, final AtomicInteger depth) {
        // delete the resource unless depth counting to avoid this
        if (depth == null || depth.get() > 0) {
            client.delete(url);
            LOGGER.debug("DELETE RESOURCE {}", url);
        }
    }

    private Utils() {
        // Prevent instantiation
    }

}
