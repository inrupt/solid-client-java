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
package com.inrupt.client.accessgrant.accessGrant;

import static com.inrupt.client.accessgrant.accessGrant.Utils.isSuccess;

import com.inrupt.client.Client;
import com.inrupt.client.ClientCache;
import com.inrupt.client.ClientProvider;
import com.inrupt.client.accessgrant.AccessGrantException;
import com.inrupt.client.auth.Session;
import com.inrupt.client.spi.ServiceProvider;

import java.net.URI;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletionStage;

public class AccessGrantClient {

    private VCClient client;

    /**
     * Create an access grant client.
     *
     * @param issuer the issuer
     */
    public AccessGrantClient(final URI issuer) {
        this(ClientProvider.getClient(), issuer);
    }

    /**
     * Create an access grant client.
     *
     * @param client the client
     * @param issuer the issuer
     */
    public AccessGrantClient(final Client client, final URI issuer) {
        Objects.requireNonNull(client, "client may not be null!");
        Objects.requireNonNull(issuer, "issuer may not be null!");
        this.client = new VCClient(client,
            ServiceProvider.getCacheBuilder().build(100, Duration.ofMinutes(60)),
            new VCConfiguration(issuer));
    }

    /**
     * Create an access grant client.
     *
     * @param client the client
     * @param issuer the issuer
     * @param metadataCache the metadata cache
     */
    public AccessGrantClient(final Client client, final URI issuer, final ClientCache<URI, Metadata> metadataCache) {
        Objects.requireNonNull(client, "client may not be null!");
        Objects.requireNonNull(issuer, "issuer may not be null!");
        Objects.requireNonNull(metadataCache, "metadataCache may not be null!");
        this.client = new VCClient(client, metadataCache, new VCConfiguration(issuer));
    }
    
    /**
     * Scope an access grant client to a particular session.
     *
     * @param session the session
     * @return the scoped access grant client
     */
    public AccessGrantClient session(final Session session) {
        Objects.requireNonNull(session, "Session may not be null!");
        this.client = client.session(session);
        return this;
    }

    /**
     * Request for an Access Request.
     *
     * @param Access the initial access
     * @return the next stage of completion containing the resulting access request
     */
    public CompletionStage<AccessRequest> requestAccess(final Access access) {

        return client.issue(access.toVC())
            .thenApply(res -> {
                final int status = res.statusCode();
                if (isSuccess(status)) {
                    return AccessRequest.fromVC(res.body());
                }
                throw new AccessGrantException("Unable to perform request access: HTTP error " + status,
                    status);
            });
    }

    /**
     * Approve an existing access request.
     *
     * @param AccessRequest the access request to be approved
     * @return the next stage of completion containing the resulting access grant
     */
    public CompletionStage<AccessGrant> approveRequest(final AccessRequest accessRequest) {
        
        return client.issue(accessRequest.toVC())
            .thenApply(res -> {
                final int status = res.statusCode();
                if (isSuccess(status)) {
                    return AccessGrant.fromVC(res.body());
                }
                throw new AccessGrantException("Unable to perform approve request: HTTP error " + status,
                    status);
            });
    }

    /**
     * Find specific grants.
     *
     * @param Access the specific access we want to find
     * @return the next stage of completion containing the resulting access grants
     */
    public CompletionStage<AccessGrant> getGrants(final Access access) {
        //would call the /query endpoint
        return null;
    }

    /**
     * Find specific access requests.
     *
     * @param Access the specific access we want to find
     * @return the next stage of completion containing the resulting access requests
     */
    public CompletionStage<AccessRequest> getRequests(final Access access) {
        //would call the /query endpoint
        return null;
    }

    /**
     * Deny an existing access request.
     *
     * @param AccessRequest the access request to be denied
     * @return the next stage of completion containing the resulting access denied
     */
    public CompletionStage<Void> denyRequest(final AccessRequest accessRequest) {
        return null;
    }
}
