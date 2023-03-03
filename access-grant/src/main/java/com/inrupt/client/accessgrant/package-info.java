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
/**
 * <h2>Classes for working with Access Grants.</h2>
 *
 * <p>This module provides utilities for using Access Grants in Solid Applications.
 * There are three primary classes used for this.
 *
 * <p><strong>AccessGrant</strong>: this class represents either a {@code SolidAccessGrant} or a
 * {@code SolidAccessRequest}. A developer can parse an {@link AccessGrant} from a {@link String} or
 * {@link java.io.InputStream} in the following way:
 *
 * <pre>{@code
   try (InputStream stream = fetchAccessGrant()) {
       AccessGrant accessGrant = AccessGrant.ofAccessGrant(data);
   }
 * }</pre>
 *
 * <p><strong>AccessGrantSession</strong>: this class can be used to build a {@link com.inrupt.client.auth.Session}
 * object that uses Access Grants when negotiating for access tokens. These sessions will <em>also</em> need an
 * OpenID-based session.
 *
 * <pre>{@code
   AccessGrant accessGrant1 = AccessGrant.ofAccessGrant(data1);
   AccessGrant accessGrant2 = AccessGrant.ofAccessGrant(data2);

   Session openid = OpenIdSession.ofIdToken(idToken);
   Session session = AccessGrantSession.ofAccessGrant(openid, accessGrant1, accessGrant2);

   SolidClient client = SolidClient.getClient().session(session);
 * }</pre>
 *
 * <p><strong>AccessGrantClient</strong>: this class can be used for managing the lifecycle of Access Grants:
 * creation, deletion, revocation and query. This client will require a suitable
 * {@link com.inrupt.client.auth.Session} object, typically an OpenID-based session:
 *
 * <pre>{@code
   URI issuer = URI.create("https://issuer.example");
   Session openid = OpenIdSession.ofIdToken(idToken);

   AccessGrantClient client = new AccessGrantClient(issuer).session(session);

   URI resource = URI.create("https://storage.example/data/resource");
   URI SOLID_ACCESS_GRANT = URI.create("http://www.w3.org/ns/solid/vc#SolidAccessGrant");
   client.query(SOLID_ACCESS_GRANT, null, resource, null)
       .thenApply(grants -> AccessGrantSession.ofAccessGrant(openid, grants.toArray(new AccessGrant[0])))
       .thenApply(session -> SolidClient.getClient().session(session))
       .thenAccept(cl -> {
            // Do something with the Access Grant-scoped client
       });
 * }</pre>
 */
package com.inrupt.client.accessgrant;
