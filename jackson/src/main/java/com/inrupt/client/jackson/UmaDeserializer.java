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
package com.inrupt.client.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.inrupt.client.auth.UmaMetadata;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

public class UmaDeserializer extends StdDeserializer<UmaMetadata> {

    public UmaDeserializer(final Class<UmaMetadata> metadata) {
        super(metadata);
    }

    @Override
    public UmaMetadata deserialize(
        final JsonParser jsonParser,
        final DeserializationContext deserializationContext
    ) throws IOException {
        final JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        final Set<String> dpopSigningAlgValuesSupported = new HashSet<>();
        if (!node.path(UmaMetadata.DPOP_SIGNING_ALG_VALUES_SUPPORTED).isMissingNode()) {
            node.get(UmaMetadata.DPOP_SIGNING_ALG_VALUES_SUPPORTED).elements()
                .forEachRemaining(element -> dpopSigningAlgValuesSupported.add(element.textValue()));
        }

        final Set<String> grantTypesSupported = new HashSet<>();
        if (!node.path(UmaMetadata.GRANT_TYPES_SUPPORTED).isMissingNode()) {
            node.get(UmaMetadata.GRANT_TYPES_SUPPORTED).elements()
                .forEachRemaining(element -> grantTypesSupported.add(element.textValue()));
        }

        URI issuer = null;
        if (!node.path(UmaMetadata.ISSUER).isMissingNode()) {
            issuer = URI.create(node.get(UmaMetadata.ISSUER).textValue());
        }

        URI jwksUri = null;
        if (!node.path(UmaMetadata.JWKS_URI).isMissingNode()) {
            jwksUri = URI.create(node.get(UmaMetadata.JWKS_URI).textValue());
        }

        URI tokenEndpoint = null;
        if (!node.path(UmaMetadata.TOKEN_ENDPOINT).isMissingNode()) {
            tokenEndpoint = URI.create(node.get(UmaMetadata.TOKEN_ENDPOINT).textValue());
        }

        final Set<URI> umaProfilesSupported = new HashSet<>();
        if (!node.path(UmaMetadata.UMA_PROFILES_SUPPORTED).isMissingNode()) {
            node.get(UmaMetadata.UMA_PROFILES_SUPPORTED).elements()
                .forEachRemaining(element -> umaProfilesSupported.add(URI.create(element.textValue())));
        }

        return new UmaMetadataJackson(
            dpopSigningAlgValuesSupported,
            grantTypesSupported,
            issuer,
            jwksUri,
            tokenEndpoint,
            umaProfilesSupported
        );
    }
}
