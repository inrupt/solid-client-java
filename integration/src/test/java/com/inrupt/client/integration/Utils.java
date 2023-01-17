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
import static org.jose4j.jwx.HeaderParameterNames.TYPE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;
import org.jose4j.lang.UncheckedJoseException;

final class Utils {

    static final String ACCEPT = "Accept";
    static final String CONTENT_TYPE = "Content-Type";
    static final String IF_NONE_MATCH = "If-None-Match";
    static final String TEXT_TURTLE = "text/turtle";
    static final String SPARQL_UPDATE = "application/sparql-update";
    static final String APPLICATION_JSON = "application/json";
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
    
    private static final MockSolidServer mockHttpServer = new MockSolidServer();
    private static final Config config = ConfigProvider.getConfig();

    static final String PRIVATE_RESOURCE_PATH = config
        .getOptionalValue("inrupt.test.privateResourcePath", String.class)
        .orElse("/private");
    static String AS_URI = config.getValue("inrupt.test.asUri", String.class);
    static String POD_URL = config.getValue("inrupt.test.storage", String.class);
    static final String USERNAME = config.getValue("inrupt.test.username", String.class);
    static URI WEBID = URI.create(POD_URL + "/" + USERNAME);
    static final String ISS = config.getValue("inrupt.test.idp", String.class);
    private static final String AZP = config.getValue("inrupt.test.azp", String.class);
    
    static String UMA_DISCOVERY_ENDPOINT = "/.well-known/uma2-configuration";
    static String TOKEN_ENDPOINT = "/token";
    static String JWKS_ENDPOINT = "/jwks";
    
    static String setupIdToken() {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("webid", POD_URL + "/" + USERNAME);
        claims.put("sub", USERNAME);
        claims.put("iss", ISS);
        claims.put("azp", AZP);

        final String token = generateIdToken(claims);
        return token;
    }

    static String generateIdToken(final Map<String, Object> claims) {
        try (final InputStream resource = Utils.class.getResourceAsStream("/signing-key.json")) {
            final String jwks = IOUtils.toString(resource, UTF_8);
            final PublicJsonWebKey jwk = PublicJsonWebKey.Factory
                .newPublicJwk(jwks);

            final JsonWebSignature jws = new JsonWebSignature();
            jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256);
            jws.setHeader(TYPE, "JWT");
            jws.setKey(jwk.getPrivateKey());
            final JwtClaims jwtClaims = new JwtClaims();
            jwtClaims.setJwtId(UUID.randomUUID().toString());
            jwtClaims.setExpirationTimeMinutesInTheFuture(5);
            jwtClaims.setIssuedAtToNow();
            // override/set claims
            claims.entrySet().forEach(entry -> jwtClaims.setClaim(entry.getKey(), entry.getValue()));
            jws.setPayload(jwtClaims.toJson());

            return jws.getCompactSerialization();
        } catch (final IOException ex) {
            throw new UncheckedIOException("Unable to read JWK", ex);
        } catch (final JoseException ex) {
            throw new UncheckedJoseException("Unable to generate DPoP token", ex);
        }
    }

    static void initMockServer() {
        mockHttpServer.start();
        POD_URL = mockHttpServer.getMockServerUrl();
        AS_URI = POD_URL + "/uma";
        WEBID = URI.create(POD_URL + "/" + USERNAME);
    }

    static void stopMockServer() {
        mockHttpServer.stop();
    }

    static String getMockServerUrl() {
        return mockHttpServer.getMockServerUrl();
    }

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
    
    static String getResource(final String path, final String baseUrl, final String issuer) {
        return getResource(path).replace("{{baseUrl}}", baseUrl).replace("{{issuerUrl}}", issuer);
    }

    static String getResource(final String path) {
        try (final InputStream res = Utils.class.getResourceAsStream(path)) {
            return new String(IOUtils.toByteArray(res), UTF_8);
        } catch (final IOException ex) {
            throw new UncheckedIOException("Could not read class resource", ex);
        }
    }
    
    static boolean isPrivateResource(final String uri) {
        return uri.contains(PRIVATE_RESOURCE_PATH);
    }

    static boolean isPodRoot(final String url) {
        return "/".equals(url);
    }

    private Utils() {
        // Prevent instantiation
    }

}
