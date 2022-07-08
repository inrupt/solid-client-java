/*
 * Copyright 2022 Inrupt Inc.
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
package com.inrupt.client.openid;

import java.net.URI;
import java.util.List;

/**
 * A data structure representing an Open ID Connect discovery document.
 */
public class Metadata {

    public URI issuer;

    //@JsonbProperty("scopes_supported")
    public List<String> scopesSupported;

    //@JsonbProperty("response_types_supported")
    public List<String> responseTypesSupported;

    //@JsonbProperty("grant_types_supported")
    public List<String> grantTypesSupported;

    //@JsonbProperty("end_session_endpoint")
    public URI endSessionEndpoint;

    //@JsonbProperty("authorization_endpoint")
    public URI authorizationEndpoint;

    //@JsonbProperty("token_endpoint")
    public URI tokenEndpoint;

    //@JsonbProperty("token_endpoint_auth_methods_supported")
    public List<String> tokenEndpointAuthMethodsSupported;

    //@JsonbProperty("userinfo_endpoint")
    public URI userinfoEndpoint;

    //@JsonbProperty("claims_supported")
    public List<String> claimsSupported;

    //@JsonbProperty("subject_types_supported")
    public List<String> subjectTypesSupported;

    //@JsonbProperty("code_challenge_methods_supported")
    public List<String> codeChallengeMethodsSupported;

    //@JsonbProperty("jwks_uri")
    public URI jwksUri;

    //@JsonbProperty("registration_endpoint")
    public URI registrationEndpoint;

    //@JsonbProperty("revocation_endpoint")
    public URI revocationEndpoint;

    //@JsonbProperty("id_token_signing_alg_values_supported")
    public List<String> idTokenSigningAlgValuesSupported;

    //@JsonbProperty("dpop_signing_alg_values_supported")
    public List<String> dpopSigningAlgValuesSupported;
}
