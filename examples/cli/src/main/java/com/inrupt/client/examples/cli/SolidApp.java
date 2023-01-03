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
package com.inrupt.client.examples.cli;

import static org.slf4j.LoggerFactory.getLogger;

import com.inrupt.client.openid.OpenIdSession;
import com.inrupt.client.solid.SolidClient;
import com.inrupt.client.solid.SolidContainer;
import com.inrupt.client.solid.SolidResource;
import com.inrupt.client.vocabulary.LDP;
import com.inrupt.client.webid.WebIdProfile;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

import java.io.PrintWriter;
import java.net.URI;
import java.util.Collection;

import javax.inject.Inject;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;

/**
 * A CLI utility for interacting with a Solid Storage.
 */
@QuarkusMain
public class SolidApp implements QuarkusApplication {

    private static final Logger LOGGER = getLogger(SolidApp.class);

    private final PrintWriter printWriter = new PrintWriter(System.out, true);

    @Inject
    AppConfig config;

    @Override
    public int run(final String... args) {
        final var options = new Options();
        options.addOption("c", "containers", false, "Show containers");
        options.addOption("r", "rdf", false, "Show RDF resources");
        options.addOption("n", "nonrdf", false, "Show non-RDF resources");
        options.addOption("h", "help", false, "Print this menu");

        final var parser = new DefaultParser();
        try {
            final var cmd = parser.parse(options, args);

            if (cmd.hasOption("h")) {
                showHelp(options);
                return 0;
            }

            final var session = OpenIdSession.ofClientCredentials(config.issuer(), config.clientId(),
                        config.clientSecret(), config.authMethod());

            final var client = SolidClient.getClient().session(session);
            session.getPrincipal().ifPresent(webid -> {
                printWriter.format("WebID: %s", webid);
                printWriter.println();
                try (final var profile = client.read(webid, WebIdProfile.class).toCompletableFuture().join()) {
                    profile.getStorage().stream().findFirst()
                        .map(storage -> client.read(storage, SolidContainer.class).toCompletableFuture().join())
                        .ifPresent(container -> {
                            try (container; final var stream = container.getContainedResources()) {
                                stream.filter(r -> filterResource(r, cmd.hasOption("c"), cmd.hasOption("r"),
                                            cmd.hasOption("n")))
                                    .forEach(r -> {
                                        printWriter.format("Resource: %s, %s", r.getIdentifier(),
                                            principalType(r.getMetadata().getType()));
                                        printWriter.println();
                                    });
                            }
                        });
                }
            });
        } catch (final ParseException ex) {
            LOGGER.error("Error parsing command line arguments: {}", ex.getMessage());
            showHelp(options);
            return 1;
        }
        return 0;
    }

    boolean filterResource(final SolidResource resource, final boolean c, final boolean r, final boolean n) {
        if (c && resource.getMetadata().getType().contains(LDP.BasicContainer)) {
            return true;
        }
        if (r && resource.getMetadata().getType().contains(LDP.RDFSource)) {
            return true;
        }
        return n && resource.getMetadata().getType().contains(LDP.NonRDFSource);
    }

    public URI principalType(final Collection<URI> types) {
        if (types.contains(LDP.BasicContainer)) {
            return LDP.BasicContainer;
        } else if (types.contains(LDP.RDFSource)) {
            return LDP.RDFSource;
        } else if (types.contains(LDP.NonRDFSource)) {
            return LDP.NonRDFSource;
        }
        return LDP.Resource;
    }

    void showHelp(final Options options) {
        final var formatter = new HelpFormatter();
        formatter.printHelp(printWriter, formatter.getWidth(), "java -jar inrupt-openid-jwk-runner.jar",
                null, options, formatter.getLeftPadding(), formatter.getDescPadding(), null, false);
    }

}
