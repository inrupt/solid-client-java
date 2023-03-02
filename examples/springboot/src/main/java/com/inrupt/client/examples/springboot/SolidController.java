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
import com.inrupt.client.jena.JenaBodyPublishers;
import com.inrupt.client.openid.OpenIdSession;
import com.inrupt.client.solid.SolidContainer;
import com.inrupt.client.solid.SolidResource;
import com.inrupt.client.solid.SolidSyncClient;
import com.inrupt.client.webid.WebIdProfile;
import com.inrupt.client.vocabulary.LDP;
import com.inrupt.client.vocabulary.RDF;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class SolidController {
    final Model model = ModelFactory.createDefaultModel();

    private final Property P_TYPE = model.createProperty(RDF.type.toString());
    private final Literal O_RESOURCE = model.createLiteral(LDP.Resource.toString());
    private final Literal O_BASIC_CONTAINER = model.createLiteral(LDP.BasicContainer.toString());
    

    final SolidSyncClient client = SolidSyncClient.getClient();

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/oidc-principal")
    public String getOidcUserPrincipal(final @AuthenticationPrincipal OidcUser principal) {
        return principal.getIdToken().getTokenValue();
    }

    @GetMapping("/readPod")
    public List<URI> readFromPod(final @AuthenticationPrincipal OidcUser principal) {
        final var list = new ArrayList<URI>();
        final URI webid = URI.create(principal.getClaimAsString("webid"));
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

    /* @GetMapping("/writePod")//TODO make POST mapping
    public void writeToPod(final @AuthenticationPrincipal OidcUser principal) {
        final URI webid = URI.create(principal.getClaimAsString("webid"));
        final SolidSyncClient session =
                client.session(OpenIdSession.ofIdToken(principal.getIdToken().getTokenValue()));
        try (final WebIdProfile profile = session.read(webid, WebIdProfile.class)) {
            profile.getStorage().stream().findFirst().ifPresent(storage -> {
                final Request request = Request.newBuilder().uri(storage)
                        .header("Accept", "text/turtle").header("Slug", "test_resource")
                        .POST(Request.BodyPublishers.ofString("message")).build();

                session.send(request, Response.BodyHandlers.discarding());
            });
        }
    } */
    
    @GetMapping("/writePod")
    public void writeToPod(final @AuthenticationPrincipal OidcUser principal) {
        final URI webid = URI.create(principal.getClaimAsString("webid"));
        final SolidSyncClient session =
                client.session(OpenIdSession.ofIdToken(principal.getIdToken().getTokenValue()));
        try (final WebIdProfile profile = session.read(webid, WebIdProfile.class)) {
            profile.getStorage().stream().findFirst().ifPresent(storage -> {
                final String RESOURCE_NAME = "test_container";
                final Resource resource = model.createResource(storage+ RESOURCE_NAME);
                //model.add(resource, P_TYPE, O_RESOURCE);
                model.add(resource, P_TYPE, O_BASIC_CONTAINER);
                
                final Request request = Request.newBuilder().uri(storage)
                        .header("Accept", "text/turtle").header("Slug", RESOURCE_NAME)
                        .POST(JenaBodyPublishers.ofModel(model)).build();

                session.send(request, Response.BodyHandlers.discarding());
            });
        }
    }
}
