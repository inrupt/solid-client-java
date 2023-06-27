package com.inrupt.client.auth;

import java.net.URI;
import java.util.Set;

public class DefaultUmaMetadata implements UmaMetadata {
    private final Set<String> dpopSigningAlgValuesSupported;
    private final Set<String> grantTypesSupported;
    private final URI issuer;
    private final URI jwksUri;
    private final URI tokenEndpoint;
    private final Set<URI> umaProfilesSupported;
    public DefaultUmaMetadata(
            Set<String> dpopSigningAlgValuesSupported,
            Set<String> grantTypesSupported,
            URI issuer,
            URI jwksUri,
            URI tokenEndpoint,
            Set<URI> umaProfilesSupported
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
