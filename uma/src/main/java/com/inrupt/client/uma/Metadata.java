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
package com.inrupt.client.uma;

import com.inrupt.client.auth.UmaMetadata;

import java.net.URI;
import java.util.Set;

/**
 * A class representing an UMA discovery document.
 * @deprecated Prefer the com.inrupt.client.auth.UmaMetadata interface.
 */
public class Metadata implements UmaMetadata {

    public Set<String> dpopSigningAlgValuesSupported;
    public Set<String> grantTypesSupported;
    public URI issuer;
    public URI jwksUri;
    public URI tokenEndpoint;
    public Set<URI> umaProfilesSupported;

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

    public static Metadata fromUmaMetadata(final UmaMetadata metadata) {
        final var result = new Metadata();
        result.dpopSigningAlgValuesSupported = metadata.getDpopSigningAlgValuesSupported();
        result.grantTypesSupported = metadata.getGrantTypesSupported();
        result.issuer = metadata.getIssuer();
        result.jwksUri = metadata.getJwksUri();
        result.tokenEndpoint = metadata.getTokenEndpoint();
        result.umaProfilesSupported = metadata.getUmaProfilesSupported();
        return result;
    }
}
