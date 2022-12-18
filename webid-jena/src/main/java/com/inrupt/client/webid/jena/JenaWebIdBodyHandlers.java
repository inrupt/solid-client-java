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
package com.inrupt.client.webid.jena;

import com.inrupt.client.Response;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

public final class JenaWebIdBodyHandlers {

    public static Response.BodyHandler<JenaWebIdProfile> ofWebIdProfile() {
        return responseInfo -> {
            try (final var input = new ByteArrayInputStream(responseInfo.body().array())) {
                final var model = ModelFactory.createDefaultModel();
                RDFDataMgr.read(model, input, responseInfo.uri().toString(), Lang.TURTLE);

                return JenaWebIdProfile.wrap(model);

            } catch (final IOException ex) {
                throw new JenaWebIdException("Error loading WebID response stream", ex);
            }
        };
    }

    private JenaWebIdBodyHandlers() {
        // Prevent instantiation
    }
}
