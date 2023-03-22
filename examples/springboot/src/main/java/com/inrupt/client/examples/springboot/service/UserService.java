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
package com.inrupt.client.examples.springboot.service;

import com.inrupt.client.examples.springboot.AuthNAuthZFailException;
import com.inrupt.client.examples.springboot.model.WebIdOwner;
import com.inrupt.client.openid.OpenIdException;
import com.inrupt.client.solid.SolidSyncClient;

import java.net.URI;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final SolidSyncClient client = SolidSyncClient.getClient();

    public WebIdOwner getCurrentUser() {

        if (SecurityContextHolder.getContext() != null) {
            final Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            if (principal instanceof OidcUser) {
                final OidcUser user = (OidcUser) principal;
                final String webidUrl = user.getClaim("webid");
                try (final WebIdOwner profile = client.read(URI.create(webidUrl), WebIdOwner.class)) {
                    profile.setToken(user.getIdToken().getTokenValue());
                    return profile;
                } catch (OpenIdException e) {
                    throw new AuthNAuthZFailException(e.getMessage());
                }
            }
        }
        return null;
    }

}
