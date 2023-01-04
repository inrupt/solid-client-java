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
package com.inrupt.client.webid;

import com.inrupt.client.*;
import com.inrupt.client.spi.RdfService;
import com.inrupt.client.spi.ServiceProvider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.apache.commons.rdf.api.Dataset;
import org.apache.commons.rdf.api.RDFSyntax;

/**
 * Body handlers for WebID Profiles.
 */
public final class WebIdBodyHandlers {

    private static final RdfService service = ServiceProvider.getRdfService();

    /**
     * Transform an HTTP response into a WebID Profile object.
     *
     * @param webid the WebID URI
     * @return an HTTP body handler
     */
    public static Response.BodyHandler<WebIdProfile> ofWebIdProfile(final URI webid) {
        return responseInfo -> {
            try (final InputStream input = new ByteArrayInputStream(responseInfo.body().array())) {
                final Dataset dataset = service.toDataset(RDFSyntax.TURTLE, input, responseInfo.uri().toString());
                return new WebIdProfile(webid, dataset);
            } catch (final IOException ex) {
                throw new WebIdException("Error processing WebId profile resource", ex);
            }
        };
    }

    private WebIdBodyHandlers() {
        // Prevent instantiation
    }
}
