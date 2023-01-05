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
package com.inrupt.client.examples.quarkus;

import com.inrupt.client.openid.OpenIdSession;
import com.inrupt.client.solid.SolidClient;
import com.inrupt.client.solid.SolidContainer;
import com.inrupt.client.vocabulary.LDP;
import com.inrupt.client.webid.WebIdProfile;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.jwt.JsonWebToken;

@ApplicationScoped
@Path("/solid")
public class SolidStorage {

    final SolidClient client = SolidClient.getClient();

    @Inject
    JsonWebToken jwt;

    @CheckedTemplate
    static class Templates {
        private static native TemplateInstance profile(
                WebIdProfile profile,
                List<String> containers,
                List<String> resources);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public CompletionStage<TemplateInstance> solid() {
        return jwt.claim("webid").map(String.class::cast).map(URI::create).map(webid -> {
            final var session = client.session(OpenIdSession.ofIdToken(jwt.getRawToken()));
            return session.read(webid, WebIdProfile.class)
                .thenCompose(profile -> profile.getStorage().stream().findFirst().map(storage ->
                            session.read(storage, SolidContainer.class).thenApply(container -> {
                                try (container; final var stream = container.getContainedResources()) {
                                    final var resources = stream.collect(Collectors.groupingBy(c ->
                                                getPrincipalType(c.getMetadata().getType()), Collectors.mapping(c ->
                                                    c.getIdentifier().toString(), Collectors.toList())));
                                    return Templates.profile(profile, resources.get(LDP.BasicContainer),
                                            resources.get(LDP.RDFSource));
                                }
                            }))
                        .orElseGet(SolidStorage::emptyProfile)
                        .whenComplete((a, b) -> profile.close()));
        }).orElseGet(SolidStorage::emptyProfile);
    }

    static CompletionStage<TemplateInstance> emptyProfile() {
        return CompletableFuture.completedFuture(Templates.profile(null, List.of(), List.of()));
    }

    static URI getPrincipalType(final Collection<URI> types) {
        if (types.contains(LDP.BasicContainer)) {
            return LDP.BasicContainer;
        } else if (types.contains(LDP.RDFSource)) {
            return LDP.RDFSource;
        } else if (types.contains(LDP.NonRDFSource)) {
            return LDP.NonRDFSource;
        }
        return LDP.Resource;
    }
}
