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
import java.util.Set;

import jakarta.json.bind.annotation.JsonbCreator;
import jakarta.json.bind.annotation.JsonbProperty;

public class UmaMetadataJsonb implements UmaMetadata {
    private final Set<String> dpopSigningAlgValuesSupported;
    private final Set<String> grantTypesSupported;
    private final URI issuer;
    private final URI jwksUri;
    private final URI tokenEndpoint;
    private final Set<URI> umaProfilesSupported;

    @JsonbCreator
    public UmaMetadataJsonb(
            @JsonbProperty("dpop_signing_alg_values_supported") Set<String> dpopSigningAlgValuesSupported,
            @JsonbProperty("grant_types_supported") Set<String> grantTypesSupported,
            @JsonbProperty("issuer") URI issuer,
            @JsonbProperty("jwks_uri") URI jwksUri,
            @JsonbProperty("token_endpoint") URI tokenEndpoint,
            @JsonbProperty("uma_profiles_supported") Set<URI> umaProfilesSupported
    ) {
        this.dpopSigningAlgValuesSupported = dpopSigningAlgValuesSupported;
        this.grantTypesSupported = grantTypesSupported;
        this.issuer = issuer;
        this.jwksUri = jwksUri;
        this.tokenEndpoint = tokenEndpoint;
        this.umaProfilesSupported = umaProfilesSupported;
    }

    @Override
    public Set<String> getDpopSigningAlgValuesSupported() {
        return dpopSigningAlgValuesSupported;
    }

    @Override
    public Set<String> getGrantTypesSupported() {
        return grantTypesSupported;
    }

    @Override
    public URI getIssuer() {
        return issuer;
    }

    @Override
    public URI getJwksUri() {
        return jwksUri;
    }

    @Override
    public URI getTokenEndpoint() {
        return tokenEndpoint;
    }

    @Override
    public Set<URI> getUmaProfilesSupported() {
        return umaProfilesSupported;
    }
}
