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
package com.inrupt.client.e2e;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.jose4j.jwx.HeaderParameterNames.TYPE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;
import org.jose4j.lang.UncheckedJoseException;

public final class Utils {

    public static final String ACCEPT = "Accept";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String IF_NONE_MATCH = "If-None-Match";
    public static final String TEXT_TURTLE = "text/turtle";
    public static final String SPARQL_UPDATE = "application/sparql-update";
    public static final String PLAIN_TEXT = "text/plain";
    public static final String WILDCARD = "*";
    public static final int SUCCESS = 200;
    public static final int CREATED = 201;
    public static final int NO_CONTENT = 204;
    public static final int NOT_FOUND = 404;
    public static final int CONFLICT = 409;
    public static final int PRECONDITION_FAILED = 412;
    public static final int ERROR = 500;

    private static final MockSolidServer mockHttpServer = new MockSolidServer();
    private static final Map<String, String> config = new HashMap<>();

    static String generateIdToken(final Map<String, Object> claims) {
        try (final InputStream resource = DomainModulesResourceTest.class.getResourceAsStream("/signing-key.json")) {
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
        config.putAll(mockHttpServer.start());
    }

    static void stopMockServer() {
        mockHttpServer.stop();
    }

    static String getMockServerUrl() {
        return config.get("solid_server");
    }

    static byte[] appendBytes(final byte[] body, final byte[] body2) throws IOException {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(body);
        outputStream.write(body2);

        return outputStream.toByteArray();

    }

    public static byte[] deleteBytes(final byte[] newBody, final String[] triplesToDelete) {
        final var body = new String(newBody);
        for (final var oneTriple : triplesToDelete) {
            body.replaceAll(oneTriple, "");
        }
        return body.getBytes();
    }

    public static byte[] modifyBody(final byte[] originalBody, final String requestBody) throws IOException {
        byte[] newBody = originalBody;

        final var insert = "INSERT DATA {";
        final var beginningOfInsert = requestBody.indexOf(insert);
        final var endOfInsert = requestBody.lastIndexOf("}");
        var triplesToAdd = "";
        if (beginningOfInsert != -1 && endOfInsert != -1) {
            triplesToAdd = requestBody.substring(beginningOfInsert + insert.length(), endOfInsert);
        }
        if (!triplesToAdd.isEmpty()) {
            newBody = Utils.appendBytes(originalBody, triplesToAdd.getBytes());
        }

        final var delete = "DELETE DATA {";
        final var beginningOfDelete = requestBody.indexOf(delete);
        final var endOfDelete = requestBody.indexOf("};");
        String[] triplesToDelete = null;
        if (beginningOfDelete != -1 && endOfDelete != -1) {
            triplesToDelete =
                requestBody.substring(beginningOfDelete + delete.length(), endOfDelete).split(".");
        }

        if (triplesToDelete != null && triplesToDelete.length > 0) {
            newBody = Utils.deleteBytes(newBody, triplesToDelete);
        }
        return newBody;
    }

    private Utils() {
        // Prevent instantiation
    }
}
