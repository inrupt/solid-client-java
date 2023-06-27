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
package com.inrupt.client.jsonb;

import com.inrupt.client.auth.UmaMetadata;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.bind.adapter.JsonbAdapter;

public class UmaAdapter implements JsonbAdapter<UmaMetadata, JsonObject> {
    @Override
    public JsonObject adaptToJson(final UmaMetadata discovery) throws Exception {

        final var result = Json.createObjectBuilder();

        if (discovery.getDpopSigningAlgValuesSupported() != null) {
            final var arrayBuilder = Json.createArrayBuilder();
            discovery.getDpopSigningAlgValuesSupported().forEach(arrayBuilder::add);
            result.add(UmaMetadata.DPOP_SIGNING_ALG_VALUES_SUPPORTED, arrayBuilder.build());
        }

        if (discovery.getGrantTypesSupported() != null) {
            final var arrayBuilder = Json.createArrayBuilder();
            discovery.getGrantTypesSupported().forEach(arrayBuilder::add);
            result.add(UmaMetadata.GRANT_TYPES_SUPPORTED, arrayBuilder.build());
        }

        if (discovery.getIssuer() != null) {
            result.add(UmaMetadata.ISSUER, discovery.getIssuer().toString());
        }

        if (discovery.getJwksUri() != null) {
            result.add(UmaMetadata.JWKS_URI, discovery.getJwksUri().toString());
        }

        if (discovery.getUmaProfilesSupported() != null) {
            final var arrayBuilder = Json.createArrayBuilder();
            discovery.getUmaProfilesSupported().forEach((uri) -> {
                arrayBuilder.add(uri.toString());
            });
            result.add(UmaMetadata.UMA_PROFILES_SUPPORTED, arrayBuilder.build());
        }

        return result.build();
    }

    @Override
    public UmaMetadata adaptFromJson(final JsonObject adapted) throws Exception {
        Set<String> dpopSigningAlgValuesSupported = new HashSet<>();
        if (adapted.containsKey(UmaMetadata.DPOP_SIGNING_ALG_VALUES_SUPPORTED)) {
            adapted.getJsonArray(UmaMetadata.DPOP_SIGNING_ALG_VALUES_SUPPORTED)
                    .forEach(value -> dpopSigningAlgValuesSupported.add(((JsonString) value).getString()));
        }
        Set<String> grantTypesSupported = new HashSet<>();
        if (adapted.containsKey(UmaMetadata.GRANT_TYPES_SUPPORTED)) {
            adapted.getJsonArray(UmaMetadata.GRANT_TYPES_SUPPORTED)
                    .forEach(value -> grantTypesSupported.add(((JsonString) value).getString()));
        }
        URI issuer = null;
        if (adapted.containsKey(UmaMetadata.ISSUER)) {
            issuer = URI.create(adapted.getString(UmaMetadata.ISSUER));
        }
        URI jwksUri = null;
        if (adapted.containsKey(UmaMetadata.JWKS_URI)) {
            jwksUri = URI.create(adapted.getString(UmaMetadata.JWKS_URI));
        }
        URI tokenEndpoint = null;
        if (adapted.containsKey(UmaMetadata.TOKEN_ENDPOINT)) {
            tokenEndpoint = URI.create(adapted.getString(UmaMetadata.TOKEN_ENDPOINT));
        }
        Set<URI> umaProfilesSupported = new HashSet<>();
        if (adapted.containsKey(UmaMetadata.UMA_PROFILES_SUPPORTED)) {
            adapted.getJsonArray(UmaMetadata.UMA_PROFILES_SUPPORTED)
                    .forEach(value -> umaProfilesSupported.add(URI.create(((JsonString) value).getString())));
        }
        return new DefaultUmaMetadata(
                dpopSigningAlgValuesSupported,
                grantTypesSupported,
                issuer,
                jwksUri,
                tokenEndpoint,
                umaProfilesSupported
        );
    }
}
