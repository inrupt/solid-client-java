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
package com.inrupt.client.demo.quarkus;

import static org.apache.jena.rdf.model.ResourceFactory.createResource;
import static org.apache.jena.vocabulary.RDF.type;

import com.inrupt.client.Client;
import com.inrupt.client.ClientProvider;
import com.inrupt.client.Request;
import com.inrupt.client.jena.JenaBodyHandlers;
import com.inrupt.client.openid.OpenIdSession;
import com.inrupt.client.vocabulary.LDP;
import com.inrupt.client.webid.WebIdBodyHandlers;
import com.inrupt.client.webid.WebIdProfile;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;

import java.net.URI;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.jwt.JsonWebToken;

@ApplicationScoped
@Path("/solid")
public class SolidResource {

    final Client client = ClientProvider.getClient();

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
    public TemplateInstance solid() {
        return jwt.claim("webid").flatMap(claim -> {
            final var webid = URI.create((String) claim);
            final var req = Request.newBuilder(webid).header("Accept", "text/turtle").build();
            final var session = client.session(OpenIdSession.ofIdToken(jwt.getRawToken()));
            final var profile = session.send(req, WebIdBodyHandlers.ofWebIdProfile(webid)).body();

            return profile.getStorage().stream().findFirst().map(storage -> Request.newBuilder(storage).build())
                .map(request -> session.send(request, JenaBodyHandlers.ofModel()).body())
                .map(model -> {
                    final var containers = model
                        .listSubjectsWithProperty(type, createResource(LDP.BasicContainer.toString()))
                        .filterKeep(s -> s.isURIResource())
                        .mapWith(s -> s.getURI())
                        .toList();

                    final var resources = model
                        .listSubjectsWithProperty(type, createResource(LDP.RDFSource.toString()))
                        .filterKeep(s -> s.isURIResource())
                        .mapWith(s -> s.getURI())
                        .toList();

                    return Templates.profile(profile, containers, resources);
                });
        }).orElseGet(() -> Templates.profile(null, List.of(), List.of()));
    }
}
