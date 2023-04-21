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
package com.inrupt.client.examples.cli;

import static org.slf4j.LoggerFactory.getLogger;

import com.inrupt.client.Request;
import com.inrupt.client.Response;
import com.inrupt.client.openid.OpenIdSession;
import com.inrupt.client.solid.SolidContainer;
import com.inrupt.client.solid.SolidResource;
import com.inrupt.client.solid.SolidSyncClient;
import com.inrupt.client.webid.WebIdProfile;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

import jakarta.inject.Inject;

import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;
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

            final var client = SolidSyncClient.getClient().session(session);
            session.getPrincipal().ifPresent(webid -> {
                printWriter.format("WebID: %s", webid);
                printWriter.println();
                try (final var profile = client.read(webid, WebIdProfile.class)) {
                    profile.getStorage().stream().findFirst().ifPresent(storage -> {
                        printWriter.format("Storage %s ", storage);
                        printWriter.println();
                        try (final var container = client.read(storage, SolidContainer.class)) {
                            final var resources = container.getResources();
                            printWriter.format("Total number of contained resources is: %s ", resources.size());
                            printWriter.println();

                            resources.stream().filter(r -> filterResource(client, r, cmd)).forEach(r -> {
                                printWriter.format("Resource: %s", r.getIdentifier());
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

    boolean filterResource(final SolidSyncClient client, final SolidResource resource, final CommandLine cl) {
        if (cl.hasOption("c") && resource.getIdentifier().toString().endsWith("/")) {
            return true;
        }
        final var req = Request.newBuilder(resource.getIdentifier())
                .HEAD()
                .build();
        final var res = client.send(req, Response.BodyHandlers.discarding());
        final var contentType = res.headers().firstValue("Content-Type");
        if (cl.hasOption("r") && contentType.isPresent() &&
            (contentType.get().toLowerCase().contains("text/turtle")) ) {
            return true;
        }
        if (cl.hasOption("n") && contentType.isPresent() &&
            !(contentType.get().toLowerCase().contains("text/turtle"))) {
            return true;
        }
        return false;
    }

    void showHelp(final Options options) {
        final var formatter = new HelpFormatter();
        formatter.printHelp(printWriter, formatter.getWidth(), "java -jar inrupt-openid-jwk-runner.jar",
                null, options, formatter.getLeftPadding(), formatter.getDescPadding(), null, false);
    }

}
