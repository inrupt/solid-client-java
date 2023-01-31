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
package com.inrupt.client.integration;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.lang.JoseException;
import org.jose4j.lang.UncheckedJoseException;

final class Utils {

    static final String ACCEPT = "Accept";
    static final String CONTENT_TYPE = "Content-Type";
    static final String IF_NONE_MATCH = "If-None-Match";
    static final String TEXT_TURTLE = "text/turtle";
    static final String SPARQL_UPDATE = "application/sparql-update";
    static final String APPLICATION_JSON = "application/json";
    static final String APPLICATION_FORM = "application/x-www-form-urlencoded";
    static final String PLAIN_TEXT = "text/plain";
    static final String WILDCARD = "*";
    static final String BEARER = "Bearer";
    static final String DPOP = "DPoP";
    static final String UMA = "UMA";

    static final int SUCCESS = 200;
    static final int CREATED = 201;
    static final int NO_CONTENT = 204;
    static final int UNAUTHORIZED = 401;
    static final int NOT_FOUND = 404;
    static final int NOT_ALLOWED = 405;
    static final int CONFLICT = 409;
    static final int PRECONDITION_FAILED = 412;
    static final int ERROR = 500;

    static final String UMA_DISCOVERY_ENDPOINT = "/.well-known/uma2-configuration";
    static final String OPENID_DISCOVERY_ENDPOINT = "/.well-known/openid-configuration";
    static final String UMA_TOKEN_ENDPOINT = "uma/token";
    static final String OAUTH_TOKEN_ENDPOINT = "oauth/oauth20/token";
    static final String JWKS_ENDPOINT = "jwks";

    static boolean isSuccessful(final int status) {
        return Arrays.asList(SUCCESS, NO_CONTENT, CREATED).contains(status);
    }

    static byte[] modifyBody(final byte[] originalBody, final String requestBody)
            throws IOException {
        try (final InputStream input = new ByteArrayInputStream(originalBody)) {
            final Model model = ModelFactory.createDefaultModel();
            RDFDataMgr.read(model, input, Lang.TURTLE);
            final UpdateRequest request = UpdateFactory.create(requestBody);
            UpdateAction.execute(request, model);

            try (final ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                RDFDataMgr.write(output, model, Lang.TURTLE);
                return output.toByteArray();
            }
        }
    }

    static PublicJsonWebKey getDpopKey(final String resource) {
        try (final InputStream stream = Utils.class.getResourceAsStream(resource)) {
            final String jwks = IOUtils.toString(stream, UTF_8);
            return PublicJsonWebKey.Factory.newPublicJwk(jwks);
        } catch (final IOException ex) {
            throw new UncheckedIOException("Unable to read JWK", ex);
        } catch (final JoseException ex) {
            throw new UncheckedJoseException("Unable to generate DPoP token", ex);
        }
    }

    static boolean isPrivateResource(final String uri) {
        return uri.contains(State.PRIVATE_RESOURCE_PATH);
    }

    static boolean isPodRoot(final String url) {
        return "/".equals(url);
    }

    private Utils() {
        // Prevent instantiation
    }

}
