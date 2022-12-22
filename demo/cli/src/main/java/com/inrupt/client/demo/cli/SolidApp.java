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
package com.inrupt.client.demo.cli;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.ECParameterSpec;
import java.util.Base64;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;

/**
 * A CLI utility for interacting with a Solid Storage.
 */
public class SolidApp {

    private static final Logger LOGGER = getLogger(SolidApp.class);

    // By default send output to stdout
    private PrintWriter printWriter = new PrintWriter(System.out, true);

    @Override
    public int run(final String... args) {
        final Options options = new Options();
        options.addOption("c", "client", true, "The client id");
        options.addOption("s", "secret", true, "The client secret");
        options.addOption("m", "method", true, "The authentication method [default: post]");
        options.addOption("i", "issuer", true, "The authentication server");
        options.addOption("h", "help", false, "Print this menu");

        final CommandLineParser parser = new DefaultParser();
        try {
            final CommandLine cmd = parser.parse(options, args);
            final String clientId = cmd.getOptionValue("c");
            final String clientSecret = cmd.getOptionValue("s");
            final String issuer = cmd.getOptionValue("i");
            final String method = cmd.getOptionValue("m", "post");

            if (cmd.hasOption("h")) {
                showHelp(options);
                return 0;
            }

        } catch (final ParseException ex) {
            LOGGER.error("Error parsing command line arguments: {}", ex.getMessage());
            showHelp(options);
            return 1;
        }
        return 0;
    }

    void setPrintWriter(final PrintWriter printWriter) {
        this.printWriter = printWriter;
    }

    void showHelp(final Options options) {
        final HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(printWriter, formatter.getWidth(), "java -jar inrupt-openid-jwk-runner.jar",
                null, options, formatter.getLeftPadding(), formatter.getDescPadding(), null, false);
    }

    static String getAlgorithm(final String type, final String algorithm) {
        if ("EC".equals(type)) {
            if (EC_ALGORITHMS.contains(algorithm)) {
                return algorithm;
            }
            LOGGER.warn("Invalid ECDSA algorithm, using ES256");
            return "ES256";
        } else {
            if (RSA_ALGORITHMS.contains(algorithm)) {
                return algorithm;
            }
            LOGGER.warn("Invalid RSA algorithm, using RS256");
            return "RS256";
        }
    }
}
