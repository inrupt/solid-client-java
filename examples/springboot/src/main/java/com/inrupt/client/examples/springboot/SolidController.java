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

import com.inrupt.client.Headers;
import com.inrupt.client.Request;
import com.inrupt.client.Response;
import com.inrupt.client.openid.OpenIdSession;
import com.inrupt.client.solid.SolidContainer;
import com.inrupt.client.solid.SolidResource;
import com.inrupt.client.solid.SolidSyncClient;
<<<<<<< HEAD
=======
import com.inrupt.client.util.URIBuilder;
>>>>>>> f88a7ddba54c95cf4fe388181f34e901fe243045
import com.inrupt.client.vocabulary.LDP;
import com.inrupt.client.webid.WebIdProfile;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
<<<<<<< HEAD
=======
import org.springframework.web.bind.annotation.PostMapping;
>>>>>>> f88a7ddba54c95cf4fe388181f34e901fe243045
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SolidController {

    final SolidSyncClient client = SolidSyncClient.getClient();
    static final String CONTENT_TYPE = "Content-Type";
    static final String IF_NONE_MATCH = "If-None-Match";
    static final String LINK = "Link";
    static final String REL_TYPE = "type";
    static final String SLUG = "Slug";
    static final String TEXT_TURTLE = "text/turtle";
    static final String WEB_ID = "webid";
    static final String WILDCARD = "*";

   /*  @GetMapping("/readPod")
    public List<URI> readFromPod() {
        final var principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        final var list = new ArrayList<URI>();
        if (principal instanceof OidcUser) {
            final OidcUser idToken = (OidcUser) principal;
            final URI webid = URI.create(idToken.getClaimAsString(WEB_ID));
            final SolidSyncClient session =
                client.session(OpenIdSession.ofIdToken(idToken.getIdToken().getTokenValue()));
            try (final WebIdProfile profile = session.read(webid, WebIdProfile.class)) {
                profile.getStorage().stream().findFirst().ifPresent(storage -> {
                try (final SolidContainer container = session.read(storage, SolidContainer.class)) {
                    container.getContainedResources().map(SolidResource::getIdentifier)
                            .forEach(list::add);
                }
            });
            }
        }
        return list;
    } */

    @GetMapping("/readPod")
    public List<URI> readFromPod(final @AuthenticationPrincipal OidcUser principal) {
        final var list = new ArrayList<URI>();
        final URI webid = URI.create(principal.getClaimAsString(WEB_ID));
        final SolidSyncClient session =
                client.session(OpenIdSession.ofIdToken(principal.getIdToken().getTokenValue()));

        try (final WebIdProfile profile = session.read(webid, WebIdProfile.class)) {
            profile.getStorage().stream().findFirst().ifPresent(storage -> {
                try (final SolidContainer container = session.read(storage, SolidContainer.class)) {
                    container.getContainedResources().map(SolidResource::getIdentifier)
                            .forEach(list::add);
                }
            });
        }
        return list;
    }

    @PostMapping("/writeNonRDF")
    public void writeNonRDF(final @AuthenticationPrincipal OidcUser principal) {
        final URI webid = URI.create(principal.getClaimAsString(WEB_ID));
        final SolidSyncClient session =
                client.session(OpenIdSession.ofIdToken(principal.getIdToken().getTokenValue()));
        try (final WebIdProfile profile = session.read(webid, WebIdProfile.class)) {
            profile.getStorage().stream().findFirst().ifPresent(storage -> {
                final Request request =
                        Request.newBuilder().uri(storage)
                            .header(SLUG, "test_nonRDF")
                            .header(CONTENT_TYPE, TEXT_TURTLE)
                            .header(IF_NONE_MATCH, WILDCARD)
                            .POST(Request.BodyPublishers.ofString("message"))
                            .build();

                session.send(request, Response.BodyHandlers.discarding());
            });
        }
    }

    @PostMapping("/writeContainer")
    public void writeContainer(final @AuthenticationPrincipal OidcUser principal) {
        final URI webid = URI.create(principal.getClaimAsString(WEB_ID));
        final SolidSyncClient session =
                client.session(OpenIdSession.ofIdToken(principal.getIdToken().getTokenValue()));
        try (final WebIdProfile profile = session.read(webid, WebIdProfile.class)) {
            profile.getStorage().stream().findFirst().ifPresent(storage -> {
                final String containerName = "test_container/";

                final Request request = Request.newBuilder()
                        .uri(storage)
                        .header(SLUG, containerName)
                        .header(CONTENT_TYPE, TEXT_TURTLE)
                        .header(IF_NONE_MATCH, WILDCARD)
                        .header(LINK, Headers.Link.of(LDP.BasicContainer, REL_TYPE).toString())
                        .POST(Request.BodyPublishers.noBody())
                        .build();
                session.send(request, Response.BodyHandlers.discarding());

                final Request request2 = Request.newBuilder()
                        .uri(URIBuilder.newBuilder(storage).path(containerName).build())
                        .header(SLUG, "test_contained_resource")
                        .header(CONTENT_TYPE, TEXT_TURTLE)
                        .header(IF_NONE_MATCH, WILDCARD)
                        .header(LINK, Headers.Link.of(LDP.RDFSource, REL_TYPE).toString())
                        .POST(Request.BodyPublishers.noBody())
                        .build();
                session.send(request2, Response.BodyHandlers.discarding());
            });
        }
    }

    @PostMapping("/writeResource")
    public void writeResource(final @AuthenticationPrincipal OidcUser principal) {
        final URI webid = URI.create(principal.getClaimAsString(WEB_ID));
        final SolidSyncClient session =
                client.session(OpenIdSession.ofIdToken(principal.getIdToken().getTokenValue()));
        try (final WebIdProfile profile = session.read(webid, WebIdProfile.class)) {
            profile.getStorage().stream().findFirst().ifPresent(storage -> {
                final Request request =
                        Request.newBuilder().uri(URIBuilder.newBuilder(storage).path("test_resource").build())
                        .header(CONTENT_TYPE, TEXT_TURTLE)
                        .header(LINK, Headers.Link.of(LDP.RDFSource, REL_TYPE).toString())
                        .PUT(Request.BodyPublishers.noBody()).build();
                session.send(request, Response.BodyHandlers.discarding());
            });
        }
    }
}
