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
package com.inrupt.client.examples.springboot;

import com.inrupt.client.Request;
import com.inrupt.client.Response;
import com.inrupt.client.openid.OpenIdSession;
import com.inrupt.client.solid.SolidClient;
import com.inrupt.client.solid.SolidContainer;
import com.inrupt.client.solid.SolidResource;
import com.inrupt.client.solid.SolidResourceHandlers;
import com.inrupt.client.vocabulary.LDP;
import com.inrupt.client.webid.WebIdProfile;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class SolidController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/oidc-principal")
    public String getOidcUserPrincipal(@AuthenticationPrincipal OidcUser principal) {
        return principal.getIdToken().getTokenValue();
    }

    @GetMapping("/solid")
    public List<URI> solid(@AuthenticationPrincipal OidcUser principal) {

        final URI webid = URI.create(principal.getClaimAsString("webid"));
        final SolidClient client = SolidClient.getClient().session(OpenIdSession.ofIdToken(principal.getIdToken().getTokenValue()));
        final Optional<URI> storage = client.read((webid), WebIdProfile.class)
                .toCompletableFuture()
                .join()
                .getStorage()
                .stream()
                .findFirst();

        if (storage.isPresent()) {
            final Request request =
                Request.newBuilder()
                .uri(storage.get())
                .header("Accept", "text/turtle")
                .GET()
                .build();

            final Response<SolidContainer> response =
                    client.send(request, SolidResourceHandlers.ofSolidContainer()).toCompletableFuture().join();
            final SolidContainer container = response.body();

            return container.getContainedResources()
                    .map(SolidResource::getIdentifier).toList();
        } else {
            return null;
        }
    }
}
