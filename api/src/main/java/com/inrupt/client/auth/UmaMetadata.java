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
package com.inrupt.client.auth;

import java.net.URI;
import java.util.Set;

/**
 * A class representing an UMA discovery document.
 */
public interface UmaMetadata {
    String DPOP_SIGNING_ALG_VALUES_SUPPORTED = "dpop_signing_alg_values_supported";
    String GRANT_TYPES_SUPPORTED = "grant_types_supported";
    String ISSUER = "issuer";
    String JWKS_URI = "jwks_uri";
    String TOKEN_ENDPOINT = "token_endpoint";
    String UMA_PROFILES_SUPPORTED = "uma_profiles_supported";

    Set<String> getDpopSigningAlgValuesSupported();
    Set<String> getGrantTypesSupported();
    URI getIssuer();
    URI getJwksUri();
    URI getTokenEndpoint();
    Set<URI> getUmaProfilesSupported();
}
