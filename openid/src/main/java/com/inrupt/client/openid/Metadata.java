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

    /**
     * The issuer URI for the given OpenID Connect provider.
     */
    public URI issuer;

    /**
     * A list of scopes supported by the given OpenID Connect provider.
     */
    public List<String> scopesSupported;

    /**
     * A list of response types supported by the given OpenID Connect provider.
     */
    //@("response_types_supported")
    public List<String> responseTypesSupported;

    /**
     * A list of grant types supported by the given OpenID Connect provider.
     */
    public List<String> grantTypesSupported;

    /**
     * The location of the end session endpoint for the given OpenID Connect provider, if supported.
     */
    public URI endSessionEndpoint;

    /**
     * The location of the authorization endpoint for the given OpenID Connect provider.
     */
    public URI authorizationEndpoint;

    /**
     * The location of the token endpoint for the given OpenID Connect provider.
     */
    public URI tokenEndpoint;

    /**
     * A list of authentication methods supported by the token endpoint of the given OpenID Connect provider.
     */
    public List<String> tokenEndpointAuthMethodsSupported;

    /**
     * The location of the userinfo endpoint for the given OpenID Connect provider.
     */
    public URI userinfoEndpoint;

    /**
     * A list of claims supported by the given OpenID Connect provider.
     */
    public List<String> claimsSupported;

    /**
     * A list of subject types supported by the given OpenID Connect provider.
     */
    public List<String> subjectTypesSupported;

    /**
     * A list of code challenge methods supported by the given OpentID Connect provider.
     */
    public List<String> codeChallengeMethodsSupported;

    /**
     * The location of the JSON Web Key Set endpoint for the given OpenID Connect provider.
     */
    public URI jwksUri;

    /**
     * The registration endpoint for the given OpenID Connect provider.
     */
    public URI registrationEndpoint;

    /**
     * The revocation endpoint for the given OpenID Connect provider.
     */
    public URI revocationEndpoint;

    /**
     * A list of ID Token signing algorithm values supported by the given OpenID Connect provider.
     */
    public List<String> idTokenSigningAlgValuesSupported;

    /**
     * A list of DPoP signing algorithm values supported by the given OpenID Connect provider.
     */
    public List<String> dpopSigningAlgValuesSupported;
}
